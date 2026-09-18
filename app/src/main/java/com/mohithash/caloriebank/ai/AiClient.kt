package com.mohithash.caloriebank.ai

import com.mohithash.caloriebank.domain.AiProvider
import com.mohithash.caloriebank.domain.AiSettings
import com.mohithash.caloriebank.domain.FoodEstimate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Bring‑your‑own‑key calorie estimator. Talks raw HTTP so the APK stays small and the
 * key never leaves the device except to the provider the user chose.
 */
class AiClient(private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }) {

    class AiException(msg: String) : Exception(msg)

    private val systemPrompt = """
        You are a nutrition estimator for a calorie-tracking app. The user describes what they ate.
        Estimate each item's calories and macros using typical portion sizes when unspecified.
        Respond with JSON only, matching this shape exactly:
        {"items":[{"name":string,"calories":int,"protein_g":number,"carbs_g":number,"fat_g":number,"serving":string}],
         "total_calories":int,"confidence":"low"|"medium"|"high","note":string}
        total_calories must equal the sum of item calories. Keep note under 20 words.
    """.trimIndent()

    private val schema: JsonObject = buildJsonObject {
        put("type", "object")
        put("additionalProperties", false)
        put("required", buildJsonArray { add(JsonPrimitive("items")); add(JsonPrimitive("total_calories")); add(JsonPrimitive("confidence")); add(JsonPrimitive("note")) })
        putJsonObject("properties") {
            putJsonObject("items") {
                put("type", "array")
                putJsonObject("items") {
                    put("type", "object")
                    put("additionalProperties", false)
                    put("required", buildJsonArray { listOf("name", "calories", "protein_g", "carbs_g", "fat_g", "serving").forEach { add(JsonPrimitive(it)) } })
                    putJsonObject("properties") {
                        putJsonObject("name") { put("type", "string") }
                        putJsonObject("calories") { put("type", "integer") }
                        putJsonObject("protein_g") { put("type", "number") }
                        putJsonObject("carbs_g") { put("type", "number") }
                        putJsonObject("fat_g") { put("type", "number") }
                        putJsonObject("serving") { put("type", "string") }
                    }
                }
            }
            putJsonObject("total_calories") { put("type", "integer") }
            putJsonObject("confidence") { put("type", "string") }
            putJsonObject("note") { put("type", "string") }
        }
    }

    /** [imageJpegBase64] is an optional photo of the meal; the text may then be empty. */
    suspend fun estimate(settings: AiSettings, description: String, imageJpegBase64: String? = null): FoodEstimate = withContext(Dispatchers.IO) {
        if (!settings.configured) throw AiException("Add your API key in Settings first.")
        val prompt = description.ifBlank { "Estimate everything visible in this photo." }
        val text = when (settings.provider) {
            AiProvider.ANTHROPIC -> callAnthropic(settings, prompt, imageJpegBase64)
            AiProvider.OPENAI_COMPAT -> callOpenAi(settings, prompt, imageJpegBase64)
        }
        parse(text)
    }

    private fun callAnthropic(s: AiSettings, description: String, image: String?): String {
        val body = buildJsonObject {
            put("model", s.effectiveModel)
            put("max_tokens", 4096)
            put("system", systemPrompt)
            putJsonObject("output_config") {
                put("effort", "low")
                putJsonObject("format") { put("type", "json_schema"); put("schema", schema) }
            }
            put("messages", buildJsonArray {
                add(buildJsonObject {
                    put("role", "user")
                    put("content", buildJsonArray {
                        if (image != null) add(buildJsonObject {
                            put("type", "image")
                            putJsonObject("source") { put("type", "base64"); put("media_type", "image/jpeg"); put("data", image) }
                        })
                        add(buildJsonObject { put("type", "text"); put("text", description) })
                    })
                })
            })
        }
        val resp = post("${s.effectiveBaseUrl}/v1/messages", body.toString(), mapOf(
            "x-api-key" to s.apiKey, "anthropic-version" to "2023-06-01"))
        val obj = json.parseToJsonElement(resp).jsonObject
        obj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content?.let { throw AiException(it) }
        if (obj["stop_reason"]?.jsonPrimitive?.content == "refusal") throw AiException("The model declined this request.")
        return obj["content"]?.jsonArray
            ?.firstOrNull { it.jsonObject["type"]?.jsonPrimitive?.content == "text" }
            ?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: throw AiException("Empty response from model.")
    }

    private fun callOpenAi(s: AiSettings, description: String, image: String?): String {
        val body = buildJsonObject {
            put("model", s.effectiveModel)
            putJsonObject("response_format") { put("type", "json_object") }
            put("messages", buildJsonArray {
                add(buildJsonObject { put("role", "system"); put("content", systemPrompt) })
                add(buildJsonObject {
                    put("role", "user")
                    put("content", buildJsonArray {
                        add(buildJsonObject { put("type", "text"); put("text", description) })
                        if (image != null) add(buildJsonObject {
                            put("type", "image_url")
                            putJsonObject("image_url") { put("url", "data:image/jpeg;base64,$image") }
                        })
                    })
                })
            })
        }
        val resp = post("${s.effectiveBaseUrl}/v1/chat/completions", body.toString(), mapOf(
            "Authorization" to "Bearer ${s.apiKey}"))
        val obj = json.parseToJsonElement(resp).jsonObject
        obj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content?.let { throw AiException(it) }
        return obj["choices"]?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content
            ?: throw AiException("Empty response from model.")
    }

    private fun parse(text: String): FoodEstimate {
        val start = text.indexOf('{'); val end = text.lastIndexOf('}')
        if (start < 0 || end <= start) throw AiException("Model did not return JSON.")
        val est = json.decodeFromString<FoodEstimate>(text.substring(start, end + 1))
        val sum = est.items.sumOf { it.calories }
        return if (est.items.isNotEmpty() && sum != est.total_calories) est.copy(total_calories = sum) else est
    }

    private fun post(url: String, body: String, headers: Map<String, String>): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
        }
        try {
            conn.outputStream.use { it.write(body.toByteArray()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.readText().orEmpty()
            if (code !in 200..299) {
                val msg = runCatching { json.parseToJsonElement(text).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content }.getOrNull()
                throw AiException(msg ?: "HTTP $code: ${text.take(200)}")
            }
            return text
        } catch (e: java.io.IOException) {
            throw AiException("Network error: ${e.message}")
        } finally { conn.disconnect() }
    }
}

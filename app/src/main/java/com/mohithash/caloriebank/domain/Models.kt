package com.mohithash.caloriebank.domain

import kotlinx.serialization.Serializable

enum class Sex { MALE, FEMALE }

enum class Activity(val label: String, val factor: Double, val hint: String) {
    SEDENTARY("Sedentary", 1.2, "Desk job, little exercise"),
    LIGHT("Light", 1.375, "Exercise 1–3 days/week"),
    MODERATE("Moderate", 1.55, "Exercise 3–5 days/week"),
    ACTIVE("Active", 1.725, "Hard exercise 6–7 days/week"),
    ATHLETE("Athlete", 1.9, "Physical job or 2× daily training"),
}

@Serializable
data class Profile(
    val name: String = "",
    val sex: Sex = Sex.MALE,
    val age: Int = 30,
    val heightCm: Double = 170.0,
    val weightKg: Double = 70.0,
    val goalWeightKg: Double = 65.0,
    val activity: Activity = Activity.LIGHT,
    /** Planned kcal below TDEE per day; drives the suggested budget and the ETA estimate. */
    val plannedDeficit: Int = 500,
    val onboarded: Boolean = false,
)

enum class AiProvider(val label: String, val defaultModel: String, val defaultBaseUrl: String) {
    ANTHROPIC("Anthropic (Claude)", "claude-opus-5", "https://api.anthropic.com"),
    OPENAI_COMPAT("OpenAI‑compatible", "gpt-4o-mini", "https://api.openai.com"),
}

@Serializable
data class AiSettings(
    val provider: AiProvider = AiProvider.ANTHROPIC,
    val apiKey: String = "",
    val model: String = "",
    val baseUrl: String = "",
) {
    val effectiveModel get() = model.ifBlank { provider.defaultModel }
    val effectiveBaseUrl get() = baseUrl.ifBlank { provider.defaultBaseUrl }.trimEnd('/')
    val configured get() = apiKey.isNotBlank()
}

@Serializable
data class FoodItem(
    val name: String,
    val calories: Int,
    val protein_g: Double = 0.0,
    val carbs_g: Double = 0.0,
    val fat_g: Double = 0.0,
    val serving: String = "",
)

@Serializable
data class FoodEstimate(
    val items: List<FoodItem> = emptyList(),
    val total_calories: Int = 0,
    val confidence: String = "",
    val note: String = "",
)

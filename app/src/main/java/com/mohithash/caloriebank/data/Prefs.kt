package com.mohithash.caloriebank.data

import android.content.Context
import com.mohithash.caloriebank.domain.AiSettings
import com.mohithash.caloriebank.domain.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("caloriebank", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _profile = MutableStateFlow(load("profile", Profile()))
    val profile: StateFlow<Profile> = _profile
    private val _ai = MutableStateFlow(load("ai", AiSettings()))
    val ai: StateFlow<AiSettings> = _ai

    fun saveProfile(p: Profile) { _profile.value = p; sp.edit().putString("profile", json.encodeToString(p)).apply() }
    fun saveAi(a: AiSettings) { _ai.value = a; sp.edit().putString("ai", json.encodeToString(a)).apply() }

    private inline fun <reified T> load(key: String, default: T): T =
        sp.getString(key, null)?.let { runCatching { json.decodeFromString<T>(it) }.getOrNull() } ?: default
}

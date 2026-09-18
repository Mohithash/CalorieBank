package com.mohithash.caloriebank.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohithash.caloriebank.ai.AiClient
import com.mohithash.caloriebank.data.DayTotal
import com.mohithash.caloriebank.data.Repository
import com.mohithash.caloriebank.data.Tx
import com.mohithash.caloriebank.data.TxKind
import com.mohithash.caloriebank.data.WaterTx
import com.mohithash.caloriebank.domain.AiSettings
import com.mohithash.caloriebank.domain.Calc
import com.mohithash.caloriebank.domain.FoodEstimate
import com.mohithash.caloriebank.domain.Profile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

data class BankState(
    val profile: Profile = Profile(),
    val balance: Int = 0,
    val todayIntake: Int = 0,
    val todayEntries: List<Tx> = emptyList(),
    val recentSettlements: List<Tx> = emptyList(),
) {
    val tdee get() = Calc.tdee(profile).roundToInt()
    val bmr get() = Calc.bmr(profile).roundToInt()
    val bmi get() = Calc.bmi(profile)
    val budget get() = Calc.dailyBudget(profile)
    val opening get() = Calc.openingBalance(profile).roundToInt()
    /** What today would settle at if it closed now. */
    val todayNet get() = todayIntake - tdee
    val budgetLeft get() = budget - todayIntake
    /** Share of the journey completed, 0..1. */
    val progress: Float get() = if (opening <= 0) 1f else (1f - balance.toFloat() / opening).coerceIn(0f, 1f)
    val avgRecentNet: Double? get() = recentSettlements.takeIf { it.isNotEmpty() }?.map { it.amount }?.average()
    val daysAtPace: Int? get() = avgRecentNet?.let { Calc.daysToZero(balance.toDouble(), it) }
    val daysAtPlan: Int? get() = Calc.daysToZero(balance.toDouble(), -profile.plannedDeficit.toDouble())
    fun eta(days: Int?): LocalDate? = days?.let { LocalDate.now().plusDays(it.toLong()) }
    val kgLeft get() = balance / 7700.0
}

data class WaterState(
    val goalMl: Int = 2500,
    val today: List<WaterTx> = emptyList(),
    val recent: List<DayTotal> = emptyList(),
) {
    val todayMl get() = today.sumOf { it.ml }
    val progress get() = (todayMl.toFloat() / goalMl).coerceIn(0f, 1f)
}

sealed interface AiUi {
    data object Idle : AiUi
    data object Loading : AiUi
    data class Result(val estimate: FoodEstimate, val query: String) : AiUi
    data class Error(val message: String) : AiUi
}

class AppViewModel(private val repo: Repository, private val ai: AiClient) : ViewModel() {
    val profile: StateFlow<Profile> = repo.prefs.profile
    val aiSettings: StateFlow<AiSettings> = repo.prefs.ai

    val bank: StateFlow<BankState> = combine(
        profile, repo.balance, repo.todayEntries(), repo.recentSettlements
    ) { p, bal, entries, settled ->
        BankState(p, bal, entries.sumOf { it.amount }, entries, settled)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BankState())

    val water: StateFlow<WaterState> = combine(profile, repo.todayWater(), repo.recentWater) { p, today, recent ->
        WaterState(Calc.waterGoalMl(p), today, recent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WaterState())

    val ledger: StateFlow<List<Tx>> = repo.allTx.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Running balance after each bank posting, oldest first — feeds the trend chart. */
    val balanceSeries: StateFlow<List<Pair<String, Int>>> = repo.allTx.map { all ->
        var run = 0
        all.filter { it.kind == TxKind.SETTLE || it.kind == TxKind.ADJUST }
            .sortedBy { it.timestamp }
            .map { run += it.amount; it.date to run }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Most recently logged distinct foods, for one-tap re-logging. */
    val recentFoods: StateFlow<List<Tx>> = repo.allTx.map { all ->
        all.filter { it.kind == TxKind.FOOD }.distinctBy { it.title.lowercase() }.take(8)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _aiUi = MutableStateFlow<AiUi>(AiUi.Idle)
    val aiUi: StateFlow<AiUi> = _aiUi

    init { viewModelScope.launch { repo.settlePastDays() } }

    fun onResume() = viewModelScope.launch { repo.settlePastDays() }

    fun openAccount(p: Profile) = viewModelScope.launch { repo.openAccount(p) }
    fun updateProfile(p: Profile) = viewModelScope.launch { repo.updateProfile(p, bank.value.balance) }
    fun saveAi(a: AiSettings) = repo.prefs.saveAi(a)

    fun addFood(name: String, kcal: Int, note: String = "") = viewModelScope.launch { repo.addFood(name, kcal, note) }
    fun addExercise(name: String, kcal: Int) = viewModelScope.launch { repo.addExercise(name, kcal) }
    fun deleteTx(id: Long) = viewModelScope.launch { repo.delete(id) }
    fun addWater(ml: Int) = viewModelScope.launch { repo.addWater(ml) }
    fun deleteWater(id: Long) = viewModelScope.launch { repo.deleteWater(id) }

    fun estimate(description: String, imageJpegBase64: String? = null) {
        if (description.isBlank() && imageJpegBase64 == null) return
        _aiUi.value = AiUi.Loading
        viewModelScope.launch {
            _aiUi.value = try {
                AiUi.Result(ai.estimate(aiSettings.value, description, imageJpegBase64), description)
            } catch (e: Exception) {
                AiUi.Error(e.message ?: "Estimate failed")
            }
        }
    }

    /** Commit an AI estimate to today's ledger, one entry per item. */
    fun acceptEstimate(est: FoodEstimate) = viewModelScope.launch {
        est.items.forEach { repo.addFood(it.name, it.calories, it.serving) }
        _aiUi.value = AiUi.Idle
    }

    fun clearAi() { _aiUi.value = AiUi.Idle }

    /** Round-trip check for the Settings screen. */
    suspend fun testAi(settings: AiSettings): Result<FoodEstimate> = runCatching { ai.estimate(settings, "one banana") }
}

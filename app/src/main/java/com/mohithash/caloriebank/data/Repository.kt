package com.mohithash.caloriebank.data

import com.mohithash.caloriebank.domain.Calc
import com.mohithash.caloriebank.domain.Profile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.LocalDate
import kotlin.math.roundToInt

class Repository(private val db: AppDb, val prefs: Prefs) {
    val txDao get() = db.tx()
    val waterDao get() = db.water()

    fun today(): String = LocalDate.now().toString()

    val balance: Flow<Int> = txDao.balance()
    val allTx: Flow<List<Tx>> = txDao.all()
    fun todayEntries(): Flow<List<Tx>> = txDao.dayEntries(today())
    fun todayWater(): Flow<List<WaterTx>> = waterDao.day(today())
    val recentSettlements: Flow<List<Tx>> = txDao.recentSettlements(7)
    val recentWater: Flow<List<DayTotal>> = waterDao.recentTotals(7)

    suspend fun addFood(name: String, kcal: Int, note: String = "") =
        txDao.insert(Tx(kind = TxKind.FOOD, date = today(), timestamp = now(), title = name, amount = kcal, note = note))

    suspend fun addExercise(name: String, kcal: Int) =
        txDao.insert(Tx(kind = TxKind.EXERCISE, date = today(), timestamp = now(), title = name, amount = -kcal))

    suspend fun delete(id: Long) = txDao.delete(id)

    suspend fun addWater(ml: Int) = waterDao.insert(WaterTx(date = today(), timestamp = now(), ml = ml))
    suspend fun deleteWater(id: Long) = waterDao.delete(id)

    /** First‑run: record the stored surplus as the opening balance. */
    suspend fun openAccount(p: Profile) {
        prefs.saveProfile(p.copy(onboarded = true))
        txDao.insert(Tx(kind = TxKind.ADJUST, date = today(), timestamp = now(),
            title = "Account opened", amount = Calc.openingBalance(p).roundToInt(),
            note = "${fmt(p.weightKg)} kg → ${fmt(p.goalWeightKg)} kg goal"))
    }

    /**
     * Profile edits after onboarding. A weight or goal change re-prices the bank:
     * the balance is corrected so that it again equals (weight − goal) × 7700.
     */
    suspend fun updateProfile(p: Profile, currentBalance: Int) {
        val old = prefs.profile.value
        prefs.saveProfile(p.copy(onboarded = true))
        val changed = old.weightKg != p.weightKg || old.goalWeightKg != p.goalWeightKg
        if (changed) {
            val target = Calc.openingBalance(p).roundToInt()
            val delta = target - currentBalance
            if (delta != 0) txDao.insert(Tx(kind = TxKind.ADJUST, date = today(), timestamp = now(),
                title = if (old.weightKg != p.weightKg) "Weigh‑in ${fmt(p.weightKg)} kg" else "Goal → ${fmt(p.goalWeightKg)} kg",
                amount = delta, note = "Balance re-priced to ${target} kcal"))
        }
    }

    /** Close every past day that has entries but no settlement yet: net = intake − TDEE. */
    suspend fun settlePastDays() {
        val p = prefs.profile.value
        if (!p.onboarded) return
        val tdee = Calc.tdee(p).roundToInt()
        for (d in txDao.unsettledDays(today())) {
            val intake = txDao.netForDay(d)
            val net = intake - tdee
            txDao.insert(Tx(kind = TxKind.SETTLE, date = d,
                timestamp = LocalDate.parse(d).plusDays(1).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                title = "Day closed $d", amount = net,
                note = "Ate $intake − burned $tdee"))
        }
    }

    private fun now() = System.currentTimeMillis()
    private fun fmt(d: Double) = if (d % 1.0 == 0.0) d.toInt().toString() else String.format("%.1f", d)
}

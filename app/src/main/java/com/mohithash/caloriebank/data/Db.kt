package com.mohithash.caloriebank.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Ledger kinds.
 *  FOOD / EXERCISE — today's running intake (kcal in, kcal out). They never touch the bank
 *  directly; they are summed and settled into one SETTLE entry when the day closes.
 *  SETTLE  — a closed day's net (intake − TDEE). Negative reduces the balance.
 *  ADJUST  — opening balance and weight‑change corrections.
 */
enum class TxKind { FOOD, EXERCISE, SETTLE, ADJUST }

@Entity(tableName = "tx")
data class Tx(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: TxKind,
    /** ISO date (yyyy-MM-dd) the entry belongs to. */
    val date: String,
    val timestamp: Long,
    val title: String,
    /** kcal. For FOOD positive, EXERCISE negative, SETTLE/ADJUST signed bank delta. */
    val amount: Int,
    val note: String = "",
)

@Entity(tableName = "water")
data class WaterTx(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val timestamp: Long,
    val ml: Int,
)

@Dao
interface TxDao {
    @Insert suspend fun insert(tx: Tx): Long
    @Query("DELETE FROM tx WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM tx ORDER BY timestamp DESC") fun all(): Flow<List<Tx>>
    @Query("SELECT * FROM tx WHERE date = :date AND kind IN ('FOOD','EXERCISE') ORDER BY timestamp DESC")
    fun dayEntries(date: String): Flow<List<Tx>>
    @Query("SELECT COALESCE(SUM(amount),0) FROM tx WHERE kind IN ('SETTLE','ADJUST')")
    fun balance(): Flow<Int>
    @Query("SELECT * FROM tx WHERE kind = 'SETTLE' ORDER BY date DESC LIMIT :n")
    fun recentSettlements(n: Int): Flow<List<Tx>>
    @Query("SELECT DISTINCT date FROM tx WHERE kind IN ('FOOD','EXERCISE') AND date < :today AND date NOT IN (SELECT date FROM tx WHERE kind = 'SETTLE')")
    suspend fun unsettledDays(today: String): List<String>
    @Query("SELECT COALESCE(SUM(amount),0) FROM tx WHERE date = :date AND kind IN ('FOOD','EXERCISE')")
    suspend fun netForDay(date: String): Int
}

@Dao
interface WaterDao {
    @Insert suspend fun insert(w: WaterTx)
    @Query("DELETE FROM water WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM water WHERE date = :date ORDER BY timestamp DESC") fun day(date: String): Flow<List<WaterTx>>
    @Query("SELECT date, SUM(ml) AS ml FROM water GROUP BY date ORDER BY date DESC LIMIT :n")
    fun recentTotals(n: Int): Flow<List<DayTotal>>
}

data class DayTotal(val date: String, val ml: Int)

@Database(entities = [Tx::class, WaterTx::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun tx(): TxDao
    abstract fun water(): WaterDao
}

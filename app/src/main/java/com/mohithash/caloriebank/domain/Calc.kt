package com.mohithash.caloriebank.domain

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/** 1 kg of body fat ≈ 7 700 kcal — the exchange rate of the bank. */
const val KCAL_PER_KG = 7700.0

object Calc {
    /** Mifflin–St Jeor resting energy expenditure. */
    fun bmr(p: Profile): Double {
        val base = 10 * p.weightKg + 6.25 * p.heightCm - 5 * p.age
        return if (p.sex == Sex.MALE) base + 5 else base - 161
    }

    fun tdee(p: Profile): Double = bmr(p) * p.activity.factor

    fun bmi(p: Profile): Double {
        val m = p.heightCm / 100.0
        return p.weightKg / (m * m)
    }

    fun bmiCategory(bmi: Double) = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25.0 -> "Healthy"
        bmi < 30.0 -> "Overweight"
        else -> "Obese"
    }

    /** Upper edge of the healthy BMI band for this height — the default savings target. */
    fun healthyMaxWeight(heightCm: Double): Double {
        val m = heightCm / 100.0
        return 24.9 * m * m
    }

    /** Stored surplus energy that has to be "spent" to reach goal weight. */
    fun openingBalance(p: Profile): Double = (p.weightKg - p.goalWeightKg) * KCAL_PER_KG

    /** Suggested daily intake: TDEE minus the planned deficit, floored at a safe minimum. */
    fun dailyBudget(p: Profile): Int {
        val floor = if (p.sex == Sex.MALE) 1500 else 1200
        return maxOf(floor, (tdee(p) - p.plannedDeficit).roundToInt())
    }

    /** Daily water need: ~35 ml/kg, plus a bump for very active people. */
    fun waterGoalMl(p: Profile): Int {
        val base = p.weightKg * 35
        val bonus = if (p.activity.factor >= Activity.ACTIVE.factor) 500 else if (p.activity == Activity.MODERATE) 250 else 0
        return ((base + bonus) / 50).roundToInt() * 50
    }

    /**
     * Days until the balance hits zero at [avgDailyNet] kcal/day (negative = burning).
     * Returns null when the trend does not move the balance toward zero.
     */
    fun daysToZero(balance: Double, avgDailyNet: Double): Int? {
        if (balance <= 0) return 0
        if (avgDailyNet >= -1.0) return null
        return ceil(balance / abs(avgDailyNet)).toInt()
    }
}

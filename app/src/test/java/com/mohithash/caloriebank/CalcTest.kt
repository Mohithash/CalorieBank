package com.mohithash.caloriebank

import com.mohithash.caloriebank.domain.Activity
import com.mohithash.caloriebank.domain.Calc
import com.mohithash.caloriebank.domain.Profile
import com.mohithash.caloriebank.domain.Sex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalcTest {
    private val p = Profile(sex = Sex.MALE, age = 30, heightCm = 175.0, weightKg = 80.0, goalWeightKg = 72.0, activity = Activity.LIGHT, plannedDeficit = 500)

    @Test fun bmrMifflin() = assertEquals(1748.75, Calc.bmr(p), 0.01)
    @Test fun tdee() = assertEquals(1748.75 * 1.375, Calc.tdee(p), 0.01)
    @Test fun bmi() = assertEquals(26.12, Calc.bmi(p), 0.01)
    @Test fun openingBalanceIs8kgOfFat() = assertEquals(61600.0, Calc.openingBalance(p), 0.0)
    @Test fun budgetIsTdeeMinusDeficit() = assertEquals(1905, Calc.dailyBudget(p))
    @Test fun budgetNeverBelowFloor() = assertEquals(1200, Calc.dailyBudget(p.copy(sex = Sex.FEMALE, weightKg = 45.0, plannedDeficit = 1000)))
    @Test fun waterGoal() = assertEquals(2800, Calc.waterGoalMl(p))
    @Test fun daysToZeroAtDeficit() = assertEquals(124, Calc.daysToZero(61600.0, -500.0))
    @Test fun daysToZeroWhenGaining() = assertNull(Calc.daysToZero(61600.0, 200.0))
    @Test fun daysToZeroWhenDone() = assertEquals(0, Calc.daysToZero(-10.0, -500.0))
}

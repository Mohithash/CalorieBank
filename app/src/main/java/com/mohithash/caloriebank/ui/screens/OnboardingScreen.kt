@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.domain.Calc
import com.mohithash.caloriebank.domain.Profile
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.graphics.Color
import com.mohithash.caloriebank.ui.AnimatedNumber
import com.mohithash.caloriebank.ui.HeroCard
import com.mohithash.caloriebank.ui.KeyValue
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.kcal
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(onOpen: (Profile) -> Unit) {
    var draft by remember { mutableStateOf<Profile?>(Profile()) }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Open your Calorie Bank") },
                subtitle = { Text("Every kilo above goal is stored energy. Spend it down to zero.") },
                scrollBehavior = scroll,
            )
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ProfileForm(Profile()) { draft = it }

            draft?.let { p ->
                val cs = MaterialTheme.colorScheme
                HeroCard(colors = listOf(cs.primary, Color(0xFF0B4D3E)), blobShape = MaterialShapes.Cookie12Sided) {
                    val on = cs.onPrimary
                    Label("Your account preview", on.copy(alpha = 0.8f))
                    AnimatedNumber(Calc.openingBalance(p).roundToInt(), MaterialTheme.typography.displaySmall, on)
                    Text("kcal opening balance to burn", style = MaterialTheme.typography.bodyMedium, color = on.copy(alpha = 0.85f))
                    Spacer(Modifier.height(6.dp))
                    val v = on.copy(alpha = 0.9f)
                    KeyValue("BMI", String.format("%.1f · %s", Calc.bmi(p), Calc.bmiCategory(Calc.bmi(p))), v)
                    KeyValue("BMR (resting)", Calc.bmr(p).roundToInt().kcal(), v)
                    KeyValue("TDEE (daily burn)", Calc.tdee(p).roundToInt().kcal(), v)
                    KeyValue("Suggested daily budget", Calc.dailyBudget(p).kcal(), v)
                    KeyValue("Water goal", "${Calc.waterGoalMl(p)} ml", v)
                    Calc.daysToZero(Calc.openingBalance(p), -p.plannedDeficit.toDouble())?.let { KeyValue("Reach zero in", "$it days", v) }
                }
            }

            Button(
                onClick = { draft?.let(onOpen) }, enabled = draft != null,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) { Text("Open account", style = MaterialTheme.typography.titleMedium) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

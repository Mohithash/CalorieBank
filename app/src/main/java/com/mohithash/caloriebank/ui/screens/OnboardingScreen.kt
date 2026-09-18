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
                StatCard(container = MaterialTheme.colorScheme.primaryContainer) {
                    Label("Your account preview")
                    Text(Calc.openingBalance(p).roundToInt().kcal(), style = MaterialTheme.typography.displaySmall)
                    Text("opening balance to burn", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(4.dp))
                    KeyValue("BMI", String.format("%.1f · %s", Calc.bmi(p), Calc.bmiCategory(Calc.bmi(p))))
                    KeyValue("BMR (resting)", Calc.bmr(p).roundToInt().kcal())
                    KeyValue("TDEE (daily burn)", Calc.tdee(p).roundToInt().kcal())
                    KeyValue("Suggested daily budget", Calc.dailyBudget(p).kcal())
                    KeyValue("Water goal", "${Calc.waterGoalMl(p)} ml")
                    Calc.daysToZero(Calc.openingBalance(p), -p.plannedDeficit.toDouble())?.let {
                        KeyValue("Reach zero in", "$it days")
                    }
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

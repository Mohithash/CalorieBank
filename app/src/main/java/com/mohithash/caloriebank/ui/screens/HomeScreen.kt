@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.domain.Calc
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.KeyValue
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.kcal
import com.mohithash.caloriebank.ui.kg1
import com.mohithash.caloriebank.ui.pretty
import com.mohithash.caloriebank.ui.signedKcal
import java.util.Locale
import kotlin.math.abs

@Composable
fun HomeScreen(vm: AppViewModel, onLog: () -> Unit, onWater: () -> Unit, onSettings: () -> Unit) {
    val s by vm.bank.collectAsState()
    val w by vm.water.collectAsState()
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val greeting = if (s.profile.name.isBlank()) "Your balance" else "${s.profile.name}'s balance"

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(greeting) },
                subtitle = { Text(java.time.LocalDate.now().pretty()) },
                actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, "Settings") } },
                scrollBehavior = scroll,
            )
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Balance hero ─────────────────────────────────────────────
            StatCard(container = MaterialTheme.colorScheme.primaryContainer) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularWavyProgressIndicator(
                            progress = { s.progress },
                            modifier = Modifier.size(112.dp),
                            stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke,
                        )
                        Text("${(s.progress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge)
                    }
                    Column {
                        Label("Balance to burn")
                        Text(String.format(Locale.US, "%,d", s.balance), style = MaterialTheme.typography.displayMedium)
                        Text("kcal  ·  ${s.kgLeft.kg1()} to goal", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = onSettings, label = { Text(String.format(Locale.US, "BMI %.1f · %s", s.bmi, Calc.bmiCategory(s.bmi))) })
                    AssistChip(onClick = onSettings, label = { Text("${s.profile.weightKg.kg1()} → ${s.profile.goalWeightKg.kg1()}") })
                }
            }

            // ── Today ────────────────────────────────────────────────────
            StatCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Label("Today")
                        Text(
                            if (s.budgetLeft >= 0) "${s.budgetLeft.kcal()} left in budget" else "${abs(s.budgetLeft).kcal()} over budget",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (s.budgetLeft >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error,
                        )
                    }
                    FilledTonalButton(onClick = onLog, shapes = ButtonDefaults.shapes()) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.size(6.dp)); Text("Log")
                    }
                }
                LinearWavyProgressIndicator(
                    progress = { (s.todayIntake.toFloat() / s.budget).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                )
                KeyValue("Eaten", s.todayIntake.kcal())
                KeyValue("Budget (TDEE − ${s.profile.plannedDeficit})", s.budget.kcal())
                KeyValue("Body burns (TDEE)", s.tdee.kcal())
                KeyValue("If the day closed now", "${s.todayNet.signedKcal()} kcal to balance")
                Text("Balance is settled once a day: eaten − TDEE. Eat under TDEE and the balance drops.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // ── ETA ──────────────────────────────────────────────────────
            StatCard(container = MaterialTheme.colorScheme.secondaryContainer) {
                Label("Estimated zero date")
                val pace = s.daysAtPace; val plan = s.daysAtPlan
                if (s.balance <= 0) {
                    Text("You're there. 🎉", style = MaterialTheme.typography.headlineMedium)
                    Text("Balance is at or below zero. Update your weight in Settings to re-price the account.")
                } else {
                    when {
                        pace != null -> {
                            Text(s.eta(pace)!!.pretty(), style = MaterialTheme.typography.headlineMedium)
                            Text("$pace days at your 7‑day pace (${s.avgRecentNet!!.toInt().signedKcal()} kcal/day)")
                        }
                        s.avgRecentNet != null -> {
                            Text("Not shrinking yet", style = MaterialTheme.typography.headlineMedium)
                            Text("7‑day average is ${s.avgRecentNet!!.toInt().signedKcal()} kcal/day — above your burn.")
                        }
                        else -> {
                            Text("Log a few days", style = MaterialTheme.typography.headlineMedium)
                            Text("The pace estimate appears after your first closed day.")
                        }
                    }
                    plan?.let { KeyValue("On plan (−${s.profile.plannedDeficit}/day)", "${s.eta(it)!!.pretty()} · $it d") }
                }
            }

            // ── Water ────────────────────────────────────────────────────
            StatCard(container = MaterialTheme.colorScheme.tertiaryContainer) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Label("Water")
                        Text("${w.todayMl} / ${w.goalMl} ml", style = MaterialTheme.typography.titleLarge)
                    }
                    FilledTonalButton(onClick = { vm.addWater(250) }, shapes = ButtonDefaults.shapes()) {
                        Icon(Icons.Default.WaterDrop, null); Spacer(Modifier.size(6.dp)); Text("+250")
                    }
                }
                LinearWavyProgressIndicator(progress = { w.progress }, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

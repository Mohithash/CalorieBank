@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.domain.Calc
import com.mohithash.caloriebank.ui.AnimatedNumber
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.HeroCard
import com.mohithash.caloriebank.ui.KeyValue
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.ShapeIcon
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.TrendChart
import com.mohithash.caloriebank.ui.animatedProgress
import com.mohithash.caloriebank.ui.kcal
import com.mohithash.caloriebank.ui.kg1
import com.mohithash.caloriebank.ui.pretty
import com.mohithash.caloriebank.ui.signedKcal
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

@Composable
fun HomeScreen(vm: AppViewModel, onLog: () -> Unit, onWater: () -> Unit, onSettings: () -> Unit) {
    val s by vm.bank.collectAsState()
    val w by vm.water.collectAsState()
    val series by vm.balanceSeries.collectAsState()
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hour = java.time.LocalTime.now().hour
    val greet = when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }
    val name = s.profile.name.ifBlank { null }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text(if (name != null) "$greet, $name" else greet) },
                subtitle = { Text(LocalDate.now().pretty()) },
                actions = { IconButton(onSettings) { Icon(Icons.Default.Settings, "Settings") } },
                scrollBehavior = scroll,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface, scrolledContainerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onLog, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Log meal") },
                containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Balance hero ───────────────────────────────────────────────
            val cs = MaterialTheme.colorScheme
            val balProg = animatedProgress(s.progress)
            val dayProg = animatedProgress(s.todayIntake.toFloat() / s.budget)
            val waterProg = animatedProgress(w.progress)
            HeroCard(colors = listOf(cs.primary, Color(0xFF0B4D3E)), blobShape = MaterialShapes.Cookie12Sided) {
                val onHero = cs.onPrimary
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Label("Balance to burn", onHero.copy(alpha = 0.8f))
                        AnimatedNumber(s.balance, MaterialTheme.typography.displayMedium, onHero)
                        Text("kcal  ·  ${s.kgLeft.kg1()} to goal", style = MaterialTheme.typography.bodyLarge, color = onHero.copy(alpha = 0.85f))
                    }
                    Box(contentAlignment = Alignment.Center) {
                        CircularWavyProgressIndicator(
                            progress = { balProg },
                            modifier = Modifier.size(96.dp),
                            color = cs.secondary, trackColor = onHero.copy(alpha = 0.18f),
                            stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke,
                        )
                        Text("${(s.progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, color = onHero)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroPill(Icons.Default.MonitorWeight, "${s.profile.weightKg.kg1()} → ${s.profile.goalWeightKg.kg1()}", onHero)
                    HeroPill(Icons.Default.LocalFireDepartment, String.format(Locale.US, "BMI %.1f", s.bmi), onHero)
                }
            }

            // ── Today ──────────────────────────────────────────────────────
            StatCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShapeIcon(Icons.Default.LocalFireDepartment, cs.secondaryContainer, cs.onSecondaryContainer, MaterialShapes.Sunny)
                        Column {
                            Label("Today")
                            Text(
                                if (s.budgetLeft >= 0) "${s.budgetLeft.kcal()} left" else "${abs(s.budgetLeft).kcal()} over",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (s.budgetLeft >= 0) cs.onSurface else cs.error,
                            )
                        }
                    }
                    Text("${s.todayIntake} / ${s.budget}", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                }
                LinearWavyProgressIndicator(
                    progress = { dayProg },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    color = if (s.budgetLeft >= 0) cs.primary else cs.error,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Metric("Eaten", s.todayIntake.kcal())
                    Metric("Burn (TDEE)", s.tdee.kcal())
                    Metric("Closes at", "${s.todayNet.signedKcal()}", if (s.todayNet <= 0) cs.primary else cs.error)
                }
            }

            // ── Trend ──────────────────────────────────────────────────────
            StatCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShapeIcon(Icons.Default.TrendingDown, cs.primaryContainer, cs.onPrimaryContainer, MaterialShapes.Clover4Leaf)
                    Column {
                        Label("Balance trend")
                        Text(if (series.size >= 2) "${series.size} postings" else "Appears after your first closed day", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    }
                }
                if (series.size >= 2) TrendChart(series.map { it.second.toFloat() }, Modifier.padding(top = 8.dp), target = 0f)
                else LinearWavyProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp), color = cs.outlineVariant)
            }

            // ── ETA ────────────────────────────────────────────────────────
            HeroCard(colors = listOf(cs.secondaryContainer, cs.secondaryContainer.copy(alpha = 0.6f)), blobShape = MaterialShapes.Sunny, blobTint = cs.secondary.copy(alpha = 0.12f)) {
                val on = cs.onSecondaryContainer
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ShapeIcon(Icons.Default.Flag, cs.secondary, cs.onSecondary, MaterialShapes.Pill)
                    Label("Estimated zero date", on.copy(alpha = 0.8f))
                }
                val pace = s.daysAtPace; val plan = s.daysAtPlan
                when {
                    s.balance <= 0 -> { Text("You're there 🎉", style = MaterialTheme.typography.headlineMedium, color = on); Text("Update your weight in Settings to re‑price the account.", color = on) }
                    pace != null -> { Text(s.eta(pace)!!.pretty(), style = MaterialTheme.typography.headlineMedium, color = on); Text("$pace days at your 7‑day pace (${s.avgRecentNet!!.toInt().signedKcal()} kcal/day)", color = on) }
                    s.avgRecentNet != null -> { Text("Not shrinking yet", style = MaterialTheme.typography.headlineMedium, color = on); Text("7‑day average is ${s.avgRecentNet!!.toInt().signedKcal()} kcal/day — above your burn.", color = on) }
                    else -> { Text("Log a few days", style = MaterialTheme.typography.headlineMedium, color = on); Text("The pace estimate appears after your first closed day.", color = on) }
                }
                if (s.balance > 0) plan?.let { Text("On plan (−${s.profile.plannedDeficit}/day): ${s.eta(it)!!.pretty()} · $it days", style = MaterialTheme.typography.bodySmall, color = on.copy(alpha = 0.8f)) }
            }

            // ── Water ──────────────────────────────────────────────────────
            StatCard(container = cs.tertiaryContainer) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ShapeIcon(Icons.Default.WaterDrop, cs.tertiary, cs.onTertiary, MaterialShapes.Pentagon)
                        Column {
                            Label("Water", cs.onTertiaryContainer.copy(alpha = 0.8f))
                            Text("${w.todayMl} / ${w.goalMl} ml", style = MaterialTheme.typography.titleLarge, color = cs.onTertiaryContainer)
                        }
                    }
                    FilledTonalButton(onClick = { vm.addWater(250) }, shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = cs.tertiary, contentColor = cs.onTertiary)) { Text("+250 ml") }
                }
                LinearWavyProgressIndicator(progress = { waterProg }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), color = cs.tertiary, trackColor = cs.onTertiaryContainer.copy(alpha = 0.15f))
            }
            Spacer(Modifier.height(88.dp))
        }
    }
}

@Composable
private fun HeroPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, tint: Color) {
    Row(
        Modifier.height(32.dp).background(tint.copy(alpha = 0.14f), MaterialTheme.shapes.large).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) { Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp)); Text(text, style = MaterialTheme.typography.labelLarge, color = tint) }
}

@Composable
private fun Metric(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Column { Label(label); Text(value, style = MaterialTheme.typography.titleMedium, color = color) }
}

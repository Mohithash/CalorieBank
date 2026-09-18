@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.ui.AnimatedNumber
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.BarChart
import com.mohithash.caloriebank.ui.EmptyState
import com.mohithash.caloriebank.ui.HeroCard
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.ShapeIcon
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.animatedProgress
import com.mohithash.caloriebank.ui.dayShort
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.PI
import kotlin.math.sin

/** A blob that fills with an animated wave to [progress]. */
@Composable
private fun WaveGlass(progress: Float, size: Int = 132) {
    val cs = MaterialTheme.colorScheme
    val level = animatedProgress(progress)
    val phase by rememberInfiniteTransition(label = "wave").animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart), label = "phase")
    Box(Modifier.size(size.dp).clip(MaterialShapes.Cookie12Sided.toShape()), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(cs.tertiaryContainer)
            fun wave(amp: Float, shift: Float, color: Color) {
                val y0 = this.size.height * (1f - level)
                val p = Path().apply {
                    moveTo(0f, y0)
                    var x = 0f
                    while (x <= this@Canvas.size.width) { lineTo(x, y0 + amp * sin(x / this@Canvas.size.width * 2 * PI.toFloat() * 1.5f + phase + shift)); x += 4f }
                    lineTo(this@Canvas.size.width, this@Canvas.size.height); lineTo(0f, this@Canvas.size.height); close()
                }
                drawPath(p, color)
            }
            wave(7f, 1.6f, cs.tertiary.copy(alpha = 0.45f))
            wave(6f, 0f, cs.tertiary)
        }
        Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, color = if (level > 0.55f) cs.onTertiary else cs.onTertiaryContainer)
    }
}

@Composable
fun WaterScreen(vm: AppViewModel) {
    val w by vm.water.collectAsState()
    val s by vm.bank.collectAsState()
    val cs = MaterialTheme.colorScheme
    var custom by remember { mutableStateOf("") }
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(topBar = { TopAppBar(title = { Text("Water") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                HeroCard(colors = listOf(cs.tertiary, Color(0xFF003A57)), blobShape = MaterialShapes.Pentagon) {
                    val on = cs.onTertiary
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        WaveGlass(w.progress)
                        Column {
                            Label("Today", on.copy(alpha = 0.8f))
                            AnimatedNumber(w.todayMl, MaterialTheme.typography.displaySmall, on, " ml")
                            Text("of ${w.goalMl} ml goal", color = on.copy(alpha = 0.85f))
                            val left = w.goalMl - w.todayMl
                            Text(if (left > 0) "$left ml to go" else "Goal reached 💧", style = MaterialTheme.typography.titleMedium, color = on)
                        }
                    }
                    Text("≈ 35 ml per kg (${s.profile.weightKg.toInt()} kg), plus a bump for ${s.profile.activity.label.lowercase()} days.",
                        style = MaterialTheme.typography.bodySmall, color = on.copy(alpha = 0.8f))
                }
            }
            item {
                StatCard {
                    Label("Quick add")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(150 to "Cup", 250 to "Glass", 500 to "Bottle").forEach { (ml, name) ->
                            FilledTonalButton(onClick = { vm.addWater(ml) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = cs.tertiaryContainer, contentColor = cs.onTertiaryContainer)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("+$ml", style = MaterialTheme.typography.titleMedium); Text(name, style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(custom, { custom = it.filter(Char::isDigit) }, label = { Text("Custom") }, suffix = { Text("ml") }, singleLine = true, shape = MaterialTheme.shapes.large,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        Button(onClick = { custom.toIntOrNull()?.let { vm.addWater(it); custom = "" } },
                            enabled = (custom.toIntOrNull() ?: 0) > 0, shapes = ButtonDefaults.shapes()) { Text("Add") }
                    }
                }
            }
            item {
                StatCard {
                    Label("Last 7 days")
                    if (w.recent.isEmpty()) Text("No history yet.", color = cs.onSurfaceVariant)
                    else BarChart(w.recent.reversed().map { it.date.dayShort() to it.ml }, w.goalMl, Modifier.padding(top = 8.dp))
                }
            }
            item { Text("Today's log", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp)) }
            if (w.today.isEmpty()) item { EmptyState(Icons.Default.WaterDrop, "Dry so far", "Tap a quick‑add button to log your first glass.", MaterialShapes.Pentagon) }
            items(w.today, key = { it.id }) { e ->
                ListItem(
                    leadingContent = { ShapeIcon(Icons.Default.WaterDrop, cs.tertiaryContainer, cs.onTertiaryContainer, MaterialShapes.Pentagon) },
                    headlineContent = { Text("${e.ml} ml") },
                    supportingContent = { Text(Instant.ofEpochMilli(e.timestamp).atZone(ZoneId.systemDefault()).format(timeFmt)) },
                    trailingContent = { IconButton({ vm.deleteWater(e.id) }) { Icon(Icons.Default.Delete, "Delete", tint = cs.onSurfaceVariant) } },
                    colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerLow),
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

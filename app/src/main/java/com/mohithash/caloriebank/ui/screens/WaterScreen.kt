@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.WavyProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.prettyDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun WaterScreen(vm: AppViewModel) {
    val w by vm.water.collectAsState()
    val s by vm.bank.collectAsState()
    var custom by remember { mutableStateOf("") }
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    Scaffold(topBar = { TopAppBar(title = { Text("Water") }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                StatCard(container = MaterialTheme.colorScheme.tertiaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularWavyProgressIndicator(progress = { w.progress }, modifier = Modifier.size(120.dp),
                                stroke = WavyProgressIndicatorDefaults.circularIndicatorStroke,
                                color = MaterialTheme.colorScheme.tertiary)
                            Text("${(w.progress * 100).toInt()}%", style = MaterialTheme.typography.titleLarge)
                        }
                        Column {
                            Label("Today")
                            Text("${w.todayMl} ml", style = MaterialTheme.typography.displaySmall)
                            Text("of ${w.goalMl} ml goal", style = MaterialTheme.typography.bodyMedium)
                            val left = w.goalMl - w.todayMl
                            Text(if (left > 0) "$left ml to go" else "Goal reached 💧", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text("Estimated need: 35 ml per kg (${s.profile.weightKg.toInt()} kg) with an activity bump for ${s.profile.activity.label.lowercase()} days.",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                StatCard {
                    Label("Quick add")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(150, 250, 500).forEach { ml ->
                            FilledTonalButton(onClick = { vm.addWater(ml) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("+$ml") }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(custom, { custom = it.filter(Char::isDigit) }, label = { Text("Custom") }, suffix = { Text("ml") }, singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        Button(onClick = { custom.toIntOrNull()?.let { vm.addWater(it); custom = "" } },
                            enabled = (custom.toIntOrNull() ?: 0) > 0, shapes = ButtonDefaults.shapes()) { Text("Add") }
                    }
                }
            }
            item {
                StatCard {
                    Label("Last 7 days")
                    val maxMl = maxOf(w.goalMl, w.recent.maxOfOrNull { it.ml } ?: 0).toFloat()
                    Row(Modifier.fillMaxWidth().height(120.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                        w.recent.reversed().forEach { d ->
                            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                val frac = (d.ml / maxMl).coerceIn(0.04f, 1f)
                                Box(Modifier.fillMaxWidth().fillMaxHeight(frac * 0.8f)
                                    .background(if (d.ml >= w.goalMl) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.small))
                                Spacer(Modifier.height(4.dp))
                                Text(d.date.takeLast(2), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (w.recent.isEmpty()) Text("No history yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { Text("Today's log", style = MaterialTheme.typography.titleMedium) }
            items(w.today, key = { it.id }) { e ->
                ListItem(
                    headlineContent = { Text("${e.ml} ml") },
                    supportingContent = { Text(Instant.ofEpochMilli(e.timestamp).atZone(ZoneId.systemDefault()).format(timeFmt)) },
                    trailingContent = { IconButton({ vm.deleteWater(e.id) }) { Icon(Icons.Default.Delete, "Delete") } },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

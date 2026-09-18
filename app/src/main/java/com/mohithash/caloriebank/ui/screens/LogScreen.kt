@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.mohithash.caloriebank.ui.MealPhoto
import com.mohithash.caloriebank.ui.Photo
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.data.TxKind
import com.mohithash.caloriebank.ui.AiUi
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.kcal
import com.mohithash.caloriebank.ui.signedKcal

@Composable
fun LogScreen(vm: AppViewModel, onSettings: () -> Unit) {
    val s by vm.bank.collectAsState()
    val ai by vm.aiUi.collectAsState()
    val settings by vm.aiSettings.collectAsState()
    var query by remember { mutableStateOf("") }
    var manual by remember { mutableStateOf(false) }
    var mName by remember { mutableStateOf("") }
    var mKcal by remember { mutableStateOf("") }
    var mExercise by remember { mutableStateOf(false) }
    var photo by remember { mutableStateOf<MealPhoto?>(null) }
    val ctx = LocalContext.current
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp -> bmp?.let { photo = Photo.fromBitmap(it) } }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> uri?.let { photo = Photo.fromUri(ctx, it) } }
    val canEstimate = (query.isNotBlank() || photo != null) && settings.configured

    Scaffold(topBar = { TopAppBar(title = { Text("Log today") }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                    ToggleButton(checked = !manual, onCheckedChange = { manual = false }, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("AI estimate")
                    }
                    ToggleButton(checked = manual, onCheckedChange = { manual = true }, shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Manual") }
                }
            }

            item {
                if (!manual) StatCard {
                    Label("Describe or photograph what you ate")
                    photo?.let { p ->
                        Box(Modifier.fillMaxWidth()) {
                            Image(p.bitmap.asImageBitmap(), "Meal photo", contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(180.dp).clip(MaterialTheme.shapes.large))
                            IconButton({ photo = null }, modifier = Modifier.align(Alignment.TopEnd)) {
                                Icon(Icons.Default.Close, "Remove photo", tint = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                    }
                    if (ai == AiUi.Idle) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { camera.launch(null) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Camera")
                        }
                        OutlinedButton(onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Gallery")
                        }
                    }
                    OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), minLines = 2,
                        placeholder = { Text(if (photo != null) "Optional: portion hints, e.g. 'the bowl is 300 g'" else "e.g. 2 rotis with dal, a bowl of rice and a chai with sugar") })
                    if (!settings.configured) {
                        Text("No API key yet. Add one to use AI estimates.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        TextButton(onSettings) { Text("Open settings") }
                    }
                    when (val a = ai) {
                        AiUi.Loading -> Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                            LoadingIndicator(); Spacer(Modifier.size(12.dp)); Text("Asking ${settings.effectiveModel}…")
                        }
                        is AiUi.Error -> {
                            Text(a.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { vm.estimate(query, photo?.base64) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth()) { Text("Retry") }
                        }
                        is AiUi.Result -> {
                            Label("Estimate · ${a.estimate.total_calories.kcal()} · ${a.estimate.confidence} confidence")
                            a.estimate.items.forEach { it ->
                                ListItem(
                                    headlineContent = { Text(it.name) },
                                    supportingContent = { Text("${it.serving}  P ${it.protein_g.toInt()}g · C ${it.carbs_g.toInt()}g · F ${it.fat_g.toInt()}g") },
                                    trailingContent = { Text(it.calories.kcal(), style = MaterialTheme.typography.titleMedium) },
                                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                    modifier = Modifier.padding(vertical = 2.dp),
                                )
                            }
                            if (a.estimate.note.isNotBlank()) Text(a.estimate.note, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = vm::clearAi, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("Discard") }
                                Button(onClick = { vm.acceptEstimate(a.estimate); query = ""; photo = null }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("Add to today") }
                            }
                        }
                        AiUi.Idle -> Button(
                            onClick = { vm.estimate(query, photo?.base64) }, enabled = canEstimate,
                            shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp),
                        ) { Icon(Icons.Default.AutoAwesome, null); Spacer(Modifier.size(8.dp)); Text(if (photo != null) "Estimate from photo" else "Estimate calories") }
                    }
                } else StatCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                        ToggleButton(checked = !mExercise, onCheckedChange = { mExercise = false }, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Food") }
                        ToggleButton(checked = mExercise, onCheckedChange = { mExercise = true }, shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Exercise") }
                    }
                    OutlinedTextField(mName, { mName = it }, label = { Text(if (mExercise) "Activity" else "Food") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(mKcal, { mKcal = it.filter(Char::isDigit) }, label = { Text(if (mExercise) "Calories burned" else "Calories") }, suffix = { Text("kcal") },
                        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            val k = mKcal.toIntOrNull() ?: return@Button
                            if (mExercise) vm.addExercise(mName.ifBlank { "Exercise" }, k) else vm.addFood(mName.ifBlank { "Food" }, k)
                            mName = ""; mKcal = ""
                        },
                        enabled = mKcal.toIntOrNull()?.let { it > 0 } == true, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text(if (mExercise) "Credit exercise" else "Add food") }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Today's entries", style = MaterialTheme.typography.titleMedium)
                    Text("${s.todayIntake.kcal()} / ${s.budget.kcal()}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (s.todayEntries.isEmpty()) item {
                Text("Nothing logged yet today.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
            }
            items(s.todayEntries, key = { it.id }) { tx ->
                ListItem(
                    headlineContent = { Text(tx.title) },
                    supportingContent = { Text(if (tx.kind == TxKind.EXERCISE) "Exercise credit" else tx.note.ifBlank { "Food" }) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tx.amount.signedKcal(), style = MaterialTheme.typography.titleMedium,
                                color = if (tx.amount < 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            IconButton({ vm.deleteTx(tx.id) }) { Icon(Icons.Default.Delete, "Delete") }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

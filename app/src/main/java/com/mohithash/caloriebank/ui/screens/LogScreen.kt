@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.data.TxKind
import com.mohithash.caloriebank.ui.AiUi
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.EmptyState
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.MealPhoto
import com.mohithash.caloriebank.ui.Photo
import com.mohithash.caloriebank.ui.ShapeIcon
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.kcal
import com.mohithash.caloriebank.ui.signedKcal

@Composable
fun LogScreen(vm: AppViewModel, onSettings: () -> Unit) {
    val s by vm.bank.collectAsState()
    val ai by vm.aiUi.collectAsState()
    val settings by vm.aiSettings.collectAsState()
    val recent by vm.recentFoods.collectAsState()
    val cs = MaterialTheme.colorScheme
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

    Scaffold(topBar = {
        TopAppBar(title = { Text("Log today") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface),
            actions = { Text("${s.todayIntake} / ${s.budget} kcal", style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant, modifier = Modifier.padding(end = 16.dp)) })
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
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
                AnimatedContent(manual, label = "mode") { isManual ->
                    if (!isManual) StatCard(Modifier.animateContentSize()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ShapeIcon(Icons.Default.AutoAwesome, cs.primaryContainer, cs.onPrimaryContainer, MaterialShapes.Cookie7Sided)
                            Column { Label("Describe or photograph"); Text("Per‑item calories and macros", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant) }
                        }
                        photo?.let { p ->
                            Box(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                                Image(p.bitmap.asImageBitmap(), "Meal photo", contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(MaterialTheme.shapes.large))
                                FilledTonalIconButton({ photo = null }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) { Icon(Icons.Default.Close, "Remove photo") }
                            }
                        }
                        if (ai == AiUi.Idle) Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            OutlinedButton(onClick = { camera.launch(null) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Camera")
                            }
                            OutlinedButton(onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Gallery")
                            }
                        }
                        OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, shape = MaterialTheme.shapes.large,
                            placeholder = { Text(if (photo != null) "Optional: portion hints, e.g. 'the bowl is 300 g'" else "e.g. 2 rotis with dal, a bowl of rice and a chai with sugar") })
                        if (!settings.configured) {
                            Text("No API key yet. Add one to use AI estimates.", color = cs.error, style = MaterialTheme.typography.bodySmall)
                            TextButton(onSettings) { Text("Open settings") }
                        }
                        when (val a = ai) {
                            AiUi.Loading -> Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                LoadingIndicator(); Spacer(Modifier.size(12.dp)); Text("Asking ${settings.effectiveModel}…", color = cs.onSurfaceVariant)
                            }
                            is AiUi.Error -> {
                                Text(a.message, color = cs.error)
                                Button(onClick = { vm.estimate(query, photo?.base64) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth()) { Text("Retry") }
                            }
                            is AiUi.Result -> {
                                Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(a.estimate.total_calories.kcal(), style = MaterialTheme.typography.headlineSmall)
                                    SuggestionChip(onClick = {}, label = { Text("${a.estimate.confidence} confidence") })
                                }
                                a.estimate.items.forEach {
                                    ListItem(
                                        headlineContent = { Text(it.name) },
                                        supportingContent = { Text("${it.serving}  ·  P ${it.protein_g.toInt()}  C ${it.carbs_g.toInt()}  F ${it.fat_g.toInt()} g") },
                                        trailingContent = { Text(it.calories.kcal(), style = MaterialTheme.typography.titleMedium) },
                                        colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerHigh),
                                        modifier = Modifier.padding(vertical = 2.dp).clip(MaterialTheme.shapes.medium),
                                    )
                                }
                                if (a.estimate.note.isNotBlank()) Text(a.estimate.note, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
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
                            ToggleButton(checked = !mExercise, onCheckedChange = { mExercise = false }, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Restaurant, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Food")
                            }
                            ToggleButton(checked = mExercise, onCheckedChange = { mExercise = true }, shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.DirectionsRun, null, Modifier.size(18.dp)); Spacer(Modifier.size(6.dp)); Text("Exercise")
                            }
                        }
                        OutlinedTextField(mName, { mName = it }, label = { Text(if (mExercise) "Activity" else "Food") }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
                        OutlinedTextField(mKcal, { mKcal = it.filter(Char::isDigit) }, label = { Text(if (mExercise) "Calories burned" else "Calories") }, suffix = { Text("kcal") },
                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
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
            }

            if (recent.isNotEmpty()) item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.History, null, Modifier.size(16.dp), tint = cs.onSurfaceVariant); Label("Log again")
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        recent.forEach { f -> SuggestionChip(onClick = { vm.addFood(f.title, f.amount, f.note) }, label = { Text("${f.title} · ${f.amount}") }) }
                    }
                }
            }

            item { Text("Today's entries", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp)) }
            if (s.todayEntries.isEmpty()) item {
                EmptyState(Icons.Default.Restaurant, "Nothing logged yet", "Describe a meal, snap a photo, or add it by hand.")
            }
            items(s.todayEntries, key = { it.id }) { tx ->
                val ex = tx.kind == TxKind.EXERCISE
                ListItem(
                    leadingContent = { ShapeIcon(if (ex) Icons.Default.DirectionsRun else Icons.Default.Restaurant,
                        if (ex) cs.primaryContainer else cs.secondaryContainer, if (ex) cs.onPrimaryContainer else cs.onSecondaryContainer,
                        if (ex) MaterialShapes.Arrow else MaterialShapes.Cookie6Sided) },
                    headlineContent = { Text(tx.title) },
                    supportingContent = { Text(if (ex) "Exercise credit" else tx.note.ifBlank { "Food" }) },
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tx.amount.signedKcal(), style = MaterialTheme.typography.titleMedium, color = if (ex) cs.primary else cs.onSurface)
                            IconButton({ vm.deleteTx(tx.id) }) { Icon(Icons.Default.Delete, "Delete", tint = cs.onSurfaceVariant) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = cs.surfaceContainerLow),
                    modifier = Modifier.clip(MaterialTheme.shapes.large),
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

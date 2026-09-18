@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.domain.Activity
import com.mohithash.caloriebank.domain.Calc
import com.mohithash.caloriebank.domain.Profile
import com.mohithash.caloriebank.domain.Sex
import com.mohithash.caloriebank.ui.Label
import java.util.Locale

/** Shared profile editor. Reports a parsed [Profile] (or null while invalid) on every change. */
@Composable
fun ProfileForm(initial: Profile, onChange: (Profile?) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var sex by remember { mutableStateOf(initial.sex) }
    var age by remember { mutableStateOf(initial.age.toString()) }
    var height by remember { mutableStateOf(num(initial.heightCm)) }
    var weight by remember { mutableStateOf(num(initial.weightKg)) }
    var goal by remember { mutableStateOf(num(initial.goalWeightKg)) }
    var activity by remember { mutableStateOf(initial.activity) }
    var deficit by remember { mutableStateOf(initial.plannedDeficit.toFloat()) }

    fun emit() {
        val a = age.toIntOrNull(); val h = height.toDoubleOrNull(); val w = weight.toDoubleOrNull(); val g = goal.toDoubleOrNull()
        val ok = a != null && a in 10..100 && h != null && h in 100.0..250.0 && w != null && w in 30.0..300.0 && g != null && g in 30.0..300.0
        onChange(if (ok) initial.copy(name = name.trim(), sex = sex, age = a!!, heightCm = h!!, weightKg = w!!, goalWeightKg = g!!,
            activity = activity, plannedDeficit = deficit.toInt()) else null)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(name, { name = it; emit() }, label = { Text("Name (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

        Label("Sex")
        Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
            ToggleButton(checked = sex == Sex.MALE, onCheckedChange = { sex = Sex.MALE; emit() },
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Male") }
            ToggleButton(checked = sex == Sex.FEMALE, onCheckedChange = { sex = Sex.FEMALE; emit() },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Female") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(age, { age = it.filter(Char::isDigit); emit() }, label = { Text("Age") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
            OutlinedTextField(height, { height = it.dec(); emit() }, label = { Text("Height") }, suffix = { Text("cm") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(weight, { weight = it.dec(); emit() }, label = { Text("Weight") }, suffix = { Text("kg") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
            OutlinedTextField(goal, { goal = it.dec(); emit() }, label = { Text("Goal weight") }, suffix = { Text("kg") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f),
                supportingText = {
                    height.toDoubleOrNull()?.let { Text("Healthy max ≈ ${num(Calc.healthyMaxWeight(it))} kg") }
                })
        }

        Label("Activity level")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Activity.entries.forEach { a ->
                FilterChip(selected = activity == a, onClick = { activity = a; emit() }, label = { Text(a.label) })
            }
        }
        Text(activity.hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Label("Planned daily deficit: ${deficit.toInt()} kcal (≈ ${String.format(Locale.US, "%.2f", deficit * 7 / 7700)} kg/week)")
        Slider(value = deficit, onValueChange = { deficit = it; emit() }, valueRange = 200f..1000f, steps = 15,
            onValueChangeFinished = { emit() }, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

private fun num(d: Double) = if (d % 1.0 == 0.0) d.toInt().toString() else String.format(Locale.US, "%.1f", d)
private fun String.dec() = filter { it.isDigit() || it == '.' }

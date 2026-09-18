@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.domain.AiProvider
import com.mohithash.caloriebank.domain.Profile
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.StatCard
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val profile by vm.profile.collectAsState()
    val ai by vm.aiSettings.collectAsState()
    var draft by remember { mutableStateOf<Profile?>(profile) }
    var provider by remember { mutableStateOf(ai.provider) }
    var key by remember { mutableStateOf(ai.apiKey) }
    var model by remember { mutableStateOf(ai.model) }
    var baseUrl by remember { mutableStateOf(ai.baseUrl) }
    var showKey by remember { mutableStateOf(false) }
    var testing by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun currentAi() = ai.copy(provider = provider, apiKey = key.trim(), model = model.trim(), baseUrl = baseUrl.trim())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }, navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }) },
        snackbarHost = { SnackbarHost(snack) },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {

            StatCard {
                Label("AI provider (bring your own key)")
                Text("Your key is stored only on this device and sent only to the provider below.", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                    ToggleButton(checked = provider == AiProvider.ANTHROPIC, onCheckedChange = { provider = AiProvider.ANTHROPIC },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Claude") }
                    ToggleButton(checked = provider == AiProvider.OPENAI_COMPAT, onCheckedChange = { provider = AiProvider.OPENAI_COMPAT },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), modifier = Modifier.weight(1f)) { Text("OpenAI‑compatible") }
                }
                OutlinedTextField(key, { key = it }, label = { Text("API key") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { IconButton({ showKey = !showKey }) { Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } })
                OutlinedTextField(model, { model = it }, label = { Text("Model") }, placeholder = { Text(provider.defaultModel) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(baseUrl, { baseUrl = it }, label = { Text("Base URL") }, placeholder = { Text(provider.defaultBaseUrl) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    supportingText = { if (provider == AiProvider.OPENAI_COMPAT) Text("Works with OpenAI, Groq, OpenRouter, Ollama, LM Studio…") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            testing = true
                            scope.launch {
                                val r = vm.testAi(currentAi())
                                testing = false
                                snack.showSnackbar(r.fold({ "OK — a banana ≈ ${it.total_calories} kcal" }, { it.message ?: "Failed" }))
                            }
                        }, enabled = key.isNotBlank() && !testing, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f),
                    ) { if (testing) LoadingIndicator(Modifier.size(20.dp)) else Text("Test") }
                    Button(onClick = { vm.saveAi(currentAi()); scope.launch { snack.showSnackbar("AI settings saved") } },
                        shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) { Text("Save key") }
                }
            }

            StatCard {
                Label("Profile & goal")
                Text("Changing weight or goal re-prices the balance to (weight − goal) × 7 700 kcal and posts an adjustment.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                ProfileForm(profile) { draft = it }
                Button(
                    onClick = { draft?.let { vm.updateProfile(it); scope.launch { snack.showSnackbar("Profile saved") } } },
                    enabled = draft != null && draft != profile, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().height(52.dp),
                ) { Text("Save profile") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.data.TxKind
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.prettyDate
import com.mohithash.caloriebank.ui.signedKcal

@Composable
fun HistoryScreen(vm: AppViewModel) {
    val all by vm.ledger.collectAsState()
    var bankOnly by remember { mutableStateOf(true) }
    val shown = if (bankOnly) all.filter { it.kind == TxKind.SETTLE || it.kind == TxKind.ADJUST } else all
    val grouped = shown.groupBy { it.date }

    Scaffold(topBar = { TopAppBar(title = { Text("Transactions") }) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            item {
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                    ToggleButton(checked = bankOnly, onCheckedChange = { bankOnly = true }, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Bank statement") }
                    ToggleButton(checked = !bankOnly, onCheckedChange = { bankOnly = false }, shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Everything") }
                }
            }
            if (shown.isEmpty()) item {
                Text(if (bankOnly) "No settlements yet. The first one posts after your first logged day closes." else "No entries yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            grouped.forEach { (date, txs) ->
                item(key = "h$date") {
                    Text(date.prettyDate(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
                }
                items(txs, key = { it.id }) { tx ->
                    val bankTx = tx.kind == TxKind.SETTLE || tx.kind == TxKind.ADJUST
                    val color = when {
                        !bankTx -> MaterialTheme.colorScheme.onSurfaceVariant
                        tx.amount < 0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.error
                    }
                    ListItem(
                        leadingContent = {
                            Icon(when (tx.kind) {
                                TxKind.FOOD -> Icons.Default.Restaurant
                                TxKind.EXERCISE -> Icons.Default.DirectionsRun
                                TxKind.SETTLE -> Icons.Default.EventAvailable
                                TxKind.ADJUST -> Icons.Default.AccountBalance
                            }, null, tint = color)
                        },
                        headlineContent = { Text(tx.title) },
                        supportingContent = { if (tx.note.isNotBlank()) Text(tx.note) },
                        trailingContent = { Text(tx.amount.signedKcal(), style = MaterialTheme.typography.titleMedium, color = color) },
                        colors = ListItemDefaults.colors(containerColor = if (bankTx) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainer),
                    )
                }
            }
        }
    }
}

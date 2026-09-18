@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.mohithash.caloriebank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.mohithash.caloriebank.data.Tx
import com.mohithash.caloriebank.data.TxKind
import com.mohithash.caloriebank.ui.AppViewModel
import com.mohithash.caloriebank.ui.EmptyState
import com.mohithash.caloriebank.ui.Label
import com.mohithash.caloriebank.ui.ShapeIcon
import com.mohithash.caloriebank.ui.StatCard
import com.mohithash.caloriebank.ui.TrendChart
import com.mohithash.caloriebank.ui.prettyDate
import com.mohithash.caloriebank.ui.signedKcal
import java.util.Locale

@Composable
fun HistoryScreen(vm: AppViewModel) {
    val all by vm.ledger.collectAsState()
    val series by vm.balanceSeries.collectAsState()
    val cs = MaterialTheme.colorScheme
    var bankOnly by remember { mutableStateOf(true) }
    val bank = all.filter { it.kind == TxKind.SETTLE || it.kind == TxKind.ADJUST }
    // Running balance keyed by tx id, so each statement row can show "balance after".
    val runningAfter: Map<Long, Int> = run { var r = 0; bank.sortedBy { it.timestamp }.associate { it.id to (r + it.amount).also { n -> r = n } } }
    val shown = if (bankOnly) bank else all
    val grouped = shown.groupBy { it.date }
    val weekNet = bank.filter { it.kind == TxKind.SETTLE }.take(7).sumOf { it.amount }

    Scaffold(topBar = { TopAppBar(title = { Text("Statement") }, colors = TopAppBarDefaults.topAppBarColors(containerColor = cs.surface)) }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            item {
                StatCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Label("Balance over time"); Text(String.format(Locale.US, "%,d kcal", series.lastOrNull()?.second ?: 0), style = MaterialTheme.typography.headlineSmall) }
                        Column(horizontalAlignment = Alignment.End) {
                            Label("Last 7 closes")
                            Text("${weekNet.signedKcal()} kcal", style = MaterialTheme.typography.headlineSmall, color = if (weekNet <= 0) cs.primary else cs.error)
                        }
                    }
                    if (series.size >= 2) TrendChart(series.map { it.second.toFloat() }, Modifier.padding(top = 8.dp), target = 0f)
                }
            }
            item {
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                    ToggleButton(checked = bankOnly, onCheckedChange = { bankOnly = true }, shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Bank statement") }
                    ToggleButton(checked = !bankOnly, onCheckedChange = { bankOnly = false }, shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(), modifier = Modifier.weight(1f)) { Text("Everything") }
                }
            }
            if (shown.isEmpty()) item {
                EmptyState(Icons.Default.Receipt, "No postings yet",
                    if (bankOnly) "The first settlement posts after your first logged day closes." else "Log a meal to see it here.")
            }
            grouped.forEach { (date, txs) ->
                item(key = "h$date") {
                    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(date.prettyDate(), style = MaterialTheme.typography.labelLarge, color = cs.primary)
                        val dayBank = txs.filter { it.kind == TxKind.SETTLE || it.kind == TxKind.ADJUST }.sumOf { it.amount }
                        if (dayBank != 0) Text(dayBank.signedKcal(), style = MaterialTheme.typography.labelLarge, color = cs.onSurfaceVariant)
                    }
                }
                items(txs, key = { it.id }) { tx -> TxRow(tx, runningAfter[tx.id]) }
            }
        }
    }
}

@Composable
private fun TxRow(tx: Tx, after: Int?) {
    val cs = MaterialTheme.colorScheme
    val bankTx = tx.kind == TxKind.SETTLE || tx.kind == TxKind.ADJUST
    val color = when { !bankTx -> cs.onSurfaceVariant; tx.amount < 0 -> cs.primary; else -> cs.error }
    val (icon, shape, bg, fg) = when (tx.kind) {
        TxKind.FOOD -> Quad(Icons.Default.Restaurant, MaterialShapes.Cookie6Sided, cs.secondaryContainer, cs.onSecondaryContainer)
        TxKind.EXERCISE -> Quad(Icons.Default.DirectionsRun, MaterialShapes.Arrow, cs.primaryContainer, cs.onPrimaryContainer)
        TxKind.SETTLE -> Quad(Icons.Default.EventAvailable, MaterialShapes.Clover4Leaf, if (tx.amount < 0) cs.primaryContainer else cs.errorContainer, if (tx.amount < 0) cs.onPrimaryContainer else cs.onErrorContainer)
        TxKind.ADJUST -> Quad(Icons.Default.AccountBalance, MaterialShapes.Diamond, cs.tertiaryContainer, cs.onTertiaryContainer)
    }
    ListItem(
        leadingContent = { ShapeIcon(icon, bg, fg, shape) },
        headlineContent = { Text(tx.title) },
        supportingContent = { Text(listOfNotNull(tx.note.ifBlank { null }, after?.let { String.format(Locale.US, "Balance %,d", it) }).joinToString("  ·  ")) },
        trailingContent = { Text(tx.amount.signedKcal(), style = MaterialTheme.typography.titleMedium, color = color) },
        colors = ListItemDefaults.colors(containerColor = if (bankTx) cs.surfaceContainer else cs.surfaceContainerLow),
        modifier = Modifier.clip(MaterialTheme.shapes.large),
    )
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

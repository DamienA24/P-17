package com.openclassrooms.rebonnte.ui.medicine.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.model.History

@Composable
fun MedicineEditView(
    padding: PaddingValues,
    viewModel: MedicineDetailViewModel
) {
    val medicine by viewModel.medicine.collectAsState()
    val history by viewModel.history.collectAsState()
    val med = medicine ?: return

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        TextField(
            value = med.name,
            onValueChange = {},
            label = { Text(stringResource(R.string.medicine_name_label)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = med.aisleName,
            onValueChange = {},
            label = { Text(stringResource(R.string.medicine_aisle_label)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { if (med.stock > 0) viewModel.updateStock(-1) }) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = stringResource(R.string.decrement_stock_cd))
            }
            TextField(
                value = med.stock.toString(),
                onValueChange = {},
                label = { Text(stringResource(R.string.medicine_stock_label)) },
                enabled = false,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { viewModel.updateStock(1) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.increment_stock_cd))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.history_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(history, key = { it.id }) { h -> HistoryItem(h) }
        }
    }
}

@Composable
private fun HistoryItem(history: History) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        val formattedDate = remember(history.date) {
            java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                .format(history.date.toDate())
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = history.medicineName, fontWeight = FontWeight.Bold)
            Text(text = stringResource(R.string.history_user, history.userEmail))
            Text(text = stringResource(R.string.history_date, formattedDate))
            Text(text = stringResource(R.string.history_details, history.details))
        }
    }
}

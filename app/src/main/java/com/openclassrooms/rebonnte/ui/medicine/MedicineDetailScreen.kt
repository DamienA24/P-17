package com.openclassrooms.rebonnte.ui.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.model.History

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    onBack: () -> Unit,
    viewModel: MedicineDetailViewModel = hiltViewModel()
) {
    val medicine by viewModel.medicine.collectAsState()
    val history by viewModel.history.collectAsState()
    val aisles by viewModel.aisles.collectAsState()
    val formName by viewModel.formName.collectAsState()
    val formStock by viewModel.formStock.collectAsState()
    val formAisleId by viewModel.formAisleId.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var aisleMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect { onBack() }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteMedicine()
                }) { Text(stringResource(R.string.delete_confirm_button)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isCreationMode) stringResource(R.string.new_medicine_title)
                        else medicine?.name ?: ""
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_cd))
                    }
                },
                actions = {
                    if (!viewModel.isCreationMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_medicine_cd))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.isCreationMode) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                TextField(
                    value = formName,
                    onValueChange = { viewModel.updateFormName(it) },
                    label = { Text(stringResource(R.string.medicine_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = aisleMenuExpanded,
                    onExpandedChange = { aisleMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextField(
                        value = aisles.find { it.id == formAisleId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.medicine_aisle_label)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aisleMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = aisleMenuExpanded,
                        onDismissRequest = { aisleMenuExpanded = false }
                    ) {
                        aisles.forEach { aisle ->
                            DropdownMenuItem(
                                text = { Text(aisle.name) },
                                onClick = {
                                    viewModel.updateFormAisle(aisle.id, aisle.name)
                                    aisleMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = if (formStock == 0) "" else formStock.toString(),
                    onValueChange = { viewModel.updateFormStock(it) },
                    label = { Text(stringResource(R.string.medicine_stock_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.saveMedicine() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save_button))
                }
            }
        } else {
            medicine?.let { med ->
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
                        items(history, key = { it.id }) { h -> HistoryItem(history = h) }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItem(history: History) {
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

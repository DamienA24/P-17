package com.openclassrooms.rebonnte.ui.medicine.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.openclassrooms.rebonnte.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineCreationForm(
    padding: PaddingValues,
    viewModel: MedicineDetailViewModel
) {
    val form by viewModel.form.collectAsState()
    val aisles by viewModel.aisles.collectAsState()
    var aisleMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        TextField(
            value = form.name,
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
                value = aisles.find { it.id == form.aisleId }?.name ?: "",
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
            value = if (form.stock == 0) "" else form.stock.toString(),
            onValueChange = { viewModel.updateFormStock(it) },
            label = { Text(stringResource(R.string.medicine_stock_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { viewModel.saveMedicine() },
            enabled = form.aisleId.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save_button))
        }
    }
}

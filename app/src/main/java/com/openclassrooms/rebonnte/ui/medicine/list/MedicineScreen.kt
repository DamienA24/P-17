package com.openclassrooms.rebonnte.ui.medicine.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R
import com.openclassrooms.rebonnte.ui.components.EmbeddedSearchBar
import com.openclassrooms.rebonnte.ui.components.MedicineItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit,
    onAddMedicine: () -> Unit,
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy((-1).dp)) {
                TopAppBar(
                    title = { Text(stringResource(R.string.medicine_screen_title)) },
                    actions = {
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options_cd))
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(x = 0.dp, y = 0.dp)
                            ) {
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByNone(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_none)) }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByName(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_name)) }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByStock(); expanded = false },
                                    text = { Text(stringResource(R.string.sort_by_stock)) }
                                )
                            }
                        }
                    }
                )
                EmbeddedSearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.filterByName(it)
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMedicine) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_medicine_cd))
            }
        }
    ) { padding ->
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            errorMessage != null -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { Text(errorMessage!!, color = MaterialTheme.colorScheme.error) }
            else -> LazyColumn(
                contentPadding = padding,
                modifier = Modifier.fillMaxSize()
            ) {
                items(medicines, key = { it.id }) { medicine ->
                    MedicineItem(medicine = medicine, onClick = { onMedicineClick(medicine.id, medicine.aisleId) })
                }
            }
        }
    }
}


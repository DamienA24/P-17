package com.openclassrooms.rebonnte.ui.medicine

import com.openclassrooms.rebonnte.model.Medicine
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.openclassrooms.rebonnte.R

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
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
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
                        viewModel.filterByName(it)
                        searchQuery = it
                    },
                    isSearchActive = isSearchActive,
                    onActiveChanged = { isSearchActive = it }
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

@Composable
fun MedicineItem(medicine: Medicine, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = medicine.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Stock: ${medicine.stock}", style = MaterialTheme.typography.bodyMedium)
        }
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = stringResource(R.string.arrow_cd))
    }
}

@Composable
fun EmbeddedSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearchActive: Boolean,
    onActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by rememberSaveable { mutableStateOf(query) }
    val activeChanged: (Boolean) -> Unit = { active ->
        searchQuery = ""
        onQueryChange("")
        onActiveChanged(active)
    }
    val shape: Shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive) {
            IconButton(onClick = { activeChanged(false) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.close_search_cd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = stringResource(R.string.search_cd),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BasicTextField(
            value = searchQuery,
            onValueChange = { q ->
                searchQuery = q
                onQueryChange(q)
            },
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )
        if (isSearchActive && searchQuery.isNotEmpty()) {
            IconButton(onClick = {
                searchQuery = ""
                onQueryChange("")
            }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.clear_search_cd),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit,
    viewModel: MedicineViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            var expanded by remember { mutableStateOf(false) }
            Column(verticalArrangement = Arrangement.spacedBy((-1).dp)) {
                TopAppBar(
                    title = { Text("Medicines") },
                    actions = {
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(x = 0.dp, y = 0.dp)
                            ) {
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByNone(); expanded = false },
                                    text = { Text("Sort by None") }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByName(); expanded = false },
                                    text = { Text("Sort by Name") }
                                )
                                DropdownMenuItem(
                                    onClick = { viewModel.sortByStock(); expanded = false },
                                    text = { Text("Sort by Stock") }
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
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(medicines) { medicine ->
                MedicineItem(medicine = medicine, onClick = { onMedicineClick(medicine.id, medicine.aisleId) })
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
        Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Arrow")
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
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
            decorationBox = { innerTextField ->
                if (searchQuery.isEmpty()) {
                    Text(
                        text = "Search",
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

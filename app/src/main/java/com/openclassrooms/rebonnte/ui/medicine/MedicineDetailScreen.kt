package com.openclassrooms.rebonnte.ui.medicine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MedicineRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val medicineId: String = checkNotNull(savedStateHandle["medicineId"])
    private val aisleId: String = checkNotNull(savedStateHandle["aisleId"])

    private val _medicine = MutableStateFlow<Medicine?>(null)
    val medicine: StateFlow<Medicine?> = _medicine.asStateFlow()

    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history: StateFlow<List<History>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMedicinesByAisle(aisleId).collect { medicines ->
                _medicine.value = medicines.find { it.id == medicineId }
            }
        }
        viewModelScope.launch {
            repo.getHistory(medicineId, aisleId).collect { _history.value = it }
        }
    }

    fun updateStock(delta: Int) {
        viewModelScope.launch {
            repo.updateStock(medicineId, aisleId, delta, auth.currentUser?.email ?: "")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    onBack: () -> Unit,
    viewModel: MedicineDetailViewModel = hiltViewModel()
) {
    val medicine by viewModel.medicine.collectAsState()
    val history by viewModel.history.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(medicine?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        medicine?.let { med ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                TextField(
                    value = med.name,
                    onValueChange = {},
                    label = { Text("Name") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = med.aisleName,
                    onValueChange = {},
                    label = { Text("Aisle") },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { if (med.stock > 0) viewModel.updateStock(-1) }) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Minus One")
                    }
                    TextField(
                        value = med.stock.toString(),
                        onValueChange = {},
                        label = { Text("Stock") },
                        enabled = false,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { viewModel.updateStock(1) }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Plus One")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("History", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(history) { h -> HistoryItem(history = h) }
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
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = history.medicineName, fontWeight = FontWeight.Bold)
            Text(text = "User: ${history.userEmail}")
            Text(text = "Date: ${history.date}")
            Text(text = "Details: ${history.details}")
        }
    }
}

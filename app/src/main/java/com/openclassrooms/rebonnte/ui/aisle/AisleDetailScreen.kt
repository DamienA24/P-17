package com.openclassrooms.rebonnte.ui.aisle

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import com.openclassrooms.rebonnte.ui.medicine.MedicineItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AisleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MedicineRepository
) : ViewModel() {

    private val aisleId: String = checkNotNull(savedStateHandle["aisleId"])

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMedicinesByAisle(aisleId).collect { _medicines.value = it }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AisleDetailScreen(
    aisleName: String,
    onBack: () -> Unit,
    onMedicineClick: (medicineId: String, aisleId: String) -> Unit,
    viewModel: AisleDetailViewModel = hiltViewModel()
) {
    val medicines by viewModel.medicines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(aisleName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize()
        ) {
            items(medicines) { medicine ->
                MedicineItem(
                    medicine = medicine,
                    onClick = { onMedicineClick(medicine.id, medicine.aisleId) }
                )
            }
        }
    }
}

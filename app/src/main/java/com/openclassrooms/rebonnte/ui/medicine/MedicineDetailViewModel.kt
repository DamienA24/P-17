package com.openclassrooms.rebonnte.ui.medicine

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

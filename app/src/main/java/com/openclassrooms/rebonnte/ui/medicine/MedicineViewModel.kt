package com.openclassrooms.rebonnte.ui.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val repo: MedicineRepository
) : ViewModel() {
    private val _allMedicines = MutableStateFlow<List<Medicine>>(emptyList())
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getMedicines().collect {
                _allMedicines.value = it
                _medicines.value = it
            }
        }
    }

    fun filterByName(name: String) {
        _medicines.value = if (name.isBlank()) {
            _allMedicines.value
        } else {
            _allMedicines.value.filter {
                it.name.lowercase(Locale.getDefault())
                    .contains(name.lowercase(Locale.getDefault()))
            }
        }
    }

    fun sortByNone() {
        _medicines.value = _allMedicines.value
    }

    fun sortByName() {
        _medicines.value = _allMedicines.value.sortedBy { it.name }
    }

    fun sortByStock() {
        _medicines.value = _allMedicines.value.sortedBy { it.stock }
    }

    fun updateStock(medicineId: String, aisleId: String, delta: Int, userEmail: String) {
        viewModelScope.launch {
            repo.updateStock(medicineId, aisleId, delta, userEmail)
        }
    }
}

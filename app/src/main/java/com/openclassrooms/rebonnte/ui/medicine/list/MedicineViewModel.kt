package com.openclassrooms.rebonnte.ui.medicine.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MedicineViewModel @Inject constructor(
    private val repo: MedicineRepository,
    private val authRepo: AuthRepository
) : ViewModel() {
    private val _allMedicines = MutableStateFlow<List<Medicine>>(emptyList())
    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _sortNameAsc = MutableStateFlow(true)
    private val _sortStockAsc = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            authRepo.authStateFlow()
                .flatMapLatest { user ->
                    if (user != null) repo.getMedicines() else emptyFlow()
                }
                .catch { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Erreur inconnue"
                }
                .collect {
                    _isLoading.value = false
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
        _sortNameAsc.value = true
        _sortStockAsc.value = true
        _medicines.value = _allMedicines.value
    }

    fun sortByName() {
        _medicines.value = if (_sortNameAsc.value) {
            _allMedicines.value.sortedBy { it.name }
        } else {
            _allMedicines.value.sortedByDescending { it.name }
        }
        _sortNameAsc.value = !_sortNameAsc.value
    }

    fun sortByStock() {
        _medicines.value = if (_sortStockAsc.value) {
            _allMedicines.value.sortedBy { it.stock }
        } else {
            _allMedicines.value.sortedByDescending { it.stock }
        }
        _sortStockAsc.value = !_sortStockAsc.value
    }

    fun updateStock(medicineId: String, aisleId: String, delta: Int, userEmail: String) {
        viewModelScope.launch {
            repo.updateStock(medicineId, aisleId, delta, userEmail)
        }
    }
}

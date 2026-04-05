package com.openclassrooms.rebonnte.ui.medicine.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Aisle
import com.openclassrooms.rebonnte.model.History
import com.openclassrooms.rebonnte.model.Medicine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicineFormState(
    val name: String = "",
    val stock: Int = 0,
    val aisleId: String = "",
    val aisleName: String = ""
)

@HiltViewModel
class MedicineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: MedicineRepository,
    private val aisleRepo: AisleRepository,
    private val auth: AuthRepository
) : ViewModel() {

    private val medicineId: String? = savedStateHandle["medicineId"]
    private val aisleId: String? = savedStateHandle["aisleId"]

    val isCreationMode: Boolean = medicineId == null

    private val _medicine = MutableStateFlow<Medicine?>(null)
    val medicine: StateFlow<Medicine?> = _medicine.asStateFlow()

    private val _history = MutableStateFlow<List<History>>(emptyList())
    val history: StateFlow<List<History>> = _history.asStateFlow()

    private val _aisles = MutableStateFlow<List<Aisle>>(emptyList())
    val aisles: StateFlow<List<Aisle>> = _aisles.asStateFlow()

    private val _form = MutableStateFlow(MedicineFormState())
    val form: StateFlow<MedicineFormState> = _form.asStateFlow()

    // replay=1: collector may subscribe after emit when using UnconfinedTestDispatcher
    private val _navigateBack = MutableSharedFlow<Unit>(replay = 1)
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        if (isCreationMode) {
            viewModelScope.launch {
                aisleRepo.getAisles().collect { _aisles.value = it }
            }
        } else {
            require(aisleId != null) { "aisleId is required when medicineId is provided" }
            viewModelScope.launch {
                repo.getMedicinesByAisle(aisleId).collect { medicines ->
                    _medicine.value = medicines.find { it.id == medicineId }
                }
            }
            viewModelScope.launch {
                repo.getHistory(medicineId!!, aisleId).collect { _history.value = it }
            }
        }
    }

    fun updateStock(delta: Int) {
        if (isCreationMode) return
        viewModelScope.launch {
            repo.updateStock(medicineId!!, aisleId!!, delta, auth.currentUser()?.email ?: "")
        }
    }

    fun updateFormName(name: String) { _form.update { it.copy(name = name) } }

    fun updateFormStock(stock: String) { _form.update { it.copy(stock = stock.toIntOrNull() ?: 0) } }

    fun updateFormAisle(id: String, name: String) { _form.update { it.copy(aisleId = id, aisleName = name) } }

    fun saveMedicine() {
        viewModelScope.launch {
            val f = _form.value
            repo.addMedicine(Medicine(name = f.name, stock = f.stock, aisleId = f.aisleId, aisleName = f.aisleName))
            _navigateBack.emit(Unit)
        }
    }

    fun deleteMedicine() {
        viewModelScope.launch {
            repo.deleteMedicine(medicineId!!, aisleId!!)
            _navigateBack.emit(Unit)
        }
    }
}

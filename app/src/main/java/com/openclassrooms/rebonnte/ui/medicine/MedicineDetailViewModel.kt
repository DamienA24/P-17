package com.openclassrooms.rebonnte.ui.medicine

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
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    private val _formName = MutableStateFlow("")
    val formName: StateFlow<String> = _formName.asStateFlow()

    private val _formStock = MutableStateFlow(0)
    val formStock: StateFlow<Int> = _formStock.asStateFlow()

    private val _formAisleId = MutableStateFlow("")
    val formAisleId: StateFlow<String> = _formAisleId.asStateFlow()

    private val _formAisleName = MutableStateFlow("")

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

    fun updateFormName(name: String) { _formName.value = name }

    fun updateFormStock(stock: String) { _formStock.value = stock.toIntOrNull() ?: 0 }

    fun updateFormAisle(aisleId: String, aisleName: String) {
        _formAisleId.value = aisleId
        _formAisleName.value = aisleName
    }

    fun saveMedicine() {
        viewModelScope.launch {
            repo.addMedicine(
                Medicine(
                    name = _formName.value,
                    stock = _formStock.value,
                    aisleId = _formAisleId.value,
                    aisleName = _formAisleName.value
                )
            )
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

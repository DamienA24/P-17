package com.openclassrooms.rebonnte.ui.aisle.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.MedicineRepository
import com.openclassrooms.rebonnte.model.Medicine
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

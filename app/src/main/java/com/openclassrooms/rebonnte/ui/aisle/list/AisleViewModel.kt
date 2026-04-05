package com.openclassrooms.rebonnte.ui.aisle.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.AisleRepository
import com.openclassrooms.rebonnte.model.Aisle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AisleViewModel @Inject constructor(
    private val repo: AisleRepository
) : ViewModel() {
    private val _aisles = MutableStateFlow<List<Aisle>>(emptyList())
    val aisles: StateFlow<List<Aisle>> = _aisles.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getAisles()
                .catch { e ->
                    _isLoading.value = false
                    _errorMessage.value = e.message ?: "Erreur inconnue"
                }
                .collect {
                    _isLoading.value = false
                    _aisles.value = it
                }
        }
    }

    fun addAisle(name: String) {
        viewModelScope.launch {
            repo.addAisle(Aisle(name = name.trim()))
        }
    }
}

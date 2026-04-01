package com.openclassrooms.rebonnte.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.rebonnte.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Loading : AuthUiState()
    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.signIn(email, password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState.Success
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Erreur de connexion")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.register(email, password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState.Success
            } else {
                AuthUiState.Error(result.exceptionOrNull()?.message ?: "Erreur d'inscription")
            }
        }
    }

    fun signOut() {
        repo.signOut()
    }
}

package com.muhammedalikocabey.aspectify.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammedalikocabey.aspectify.core.proxyOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: String) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    private val userService = proxyOf<UserService> {
        target = RealUserService()
    }

    fun loginAndFetch() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                userService.login("admin", "1234")
                val result = "Login başarılı"
                _uiState.value = UiState.Success(result)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Login başarısız: ${e.message}")
            }
        }
    }
}

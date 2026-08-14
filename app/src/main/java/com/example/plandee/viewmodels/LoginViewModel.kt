package com.example.plandee.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var emailState: MutableStateFlow<String> = MutableStateFlow("")
        private set
    var passwordState: MutableStateFlow<String> = MutableStateFlow("")
        private set

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) { emailState.value = email }
    fun onPasswordChanged(password: String) { passwordState.value = password }

    fun login() {
        val email = emailState.value.trim()
        val password = passwordState.value

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please fill in all fields.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            delay(1000) // Mock Network Call

            if (email.contains("@")) {
                _uiState.value = AuthUiState(isSuccess = true)
            } else {
                _uiState.value = AuthUiState(errorMessage = "Invalid email address.")
            }
        }
    }
}
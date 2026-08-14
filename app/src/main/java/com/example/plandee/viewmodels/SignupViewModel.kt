package com.example.plandee.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {
    var signupData: MutableStateFlow<SignupData> = MutableStateFlow(SignupData())
        private set

    var currentStep: MutableStateFlow<Int> = MutableStateFlow(1) // Step 1: Account, Step 2: Context
        private set

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Step 1 Updates
    fun updateFirstName(name: String) { signupData.value = signupData.value.copy(firstName = name) }
    fun updateLastName(name: String) { signupData.value = signupData.value.copy(lastName = name) }
    fun updateEmail(email: String) { signupData.value = signupData.value.copy(email = email) }
    fun updatePassword(pass: String) { signupData.value = signupData.value.copy(password = pass) }

    // Step 2 Updates
    fun updateCountry(country: String) { signupData.value = signupData.value.copy(country = country) }
    fun toggleNetwork(network: String) {
        val current = signupData.value.selectedNetworks.toMutableList()
        if (current.contains(network)) current.remove(network) else current.add(network)
        signupData.value = signupData.value.copy(selectedNetworks = current)
    }
    fun updateReason(reason: String) { signupData.value = signupData.value.copy(primaryReason = reason) }

    fun goToNextStep() {
        val data = signupData.value
        if (data.firstName.isEmpty() || data.lastName.isEmpty() || data.email.isEmpty() || data.password.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please complete all fields.")
            return
        }
        if (data.password.length < 6) {
            _uiState.value = AuthUiState(errorMessage = "Password must be at least 6 characters.")
            return
        }
        _uiState.value = AuthUiState(errorMessage = null)
        currentStep.value = 2
    }

    fun goToPreviousStep() {
        currentStep.value = 1
    }

    fun completeSignup() {
        val data = signupData.value
        if (data.selectedNetworks.isEmpty() || data.primaryReason.isEmpty()) {
            _uiState.value = AuthUiState(errorMessage = "Please select your network(s) and primary goal.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            delay(1200) // Mock Network Call
            _uiState.value = AuthUiState(isSuccess = true)
        }
    }
}
package com.example.plandee.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.plandee.data.network.LoginRequest
import com.example.plandee.data.network.RegisterRequest
import com.example.plandee.data.network.RetrofitClient
import com.example.plandee.data.security.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager.getInstance(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all email and password fields.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val api = RetrofitClient.getApiService(getApplication())
                val response = api.login(LoginRequest(email = email.trim(), password = pass.trim()))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (!body.token.isNullOrEmpty()) {
                        sessionManager.saveAuthToken(body.token)
                        sessionManager.saveUserEmail(email.trim())
                        _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true, isSuccess = true)
                        onSuccess()
                        return@launch
                    }
                }

                val errBody = response.errorBody()?.string()
                val msg = if (!errBody.isNullOrEmpty() && errBody.contains("error")) {
                    "Invalid login credentials. Please check email and password."
                } else {
                    "Login failed (HTTP ${response.code()}). Please try again."
                }
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
            } catch (e: ConnectException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Cannot connect to server at 10.249.38.84:8080. Check connection or IP."
                )
            } catch (e: UnknownHostException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to resolve server host address. Please check your network connection."
                )
            } catch (e: SocketTimeoutException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Server connection timed out. Please check if the Go backend is running."
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Unable to connect to server."
                )
            }
        }
    }

    fun signup(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please fill in all email and password fields.")
            return
        }

        if (pass.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password must be at least 6 characters long.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val api = RetrofitClient.getApiService(getApplication())
                val response = api.register(RegisterRequest(email = email.trim(), password = pass.trim(), country = "Nigeria"))

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    if (!body.token.isNullOrEmpty()) {
                        sessionManager.saveAuthToken(body.token)
                        sessionManager.saveUserEmail(email.trim())
                        _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true, isSuccess = true)
                        onSuccess()
                        return@launch
                    }
                }

                val errBody = response.errorBody()?.string()
                val msg = if (!errBody.isNullOrEmpty() && errBody.contains("exists")) {
                    "An account with this email already exists."
                } else {
                    "Registration failed (HTTP ${response.code()}). Please try again."
                }
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = msg)
            } catch (e: ConnectException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Cannot connect to server at 10.249.38.84:8080. Check connection or IP."
                )
            } catch (e: UnknownHostException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Unable to resolve server host address. Please check your network connection."
                )
            } catch (e: SocketTimeoutException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Server connection timed out. Please check if the Go backend is running."
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Unable to connect to server."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

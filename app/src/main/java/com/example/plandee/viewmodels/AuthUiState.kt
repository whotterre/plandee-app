package com.example.plandee.viewmodels

data class AuthUiState(
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)

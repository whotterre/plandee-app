package com.example.plandee.viewmodels

data class SignupData(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val country: String = "",
    val selectedNetworks: List<String> = emptyList(),
    val primaryReason: String = ""
)

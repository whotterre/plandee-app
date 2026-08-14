package com.example.plandee.ui.screens.auth

import androidx.compose.runtime.Composable

@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    AuthScreen(
        onLoginSuccess = onLoginSuccess,
        onSignupSuccess = onNavigateToSignup
    )
}
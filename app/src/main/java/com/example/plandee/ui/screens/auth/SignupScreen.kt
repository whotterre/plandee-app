package com.example.plandee.ui.screens.auth

import androidx.compose.runtime.Composable

@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit = {},
    onSignupSuccess: () -> Unit = {}
) {
    AuthScreen(
        onLoginSuccess = onNavigateToLogin,
        onSignupSuccess = onSignupSuccess
    )
}
package com.example.plandee.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plandee.ui.screens.DashboardScreen
import com.example.plandee.ui.screens.auth.AuthScreen
import com.example.plandee.ui.screens.auth.OnboardingStep1Screen
import com.example.plandee.ui.screens.auth.OnboardingStep2Screen

object PlanDeeRoutes {
    const val Auth = "auth"
    const val OnboardingStep1 = "onboarding_step1"
    const val OnboardingStep2 = "onboarding_step2"
    const val Login = "login"
    const val Signup = "signup"
    const val Dashboard = "dashboard"
}

@Composable
fun PlanDeeNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = PlanDeeRoutes.Auth
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(PlanDeeRoutes.Auth) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(PlanDeeRoutes.Dashboard) {
                        popUpTo(PlanDeeRoutes.Auth) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignupSuccess = {
                    navController.navigate(PlanDeeRoutes.OnboardingStep1)
                }
            )
        }

        composable(PlanDeeRoutes.OnboardingStep1) {
            OnboardingStep1Screen(
                onContinue = { _, _ ->
                    navController.navigate(PlanDeeRoutes.OnboardingStep2)
                },
                onSkip = {
                    navController.navigate(PlanDeeRoutes.OnboardingStep2)
                }
            )
        }

        composable(PlanDeeRoutes.OnboardingStep2) {
            OnboardingStep2Screen(
                onFinish = { _, _ ->
                    navController.navigate(PlanDeeRoutes.Dashboard) {
                        popUpTo(PlanDeeRoutes.Auth) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(PlanDeeRoutes.Login) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(PlanDeeRoutes.Dashboard) {
                        popUpTo(PlanDeeRoutes.Auth) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignupSuccess = {
                    navController.navigate(PlanDeeRoutes.OnboardingStep1)
                }
            )
        }

        composable(PlanDeeRoutes.Signup) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(PlanDeeRoutes.Dashboard) {
                        popUpTo(PlanDeeRoutes.Auth) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onSignupSuccess = {
                    navController.navigate(PlanDeeRoutes.OnboardingStep1)
                }
            )
        }

        composable(PlanDeeRoutes.Dashboard) {
            DashboardScreen()
        }
    }
}

package com.yurtdolap.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.presentation.auth.LoginScreen
import com.yurtdolap.app.presentation.auth.RegisterScreen

@Composable
fun RootNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()
    val startDestination = if (authRepository.isUserAuthenticatedInFirebase) {
        "main_graph"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("main_graph") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateBackToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.navigate("main_graph") {
                        popUpTo("register") { inclusive = true }
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main_graph") {
            MainScreen(
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("main_graph") { inclusive = true }
                    }
                }
            )
        }
    }
}

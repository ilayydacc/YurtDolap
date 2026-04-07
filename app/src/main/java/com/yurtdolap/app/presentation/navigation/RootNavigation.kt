package com.yurtdolap.app.presentation.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yurtdolap.app.domain.repository.AuthRepository
import com.yurtdolap.app.presentation.auth.LoginScreen
import com.yurtdolap.app.presentation.auth.RegisterScreen

@Composable
fun RootNavigation(authRepository: AuthRepository) {
    val navController = rememberNavController()
    
    // Uygulama ilk açıldığında kontrol et
    val startDestination = if (authRepository.isUserAuthenticatedInFirebase) {
        "main_graph"
    } else {
        "auth_graph"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        
        // Auth Graph
        composable("auth_graph") {
            // Normalde nested graph da yapabilirdik, basit olsun diye route olarak verdik.
            // Fakat login'i başlangıç rotası yapalım
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo("auth_graph") { inclusive = true }
                }
            }
        }
        
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

        // Main App Graph (Bottom Bar vs olan)
        composable("main_graph") {
            MainScreen(
                onSignOut = {
                    navController.navigate("auth_graph") {
                        popUpTo("main_graph") { inclusive = true }
                    }
                }
            )
        }
    }
}

package com.yurtdolap.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.yurtdolap.app.presentation.add.AddProductScreen
import com.yurtdolap.app.presentation.detail.DetailScreen
import com.yurtdolap.app.presentation.home.HomeScreen
import com.yurtdolap.app.presentation.home.MessagesScreen
import com.yurtdolap.app.presentation.saved.SavedScreen
import com.yurtdolap.app.presentation.profile.ProfileScreen
import com.yurtdolap.app.presentation.profile.EditProfileScreen
import com.yurtdolap.app.presentation.chat.ChatDetailScreen
import com.yurtdolap.app.presentation.edit.EditProductScreen

@Composable
fun MainScreen(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            YurtBottomNavBar(
                navController = navController,
                currentDestination = currentDestination
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController, 
                startDestination = BottomNavItem.Home.route,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(300)
                    ) + fadeOut(animationSpec = tween(300))
                }
            ) {
                composable(BottomNavItem.Home.route) { 
                    HomeScreen(onNavigateToDetail = { productId ->
                        navController.navigate("detail/$productId")
                    }) 
                }
                composable(
                    route = "detail/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) {
                    DetailScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToChat = { chatId ->
                            navController.navigate("chat/$chatId")
                        }
                    )
                }
                composable(BottomNavItem.Favorites.route) { 
                    SavedScreen(onNavigateToDetail = { productId ->
                        navController.navigate("detail/$productId")
                    })
                }
                composable(BottomNavItem.Add.route) { 
                    AddProductScreen(
                        onNavigateBack = { navController.popBackStack() }
                    ) 
                }
                composable(BottomNavItem.Messages.route) { 
                    MessagesScreen(
                        onNavigateToChat = { chatId ->
                            navController.navigate("chat/$chatId")
                        }
                    ) 
                }
                composable(BottomNavItem.Profile.route) { 
                    ProfileScreen(
                        onNavigateToEdit = { productId ->
                            navController.navigate("edit/$productId")
                        },
                        onNavigateToEditProfile = {
                            navController.navigate("edit_profile")
                        },
                        onSignOut = onSignOut
                    ) 
                }
                composable("edit_profile") {
                    EditProfileScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "edit/{productId}",
                    arguments = listOf(navArgument("productId") { type = NavType.StringType })
                ) {
                    EditProductScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "chat/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) {
                    ChatDetailScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

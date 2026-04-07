package com.yurtdolap.app.presentation.navigation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.yurtdolap.app.presentation.designsystem.theme.CtaGreen
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.SurfaceLight
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@Composable
fun YurtBottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Favorites,
        BottomNavItem.Add,
        BottomNavItem.Messages,
        BottomNavItem.Profile
    )

    Surface(
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        color = SurfaceLight
    ) {
        NavigationBar(
            containerColor = SurfaceLight,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                
                // Emphasize the 'Add' button to match the energetic block UX pattern
                val isAddButton = item == BottomNavItem.Add

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        val poppedToExistingTab = navController.popBackStack(item.route, false)
                        if (!poppedToExistingTab && !isSelected) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        if (item == BottomNavItem.Favorites) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = item.title,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected) PrimaryLilac else TextDarkPurple.copy(alpha = 0.5f)
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.title,
                                modifier = if (isAddButton) Modifier.size(32.dp).padding(bottom = 4.dp) else Modifier.size(24.dp),
                                tint = if (isAddButton) CtaGreen else if (isSelected) PrimaryLilac else TextDarkPurple.copy(alpha = 0.5f)
                            )
                        }
                    },
                    label = {
                        if (!isAddButton) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrimaryLilac else TextDarkPurple.copy(alpha = 0.5f)
                            )
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = if (isAddButton) SurfaceLight else PrimaryLilac.copy(alpha = 0.1f)
                    ),
                    alwaysShowLabel = !isAddButton
                )
            }
        }
    }
}

package com.yurtdolap.app.presentation.navigation

import androidx.annotation.DrawableRes
import com.yurtdolap.app.R

sealed class BottomNavItem(
    val route: String,
    val title: String,
    @DrawableRes val icon: Int // Placeholder for now, can be ImageVector
) {
    object Home : BottomNavItem("home", "Ana Sayfa", R.drawable.ic_home)
    object Favorites : BottomNavItem("favorites", "Favoriler", R.drawable.ic_search) // Will replace icon if there is an ic_favorite, else keep placeholder or use Icons.Default.Favorite in YurtBottomNavBar
    object Add : BottomNavItem("add", "Ekle", R.drawable.ic_add_circle) // Special CTA
    object Messages : BottomNavItem("messages", "Mesajlar", R.drawable.ic_message)
    object Profile : BottomNavItem("profile", "Profil", R.drawable.ic_person)
}

package com.yurtdolap.app.domain.model

data class UserProfileData(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val city: String = "",
    val dormitory: String = "",
    val createdAt: Long = 0L,
    val isAdmin: Boolean = false
)

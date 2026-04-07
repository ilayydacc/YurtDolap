package com.yurtdolap.app.domain.model

data class Category(
    val id: String,
    val name: String,
    val iconEmoji: String // Actually we are strictly forbidden from using Emoji for UI icons, so we will use local drawable IDs later. But let's use a solid identifier.
)

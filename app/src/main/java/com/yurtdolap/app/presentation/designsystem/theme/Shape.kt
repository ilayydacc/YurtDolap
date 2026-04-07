package com.yurtdolap.app.presentation.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),   // Buttons, inputs
    medium = RoundedCornerShape(12.dp), // Cards, dialogs
    large = RoundedCornerShape(16.dp)   // Bottom sheets, big containers
)

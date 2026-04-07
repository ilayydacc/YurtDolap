package com.yurtdolap.app.presentation.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yurtdolap.app.presentation.designsystem.theme.ErrorRed
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac

sealed class UIState<out T> {
    object Idle : UIState<Nothing>()
    object Loading : UIState<Nothing>()
    data class Success<T>(val data: T) : UIState<T>()
    data class Error(val message: String) : UIState<Nothing>()
}

@Composable
fun <T> UIStateWrapper(
    state: UIState<T>,
    loadingMessage: String = "Yukleniyor...",
    loadingContent: (@Composable () -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    successContent: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "UIState Transition",
        modifier = modifier.fillMaxSize()
    ) { targetState ->
        when (targetState) {
            is UIState.Idle -> {
                Box(modifier = Modifier.fillMaxSize())
            }
            is UIState.Loading -> {
                if (loadingContent != null) {
                    loadingContent()
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = PrimaryLilac)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = loadingMessage, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
            is UIState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "Bir hata olustu",
                            style = MaterialTheme.typography.titleLarge,
                            color = ErrorRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = targetState.message, style = MaterialTheme.typography.bodyMedium)

                        if (onRetry != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            YurtPrimaryButton(text = "Tekrar Dene", onClick = onRetry)
                        }
                    }
                }
            }
            is UIState.Success -> {
                successContent(targetState.data)
            }
        }
    }
}

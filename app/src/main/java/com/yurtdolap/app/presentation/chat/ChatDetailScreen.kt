package com.yurtdolap.app.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.Message
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.PrimaryLilac
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messageText by viewModel.messageText.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sohbet", color = TextDarkPurple, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri Dön", tint = TextDarkPurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundWhite)
            )
        },
        bottomBar = {
            ChatInputBar(
                text = messageText,
                onTextChange = viewModel::updateMessageText,
                onSend = viewModel::sendMessage
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(padding)
        ) {
            UIStateWrapper(
                state = uiState,
                onRetry = { /* auto retries due to snapshot listener */ }
            ) { messages ->
                if (messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Henüz bir mesaj yok. İlk mesajı gönder!", color = TextDarkPurple.copy(alpha = 0.5f))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = false
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            val isMe = msg.senderId == viewModel.currentUserId
                            MessageBubble(message = msg, isMe = isMe)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isMe: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .background(if (isMe) PrimaryLilac else Color.LightGray.copy(alpha = 0.3f))
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (isMe) Color.White else TextDarkPurple,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ChatInputBar(text: String, onTextChange: (String) -> Unit, onSend: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Mesaj yaz...", color = TextDarkPurple.copy(alpha = 0.4f)) },
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryLilac,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = BackgroundWhite,
            ),
            maxLines = 3
        )

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(48.dp)
                .background(PrimaryLilac, RoundedCornerShape(24.dp))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Gönder",
                tint = Color.White,
                modifier = Modifier.padding(start = 4.dp) // center visual
            )
        }
    }
}

package com.yurtdolap.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yurtdolap.app.domain.model.ChatRoom
import com.yurtdolap.app.presentation.designsystem.components.UIStateWrapper
import com.yurtdolap.app.presentation.designsystem.theme.BackgroundWhite
import com.yurtdolap.app.presentation.designsystem.theme.ErrorRed
import com.yurtdolap.app.presentation.designsystem.theme.TextDarkPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesScreen(
    onNavigateToChat: (String) -> Unit = {},
    viewModel: MessagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteError by viewModel.deleteError.collectAsState()
    var chatRoomToDelete by remember { mutableStateOf<ChatRoom?>(null) }

    chatRoomToDelete?.let { room ->
        AlertDialog(
            onDismissRequest = { chatRoomToDelete = null },
            title = {
                Text(
                    text = "Sohbeti Sil",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Bu sohbeti ve tum mesajlari kalici olarak silmek istiyor musun?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteChatRoom(room.id)
                        chatRoomToDelete = null
                    }
                ) {
                    Text("Sil", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { chatRoomToDelete = null }) {
                    Text("Iptal")
                }
            }
        )
    }

    deleteError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearDeleteError,
            title = {
                Text(
                    text = "Sohbet Silinemedi",
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearDeleteError) {
                    Text("Tamam")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, bottom = 12.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "Mesajlarım",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = TextDarkPurple
            )
        }

        UIStateWrapper(state = uiState) { chatRooms ->
            if (chatRooms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Henüz bir mesajınız yok.", color = TextDarkPurple.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(chatRooms, key = { it.id }) { room ->
                        DismissibleChatRoomItem(
                            room = room,
                            onClick = { onNavigateToChat(room.id) },
                            onDeleteRequest = { chatRoomToDelete = room }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissibleChatRoomItem(
    room: ChatRoom,
    onClick: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorRed.copy(alpha = 0.92f))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sohbeti sil",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        },
        content = {
            ChatRoomItem(
                room = room,
                onClick = onClick
            )
        }
    )
}

@Composable
fun ChatRoomItem(room: ChatRoom, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = if (room.lastMessageTimestamp > 0L) {
        dateFormat.format(Date(room.lastMessageTimestamp))
    } else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.productTitle.ifEmpty { "İlan" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPurple,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = room.lastMessage.ifEmpty { "Henüz mesaj yok" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDarkPurple.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = timeString,
                style = MaterialTheme.typography.bodySmall,
                color = TextDarkPurple.copy(alpha = 0.5f)
            )
        }
    }
}

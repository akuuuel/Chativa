package com.chativa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.data.model.Chat
import com.chativa.data.model.Contact
import com.chativa.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListScreen(
    mainViewModel: MainViewModel,
    currentUserId: String,
    onChatClick: (chatId: String, peerName: String, peerId: String) -> Unit
) {
    val chats by mainViewModel.chats.collectAsState()
    val contacts by mainViewModel.contacts.collectAsState()
    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    var selectedChatForDelete by remember { mutableStateOf<Pair<Chat, String>?>(null) }

    val filteredChats = remember(chats, contacts, searchQuery) {
        val list = if (searchQuery.isBlank()) chats
        else {
            chats.filter { chat ->
                val (_, otherName) = resolveContactName(chat, currentUserId, contacts)
                otherName.contains(searchQuery, ignoreCase = true) || chat.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }
        list
    }

    if (filteredChats.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (searchQuery.isNotEmpty()) "Percakapan tidak ditemukan" else "Belum ada percakapan. Mulai obrolan baru!",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredChats) { chat ->
                val (otherId, otherName) = resolveContactName(chat, currentUserId, contacts)
                ChatItemRow(
                    chat = chat,
                    currentUserId = currentUserId,
                    otherName = otherName,
                    onClick = {
                        onChatClick(chat.id, otherName, otherId)
                    },
                    onLongClick = {
                        selectedChatForDelete = Pair(chat, otherName)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
        }
    }

    // Delete Chat Confirmation Dialog
    selectedChatForDelete?.let { (chatToDelete, peerName) ->
        AlertDialog(
            onDismissRequest = { selectedChatForDelete = null },
            title = { Text("Hapus Percakapan") },
            text = {
                Text("Apakah Anda yakin ingin menghapus seluruh percakapan dengan \"$peerName\"? Semua riwayat pesan dalam percakapan ini akan dihapus.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.deleteChat(chatToDelete.id)
                        Toast.makeText(context, "Percakapan dengan $peerName telah dihapus", Toast.LENGTH_SHORT).show()
                        selectedChatForDelete = null
                    }
                ) {
                    Text("HAPUS", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChatForDelete = null }) {
                    Text("BATAL")
                }
            }
        )
    }
}

fun resolveContactName(chat: Chat, currentUserId: String, contacts: List<Contact>): Pair<String, String> {
    val otherId = chat.participantIds.firstOrNull {
        it != currentUserId && it != "current_user"
    } ?: chat.participantIds.firstOrNull { it != currentUserId }
      ?: chat.participantIds.firstOrNull() ?: ""

    val saved = contacts.find { it.id == otherId || it.registeredUserId == otherId }
    if (saved != null && saved.name.isNotBlank()) {
        return Pair(otherId, saved.name)
    }

    val nameNotSaya = chat.participantNames.entries.firstOrNull { it.value != "Saya" }?.value
    if (!nameNotSaya.isNullOrBlank()) {
        return Pair(otherId, nameNotSaya)
    }

    val nameForOtherId = chat.participantNames[otherId]
    if (!nameForOtherId.isNullOrBlank() && nameForOtherId != "Saya") {
        return Pair(otherId, nameForOtherId)
    }

    return Pair(otherId, "Kontak CHATIVA")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatItemRow(
    chat: Chat,
    currentUserId: String,
    otherName: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val unreadCount = chat.unreadCounts[currentUserId] ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Contact Avatar with online indicator
        Box {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = otherName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            // Online status indicator dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = otherName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.lastMessageTime))
                Text(
                    text = formattedTime,
                    fontSize = 11.sp,
                    color = if (unreadCount > 0) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.lastMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

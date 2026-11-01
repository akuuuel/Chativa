package com.chativa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.data.model.Contact
import com.chativa.ui.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsScreen(
    mainViewModel: MainViewModel,
    currentUserId: String = "current_user",
    currentUserName: String = "Saya",
    onContactClick: (contactId: String) -> Unit,
    onStartChat: (chatId: String, peerName: String, peerId: String) -> Unit,
    onVoiceCall: (peerName: String) -> Unit,
    onVideoCall: (peerName: String) -> Unit
) {
    val contacts by mainViewModel.contacts.collectAsState()
    val searchQuery by mainViewModel.searchQuery.collectAsState()
    val context = LocalContext.current

    var selectedContactForDelete by remember { mutableStateOf<Contact?>(null) }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) contacts
        else {
            contacts.filter { c ->
                c.name.contains(searchQuery, ignoreCase = true) || c.phoneNumber.contains(searchQuery)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(filteredContacts) { contact ->
            ContactItemRow(
                contact = contact,
                onClick = {
                    val peerId = contact.registeredUserId ?: contact.id
                    val chat = mainViewModel.startOrGetChat(currentUserId, currentUserName, peerId, contact.name)
                    onStartChat(chat.id, contact.name, peerId)
                },
                onLongClick = {
                    selectedContactForDelete = contact
                },
                onStartChat = {
                    val peerId = contact.registeredUserId ?: contact.id
                    val chat = mainViewModel.startOrGetChat(currentUserId, currentUserName, peerId, contact.name)
                    onStartChat(chat.id, contact.name, peerId)
                },
                onVoiceCall = { onVoiceCall(contact.name) },
                onVideoCall = { onVideoCall(contact.name) },
                onInvite = { }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        }
    }

    // Delete Contact Confirmation Dialog
    selectedContactForDelete?.let { contactToDelete ->
        AlertDialog(
            onDismissRequest = { selectedContactForDelete = null },
            title = { Text("Hapus Kontak") },
            text = {
                Text("Apakah Anda yakin ingin menghapus kontak \"${contactToDelete.name}\" dari daftar kontak Anda?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.deleteContact(contactToDelete.id)
                        Toast.makeText(context, "Kontak ${contactToDelete.name} telah dihapus", Toast.LENGTH_SHORT).show()
                        selectedContactForDelete = null
                    }
                ) {
                    Text("HAPUS", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedContactForDelete = null }) {
                    Text("BATAL")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactItemRow(
    contact: Contact,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onStartChat: () -> Unit,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit,
    onInvite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (contact.isRegistered) MaterialTheme.colorScheme.primary else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            if (contact.isRegistered && contact.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = if (contact.isRegistered) (if (contact.isOnline) "Online" else contact.about) else "Belum terdaftar di CHATIVA",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (contact.isRegistered) {
            IconButton(onClick = onStartChat) {
                Icon(Icons.Default.ChatBubble, contentDescription = "Pesan", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onVoiceCall) {
                Icon(Icons.Default.Call, contentDescription = "Panggilan Suara", tint = MaterialTheme.colorScheme.primary)
            }
        } else {
            TextButton(onClick = onInvite) {
                Text("UNDANG", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

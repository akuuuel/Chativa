package com.chativa.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.chativa.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId: String,
    mainViewModel: MainViewModel,
    currentUserId: String = "current_user",
    currentUserName: String = "Saya",
    onNavigateBack: () -> Unit,
    onStartChat: (chatId: String, peerName: String, peerId: String) -> Unit,
    onVoiceCall: (peerName: String) -> Unit,
    onVideoCall: (peerName: String) -> Unit
) {
    val contacts by mainViewModel.contacts.collectAsState()
    val contact = remember(contacts, contactId) {
        contacts.find { it.id == contactId }
    }
    val context = LocalContext.current

    if (contact == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Kontak tidak ditemukan.")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Kontak") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        mainViewModel.deleteContact(contactId)
                        Toast.makeText(context, "Kontak dihapus", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Kontak", tint = Color.Red)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(if (contact.isRegistered) MaterialTheme.colorScheme.primary else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = contact.name,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Text(
                text = if (contact.isRegistered) (if (contact.isOnline) "Online" else "Terakhir dilihat baru saja") else "Belum terdaftar di CHATIVA",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (contact.isRegistered) {
                    ElevatedButton(
                        onClick = {
                            val peerId = contact.registeredUserId ?: contact.id
                            val chat = mainViewModel.startOrGetChat(currentUserId, currentUserName, peerId, contact.name)
                            onStartChat(chat.id, contact.name, peerId)
                        }
                    ) {
                        Icon(Icons.Default.ChatBubble, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pesan")
                    }
                    ElevatedButton(onClick = { onVoiceCall(contact.name) }) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Suara")
                    }
                    ElevatedButton(onClick = { onVideoCall(contact.name) }) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Video")
                    }
                } else {
                    Button(
                        onClick = {
                            val inviteIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Halo! Gabung di CHATIVA bersama saya!")
                            }
                            context.startActivity(Intent.createChooser(inviteIntent, "Undang Teman via"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("UNDANG KE CHATIVA")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contact Info Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Nomor Telepon", fontSize = 12.sp, color = Color.Gray)
                            Text(contact.phoneNumber, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (contact.email.isNotEmpty()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Email", fontSize = 12.sp, color = Color.Gray)
                                Text(contact.email, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Info tentang kontak", fontSize = 12.sp, color = Color.Gray)
                            Text(contact.about.ifEmpty { "Halo! Saya menggunakan CHATIVA." }, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

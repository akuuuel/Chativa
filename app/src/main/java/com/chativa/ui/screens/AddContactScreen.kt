package com.chativa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    mainViewModel: MainViewModel,
    currentUserId: String = "current_user",
    currentUserName: String = "Saya",
    onNavigateBack: () -> Unit,
    onContactSaved: (chatId: String, peerName: String, peerId: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambah Kontak Baru") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Isi formulir di bawah ini untuk menambahkan kontak baru ke CHATIVA.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Kontak *") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nama") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Nomor Telepon *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Telepon") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email (Opsional)") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                enabled = !isSaving,
                onClick = {
                    if (name.isBlank() || phone.isBlank()) {
                        Toast.makeText(context, "Nama dan Nomor Telepon wajib diisi", Toast.LENGTH_SHORT).show()
                    } else {
                        isSaving = true
                        coroutineScope.launch {
                            val newContact = mainViewModel.addContact(name, phone, email)
                            val peerId = newContact.registeredUserId ?: newContact.id
                            val chat = mainViewModel.startOrGetChat(
                                currentUserId = currentUserId,
                                currentUserName = currentUserName,
                                targetUserId = peerId,
                                targetUserName = newContact.name
                            )
                            isSaving = false
                            Toast.makeText(context, "Kontak $name disimpan! Membuka ruang percakapan...", Toast.LENGTH_SHORT).show()
                            onContactSaved(chat.id, newContact.name, peerId)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("SIMPAN KONTAK & MULAI CHAT", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    }
}

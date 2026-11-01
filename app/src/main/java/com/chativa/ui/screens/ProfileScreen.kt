package com.chativa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.data.model.User
import com.chativa.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    currentUser: User,
    authViewModel: AuthViewModel,
    onNavigateToPrivacySettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var isEditingAbout by remember { mutableStateOf(false) }
    var isEditingPhone by remember { mutableStateOf(false) }

    var nameText by remember(currentUser) { mutableStateOf(currentUser.name) }
    var aboutText by remember(currentUser) { mutableStateOf(currentUser.about) }
    var phoneText by remember(currentUser) { mutableStateOf(currentUser.phoneNumber) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture Avatar
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentUser.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp
                )
            }
            FloatingActionButton(
                onClick = { /* Pick Image from Gallery */ },
                modifier = Modifier.size(36.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Ubah Foto", modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Info Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Name Item
                ProfileInfoRow(
                    icon = Icons.Default.Person,
                    title = "Nama",
                    value = nameText,
                    isEditing = isEditingName,
                    onValueChange = { nameText = it },
                    onEditToggle = {
                        if (isEditingName) {
                            authViewModel.updateProfile(nameText, aboutText, phoneText)
                        }
                        isEditingName = !isEditingName
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // About Item
                ProfileInfoRow(
                    icon = Icons.Default.Info,
                    title = "Info tentang Anda",
                    value = aboutText,
                    isEditing = isEditingAbout,
                    onValueChange = { aboutText = it },
                    onEditToggle = {
                        if (isEditingAbout) {
                            authViewModel.updateProfile(nameText, aboutText, phoneText)
                        }
                        isEditingAbout = !isEditingAbout
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Phone Item
                ProfileInfoRow(
                    icon = Icons.Default.Phone,
                    title = "Nomor Telepon",
                    value = phoneText.ifEmpty { "+628..." },
                    isEditing = isEditingPhone,
                    onValueChange = { phoneText = it },
                    onEditToggle = {
                        if (isEditingPhone) {
                            authViewModel.updateProfile(nameText, aboutText, phoneText)
                        }
                        isEditingPhone = !isEditingPhone
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Email Display (Read only)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Alamat Email", fontSize = 12.sp, color = Color.Gray)
                        Text(currentUser.email.ifEmpty { "email@chativa.com" }, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Shortcuts Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Surface(
                    onClick = onNavigateToPrivacySettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Privasi & Keamanan", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Status online, Terakhir dilihat, Blokir kontak") },
                        leadingContent = { Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
                HorizontalDivider()
                Surface(
                    onClick = onNavigateToThemeSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Tampilan & Tema", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Mode gelap, Warna utama, Wallpaper chat") },
                        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("KELUAR DARI AKUN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    onEditToggle: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 12.sp, color = Color.Gray)
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        IconButton(onClick = onEditToggle) {
            Icon(
                imageVector = if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                contentDescription = "Edit $title",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

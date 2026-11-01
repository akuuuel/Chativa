package com.chativa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.data.remote.SupabaseConfig
import com.chativa.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSettingsScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit
) {
    val context = LocalContext.current
    val themeSettings by mainViewModel.themeSettings.collectAsState()
    val supabaseUrl by mainViewModel.supabaseUrl.collectAsState()
    val supabaseAnonKey by mainViewModel.supabaseAnonKey.collectAsState()

    var soundEnabled by remember(themeSettings) { mutableStateOf(themeSettings.soundEnabled) }
    var vibrationEnabled by remember(themeSettings) { mutableStateOf(themeSettings.vibrationEnabled) }

    var showSupabaseDialog by remember { mutableStateOf(false) }
    var editUrl by remember { mutableStateOf("") }
    var editKey by remember { mutableStateOf("") }

    if (showSupabaseDialog) {
        AlertDialog(
            onDismissRequest = { showSupabaseDialog = false },
            title = { Text("Konfigurasi Supabase", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Masukkan URL Proyek dan Anon Key Supabase Anda:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editUrl,
                        onValueChange = { editUrl = it },
                        label = { Text("Supabase URL") },
                        placeholder = { Text("https://xxx.supabase.co") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editKey,
                        onValueChange = { editKey = it },
                        label = { Text("Supabase Anon Key") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            editUrl = SupabaseConfig.DEFAULT_SUPABASE_URL
                            editKey = SupabaseConfig.DEFAULT_SUPABASE_ANON_KEY
                        }
                    ) {
                        Text("Reset ke Default Proyek", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editUrl.isNotBlank() && editKey.isNotBlank()) {
                            mainViewModel.updateSupabaseConfig(editUrl.trim(), editKey.trim())
                            Toast.makeText(context, "Konfigurasi Supabase berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                            showSupabaseDialog = false
                        } else {
                            Toast.makeText(context, "URL dan Anon Key tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSupabaseDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Aplikasi") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Pengaturan Umum", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Tampilan & Warna Tema") },
                        supportingContent = { Text("Mode gelap, warna utama, wallpaper") },
                        leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToThemeSettings() }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Privasi & Keamanan") },
                        supportingContent = { Text("Status online, foto profil, blokir kontak") },
                        leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToPrivacySettings() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Backend & Database", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Koneksi Supabase") },
                        supportingContent = {
                            Text(
                                if (supabaseUrl.isNotEmpty()) supabaseUrl else "Belum dikonfigurasi",
                                maxLines = 1
                            )
                        },
                        leadingContent = { Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingContent = {
                            FilledTonalButton(
                                onClick = {
                                    editUrl = supabaseUrl
                                    editKey = supabaseAnonKey
                                    showSupabaseDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Ubah", fontSize = 12.sp)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Notifikasi & Suara", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Suara Notifikasi") },
                        supportingContent = { Text("Putar suara saat pesan atau panggilan masuk") },
                        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = soundEnabled,
                                onCheckedChange = {
                                    soundEnabled = it
                                    mainViewModel.setSoundEnabled(it)
                                }
                            )
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Getar Notifikasi") },
                        supportingContent = { Text("Getar perangkat saat menerima notifikasi") },
                        leadingContent = { Icon(Icons.Default.Vibration, contentDescription = null) },
                        trailingContent = {
                            Switch(
                                checked = vibrationEnabled,
                                onCheckedChange = {
                                    vibrationEnabled = it
                                    mainViewModel.setVibrationEnabled(it)
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Tentang Aplikasi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("CHATIVA v1.0.0") },
                        supportingContent = { Text("Aplikasi Komunikasi Instan & Panggilan") },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
            }
        }
    }
}

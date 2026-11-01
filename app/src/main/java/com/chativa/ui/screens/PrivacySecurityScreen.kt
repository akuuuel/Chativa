package com.chativa.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.data.model.PrivacyOption
import com.chativa.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val privacySettings by mainViewModel.privacySettings.collectAsState()
    val context = LocalContext.current

    var showReportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privasi & Keamanan") },
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
            Text("Siapa yang Dapat Melihat Info Anda", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    // Status Online Option
                    PrivacyOptionSelector(
                        title = "Status Online",
                        selectedOption = privacySettings.whoCanSeeOnline,
                        onOptionSelected = { mainViewModel.setPrivacyOnline(it) }
                    )
                    HorizontalDivider()

                    // Last Seen Option
                    PrivacyOptionSelector(
                        title = "Terakhir Dilihat (Last Seen)",
                        selectedOption = privacySettings.whoCanSeeLastSeen,
                        onOptionSelected = { mainViewModel.setPrivacyLastSeen(it) }
                    )
                    HorizontalDivider()

                    // Profile Pic Option
                    PrivacyOptionSelector(
                        title = "Foto Profil",
                        selectedOption = privacySettings.whoCanSeeProfilePic,
                        onOptionSelected = { mainViewModel.setPrivacyProfilePic(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Laporan Pembacaan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                ListItem(
                    headlineContent = { Text("Tanda Centang Biru (Laporan Dibaca)") },
                    supportingContent = { Text("Jika dimatikan, Anda tidak dapat melihat atau mengirim laporan pembacaan pesan.") },
                    trailingContent = {
                        Switch(
                            checked = privacySettings.showReadReceipts,
                            onCheckedChange = { mainViewModel.setReadReceipts(it) }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Keamanan & Blokir", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Kontak Diblokir") },
                        supportingContent = { Text("${privacySettings.blockedUserIds.size} kontak diblokir") },
                        leadingContent = { Icon(Icons.Default.Block, contentDescription = null, tint = Color.Red) },
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Daftar kontak diblokir", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Laporkan Masalah / Akun", color = Color.Red) },
                        supportingContent = { Text("Kirim laporan tentang penyalahgunaan atau masalah akun") },
                        leadingContent = { Icon(Icons.Default.Report, contentDescription = null, tint = Color.Red) },
                        modifier = Modifier.clickable { showReportDialog = true }
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Laporkan Akun / Masalah") },
            text = { Text("Terima kasih telah membantu menjaga keamanan komunitas CHATIVA. Tim kami akan meninjau laporan Anda.") },
            confirmButton = {
                TextButton(onClick = {
                    showReportDialog = false
                    Toast.makeText(context, "Laporan Anda telah terkirim.", Toast.LENGTH_SHORT).show()
                }) {
                    Text("KIRIM LAPORAN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("BATAL")
                }
            }
        )
    }
}

@Composable
fun PrivacyOptionSelector(
    title: String,
    selectedOption: PrivacyOption,
    onOptionSelected: (PrivacyOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                text = when (selectedOption) {
                    PrivacyOption.EVERYONE -> "Semua Orang"
                    PrivacyOption.MY_CONTACTS -> "Kontak Saya"
                    PrivacyOption.NOBODY -> "Tidak Ada"
                },
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FilterChip(
                    selected = selectedOption == PrivacyOption.EVERYONE,
                    onClick = {
                        onOptionSelected(PrivacyOption.EVERYONE)
                        expanded = false
                    },
                    label = { Text("Semua") }
                )
                FilterChip(
                    selected = selectedOption == PrivacyOption.MY_CONTACTS,
                    onClick = {
                        onOptionSelected(PrivacyOption.MY_CONTACTS)
                        expanded = false
                    },
                    label = { Text("Kontak") }
                )
                FilterChip(
                    selected = selectedOption == PrivacyOption.NOBODY,
                    onClick = {
                        onOptionSelected(PrivacyOption.NOBODY)
                        expanded = false
                    },
                    label = { Text("Tidak Ada") }
                )
            }
        }
    }
}

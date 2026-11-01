package com.chativa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chativa.data.model.ThemeMode
import com.chativa.ui.theme.parseHexColor
import com.chativa.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val themeSettings by mainViewModel.themeSettings.collectAsState()

    val colorOptions = listOf(
        "#673AB7" to "Ungu CHATIVA",
        "#0288D1" to "Biru Laut",
        "#009688" to "Teal",
        "#3F51B5" to "Indigo",
        "#FF5722" to "Jingga",
        "#4CAF50" to "Hijau"
    )

    val bubbleColorOptions = listOf(
        "#E8EAF6" to "Nila Muda",
        "#E0F7FA" to "Sian Muda",
        "#F3E5F5" to "Ungu Muda",
        "#FFF3E0" to "Krem",
        "#DCEDC8" to "Hijau Muda"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan Tema") },
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
            Text("Mode Tampilan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Ikuti Sistem") },
                        leadingContent = {
                            RadioButton(
                                selected = themeSettings.themeMode == ThemeMode.SYSTEM,
                                onClick = { mainViewModel.setThemeMode(ThemeMode.SYSTEM) }
                            )
                        },
                        modifier = Modifier.clickable { mainViewModel.setThemeMode(ThemeMode.SYSTEM) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Mode Terang (Light Mode)") },
                        leadingContent = {
                            RadioButton(
                                selected = themeSettings.themeMode == ThemeMode.LIGHT,
                                onClick = { mainViewModel.setThemeMode(ThemeMode.LIGHT) }
                            )
                        },
                        modifier = Modifier.clickable { mainViewModel.setThemeMode(ThemeMode.LIGHT) }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Mode Gelap (Dark Mode)") },
                        leadingContent = {
                            RadioButton(
                                selected = themeSettings.themeMode == ThemeMode.DARK,
                                onClick = { mainViewModel.setThemeMode(ThemeMode.DARK) }
                            )
                        },
                        modifier = Modifier.clickable { mainViewModel.setThemeMode(ThemeMode.DARK) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Warna Utama Aplikasi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    colorOptions.forEach { (hex, label) ->
                        val color = parseHexColor(hex)
                        val isSelected = themeSettings.primaryColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { mainViewModel.setPrimaryColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = label, tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Warna Gelembung Chat", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    bubbleColorOptions.forEach { (hex, label) ->
                        val color = parseHexColor(hex)
                        val isSelected = themeSettings.chatBubbleColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { mainViewModel.setChatBubbleColor(hex) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

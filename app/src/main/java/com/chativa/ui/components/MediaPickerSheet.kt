package com.chativa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaPickerSheet(
    onDismiss: () -> Unit,
    onSelectImage: () -> Unit,
    onSelectVideo: () -> Unit,
    onSelectDocument: () -> Unit,
    onSelectVoice: () -> Unit,
    onSelectLocation: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bagikan Media & Berkas",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MediaOptionItem(
                    icon = Icons.Default.Image,
                    label = "Galeri Foto",
                    bgColor = Color(0xFF4CAF50),
                    onClick = {
                        onSelectImage()
                        onDismiss()
                    }
                )
                MediaOptionItem(
                    icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                    label = "Dokumen / File",
                    bgColor = Color(0xFF2196F3),
                    onClick = {
                        onSelectDocument()
                        onDismiss()
                    }
                )
                MediaOptionItem(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    bgColor = Color(0xFFE91E63),
                    onClick = {
                        onSelectVideo()
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MediaOptionItem(
                    icon = Icons.Default.Mic,
                    label = "Pesan Suara",
                    bgColor = Color(0xFFFF9800),
                    onClick = {
                        onSelectVoice()
                        onDismiss()
                    }
                )
                MediaOptionItem(
                    icon = Icons.Default.LocationOn,
                    label = "Lokasi",
                    bgColor = Color(0xFF9C27B0),
                    onClick = {
                        onSelectLocation()
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.width(60.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MediaOptionItem(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

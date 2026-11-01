package com.chativa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.chativa.ui.theme.ChativaPurplePrimary
import com.chativa.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoCallScreen(
    peerName: String,
    mainViewModel: MainViewModel,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isCameraOff by remember { mutableStateOf(false) }
    var durationSec by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            durationSec++
        }
    }

    val minutes = durationSec / 60
    val seconds = durationSec % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Peer Remote Video Frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ChativaPurplePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = peerName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = peerName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timeFormatted,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
        }

        // Local Self Camera Preview Window
        Box(
            modifier = Modifier
                .padding(24.dp)
                .size(width = 110.dp, height = 150.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isCameraOff) Color.Gray else Color(0xFF333333))
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            if (isCameraOff) {
                Icon(Icons.Default.VideocamOff, contentDescription = "Kamera Mati", tint = Color.White)
            } else {
                Text("Kamera Anda", fontSize = 11.sp, color = Color.White)
            }
        }

        // Call Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera On/Off Button
            FloatingActionButton(
                onClick = { isCameraOff = !isCameraOff },
                containerColor = if (isCameraOff) Color.White else Color.White.copy(alpha = 0.3f),
                contentColor = if (isCameraOff) Color.Black else Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    contentDescription = "Kamera"
                )
            }

            // Mute Mic Button
            FloatingActionButton(
                onClick = { isMuted = !isMuted },
                containerColor = if (isMuted) Color.White else Color.White.copy(alpha = 0.3f),
                contentColor = if (isMuted) Color.Black else Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mikrofon"
                )
            }

            // End Call Button
            FloatingActionButton(
                onClick = {
                    mainViewModel.endCall()
                    onEndCall()
                },
                containerColor = Color.Red,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(68.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Akhiri Panggilan",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

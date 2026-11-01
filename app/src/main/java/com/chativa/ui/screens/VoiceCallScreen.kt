package com.chativa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
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
fun VoiceCallScreen(
    peerName: String,
    mainViewModel: MainViewModel,
    onEndCall: () -> Unit
) {
    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(true) }
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
            .background(ChativaPurplePrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(32.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Panggilan Suara",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = peerName,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = timeFormatted,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Peer Avatar Circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = peerName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 54.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Mic Button
                FloatingActionButton(
                    onClick = {
                        isMuted = !isMuted
                        mainViewModel.toggleCallMute()
                    },
                    containerColor = if (isMuted) Color.White else Color.White.copy(alpha = 0.3f),
                    contentColor = if (isMuted) ChativaPurplePrimary else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mikrofon",
                        modifier = Modifier.size(28.dp)
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
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Akhiri Panggilan",
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Speaker Button
                FloatingActionButton(
                    onClick = {
                        isSpeakerOn = !isSpeakerOn
                        mainViewModel.toggleCallSpeaker()
                    },
                    containerColor = if (isSpeakerOn) Color.White else Color.White.copy(alpha = 0.3f),
                    contentColor = if (isSpeakerOn) ChativaPurplePrimary else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speaker",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

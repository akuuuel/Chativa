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
import com.chativa.data.model.CallLog
import com.chativa.data.model.CallStatus
import com.chativa.data.model.CallType
import com.chativa.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallsScreen(
    mainViewModel: MainViewModel,
    onVoiceCall: (peerName: String) -> Unit,
    onVideoCall: (peerName: String) -> Unit
) {
    val callLogs by mainViewModel.callLogs.collectAsState()
    val context = LocalContext.current

    var selectedLogForDelete by remember { mutableStateOf<Pair<CallLog, String>?>(null) }

    if (callLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada riwayat panggilan.",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(callLogs) { log ->
                val peerName = if (log.callerName == "Saya") log.receiverName else log.callerName
                CallItemRow(
                    log = log,
                    peerName = peerName,
                    onCallBackVoice = { onVoiceCall(peerName) },
                    onCallBackVideo = { onVideoCall(peerName) },
                    onLongClick = {
                        selectedLogForDelete = Pair(log, peerName)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            }
        }
    }

    // Delete Call Log Confirmation Dialog
    selectedLogForDelete?.let { (logToDelete, peerName) ->
        AlertDialog(
            onDismissRequest = { selectedLogForDelete = null },
            title = { Text("Hapus Riwayat Panggilan") },
            text = {
                Text("Apakah Anda yakin ingin menghapus riwayat panggilan dengan \"$peerName\"?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mainViewModel.deleteCallLog(logToDelete.id)
                        Toast.makeText(context, "Riwayat panggilan dengan $peerName telah dihapus", Toast.LENGTH_SHORT).show()
                        selectedLogForDelete = null
                    }
                ) {
                    Text("HAPUS", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedLogForDelete = null }) {
                    Text("BATAL")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CallItemRow(
    log: CallLog,
    peerName: String,
    onCallBackVoice: () -> Unit,
    onCallBackVideo: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = peerName.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = peerName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = if (log.status == CallStatus.MISSED) Color.Red else MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                val (statusIcon, iconColor) = when (log.status) {
                    CallStatus.INCOMING -> Icons.Default.CallReceived to Color(0xFF4CAF50)
                    CallStatus.OUTGOING -> Icons.Default.CallMade to Color(0xFF2196F3)
                    CallStatus.MISSED -> Icons.Default.CallMissed to Color.Red
                }

                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                val formattedTime = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                Text(
                    text = "$formattedTime • ${if (log.durationSec > 0) "${log.durationSec}s" else "Tidak dijawab"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        IconButton(onClick = if (log.type == CallType.VIDEO) onCallBackVideo else onCallBackVoice) {
            Icon(
                imageVector = if (log.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Panggil Kembali",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

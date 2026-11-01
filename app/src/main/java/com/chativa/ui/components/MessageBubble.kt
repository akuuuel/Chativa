package com.chativa.ui.components

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Shortcut
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.chativa.data.model.Message
import com.chativa.data.model.MessageStatus
import com.chativa.data.model.MessageType
import com.chativa.ui.theme.LocalChativaCustomColors
import com.chativa.ui.theme.ReadTickBlue
import com.chativa.ui.theme.SentTickGray
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isFromMe: Boolean,
    onLongClick: () -> Unit = {},
    onReplyClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val customColors = LocalChativaCustomColors.current
    val bubbleColor = if (isFromMe) {
        customColors.primaryColor.copy(alpha = 0.15f)
    } else {
        customColors.chatBubbleColor
    }

    val alignment = if (isFromMe) Alignment.End else Alignment.Start
    val shape = if (isFromMe) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    var isVoicePlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var showFullImageDialog by remember { mutableStateOf(false) }

    DisposableEffect(message.id) {
        onDispose {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (_: Exception) {}
            mediaPlayer = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            color = bubbleColor,
            shape = shape,
            shadowElevation = 1.dp,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = onLongClick
                )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {

                // Forwarded tag
                if (message.isForwarded) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Shortcut,
                            contentDescription = "Forwarded",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Diteruskan",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Reply snippet
                if (!message.replyToText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(customColors.primaryColor.copy(alpha = 0.1f))
                            .clickable { onReplyClick() }
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = message.replyToSender ?: "Pesan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = customColors.primaryColor
                            )
                            Text(
                                text = message.replyToText,
                                fontSize = 12.sp,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Content by message type
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.text,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    MessageType.IMAGE -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Gray.copy(alpha = 0.2f))
                                    .clickable {
                                        if (message.mediaUrl.isNotBlank()) {
                                            showFullImageDialog = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (message.mediaUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = message.mediaUrl,
                                        contentDescription = "Foto",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Foto",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            if (message.text.isNotEmpty() &&
                                message.text != "Foto dari Galeri" &&
                                message.text != "Foto Kamera") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.text,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    MessageType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.DarkGray)
                                .clickable {
                                    if (message.mediaUrl.isNotBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.mediaUrl)).apply {
                                                setDataAndType(Uri.parse(message.mediaUrl), "video/*")
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Tidak dapat memutar video", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Putar Video",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    MessageType.DOCUMENT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable {
                                    if (message.mediaUrl.isNotBlank()) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.mediaUrl))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Tidak dapat membuka berkas: tidak ada aplikasi yang cocok", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Tautan berkas tidak tersedia", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = "Dokumen",
                                tint = customColors.primaryColor,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = message.fileName.ifEmpty { message.text.ifEmpty { "Dokumen" } },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = message.fileSize.ifEmpty { "Berkas Unduhan" },
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Unduh/Buka Berkas",
                                tint = customColors.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    MessageType.VOICE -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (message.mediaUrl.isNotBlank()) {
                                        if (isVoicePlaying) {
                                            try {
                                                mediaPlayer?.pause()
                                            } catch (_: Exception) {}
                                            isVoicePlaying = false
                                        } else {
                                            try {
                                                if (mediaPlayer == null) {
                                                    mediaPlayer = MediaPlayer().apply {
                                                        if (message.mediaUrl.startsWith("content://")) {
                                                            setDataSource(context, Uri.parse(message.mediaUrl))
                                                        } else {
                                                            setDataSource(message.mediaUrl)
                                                        }
                                                        prepareAsync()
                                                        setOnPreparedListener {
                                                            start()
                                                            isVoicePlaying = true
                                                        }
                                                        setOnCompletionListener {
                                                            isVoicePlaying = false
                                                        }
                                                        setOnErrorListener { _, _, _ ->
                                                            isVoicePlaying = false
                                                            Toast.makeText(context, "Gagal memutar pesan suara", Toast.LENGTH_SHORT).show()
                                                            true
                                                        }
                                                    }
                                                } else {
                                                    mediaPlayer?.start()
                                                    isVoicePlaying = true
                                                }
                                            } catch (e: Exception) {
                                                isVoicePlaying = false
                                                Toast.makeText(context, "Gagal memutar pesan suara", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Tautan audio tidak ditemukan", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isVoicePlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = "Voice Note",
                                    tint = customColors.primaryColor,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            LinearProgressIndicator(
                                progress = { if (isVoicePlaying) 0.7f else 0.0f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = customColors.primaryColor,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (message.voiceDurationSec > 0) "${message.voiceDurationSec} dtk" else "Voice",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    MessageType.LOCATION -> {
                        Column(
                            modifier = Modifier.clickable {
                                try {
                                    val geoUri = Uri.parse("geo:${message.latitude},${message.longitude}?q=${message.latitude},${message.longitude}(${Uri.encode("Lokasi CHATIVA")})")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Tidak ada aplikasi peta ditemukan", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Lokasi",
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lokasi Terkini (Ketuk utk Buka)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = customColors.primaryColor
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lat: ${message.latitude}, Long: ${message.longitude}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Footer: timestamp & checkmarks
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.isEdited) {
                        Text(
                            text = "(diedit) ",
                            fontSize = 10.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (isFromMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        when (message.status) {
                            MessageStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Terkirim (1 Centang)",
                                    tint = SentTickGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            MessageStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Diterima (2 Centang)",
                                    tint = SentTickGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            MessageStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Dibaca (2 Centang Biru)",
                                    tint = ReadTickBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Screen Image Dialog
    if (showFullImageDialog && message.mediaUrl.isNotBlank()) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { showFullImageDialog = false },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = message.mediaUrl,
                    contentDescription = "Foto Penuh",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

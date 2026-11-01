package com.chativa.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.chativa.data.model.Message
import com.chativa.data.model.MessageType
import com.chativa.ui.components.MediaPickerSheet
import com.chativa.ui.components.MessageBubble
import com.chativa.ui.viewmodel.MainViewModel
import com.chativa.utils.AudioRecorder
import com.chativa.utils.MediaStorageHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    chatId: String,
    peerName: String,
    peerId: String,
    currentUserId: String = "current_user",
    currentUserName: String = "Saya",
    mainViewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onVoiceCall: (peerName: String) -> Unit,
    onVideoCall: (peerName: String) -> Unit
) {
    var textState by remember { mutableStateOf("") }
    var showMediaSheet by remember { mutableStateOf(false) }
    var selectedMessageForOptions by remember { mutableStateOf<Message?>(null) }
    var replyToMessage by remember { mutableStateOf<Message?>(null) }
    var editingMessage by remember { mutableStateOf<Message?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadStatusText by remember { mutableStateOf("Mengunggah...") }

    // Voice Note State
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var currentRecordFile by remember { mutableStateOf<File?>(null) }

    var isSearching by remember { mutableStateOf(false) }
    var searchQueryInChat by remember { mutableStateOf("") }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val typingStatusMap by mainViewModel.chatRepository.typingStatusMap.collectAsState()
    val isPeerTyping = typingStatusMap[chatId] == true

    val messagesMap by mainViewModel.chatRepository.messagesMap.collectAsState()
    val messages = remember(chatId, messagesMap) {
        mainViewModel.chatRepository.getMessagesForChat(chatId)
    }

    val filteredMessages = remember(messages, searchQueryInChat) {
        if (searchQueryInChat.isBlank()) messages
        else messages.filter { it.text.contains(searchQueryInChat, ignoreCase = true) }
    }

    val audioRecorder = remember { AudioRecorder(context) }

    // Timer while recording
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingDurationSec = 0
            while (isActive && isRecordingAudio) {
                delay(1000)
                recordingDurationSec++
            }
        }
    }

    // Function to start audio recording
    fun startRecording() {
        val audioFile = File(context.cacheDir, "vn_${System.currentTimeMillis()}.m4a")
        currentRecordFile = audioFile
        if (audioRecorder.start(audioFile)) {
            isRecordingAudio = true
            recordingDurationSec = 0
            Toast.makeText(context, "Merekam suara... Ketuk kirim jika selesai", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Gagal memulai perekaman audio", Toast.LENGTH_SHORT).show()
        }
    }

    // Audio Permission Launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecording()
        } else {
            Toast.makeText(context, "Izin mikrofon diperlukan untuk merekam pesan suara", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestStartRecording() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startRecording()
        } else {
            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadStatusText = "Mengunggah gambar..."
            coroutineScope.launch {
                val uploadedUrl = MediaStorageHelper.uploadImage(context, uri)
                isUploading = false
                if (!uploadedUrl.isNullOrBlank()) {
                    mainViewModel.sendMessage(
                        chatId = chatId,
                        senderId = currentUserId,
                        receiverId = peerId,
                        senderName = currentUserName,
                        text = "Foto dari Galeri",
                        type = MessageType.IMAGE,
                        mediaUrl = uploadedUrl
                    )
                    Toast.makeText(context, "Foto berhasil dikirim!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal mengunggah foto ke penyimpanan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // File / Document Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            uploadStatusText = "Mengunggah berkas..."
            coroutineScope.launch {
                val uploadResult = MediaStorageHelper.uploadFile(context, uri)
                isUploading = false
                if (uploadResult != null) {
                    mainViewModel.sendMessage(
                        chatId = chatId,
                        senderId = currentUserId,
                        receiverId = peerId,
                        senderName = currentUserName,
                        text = uploadResult.fileName,
                        type = MessageType.DOCUMENT,
                        mediaUrl = uploadResult.url,
                        fileName = uploadResult.fileName,
                        fileSize = uploadResult.fileSizeFormatted
                    )
                    Toast.makeText(context, "Berkas '${uploadResult.fileName}' berhasil dikirim!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal mengunggah berkas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(chatId) {
        mainViewModel.markMessagesAsRead(chatId, currentUserId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQueryInChat,
                            onValueChange = { searchQueryInChat = it },
                            placeholder = { Text("Cari dlm chat...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = peerName.take(1).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = peerName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPeerTyping) "Online • mengetik..." else "Online",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSearching) {
                            isSearching = false
                            searchQueryInChat = ""
                        } else {
                            mainViewModel.setTyping(chatId, false)
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                actions = {
                    IconButton(onClick = { isSearching = !isSearching }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari Pesan")
                    }
                    IconButton(onClick = { onVoiceCall(peerName) }) {
                        Icon(Icons.Default.Call, contentDescription = "Panggilan Suara")
                    }
                    IconButton(onClick = { onVideoCall(peerName) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Panggilan Video")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Uploading Indicator Banner
            if (isUploading) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uploadStatusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Message List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(filteredMessages) { msg ->
                    MessageBubble(
                        message = msg,
                        isFromMe = msg.senderId == currentUserId,
                        onLongClick = { selectedMessageForOptions = msg },
                        onReplyClick = { replyToMessage = msg }
                    )
                }
            }

            // Reply Preview Banner
            replyToMessage?.let { replyMsg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Membalas ${replyMsg.senderName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = replyMsg.text,
                            fontSize = 13.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { replyToMessage = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Batal Balas", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Edit Preview Banner
            editingMessage?.let { _ ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mengedit pesan...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        editingMessage = null
                        textState = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Batal Edit", modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRecordingAudio) {
                    // Dedicated Voice Recording Bar (Active Recording)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cancel / Discard Button
                        IconButton(
                            onClick = {
                                audioRecorder.cancel()
                                currentRecordFile?.delete()
                                currentRecordFile = null
                                isRecordingAudio = false
                                recordingDurationSec = 0
                                Toast.makeText(context, "Rekaman dibatalkan", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Batalkan rekaman",
                                tint = Color.Red,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Red Recording Indicator + Live Duration
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            val min = recordingDurationSec / 60
                            val sec = recordingDurationSec % 60
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", min, sec),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Merekam suara...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        // Send Voice Note Button
                        IconButton(
                            onClick = {
                                val durationSec = audioRecorder.stop()
                                isRecordingAudio = false
                                val recordFile = currentRecordFile
                                currentRecordFile = null
                                if (recordFile != null && recordFile.exists()) {
                                    isUploading = true
                                    uploadStatusText = "Mengunggah pesan suara..."
                                    coroutineScope.launch {
                                        val uploadedUrl = MediaStorageHelper.uploadAudio(recordFile)
                                        isUploading = false
                                        if (!uploadedUrl.isNullOrBlank()) {
                                            mainViewModel.sendMessage(
                                                chatId = chatId,
                                                senderId = currentUserId,
                                                receiverId = peerId,
                                                senderName = currentUserName,
                                                text = "Pesan Suara",
                                                type = MessageType.VOICE,
                                                mediaUrl = uploadedUrl,
                                                voiceDurationSec = durationSec.coerceAtLeast(1)
                                            )
                                            Toast.makeText(context, "Pesan suara terkirim!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Gagal mengunggah pesan suara", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim Pesan Suara",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    // Standard Input Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showMediaSheet = true }) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Lampiran Media",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        OutlinedTextField(
                            value = textState,
                            onValueChange = {
                                textState = it
                                mainViewModel.setTyping(chatId, it.isNotEmpty())
                            },
                            placeholder = { Text("Ketik pesan...") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )

                        if (textState.isNotBlank()) {
                            // Send Text Message Button
                            IconButton(
                                onClick = {
                                    val txt = textState.trim()
                                    if (txt.isNotEmpty()) {
                                        if (editingMessage != null) {
                                            mainViewModel.editMessage(chatId, editingMessage!!.id, txt)
                                            editingMessage = null
                                        } else {
                                            mainViewModel.sendMessage(
                                                chatId = chatId,
                                                senderId = currentUserId,
                                                receiverId = peerId,
                                                senderName = currentUserName,
                                                text = txt,
                                                type = MessageType.TEXT,
                                                replyToMessage = replyToMessage
                                            )
                                            replyToMessage = null
                                        }
                                        textState = ""
                                        mainViewModel.setTyping(chatId, false)
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Kirim",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Microphone Button for 1-click Voice Note
                            IconButton(
                                onClick = {
                                    requestStartRecording()
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Rekam Pesan Suara",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Media Picker Bottom Sheet
    if (showMediaSheet) {
        MediaPickerSheet(
            onDismiss = { showMediaSheet = false },
            onSelectImage = {
                imagePickerLauncher.launch("image/*")
            },
            onSelectVideo = {
                mainViewModel.sendMessage(
                    chatId = chatId,
                    senderId = currentUserId,
                    receiverId = peerId,
                    senderName = currentUserName,
                    text = "Video dikirim",
                    type = MessageType.VIDEO
                )
            },
            onSelectDocument = {
                filePickerLauncher.launch("*/*")
            },
            onSelectVoice = {
                requestStartRecording()
            },
            onSelectLocation = {
                val location = MediaStorageHelper.getCurrentLocation(context)
                if (location != null) {
                    mainViewModel.sendMessage(
                        chatId = chatId,
                        senderId = currentUserId,
                        receiverId = peerId,
                        senderName = currentUserName,
                        text = "Lokasi Saya",
                        type = MessageType.LOCATION,
                        latitude = location.first,
                        longitude = location.second
                    )
                    Toast.makeText(context, "Lokasi berhasil dikirim!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal mendapatkan lokasi GPS saat ini", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Message Long Click Options Dialog
    selectedMessageForOptions?.let { msg ->
        AlertDialog(
            onDismissRequest = { selectedMessageForOptions = null },
            title = { Text("Opsi Pesan") },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Salin Pesan") },
                        leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(msg.text))
                            Toast.makeText(context, "Pesan disalin ke papan klip", Toast.LENGTH_SHORT).show()
                            selectedMessageForOptions = null
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Balas (Reply)") },
                        leadingContent = { Icon(Icons.Default.Reply, contentDescription = null) },
                        modifier = Modifier.clickable {
                            replyToMessage = msg
                            selectedMessageForOptions = null
                        }
                    )
                    if (msg.senderId == currentUserId && msg.type == MessageType.TEXT) {
                        ListItem(
                            headlineContent = { Text("Edit Pesan") },
                            leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                            modifier = Modifier.clickable {
                                editingMessage = msg
                                textState = msg.text
                                selectedMessageForOptions = null
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text("Hapus Pesan", color = Color.Red) },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        modifier = Modifier.clickable {
                            mainViewModel.deleteMessage(chatId, msg.id)
                            Toast.makeText(context, "Pesan telah dihapus", Toast.LENGTH_SHORT).show()
                            selectedMessageForOptions = null
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedMessageForOptions = null }) {
                    Text("Tutup")
                }
            }
        )
    }
}

package com.chativa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.chativa.ui.components.ChativaLogo
import com.chativa.ui.viewmodel.AuthViewModel
import com.chativa.ui.viewmodel.MainViewModel

enum class DashboardTab {
    HOME,
    CHAT,
    CONTACTS,
    CALLS,
    PROFILE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    onNavigateToChatRoom: (String, String, String) -> Unit,
    onNavigateToAddContact: () -> Unit,
    onNavigateToContactDetail: (String) -> Unit,
    onNavigateToVoiceCall: (String) -> Unit,
    onNavigateToVideoCall: (String) -> Unit,
    onNavigateToAppSettings: () -> Unit,
    onNavigateToThemeSettings: () -> Unit,
    onNavigateToPrivacySettings: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.HOME) }
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        val uid = currentUser?.id
        if (!uid.isNullOrBlank() && uid != "current_user") {
            mainViewModel.startListeningToUserChats(uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChativaLogo(size = 36.dp, showTitle = false)
                        Text(
                            text = "CHATIVA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToThemeSettings) {
                        Icon(Icons.Default.Palette, contentDescription = "Tema")
                    }
                    IconButton(onClick = onNavigateToAppSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Pengaturan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.HOME,
                    onClick = { selectedTab = DashboardTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                    label = { Text("Beranda", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.CHAT,
                    onClick = { selectedTab = DashboardTab.CHAT },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chat") },
                    label = { Text("Chat", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.CONTACTS,
                    onClick = { selectedTab = DashboardTab.CONTACTS },
                    icon = { Icon(Icons.Default.Contacts, contentDescription = "Kontak") },
                    label = { Text("Kontak", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.CALLS,
                    onClick = { selectedTab = DashboardTab.CALLS },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Telepon") },
                    label = { Text("Telepon", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == DashboardTab.PROFILE,
                    onClick = { selectedTab = DashboardTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                    label = { Text("Profil", fontSize = 11.sp) }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == DashboardTab.CHAT || selectedTab == DashboardTab.CONTACTS) {
                FloatingActionButton(
                    onClick = onNavigateToAddContact,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Kontak")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                DashboardTab.HOME -> HomeContent(
                    currentUser = currentUser,
                    mainViewModel = mainViewModel,
                    onNavigateToChatRoom = onNavigateToChatRoom,
                    onNavigateToContacts = { selectedTab = DashboardTab.CONTACTS }
                )
                DashboardTab.CHAT -> ChatListScreen(
                    mainViewModel = mainViewModel,
                    currentUserId = currentUser?.id ?: "current_user",
                    onChatClick = onNavigateToChatRoom
                )
                DashboardTab.CONTACTS -> ContactsScreen(
                    mainViewModel = mainViewModel,
                    currentUserId = currentUser?.id ?: "current_user",
                    currentUserName = currentUser?.name ?: "Saya",
                    onContactClick = onNavigateToContactDetail,
                    onStartChat = onNavigateToChatRoom,
                    onVoiceCall = onNavigateToVoiceCall,
                    onVideoCall = onNavigateToVideoCall
                )
                DashboardTab.CALLS -> CallsScreen(
                    mainViewModel = mainViewModel,
                    onVoiceCall = onNavigateToVoiceCall,
                    onVideoCall = onNavigateToVideoCall
                )
                DashboardTab.PROFILE -> ProfileScreen(
                    currentUser = currentUser ?: User(name = "Pengguna CHATIVA"),
                    authViewModel = authViewModel,
                    onNavigateToPrivacySettings = onNavigateToPrivacySettings,
                    onNavigateToThemeSettings = onNavigateToThemeSettings,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun HomeContent(
    currentUser: User?,
    mainViewModel: MainViewModel,
    onNavigateToChatRoom: (String, String, String) -> Unit,
    onNavigateToContacts: () -> Unit
) {
    val searchQuery by mainViewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User Greeting Header
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (currentUser?.name?.take(1) ?: "C").uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Selamat Datang,",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currentUser?.name ?: "Pengguna CHATIVA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { mainViewModel.updateSearchQuery(it) },
            placeholder = { Text("Cari kontak atau percakapan...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { mainViewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Hapus")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Percakapan Terbaru",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        ChatListScreen(
            mainViewModel = mainViewModel,
            currentUserId = currentUser?.id ?: "current_user",
            onChatClick = onNavigateToChatRoom
        )
    }
}

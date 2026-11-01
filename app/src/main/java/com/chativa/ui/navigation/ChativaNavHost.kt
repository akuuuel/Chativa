package com.chativa.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chativa.ui.screens.*
import com.chativa.ui.viewmodel.AuthViewModel
import com.chativa.ui.viewmodel.MainViewModel

@Composable
fun ChativaNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // 1. Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    if (currentUser != null) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // 2. Login Screen
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        // 3. Register Screen
        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // 4. Forgot Password Screen
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 5. Dashboard / Home Screen
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                authViewModel = authViewModel,
                mainViewModel = mainViewModel,
                onNavigateToChatRoom = { chatId, peerName, peerId ->
                    navController.navigate(Screen.ChatRoom.createRoute(chatId, peerName, peerId))
                },
                onNavigateToAddContact = { navController.navigate(Screen.AddContact.route) },
                onNavigateToContactDetail = { contactId ->
                    navController.navigate(Screen.ContactDetail.createRoute(contactId))
                },
                onNavigateToVoiceCall = { peerName ->
                    navController.navigate(Screen.VoiceCall.createRoute(peerName))
                },
                onNavigateToVideoCall = { peerName ->
                    navController.navigate(Screen.VideoCall.createRoute(peerName))
                },
                onNavigateToAppSettings = { navController.navigate(Screen.AppSettings.route) },
                onNavigateToThemeSettings = { navController.navigate(Screen.ThemeSettings.route) },
                onNavigateToPrivacySettings = { navController.navigate(Screen.PrivacySettings.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // 7. Chat Room Screen
        composable(
            route = Screen.ChatRoom.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("peerName") { type = NavType.StringType },
                navArgument("peerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rawChatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val rawPeerName = backStackEntry.arguments?.getString("peerName") ?: "Kontak"
            val rawPeerId = backStackEntry.arguments?.getString("peerId") ?: ""

            val chatId = Uri.decode(rawChatId)
            val peerName = Uri.decode(rawPeerName)
            val peerId = Uri.decode(rawPeerId)

            ChatRoomScreen(
                chatId = chatId,
                peerName = peerName,
                peerId = peerId,
                currentUserId = currentUser?.id ?: "current_user",
                currentUserName = currentUser?.name ?: "Saya",
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onVoiceCall = { pName -> navController.navigate(Screen.VoiceCall.createRoute(pName)) },
                onVideoCall = { pName -> navController.navigate(Screen.VideoCall.createRoute(pName)) }
            )
        }

        // 8. Add Contact Screen
        composable(Screen.AddContact.route) {
            AddContactScreen(
                mainViewModel = mainViewModel,
                currentUserId = currentUser?.id ?: "current_user",
                currentUserName = currentUser?.name ?: "Saya",
                onNavigateBack = { navController.popBackStack() },
                onContactSaved = { chatId, peerName, peerId ->
                    navController.popBackStack()
                    navController.navigate(Screen.ChatRoom.createRoute(chatId, peerName, peerId))
                }
            )
        }

        // 9. Contact Detail Screen
        composable(
            route = Screen.ContactDetail.route,
            arguments = listOf(navArgument("contactId") { type = NavType.StringType })
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
            ContactDetailScreen(
                contactId = contactId,
                mainViewModel = mainViewModel,
                currentUserId = currentUser?.id ?: "current_user",
                currentUserName = currentUser?.name ?: "Saya",
                onNavigateBack = { navController.popBackStack() },
                onStartChat = { chatId, peerName, peerId ->
                    navController.navigate(Screen.ChatRoom.createRoute(chatId, peerName, peerId))
                },
                onVoiceCall = { pName -> navController.navigate(Screen.VoiceCall.createRoute(pName)) },
                onVideoCall = { pName -> navController.navigate(Screen.VideoCall.createRoute(pName)) }
            )
        }

        // 11. Voice Call Screen
        composable(
            route = Screen.VoiceCall.route,
            arguments = listOf(navArgument("peerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val peerName = backStackEntry.arguments?.getString("peerName") ?: "Kontak"
            VoiceCallScreen(
                peerName = peerName,
                mainViewModel = mainViewModel,
                onEndCall = { navController.popBackStack() }
            )
        }

        // 12. Video Call Screen
        composable(
            route = Screen.VideoCall.route,
            arguments = listOf(navArgument("peerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val peerName = backStackEntry.arguments?.getString("peerName") ?: "Kontak"
            VideoCallScreen(
                peerName = peerName,
                mainViewModel = mainViewModel,
                onEndCall = { navController.popBackStack() }
            )
        }

        // 14. App Settings Screen
        composable(Screen.AppSettings.route) {
            AppSettingsScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToThemeSettings = { navController.navigate(Screen.ThemeSettings.route) },
                onNavigateToPrivacySettings = { navController.navigate(Screen.PrivacySettings.route) }
            )
        }

        // 15. Theme Settings Screen
        composable(Screen.ThemeSettings.route) {
            ThemeSettingsScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 16. Privacy Settings Screen
        composable(Screen.PrivacySettings.route) {
            PrivacySecurityScreen(
                mainViewModel = mainViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

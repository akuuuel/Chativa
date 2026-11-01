package com.chativa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.chativa.data.model.ThemeSettings
import com.chativa.ui.navigation.ChativaNavHost
import com.chativa.ui.theme.ChativaTheme
import com.chativa.ui.viewmodel.AuthViewModel
import com.chativa.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeSettings by mainViewModel.themeSettings.collectAsState(initial = ThemeSettings())
            val navController = rememberNavController()

            ChativaTheme(themeSettings = themeSettings) {
                ChativaNavHost(
                    navController = navController,
                    authViewModel = authViewModel,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

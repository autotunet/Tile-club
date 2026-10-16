package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.admob.AdMobManager
import com.example.data.localization.LocalAppStrings
import com.example.data.localization.getStrings
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.components.RotatingBottomAdBanner
import com.example.ui.screens.GamePlayScreen
import com.example.ui.screens.LevelMapScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Google Mobile Ads (AdMob) SDK with preloaded ads
        AdMobManager.initialize(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: GameViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()
                val currentLanguage by viewModel.currentLanguage.collectAsState()
                val appStrings = remember(currentLanguage) { getStrings(currentLanguage) }

                CompositionLocalProvider(LocalAppStrings provides appStrings) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                            ) {
                                Crossfade(
                                    targetState = currentScreen,
                                    label = "screen_transition"
                                ) { screen ->
                                    when (screen) {
                                        AppScreen.MAIN_MENU -> MainMenuScreen(viewModel = viewModel)
                                        AppScreen.LEVEL_MAP -> LevelMapScreen(viewModel = viewModel)
                                        AppScreen.GAME_PLAY -> GamePlayScreen(viewModel = viewModel)
                                    }
                                }
                            }

                            // Bottom rotating banner that automatically cycles advertisements across the whole app
                            RotatingBottomAdBanner()
                        }
                    }
                }
            }
        }
    }
}

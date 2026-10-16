package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import com.example.data.model.BoosterType
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserProfileEntity
import com.example.domain.GameState
import com.example.domain.GameStatus
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.components.BoosterBar
import com.example.ui.components.CollectorTray
import com.example.ui.components.GameOverDialog
import com.example.ui.components.LandscapeBackground
import com.example.ui.components.ShopDialog
import com.example.ui.components.Tile3DGridView
import com.example.ui.components.TimeOutDialog
import com.example.ui.components.VictoryCelebrationOverlay
import com.example.ui.components.WinDialog
import com.example.ui.theme.CoinGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent

@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.gameState.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val theme by viewModel.selectedTheme.collectAsState()
    val showShop by viewModel.showShopDialog.collectAsState()
    val rewardCoins by viewModel.levelCompletionReward.collectAsState()
    val levelStars by viewModel.levelStarsEarned.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    // Automatic popup interstitial ad when the player loses
    LaunchedEffect(state.status) {
        if (state.status == GameStatus.LOST || state.status == GameStatus.TIME_OUT) {
            viewModel.triggerLossInterstitial(activity)
        }
    }

    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // HD Scenic Wallpaper of Country & World Monument
        LandscapeBackground(destination = state.destination)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar con Temporizador de Nivel y Contador de Fichas
            GameHeader(
                state = state,
                profile = profile,
                onBack = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                onRestart = { viewModel.restartCurrentLevel() }
            )

            // Alert banner when no possible moves remain: prompts player to Shuffle
            AnimatedVisibility(
                visible = state.hasNoPossibleMoves && state.status == GameStatus.PLAYING,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D).copy(alpha = 0.95f)),
                    border = BorderStroke(1.5.dp, GoldAccent),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⚠️", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "¡Sin movimientos posibles!",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Mezcla las fichas para resolver el tablero",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFDE68A)
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.useBooster(BoosterType.SHUFFLE) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("🔀 Mezclar", fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Dynamic 3D Grid Board with Cell Stacks
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Tile3DGridView(
                    grid3D = state.grid3D,
                    theme = theme,
                    highlightedTileIds = state.highlightedTileIds,
                    onTileClick = { tileId -> viewModel.onTileClicked(tileId) }
                )

                // Match celebration / Combo badge floater & Shuffle banner
                Column(
                    modifier = Modifier.align(Alignment.TopCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val event = state.lastMatchEvent
                    AnimatedVisibility(
                        visible = event != null && (System.currentTimeMillis() - event.timestamp < 2200),
                        enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        if (event != null) {
                            val isCombo = event.combo > 1
                            Box(
                                modifier = Modifier
                                    .offset(y = 8.dp)
                                    .shadow(10.dp, RoundedCornerShape(20.dp))
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = if (isCombo) {
                                                listOf(Color(0xFFDC2626), Color(0xFFF97316), GoldAccent)
                                            } else {
                                                listOf(Color(0xFF047857), EmeraldPrimary, GoldAccent)
                                            }
                                        )
                                    )
                                    .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isCombo) "🔥 COMBO x${event.combo}! +${event.points} pts 🔥" else "✨ ¡TRÍO ELIMINADO! +${event.points} pts ✨",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Banner when board has been shuffled with guaranteed solvability
                    AnimatedVisibility(
                        visible = state.shuffleBannerText != null,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .offset(y = 12.dp)
                                .shadow(10.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF1D4ED8), Color(0xFF7C3AED), GoldAccent)
                                    )
                                )
                                .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "🔀 ${state.shuffleBannerText}",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Section: Collector Tray & Boosters
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                CollectorTray(
                    trayTiles = state.trayTiles,
                    capacity = state.trayCapacity,
                    theme = theme,
                    matchingTileIds = state.matchingTileIds
                )

                Spacer(modifier = Modifier.height(4.dp))

                BoosterBar(
                    profile = profile,
                    hintCooldownSec = state.hintCooldownSec,
                    highlightShuffle = state.hasNoPossibleMoves,
                    onBoosterClick = { booster -> viewModel.useBooster(booster) }
                )
            }
        }

        // Satisfying Victory Celebration Sequence with Particle Physics & Sunburst
        if (state.status == GameStatus.WON) {
            VictoryCelebrationOverlay(
                state = state,
                starsEarned = levelStars,
                rewardCoins = rewardCoins,
                onNextLevel = { viewModel.nextLevel() },
                onReplay = { viewModel.restartCurrentLevel() },
                onHome = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                onPlayStarSound = { viewModel.playStarSound(it) }
            )
        }

        // Game Over Dialog (Bandeja Llena)
        if (state.status == GameStatus.LOST) {
            GameOverDialog(
                coins = profile.coins,
                canUndo = profile.boosterUndo > 0,
                onWatchAdForExtraLife = { viewModel.watchAdForExtraLife(activity) },
                onUndo = { viewModel.useBooster(com.example.data.model.BoosterType.UNDO) },
                onRevive = { viewModel.reviveWithCoins() },
                onRestart = { viewModel.restartCurrentLevel() },
                onHome = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
            )
        }

        // Time Out Dialog (Tiempo Agotado)
        if (state.status == GameStatus.TIME_OUT) {
            TimeOutDialog(
                coins = profile.coins,
                onWatchAdForExtraTime = { viewModel.watchAdForExtraLife(activity) },
                onAddExtraTime = { viewModel.addExtraTimeWithCoins(60) },
                onRestart = { viewModel.restartCurrentLevel() },
                onHome = { viewModel.navigateTo(AppScreen.MAIN_MENU) }
            )
        }

        // Shop Dialog
        if (showShop) {
            ShopDialog(
                profile = profile,
                onBuyBooster = { booster -> viewModel.buyBooster(booster) },
                onWatchAdForCoins = { viewModel.watchAdForFreeCoins(activity) },
                onDismiss = { viewModel.setShowShopDialog(false) }
            )
        }
    }
}

@Composable
private fun GameHeader(
    state: GameState,
    profile: UserProfileEntity,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    val isDanger = state.timeRemainingSec <= 10
    val isWarning = state.timeRemainingSec <= 30
    val min = state.timeRemainingSec / 60
    val sec = state.timeRemainingSec % 60
    val timeFormatted = String.format(java.util.Locale.getDefault(), "%02d:%02d", min, sec)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF334155), CircleShape)
                    .testTag("btn_back")
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${state.destination.flag} ${state.destination.country}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Nivel ${state.levelNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
                Text(
                    text = state.destination.monument,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Coins chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${profile.coins} 🪙",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoinGold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF334155), CircleShape)
                        .testTag("btn_restart")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reiniciar",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Timer Pill & Tiles Remaining Progress Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Animated Countdown Timer Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        when {
                            isDanger -> DangerRed.copy(alpha = 0.30f)
                            isWarning -> GoldAccent.copy(alpha = 0.20f)
                            else -> Color(0xFF0F172A).copy(alpha = 0.85f)
                        }
                    )
                    .border(
                        width = if (isDanger) 2.dp else 1.2.dp,
                        color = when {
                            isDanger -> DangerRed
                            isWarning -> GoldAccent
                            else -> Color(0xFF38BDF8)
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 9.dp, vertical = 3.dp)
                    .testTag("timer_badge"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isDanger) "🔥" else "⏱️",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            isDanger -> DangerRed
                            isWarning -> GoldAccent
                            else -> Color.White
                        }
                    )
                }
            }

            // Dual Progress: Board Clearing & Timer Gauge
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Tiles Cleared Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { state.progressPercent },
                        modifier = Modifier
                            .weight(1f)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldPrimary,
                        trackColor = Color.Black.copy(alpha = 0.45f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${state.remainingBoardTilesCount} restantes",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Time remaining micro-bar
                LinearProgressIndicator(
                    progress = { state.timePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = when {
                        isDanger -> DangerRed
                        isWarning -> GoldAccent
                        else -> Color(0xFF38BDF8)
                    },
                    trackColor = Color.Black.copy(alpha = 0.35f)
                )
            }
        }
    }
}

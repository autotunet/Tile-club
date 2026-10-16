package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import android.app.Activity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.components.DailyRewardDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.ShopDialog
import com.example.ui.theme.BoardBackgroundEnd
import com.example.ui.theme.BoardBackgroundStart
import com.example.ui.theme.CoinGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SurfaceDark

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val levels by viewModel.levels.collectAsState()
    val showShop by viewModel.showShopDialog.collectAsState()
    val showDaily by viewModel.showDailyRewardDialog.collectAsState()
    val showSettings by viewModel.showSettingsDialog.collectAsState()
    val showRules by viewModel.showRulesDialog.collectAsState()
    val dailyClaimResult by viewModel.dailyClaimResult.collectAsState()

    val totalStars = levels.sumOf { it.stars }
    val highestUnlocked = levels.filter { it.isUnlocked }.maxOfOrNull { it.levelNumber } ?: 1

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        BoardBackgroundStart,
                        BoardBackgroundEnd
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars & Coins
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⭐ $totalStars",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${profile.coins} 🪙",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoinGold
                        )
                    }
                }

                // Action icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.setShowRulesDialog(true) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("btn_info")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Reglas", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.setShowSettingsDialog(true) },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("btn_settings")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Game Hero Logo & Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tile_club_icon_1792032064175),
                    contentDescription = "Tile Club Icon",
                    modifier = Modifier
                        .size(160.dp)
                        .shadow(16.dp, RoundedCornerShape(32.dp))
                        .clip(RoundedCornerShape(32.dp))
                        .border(3.dp, GoldAccent, RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "TILE CLUB",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldAccent,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Puzzles de Fichas en Capas 3D",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.LightGray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Play Button
            Button(
                onClick = { viewModel.startLevel(highestUnlocked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .testTag("btn_main_play"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("▶️", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "JUGAR NIVEL $highestUnlocked",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Toca para continuar el desafío",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val currentDest = com.example.data.model.WorldDestinations.getDestinationForLevel(highestUnlocked)
                MenuFeatureCard(
                    title = "${currentDest.flag} Niveles",
                    subtitle = "$highestUnlocked / 1000",
                    icon = "🗺️",
                    modifier = Modifier.weight(1f),
                    testTag = "btn_map",
                    onClick = { viewModel.navigateTo(AppScreen.LEVEL_MAP) }
                )

                MenuFeatureCard(
                    title = "Diario",
                    subtitle = "Día ${(profile.dailyStreak % 7) + 1}",
                    icon = "🎁",
                    modifier = Modifier.weight(1f),
                    testTag = "btn_daily",
                    onClick = { viewModel.setShowDailyRewardDialog(true) }
                )

                MenuFeatureCard(
                    title = "Tienda",
                    subtitle = "Potenciadores",
                    icon = "🛒",
                    modifier = Modifier.weight(1f),
                    testTag = "btn_shop",
                    onClick = { viewModel.setShowShopDialog(true) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Boosters Quick Inventory Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BoosterPill("💡", "Pista", profile.boosterHint)
                    BoosterPill("↩️", "Deshacer", profile.boosterUndo)
                    BoosterPill("🔀", "Mezclar", profile.boosterShuffle)
                    BoosterPill("🪄", "Varita", profile.boosterWand)
                    BoosterPill("➕", "+1 Ranura", profile.boosterSlot)
                }
            }
        }

        val context = LocalContext.current
        val activity = context as? Activity

        // Dialogs
        if (showShop) {
            ShopDialog(
                profile = profile,
                onBuyBooster = { viewModel.buyBooster(it) },
                onWatchAdForCoins = { viewModel.watchAdForFreeCoins(activity) },
                onDismiss = { viewModel.setShowShopDialog(false) }
            )
        }

        if (showDaily) {
            DailyRewardDialog(
                profile = profile,
                claimedReward = dailyClaimResult,
                onClaim = { viewModel.claimDailyReward() },
                onDismiss = { viewModel.setShowDailyRewardDialog(false) }
            )
        }

        if (showSettings) {
            SettingsDialog(
                profile = profile,
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onSelectTheme = { viewModel.switchTheme(it) },
                onDismiss = { viewModel.setShowSettingsDialog(false) }
            )
        }

        if (showRules) {
            RulesDialog(onDismiss = { viewModel.setShowRulesDialog(false) })
        }
    }
}

@Composable
private fun MenuFeatureCard(
    title: String,
    subtitle: String,
    icon: String,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color.LightGray)
        }
    }
}

@Composable
private fun BoosterPill(icon: String, name: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Text(text = "$count", fontWeight = FontWeight.Black, fontSize = 13.sp, color = GoldAccent)
        Text(text = name, fontSize = 10.sp, color = Color.LightGray)
    }
}

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "📖 Cómo Jugar a Tile Club",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(12.dp))

                val rules = listOf(
                    "1. Toca fichas desbloqueadas en el tablero para moverlas a la bandeja inferior.",
                    "2. Las fichas cubiertas por otras en capas superiores están bloqueadas hasta que retires las de arriba.",
                    "3. Junta 3 fichas idénticas en la bandeja para hacer 'Match' y eliminarlas.",
                    "4. Si la bandeja de 7 casillas se llena sin poder hacer tríos, ¡perderás el nivel!",
                    "5. Usa los potenciadores: 💡 Pista para resaltar pares válidos cuando estés atascado, ↩️ Deshacer, 🔀 Mezclar fichas, 🪄 Varita mágica y ➕ +1 Ranura extra para superar tableros difíciles."
                )

                rules.forEach { rule ->
                    Text(
                        text = rule,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("¡Entendido!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

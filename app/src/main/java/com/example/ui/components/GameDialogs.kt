package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.UserProfileEntity
import com.example.data.localization.AppLanguage
import com.example.data.localization.LocalAppStrings
import com.example.data.model.BoosterType
import com.example.data.model.TileThemes
import com.example.domain.GameState
import com.example.ui.theme.CoinGold
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StarGold
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WoodDark

@Composable
fun WinDialog(
    state: GameState,
    rewardCoins: Int,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    val strings = LocalAppStrings.current
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("win_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.victoryTitle,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldAccent
                )

                Text(
                    text = "${strings.levelNumberLabel(state.levelNumber, state.levelName)}",
                    fontSize = 15.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stars display (3 stars)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Text(
                            text = "⭐",
                            fontSize = if (index == 1) 48.sp else 38.sp,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(strings.score, fontSize = 12.sp, color = Color.Gray)
                            Text("${state.score}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(strings.movesCount, fontSize = 12.sp, color = Color.Gray)
                            Text("${state.movesCount}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(strings.reward, fontSize = 12.sp, color = Color.Gray)
                            Text("+$rewardCoins 🪙", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CoinGold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_next_level"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(strings.nextLevel, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.replay, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.home, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun GameOverDialog(
    coins: Int,
    canUndo: Boolean,
    onWatchAdForExtraLife: () -> Unit,
    onUndo: () -> Unit,
    onRevive: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val strings = LocalAppStrings.current
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, DangerRed, RoundedCornerShape(24.dp))
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("game_over_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.trayFullTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = DangerRed
                )

                Text(
                    text = strings.trayFullDesc,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Reward Option: Watch Ad for Free Extra Life
                Button(
                    onClick = onWatchAdForExtraLife,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_extra_life_ad"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(strings.freeExtraLifeAd, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text(strings.freeExtraLifeAdDesc, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (canUndo) {
                    Button(
                        onClick = onUndo,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_undo_rescue"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.undoLastTile, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = onRevive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_revive"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.reviveWithCoins(60), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.replay, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.home, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun TimeOutDialog(
    coins: Int,
    onWatchAdForExtraTime: () -> Unit,
    onAddExtraTime: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    val strings = LocalAppStrings.current
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, DangerRed, RoundedCornerShape(24.dp))
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("timeout_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = strings.timeOutTitle,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = DangerRed
                )

                Text(
                    text = strings.timeOutDesc,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Primary Option: Free Extra Time with Ad
                Button(
                    onClick = onWatchAdForExtraTime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_extra_time_ad"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎬", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(strings.freeExtraTimeAd, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text(strings.freeExtraTimeAdDesc, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rescue option: Add 60 seconds with coins
                Button(
                    onClick = onAddExtraTime,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("btn_add_extra_time"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.extraTimeCoins(50), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.replay, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(strings.home, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ShopDialog(
    profile: UserProfileEntity,
    onBuyBooster: (BoosterType) -> Unit,
    onWatchAdForCoins: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldAccent, RoundedCornerShape(24.dp))
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("shop_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛒 Tienda de Potenciadores",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Coins Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tus Monedas: ${profile.coins} 🪙",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CoinGold
                    )
                }

                // Rewarded Ad Offer for free coins
                if (onWatchAdForCoins != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🎬", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                Column {
                                    Text("+100 Monedas Gratis", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("Ver anuncio recompensado", fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                            Button(
                                onClick = onWatchAdForCoins,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_ad_free_coins")
                            ) {
                                Text("VER", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val boosters = listOf(
                    Triple(BoosterType.HINT, "Pista", "Resalta un par de fichas válidas para emparejar"),
                    Triple(BoosterType.UNDO, "Deshacer", "Devuelve la última ficha al tablero"),
                    Triple(BoosterType.SHUFFLE, "Mezclar", "Reorganiza las fichas restantes del tablero"),
                    Triple(BoosterType.MAGIC_WAND, "Varita Mágica", "Elimina automáticamente 3 fichas iguales"),
                    Triple(BoosterType.EXTRA_SLOT, "+1 Hueco", "Añade una ranura extra a la bandeja")
                )

                boosters.forEach { (type, name, desc) ->
                    val canAfford = profile.coins >= type.costCoins
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(type.icon, fontSize = 28.sp, modifier = Modifier.padding(end = 10.dp))
                                Column {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text(desc, fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                            Button(
                                onClick = { onBuyBooster(type) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) GoldAccent else Color.Gray,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("buy_${type.name.lowercase()}")
                            ) {
                                Text("${type.costCoins} 🪙", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyRewardDialog(
    profile: UserProfileEntity,
    claimedReward: Int?,
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, EmeraldPrimary, RoundedCornerShape(24.dp))
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("daily_reward_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎁 Recompensa Diaria",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "¡Inicia sesión cada día para desbloquear mejores premios!",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7 Days Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in 1..4) {
                        val isToday = (profile.dailyStreak % 7) + 1 == day
                        DayRewardPill(day = day, coins = 100 + day * 30, isToday = isToday)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (day in 5..7) {
                        val isToday = (profile.dailyStreak % 7) + 1 == day
                        DayRewardPill(day = day, coins = 100 + day * 30, isToday = isToday, hasBooster = day == 7)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                if (claimedReward != null) {
                    Text(
                        text = "🎉 ¡Has reclamado +$claimedReward Monedas! 🎉",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                } else {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_claim_daily")
                    ) {
                        Text("Reclamar Premio Hoy 🎁", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DayRewardPill(day: Int, coins: Int, isToday: Boolean, hasBooster: Boolean = false) {
    Box(
        modifier = Modifier
            .size(68.dp, 76.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isToday) EmeraldDark else Color(0xFF334155))
            .border(
                1.5.dp,
                if (isToday) EmeraldPrimary else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Día $day", fontSize = 11.sp, color = if (isToday) EmeraldPrimary else Color.Gray, fontWeight = FontWeight.Bold)
            Text(if (hasBooster) "🪄" else "🪙", fontSize = 20.sp)
            Text("$coins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CoinGold)
        }
    }
}

@Composable
fun SettingsDialog(
    profile: UserProfileEntity,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onSelectTheme: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF64748B), RoundedCornerShape(24.dp))
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ Ajustes & Temas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sound Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Efectos de Sonido 🔊", fontSize = 15.sp, color = Color.White)
                    Switch(
                        checked = profile.soundEnabled,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }

                // Haptics Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibración Háptica 📳", fontSize = 15.sp, color = Color.White)
                    Switch(
                        checked = profile.hapticEnabled,
                        onCheckedChange = { onToggleHaptics() },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Tema de Fichas", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                Spacer(modifier = Modifier.height(8.dp))

                TileThemes.ALL.forEachIndexed { index, theme ->
                    val isSelected = profile.selectedThemeIndex == index
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) EmeraldDark.copy(alpha = 0.5f) else Color(0xFF334155))
                            .border(1.5.dp, if (isSelected) EmeraldPrimary else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { onSelectTheme(index) }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(theme.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(theme.symbols.take(5).joinToString(" "), fontSize = 16.sp)
                        }
                        if (isSelected) {
                            Text("✓ Activo", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

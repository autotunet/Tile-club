package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.GameTile
import com.example.data.model.TileTheme
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TrayBackground
import com.example.ui.theme.TrayBorder
import com.example.ui.theme.TraySlotBackground
import com.example.ui.theme.WoodDark
import com.example.ui.theme.WoodMedium

@Composable
fun CollectorTray(
    trayTiles: List<GameTile>,
    capacity: Int,
    theme: TileTheme,
    matchingTileIds: Set<Int> = emptySet(),
    modifier: Modifier = Modifier
) {
    val isNearFull = trayTiles.size >= capacity - 1
    val isFull = trayTiles.size >= capacity
    val hasMatches = matchingTileIds.isNotEmpty()

    val borderColor by animateColorAsState(
        targetValue = when {
            hasMatches -> GoldAccent
            isFull -> DangerRed
            isNearFull -> GoldAccent
            else -> TrayBorder
        },
        animationSpec = tween(250),
        label = "trayBorder"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Floating match banner when forming a match
            if (hasMatches) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF047857), EmeraldPrimary, GoldAccent)
                            )
                        )
                        .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "✨ ¡TRÍO FORMADO! ✨",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            } else if (isNearFull && !isFull) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF991B1B), DangerRed, Color(0xFFEA580C))
                            )
                        )
                        .border(1.dp, Color.White, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⚠️ ¡ALERTA! BANDEJA CASI LLENA (${trayTiles.size}/$capacity)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WoodMedium,
                                WoodDark,
                                TrayBackground
                            )
                        )
                    )
                    .border(2.5.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp)
                    .testTag("collector_tray"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until capacity) {
                        val tile = trayTiles.getOrNull(i)
                        Box(
                            modifier = Modifier
                                .size(38.dp, 44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TraySlotBackground)
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (tile != null) {
                                val countSame = trayTiles.count { it.typeId == tile.typeId }
                                val isMatch = tile.id in matchingTileIds || countSame >= 3
                                val badge = when {
                                    isMatch -> "3/3 ✨"
                                    countSame == 2 -> "2/3"
                                    else -> "1/3"
                                }

                                TileView(
                                    tile = tile,
                                    theme = theme,
                                    tileSize = 36.dp,
                                    isMatching = isMatch,
                                    formationBadge = badge,
                                    onClick = null
                                )
                            } else {
                                // Empty slot indicator
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color(0xFF1E293B))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

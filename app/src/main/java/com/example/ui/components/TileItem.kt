package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameTile
import com.example.data.model.TileTheme
import com.example.ui.theme.TileBaseLight
import com.example.ui.theme.TileBaseShade
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TileBlockedOverlay

@Composable
fun TileView(
    tile: GameTile,
    theme: TileTheme,
    modifier: Modifier = Modifier,
    tileSize: Dp = 48.dp,
    isMatching: Boolean = false,
    formationBadge: String? = null,
    onClick: (() -> Unit)? = null
) {
    val symbol = theme.symbols.getOrElse(tile.typeId) { "❓" }
    val tileBgColor = theme.colors.getOrElse(tile.typeId) { 0xFFFFFFFF }
    val shape = RoundedCornerShape(10.dp)

    val elevation = if (isMatching) 10.dp else if (tile.isBlocked) 2.dp else (4 + tile.layer * 2).dp
    val scale by animateFloatAsState(
        targetValue = when {
            isMatching -> 1.14f
            tile.isBlocked -> 0.96f
            else -> 1.0f
        },
        animationSpec = tween(180),
        label = "tileScale"
    )

    Box(
        modifier = modifier
            .size(tileSize, tileSize * 1.15f)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isMatching) GoldAccent else Color.Black.copy(alpha = 0.4f),
                spotColor = if (isMatching) GoldAccent else Color.Black.copy(alpha = 0.5f)
            )
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isMatching) {
                        listOf(
                            Color(0xFFFEF08A),
                            Color(tileBgColor),
                            TileBaseLight
                        )
                    } else {
                        listOf(
                            Color(tileBgColor),
                            TileBaseLight,
                            TileBaseShade
                        )
                    }
                )
            )
            .border(
                width = if (isMatching) 2.5.dp else 1.5.dp,
                color = when {
                    isMatching -> GoldAccent
                    tile.isBlocked -> Color(0xFF94A3B8).copy(alpha = 0.6f)
                    else -> Color(0xFFD97706)
                },
                shape = shape
            )
            .clickable(
                enabled = !tile.isBlocked && !tile.isCollected && onClick != null,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick?.invoke()
            }
            .testTag("tile_${tile.id}"),
        contentAlignment = Alignment.Center
    ) {
        // Tile 3D bevel / top highlight
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isMatching) 0.7f else 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Emoji / Symbol
        Text(
            text = symbol,
            fontSize = (tileSize.value * 0.52f).sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.offset(y = (-1).dp)
        )

        // Badge indicador de formación de set ("1/3", "2/3", "3/3 ✨")
        if (formationBadge != null && !tile.isBlocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isMatching) Color(0xFF047857) else Color(0xFF0F172A).copy(alpha = 0.85f)
                    )
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Text(
                    text = formationBadge,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isMatching) Color.White else GoldAccent
                )
            }
        }

        // Blocked Overlay (darkened layer to visually distinguish covered tiles)
        if (tile.isBlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TileBlockedOverlay)
            )
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.model.BoardGrid3D
import com.example.data.model.GameTile
import com.example.data.model.GridCell3D
import com.example.data.model.TileTheme
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.TileBaseLight
import com.example.ui.theme.TileBaseShade
import com.example.ui.theme.TileBlockedOverlay

/**
 * Componente Compose que renderiza el tablero de fichas en 3D basado en una cuadrícula (grid)
 * donde cada celda contiene una pila (stack) de fichas con capas de profundidad física
 * y soporte para resaltar pares sugeridos por la mecánica de 'Pista' (Hint).
 */
@Composable
fun Tile3DGridView(
    grid3D: BoardGrid3D,
    theme: TileTheme,
    highlightedTileIds: Set<Int> = emptySet(),
    onTileClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth.value
        val availableHeight = maxHeight.value

        val cols = grid3D.columns
        val rows = grid3D.rows

        val cellW = (availableWidth / cols).coerceIn(28f, 52f)
        val cellH = (availableHeight / (rows + 1.2f)).coerceIn(32f, 60f)
        val tileSizeDp = minOf(cellW, cellH).dp

        val boardTotalWidth = (cols * cellW).dp
        val boardTotalHeight = (rows * cellH).dp

        Box(
            modifier = Modifier.size(boardTotalWidth, boardTotalHeight),
            contentAlignment = Alignment.Center
        ) {
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val cell = grid3D.getCell(c, r) ?: continue
                    if (cell.isEmpty) continue

                    val baseOffsetX = ((c - cols / 2f + 0.5f) * cellW).dp
                    val baseOffsetY = ((r - rows / 2f + 0.5f) * cellH).dp

                    Cell3DStackItem(
                        cell = cell,
                        theme = theme,
                        tileSize = tileSizeDp,
                        highlightedTileIds = highlightedTileIds,
                        modifier = Modifier.offset(x = baseOffsetX, y = baseOffsetY),
                        onTileClick = onTileClick
                    )
                }
            }
        }
    }
}

/**
 * Renderizado de una pila 3D dentro de una celda de la cuadrícula.
 * Dibuja los bordes físicos de las capas inferiores, la ficha activa en la cima
 * y el efecto de resalte pulsante si la ficha forma parte de una Pista activa.
 */
@Composable
private fun Cell3DStackItem(
    cell: GridCell3D,
    theme: TileTheme,
    tileSize: Dp,
    highlightedTileIds: Set<Int>,
    modifier: Modifier = Modifier,
    onTileClick: (Int) -> Unit
) {
    val uncollectedTiles = cell.tileStack.filter { !it.isCollected }
    if (uncollectedTiles.isEmpty()) return

    val topTile = uncollectedTiles.last()
    val stackHeight = uncollectedTiles.size
    val layer = topTile.layer
    val isHighlighted = topTile.id in highlightedTileIds

    // Desplazamiento 3D hacia arriba proporcional a la capa física
    val layerElevOffset = (-layer * 3.5f).dp
    val shape = RoundedCornerShape(9.dp)

    // Animación de pulso para la mecánica de 'Pista'
    val infiniteTransition = rememberInfiniteTransition(label = "hintPulse")
    val hintPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintScale"
    )
    val hintGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintGlow"
    )

    Box(
        modifier = modifier
            .size(tileSize, tileSize * 1.15f)
            .offset(y = layerElevOffset),
        contentAlignment = Alignment.Center
    ) {
        // Efecto 3D de capas inferiores visibles en la pila
        if (stackHeight > 1) {
            for (step in 1 until minOf(stackHeight, 4)) {
                val rimOffset = (step * 2.8f).dp
                Box(
                    modifier = Modifier
                        .size(tileSize, tileSize * 1.15f)
                        .offset(y = rimOffset)
                        .shadow(2.dp, shape)
                        .clip(shape)
                        .background(Color(0xFFCBD5E1))
                        .border(1.dp, Color(0xFF64748B), shape)
                )
            }
        }

        // Ficha superior activa (Top tile)
        val symbol = theme.symbols.getOrElse(topTile.typeId) { "❓" }
        val tileBgColor = theme.colors.getOrElse(topTile.typeId) { 0xFFFFFFFF }
        val elevation = if (isHighlighted) 12.dp else if (topTile.isBlocked) 2.dp else (5 + layer * 2).dp

        val baseScale by animateFloatAsState(
            targetValue = if (topTile.isBlocked) 0.96f else 1.0f,
            animationSpec = tween(120),
            label = "tile3DScale"
        )
        val effectiveScale = if (isHighlighted) hintPulseScale else baseScale

        Box(
            modifier = Modifier
                .size(tileSize, tileSize * 1.15f)
                .scale(effectiveScale)
                .shadow(elevation, shape, ambientColor = if (isHighlighted) GoldAccent else Color.Black.copy(alpha = 0.5f))
                .clip(shape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isHighlighted) {
                            listOf(Color(0xFFFEF08A), Color(tileBgColor), TileBaseLight)
                        } else {
                            listOf(Color(tileBgColor), TileBaseLight, TileBaseShade)
                        }
                    )
                )
                .border(
                    width = if (isHighlighted) 2.8.dp else 1.5.dp,
                    color = if (isHighlighted) {
                        GoldAccent.copy(alpha = hintGlowAlpha)
                    } else if (topTile.isBlocked) {
                        Color(0xFF94A3B8).copy(alpha = 0.7f)
                    } else {
                        Color(0xFFD97706)
                    },
                    shape = shape
                )
                .clickable(
                    enabled = !topTile.isBlocked && !topTile.isCollected,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onTileClick(topTile.id)
                }
                .testTag("tile_${topTile.id}"),
            contentAlignment = Alignment.Center
        ) {
            // Relieve biselado superior brillante
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.5.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.55f), Color.Transparent)
                        )
                    )
            )

            // Símbolo / Emoji de la ficha
            Text(
                text = symbol,
                fontSize = (tileSize.value * 0.50f).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (-1).dp)
            )

            // Capa bloqueada
            if (topTile.isBlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TileBlockedOverlay)
                )
            }

            // Indicador de altura de la pila (x2, x3)
            if (stackHeight > 1 && !isHighlighted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "x$stackHeight",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                }
            }

            // Badge luminoso de 'Pista'
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 3.dp, y = (-3).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💡",
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

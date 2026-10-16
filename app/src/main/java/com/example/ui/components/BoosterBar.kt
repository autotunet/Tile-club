package com.example.ui.components

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserProfileEntity
import com.example.data.model.BoosterType
import com.example.ui.theme.BoosterShuffleColor
import com.example.ui.theme.BoosterSlotColor
import com.example.ui.theme.BoosterUndoColor
import com.example.ui.theme.BoosterWandColor
import com.example.ui.theme.GoldAccent

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.scale

val BoosterHintColor = Color(0xFFEAB308)

@Composable
fun BoosterBar(
    profile: UserProfileEntity,
    hintCooldownSec: Int,
    highlightShuffle: Boolean = false,
    onBoosterClick: (BoosterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoosterItem(
            type = BoosterType.HINT,
            count = profile.boosterHint,
            color = BoosterHintColor,
            cooldownSec = hintCooldownSec,
            isHighlighted = false,
            onClick = { onBoosterClick(BoosterType.HINT) }
        )
        BoosterItem(
            type = BoosterType.UNDO,
            count = profile.boosterUndo,
            color = BoosterUndoColor,
            cooldownSec = 0,
            isHighlighted = false,
            onClick = { onBoosterClick(BoosterType.UNDO) }
        )
        BoosterItem(
            type = BoosterType.SHUFFLE,
            count = profile.boosterShuffle,
            color = BoosterShuffleColor,
            cooldownSec = 0,
            isHighlighted = highlightShuffle,
            onClick = { onBoosterClick(BoosterType.SHUFFLE) }
        )
        BoosterItem(
            type = BoosterType.MAGIC_WAND,
            count = profile.boosterWand,
            color = BoosterWandColor,
            cooldownSec = 0,
            isHighlighted = false,
            onClick = { onBoosterClick(BoosterType.MAGIC_WAND) }
        )
        BoosterItem(
            type = BoosterType.EXTRA_SLOT,
            count = profile.boosterSlot,
            color = BoosterSlotColor,
            cooldownSec = 0,
            isHighlighted = false,
            onClick = { onBoosterClick(BoosterType.EXTRA_SLOT) }
        )
    }
}

@Composable
private fun BoosterItem(
    type: BoosterType,
    count: Int,
    color: Color,
    cooldownSec: Int,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val isOnCooldown = cooldownSec > 0

    val infiniteTransition = rememberInfiniteTransition(label = "boosterPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHighlighted) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = !isOnCooldown) { onClick() }
            .padding(2.dp)
            .scale(if (isHighlighted) pulseScale else 1f)
            .testTag("booster_${type.name.lowercase()}")
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            // Main button disc
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(if (isHighlighted) 12.dp else 6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (isOnCooldown) {
                                listOf(Color(0xFF475569), Color(0xFF334155))
                            } else if (isHighlighted) {
                                listOf(Color(0xFFEF4444), Color(0xFFF59E0B), GoldAccent)
                            } else {
                                listOf(
                                    color.copy(alpha = 0.95f),
                                    color,
                                    color.copy(alpha = 0.75f)
                                )
                            }
                        )
                    )
                    .border(
                        width = if (isHighlighted) 3.dp else 2.dp,
                        color = if (isOnCooldown) Color(0xFF64748B) else if (isHighlighted) GoldAccent else Color.White.copy(alpha = 0.85f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isOnCooldown) {
                    Text(
                        text = "${cooldownSec}s",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                } else {
                    Text(
                        text = type.icon,
                        fontSize = 22.sp
                    )
                }
            }

            // Quantity or plus badge
            if (!isOnCooldown) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = (-2).dp)
                        .size(if (isHighlighted) 22.dp else 20.dp)
                        .clip(CircleShape)
                        .background(if (isHighlighted) Color(0xFFEF4444) else if (count > 0) GoldAccent else Color(0xFFEF4444))
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isHighlighted) "!" else if (count > 0) "$count" else "+",
                        fontSize = if (isHighlighted) 11.sp else 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isHighlighted) Color.White else Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = if (isHighlighted) "¡USAR!" else type.displayName,
            fontSize = 10.sp,
            fontWeight = if (isHighlighted) FontWeight.Black else FontWeight.SemiBold,
            color = if (isHighlighted) GoldAccent else if (isOnCooldown) Color(0xFF94A3B8) else Color.White
        )
    }
}

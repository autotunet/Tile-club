package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.data.database.LevelEntity
import com.example.data.model.WorldDestination
import com.example.data.model.WorldDestinations
import com.example.ui.AppScreen
import com.example.ui.GameViewModel
import com.example.ui.theme.BoardBackgroundEnd
import com.example.ui.theme.BoardBackgroundStart
import com.example.ui.theme.CoinGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.StarGold

@Composable
fun LevelMapScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val levels by viewModel.levels.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val totalStars = levels.sumOf { it.stars }
    val currentMaxUnlocked = maxOf(profile.currentLevel, levels.filter { it.isUnlocked }.maxOfOrNull { it.levelNumber } ?: 1)

    // Find which chapter corresponds to player's current level
    val defaultChapter = WorldDestinations.getDestinationForLevel(currentMaxUnlocked)
    var selectedDestination by remember { mutableStateOf(defaultChapter) }

    val levelsMap = remember(levels) { levels.associateBy { it.levelNumber } }

    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN_MENU)
    }

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
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.MAIN_MENU) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("btn_back_menu")
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "1000 Niveles del Mundo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldAccent
                    )
                    Text(
                        text = "Capítulo: ${selectedDestination.country}",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                }

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
                        color = StarGold
                    )
                }
            }

            // Country / Chapter Selector Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WorldDestinations.DESTINATIONS) { dest ->
                    val isSelected = dest.id == selectedDestination.id
                    val isUnlocked = currentMaxUnlocked >= dest.startLevel
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) EmeraldDark else Color(0xFF1E293B)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) EmeraldPrimary else Color(0xFF334155),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { selectedDestination = dest }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(dest.flag, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${dest.country} (${dest.startLevel}-${dest.endLevel})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            // Featured Landmark Banner Card for selected chapter
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, Color(selectedDestination.ambientColor).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedDestination.flag, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedDestination.country} • ${selectedDestination.monument}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = selectedDestination.quote,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }

            // Grid of 50 Levels for this Chapter
            val chapterLevels = (selectedDestination.startLevel..selectedDestination.endLevel).toList()

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(chapterLevels) { levelNum ->
                    val entity = levelsMap[levelNum]
                    val isUnlocked = levelNum <= currentMaxUnlocked || (entity?.isUnlocked == true)
                    val stars = entity?.stars ?: 0
                    val isCurrent = levelNum == currentMaxUnlocked

                    LevelGridNode(
                        levelNumber = levelNum,
                        isUnlocked = isUnlocked,
                        isCurrent = isCurrent,
                        stars = stars,
                        onClick = {
                            if (isUnlocked) {
                                viewModel.startLevel(levelNum)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelGridNode(
    levelNumber: Int,
    isUnlocked: Boolean,
    isCurrent: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = isUnlocked) { onClick() }
            .testTag("level_node_$levelNumber")
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(if (isCurrent) 8.dp else 2.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = when {
                        isCurrent -> Brush.verticalGradient(listOf(GoldAccent, Color(0xFFD97706)))
                        stars > 0 -> Brush.verticalGradient(listOf(EmeraldPrimary, EmeraldDark))
                        isUnlocked -> Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8)))
                        else -> Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    }
                )
                .border(
                    width = if (isCurrent) 2.dp else 1.dp,
                    color = if (isCurrent) Color.White else if (isUnlocked) GoldAccent else Color(0xFF475569),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = "$levelNumber",
                    fontSize = if (levelNumber >= 100) 14.sp else 17.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isCurrent) Color.Black else Color.White
                )
                if (levelNumber % 5 == 0) {
                    val icon = if (levelNumber % 10 == 0) "👑" else "🔥"
                    Text(
                        text = icon,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Bloqueado",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Stars row
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(3) { starIndex ->
                val filled = starIndex < stars
                Text(
                    text = if (filled) "⭐" else "▪️",
                    fontSize = if (filled) 9.sp else 7.sp
                )
            }
        }
    }
}

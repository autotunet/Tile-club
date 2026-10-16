package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 0,
    val highScore: Int = 0,
    val isUnlocked: Boolean = false,
    val bestMoves: Int = 0
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 200,
    val boosterHint: Int = 3,
    val boosterUndo: Int = 3,
    val boosterShuffle: Int = 3,
    val boosterWand: Int = 2,
    val boosterSlot: Int = 2,
    val currentLevel: Int = 1,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val selectedThemeIndex: Int = 0,
    val lastDailyRewardEpochDay: Long = 0,
    val dailyStreak: Int = 0,
    val totalMatchesMade: Int = 0,
    val languageCode: String = "es"
)

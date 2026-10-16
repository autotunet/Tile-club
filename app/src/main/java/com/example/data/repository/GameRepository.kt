package com.example.data.repository

import com.example.data.database.LevelEntity
import com.example.data.database.TileClubDao
import com.example.data.database.UserProfileEntity
import com.example.data.model.BoosterType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull

class GameRepository(private val dao: TileClubDao) {

    val allLevels: Flow<List<LevelEntity>> = dao.getAllLevels()
    val userProfile: Flow<UserProfileEntity> = dao.getUserProfile().filterNotNull()

    suspend fun initializeIfEmpty() {
        val existingProfile = dao.getUserProfileOnce()
        if (existingProfile == null) {
            dao.insertOrUpdateProfile(UserProfileEntity())
        }

        val level1 = dao.getLevel(1)
        if (level1 == null) {
            // Unlock level 1
            dao.insertLevels(listOf(LevelEntity(levelNumber = 1, isUnlocked = true)))
        }
    }

    suspend fun completeLevel(levelNumber: Int, score: Int, moves: Int, starsEarned: Int): Int {
        val currentLevel = dao.getLevel(levelNumber)
        val oldStars = currentLevel?.stars ?: 0
        val newStars = maxOf(oldStars, starsEarned)
        val newScore = maxOf(currentLevel?.highScore ?: 0, score)

        val updatedLevel = LevelEntity(
            levelNumber = levelNumber,
            stars = newStars,
            highScore = newScore,
            isUnlocked = true,
            bestMoves = if (currentLevel == null || currentLevel.bestMoves == 0) moves else minOf(currentLevel.bestMoves, moves)
        )
        dao.insertLevels(listOf(updatedLevel))

        // Unlock next level up to 1000
        val nextLevelNumber = (levelNumber + 1).coerceAtMost(1000)
        val nextLevel = dao.getLevel(nextLevelNumber)
        if (nextLevel != null && !nextLevel.isUnlocked) {
            dao.updateLevel(nextLevel.copy(isUnlocked = true))
        } else if (nextLevel == null) {
            dao.insertLevels(listOf(LevelEntity(levelNumber = nextLevelNumber, isUnlocked = true)))
        }

        // Reward coins
        val coinsReward = 60 + (starsEarned * 20)
        dao.addCoins(coinsReward)

        val profile = dao.getUserProfileOnce() ?: UserProfileEntity()
        dao.insertOrUpdateProfile(
            profile.copy(
                currentLevel = maxOf(profile.currentLevel, nextLevelNumber),
                coins = profile.coins + coinsReward,
                totalMatchesMade = profile.totalMatchesMade + 1
            )
        )
        return coinsReward
    }

    suspend fun useBooster(type: BoosterType): Boolean {
        val profile = dao.getUserProfileOnce() ?: return false
        val (hasCount, updated) = when (type) {
            BoosterType.HINT -> (profile.boosterHint > 0) to profile.copy(boosterHint = profile.boosterHint - 1)
            BoosterType.UNDO -> (profile.boosterUndo > 0) to profile.copy(boosterUndo = profile.boosterUndo - 1)
            BoosterType.SHUFFLE -> (profile.boosterShuffle > 0) to profile.copy(boosterShuffle = profile.boosterShuffle - 1)
            BoosterType.MAGIC_WAND -> (profile.boosterWand > 0) to profile.copy(boosterWand = profile.boosterWand - 1)
            BoosterType.EXTRA_SLOT -> (profile.boosterSlot > 0) to profile.copy(boosterSlot = profile.boosterSlot - 1)
        }
        if (hasCount) {
            dao.insertOrUpdateProfile(updated)
            return true
        }
        return false
    }

    suspend fun buyBooster(type: BoosterType): Boolean {
        val profile = dao.getUserProfileOnce() ?: return false
        if (profile.coins < type.costCoins) return false

        val updated = when (type) {
            BoosterType.HINT -> profile.copy(coins = profile.coins - type.costCoins, boosterHint = profile.boosterHint + 1)
            BoosterType.UNDO -> profile.copy(coins = profile.coins - type.costCoins, boosterUndo = profile.boosterUndo + 1)
            BoosterType.SHUFFLE -> profile.copy(coins = profile.coins - type.costCoins, boosterShuffle = profile.boosterShuffle + 1)
            BoosterType.MAGIC_WAND -> profile.copy(coins = profile.coins - type.costCoins, boosterWand = profile.boosterWand + 1)
            BoosterType.EXTRA_SLOT -> profile.copy(coins = profile.coins - type.costCoins, boosterSlot = profile.boosterSlot + 1)
        }
        dao.insertOrUpdateProfile(updated)
        return true
    }

    suspend fun claimDailyReward(): Int? {
        val profile = dao.getUserProfileOnce() ?: return null
        val todayEpochDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

        if (profile.lastDailyRewardEpochDay == todayEpochDay) {
            return null
        }

        val streak = if (profile.lastDailyRewardEpochDay == todayEpochDay - 1) {
            (profile.dailyStreak % 7) + 1
        } else {
            1
        }

        val rewardCoins = 100 + (streak * 30)
        val updated = profile.copy(
            coins = profile.coins + rewardCoins,
            lastDailyRewardEpochDay = todayEpochDay,
            dailyStreak = streak,
            boosterUndo = profile.boosterUndo + (if (streak >= 3) 1 else 0),
            boosterWand = profile.boosterWand + (if (streak >= 7) 1 else 0)
        )
        dao.insertOrUpdateProfile(updated)
        return rewardCoins
    }

    suspend fun toggleSound() {
        val profile = dao.getUserProfileOnce() ?: return
        dao.insertOrUpdateProfile(profile.copy(soundEnabled = !profile.soundEnabled))
    }

    suspend fun toggleHaptics() {
        val profile = dao.getUserProfileOnce() ?: return
        dao.insertOrUpdateProfile(profile.copy(hapticEnabled = !profile.hapticEnabled))
    }

    suspend fun setSelectedTheme(index: Int) {
        val profile = dao.getUserProfileOnce() ?: return
        dao.insertOrUpdateProfile(profile.copy(selectedThemeIndex = index))
    }

    suspend fun setLanguage(code: String) {
        val profile = dao.getUserProfileOnce() ?: return
        dao.insertOrUpdateProfile(profile.copy(languageCode = code))
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val profile = dao.getUserProfileOnce() ?: return false
        if (profile.coins >= amount) {
            dao.insertOrUpdateProfile(profile.copy(coins = profile.coins - amount))
            return true
        }
        return false
    }

    suspend fun addRewardCoins(amount: Int) {
        val profile = dao.getUserProfileOnce() ?: return
        dao.insertOrUpdateProfile(profile.copy(coins = profile.coins + amount))
    }
}

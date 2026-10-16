package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.admob.AdMobManager
import com.example.data.localization.AppLanguage
import androidx.lifecycle.viewModelScope
import com.example.data.database.LevelEntity
import com.example.data.database.TileClubDatabase
import com.example.data.database.UserProfileEntity
import com.example.data.model.BoosterType
import com.example.data.model.TileTheme
import com.example.data.model.TileThemes
import com.example.data.repository.GameRepository
import com.example.domain.GameEngine
import com.example.domain.GameState
import com.example.domain.GameStatus
import com.example.domain.SoundEffectTrigger
import com.example.domain.SoundHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppScreen {
    MAIN_MENU,
    LEVEL_MAP,
    GAME_PLAY
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val database = TileClubDatabase.getDatabase(application)
    private val repository = GameRepository(database.dao())
    private val soundHelper = SoundHelper(application)
    private val gameEngine = GameEngine()

    private var timerJob: Job? = null
    private var matchAnimationJob: Job? = null

    private val _currentScreen = MutableStateFlow(AppScreen.MAIN_MENU)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfileEntity())
    val userProfile: StateFlow<UserProfileEntity> = _userProfile.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ESPAÑOL)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _levels = MutableStateFlow<List<LevelEntity>>(emptyList())
    val levels: StateFlow<List<LevelEntity>> = _levels.asStateFlow()

    private val _selectedTheme = MutableStateFlow(TileThemes.FRUITS)
    val selectedTheme: StateFlow<TileTheme> = _selectedTheme.asStateFlow()

    private val _showShopDialog = MutableStateFlow(false)
    val showShopDialog: StateFlow<Boolean> = _showShopDialog.asStateFlow()

    private val _showDailyRewardDialog = MutableStateFlow(false)
    val showDailyRewardDialog: StateFlow<Boolean> = _showDailyRewardDialog.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showRulesDialog = MutableStateFlow(false)
    val showRulesDialog: StateFlow<Boolean> = _showRulesDialog.asStateFlow()

    private val _levelCompletionReward = MutableStateFlow(0)
    val levelCompletionReward: StateFlow<Int> = _levelCompletionReward.asStateFlow()

    private val _levelStarsEarned = MutableStateFlow(3)
    val levelStarsEarned: StateFlow<Int> = _levelStarsEarned.asStateFlow()

    private val _dailyClaimResult = MutableStateFlow<Int?>(null)
    val dailyClaimResult: StateFlow<Int?> = _dailyClaimResult.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeIfEmpty()
        }

        viewModelScope.launch {
            repository.userProfile.collect { profile ->
                _userProfile.value = profile
                soundHelper.soundEnabled = profile.soundEnabled
                soundHelper.hapticEnabled = profile.hapticEnabled
                val themeIndex = profile.selectedThemeIndex.coerceIn(0, TileThemes.ALL.size - 1)
                _selectedTheme.value = TileThemes.ALL[themeIndex]
                _currentLanguage.value = AppLanguage.fromCode(profile.languageCode)
            }
        }

        viewModelScope.launch {
            repository.allLevels.collect { levelList ->
                _levels.value = levelList
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen != AppScreen.GAME_PLAY) {
            timerJob?.cancel()
        }
        _currentScreen.value = screen
    }

    fun startLevel(levelNumber: Int) {
        matchAnimationJob?.cancel()
        val newState = gameEngine.loadLevel(levelNumber)
        _gameState.value = newState
        _levelCompletionReward.value = 0
        _currentScreen.value = AppScreen.GAME_PLAY
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_gameState.value.status == GameStatus.PLAYING) {
                    val (newState, trigger) = gameEngine.tickTimer()
                    _gameState.value = newState
                    when (trigger) {
                        SoundEffectTrigger.TIMER_WARNING -> soundHelper.playTimerTick()
                        SoundEffectTrigger.TIME_UP -> soundHelper.playTimeUp()
                        else -> {}
                    }
                }
            }
        }
    }

    fun restartCurrentLevel() {
        startLevel(_gameState.value.levelNumber)
    }

    fun nextLevel() {
        startLevel(_gameState.value.levelNumber + 1)
    }

    fun onTileClicked(tileId: Int) {
        val (newState, trigger) = gameEngine.onTileClicked(tileId)
        _gameState.value = newState

        when (trigger) {
            SoundEffectTrigger.TILE_CLICK -> soundHelper.playTileClick()
            SoundEffectTrigger.TRIPLE_MATCH -> {
                soundHelper.playTripleMatch()
                val matchingIds = newState.matchingTileIds
                // Retraso intencional para mostrar las 3 fichas agrupadas en la bandeja con su brillo
                // antes de eliminarlas definitivamente, cumpliendo con la expectativa de formarlas y eliminarlas
                matchAnimationJob?.cancel()
                matchAnimationJob = viewModelScope.launch {
                    delay(280)
                    val finalizedState = gameEngine.finalizeMatch(matchingIds)
                    _gameState.value = finalizedState
                    if (finalizedState.status == GameStatus.WON) {
                        soundHelper.playVictory()
                        onLevelWon(finalizedState)
                    } else if (finalizedState.status == GameStatus.LOST) {
                        soundHelper.playGameOver()
                    }
                }
            }
            SoundEffectTrigger.VICTORY -> {
                soundHelper.playVictory()
                onLevelWon(newState)
            }
            SoundEffectTrigger.GAME_OVER -> soundHelper.playGameOver()
            else -> {}
        }
    }

    private fun onLevelWon(state: GameState) {
        timerJob?.cancel()
        viewModelScope.launch {
            val moves = state.movesCount
            val initial = state.initialTilesCount
            // Bonus por tiempo restante
            val timeBonus = (state.timeRemainingSec * 5).coerceAtLeast(0)
            val finalScore = state.score + timeBonus

            // Calcular estrellas: 3 estrellas si es eficiente en movimientos y tiempo
            val starsEarned = when {
                moves <= initial + 6 && state.timePercent > 0.35f -> 3
                moves <= initial + 15 -> 2
                else -> 1
            }
            _levelStarsEarned.value = starsEarned
            val rewardCoins = repository.completeLevel(state.levelNumber, finalScore, moves, starsEarned)
            _levelCompletionReward.value = rewardCoins
        }
    }

    fun playStarSound(index: Int) {
        soundHelper.playStarEarned(index)
    }

    fun useBooster(type: BoosterType) {
        viewModelScope.launch {
            val profile = _userProfile.value
            val availableCount = when (type) {
                BoosterType.HINT -> profile.boosterHint
                BoosterType.UNDO -> profile.boosterUndo
                BoosterType.SHUFFLE -> profile.boosterShuffle
                BoosterType.MAGIC_WAND -> profile.boosterWand
                BoosterType.EXTRA_SLOT -> profile.boosterSlot
            }

            if (type == BoosterType.HINT && _gameState.value.hintCooldownSec > 0) {
                return@launch
            }

            val isEmergencyShuffle = (type == BoosterType.SHUFFLE && _gameState.value.hasNoPossibleMoves)

            if (availableCount > 0 || isEmergencyShuffle) {
                val success = when (type) {
                    BoosterType.HINT -> {
                        val (s, ok) = gameEngine.useHintBooster()
                        if (ok) {
                            _gameState.value = s
                            soundHelper.playBoosterHint()
                            viewModelScope.launch {
                                for (sec in 15 downTo 0) {
                                    _gameState.value = gameEngine.updateHintCooldown(sec)
                                    kotlinx.coroutines.delay(1000)
                                }
                            }
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(10000)
                                if (_gameState.value.highlightedTileIds.isNotEmpty()) {
                                    _gameState.value = _gameState.value.copy(highlightedTileIds = emptySet())
                                }
                            }
                        }
                        ok
                    }
                    BoosterType.UNDO -> {
                        val (s, ok) = gameEngine.useUndoBooster()
                        if (ok) {
                            _gameState.value = s
                            soundHelper.playBoosterUndo()
                        }
                        ok
                    }
                    BoosterType.SHUFFLE -> {
                        val (s, ok) = gameEngine.useShuffleBooster()
                        if (ok) {
                            _gameState.value = s
                            soundHelper.playBoosterShuffle()
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(3200)
                                _gameState.value = gameEngine.clearShuffleBanner()
                            }
                        }
                        ok
                    }
                    BoosterType.MAGIC_WAND -> {
                        val (s, ok) = gameEngine.useMagicWandBooster()
                        if (ok) {
                            _gameState.value = s
                            soundHelper.playBoosterWand()
                            if (s.status == GameStatus.WON) {
                                soundHelper.playVictory()
                                onLevelWon(s)
                            }
                        }
                        ok
                    }
                    BoosterType.EXTRA_SLOT -> {
                        val (s, ok) = gameEngine.useExtraSlotBooster()
                        if (ok) {
                            _gameState.value = s
                            soundHelper.playBoosterWand()
                        }
                        ok
                    }
                }
                if (success && availableCount > 0) {
                    repository.useBooster(type)
                }
            } else {
                // Open shop to buy this booster!
                _showShopDialog.value = true
            }
        }
    }

    fun buyBooster(type: BoosterType) {
        viewModelScope.launch {
            val bought = repository.buyBooster(type)
            if (bought) {
                soundHelper.playBoosterUndo()
            }
        }
    }

    fun reviveWithCoins() {
        viewModelScope.launch {
            val cost = 60
            if (repository.spendCoins(cost)) {
                val newState = gameEngine.reviveWithCoins()
                _gameState.value = newState
                soundHelper.playBoosterUndo()
            } else {
                _showShopDialog.value = true
            }
        }
    }

    fun addExtraTimeWithCoins(seconds: Int = 60) {
        viewModelScope.launch {
            val cost = 50
            if (repository.spendCoins(cost)) {
                val newState = gameEngine.addExtraTime(seconds)
                _gameState.value = newState
                soundHelper.playBoosterUndo()
            } else {
                _showShopDialog.value = true
            }
        }
    }

    /**
     * Permite revivir al jugador (Vida Extra) viendo un anuncio recompensado de AdMob.
     */
    fun watchAdForExtraLife(activity: Activity?, onAdUnavailable: () -> Unit = {}) {
        AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                viewModelScope.launch {
                    val newState = gameEngine.reviveWithExtraLife()
                    _gameState.value = newState
                    soundHelper.playVictory()
                }
            },
            onAdClosed = {
                // Return to game if state revived
            },
            onAdUnavailable = {
                onAdUnavailable()
            }
        )
    }

    /**
     * Muestra anuncio emergente automático (Interstitial) cuando el jugador pierde la partida.
     */
    fun triggerLossInterstitial(activity: Activity?) {
        AdMobManager.showInterstitial(activity)
    }

    /**
     * Permite ganar 100 monedas gratis viendo un anuncio recompensado.
     */
    fun watchAdForFreeCoins(activity: Activity?) {
        AdMobManager.showRewardedAd(
            activity = activity,
            onRewardEarned = {
                viewModelScope.launch {
                    repository.addRewardCoins(100)
                    soundHelper.playVictory()
                }
            }
        )
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            val coins = repository.claimDailyReward()
            _dailyClaimResult.value = coins
            if (coins != null) {
                soundHelper.playVictory()
            }
        }
    }

    fun switchTheme(index: Int) {
        viewModelScope.launch {
            repository.setSelectedTheme(index)
        }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            _currentLanguage.value = language
            repository.setLanguage(language.code)
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            repository.toggleSound()
        }
    }

    fun toggleHaptics() {
        viewModelScope.launch {
            repository.toggleHaptics()
        }
    }

    fun setShowShopDialog(show: Boolean) {
        _showShopDialog.value = show
    }

    fun setShowDailyRewardDialog(show: Boolean) {
        _showDailyRewardDialog.value = show
        _dailyClaimResult.value = null
    }

    fun setShowSettingsDialog(show: Boolean) {
        _showSettingsDialog.value = show
    }

    fun setShowRulesDialog(show: Boolean) {
        _showRulesDialog.value = show
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        matchAnimationJob?.cancel()
        soundHelper.release()
    }
}

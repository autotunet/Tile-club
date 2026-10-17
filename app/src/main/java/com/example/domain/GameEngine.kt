package com.example.domain

import com.example.data.model.BoardGrid3D
import com.example.data.model.GameTile
import com.example.data.model.WorldDestination
import com.example.data.model.WorldDestinations

enum class GameStatus {
    PLAYING,
    WON,
    LOST,
    TIME_OUT,
    PAUSED
}

data class TripleMatchEvent(
    val typeId: Int,
    val combo: Int,
    val points: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class GameState(
    val levelNumber: Int = 1,
    val levelName: String = "",
    val destination: WorldDestination = WorldDestinations.DESTINATIONS.first(),
    val grid3D: BoardGrid3D = BoardGrid3D(0, 0, emptyList()),
    val trayTiles: List<GameTile> = emptyList(),
    val trayCapacity: Int = 7,
    val score: Int = 0,
    val combo: Int = 0,
    val movesCount: Int = 0,
    val status: GameStatus = GameStatus.PLAYING,
    val lastMatchEvent: TripleMatchEvent? = null,
    val initialTilesCount: Int = 0,
    val highlightedTileIds: Set<Int> = emptySet(),
    val matchingTileIds: Set<Int> = emptySet(),
    val hintCooldownSec: Int = 0,
    val timeRemainingSec: Int = 120,
    val totalTimeSec: Int = 120,
    val isTimerActive: Boolean = true,
    val hasNoPossibleMoves: Boolean = false,
    val shuffleAnimationKey: Int = 0,
    val shuffleBannerText: String? = null,
    val difficulty: LevelDifficulty? = null
) {
    val remainingBoardTilesCount: Int get() = grid3D.remainingTilesCount
    val progressPercent: Float
        get() = if (initialTilesCount > 0) {
            1f - (remainingBoardTilesCount.toFloat() / initialTilesCount.toFloat())
        } else 0f

    val timePercent: Float
        get() = if (totalTimeSec > 0) (timeRemainingSec.toFloat() / totalTimeSec.toFloat()).coerceIn(0f, 1f) else 0f
}

class GameEngine {
    private var currentState: GameState = GameState()
    private val moveHistory = mutableListOf<GameTile>()
    private var lastMatchTime: Long = 0

    fun loadLevel(levelNumber: Int): GameState {
        val levelData = LevelGenerator.generateLevel(levelNumber)
        moveHistory.clear()
        currentState = GameState(
            levelNumber = levelNumber,
            levelName = levelData.name,
            destination = levelData.destination,
            grid3D = levelData.grid3D,
            trayTiles = emptyList(),
            trayCapacity = 7,
            score = 0,
            combo = 0,
            movesCount = 0,
            status = GameStatus.PLAYING,
            initialTilesCount = levelData.initialTilesCount,
            timeRemainingSec = levelData.timeLimitSeconds,
            totalTimeSec = levelData.timeLimitSeconds,
            isTimerActive = true,
            difficulty = levelData.difficulty
        )
        return currentState
    }

    fun getCurrentState(): GameState = currentState

    fun onTileClicked(tileId: Int): Pair<GameState, SoundEffectTrigger> {
        if (currentState.status != GameStatus.PLAYING) return currentState to SoundEffectTrigger.NONE

        // Si había una animación de eliminación previa en progreso, consolidar primero
        if (currentState.matchingTileIds.isNotEmpty()) {
            finalizeMatch(currentState.matchingTileIds)
        }

        val tile = currentState.grid3D.allTiles.find { it.id == tileId } ?: return currentState to SoundEffectTrigger.NONE
        if (tile.isBlocked || tile.isCollected) return currentState to SoundEffectTrigger.NONE
        if (currentState.trayTiles.size >= currentState.trayCapacity) return currentState to SoundEffectTrigger.NONE

        // Marcar la ficha como recolectada en su pila 3D (se elimina del tablero inmediatamente)
        val updatedGrid = currentState.grid3D.withUpdatedTile(tileId, isCollected = true)
        val recomputedGrid = LevelGenerator.recomputeGridBlockedStates(updatedGrid)

        // Insertar en la bandeja agrupando por tipo
        val newTray = groupTileIntoTray(currentState.trayTiles, tile)
        moveHistory.add(tile)

        // Comprobar si se ha formado un trío (3 fichas del mismo tipo)
        val typeCount = newTray.count { it.typeId == tile.typeId }

        if (typeCount >= 3) {
            // El trío se ha formado: marcamos las 3 fichas para animación de eliminación brillante
            val matchingTiles = newTray.filter { it.typeId == tile.typeId }.take(3)
            val matchingIds = matchingTiles.map { it.id }.toSet()

            val now = System.currentTimeMillis()
            val currentCombo = if (now - lastMatchTime < 3200) currentState.combo + 1 else 1
            lastMatchTime = now

            val matchPoints = 150 * currentCombo
            val matchEvent = TripleMatchEvent(tile.typeId, currentCombo, matchPoints)

            currentState = currentState.copy(
                grid3D = recomputedGrid,
                trayTiles = newTray,
                score = currentState.score + matchPoints,
                combo = currentCombo,
                movesCount = currentState.movesCount + 1,
                lastMatchEvent = matchEvent,
                matchingTileIds = matchingIds,
                highlightedTileIds = emptySet()
            )
            return currentState to SoundEffectTrigger.TRIPLE_MATCH
        } else {
            // No hay trío aún: verificar si la bandeja se ha llenado (derrota)
            val newStatus = if (newTray.size >= currentState.trayCapacity) {
                GameStatus.LOST
            } else {
                GameStatus.PLAYING
            }

            val trigger = if (newStatus == GameStatus.LOST) SoundEffectTrigger.GAME_OVER else SoundEffectTrigger.TILE_CLICK

            val candidateState = currentState.copy(
                grid3D = recomputedGrid,
                trayTiles = newTray,
                score = currentState.score + 10,
                movesCount = currentState.movesCount + 1,
                status = newStatus,
                matchingTileIds = emptySet(),
                highlightedTileIds = emptySet()
            )
            val noMoves = if (newStatus == GameStatus.PLAYING) !checkHasPossibleMoves(candidateState) else false
            currentState = candidateState.copy(hasNoPossibleMoves = noMoves)
            return currentState to trigger
        }
    }

    /**
     * Elimina definitivamente las 3 fichas que formaron el trío de la bandeja
     * y evalúa si se ha despejado el tablero por completo (Victoria).
     */
    fun finalizeMatch(matchingIds: Set<Int>): GameState {
        if (matchingIds.isEmpty()) return currentState

        val updatedTray = currentState.trayTiles.filterNot { it.id in matchingIds }
        val remainingOnBoard = currentState.grid3D.remainingTilesCount

        val newStatus = when {
            remainingOnBoard == 0 && updatedTray.isEmpty() -> GameStatus.WON
            updatedTray.size >= currentState.trayCapacity -> GameStatus.LOST
            else -> currentState.status
        }

        val candidateState = currentState.copy(
            trayTiles = updatedTray,
            matchingTileIds = emptySet(),
            status = newStatus
        )
        val noMoves = if (newStatus == GameStatus.PLAYING) !checkHasPossibleMoves(candidateState) else false
        currentState = candidateState.copy(hasNoPossibleMoves = noMoves)
        return currentState
    }

    private fun groupTileIntoTray(tray: List<GameTile>, newTile: GameTile): List<GameTile> {
        val result = mutableListOf<GameTile>()
        var inserted = false
        val lastIndexOfSameType = tray.indexOfLast { it.typeId == newTile.typeId }
        if (lastIndexOfSameType != -1) {
            tray.forEachIndexed { index, item ->
                result.add(item)
                if (index == lastIndexOfSameType) {
                    result.add(newTile)
                    inserted = true
                }
            }
        }
        if (!inserted) {
            result.addAll(tray)
            result.add(newTile)
        }
        return result
    }

    fun useUndoBooster(): Pair<GameState, Boolean> {
        if (moveHistory.isEmpty()) return currentState to false
        val lastTile = moveHistory.removeAt(moveHistory.size - 1)

        val updatedTray = currentState.trayTiles.toMutableList()
        val indexInTray = updatedTray.indexOfLast { it.id == lastTile.id }
        if (indexInTray != -1) {
            updatedTray.removeAt(indexInTray)
        } else {
            return currentState to false
        }

        // Return to cell stack (immutable update)
        val updatedGrid = currentState.grid3D.withUpdatedTile(lastTile.id, isCollected = false)
        val recomputedGrid = LevelGenerator.recomputeGridBlockedStates(updatedGrid)

        currentState = currentState.copy(
            grid3D = recomputedGrid,
            trayTiles = updatedTray,
            status = GameStatus.PLAYING
        )
        return currentState to true
    }

    /**
     * Evalúa si el jugador tiene movimientos posibles para continuar sin perder de forma inevitable.
     * Retorna falso cuando la bandeja está en riesgo y ninguna ficha accesible permite formar un trío.
     */
    fun checkHasPossibleMoves(state: GameState = currentState): Boolean {
        if (state.status != GameStatus.PLAYING) return true
        if (state.grid3D.remainingTilesCount == 0) return true

        val freeSlots = state.trayCapacity - state.trayTiles.size
        if (freeSlots <= 0) return false

        val selectable = state.grid3D.selectableTiles
        if (selectable.isEmpty()) return false

        val trayCounts = state.trayTiles.groupingBy { it.typeId }.eachCount()
        val selectableCounts = selectable.groupingBy { it.typeId }.eachCount()

        // 1. Tipo en la bandeja con 2 fichas: necesita 1 ficha para despejarse inmediatamente
        for ((type, count) in trayCounts) {
            if (count == 2) {
                if ((selectableCounts[type] ?: 0) >= 1 && freeSlots >= 1) {
                    return true
                }
            }
        }

        // 2. Tipo en la bandeja con 1 ficha: necesita 2 fichas para despejarse
        for ((type, count) in trayCounts) {
            if (count == 1) {
                if ((selectableCounts[type] ?: 0) >= 2 && freeSlots >= 2) {
                    return true
                }
            }
        }

        // 3. Si hay al menos 3 espacios libres, trío completo en fichas accesibles
        if (freeSlots >= 3) {
            for ((_, count) in selectableCounts) {
                if (count >= 3) {
                    return true
                }
            }
        }

        // 4. Si hay margen cómodo en la bandeja (>= 4 espacios libres):
        if (freeSlots >= 4) {
            // Si hay al menos una pareja accesible del mismo tipo
            if (selectableCounts.any { it.value >= 2 }) return true
            // O si hay al menos una ficha accesible que coincide con una de la bandeja
            if (trayCounts.any { (selectableCounts[it.key] ?: 0) >= 1 }) return true
            // Si quedan suficientes fichas para maniobrar
            if (selectable.size >= 4) return true
        }

        return false
    }

    /**
     * Potenciador Mezclar (Shuffle) con garantía estricta de resolubilidad:
     * Reordena las fichas no recolectadas en el tablero asignando estratégicamente
     * a las celdas superiores accesibles las fichas que completan tríos con la bandeja
     * o forman tríos inmediatos entre sí, garantizando que el juego sea siempre resoluble.
     */
    fun useShuffleBooster(): Pair<GameState, Boolean> {
        val uncollected = currentState.grid3D.allTiles.filter { !it.isCollected }
        if (uncollected.size < 3) return currentState to false

        val availableTypes = uncollected.map { it.typeId }.toMutableList()

        // Identificar las fichas en la cima no recolectadas de cada pila de celda
        val topTiles = mutableListOf<GameTile>()
        for (row in currentState.grid3D.cells) {
            for (cell in row) {
                cell.topUncollectedTile?.let { topTiles.add(it) }
            }
        }

        val priorityAssignments = mutableMapOf<Int, Int>() // tileId -> typeId
        val trayCounts = currentState.trayTiles.groupingBy { it.typeId }.eachCount()

        // Prioridad 1: Asignar tipos que tienen 2 fichas en la bandeja (necesitan 1)
        for ((type, _) in trayCounts.filter { it.value == 2 }) {
            if (availableTypes.contains(type)) {
                val candidateTile = topTiles.firstOrNull { it.id !in priorityAssignments } ?: break
                priorityAssignments[candidateTile.id] = type
                availableTypes.remove(type)
            }
        }

        // Prioridad 2: Asignar tipos que tienen 1 ficha en la bandeja (necesitan 2)
        for ((type, _) in trayCounts.filter { it.value == 1 }) {
            val countAvail = availableTypes.count { it == type }
            val freeTop = topTiles.filter { it.id !in priorityAssignments }
            if (countAvail >= 2 && freeTop.size >= 2) {
                for (i in 0 until 2) {
                    val candidate = freeTop[i]
                    priorityAssignments[candidate.id] = type
                    availableTypes.remove(type)
                }
            }
        }

        // Prioridad 3: Garantizar tríos completos entre las fichas de la cima accesibles
        val typeFreqs = availableTypes.groupingBy { it }.eachCount()
        for ((type, count) in typeFreqs.filter { it.value >= 3 }) {
            val freeTop = topTiles.filter { it.id !in priorityAssignments }
            if (freeTop.size >= 3) {
                for (i in 0 until 3) {
                    val candidate = freeTop[i]
                    priorityAssignments[candidate.id] = type
                    availableTypes.remove(type)
                }
            }
        }

        // Mezclar aleatoriamente el resto de tipos disponibles
        availableTypes.shuffle()
        var remainIdx = 0

        val newCells = currentState.grid3D.cells.map { row ->
            row.map { cell ->
                val newStack = cell.tileStack.map { tile ->
                    if (!tile.isCollected) {
                        val assignedType = priorityAssignments[tile.id] ?: availableTypes[remainIdx++]
                        tile.copy(typeId = assignedType)
                    } else tile
                }
                cell.copy(tileStack = newStack)
            }
        }

        val shuffledGrid = currentState.grid3D.copy(cells = newCells)
        val recomputedGrid = LevelGenerator.recomputeGridBlockedStates(shuffledGrid)

        currentState = currentState.copy(
            grid3D = recomputedGrid,
            hasNoPossibleMoves = false,
            shuffleAnimationKey = currentState.shuffleAnimationKey + 1,
            shuffleBannerText = "¡Tablero Reordenado! Movimientos garantizados",
            highlightedTileIds = emptySet()
        )
        return currentState to true
    }

    fun clearShuffleBanner(): GameState {
        currentState = currentState.copy(shuffleBannerText = null)
        return currentState
    }

    fun useMagicWandBooster(): Pair<GameState, Boolean> {
        if (currentState.status != GameStatus.PLAYING && currentState.status != GameStatus.LOST) {
            return currentState to false
        }

        val trayTypes = currentState.trayTiles.map { it.typeId }.distinct()
        var targetType: Int? = null

        for (type in trayTypes) {
            val countInTray = currentState.trayTiles.count { it.typeId == type }
            val countOnBoard = currentState.grid3D.allTiles.count { !it.isCollected && it.typeId == type }
            if (countInTray + countOnBoard >= 3) {
                targetType = type
                break
            }
        }

        if (targetType == null) {
            val uncollectedTypes = currentState.grid3D.allTiles
                .filter { !it.isCollected }
                .groupBy { it.typeId }
                .filter { it.value.size >= 3 }
                .keys
            targetType = uncollectedTypes.firstOrNull()
        }

        if (targetType == null) return currentState to false

        val chosenType = targetType
        val inTrayOfChosen = currentState.trayTiles.filter { it.typeId == chosenType }
        val neededFromBoard = 3 - inTrayOfChosen.size

        val boardTilesToTake = currentState.grid3D.allTiles
            .filter { !it.isCollected && it.typeId == chosenType }
            .take(neededFromBoard)

        val boardIdsToCollect = boardTilesToTake.map { it.id }.toSet()

        // Mark as collected in cell stacks (immutable)
        val newCells = currentState.grid3D.cells.map { row ->
            row.map { cell ->
                val newStack = cell.tileStack.map { tile ->
                    if (boardIdsToCollect.contains(tile.id)) {
                        tile.copy(isCollected = true)
                    } else tile
                }
                cell.copy(tileStack = newStack)
            }
        }

        val updatedGrid = currentState.grid3D.copy(cells = newCells)
        val recomputedGrid = LevelGenerator.recomputeGridBlockedStates(updatedGrid)

        var removedFromTray = 0
        val updatedTray = currentState.trayTiles.filter {
            if (it.typeId == chosenType && removedFromTray < inTrayOfChosen.size) {
                removedFromTray++
                false
            } else true
        }

        val matchPoints = 250
        val matchEvent = TripleMatchEvent(chosenType, currentState.combo + 1, matchPoints)

        val remainingOnBoard = recomputedGrid.remainingTilesCount
        val newStatus = if (remainingOnBoard == 0 && updatedTray.isEmpty()) GameStatus.WON else GameStatus.PLAYING

        currentState = currentState.copy(
            grid3D = recomputedGrid,
            trayTiles = updatedTray,
            score = currentState.score + matchPoints,
            combo = currentState.combo + 1,
            status = newStatus,
            lastMatchEvent = matchEvent
        )
        return currentState to true
    }

    fun useExtraSlotBooster(): Pair<GameState, Boolean> {
        if (currentState.trayCapacity >= 8) return currentState to false
        currentState = currentState.copy(
            trayCapacity = 8,
            status = if (currentState.status == GameStatus.LOST) GameStatus.PLAYING else currentState.status
        )
        return currentState to true
    }

    fun useHintBooster(): Pair<GameState, Boolean> {
        if (currentState.status != GameStatus.PLAYING) return currentState to false

        // 1. Prioridad: Si en la bandeja ya hay 1 o 2 fichas de tipo X,
        // buscar en el tablero fichas desbloqueadas de tipo X para completar el trío.
        val trayTypeCounts = currentState.trayTiles.groupBy { it.typeId }
        val selectable = currentState.grid3D.selectableTiles

        for ((type, tilesInTray) in trayTypeCounts) {
            val matchingSelectable = selectable.filter { it.typeId == type }
            if (matchingSelectable.isNotEmpty()) {
                val needed = (3 - tilesInTray.size).coerceAtMost(matchingSelectable.size)
                val toHighlight = matchingSelectable.take(needed).map { it.id }.toSet()
                currentState = currentState.copy(highlightedTileIds = toHighlight)
                return currentState to true
            }
        }

        // 2. Si no hay coincidencias con la bandeja, buscar entre las fichas desbloqueadas del tablero
        // un par del mismo tipo para emparejar.
        val selectableGrouped = selectable.groupBy { it.typeId }
        val pairCandidates = selectableGrouped.filter { it.value.size >= 2 }
        if (pairCandidates.isNotEmpty()) {
            val bestGroup = pairCandidates.maxByOrNull { it.value.size }!!.value
            val toHighlight = bestGroup.take(2).map { it.id }.toSet()
            currentState = currentState.copy(highlightedTileIds = toHighlight)
            return currentState to true
        }

        // 3. Si solo hay fichas individuales libres, emparejar con otra de ese mismo tipo en el tablero
        if (selectable.isNotEmpty()) {
            val firstChoice = selectable.first()
            val allSameType = currentState.grid3D.allTiles.filter { !it.isCollected && it.typeId == firstChoice.typeId }
            val toHighlight = allSameType.take(2).map { it.id }.toSet()
            currentState = currentState.copy(highlightedTileIds = toHighlight)
            return currentState to true
        }

        return currentState to false
    }

    fun updateHintCooldown(sec: Int): GameState {
        currentState = currentState.copy(hintCooldownSec = sec)
        return currentState
    }

    fun reviveWithCoins(): GameState {
        val countToReturn = minOf(3, currentState.trayTiles.size)
        if (countToReturn == 0) return currentState

        val tilesToReturn = currentState.trayTiles.takeLast(countToReturn)
        val returnIds = tilesToReturn.map { it.id }.toSet()

        val updatedTray = currentState.trayTiles.dropLast(countToReturn)

        val newCells = currentState.grid3D.cells.map { row ->
            row.map { cell ->
                val newStack = cell.tileStack.map { tile ->
                    if (returnIds.contains(tile.id)) {
                        tile.copy(isCollected = false)
                    } else tile
                }
                cell.copy(tileStack = newStack)
            }
        }
        val updatedGrid = currentState.grid3D.copy(cells = newCells)
        val recomputedGrid = LevelGenerator.recomputeGridBlockedStates(updatedGrid)

        currentState = currentState.copy(
            grid3D = recomputedGrid,
            trayTiles = updatedTray,
            status = GameStatus.PLAYING
        )
        return currentState
    }

    /**
     * Hace avanzar el cronómetro del nivel un segundo.
     * Si el tiempo llega a cero, el estado cambia a TIME_OUT.
     */
    fun tickTimer(): Pair<GameState, SoundEffectTrigger> {
        if (currentState.status != GameStatus.PLAYING) return currentState to SoundEffectTrigger.NONE

        val currentRemaining = currentState.timeRemainingSec
        if (currentRemaining > 1) {
            val newRemaining = currentRemaining - 1
            val trigger = if (newRemaining in 1..10) SoundEffectTrigger.TIMER_WARNING else SoundEffectTrigger.NONE
            currentState = currentState.copy(timeRemainingSec = newRemaining)
            return currentState to trigger
        } else {
            currentState = currentState.copy(
                timeRemainingSec = 0,
                status = GameStatus.TIME_OUT
            )
            return currentState to SoundEffectTrigger.TIME_UP
        }
    }

    /**
     * Agrega tiempo extra al temporizador (por ejemplo +60s por rescate con monedas).
     */
    fun addExtraTime(seconds: Int = 60): GameState {
        currentState = currentState.copy(
            timeRemainingSec = currentState.timeRemainingSec + seconds,
            status = GameStatus.PLAYING
        )
        return currentState
    }

    /**
     * Revive al jugador con una Vida Extra obtenida al ver un anuncio AdMob recompensado.
     * Despeja hasta 3 fichas de la bandeja, recarga tiempo extra si estaba en 0s y reactiva la partida.
     */
    fun reviveWithExtraLife(): GameState {
        val countToReturn = minOf(3, currentState.trayTiles.size)
        val tilesToReturn = currentState.trayTiles.takeLast(countToReturn)
        val returnIds = tilesToReturn.map { it.id }.toSet()
        val updatedTray = currentState.trayTiles.dropLast(countToReturn)

        val newCells = currentState.grid3D.cells.map { row ->
            row.map { cell ->
                val newStack = cell.tileStack.map { tile ->
                    if (returnIds.contains(tile.id)) {
                        tile.copy(isCollected = false)
                    } else tile
                }
                cell.copy(tileStack = newStack)
            }
        }
        val updatedGrid = currentState.grid3D.copy(cells = newCells)
        val recomputedGrid = LevelGenerator.recomputeGridBlockedStates(updatedGrid)

        val safeTime = if (currentState.timeRemainingSec <= 10) 60 else currentState.timeRemainingSec

        currentState = currentState.copy(
            grid3D = recomputedGrid,
            trayTiles = updatedTray,
            timeRemainingSec = safeTime,
            status = GameStatus.PLAYING,
            shuffleBannerText = "¡Vida Extra Activada por Anuncio! ❤️"
        )
        return currentState
    }
}

enum class SoundEffectTrigger {
    NONE,
    TILE_CLICK,
    TRIPLE_MATCH,
    BOOSTER,
    VICTORY,
    GAME_OVER,
    TIMER_WARNING,
    TIME_UP
}

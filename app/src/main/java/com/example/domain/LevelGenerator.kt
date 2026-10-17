package com.example.domain

import com.example.data.model.BoardGrid3D
import com.example.data.model.GameTile
import com.example.data.model.GridCell3D
import com.example.data.model.WorldDestination
import com.example.data.model.WorldDestinations
import kotlin.random.Random

data class LevelDifficulty(
    val levelNumber: Int,
    val gridCols: Int,
    val gridRows: Int,
    val maxStackHeight: Int,
    val targetTiles: Int,
    val numSymbols: Int,
    val layoutType: String,
    val timeLimitSeconds: Int = 120,
    val difficultyTier: String = "Desafiante",
    val difficultyIcon: String = "⚡"
)

data class LevelData3D(
    val levelNumber: Int,
    val name: String,
    val destination: WorldDestination,
    val difficulty: LevelDifficulty,
    val grid3D: BoardGrid3D
) {
    val allTiles: List<GameTile> get() = grid3D.allTiles
    val initialTilesCount: Int get() = allTiles.size
    val timeLimitSeconds: Int get() = difficulty.timeLimitSeconds
}

object LevelGenerator {

    /**
     * Calcula la curva de dificultad aumentada desde el nivel 1 hasta el nivel 1000.
     * Diseñada para proporcionar un desafío sustancialmente mayor, con más fichas,
     * pilas 3D más profundas, mayor variedad de símbolos y tiempos más ajustados.
     */
    fun calculateDifficulty(level: Int): LevelDifficulty {
        val clampedLevel = level.coerceIn(1, 1000)

        val (cols, rows) = when {
            clampedLevel <= 5 -> 5 to 6
            clampedLevel <= 25 -> 6 to 7
            clampedLevel <= 100 -> 7 to 8
            clampedLevel <= 300 -> 8 to 8
            else -> 8 to 9
        }

        // Mayor altura de pila para capas 3D más profundas y desafiantes
        val maxStack = when {
            clampedLevel <= 2 -> 4
            clampedLevel <= 10 -> 5
            clampedLevel <= 35 -> 6
            clampedLevel <= 100 -> 7
            clampedLevel <= 300 -> 8
            else -> 9
        }

        // Mayor cantidad de símbolos: incrementa la presión sobre la bandeja de 7 ranuras
        val numSymbols = when {
            clampedLevel <= 3 -> 8
            clampedLevel <= 10 -> 10
            clampedLevel <= 30 -> 12
            clampedLevel <= 80 -> 14
            clampedLevel <= 200 -> 16
            clampedLevel <= 500 -> 18
            else -> 20
        }

        // Mayor cantidad de fichas objetivo (siempre múltiplo de 3)
        // Comienza en 36 (12 tríos) y escala rápidamente hasta 216 fichas
        val rawTiles = when {
            clampedLevel == 1 -> 36
            clampedLevel <= 3 -> 39 + (clampedLevel - 1) * 3 // 42, 45
            clampedLevel <= 10 -> 48 + ((clampedLevel - 3) * 3) // hasta 69
            clampedLevel <= 30 -> 72 + (((clampedLevel - 10) / 2) * 3) // hasta 102
            clampedLevel <= 100 -> 105 + (((clampedLevel - 30) / 4) * 3) // hasta 156
            clampedLevel <= 300 -> 159 + (((clampedLevel - 100) / 10) * 3) // hasta 216
            else -> 216
        }.coerceIn(36, 216)

        val targetTiles = (rawTiles / 3) * 3

        val layoutNames = listOf(
            "Pirámide Central Escalonada",
            "Fortaleza de 4 Torres",
            "Cumbres Gemelas Andinas",
            "Laberinto de Murallas",
            "Espiral del Dragón",
            "Zigurats Cruzados",
            "Anillos Concéntricos",
            "El Gran Baluarte Imperial"
        )
        val layoutType = layoutNames[clampedLevel % layoutNames.size]

        // Temporizador de nivel ajustado y de ritmo rápido
        val timeLimit = when {
            clampedLevel <= 3 -> 60 // 1m 00s para 36-45 fichas
            clampedLevel <= 10 -> 70 // 1m 10s
            clampedLevel <= 30 -> 85 // 1m 25s
            clampedLevel <= 80 -> 100 // 1m 40s
            clampedLevel <= 200 -> 120 // 2m 00s
            clampedLevel <= 500 -> 135 // 2m 15s
            else -> 150 // 2m 30s
        }

        val (tier, icon) = when {
            clampedLevel <= 3 -> "Desafiante" to "⚡"
            clampedLevel <= 15 -> "Difícil" to "🔥"
            clampedLevel <= 50 -> "Muy Difícil" to "⚔️"
            clampedLevel <= 150 -> "Experto" to "💎"
            clampedLevel <= 350 -> "Maestro" to "👑"
            else -> "Leyenda Extrema" to "☠️"
        }

        return LevelDifficulty(
            levelNumber = clampedLevel,
            gridCols = cols,
            gridRows = rows,
            maxStackHeight = maxStack,
            targetTiles = targetTiles,
            numSymbols = numSymbols,
            layoutType = layoutType,
            timeLimitSeconds = timeLimit,
            difficultyTier = tier,
            difficultyIcon = icon
        )
    }

    /**
     * Genera un nivel completo usando la estructura de datos en cuadrícula 3D (BoardGrid3D).
     */
    fun generateLevel(levelNumber: Int): LevelData3D {
        val diff = calculateDifficulty(levelNumber)
        val destination = WorldDestinations.getDestinationForLevel(levelNumber)
        val rand = Random(levelNumber * 104729L + 42)

        val heightMap = computeHeightPattern(diff, rand)
        val stackSlots = mutableListOf<Pair<Int, Int>>() // list of (col, row)
        for (r in 0 until diff.gridRows) {
            for (c in 0 until diff.gridCols) {
                val h = heightMap[r][c]
                repeat(h) {
                    stackSlots.add(c to r)
                }
            }
        }

        // Asegurar que la cantidad sea múltiplo de 3 y coincida con targetTiles
        var validSlots = stackSlots
        if (validSlots.size > diff.targetTiles) {
            validSlots = validSlots.shuffled(rand).take((diff.targetTiles / 3) * 3).toMutableList()
        } else if (validSlots.size < diff.targetTiles) {
            val needed = diff.targetTiles - validSlots.size
            val candidates = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until diff.gridRows) {
                for (c in 0 until diff.gridCols) {
                    if (heightMap[r][c] > 0) candidates.add(c to r)
                }
            }
            if (candidates.isEmpty()) {
                candidates.add(diff.gridCols / 2 to diff.gridRows / 2)
            }
            for (i in 0 until needed) {
                validSlots.add(candidates[i % candidates.size])
            }
        }
        val exactCount = (validSlots.size / 3) * 3
        val trimmedSlots = validSlots.take(exactCount)

        // Generar pool de tipos en tríos estrictos
        val triplets = exactCount / 3
        val typePool = mutableListOf<Int>()
        for (i in 0 until triplets) {
            val type = i % diff.numSymbols
            typePool.add(type)
            typePool.add(type)
            typePool.add(type)
        }
        typePool.shuffle(rand)

        // Agrupar slots por celda
        val cellSlotCounts = trimmedSlots.groupBy { it }
        var tileIdCounter = 0

        val cellMatrix = List(diff.gridRows) { r ->
            List(diff.gridCols) { c ->
                val slotsForCell = cellSlotCounts[c to r] ?: emptyList()
                val stack = slotsForCell.mapIndexed { stackIndex, _ ->
                    val type = typePool[tileIdCounter++]
                    GameTile(
                        id = tileIdCounter - 1,
                        typeId = type,
                        layer = stackIndex,
                        col = c.toFloat(),
                        row = r.toFloat(),
                        isCollected = false,
                        isBlocked = false
                    )
                }
                GridCell3D(col = c, row = r, tileStack = stack)
            }
        }

        val grid3D = BoardGrid3D(
            columns = diff.gridCols,
            rows = diff.gridRows,
            cells = cellMatrix
        )

        // Calcular estados de bloqueo iniciales
        val evaluatedGrid = recomputeGridBlockedStates(grid3D)

        val levelTitle = "${destination.flag} ${destination.country} • Nivel $levelNumber"
        return LevelData3D(
            levelNumber = levelNumber,
            name = levelTitle,
            destination = destination,
            difficulty = diff,
            grid3D = evaluatedGrid
        )
    }

    /**
     * Calcula la máscara de altura de las pilas en la cuadrícula según los 8 diseños arquitectónicos.
     */
    private fun computeHeightPattern(diff: LevelDifficulty, rand: Random): Array<IntArray> {
        val rows = diff.gridRows
        val cols = diff.gridCols
        val maxH = diff.maxStackHeight
        val pattern = Array(rows) { IntArray(cols) { 0 } }

        val centerR = rows / 2.0f
        val centerC = cols / 2.0f
        val maxDist = kotlin.math.hypot(centerR.toDouble(), centerC.toDouble()).toFloat()

        val layoutIndex = diff.levelNumber % 8

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val distFromCenter = kotlin.math.hypot((r - centerR).toDouble(), (c - centerC).toDouble()).toFloat()
                val normDist = (1f - (distFromCenter / maxDist)).coerceIn(0f, 1f)

                val h = when (layoutIndex) {
                    0 -> {
                        // Pirámide central escalonada
                        (normDist * maxH + 0.5f).toInt().coerceIn(1, maxH)
                    }
                    1 -> {
                        // Fortaleza con 4 torres en las esquinas y núcleo
                        val isCorner = (r <= 1 || r >= rows - 2) && (c <= 1 || c >= cols - 2)
                        val isPerimeter = (r == 0 || r == rows - 1 || c == 0 || c == cols - 1)
                        when {
                            isCorner -> maxH
                            distFromCenter < 1.6f -> (maxH - 1).coerceAtLeast(2)
                            isPerimeter -> (maxH / 2).coerceAtLeast(1)
                            else -> 1
                        }
                    }
                    2 -> {
                        // Doble cima / Cresta de dragón con foso intermedio
                        val distPeak1 = kotlin.math.hypot((r - centerR).toDouble(), (c - (centerC - 1.6f)).toDouble()).toFloat()
                        val distPeak2 = kotlin.math.hypot((r - centerR).toDouble(), (c - (centerC + 1.6f)).toDouble()).toFloat()
                        val peakDist = minOf(distPeak1, distPeak2)
                        (maxH - peakDist * 1.2f).toInt().coerceIn(1, maxH)
                    }
                    3 -> {
                        // Laberinto de murallas y columnas
                        if ((r + c) % 2 == 0) {
                            (normDist * maxH + 1).toInt().coerceIn(2, maxH)
                        } else {
                            if (normDist > 0.35f) 1 else 0
                        }
                    }
                    4 -> {
                        // Espiral del Dragón
                        val angle = (kotlin.math.atan2((r - centerR).toDouble(), (c - centerC).toDouble()) + Math.PI).toFloat()
                        val spiralFactor = ((angle / (2 * Math.PI.toFloat()) + normDist) % 1.0f)
                        (spiralFactor * maxH + 1).toInt().coerceIn(1, maxH)
                    }
                    5 -> {
                        // Zigurats Cruzados (Cruz sagrada con gradas)
                        val isCrossAxis = (kotlin.math.abs(r - centerR) < 1.3f || kotlin.math.abs(c - centerC) < 1.3f)
                        if (isCrossAxis) {
                            (normDist * maxH + 1).toInt().coerceIn(2, maxH)
                        } else {
                            if (distFromCenter < 2.5f) 1 else 0
                        }
                    }
                    6 -> {
                        // Anillos Concéntricos con crestas alternadas
                        val ring = (distFromCenter * 1.5f).toInt()
                        if (ring % 2 == 0) {
                            (normDist * maxH + 1).toInt().coerceIn(2, maxH)
                        } else {
                            1
                        }
                    }
                    else -> {
                        // Gran Baluarte Imperial Staggered
                        val diamondDist = kotlin.math.abs(r - centerR) + kotlin.math.abs(c - centerC)
                        if (diamondDist < 2.5f) {
                            maxH
                        } else if (diamondDist < 4.2f) {
                            (maxH - 1).coerceAtLeast(2)
                        } else {
                            1
                        }
                    }
                }
                pattern[r][c] = h
            }
        }
        return pattern
    }

    /**
     * Lógica de bloqueo 3D en la cuadrícula:
     * Una ficha está bloqueada si:
     * 1. No es la ficha en la cima de su propia pila (hay fichas no recolectadas sobre ella).
     * 2. O si en celdas vecinas hay fichas sin recolectar en capas superiores que la solapan físicamente.
     */
    fun recomputeGridBlockedStates(grid3D: BoardGrid3D): BoardGrid3D {
        val rows = grid3D.rows
        val cols = grid3D.columns

        val newCells = grid3D.cells.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                val lastUncollectedIdx = cell.tileStack.indexOfLast { !it.isCollected }
                val newStack = cell.tileStack.mapIndexed { idx, tile ->
                    if (tile.isCollected) {
                        tile.copy(isBlocked = false)
                    } else if (idx < lastUncollectedIdx) {
                        tile.copy(isBlocked = true)
                    } else {
                        var coveredByNeighbor = false
                        for (dr in -1..1) {
                            for (dc in -1..1) {
                                if (dr == 0 && dc == 0) continue
                                val neighbor = grid3D.getCell(c + dc, r + dr) ?: continue
                                val neighborTop = neighbor.topUncollectedTile ?: continue
                                val isCardinal = (dr == 0 || dc == 0)
                                if (isCardinal && neighborTop.layer > tile.layer) {
                                    coveredByNeighbor = true
                                    break
                                } else if (!isCardinal && neighborTop.layer >= tile.layer + 2) {
                                    coveredByNeighbor = true
                                    break
                                }
                            }
                            if (coveredByNeighbor) break
                        }
                        tile.copy(isBlocked = coveredByNeighbor)
                    }
                }
                cell.copy(tileStack = newStack)
            }
        }
        return grid3D.copy(cells = newCells)
    }
}

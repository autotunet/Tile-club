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
    val timeLimitSeconds: Int = 120
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
     * Calcula la curva de dificultad automática desde el nivel 1 hasta el nivel 1000.
     */
    fun calculateDifficulty(level: Int): LevelDifficulty {
        val clampedLevel = level.coerceIn(1, 1000)

        val (cols, rows) = when {
            clampedLevel <= 20 -> 5 to 6
            clampedLevel <= 100 -> 6 to 7
            clampedLevel <= 300 -> 7 to 8
            clampedLevel <= 600 -> 8 to 8
            else -> 8 to 9
        }

        val maxStack = when {
            clampedLevel <= 10 -> 2
            clampedLevel <= 50 -> 3
            clampedLevel <= 200 -> 4
            clampedLevel <= 500 -> 5
            clampedLevel <= 800 -> 6
            else -> 7
        }

        val numSymbols = when {
            clampedLevel <= 15 -> 4
            clampedLevel <= 50 -> 6
            clampedLevel <= 150 -> 8
            clampedLevel <= 400 -> 10
            clampedLevel <= 750 -> 12
            else -> 14
        }

        // Cantidad de fichas objetivo (siempre múltiplo de 3)
        val rawTiles = when {
            clampedLevel <= 5 -> 18
            clampedLevel <= 15 -> 24 + ((clampedLevel - 5) / 2) * 3
            clampedLevel <= 50 -> 39 + ((clampedLevel - 15) / 3) * 3
            clampedLevel <= 150 -> 60 + ((clampedLevel - 50) / 10) * 3
            clampedLevel <= 400 -> 78 + ((clampedLevel - 150) / 25) * 3
            clampedLevel <= 700 -> 96 + ((clampedLevel - 400) / 35) * 3
            else -> 114 + ((clampedLevel - 700) / 45) * 3
        }.coerceIn(18, 138)

        val targetTiles = (rawTiles / 3) * 3

        val layoutNames = listOf(
            "Pirámide Central",
            "Fortaleza de Torres",
            "Doble Cima Andina",
            "Laberinto de Murallas",
            "Loto Imperial",
            "Cruz Sagrada",
            "Anillos Concéntricos",
            "El Baluarte del Sol"
        )
        val layoutType = layoutNames[clampedLevel % layoutNames.size]

        // Temporizador de nivel progresivo (tiempo límite según dificultad y cantidad de fichas)
        val timeLimit = when {
            clampedLevel <= 5 -> 90 // 1m 30s
            clampedLevel <= 20 -> 120 // 2m 00s
            clampedLevel <= 50 -> 150 // 2m 30s
            clampedLevel <= 150 -> 180 // 3m 00s
            clampedLevel <= 400 -> 210 // 3m 30s
            clampedLevel <= 700 -> 240 // 4m 00s
            else -> 270 // 4m 30s
        }

        return LevelDifficulty(
            levelNumber = clampedLevel,
            gridCols = cols,
            gridRows = rows,
            maxStackHeight = maxStack,
            targetTiles = targetTiles,
            numSymbols = numSymbols,
            layoutType = layoutType,
            timeLimitSeconds = timeLimit
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
            val centerCols = diff.gridCols / 2
            val centerRows = diff.gridRows / 2
            repeat(needed) {
                validSlots.add(centerCols to centerRows)
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
     * Calcula la máscara de altura de las pilas en la cuadrícula según el diseño de dificultad.
     */
    private fun computeHeightPattern(diff: LevelDifficulty, rand: Random): Array<IntArray> {
        val rows = diff.gridRows
        val cols = diff.gridCols
        val maxH = diff.maxStackHeight
        val pattern = Array(rows) { IntArray(cols) { 0 } }

        val centerR = rows / 2.0f
        val centerC = cols / 2.0f

        val layoutIndex = diff.levelNumber % 5

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val distFromCenter = kotlin.math.hypot((r - centerR).toDouble(), (c - centerC).toDouble()).toFloat()
                val maxDist = kotlin.math.hypot(centerR.toDouble(), centerC.toDouble()).toFloat()
                val normDist = (1f - (distFromCenter / maxDist)).coerceIn(0f, 1f)

                val h = when (layoutIndex) {
                    0 -> {
                        // Pirámide central escalonada
                        (normDist * maxH).toInt().coerceIn(0, maxH)
                    }
                    1 -> {
                        // Fortaleza con bastiones perimetrales y núcleo
                        val isPerimeter = (r == 0 || r == rows - 1 || c == 0 || c == cols - 1)
                        if (isPerimeter) (maxH / 2).coerceAtLeast(1)
                        else if (distFromCenter < 1.8f) maxH
                        else 1
                    }
                    2 -> {
                        // Doble cima / Cresta de dragón
                        val distPeak1 = kotlin.math.hypot((r - centerR).toDouble(), (c - (centerC - 1.5f)).toDouble()).toFloat()
                        val distPeak2 = kotlin.math.hypot((r - centerR).toDouble(), (c - (centerC + 1.5f)).toDouble()).toFloat()
                        val peakDist = minOf(distPeak1, distPeak2)
                        (maxH - peakDist).toInt().coerceIn(0, maxH)
                    }
                    3 -> {
                        // Patrón de tablero de ajedrez / Laberinto
                        if ((r + c) % 2 == 0) {
                            (normDist * maxH + 1).toInt().coerceIn(1, maxH)
                        } else {
                            if (normDist > 0.4f) 1 else 0
                        }
                    }
                    else -> {
                        // Cruz sagrada
                        val isCross = (kotlin.math.abs(r - centerR) < 1.2f || kotlin.math.abs(c - centerC) < 1.2f)
                        if (isCross) (normDist * maxH + 1).toInt().coerceIn(1, maxH) else 0
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
     * 2. O si en celdas adyacentes inmediatas hay fichas sin recolectar en capas estrictamente superiores que la solapen.
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
                                if (neighborTop.layer > tile.layer + 1) {
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

package com.example

import com.example.data.model.WorldDestinations
import com.example.domain.GameEngine
import com.example.domain.LevelGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun levelGenerator_progressiveScalingUpTo1000Levels() {
        val testLevels = listOf(1, 10, 50, 100, 250, 500, 750, 1000)

        for (lvl in testLevels) {
            val levelData = LevelGenerator.generateLevel(lvl)
            val allTiles = levelData.allTiles

            assertEquals("Nivel $lvl debe tener fichas múltiplos de 3", 0, allTiles.size % 3)
            assertTrue("Nivel $lvl debe tener al menos 18 fichas", allTiles.size >= 18)

            // Verificar que cada tipo de ficha aparece en múltiplos de 3 (tríos garantizados)
            val counts = allTiles.groupBy { it.typeId }
            counts.forEach { (type, tiles) ->
                assertEquals("Tipo $type en Nivel $lvl debe ser múltiplo de 3", 0, tiles.size % 3)
            }

            // Verificar que la cuadrícula 3D tiene dimensiones válidas
            assertTrue("Columnas > 0", levelData.grid3D.columns > 0)
            assertTrue("Filas > 0", levelData.grid3D.rows > 0)

            // Destino mundial correspondiente
            assertNotNull(levelData.destination)
        }

        // Verificar incremento automático de dificultad
        val diff1 = LevelGenerator.calculateDifficulty(1)
        val diff100 = LevelGenerator.calculateDifficulty(100)
        val diff1000 = LevelGenerator.calculateDifficulty(1000)

        assertTrue(diff100.targetTiles >= diff1.targetTiles)
        assertTrue(diff1000.targetTiles >= diff100.targetTiles)
        assertTrue(diff1000.maxStackHeight >= diff1.maxStackHeight)
        assertTrue(diff1000.numSymbols >= diff1.numSymbols)
    }

    @Test
    fun levelGenerator_worldDestinationsCoverage() {
        assertEquals("Debe haber 20 destinos mundiales", 20, WorldDestinations.DESTINATIONS.size)
        assertEquals(1, WorldDestinations.DESTINATIONS.first().startLevel)
        assertEquals(1000, WorldDestinations.DESTINATIONS.last().endLevel)

        val japan = WorldDestinations.getDestinationForLevel(1)
        assertEquals("Japón", japan.country)

        val france = WorldDestinations.getDestinationForLevel(60)
        assertEquals("Francia", france.country)

        val egypt = WorldDestinations.getDestinationForLevel(120)
        assertEquals("Egipto", egypt.country)
    }

    @Test
    fun gameEngine_undoBoosterRestoresTile() {
        val engine = GameEngine()
        val state = engine.loadLevel(1)

        val unblockedTile = state.grid3D.allTiles.firstOrNull { !it.isBlocked && !it.isCollected }
        if (unblockedTile != null) {
            val (afterClick, trigger) = engine.onTileClicked(unblockedTile.id)
            assertEquals(1, afterClick.trayTiles.size)

            val (afterUndo, ok) = engine.useUndoBooster()
            assertTrue(ok)
            assertEquals(0, afterUndo.trayTiles.size)
        }
    }

    @Test
    fun gameEngine_hintBoosterHighlightsValidPair() {
        val engine = GameEngine()
        engine.loadLevel(1)

        val (afterHint, ok) = engine.useHintBooster()
        assertTrue("Hint must succeed on initial board", ok)
        assertTrue("Hint must highlight at least 1 or 2 matching tiles", afterHint.highlightedTileIds.isNotEmpty())
        assertTrue("Hint highlights <= 2 tiles", afterHint.highlightedTileIds.size <= 2)

        // Highlighted tiles must share the same type
        val highlighted = afterHint.grid3D.allTiles.filter { it.id in afterHint.highlightedTileIds }
        val types = highlighted.map { it.typeId }.distinct()
        assertEquals("Highlighted tiles must be of the same type", 1, types.size)

        // When player clicks a tile, hint clears
        val (afterClick, _) = engine.onTileClicked(highlighted.first().id)
        assertTrue("Clicking clears highlight", afterClick.highlightedTileIds.isEmpty())
    }

    @Test
    fun gameEngine_extraSlotBoosterExpandsCapacity() {
        val engine = GameEngine()
        engine.loadLevel(1)
        val (afterSlot, ok) = engine.useExtraSlotBooster()
        assertTrue(ok)
        assertEquals(8, afterSlot.trayCapacity)
    }
}

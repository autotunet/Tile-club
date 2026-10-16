package com.example.data.model

/**
 * Representa una celda dentro de la cuadrícula 3D (X, Y).
 * Cada celda contiene una pila inmutable de fichas apiladas a lo largo del eje Z (capas / profundidad).
 */
data class GridCell3D(
    val col: Int,
    val row: Int,
    val tileStack: List<GameTile> = emptyList()
) {
    /**
     * Retorna la ficha activa en la cima de la pila (la más alta que aún no ha sido recolectada).
     */
    val topUncollectedTile: GameTile?
        get() = tileStack.lastOrNull { !it.isCollected }

    /**
     * Altura actual de fichas sin recolectar en esta celda.
     */
    val height: Int
        get() = tileStack.count { !it.isCollected }

    /**
     * Retorna si la celda ya no tiene fichas pendientes.
     */
    val isEmpty: Boolean
        get() = height == 0

    /**
     * Retorna una copia de la celda actualizando el estado de recolección de una ficha.
     */
    fun withUpdatedTile(tileId: Int, isCollected: Boolean): GridCell3D {
        val updated = tileStack.map {
            if (it.id == tileId) it.copy(isCollected = isCollected) else it
        }
        return copy(tileStack = updated)
    }
}

/**
 * Cuadrícula tridimensional completa del tablero.
 * Organiza las celdas en un grid inmutable de [columns] x [rows], donde cada celda contiene una pila de fichas.
 */
data class BoardGrid3D(
    val columns: Int,
    val rows: Int,
    val cells: List<List<GridCell3D>> // [row][col]
) {
    /**
     * Obtiene la celda en las coordenadas (col, row). Retorna null si está fuera de rango.
     */
    fun getCell(col: Int, row: Int): GridCell3D? {
        if (row in 0 until rows && col in 0 until columns) {
            return cells[row][col]
        }
        return null
    }

    /**
     * Lista plana de todas las fichas contenidas en todas las pilas de la cuadrícula 3D.
     */
    val allTiles: List<GameTile>
        get() = cells.flatMap { row -> row.flatMap { it.tileStack } }

    /**
     * Fichas que están en la cima de cada celda y que no están cubiertas por fichas superiores.
     */
    val selectableTiles: List<GameTile>
        get() = allTiles.filter { !it.isCollected && !it.isBlocked }

    /**
     * Cantidad total de fichas restantes sin recolectar en toda la cuadrícula.
     */
    val remainingTilesCount: Int
        get() = allTiles.count { !it.isCollected }

    /**
     * Retorna una nueva cuadrícula inmutable actualizando el estado de una ficha.
     */
    fun withUpdatedTile(tileId: Int, isCollected: Boolean): BoardGrid3D {
        val newCells = cells.map { row ->
            row.map { cell ->
                if (cell.tileStack.any { it.id == tileId }) {
                    cell.withUpdatedTile(tileId, isCollected)
                } else {
                    cell
                }
            }
        }
        return copy(cells = newCells)
    }
}

/**
 * Configuración visual de renderizado 3D de las capas.
 */
data class Layer3DRenderSpec(
    val layerElevationPx: Float = 6.0f,
    val shadowOffsetPx: Float = 4.0f,
    val thicknessBevelPx: Float = 3.5f,
    val perspectiveTiltX: Float = 0.0f,
    val perspectiveTiltY: Float = 0.0f
)

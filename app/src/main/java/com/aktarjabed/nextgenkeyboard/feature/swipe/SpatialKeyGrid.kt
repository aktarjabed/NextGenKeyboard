package com.aktarjabed.nextgenkeyboard.feature.swipe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * Spatial grid for O(1) key lookup by coordinates.
 * Divides screen into cells, each cell contains keys that overlap it.
 */
class SpatialKeyGrid(
    screenWidth: Int = 2560,
    screenHeight: Int = 1600,
    cellSize: Int = 100
) {
    private val cellWidth = cellSize
    private val cellHeight = cellSize
    private val gridWidth = (screenWidth + cellSize - 1) / cellSize
    private val gridHeight = (screenHeight + cellSize - 1) / cellSize

    // Grid[row][col] = list of keys in that cell
    private val grid: Array<Array<MutableList<String>>> =
        Array(gridHeight) { Array(gridWidth) { mutableListOf() } }

    private val keyRects = mutableMapOf<String, Rect>()

    /**
     * Register a key in the spatial grid.
     * Key can span multiple cells.
     */
    fun registerKey(key: String, rect: Rect) {
        keyRects[key] = rect

        val startRow = (rect.top / cellHeight).toInt().coerceIn(0, gridHeight - 1)
        val endRow = (rect.bottom / cellHeight).toInt().coerceIn(0, gridHeight - 1)
        val startCol = (rect.left / cellWidth).toInt().coerceIn(0, gridWidth - 1)
        val endCol = (rect.right / cellWidth).toInt().coerceIn(0, gridWidth - 1)

        for (row in startRow..endRow) {
            for (col in startCol..endCol) {
                if (!grid[row][col].contains(key)) {
                    grid[row][col].add(key)
                }
            }
        }
    }

    /**
     * Find key at offset in O(1) time (check only 1-4 cells).
     */
    fun findKeyAt(offset: Offset): String? {
        val row = (offset.y / cellHeight).toInt().coerceIn(0, gridHeight - 1)
        val col = (offset.x / cellWidth).toInt().coerceIn(0, gridWidth - 1)

        return grid[row][col]
            .firstOrNull { key ->
                keyRects[key]?.let {
                    offset.x >= it.left && offset.x <= it.right &&
                    offset.y >= it.top && offset.y <= it.bottom
                } ?: false
            }
    }

    fun clear() {
        for (row in grid) {
            for (cell in row) {
                cell.clear()
            }
        }
        keyRects.clear()
    }
}

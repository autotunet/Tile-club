package com.example.data.model

data class GameTile(
    val id: Int,
    val typeId: Int,
    val layer: Int,
    val col: Float,
    val row: Float,
    val widthUnits: Float = 2.0f,
    val heightUnits: Float = 2.4f,
    val isCollected: Boolean = false,
    val isBlocked: Boolean = false
) {
    fun overlaps(other: GameTile): Boolean {
        if (this.isCollected || other.isCollected) return false
        val horizontalOverlap = kotlin.math.abs(this.col - other.col) < 1.85f
        val verticalOverlap = kotlin.math.abs(this.row - other.row) < 2.2f
        return horizontalOverlap && verticalOverlap
    }
}

enum class BoosterType(val displayName: String, val icon: String, val costCoins: Int) {
    HINT("Pista", "💡", 40),
    UNDO("Deshacer", "↩️", 50),
    SHUFFLE("Mezclar", "🔀", 75),
    MAGIC_WAND("Varita", "🪄", 100),
    EXTRA_SLOT("+1 Hueco", "➕", 120)
}

data class TileTheme(
    val id: String,
    val name: String,
    val symbols: List<String>,
    val colors: List<Long>
)

object TileThemes {
    val FRUITS = TileTheme(
        id = "fruits",
        name = "Frutas Frescas",
        symbols = listOf(
            "🍓", "🍒", "🍋", "🥑", "🍇", "🍉", "🍍", "🍎", "🍊", "🥝",
            "🥥", "🍑", "🍌", "🫐", "🥭", "🍐", "🍈", "🍏", "🌽", "🥕"
        ),
        colors = listOf(
            0xFFFFE4E6, 0xFFFFE4E6, 0xFFFEF3C7, 0xFFDCFCE7, 0xFFF3E8FF,
            0xFFFFE4E6, 0xFFFEF9C3, 0xFFFFE4E6, 0xFFFFEDD5, 0xFFDCFCE7,
            0xFFF5F5F4, 0xFFFFE4E6, 0xFFFEF08A, 0xFFE0E7FF, 0xFFFFEDD5,
            0xFFDCFCE7, 0xFFE0F2FE, 0xFFDCFCE7, 0xFFFEF08A, 0xFFFFEDD5
        )
    )

    val GEMS = TileTheme(
        id = "gems",
        name = "Gemas Mágicas",
        symbols = listOf(
            "💎", "🔮", "💍", "👑", "🪙", "⭐", "💠", "🪨", "🧿", "💫",
            "✨", "🔥", "⚡", "🌟", "📿", "🏆", "🗡️", "🛡️", "🗝️", "🏺"
        ),
        colors = listOf(
            0xFFE0F2FE, 0xFFF3E8FF, 0xFFFEF08A, 0xFFFEF3C7, 0xFFFEF08A,
            0xFFFEF9C3, 0xFFCFFAFE, 0xFFE2E8F0, 0xFFDBEAFE, 0xFFDDD6FE,
            0xFFFEF08A, 0xFFFFEDD5, 0xFFFEF08A, 0xFFFEF9C3, 0xFFE0E7FF,
            0xFFFEF08A, 0xFFE2E8F0, 0xFFCFFAFE, 0xFFFEF3C7, 0xFFFFEDD5
        )
    )

    val ANIMALS = TileTheme(
        id = "animals",
        name = "Animales Felices",
        symbols = listOf(
            "🐼", "🦁", "🦊", "🐯", "🐸", "🐵", "🐨", "🐰", "🐶", "🐱",
            "🐻", "🦉", "🦄", "🦓", "🦒", "🐘", "🦔", "🐧", "🐬", "🦋"
        ),
        colors = listOf(
            0xFFF1F5F9, 0xFFFEF3C7, 0xFFFFEDD5, 0xFFFFEDD5, 0xFFDCFCE7,
            0xFFFEF3C7, 0xFFE2E8F0, 0xFFFFE4E6, 0xFFFEF3C7, 0xFFFEF08A,
            0xFFF5E6D3, 0xFFE2E8F0, 0xFFF3E8FF, 0xFFF1F5F9, 0xFFFEF3C7,
            0xFFE0E7FF, 0xFFFED7AA, 0xFFCFFAFE, 0xFFBAE6FD, 0xFFDDD6FE
        )
    )

    val SWEETS = TileTheme(
        id = "sweets",
        name = "Dulces & Postres",
        symbols = listOf(
            "🍩", "🍦", "🍰", "🍭", "🧁", "🍫", "🍬", "🍪", "🍮", "🥧",
            "🥞", "🍧", "🍨", "🧇", "🥨", "🎂", "🍯", "🍡", "🍥", "🧋"
        ),
        colors = listOf(
            0xFFFFE4E6, 0xFFFEF3C7, 0xFFFFE4E6, 0xFFF3E8FF, 0xFFFFE4E6,
            0xFFEDE9FE, 0xFFFFE4E6, 0xFFFEF3C7, 0xFFFEF08A, 0xFFFFEDD5,
            0xFFFEF08A, 0xFFFFE4E6, 0xFFCFFAFE, 0xFFFEF3C7, 0xFFFFEDD5,
            0xFFFFE4E6, 0xFFFEF08A, 0xFFFFE4E6, 0xFFDCFCE7, 0xFFEDE9FE
        )
    )

    val ALL = listOf(FRUITS, GEMS, ANIMALS, SWEETS)
}

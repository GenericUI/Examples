package com.nexustools.genericui.examples.dungeoncrawler

/**
 * Tile terrain types inside the multi-floor Abyssal Crypt dungeon grid.
 */
enum class DungeonTileType(
    val glyph: String,
    val displayName: String,
    val walkable: Boolean,
    val tileHex: String
) {
    WALL("#", "Stone Wall", false, "#1E293B"),
    FLOOR(".", "Flagstone", true, "#0F172A"),
    SPIKE_TRAP("^", "Spike Trap", true, "#7F1D1D"),
    HEALING_SHRINE("+", "Sun Shrine", true, "#065F46"),
    EXIT_STAIRS(">", "Descent Stairs", true, "#4338CA"),
    TORCH_ALTAR("*", "Torch Brazier", false, "#92400E")
}

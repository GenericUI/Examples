package com.nexustools.genericui.examples.dungeoncrawler

/**
 * Generated layout data for a single Abyssal Crypt floor.
 */
data class GeneratedDungeonFloor(
    val floor: Int,
    val tiles: Array<Array<DungeonTileType>>,
    val heroStartX: Int,
    val heroStartY: Int,
    val actors: MutableList<DungeonActor>
) {
    val height: Int get() = tiles.size
    val width: Int get() = if (tiles.isNotEmpty()) tiles[0].size else 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeneratedDungeonFloor) return false
        return floor == other.floor &&
            heroStartX == other.heroStartX &&
            heroStartY == other.heroStartY &&
            actors == other.actors &&
            tiles.contentDeepEquals(other.tiles)
    }

    override fun hashCode(): Int {
        var result = floor
        result = 31 * result + tiles.contentDeepHashCode()
        result = 31 * result + heroStartX
        result = 31 * result + heroStartY
        result = 31 * result + actors.hashCode()
        return result
    }
}

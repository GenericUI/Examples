package com.nexustools.genericui.examples.dungeoncrawler

/**
 * Deterministic multi-floor dungeon layout generator producing playable 11x9 crypt floors.
 */
object DungeonFloorGenerator {
    const val GRID_WIDTH: Int = 11
    const val GRID_HEIGHT: Int = 9
    const val MAX_FLOORS: Int = 3

    /**
     * Generates the terrain grid, hero starting coordinates, and populated floor actors/items.
     */
    fun generateFloor(floor: Int): GeneratedDungeonFloor {
        val safeFloor = floor.coerceIn(1, MAX_FLOORS)
        val grid = Array(GRID_HEIGHT) { Array(GRID_WIDTH) { DungeonTileType.FLOOR } }

        // Outer crypt perimeter walls
        for (x in 0 until GRID_WIDTH) {
            grid[0][x] = DungeonTileType.WALL
            grid[GRID_HEIGHT - 1][x] = DungeonTileType.WALL
        }
        for (y in 0 until GRID_HEIGHT) {
            grid[y][0] = DungeonTileType.WALL
            grid[y][GRID_WIDTH - 1] = DungeonTileType.WALL
        }

        // Interior pillars & chambers keyed by floor number
        val pillars = when (safeFloor) {
            1 -> listOf(
                3 to 2, 3 to 3, 7 to 2, 7 to 3,
                5 to 4, 3 to 6, 7 to 6
            )
            2 -> listOf(
                2 to 3, 2 to 5, 4 to 2, 4 to 6,
                6 to 2, 6 to 6, 8 to 3, 8 to 5
            )
            else -> listOf(
                2 to 2, 2 to 6, 4 to 4, 6 to 4,
                8 to 2, 8 to 6, 5 to 2
            )
        }
        for ((px, py) in pillars) {
            grid[py][px] = DungeonTileType.WALL
        }

        // Torches, traps, healing shrine, and exit stairs
        grid[2][5] = if (grid[2][5] == DungeonTileType.FLOOR) DungeonTileType.TORCH_ALTAR else grid[2][5]
        grid[6][5] = DungeonTileType.HEALING_SHRINE
        grid[4][3] = DungeonTileType.SPIKE_TRAP
        grid[3][8] = DungeonTileType.SPIKE_TRAP
        if (safeFloor >= 2) {
            grid[5][7] = DungeonTileType.SPIKE_TRAP
        }
        grid[1][9] = DungeonTileType.EXIT_STAIRS

        val actors = mutableListOf<DungeonActor>()
        when (safeFloor) {
            1 -> {
                actors += DungeonActor("goblin-1", "Crypt Goblin", DungeonEntityKind.GOBLIN_STALKER, 4, 2, 9, 9, 3, 0, 25)
                actors += DungeonActor("goblin-2", "Tunnel Lurker", DungeonEntityKind.GOBLIN_STALKER, 6, 5, 10, 10, 4, 0, 30)
                actors += DungeonActor("skel-1", "Bone Warden", DungeonEntityKind.SKELETON_GUARD, 8, 2, 14, 14, 4, 1, 45)
                actors += DungeonActor("chest-1", "Ironbound Chest", DungeonEntityKind.RELIC_CHEST, 2, 6, 1, 1, 0, 0, 80)
                actors += DungeonActor("potion-1", "Crimson Vial", DungeonEntityKind.HEALTH_POTION, 9, 6, 1, 1, 0, 0, 15)
            }
            2 -> {
                actors += DungeonActor("goblin-3", "Vault Stalker", DungeonEntityKind.GOBLIN_STALKER, 3, 4, 12, 12, 4, 1, 35)
                actors += DungeonActor("skel-2", "Crypt Halberdier", DungeonEntityKind.SKELETON_GUARD, 6, 3, 18, 18, 5, 1, 55)
                actors += DungeonActor("skel-3", "Sanctum Sentinel", DungeonEntityKind.SKELETON_GUARD, 8, 4, 18, 18, 5, 2, 60)
                actors += DungeonActor("chest-2", "Gilded Reliquary", DungeonEntityKind.RELIC_CHEST, 1, 2, 1, 1, 0, 0, 110)
                actors += DungeonActor("potion-2", "Emerald Flask", DungeonEntityKind.HEALTH_POTION, 9, 5, 1, 1, 0, 0, 20)
            }
            else -> {
                actors += DungeonActor("skel-4", "Honor Guard", DungeonEntityKind.SKELETON_GUARD, 5, 5, 20, 20, 6, 2, 70)
                actors += DungeonActor("goblin-4", "Ash Raider", DungeonEntityKind.GOBLIN_STALKER, 3, 6, 14, 14, 5, 1, 45)
                actors += DungeonActor("lich-boss", "Malakor the Lich", DungeonEntityKind.LICH_ADEPT, 8, 1, 32, 32, 7, 2, 250)
                actors += DungeonActor("chest-3", "Crown Vault", DungeonEntityKind.RELIC_CHEST, 9, 7, 1, 1, 0, 0, 150)
                actors += DungeonActor("potion-3", "Elixir of Dawn", DungeonEntityKind.HEALTH_POTION, 1, 2, 1, 1, 0, 0, 25)
            }
        }

        return GeneratedDungeonFloor(
            floor = safeFloor,
            tiles = grid,
            heroStartX = 1,
            heroStartY = 7,
            actors = actors
        )
    }
}

package com.nexustools.genericui.examples.dungeoncrawler

/**
 * Classification of actors, monsters, and interactive pickups inhabiting each dungeon floor.
 */
enum class DungeonEntityKind(
    val glyph: String,
    val isHostile: Boolean
) {
    HERO("@", false),
    GOBLIN_STALKER("g", true),
    SKELETON_GUARD("K", true),
    LICH_ADEPT("L", true),
    RELIC_CHEST("$", false),
    HEALTH_POTION("!", false)
}

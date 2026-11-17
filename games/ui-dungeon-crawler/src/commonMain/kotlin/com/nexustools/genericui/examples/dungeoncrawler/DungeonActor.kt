package com.nexustools.genericui.examples.dungeoncrawler

/**
 * Grid entity representing a monster, treasure chest, or potion on the active dungeon floor.
 */
data class DungeonActor(
    val id: String,
    val name: String,
    val kind: DungeonEntityKind,
    var x: Int,
    var y: Int,
    var hp: Int,
    val maxHp: Int,
    val attack: Int,
    val defense: Int,
    val xpReward: Int = 0,
    var consumedOrDefeated: Boolean = false
)

package com.nexustools.genericui.examples.dungeoncrawler

import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile

/**
 * Immutable snapshot of dungeon run progression, hero stats, cursor state, and controller profile.
 */
data class DungeonStateSnapshot(
    val floor: Int,
    val maxFloors: Int,
    val turnCount: Int,
    val score: Int,
    val heroHp: Int,
    val heroMaxHp: Int,
    val heroAtk: Int,
    val heroDef: Int,
    val heroX: Int,
    val heroY: Int,
    val potions: Int,
    val relicsCollected: Int,
    val heroAlive: Boolean,
    val victory: Boolean,
    val paused: Boolean,
    val gyroAimCursorEnabled: Boolean,
    val cursorGridX: Int,
    val cursorGridY: Int,
    val focusMode: DungeonFocusMode,
    val activeProfile: ControllerHardwareProfile
)

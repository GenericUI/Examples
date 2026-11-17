package com.nexustools.genericui.examples.arena3d

import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile

/**
 * Immutable live snapshot of Vanguard Arena 3D combat, aiming, physics, and controller state.
 */
data class ArenaStateSnapshot(
    val wave: Int,
    val score: Int,
    val heroHp: Int,
    val heroMaxHp: Int,
    val enemiesRemaining: Int,
    val shotsFired: Int,
    val hitsLanded: Int,
    val heroX: Float,
    val heroY: Float,
    val heroZ: Float,
    val heroGrounded: Boolean,
    val aimYawDeg: Float,
    val aimPitchDeg: Float,
    val raycastDistanceMeters: Float?,
    val raycastTargetName: String,
    val cameraMode: ArenaCameraMode,
    val gyroAimEnabled: Boolean,
    val paused: Boolean,
    val victory: Boolean,
    val activeProfile: ControllerHardwareProfile
)

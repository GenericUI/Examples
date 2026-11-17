package com.nexustools.genericui.examples.platformer2d

import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile

/**
 * Live telemetry snapshot of Skyline Courier 2D state.
 */
data class PlatformerSnapshot(
    val score: Int,
    val coresCollected: Int,
    val totalCores: Int,
    val playerX: Float,
    val playerY: Float,
    val vx: Float,
    val vy: Float,
    val grounded: Boolean,
    val dashCooldownSeconds: Float,
    val animState: PlatformerAnimationState,
    val facingSign: Float,
    val aimAngleDeg: Float,
    val drawBoneOverlay: Boolean,
    val paused: Boolean,
    val completed: Boolean,
    val activeProfile: ControllerHardwareProfile
)

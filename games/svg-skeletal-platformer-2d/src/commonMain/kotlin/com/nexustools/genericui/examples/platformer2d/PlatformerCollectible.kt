package com.nexustools.genericui.examples.platformer2d

import com.nexustools.genericui.ext.engine.scene.SceneEntity

/**
 * Collectible Quantum Core placed along the Skyline Courier rooftops.
 */
data class PlatformerCollectible(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val points: Int,
    val entity: SceneEntity,
    var collected: Boolean = false
)

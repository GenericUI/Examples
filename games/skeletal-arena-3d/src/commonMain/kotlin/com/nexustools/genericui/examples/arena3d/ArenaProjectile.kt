package com.nexustools.genericui.examples.arena3d

import com.nexustools.genericui.ext.engine.scene.SceneEntity
import com.nexustools.genericui.ext.engine3d.RigidBody3DModule

/**
 * Active 3D plasma bolt projectile driven by Box3D rigid-body physics and Three.js `SphereGeometry`.
 */
data class ArenaProjectile(
    val id: String,
    val entity: SceneEntity,
    val bodyModule: RigidBody3DModule,
    var ttlSeconds: Float,
    val damage: Int = 18
)

package com.nexustools.genericui.examples.arena3d

import com.nexustools.genericui.ext.engine.scene.SceneEntity
import com.nexustools.genericui.ext.engine3d.SkeletalModel3DModule

/**
 * Rigged 3D arena adversary powered by `ProceduralHumanoidRig3D` and Box3D colliders.
 */
data class ArenaEnemyUnit(
    val id: String,
    val name: String,
    val patrolCenterX: Float,
    val patrolCenterZ: Float,
    val patrolRadius: Float,
    var patrolPhase: Float,
    var hp: Int,
    val maxHp: Int,
    val entity: SceneEntity,
    val rigModule: SkeletalModel3DModule,
    var defeated: Boolean = false
)

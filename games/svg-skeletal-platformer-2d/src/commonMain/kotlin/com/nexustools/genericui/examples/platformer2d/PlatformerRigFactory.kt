package com.nexustools.genericui.examples.platformer2d

import com.nexustools.genericui.ext.engine.animation.AnimationWrapMode
import com.nexustools.genericui.ext.engine.animation.EasingFunction
import com.nexustools.genericui.ext.engine.math.Vec2f
import com.nexustools.genericui.ext.engine.svg.Bone2D
import com.nexustools.genericui.ext.engine.svg.BonePoseTrack2D
import com.nexustools.genericui.ext.engine.svg.HumanoidSkeleton2DFactory
import com.nexustools.genericui.ext.engine.svg.SkeletalAnimationClip2D
import com.nexustools.genericui.ext.engine.svg.Skeleton2D
import com.nexustools.genericui.ext.engine.svg.SvgBonePart
import com.nexustools.genericui.ext.engine.svg.SvgCirclePrimitive
import com.nexustools.genericui.ext.engine.svg.SvgPathPrimitive
import com.nexustools.genericui.ext.engine.svg.SvgPolygonPrimitive
import com.nexustools.genericui.ext.engine.svg.SvgRectPrimitive
import kotlin.math.PI
import kotlin.math.sin

/**
 * Factory producing articulated 2D SVG vector skeletal rigs (`Skeleton2D`) with custom SVG paths,
 * polygons, animation clips (`Idle`, `Run`, `Jump`, `Dash`), and Quantum Core crystal rigs.
 */
object PlatformerRigFactory {

    fun createCourierSkeleton(scale: Float = 0.82f): Skeleton2D {
        val base = HumanoidSkeleton2DFactory.create(scale = scale, attachDefaultParts = false)
        val s = scale.coerceAtLeast(0.1f)
        val customParts = listOf(
            // Rear arm (left)
            SvgBonePart("upperArmL", SvgRectPrimitive(0f, -4f * s, 28f * s, 8f * s, 4f * s), "#0284C7", "#0C4A6E", 1.4f, zOrder = 1),
            SvgBonePart("forearmL", SvgRectPrimitive(0f, -3.5f * s, 26f * s, 7f * s, 3.5f * s), "#BAE6FD", "#0369A1", 1.4f, zOrder = 2),
            // Rear leg (left)
            SvgBonePart("thighL", SvgRectPrimitive(0f, -5.5f * s, 32f * s, 11f * s, 5f * s), "#1E293B", "#0F172A", 1.4f, zOrder = 3),
            SvgBonePart("shinL", SvgRectPrimitive(0f, -4.5f * s, 30f * s, 9f * s, 4f * s), "#38BDF8", "#0369A1", 1.4f, zOrder = 4),
            // Pelvis & aerodynamic jetpack fin polygon
            SvgBonePart("pelvis", SvgRectPrimitive(-4f * s, -12f * s, 16f * s, 24f * s, 6f * s), "#0F172A", "#38BDF8", 1.5f, zOrder = 5),
            SvgBonePart(
                "torso",
                SvgPolygonPrimitive(
                    listOf(
                        Vec2f(8f * s, -15f * s),
                        Vec2f(28f * s, -28f * s),
                        Vec2f(36f * s, -15f * s),
                        Vec2f(12f * s, -10f * s)
                    )
                ),
                "#334155",
                "#38BDF8",
                1.4f,
                zOrder = 5
            ),
            // Courier jacket torso + flowing SVG bezier scarf
            SvgBonePart("torso", SvgRectPrimitive(0f, -15f * s, 42f * s, 30f * s, 8f * s), "#0EA5E9", "#0C4A6E", 1.6f, zOrder = 6),
            SvgBonePart(
                "torso",
                SvgPathPrimitive(
                    "M ${36f * s} ${-10f * s} C ${44f * s} ${-34f * s}, ${22f * s} ${-50f * s}, ${10f * s} ${-48f * s} L ${20f * s} ${-36f * s} Z"
                ),
                "#F43F5E",
                "#881337",
                1.4f,
                zOrder = 7
            ),
            // Helmet head & amber SVG visor path
            SvgBonePart("head", SvgCirclePrimitive(12f * s, 0f, 13.5f * s), "#E2E8F0", "#0F172A", 1.6f, zOrder = 8),
            SvgBonePart(
                "head",
                SvgPathPrimitive("M ${10f * s} ${-2f * s} L ${22f * s} ${-2f * s} L ${18f * s} ${11f * s} L ${9f * s} ${8f * s} Z"),
                "#F59E0B",
                "#78350F",
                1.2f,
                zOrder = 9
            ),
            // Front leg (right)
            SvgBonePart("thighR", SvgRectPrimitive(0f, -5.5f * s, 32f * s, 11f * s, 5f * s), "#334155", "#0F172A", 1.4f, zOrder = 10),
            SvgBonePart("shinR", SvgRectPrimitive(0f, -4.5f * s, 30f * s, 9f * s, 4f * s), "#38BDF8", "#0369A1", 1.4f, zOrder = 11),
            // Front aiming arm (right) with cyber-gauntlet
            SvgBonePart("upperArmR", SvgRectPrimitive(0f, -4f * s, 28f * s, 8f * s, 4f * s), "#38BDF8", "#0C4A6E", 1.5f, zOrder = 12),
            SvgBonePart(
                "forearmR",
                SvgPathPrimitive("M 0 ${-4f * s} L ${28f * s} ${-5.5f * s} L ${31f * s} 0 L ${28f * s} ${5.5f * s} L 0 ${4f * s} Z"),
                "#FBBF24",
                "#92400E",
                1.5f,
                zOrder = 13
            )
        )
        for (part in customParts) {
            base.attachPart(part)
        }
        base.updateWorldTransforms()
        return base
    }

    fun createQuantumCoreSkeleton(scale: Float = 1f): Skeleton2D {
        val coreBone = Bone2D("core", 24f * scale, 0f, 0f, 0f)
        val diamond = SvgBonePart(
            boneName = "core",
            primitive = SvgPolygonPrimitive(
                listOf(
                    Vec2f(0f, -15f * scale),
                    Vec2f(13f * scale, 0f),
                    Vec2f(0f, 15f * scale),
                    Vec2f(-13f * scale, 0f)
                )
            ),
            fillHex = "#22D3EE",
            strokeHex = "#FEF08A",
            strokeWidth = 2.0f,
            zOrder = 1
        )
        val innerCore = SvgBonePart(
            boneName = "core",
            primitive = SvgCirclePrimitive(0f, 0f, 5f * scale),
            fillHex = "#FDE047",
            strokeHex = null,
            strokeWidth = 0f,
            zOrder = 2
        )
        return Skeleton2D(
            bones = listOf(coreBone),
            initialParts = listOf(diamond, innerCore)
        )
    }

    fun createIdleClip(durationSeconds: Float = 1.6f): SkeletalAnimationClip2D {
        val halfPi = (PI * 0.5).toFloat()
        val pi = PI.toFloat()
        val twoPi = (PI * 2.0).toFloat()
        val tracks = listOf(
            BonePoseTrack2D.fromLambda("pelvis", EasingFunction.SINE_IN_OUT) { p ->
                -halfPi + sin(p * twoPi) * 0.04f
            },
            BonePoseTrack2D.fromLambda("thighL") { _ -> pi - 0.12f },
            BonePoseTrack2D.fromLambda("shinL") { _ -> 0.12f },
            BonePoseTrack2D.fromLambda("thighR") { _ -> pi + 0.12f },
            BonePoseTrack2D.fromLambda("shinR") { _ -> 0.08f },
            BonePoseTrack2D.fromLambda("upperArmL") { p -> pi + 0.25f + sin(p * twoPi) * 0.08f },
            BonePoseTrack2D.fromLambda("forearmL") { _ -> -0.4f }
        )
        return SkeletalAnimationClip2D("Idle", durationSeconds, AnimationWrapMode.LOOP, tracks)
    }

    fun createRunClip(durationSeconds: Float = 0.55f): SkeletalAnimationClip2D =
        HumanoidSkeleton2DFactory.createWalkClip(durationSeconds)

    fun createJumpClip(durationSeconds: Float = 0.6f): SkeletalAnimationClip2D {
        val halfPi = (PI * 0.5).toFloat()
        val pi = PI.toFloat()
        val tracks = listOf(
            BonePoseTrack2D.fromLambda("pelvis") { _ -> -halfPi + 0.12f },
            BonePoseTrack2D.fromLambda("thighL") { _ -> pi - 0.65f },
            BonePoseTrack2D.fromLambda("shinL") { _ -> 1.05f },
            BonePoseTrack2D.fromLambda("thighR") { _ -> pi + 0.45f },
            BonePoseTrack2D.fromLambda("shinR") { _ -> 0.45f },
            BonePoseTrack2D.fromLambda("upperArmL") { _ -> pi - 1.15f },
            BonePoseTrack2D.fromLambda("forearmL") { _ -> -0.55f }
        )
        return SkeletalAnimationClip2D("Jump", durationSeconds, AnimationWrapMode.LOOP, tracks)
    }

    fun createDashClip(durationSeconds: Float = 0.35f): SkeletalAnimationClip2D {
        val halfPi = (PI * 0.5).toFloat()
        val pi = PI.toFloat()
        val tracks = listOf(
            BonePoseTrack2D.fromLambda("pelvis") { _ -> -halfPi + 0.32f },
            BonePoseTrack2D.fromLambda("thighL") { _ -> pi + 0.7f },
            BonePoseTrack2D.fromLambda("shinL") { _ -> 0.3f },
            BonePoseTrack2D.fromLambda("thighR") { _ -> pi - 0.35f },
            BonePoseTrack2D.fromLambda("shinR") { _ -> 0.85f },
            BonePoseTrack2D.fromLambda("upperArmL") { _ -> pi + 0.9f }
        )
        return SkeletalAnimationClip2D("Dash", durationSeconds, AnimationWrapMode.LOOP, tracks)
    }
}

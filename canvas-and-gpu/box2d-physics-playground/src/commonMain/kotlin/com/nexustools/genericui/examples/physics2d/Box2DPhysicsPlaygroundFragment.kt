package com.nexustools.genericui.examples.physics2d

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.form.RangeSlider
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.media.Canvas2DContext
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.ext.box2d.Body2D
import com.nexustools.genericui.ext.box2d.BodyDef2D
import com.nexustools.genericui.ext.box2d.BodyType2D
import com.nexustools.genericui.ext.box2d.Box2DDebugDrawer
import com.nexustools.genericui.ext.box2d.CircleShape2D
import com.nexustools.genericui.ext.box2d.FixtureDef2D
import com.nexustools.genericui.ext.box2d.PolygonShape2D
import com.nexustools.genericui.ext.box2d.RevoluteJoint2D
import com.nexustools.genericui.ext.box2d.Vec2
import com.nexustools.genericui.ext.box2d.World2D
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtSurface
import com.nexustools.genericui.ext.canvas2d.dsl.canvas2DExt
import com.nexustools.genericui.ext.dsl.badge
import com.nexustools.genericui.ext.dsl.build
import com.nexustools.genericui.ext.dsl.button
import com.nexustools.genericui.ext.dsl.card
import com.nexustools.genericui.ext.dsl.h5
import com.nexustools.genericui.ext.dsl.hstack
import com.nexustools.genericui.ext.dsl.label
import com.nexustools.genericui.ext.dsl.rangeSlider
import com.nexustools.genericui.ext.dsl.vstack
import com.nexustools.genericui.style.RgbaColor
import kotlin.math.roundToInt

/**
 * Interactive 2D rigid-body physics sandbox built with `:extensions:box2d`
 * ([World2D], [Body2D], [PolygonShape2D], [CircleShape2D], [RevoluteJoint2D],
 * [Box2DDebugDrawer]) and rendered onto [Canvas2DExtSurface].
 */
class Box2DPhysicsPlaygroundFragment : Fragment() {

    var world: World2D = World2D(gravity = Vec2(0f, -9.81f))
        private set

    val debugDrawer: Box2DDebugDrawer = Box2DDebugDrawer(
        pixelsPerMeter = 46f,
        offsetX = 280f,
        offsetY = 190f,
        flipY = true
    )

    var activePreset: PhysicsScenePreset = PhysicsScenePreset.PYRAMID_STACK
        private set

    var gravityY: Float = -9.81f
        private set

    var isPaused: Boolean = false
        private set

    var totalStepCount: Int = 0
        private set

    private var spawnSequenceIndex: Int = 0

    lateinit var arenaSurface: Canvas2DExtSurface
    lateinit var bodyCountBadge: Badge
    lateinit var stepCountBadge: Badge
    lateinit var presetBadge: Badge
    lateinit var pauseResumeButton: Button
    lateinit var gravitySlider: RangeSlider
    lateinit var statusLabel: Label

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("physics-shell")
        buildUi()
        loadScenePreset(PhysicsScenePreset.PYRAMID_STACK)
    }

    private fun buildUi() {
        val self = this
        build(clearFirst = true) {
            vstack(classes = "gap-2") {
                card(classes = "toolbar-panel") {
                    hstack(classes = "gap-2 flex-wrap align-items-center") {
                        h5("Box2D Physics Playground", id = "lbl-title", classes = "mb-0 me-2")

                        self.presetBadge = badge(
                            text = "Scene: Pyramid Stack",
                            type = BootstrapType.PRIMARY,
                            pill = true,
                            id = "badge-scene-preset"
                        )
                        self.bodyCountBadge = badge(
                            text = "Bodies: 0",
                            type = BootstrapType.INFO,
                            pill = true,
                            id = "badge-body-count"
                        )
                        self.stepCountBadge = badge(
                            text = "Steps: 0",
                            type = BootstrapType.SECONDARY,
                            pill = true,
                            id = "badge-step-count"
                        )

                        button(
                            text = "Spawn Box",
                            type = BootstrapType.SUCCESS,
                            size = BootstrapSize.SM,
                            id = "btn-spawn-box",
                            onClick = { self.spawnDynamicBox() }
                        )
                        button(
                            text = "Spawn Circle",
                            type = BootstrapType.INFO,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-spawn-circle",
                            onClick = { self.spawnDynamicCircle() }
                        )
                        button(
                            text = "Step World",
                            type = BootstrapType.PRIMARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-step-world",
                            onClick = { self.stepSimulation(steps = 1) }
                        )
                        self.pauseResumeButton = button(
                            text = "Pause",
                            type = BootstrapType.WARNING,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-toggle-pause",
                            onClick = { self.togglePause() }
                        )
                        button(
                            text = "Reset Scene",
                            type = BootstrapType.DANGER,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-reset-scene",
                            onClick = { self.loadScenePreset(self.activePreset) }
                        )
                    }
                }

                hstack(classes = "arena-card gap-3 align-items-start") {
                    vstack(classes = "gap-2") {
                        label("World2D Debug Viewport (560x380 — 46 px/m)", id = "lbl-arena-heading")
                        self.arenaSurface = canvas2DExt(
                            width = 560,
                            height = 380,
                            id = "physics-arena-canvas",
                            classes = "physics-arena-canvas",
                            onRender = { ctx, _, _, _ ->
                                if (!self.isPaused) {
                                    self.stepSimulation(steps = 1, redrawImmediately = false)
                                }
                                self.renderWorldToContext(ctx)
                            }
                        )
                    }

                    vstack(classes = "controls-card gap-2") {
                        label("Scene Presets", id = "lbl-presets-heading")

                        button(
                            text = "Pyramid Stack",
                            type = BootstrapType.PRIMARY,
                            size = BootstrapSize.SM,
                            id = "btn-preset-pyramid",
                            onClick = { self.loadScenePreset(PhysicsScenePreset.PYRAMID_STACK) }
                        )
                        button(
                            text = "Pendulum Cradle",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-preset-pendulum",
                            onClick = { self.loadScenePreset(PhysicsScenePreset.PENDULUM_CRADLE) }
                        )
                        button(
                            text = "Bouncy Cascade",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-preset-bouncy",
                            onClick = { self.loadScenePreset(PhysicsScenePreset.BOUNCY_CASCADE) }
                        )

                        label("Vertical Gravity (-20.0 to 0.0 m/s²)", id = "lbl-gravity")
                        self.gravitySlider = rangeSlider(
                            minValue = self.gravityY.toDouble(),
                            maxValue = 0.0,
                            min = -20.0,
                            max = 0.0,
                            step = 0.5,
                            id = "slider-gravity"
                        ) {
                            onValueChanged = { updated ->
                                self.updateGravity(updated.toFloat())
                            }
                        }
                    }
                }

                self.statusLabel = label(
                    text = "Simulation ready",
                    id = "lbl-status"
                )
            }
        }
    }

    fun loadScenePreset(preset: PhysicsScenePreset) {
        activePreset = preset
        totalStepCount = 0
        spawnSequenceIndex = 0
        world = World2D(gravity = Vec2(0f, gravityY), allowSleeping = true)

        // Build static floor & side boundary walls
        createStaticBox(x = 0f, y = -3.2f, halfWidth = 5.4f, halfHeight = 0.35f, friction = 0.6f)
        createStaticBox(x = -5.2f, y = 0.0f, halfWidth = 0.25f, halfHeight = 3.2f, friction = 0.4f)
        createStaticBox(x = 5.2f, y = 0.0f, halfWidth = 0.25f, halfHeight = 3.2f, friction = 0.4f)

        when (preset) {
            PhysicsScenePreset.PYRAMID_STACK -> {
                // 3-tier pyramid of 6 dynamic crates
                val boxHalf = 0.38f
                val baseY = -2.4f
                for (row in 0 until 3) {
                    val count = 3 - row
                    val startX = -(count - 1) * (boxHalf * 1.15f)
                    val y = baseY + row * (boxHalf * 2.08f)
                    for (col in 0 until count) {
                        val x = startX + col * (boxHalf * 2.3f)
                        createDynamicBox(x = x, y = y, halfWidth = boxHalf, halfHeight = boxHalf, restitution = 0.15f)
                    }
                }
            }

            PhysicsScenePreset.PENDULUM_CRADLE -> {
                val pivotBody = createStaticBox(x = 0f, y = 2.6f, halfWidth = 0.3f, halfHeight = 0.15f, friction = 0.2f)
                val bobBody = createDynamicCircle(x = 1.6f, y = 1.4f, radius = 0.42f, restitution = 0.8f)
                world.addJoint(
                    RevoluteJoint2D(
                        bodyA = pivotBody,
                        bodyB = bobBody,
                        localAnchorA = Vec2(0f, 0f),
                        localAnchorB = Vec2(-1.6f, 1.2f)
                    )
                )
                createDynamicBox(x = -0.6f, y = -2.3f, halfWidth = 0.4f, halfHeight = 0.4f, restitution = 0.3f)
                createDynamicBox(x = -1.5f, y = -2.3f, halfWidth = 0.4f, halfHeight = 0.4f, restitution = 0.3f)
            }

            PhysicsScenePreset.BOUNCY_CASCADE -> {
                val rampBody = world.createBody(
                    BodyDef2D(
                        type = BodyType2D.STATIC,
                        position = Vec2(-1.2f, 0.2f),
                        angle = -0.25f
                    )
                )
                rampBody.createFixture(
                    FixtureDef2D(
                        shape = PolygonShape2D().setAsBox(2.2f, 0.18f),
                        density = 0f,
                        friction = 0.2f,
                        restitution = 0.6f
                    )
                )
                for (i in 0 until 4) {
                    createDynamicCircle(
                        x = -2.2f + i * 0.75f,
                        y = 1.8f + i * 0.45f,
                        radius = 0.34f,
                        restitution = 0.78f
                    )
                }
            }
        }

        syncTelemetryLabels("Loaded preset: ${preset.label}")
        if (::arenaSurface.isInitialized) {
            arenaSurface.requestRender()
        }
    }

    fun spawnDynamicBox(
        x: Float = ((spawnSequenceIndex % 5) - 2) * 0.65f,
        y: Float = 2.4f
    ): Body2D {
        spawnSequenceIndex++
        val body = createDynamicBox(x = x, y = y, halfWidth = 0.38f, halfHeight = 0.38f, restitution = 0.25f)
        syncTelemetryLabels("Spawned dynamic box at (${round1(x)}, ${round1(y)})")
        if (::arenaSurface.isInitialized) {
            arenaSurface.requestRender()
        }
        return body
    }

    fun spawnDynamicCircle(
        x: Float = ((spawnSequenceIndex % 5) - 2) * 0.60f,
        y: Float = 2.6f,
        radius: Float = 0.36f
    ): Body2D {
        spawnSequenceIndex++
        val body = createDynamicCircle(x = x, y = y, radius = radius, restitution = 0.65f)
        syncTelemetryLabels("Spawned dynamic circle at (${round1(x)}, ${round1(y)})")
        if (::arenaSurface.isInitialized) {
            arenaSurface.requestRender()
        }
        return body
    }

    fun stepSimulation(steps: Int = 1, timeStepSeconds: Float = 1f / 60f, redrawImmediately: Boolean = true) {
        val count = steps.coerceAtLeast(1)
        repeat(count) {
            world.step(timeStepSeconds = timeStepSeconds, velocityIterations = 8, positionIterations = 3)
            totalStepCount++
        }
        syncTelemetryLabels("Stepped simulation ($totalStepCount total steps)")
        if (redrawImmediately && ::arenaSurface.isInitialized) {
            arenaSurface.requestRender()
        }
    }

    fun togglePause() {
        isPaused = !isPaused
        if (::pauseResumeButton.isInitialized) {
            pauseResumeButton.text = if (isPaused) "Resume" else "Pause"
        }
        syncTelemetryLabels(if (isPaused) "Simulation paused" else "Simulation running")
    }

    fun updateGravity(newGravityY: Float) {
        gravityY = newGravityY.coerceIn(-25f, 5f)
        world.gravity.set(0f, gravityY)
        for (body in world.bodies) {
            if (body.type == BodyType2D.DYNAMIC) {
                body.isAwake = true
            }
        }
        syncTelemetryLabels("Gravity updated to ${round1(gravityY)} m/s²")
    }

    fun dynamicBodies(): List<Body2D> =
        world.bodies.filter { it.type == BodyType2D.DYNAMIC }

    fun renderWorldToContext(ctx: Canvas2DContext) {
        ctx.setFillColor(RgbaColor(12, 17, 29, 255))
        ctx.fillRect(0f, 0f, ctx.width.toFloat(), ctx.height.toFloat())
        debugDrawer.drawWorld(world, ctx)
    }

    private fun createStaticBox(
        x: Float,
        y: Float,
        halfWidth: Float,
        halfHeight: Float,
        friction: Float
    ): Body2D {
        val body = world.createBody(
            BodyDef2D(
                type = BodyType2D.STATIC,
                position = Vec2(x, y)
            )
        )
        body.createFixture(
            FixtureDef2D(
                shape = PolygonShape2D().setAsBox(halfWidth, halfHeight),
                density = 0f,
                friction = friction,
                restitution = 0.1f
            )
        )
        return body
    }

    private fun createDynamicBox(
        x: Float,
        y: Float,
        halfWidth: Float,
        halfHeight: Float,
        restitution: Float
    ): Body2D {
        val body = world.createBody(
            BodyDef2D(
                type = BodyType2D.DYNAMIC,
                position = Vec2(x, y)
            )
        )
        body.createFixture(
            FixtureDef2D(
                shape = PolygonShape2D().setAsBox(halfWidth, halfHeight),
                density = 1.0f,
                friction = 0.35f,
                restitution = restitution
            )
        )
        return body
    }

    private fun createDynamicCircle(
        x: Float,
        y: Float,
        radius: Float,
        restitution: Float
    ): Body2D {
        val body = world.createBody(
            BodyDef2D(
                type = BodyType2D.DYNAMIC,
                position = Vec2(x, y)
            )
        )
        body.createFixture(
            FixtureDef2D(
                shape = CircleShape2D(radius = radius),
                density = 1.0f,
                friction = 0.25f,
                restitution = restitution
            )
        )
        return body
    }

    private fun syncTelemetryLabels(statusMessage: String) {
        if (!::presetBadge.isInitialized) return
        presetBadge.text = "Scene: ${activePreset.label}"
        bodyCountBadge.text = "Bodies: ${world.bodies.size}"
        stepCountBadge.text = "Steps: $totalStepCount"
        statusLabel.text = statusMessage
    }

    private fun round1(v: Float): Float = ((v * 10f).roundToInt() / 10f)
}

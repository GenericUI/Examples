package com.nexustools.genericui.examples.physics2d

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.form.RangeSlider
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.media.Canvas2D
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.css.CssCursor
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
import com.nexustools.genericui.ext.canvas2d.dsl.canvas2D
import com.nexustools.genericui.ext.dsl.badge
import com.nexustools.genericui.ext.dsl.build
import com.nexustools.genericui.ext.dsl.button
import com.nexustools.genericui.ext.dsl.card
import com.nexustools.genericui.ext.dsl.column
import com.nexustools.genericui.ext.dsl.columnLayout
import com.nexustools.genericui.ext.dsl.dataTable
import com.nexustools.genericui.ext.dsl.h5
import com.nexustools.genericui.ext.dsl.hstack
import com.nexustools.genericui.ext.dsl.label
import com.nexustools.genericui.ext.dsl.rangeSlider
import com.nexustools.genericui.ext.dsl.switch
import com.nexustools.genericui.ext.dsl.vstack
import com.nexustools.genericui.ext.imageprocessing.context2d.ImageBuffer2DContext
import com.nexustools.genericui.style.color.RgbaColor
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Interactive 2D rigid-body physics sandbox built with `:extensions:box2d`
 * ([World2D], [Body2D], [PolygonShape2D], [CircleShape2D], [RevoluteJoint2D],
 * [Box2DDebugDrawer]) and rendered onto [Canvas2D].
 */
@MainAppFragment
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

    lateinit var arenaSurface: Canvas2D
    lateinit var bodyCountBadge: Badge
    lateinit var stepCountBadge: Badge
    lateinit var presetBadge: Badge
    lateinit var dynamicSummaryBadge: Badge
    lateinit var pauseResumeButton: Button
    lateinit var pyramidPresetButton: Button
    lateinit var pendulumPresetButton: Button
    lateinit var bouncyPresetButton: Button
    lateinit var gravitySlider: RangeSlider
    lateinit var gravityValueLabel: Label
    lateinit var jointSummaryLabel: Label
    lateinit var statusLabel: Label
    lateinit var bodyTable: Table

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

                columnLayout(gutter = SpacingScale.S3, classes = "arena-workspace") {
                    column(lg = 8, md = 7, span = 12, classes = "gap-2") {
                        card(classes = "arena-card") {
                            hstack(classes = "justify-content-between align-items-center gap-2") {
                                label("World2D Debug Viewport (560x380 — 46 px/m)", id = "lbl-arena-heading")
                                badge(
                                    text = "Solver: 60 Hz (8v / 3p)",
                                    type = BootstrapType.SECONDARY,
                                    pill = true,
                                    id = "badge-solver-spec"
                                )
                            }
                            self.arenaSurface = canvas2D(
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
                            ) {
                                getStyle().apply {
                                    borderRadiusPx = 8f
                                    setCursor(CssCursor.CROSSHAIR)
                                }
                            }
                        }

                        card(classes = "arena-card") {
                            hstack(classes = "justify-content-between align-items-center gap-2") {
                                label("Active World2D Rigid Bodies & Fixtures", id = "lbl-body-table-heading")
                                self.dynamicSummaryBadge = badge(
                                    text = "Dynamic: 6 | Static: 3",
                                    type = BootstrapType.INFO,
                                    pill = true,
                                    id = "badge-dynamic-summary"
                                )
                            }
                            self.bodyTable = dataTable(id = "tbl-rigid-bodies") {
                                setColumns(
                                    listOf(
                                        TableColumn("body", "Body"),
                                        TableColumn("type", "Type"),
                                        TableColumn("shape", "Fixture Shape"),
                                        TableColumn("pos", "Position (m)"),
                                        TableColumn("vel", "Velocity (m/s)"),
                                        TableColumn("state", "State")
                                    )
                                )
                                setColumnWeights(1.6f, 0.9f, 1.2f, 1.1f, 1.1f, 0.8f)
                            }
                        }
                    }

                    column(lg = 4, md = 5, span = 12, classes = "gap-2") {
                        card(classes = "controls-card") {
                            label("Scene Presets", id = "lbl-presets-heading")

                            self.pyramidPresetButton = button(
                                text = "Pyramid Stack",
                                type = BootstrapType.PRIMARY,
                                size = BootstrapSize.SM,
                                id = "btn-preset-pyramid",
                                onClick = { self.loadScenePreset(PhysicsScenePreset.PYRAMID_STACK) }
                            )
                            self.pendulumPresetButton = button(
                                text = "Pendulum Cradle",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-preset-pendulum",
                                onClick = { self.loadScenePreset(PhysicsScenePreset.PENDULUM_CRADLE) }
                            )
                            self.bouncyPresetButton = button(
                                text = "Bouncy Cascade",
                                type = BootstrapType.SECONDARY,
                                size = BootstrapSize.SM,
                                outline = true,
                                id = "btn-preset-bouncy",
                                onClick = { self.loadScenePreset(PhysicsScenePreset.BOUNCY_CASCADE) }
                            )
                        }

                        card(classes = "controls-card") {
                            label("Vertical Gravity (-20.0 to 0.0 m/s^2)", id = "lbl-gravity")
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
                            self.gravityValueLabel = label(
                                text = "Current Gravity: -9.8 m/s^2 (Earth)",
                                id = "lbl-gravity-value"
                            )
                            hstack(classes = "gap-2 flex-wrap") {
                                button(
                                    text = "Earth (-9.8)",
                                    type = BootstrapType.INFO,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-gravity-earth",
                                    onClick = { self.applyGravityPreset(-9.81f) }
                                )
                                button(
                                    text = "Moon (-1.6)",
                                    type = BootstrapType.INFO,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-gravity-moon",
                                    onClick = { self.applyGravityPreset(-1.62f) }
                                )
                                button(
                                    text = "Zero-G (0.0)",
                                    type = BootstrapType.INFO,
                                    size = BootstrapSize.SM,
                                    outline = true,
                                    id = "btn-gravity-zero",
                                    onClick = { self.applyGravityPreset(0.0f) }
                                )
                            }
                        }

                        card(classes = "controls-card") {
                            label("Box2DDebugDrawer Layers", id = "lbl-drawer-layers-heading")
                            switch(
                                label = "Draw Fixture Shapes",
                                checked = self.debugDrawer.drawShapes,
                                id = "sw-draw-shapes",
                                onCheckedChanged = {
                                    self.debugDrawer.drawShapes = it
                                    self.requestArenaRender()
                                }
                            )
                            switch(
                                label = "Draw Revolute Joints",
                                checked = self.debugDrawer.drawJoints,
                                id = "sw-draw-joints",
                                onCheckedChanged = {
                                    self.debugDrawer.drawJoints = it
                                    self.requestArenaRender()
                                }
                            )
                            switch(
                                label = "Draw AABB Bounds",
                                checked = self.debugDrawer.drawAABBs,
                                id = "sw-draw-aabbs",
                                onCheckedChanged = {
                                    self.debugDrawer.drawAABBs = it
                                    self.requestArenaRender()
                                }
                            )
                            switch(
                                label = "Draw Contact Manifold Points",
                                checked = self.debugDrawer.drawContacts,
                                id = "sw-draw-contacts",
                                onCheckedChanged = {
                                    self.debugDrawer.drawContacts = it
                                    self.requestArenaRender()
                                }
                            )
                        }

                        card(classes = "controls-card") {
                            label("Simulation Status & Diagnostics", id = "lbl-diagnostics-heading")
                            self.jointSummaryLabel = label(
                                text = "Revolute Joints: 0 | Contacts: 0",
                                id = "lbl-joint-summary"
                            )
                            self.statusLabel = label(
                                text = "Simulation ready",
                                id = "lbl-status"
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadScenePreset(preset: PhysicsScenePreset) {
        activePreset = preset
        totalStepCount = 0
        spawnSequenceIndex = 0
        world = World2D(gravity = Vec2(0f, gravityY), allowSleeping = true)

        // Build static floor & side boundary walls
        createStaticBox(x = 0f, y = -3.2f, halfWidth = 5.4f, halfHeight = 0.35f, friction = 0.6f, tag = "Floor Arena Base")
        createStaticBox(x = -5.2f, y = 0.0f, halfWidth = 0.25f, halfHeight = 3.2f, friction = 0.4f, tag = "Left Arena Wall")
        createStaticBox(x = 5.2f, y = 0.0f, halfWidth = 0.25f, halfHeight = 3.2f, friction = 0.4f, tag = "Right Arena Wall")

        when (preset) {
            PhysicsScenePreset.PYRAMID_STACK -> {
                // 3-tier pyramid of 6 dynamic crates
                val boxHalf = 0.38f
                val baseY = -2.4f
                var crateIdx = 1
                for (row in 0 until 3) {
                    val count = 3 - row
                    val startX = -(count - 1) * (boxHalf * 1.15f)
                    val y = baseY + row * (boxHalf * 2.08f)
                    for (col in 0 until count) {
                        val x = startX + col * (boxHalf * 2.3f)
                        createDynamicBox(
                            x = x,
                            y = y,
                            halfWidth = boxHalf,
                            halfHeight = boxHalf,
                            restitution = 0.15f,
                            tag = "Crate #$crateIdx (Tier ${row + 1})"
                        )
                        crateIdx++
                    }
                }
            }

            PhysicsScenePreset.PENDULUM_CRADLE -> {
                val pivotBody = createStaticBox(x = 0f, y = 2.6f, halfWidth = 0.3f, halfHeight = 0.15f, friction = 0.2f, tag = "Pendulum Pivot")
                val bobBody = createDynamicCircle(x = 1.6f, y = 1.4f, radius = 0.42f, restitution = 0.8f, tag = "Pendulum Bob")
                world.addJoint(
                    RevoluteJoint2D(
                        bodyA = pivotBody,
                        bodyB = bobBody,
                        localAnchorA = Vec2(0f, 0f),
                        localAnchorB = Vec2(-1.6f, 1.2f)
                    )
                )
                createDynamicBox(x = -0.6f, y = -2.3f, halfWidth = 0.4f, halfHeight = 0.4f, restitution = 0.3f, tag = "Target Crate #1")
                createDynamicBox(x = -1.5f, y = -2.3f, halfWidth = 0.4f, halfHeight = 0.4f, restitution = 0.3f, tag = "Target Crate #2")
            }

            PhysicsScenePreset.BOUNCY_CASCADE -> {
                val rampBody = world.createBody(
                    BodyDef2D(
                        type = BodyType2D.STATIC,
                        position = Vec2(-1.2f, 0.2f),
                        angle = -0.25f,
                        userData = "Inclined Ramp"
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
                        restitution = 0.78f,
                        tag = "Cascade Sphere #${i + 1}"
                    )
                }
            }
        }

        syncPresetButtons()
        syncTelemetryLabels("Loaded preset: ${preset.label}")
        requestArenaRender()
    }

    fun spawnDynamicBox(
        x: Float = ((spawnSequenceIndex % 5) - 2) * 0.65f,
        y: Float = 2.4f
    ): Body2D {
        spawnSequenceIndex++
        val body = createDynamicBox(
            x = x,
            y = y,
            halfWidth = 0.38f,
            halfHeight = 0.38f,
            restitution = 0.25f,
            tag = "Spawned Box #$spawnSequenceIndex"
        )
        syncTelemetryLabels("Spawned dynamic box at (${round1(x)}, ${round1(y)})")
        requestArenaRender()
        return body
    }

    fun spawnDynamicCircle(
        x: Float = ((spawnSequenceIndex % 5) - 2) * 0.60f,
        y: Float = 2.6f,
        radius: Float = 0.36f
    ): Body2D {
        spawnSequenceIndex++
        val body = createDynamicCircle(
            x = x,
            y = y,
            radius = radius,
            restitution = 0.65f,
            tag = "Spawned Circle #$spawnSequenceIndex"
        )
        syncTelemetryLabels("Spawned dynamic circle at (${round1(x)}, ${round1(y)})")
        requestArenaRender()
        return body
    }

    fun stepSimulation(steps: Int = 1, timeStepSeconds: Float = 1f / 60f, redrawImmediately: Boolean = true) {
        val count = steps.coerceAtLeast(1)
        repeat(count) {
            world.step(timeStepSeconds = timeStepSeconds, velocityIterations = 8, positionIterations = 3)
            totalStepCount++
        }
        syncTelemetryLabels("Stepped simulation ($totalStepCount total steps)")
        if (redrawImmediately) {
            requestArenaRender()
        }
    }

    fun togglePause() {
        isPaused = !isPaused
        if (::pauseResumeButton.isInitialized) {
            pauseResumeButton.text = if (isPaused) "Resume" else "Pause"
            pauseResumeButton.getBootstrap().apply {
                type = if (isPaused) BootstrapType.SUCCESS else BootstrapType.WARNING
                outline = !isPaused
            }
        }
        syncTelemetryLabels(if (isPaused) "Simulation paused" else "Simulation running")
    }

    fun applyGravityPreset(newGravityY: Float) {
        if (::gravitySlider.isInitialized) {
            gravitySlider.value = newGravityY.toDouble()
        }
        updateGravity(newGravityY)
    }

    fun updateGravity(newGravityY: Float) {
        gravityY = newGravityY.coerceIn(-25f, 5f)
        world.gravity.set(0f, gravityY)
        for (body in world.bodies) {
            if (body.type == BodyType2D.DYNAMIC) {
                body.isAwake = true
            }
        }
        if (::gravityValueLabel.isInitialized) {
            val tag = when {
                abs(gravityY - (-9.81f)) < 0.3f -> " (Earth)"
                abs(gravityY - (-1.62f)) < 0.3f -> " (Moon)"
                abs(gravityY) < 0.1f -> " (Zero-G)"
                else -> ""
            }
            gravityValueLabel.text = "Current Gravity: ${round1(gravityY)} m/s^2$tag"
        }
        syncTelemetryLabels("Gravity updated to ${round1(gravityY)} m/s^2")
    }

    fun dynamicBodies(): List<Body2D> =
        world.bodies.filter { it.type == BodyType2D.DYNAMIC }

    fun renderWorldToContext(ctx: ImageBuffer2DContext) {
        ctx.setFillColor(RgbaColor(12, 17, 29, 255))
        ctx.fillRect(0f, 0f, ctx.width.toFloat(), ctx.height.toFloat())
        debugDrawer.drawWorld(world, ctx)
    }

    private fun requestArenaRender() {
        if (::arenaSurface.isInitialized) {
            arenaSurface.requestRender()
        }
    }

    private fun createStaticBox(
        x: Float,
        y: Float,
        halfWidth: Float,
        halfHeight: Float,
        friction: Float,
        tag: String = "Static Box"
    ): Body2D {
        val body = world.createBody(
            BodyDef2D(
                type = BodyType2D.STATIC,
                position = Vec2(x, y),
                userData = tag
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
        restitution: Float,
        tag: String = "Dynamic Box"
    ): Body2D {
        val body = world.createBody(
            BodyDef2D(
                type = BodyType2D.DYNAMIC,
                position = Vec2(x, y),
                userData = tag
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
        restitution: Float,
        tag: String = "Dynamic Circle"
    ): Body2D {
        val body = world.createBody(
            BodyDef2D(
                type = BodyType2D.DYNAMIC,
                position = Vec2(x, y),
                userData = tag
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

    private fun syncPresetButtons() {
        if (!::pyramidPresetButton.isInitialized) return
        val presets = listOf(
            PhysicsScenePreset.PYRAMID_STACK to pyramidPresetButton,
            PhysicsScenePreset.PENDULUM_CRADLE to pendulumPresetButton,
            PhysicsScenePreset.BOUNCY_CASCADE to bouncyPresetButton
        )
        for ((preset, btn) in presets) {
            val active = preset == activePreset
            btn.getBootstrap().apply {
                type = if (active) BootstrapType.PRIMARY else BootstrapType.SECONDARY
                outline = !active
            }
        }
    }

    private fun syncTelemetryLabels(statusMessage: String) {
        if (!::presetBadge.isInitialized) return
        presetBadge.text = "Scene: ${activePreset.label}"
        presetBadge.getBootstrap().type = when (activePreset) {
            PhysicsScenePreset.PYRAMID_STACK -> BootstrapType.PRIMARY
            PhysicsScenePreset.PENDULUM_CRADLE -> BootstrapType.INFO
            PhysicsScenePreset.BOUNCY_CASCADE -> BootstrapType.SUCCESS
        }
        val totalBodies = world.bodies.size
        val dynCount = dynamicBodies().size
        val staticCount = totalBodies - dynCount
        bodyCountBadge.text = "Bodies: $totalBodies"
        stepCountBadge.text = "Steps: $totalStepCount"
        if (::dynamicSummaryBadge.isInitialized) {
            dynamicSummaryBadge.text = "Dynamic: $dynCount | Static: $staticCount"
        }
        if (::jointSummaryLabel.isInitialized) {
            jointSummaryLabel.text = "Revolute Joints: ${world.joints.size} | Contacts: ${world.contacts.size}"
        }
        statusLabel.text = statusMessage
        syncBodyTable()
    }

    private fun syncBodyTable() {
        if (!::bodyTable.isInitialized) return
        val rows = world.bodies.mapIndexed { idx, body ->
            val name = (body.userData as? String) ?: "Body #${idx + 1}"
            val fixture = body.fixtures.firstOrNull()
            val shapeDesc = when (val shape = fixture?.shape) {
                is CircleShape2D -> "Circle r=${round1(shape.radius)}m"
                is PolygonShape2D -> "Polygon (${shape.vertices.size}v)"
                else -> "Custom"
            }
            val posDesc = "(${round1(body.position.x)}, ${round1(body.position.y)})"
            val velDesc = "(${round1(body.linearVelocity.x)}, ${round1(body.linearVelocity.y)})"
            val stateDesc = when {
                body.type == BodyType2D.STATIC -> "Static"
                body.isAwake -> "Awake"
                else -> "Sleeping"
            }
            TableRow(
                id = "body-row-$idx",
                cells = listOf(
                    TableCell(name),
                    TableCell(body.type.name),
                    TableCell(shapeDesc),
                    TableCell(posDesc),
                    TableCell(velDesc),
                    TableCell(stateDesc)
                )
            )
        }
        bodyTable.setRows(rows)
    }

    private fun round1(v: Float): Float = ((v * 10f).roundToInt() / 10f)
}

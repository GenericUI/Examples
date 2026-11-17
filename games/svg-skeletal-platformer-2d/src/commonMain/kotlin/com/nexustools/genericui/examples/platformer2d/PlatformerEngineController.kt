package com.nexustools.genericui.examples.platformer2d

import com.nexustools.genericui.component.media.Canvas2D
import com.nexustools.genericui.ext.box2d.BodyType2D
import com.nexustools.genericui.ext.box2d.Vec2
import com.nexustools.genericui.ext.engine.math.Vec2f
import com.nexustools.genericui.ext.engine.scene.EntityBehavior
import com.nexustools.genericui.ext.engine.scene.GameScene
import com.nexustools.genericui.ext.engine.scene.SceneEntity
import com.nexustools.genericui.ext.engine.svg.TwoBoneIkSolver2D
import com.nexustools.genericui.ext.engine.threading.GameLoopScheduler
import com.nexustools.genericui.ext.engine2d.Camera2DModule
import com.nexustools.genericui.ext.engine2d.Collider2DModule
import com.nexustools.genericui.ext.engine2d.Engine2DSceneRenderer
import com.nexustools.genericui.ext.engine2d.Game2DCanvasHost
import com.nexustools.genericui.ext.engine2d.ParticleEmitter2DModule
import com.nexustools.genericui.ext.engine2d.PhysicsWorld2DSubsystem
import com.nexustools.genericui.ext.engine2d.RigidBody2DModule
import com.nexustools.genericui.ext.engine2d.SvgCharacter2DModule
import com.nexustools.genericui.ext.engine2d.Tilemap2DModule
import com.nexustools.genericui.ext.gamepad.AdaptiveTriggerEffect
import com.nexustools.genericui.ext.gamepad.AdaptiveTriggerSide
import com.nexustools.genericui.ext.gamepad.ControllerFamily
import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile
import com.nexustools.genericui.ext.gamepad.GamepadAxis2D
import com.nexustools.genericui.ext.gamepad.GamepadButton
import com.nexustools.genericui.ext.gamepad.GamepadFrameSnapshot
import com.nexustools.genericui.ext.gamepad.GamepadManager
import com.nexustools.genericui.ext.gamepad.GamepadRumbleRequest
import com.nexustools.genericui.ext.gamepad.PlayStationControllerFeatures
import com.nexustools.genericui.ext.gamepad.SixAxisMotionState
import com.nexustools.genericui.ext.gamepad.SwitchControllerFeatures
import com.nexustools.genericui.ext.gamepad.SwitchHdRumbleCommand
import com.nexustools.genericui.ext.gamepad.SwitchHomeLedPattern
import com.nexustools.genericui.ext.gamepad.XboxControllerFeatures
import com.nexustools.genericui.ext.gamepad.XboxImpulseRumbleCommand
import com.nexustools.genericui.style.color.RgbaColor
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 2D side-scrolling platformer game controller orchestrating [Game2DCanvasHost], Box2D physics
 * (`PhysicsWorld2DSubsystem`, [RigidBody2DModule], [Collider2DModule]), Canvas2D parallax skyline
 * layers ([Tilemap2DModule]), smooth follow camera ([Camera2DModule]), articulated SVG skeletal rig
 * ([SvgCharacter2DModule] + [TwoBoneIkSolver2D]), particle bursts, and multi-family gamepad haptics.
 */
class PlatformerEngineController(
    val canvasWidth: Int = 880,
    val canvasHeight: Int = 495
) {
    val canvas: Canvas2D = Canvas2D(width = canvasWidth, height = canvasHeight).apply {
        id = "canvas-platformer-stage"
        addClass("platformer-stage-canvas")
    }

    val scene: GameScene = GameScene("SkylineCourier2DScene")
    val host: Game2DCanvasHost = Game2DCanvasHost(
        scene = scene,
        canvas = canvas,
        targetTicksPerSecond = 60,
        renderer = Engine2DSceneRenderer(clearColor = RgbaColor(7, 11, 20, 255))
    )

    val gamepadManager: GamepadManager
        get() = scene.gamepadManager

    val scheduler: GameLoopScheduler
        get() = host.scheduler

    val physicsSubsystem: PhysicsWorld2DSubsystem = scene.registerSubsystem(
        PhysicsWorld2DSubsystem(gravity = Vec2(0f, 920f), allowSleeping = false)
    )

    lateinit var cameraEntity: SceneEntity private set
    lateinit var cameraModule: Camera2DModule private set
    lateinit var parallaxFarEntity: SceneEntity private set
    lateinit var parallaxMidEntity: SceneEntity private set
    lateinit var playerEntity: SceneEntity private set
    lateinit var playerBodyModule: RigidBody2DModule private set
    lateinit var playerCharacterModule: SvgCharacter2DModule private set
    lateinit var playerParticlesModule: ParticleEmitter2DModule private set

    private val idleClip = PlatformerRigFactory.createIdleClip()
    private val runClip = PlatformerRigFactory.createRunClip()
    private val jumpClip = PlatformerRigFactory.createJumpClip()
    private val dashClip = PlatformerRigFactory.createDashClip()

    private val _collectibles = mutableListOf<PlatformerCollectible>()
    val collectibles: List<PlatformerCollectible> get() = _collectibles

    private data class PlatformSpec(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )

    private val platforms = listOf(
        PlatformSpec(320f, 445f, 640f, 48f),
        PlatformSpec(820f, 380f, 280f, 36f),
        PlatformSpec(1180f, 310f, 280f, 36f),
        PlatformSpec(1560f, 240f, 360f, 44f)
    )

    private var score: Int = 0
    private var coresCollected: Int = 0
    private var animState: PlatformerAnimationState = PlatformerAnimationState.IDLE
    private var facingSign: Float = 1f
    private var aimAngleRad: Float = -0.35f
    private var dashCooldownSeconds: Float = 0f
    private var dashTimerSeconds: Float = 0f
    private var manualMoveInputX: Float = 0f
    private var paused: Boolean = false
    private var completed: Boolean = false
    private var activeProfile: ControllerHardwareProfile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE
    private var simulatedInputFrame: GamepadFrameSnapshot? = null

    var lastRumbleTelemetry: String = "Haptics ready"
        private set
    var hapticBurstCount: Int = 0
        private set

    init {
        buildParallaxSkyline()
        buildPlatformsAndGoalTilemap()
        buildPlayerAndCamera()
        spawnQuantumCores()
    }

    private fun buildParallaxSkyline() {
        // Far skyline towers (scrolls at 65% of camera displacement, zOrder = -30)
        parallaxFarEntity = scene.createEntity("ParallaxSkylineFar", tag = "Background").apply {
            transform.setPosition2D(-100f, 95f)
            val farMap = Tilemap2DModule(columns = 42, rows = 8, tileWidth = 48f, tileHeight = 48f, zOrder = -30)
            farMap.setTileColor(1, RgbaColor(15, 23, 42, 255))
            farMap.setTileColor(2, RgbaColor(30, 41, 59, 255))
            farMap.setTileColor(3, RgbaColor(56, 189, 248, 70))
            for (c in 0 until 42) {
                val towerH = 2 + ((c * 5 + 3) % 5)
                for (r in (8 - towerH) until 8) {
                    farMap.setTile(c, r, if ((r + c) % 3 == 0 && r == 8 - towerH) 3 else 1)
                }
            }
            addModule(farMap)
            addModule(object : EntityBehavior() {
                override fun onLateTick(deltaSeconds: Float) {
                    val camX = cameraEntity.transform.position.x
                    entity?.transform?.setPosition2D(-260f + (camX - 440f) * 0.65f, 95f)
                }
            })
        }

        // Mid skyline neon girders (scrolls at 35% of camera displacement, zOrder = -20)
        parallaxMidEntity = scene.createEntity("ParallaxSkylineMid", tag = "Background").apply {
            transform.setPosition2D(-60f, 180f)
            val midMap = Tilemap2DModule(columns = 38, rows = 7, tileWidth = 56f, tileHeight = 48f, zOrder = -20)
            midMap.setTileColor(1, RgbaColor(23, 37, 84, 215))
            midMap.setTileColor(2, RgbaColor(14, 165, 233, 130))
            for (c in 0 until 38) {
                val height = 2 + ((c * 3 + 1) % 4)
                for (r in (7 - height) until 7) {
                    midMap.setTile(c, r, if (r == 7 - height) 2 else 1)
                }
            }
            addModule(midMap)
            addModule(object : EntityBehavior() {
                override fun onLateTick(deltaSeconds: Float) {
                    val camX = cameraEntity.transform.position.x
                    entity?.transform?.setPosition2D(-180f + (camX - 440f) * 0.35f, 180f)
                }
            })
        }
    }

    private fun buildPlatformsAndGoalTilemap() {
        for ((idx, spec) in platforms.withIndex()) {
            scene.createEntity("Platform_$idx", tag = "Platform").apply {
                transform.setPosition2D(spec.x, spec.y)
                addModule(RigidBody2DModule(BodyType2D.STATIC))
                addModule(Collider2DModule.box(spec.width, spec.height, friction = 0.25f, restitution = 0f))

                val cols = (spec.width / 32f).roundToInt().coerceAtLeast(1)
                val rows = 1
                val tilemap = Tilemap2DModule(
                    columns = cols,
                    rows = rows,
                    tileWidth = spec.width / cols,
                    tileHeight = spec.height,
                    zOrder = -10
                ).apply {
                    setTileColor(0, RgbaColor(51, 65, 85, 255))
                    setTileColor(1, if (idx == platforms.lastIndex) RgbaColor(245, 158, 11, 255) else RgbaColor(56, 189, 248, 255))
                }
                for (c in 0 until cols) {
                    tilemap.setTile(c, 0, if (c == 0 || c == cols - 1 || idx == platforms.lastIndex) 1 else 0)
                }
                addModule(tilemap)
            }
        }
    }

    private fun buildPlayerAndCamera() {
        val skeleton = PlatformerRigFactory.createCourierSkeleton(scale = 0.82f)
        playerCharacterModule = SvgCharacter2DModule(
            skeleton = skeleton,
            zOrder = 10,
            drawBoneOverlay = false
        ).apply {
            animator.play(idleClip)
        }

        playerParticlesModule = ParticleEmitter2DModule(
            emissionRatePerSecond = 0f,
            maxParticles = 160,
            particleLifetimeSeconds = 0.45f,
            initialSpeedMin = 70f,
            initialSpeedMax = 210f,
            gravity = Vec2f(0f, 180f),
            startSize = 7f,
            endSize = 1.5f,
            startColor = RgbaColor(56, 189, 248, 255),
            endColor = RgbaColor(244, 63, 94, 0),
            zOrder = 8,
            isEmitting = false
        )

        playerBodyModule = RigidBody2DModule(
            bodyType = BodyType2D.DYNAMIC,
            gravityScale = 1f,
            linearDamping = 0.1f,
            fixedRotation = true
        )

        // Start cleanly just above Floor 0 (`y = 445 - 24 - 44 = 377`)
        playerEntity = scene.createEntity("SkylineHero", tag = "Player").apply {
            transform.setPosition2D(160f, 360f)
            addModule(playerBodyModule)
            addModule(Collider2DModule.box(width = 34f, height = 88f, density = 1f, friction = 0.1f, restitution = 0f))
            addModule(playerCharacterModule)
            addModule(playerParticlesModule)
            addModule(object : EntityBehavior() {
                override fun onFixedStep(fixedDeltaSeconds: Float) {
                    stepHeroPhysicsAndControls(fixedDeltaSeconds)
                }

                override fun onLateTick(deltaSeconds: Float) {
                    applyTwoBoneIkArmAim()
                    checkCollectiblesAndGoal()
                }
            })
        }

        cameraModule = Camera2DModule(
            viewportWidth = canvasWidth.toFloat(),
            viewportHeight = canvasHeight.toFloat(),
            zoom = 1f,
            pixelsPerUnit = 1f,
            followTarget = playerEntity,
            followSmoothing = 8.5f
        )

        cameraEntity = scene.createEntity("MainCamera", tag = "Camera").apply {
            transform.setPosition2D(440f, 300f)
            addModule(cameraModule)
        }
    }

    private fun spawnQuantumCores() {
        _collectibles.clear()
        val placements = listOf(
            Triple("core-1", "Skyline Core Alpha", 340f to 365f),
            Triple("core-2", "Overpass Core Beta", 820f to 305f),
            Triple("core-3", "Skybridge Core Gamma", 1180f to 235f),
            Triple("core-4", "Apex Relay Core", 1620f to 165f)
        )
        for ((id, name, pos) in placements) {
            val coreSkel = PlatformerRigFactory.createQuantumCoreSkeleton(1.0f)
            val coreMod = SvgCharacter2DModule(coreSkel, zOrder = 5)
            val entity = scene.createEntity(id, tag = "Collectible").apply {
                transform.setPosition2D(pos.first, pos.second)
                addModule(coreMod)
                addModule(object : EntityBehavior() {
                    private var phase = 0f
                    override fun onTick(deltaSeconds: Float) {
                        phase += deltaSeconds * 3.2f
                        entity?.transform?.rotation2DRad = phase * 0.45f
                        entity?.transform?.setPosition2D(pos.first, pos.second + sin(phase) * 7f)
                    }
                })
            }
            _collectibles += PlatformerCollectible(
                id = id,
                name = name,
                x = pos.first,
                y = pos.second,
                points = 100,
                entity = entity
            )
        }
    }

    fun getSnapshot(): PlatformerSnapshot {
        val body = playerBodyModule.body
        val vx = body?.linearVelocity?.x ?: 0f
        val vy = body?.linearVelocity?.y ?: 0f
        return PlatformerSnapshot(
            score = score,
            coresCollected = coresCollected,
            totalCores = _collectibles.size,
            playerX = playerEntity.transform.position.x,
            playerY = playerEntity.transform.position.y,
            vx = vx,
            vy = vy,
            grounded = isHeroGrounded(),
            dashCooldownSeconds = dashCooldownSeconds,
            animState = animState,
            facingSign = facingSign,
            aimAngleDeg = (aimAngleRad * 57.29578f),
            drawBoneOverlay = playerCharacterModule.drawBoneOverlay,
            paused = paused,
            completed = completed,
            activeProfile = activeProfile
        )
    }

    fun isHeroGrounded(): Boolean {
        val body = playerBodyModule.body ?: return false
        val px = playerEntity.transform.position.x
        val pyBottom = playerEntity.transform.position.y + 44f
        val vy = body.linearVelocity.y
        if (abs(vy) > 35f) return false
        return platforms.any { spec ->
            val halfW = spec.width * 0.5f + 16f
            val topY = spec.y - spec.height * 0.5f
            px in (spec.x - halfW)..(spec.x + halfW) && abs(pyBottom - topY) <= 10f
        }
    }

    fun setPaused(paused: Boolean) {
        this.paused = paused
        scene.clock.paused = paused
    }

    fun togglePaused(): Boolean {
        setPaused(!paused)
        return paused
    }

    fun toggleBoneOverlay(): Boolean {
        playerCharacterModule.drawBoneOverlay = !playerCharacterModule.drawBoneOverlay
        return playerCharacterModule.drawBoneOverlay
    }

    fun setControllerProfile(profile: ControllerHardwareProfile) {
        activeProfile = profile
        val current = gamepadManager.snapshot(0)
        if (current.connected && current.deviceId.startsWith("sim-")) {
            val updated = current.copy(
                deviceId = "sim-plat-${profile.name.lowercase()}",
                deviceName = "Simulated ${profile.name}",
                profile = profile,
                playStation = if (profile.family == ControllerFamily.PLAYSTATION) PlayStationControllerFeatures() else null,
                switchFeatures = if (profile.family == ControllerFamily.NINTENDO_SWITCH) SwitchControllerFeatures() else null,
                xbox = if (profile.family == ControllerFamily.XBOX) XboxControllerFeatures() else null
            )
            simulatedInputFrame = updated
            gamepadManager.setSyntheticController(updated)
            gamepadManager.poll()
        }
        applyPlatformerHaptics(0.15f, 0.35f, 80, "Profile ${profile.name}")
    }

    fun cycleControllerProfile(): ControllerHardwareProfile {
        val order = listOf(
            ControllerHardwareProfile.PLAYSTATION_DUALSENSE,
            ControllerHardwareProfile.XBOX_ONE_SERIES,
            ControllerHardwareProfile.NINTENDO_SWITCH_PRO
        )
        val idx = order.indexOf(activeProfile)
        val next = order[(idx + 1) % order.size]
        setControllerProfile(next)
        return next
    }

    fun simulateGamepadInput(
        profile: ControllerHardwareProfile = activeProfile,
        pressedButtonsMask: Int = 0,
        leftStickX: Float = 0f,
        leftStickY: Float = 0f,
        rightStickX: Float = 0f,
        rightStickY: Float = 0f,
        gyroYawRadPerSec: Float = 0f,
        gyroPitchRadPerSec: Float = 0f
    ) {
        activeProfile = profile
        val snap = GamepadFrameSnapshot(
            playerIndex = 0,
            connected = true,
            deviceId = "sim-plat-${profile.name.lowercase()}",
            deviceName = "Simulated ${profile.name}",
            profile = profile,
            pressedButtonsMask = pressedButtonsMask,
            leftStick = GamepadAxis2D(leftStickX, leftStickY),
            rightStick = GamepadAxis2D(rightStickX, rightStickY),
            motion = SixAxisMotionState(
                gyroRadPerSecX = gyroPitchRadPerSec,
                gyroRadPerSecY = gyroYawRadPerSec,
                pitchRad = gyroPitchRadPerSec * 0.25f,
                yawRad = gyroYawRadPerSec * 0.25f,
                isAvailable = profile.family != ControllerFamily.GENERIC
            ),
            playStation = if (profile.family == ControllerFamily.PLAYSTATION) PlayStationControllerFeatures() else null,
            switchFeatures = if (profile.family == ControllerFamily.NINTENDO_SWITCH) SwitchControllerFeatures() else null,
            xbox = if (profile.family == ControllerFamily.XBOX) XboxControllerFeatures() else null
        )
        simulatedInputFrame = snap
        gamepadManager.setSyntheticController(snap)
    }

    fun currentControllerFrame(): GamepadFrameSnapshot {
        val polled = gamepadManager.snapshot(0)
        return if (polled.connected) {
            polled
        } else {
            simulatedInputFrame ?: GamepadFrameSnapshot(
                playerIndex = 0,
                connected = false,
                deviceName = activeProfile.name,
                profile = activeProfile
            )
        }
    }

    fun setManualMoveInput(dirX: Float) {
        manualMoveInputX = dirX.coerceIn(-1f, 1f)
    }

    fun triggerJump(): Boolean {
        val body = playerBodyModule.ensureBodyCreated() ?: return false
        if (!isHeroGrounded()) return false
        body.linearVelocity.y = -470f
        body.isAwake = true
        animState = PlatformerAnimationState.JUMP
        playerCharacterModule.animator.play(jumpClip)
        playerParticlesModule.emitBurst(12)
        applyPlatformerHaptics(0.25f, 0.55f, 100, "Jump thrust")
        return true
    }

    fun triggerDash(): Boolean {
        if (dashCooldownSeconds > 0f) return false
        val body = playerBodyModule.ensureBodyCreated() ?: return false
        dashCooldownSeconds = 0.8f
        dashTimerSeconds = 0.22f
        body.linearVelocity.x = facingSign * 440f
        body.linearVelocity.y = (-40f).coerceAtMost(body.linearVelocity.y)
        body.isAwake = true
        animState = PlatformerAnimationState.DASH
        playerCharacterModule.animator.play(dashClip)
        cameraModule.triggerShake(7.5f)
        playerParticlesModule.emitBurst(22)
        applyPlatformerHaptics(0.65f, 0.9f, 160, "Jet Dash!")
        return true
    }

    fun resetStage() {
        score = 0
        coresCollected = 0
        completed = false
        paused = false
        scene.clock.paused = false
        dashCooldownSeconds = 0f
        dashTimerSeconds = 0f
        facingSign = 1f
        manualMoveInputX = 0f
        val body = playerBodyModule.ensureBodyCreated()
        body?.setTransform(Vec2(160f, 360f), 0f)
        body?.linearVelocity?.set(0f, 0f)
        playerEntity.transform.setPosition2D(160f, 360f)
        playerEntity.transform.setScale2D(1f, 1f)
        cameraEntity.transform.setPosition2D(440f, 300f)
        for (c in _collectibles) {
            c.collected = false
            c.entity.isActive = true
        }
        animState = PlatformerAnimationState.IDLE
        playerCharacterModule.animator.play(idleClip)
    }

    fun stepLogicOnly(deltaSeconds: Float) {
        host.scheduler.stepManualTick(deltaSeconds)
    }

    fun renderVBlankFrame() {
        host.stepVBlank(scene.clock.totalElapsedSeconds * 1000.0)
    }

    fun stepSimulation(deltaSeconds: Float) {
        stepLogicOnly(deltaSeconds)
        renderVBlankFrame()
    }

    private fun stepHeroPhysicsAndControls(dt: Float) {
        if (dashCooldownSeconds > 0f) dashCooldownSeconds = (dashCooldownSeconds - dt).coerceAtLeast(0f)
        if (dashTimerSeconds > 0f) dashTimerSeconds = (dashTimerSeconds - dt).coerceAtLeast(0f)

        val frame = currentControllerFrame()
        if (frame.connected && frame.profile != activeProfile) {
            activeProfile = frame.profile
        }

        val body = playerBodyModule.ensureBodyCreated() ?: return
        var moveX = frame.leftStick.x
        if (abs(moveX) < 0.12f) moveX = frame.dpadVector.x
        if (abs(moveX) < 0.12f && abs(manualMoveInputX) > 0.05f) moveX = manualMoveInputX
        if (abs(moveX) < 0.12f) {
            if (scene.input.isKeyDown("ArrowRight") || scene.input.isKeyDown("KeyD")) moveX += 1f
            if (scene.input.isKeyDown("ArrowLeft") || scene.input.isKeyDown("KeyA")) moveX -= 1f
        }

        if (moveX > 0.12f) facingSign = 1f else if (moveX < -0.12f) facingSign = -1f
        playerEntity.transform.setScale2D(facingSign, 1f)

        if (dashTimerSeconds <= 0f) {
            val targetVx = moveX.coerceIn(-1f, 1f) * 235f
            val blend = (14f * dt).coerceIn(0f, 1f)
            body.linearVelocity.x = body.linearVelocity.x + (targetVx - body.linearVelocity.x) * blend
        }
        body.isAwake = true

        if (frame.isButtonJustPressed(GamepadButton.A) || scene.input.wasKeyJustPressed("Space")) {
            triggerJump()
        }
        if (frame.isButtonJustPressed(GamepadButton.RIGHT_BUMPER) || frame.isButtonJustPressed(GamepadButton.X)) {
            triggerDash()
        }

        // Aim right arm via Right Stick or 6-axis gyro pitch/yaw
        val rx = frame.rightStick.x
        val ry = frame.rightStick.y
        if (abs(rx) > 0.18f || abs(ry) > 0.18f) {
            aimAngleRad = atan2(-ry, rx)
        } else if (frame.motion.isAvailable && (abs(frame.motion.gyroRadPerSecX) > 0.1f || abs(frame.motion.pitchRad) > 0.05f)) {
            aimAngleRad = (frame.motion.pitchRad - 0.35f).coerceIn(-1.2f, 0.8f)
        }

        val grounded = isHeroGrounded()
        val nextState = when {
            dashTimerSeconds > 0f -> PlatformerAnimationState.DASH
            !grounded -> PlatformerAnimationState.JUMP
            abs(body.linearVelocity.x) > 25f -> PlatformerAnimationState.RUN
            else -> PlatformerAnimationState.IDLE
        }
        if (nextState != animState) {
            animState = nextState
            val clip = when (nextState) {
                PlatformerAnimationState.IDLE -> idleClip
                PlatformerAnimationState.RUN -> runClip
                PlatformerAnimationState.JUMP -> jumpClip
                PlatformerAnimationState.DASH -> dashClip
            }
            playerCharacterModule.animator.play(clip)
        }

        // Respawn safety net if hero falls below skyline
        if (playerEntity.transform.position.y > 680f) {
            body.setTransform(Vec2(160f, 360f), 0f)
            body.linearVelocity.set(0f, 0f)
            cameraModule.triggerShake(10f)
            applyPlatformerHaptics(0.8f, 0.4f, 190, "Skyline recovery")
        }
    }

    private fun applyTwoBoneIkArmAim() {
        val shoulderBone = playerCharacterModule.skeleton.getBone("upperArmR") ?: return
        playerCharacterModule.skeleton.updateWorldTransforms()
        val reachPx = 42f
        val localAngle = if (facingSign < 0f) PI.toFloat() - aimAngleRad else aimAngleRad
        val targetX = shoulderBone.worldX + cos(localAngle) * reachPx
        val targetY = shoulderBone.worldY + sin(localAngle) * reachPx
        TwoBoneIkSolver2D.applyToBones(
            skeleton = playerCharacterModule.skeleton,
            upperBoneName = "upperArmR",
            lowerBoneName = "forearmR",
            targetX = targetX,
            targetY = targetY,
            bendDirection = 1f,
            weight = 0.85f
        )
    }

    private fun checkCollectiblesAndGoal() {
        val hx = playerEntity.transform.position.x
        val hy = playerEntity.transform.position.y
        for (c in _collectibles) {
            if (c.collected) continue
            if (hypot(hx - c.x, hy - c.y) <= 62f) {
                c.collected = true
                c.entity.isActive = false
                coresCollected++
                score += c.points
                playerParticlesModule.emitBurst(20)
                cameraModule.triggerShake(5f)
                applyPlatformerHaptics(0.3f, 0.85f, 140, "Collected ${c.name}!")
                if (coresCollected == _collectibles.size) {
                    completed = true
                    score += 250
                }
            }
        }
    }

    private fun applyPlatformerHaptics(low: Float, high: Float, durationMs: Int, reason: String) {
        gamepadManager.setRumble(
            0,
            GamepadRumbleRequest(
                lowFrequency = low,
                highFrequency = high,
                leftTrigger = low * 0.5f,
                rightTrigger = high * 0.6f,
                durationMs = durationMs
            )
        )
        gamepadManager.setPlayStationLightbarColor(
            0,
            if (completed) 250 else 56,
            if (completed) 204 else 189,
            if (completed) 21 else 248
        )
        gamepadManager.setPlayStationAdaptiveTrigger(
            0,
            AdaptiveTriggerSide.RIGHT,
            AdaptiveTriggerEffect.SectionResistance(0.3f, 0.85f, 0.5f)
        )
        gamepadManager.setSwitchHdRumble(
            0,
            SwitchHdRumbleCommand(
                leftHighAmp = high,
                leftLowAmp = low,
                rightHighAmp = high,
                rightLowAmp = low,
                durationMs = durationMs
            )
        )
        gamepadManager.setSwitchHomeLedPattern(0, SwitchHomeLedPattern.SOLID_ON)
        gamepadManager.setXboxImpulseRumble(
            0,
            XboxImpulseRumbleCommand(low, high, low * 0.5f, high * 0.7f, durationMs)
        )
        hapticBurstCount++
        lastRumbleTelemetry = "${activeProfile.family.name} L=${(low * 100).roundToInt()}% H=${(high * 100).roundToInt()}% ($reason)"
    }
}

package com.nexustools.genericui.examples.arena3d

import com.nexustools.genericui.component.media.Canvas3D
import com.nexustools.genericui.ext.box3d.BodyType3D
import com.nexustools.genericui.ext.box3d.Ray3D
import com.nexustools.genericui.ext.box3d.Vec3
import com.nexustools.genericui.ext.engine.animation.AnimationWrapMode
import com.nexustools.genericui.ext.engine.animation.EasingFunction
import com.nexustools.genericui.ext.engine.math.Vec3f
import com.nexustools.genericui.ext.engine.scene.EntityBehavior
import com.nexustools.genericui.ext.engine.scene.GameScene
import com.nexustools.genericui.ext.engine.scene.SceneEntity
import com.nexustools.genericui.ext.engine3d.CharacterController3DModule
import com.nexustools.genericui.ext.engine3d.Collider3DModule
import com.nexustools.genericui.ext.engine3d.DirectionalLight3DModule
import com.nexustools.genericui.ext.engine3d.FollowCamera3DModule
import com.nexustools.genericui.ext.engine3d.Game3DCanvasHost
import com.nexustools.genericui.ext.engine3d.MeshRenderer3DModule
import com.nexustools.genericui.ext.engine3d.OrbitCamera3DModule
import com.nexustools.genericui.ext.engine3d.PhysicsWorld3DSubsystem
import com.nexustools.genericui.ext.engine3d.PointLight3DModule
import com.nexustools.genericui.ext.engine3d.ProceduralHumanoidRig3D
import com.nexustools.genericui.ext.engine3d.RigidBody3DModule
import com.nexustools.genericui.ext.engine3d.SkeletalModel3DModule
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
import com.nexustools.genericui.ext.gpu.GpuAccelerationPolicy
import com.nexustools.genericui.ext.gpu.GpuBackendType
import com.nexustools.genericui.ext.threejs.cameras.PerspectiveCamera
import com.nexustools.genericui.ext.threejs.geometries.BoxGeometry
import com.nexustools.genericui.ext.threejs.geometries.SphereGeometry
import com.nexustools.genericui.ext.threejs.lights.AmbientLight
import com.nexustools.genericui.ext.threejs.materials.MeshStandardMaterial
import com.nexustools.genericui.ext.threejs.math.Color
import com.nexustools.genericui.ext.threejs.renderers.WebGLRenderer
import com.nexustools.genericui.ext.threejs.renderers.WebGLRendererConfig
import com.nexustools.genericui.ext.threejs.scenes.Scene
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * 3D Arena Action controller orchestrating [Game3DCanvasHost], Three.js lighting and materials,
 * 15-bone rigged [ProceduralHumanoidRig3D] heroes/adversaries with procedural/additive bone overrides,
 * Box3D [CharacterController3DModule] + dynamic projectiles + raycast sightline, dual-stick & 6-axis
 * gyro aim, and full DualSense / Switch Pro / Xbox controller feedback.
 */
class ArenaEngineController(
    val viewportWidth: Int = 920,
    val viewportHeight: Int = 518
) {
    val canvas: Canvas3D = Canvas3D(
        backend = GpuBackendType.OPENGL_ES,
        acceleration = GpuAccelerationPolicy.HARDWARE_PREFERRED,
        width = viewportWidth,
        height = viewportHeight
    ).apply {
        id = "canvas-arena-3d"
        addClass("arena-canvas-3d")
    }

    val threeScene: Scene = Scene().apply {
        background = Color(0x090714)
        add(AmbientLight(color = Color(0x475569), intensity = 0.85f))
    }

    private val aspect: Float = viewportWidth.toFloat() / viewportHeight.coerceAtLeast(1).toFloat()
    val followPerspectiveCamera: PerspectiveCamera = PerspectiveCamera(52f, aspect, 0.1f, 2000f)
    val orbitPerspectiveCamera: PerspectiveCamera = PerspectiveCamera(52f, aspect, 0.1f, 2000f)

    val renderer: WebGLRenderer = WebGLRenderer(
        config = WebGLRendererConfig(preferVulkanWhenAvailable = false),
        canvas = canvas
    )
    val scene: GameScene = GameScene("VanguardArena3DScene")
    val host: Game3DCanvasHost = Game3DCanvasHost(
        scene = scene,
        renderer = renderer,
        threeScene = threeScene,
        activeCamera = followPerspectiveCamera,
        targetTicksPerSecond = 60
    )

    val gamepadManager: GamepadManager
        get() = scene.gamepadManager

    val physicsSubsystem: PhysicsWorld3DSubsystem = scene.registerSubsystem(
        PhysicsWorld3DSubsystem(gravity = Vec3(0f, -18f, 0f), threeScene = threeScene)
    )

    lateinit var heroEntity: SceneEntity private set
    lateinit var heroRigModule: SkeletalModel3DModule private set
    lateinit var heroController: CharacterController3DModule private set
    lateinit var followCameraEntity: SceneEntity private set
    lateinit var followCameraModule: FollowCamera3DModule private set
    lateinit var orbitCameraEntity: SceneEntity private set
    lateinit var orbitCameraModule: OrbitCamera3DModule private set

    private val _enemies = mutableListOf<ArenaEnemyUnit>()
    val enemies: List<ArenaEnemyUnit> get() = _enemies

    private val _projectiles = mutableListOf<ArenaProjectile>()
    val projectiles: List<ArenaProjectile> get() = _projectiles

    private var wave: Int = 1
    private var score: Int = 0
    private var heroHp: Int = 100
    private var heroMaxHp: Int = 100
    private var shotsFired: Int = 0
    private var hitsLanded: Int = 0
    private var projectileCounter: Int = 0
    private var fireCooldownSeconds: Float = 0f
    private var aimYawRad: Float = 0f
    private var aimPitchRad: Float = -0.05f
    private var lastRaycastDistance: Float? = null
    private var lastRaycastTargetName: String = "Clear sky"
    private var cameraMode: ArenaCameraMode = ArenaCameraMode.THIRD_PERSON_FOLLOW
    private var gyroAimEnabled: Boolean = true
    private var paused: Boolean = false
    private var victory: Boolean = false
    private var manualMoveForward: Float = 0f
    private var manualStrafeX: Float = 0f
    private var activeProfile: ControllerHardwareProfile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE
    private var simulatedInputFrame: GamepadFrameSnapshot? = null

    var lastHapticTelemetry: String = "DualSense Adaptive Triggers Armed"
        private set
    var hapticTriggerCount: Int = 0
        private set
    var boneOverrideEvaluations: Int = 0
        private set

    init {
        buildArenaEnvironment()
        buildHeroAndCameras()
        spawnWaveEnemies(wave)
        updateAimRaycast()
    }

    private fun buildArenaEnvironment() {
        // Primary arena floor (`BoxGeometry(36, 0.8, 36)` top surface at y = 0)
        scene.createEntity("ArenaFloor", tag = "Floor").apply {
            transform.setPosition(0f, -0.4f, 0f)
            addModule(
                MeshRenderer3DModule(
                    geometry = BoxGeometry(36f, 0.8f, 36f),
                    material = MeshStandardMaterial(color = Color(0x1e293b), roughness = 0.7f, metalness = 0.2f)
                )
            )
            addModule(RigidBody3DModule(BodyType3D.STATIC))
            addModule(Collider3DModule.box(18f, 0.4f, 18f, friction = 0.6f, restitution = 0.05f))
        }

        // 4 neon cover pillars around the arena
        val pillarCoords = listOf(
            Triple("PillarNW", -6.5f, -6.5f),
            Triple("PillarNE", 6.5f, -6.5f),
            Triple("PillarSW", -6.5f, 6.5f),
            Triple("PillarSE", 6.5f, 6.5f)
        )
        for ((name, px, pz) in pillarCoords) {
            scene.createEntity(name, tag = "Obstacle").apply {
                transform.setPosition(px, 1.8f, pz)
                addModule(
                    MeshRenderer3DModule(
                        geometry = BoxGeometry(1.8f, 3.6f, 1.8f),
                        material = MeshStandardMaterial(
                            color = Color(0x312e81),
                            emissive = Color(0x6d28d9),
                            emissiveIntensity = 0.35f,
                            roughness = 0.4f
                        )
                    )
                )
                addModule(RigidBody3DModule(BodyType3D.STATIC))
                addModule(Collider3DModule.box(0.9f, 1.8f, 0.9f))
            }
        }

        // Perimeter walls
        val wallSpecs = listOf(
            Triple("WallNorth", 0f to -17.5f, 36f to 1f),
            Triple("WallSouth", 0f to 17.5f, 36f to 1f),
            Triple("WallWest", -17.5f to 0f, 1f to 36f),
            Triple("WallEast", 17.5f to 0f, 1f to 36f)
        )
        for ((name, pos, size) in wallSpecs) {
            scene.createEntity(name, tag = "Boundary").apply {
                transform.setPosition(pos.first, 1.2f, pos.second)
                addModule(
                    MeshRenderer3DModule(
                        geometry = BoxGeometry(size.first, 2.4f, size.second),
                        material = MeshStandardMaterial(color = Color(0x0f172a), roughness = 0.8f)
                    )
                )
                addModule(RigidBody3DModule(BodyType3D.STATIC))
                addModule(Collider3DModule.box(size.first * 0.5f, 1.2f, size.second * 0.5f))
            }
        }

        // Lighting: Directional key light + Magenta/Cyan arena rim point lights
        scene.createEntity("KeySunLight", tag = "Light").apply {
            transform.setPosition(10f, 16f, 12f)
            addModule(DirectionalLight3DModule(color = Color(0xf8fafc), intensity = 1.35f))
        }
        scene.createEntity("PinkRimLight", tag = "Light").apply {
            transform.setPosition(-9f, 5.5f, -7f)
            addModule(PointLight3DModule(color = Color(0xec4899), intensity = 2.6f, distance = 26f, decay = 1.5f))
        }
        scene.createEntity("CyanRimLight", tag = "Light").apply {
            transform.setPosition(9f, 5.5f, 7f)
            addModule(PointLight3DModule(color = Color(0x38bdf8), intensity = 2.2f, distance = 26f, decay = 1.5f))
        }
    }

    private fun buildHeroAndCameras() {
        val rig = ProceduralHumanoidRig3D.create(
            material = MeshStandardMaterial(color = Color(0x38bdf8), roughness = 0.45f, metalness = 0.3f),
            scale = 1.0f
        )
        heroRigModule = SkeletalModel3DModule(rig).apply {
            playClip("walk", loop = true)
        }
        heroController = CharacterController3DModule(
            maxSlopeAngleRad = 0.78f,
            maxStepHeight = 0.35f,
            skinWidth = 0.02f
        )

        heroEntity = scene.createEntity("VanguardHero", tag = "Player").apply {
            transform.setPosition(0f, 1.0f, 5.5f)
            addModule(RigidBody3DModule(BodyType3D.KINEMATIC, gravityScale = 0f, lockRotations = true))
            addModule(Collider3DModule.capsule(radius = 0.45f, halfHeight = 0.5f))
            addModule(heroController)
            addModule(heroRigModule)
            addModule(object : EntityBehavior() {
                override fun onFixedStep(fixedDeltaSeconds: Float) {
                    stepHeroControlsAndProjectiles(fixedDeltaSeconds)
                }

                override fun onRenderVBlank(frameNumber: Long, deltaSeconds: Float, timestampMs: Double) {
                    applyProceduralHeroBoneAimOverrides()
                }
            })
        }

        // Register additive chest/shoulder VBlank lambda bone override showcasing animateBoneLambda
        heroRigModule.animateBoneLambda(
            boneName = "chest",
            durationSeconds = 1.6f,
            curve = EasingFunction.SINE_IN_OUT,
            wrapMode = AnimationWrapMode.LOOP,
            scheduler = scene.animations
        ) { chestBone, p ->
            val breathAngle = sin(p * PI.toFloat() * 2f) * 0.05f
            chestBone.rotation.set(breathAngle, aimYawRad * 0.35f, 0f)
            boneOverrideEvaluations++
        }

        followCameraModule = FollowCamera3DModule(
            camera = followPerspectiveCamera,
            target = heroEntity,
            offset = Vec3f(0f, 4.2f, 8.2f),
            lookAtOffset = Vec3f(0f, 1.35f, 0f),
            damping = 8.0f
        )
        followCameraEntity = scene.createEntity("FollowCamera", tag = "Camera").apply {
            transform.setPosition(0f, 5.2f, 13.7f)
            addModule(followCameraModule)
        }

        orbitCameraModule = OrbitCamera3DModule(
            camera = orbitPerspectiveCamera,
            canvas = canvas
        )
        orbitCameraEntity = scene.createEntity("OrbitCamera", tag = "Camera").apply {
            transform.setPosition(8f, 9f, 13f)
            addModule(orbitCameraModule)
        }
        orbitCameraModule.controls.target.set(0f, 1.2f, 0f)
        orbitCameraModule.controls.update(0f)

        host.activeCamera = followCameraModule.camera
    }

    private fun spawnWaveEnemies(waveNum: Int) {
        _enemies.clear()
        val configs = listOf(
            Triple("Sentry Alpha", -4.5f to -5.0f, 2.2f),
            Triple("Vanguard Drone", 0.0f to -8.0f, 3.0f),
            Triple("Striker Omega", 4.5f to -4.5f, 2.4f)
        )
        for ((idx, cfg) in configs.withIndex()) {
            val (name, center, radius) = cfg
            val rig = ProceduralHumanoidRig3D.create(
                material = MeshStandardMaterial(
                    color = Color(0xf43f5e),
                    emissive = Color(0x881337),
                    emissiveIntensity = 0.25f,
                    roughness = 0.5f
                ),
                scale = 1.0f
            )
            val rigMod = SkeletalModel3DModule(rig).apply {
                playClip(if (idx % 2 == 0) "walk" else "run", loop = true)
            }
            val hp = 36 + (waveNum - 1) * 18
            val enemyEntity = scene.createEntity("Enemy_${waveNum}_$idx", tag = "Enemy").apply {
                transform.setPosition(center.first, 1.0f, center.second)
                addModule(RigidBody3DModule(BodyType3D.KINEMATIC, gravityScale = 0f, lockRotations = true))
                addModule(Collider3DModule.box(0.55f, 0.95f, 0.55f))
                addModule(rigMod)
            }
            _enemies += ArenaEnemyUnit(
                id = "enemy-${waveNum}-$idx",
                name = name,
                patrolCenterX = center.first,
                patrolCenterZ = center.second,
                patrolRadius = radius,
                patrolPhase = idx * 1.4f,
                hp = hp,
                maxHp = hp,
                entity = enemyEntity,
                rigModule = rigMod
            )
        }
    }

    fun getSnapshot(): ArenaStateSnapshot = ArenaStateSnapshot(
        wave = wave,
        score = score,
        heroHp = heroHp,
        heroMaxHp = heroMaxHp,
        enemiesRemaining = _enemies.count { !it.defeated },
        shotsFired = shotsFired,
        hitsLanded = hitsLanded,
        heroX = heroEntity.transform.position.x,
        heroY = heroEntity.transform.position.y,
        heroZ = heroEntity.transform.position.z,
        heroGrounded = heroController.isGrounded,
        aimYawDeg = aimYawRad * 57.29578f,
        aimPitchDeg = aimPitchRad * 57.29578f,
        raycastDistanceMeters = lastRaycastDistance,
        raycastTargetName = lastRaycastTargetName,
        cameraMode = cameraMode,
        gyroAimEnabled = gyroAimEnabled,
        paused = paused,
        victory = victory,
        activeProfile = activeProfile
    )

    fun toggleCameraMode(): ArenaCameraMode {
        cameraMode = if (cameraMode == ArenaCameraMode.THIRD_PERSON_FOLLOW) {
            host.activeCamera = orbitCameraModule.camera
            ArenaCameraMode.ORBIT_TACTICAL
        } else {
            host.activeCamera = followCameraModule.camera
            ArenaCameraMode.THIRD_PERSON_FOLLOW
        }
        return cameraMode
    }

    fun toggleGyroAim(): Boolean {
        gyroAimEnabled = !gyroAimEnabled
        return gyroAimEnabled
    }

    fun setPaused(paused: Boolean) {
        this.paused = paused
        scene.clock.paused = paused
    }

    fun togglePaused(): Boolean {
        setPaused(!paused)
        return paused
    }

    fun setControllerProfile(profile: ControllerHardwareProfile) {
        activeProfile = profile
        val current = gamepadManager.snapshot(0)
        if (current.connected && current.deviceId.startsWith("sim-")) {
            val updated = current.copy(
                deviceId = "sim-3d-${profile.name.lowercase()}",
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
        applyArenaFeedback(0.15f, 0.35f, 80, "Profile ${profile.name}")
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
        rightTrigger: Float = 0f,
        gyroYawRadPerSec: Float = 0f,
        gyroPitchRadPerSec: Float = 0f
    ) {
        activeProfile = profile
        val snap = GamepadFrameSnapshot(
            playerIndex = 0,
            connected = true,
            deviceId = "sim-3d-${profile.name.lowercase()}",
            deviceName = "Simulated ${profile.name}",
            profile = profile,
            pressedButtonsMask = pressedButtonsMask,
            leftStick = GamepadAxis2D(leftStickX, leftStickY),
            rightStick = GamepadAxis2D(rightStickX, rightStickY),
            rightTrigger = rightTrigger,
            motion = SixAxisMotionState(
                gyroRadPerSecX = gyroPitchRadPerSec,
                gyroRadPerSecY = gyroYawRadPerSec,
                pitchRad = gyroPitchRadPerSec * 0.2f,
                yawRad = gyroYawRadPerSec * 0.2f,
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

    fun setManualMove(strafeX: Float, forward: Float) {
        manualStrafeX = strafeX.coerceIn(-1f, 1f)
        manualMoveForward = forward.coerceIn(-1f, 1f)
    }

    fun adjustAim(yawDeltaRad: Float, pitchDeltaRad: Float) {
        aimYawRad = (aimYawRad + yawDeltaRad).coerceIn(-PI.toFloat(), PI.toFloat())
        aimPitchRad = (aimPitchRad + pitchDeltaRad).coerceIn(-0.65f, 0.55f)
        updateAimRaycast()
    }

    fun firePlasmaBolt(): Boolean {
        if (paused || victory) return false
        fireCooldownSeconds = 0.2f
        shotsFired++

        val dir = aimDirectionVec3f()
        val originPos = heroEntity.transform.position
        val spawnPos = Vec3f(
            originPos.x + dir.x * 0.85f,
            originPos.y + 1.25f + dir.y * 0.6f,
            originPos.z + dir.z * 0.85f
        )

        val boltId = "bolt-${++projectileCounter}"
        val boltEntity = scene.createEntity(boltId, tag = "Projectile").apply {
            transform.setPosition(spawnPos.x, spawnPos.y, spawnPos.z)
            addModule(
                MeshRenderer3DModule(
                    geometry = SphereGeometry(radius = 0.22f, widthSegments = 12, heightSegments = 8),
                    material = MeshStandardMaterial(
                        color = Color(0xfde047),
                        emissive = Color(0xf59e0b),
                        emissiveIntensity = 1.4f,
                        roughness = 0.2f
                    )
                )
            )
            addModule(RigidBody3DModule(BodyType3D.DYNAMIC, gravityScale = 0.04f, linearDamping = 0f))
            addModule(Collider3DModule.sphere(radius = 0.22f, density = 0.5f, restitution = 0.35f))
        }
        val bodyMod = boltEntity.getModule<RigidBody3DModule>()!!
        val speed = 26f
        bodyMod.ensureBodyCreated()?.linearVelocity?.set(dir.x * speed, dir.y * speed, dir.z * speed)
        _projectiles += ArenaProjectile(boltId, boltEntity, bodyMod, ttlSeconds = 2.5f, damage = 20)

        heroRigModule.playClip("attack", loop = false)
        applyArenaFeedback(0.45f, 0.85f, 140, "Plasma Bolt Fired")
        updateAimRaycast()
        return true
    }

    fun resetArena() {
        wave = 1
        score = 0
        heroHp = heroMaxHp
        shotsFired = 0
        hitsLanded = 0
        aimYawRad = 0f
        aimPitchRad = -0.05f
        victory = false
        paused = false
        scene.clock.paused = false
        for (p in _projectiles) {
            p.entity.isActive = false
            scene.removeEntity(p.entity)
        }
        _projectiles.clear()
        for (e in _enemies) {
            e.entity.isActive = false
            scene.removeEntity(e.entity)
        }
        heroEntity.transform.setPosition(0f, 1.0f, 5.5f)
        heroController.ensureController()?.let {
            heroController.move(Vec3f.ZERO, 1f / 60f)
        }
        spawnWaveEnemies(1)
        followCameraModule.snapToTarget()
        updateAimRaycast()
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

    private fun aimDirectionVec3f(): Vec3f {
        val cosPitch = cos(aimPitchRad)
        return Vec3f(
            x = sin(aimYawRad) * cosPitch,
            y = sin(aimPitchRad),
            z = -cos(aimYawRad) * cosPitch
        ).normalized()
    }

    private fun updateAimRaycast() {
        val pos = heroEntity.transform.position
        val dir = aimDirectionVec3f()
        val hit = physicsSubsystem.world.raycastClosest(
            Ray3D(
                origin = Vec3(pos.x + dir.x * 0.7f, pos.y + 1.25f, pos.z + dir.z * 0.7f),
                direction = Vec3(dir.x, dir.y, dir.z),
                maxDistance = 40f
            )
        )
        if (hit != null) {
            lastRaycastDistance = hit.distance
            val hitEntity = hit.collider.userData as? SceneEntity ?: hit.collider.body.userData as? SceneEntity
            lastRaycastTargetName = hitEntity?.name ?: "Collider"
        } else {
            lastRaycastDistance = null
            lastRaycastTargetName = "Clear sky"
        }
    }

    private fun applyProceduralHeroBoneAimOverrides() {
        // Aim upperArmR, forearmR, and head smoothly with aimPitchRad & aimYawRad even during walk/attack clips
        heroRigModule.getBone("upperArmR")?.let { arm ->
            arm.rotation.set(-1.15f + aimPitchRad, -aimYawRad * 0.45f, 0f)
            arm.syncQuaternionFromEuler()
        }
        heroRigModule.getBone("forearmR")?.let { forearm ->
            forearm.rotation.set(-0.25f, 0f, 0f)
            forearm.syncQuaternionFromEuler()
        }
        heroRigModule.getBone("head")?.let { head ->
            head.rotation.set(-aimPitchRad * 0.6f, aimYawRad * 0.35f, 0f)
            head.syncQuaternionFromEuler()
        }
    }

    private fun stepHeroControlsAndProjectiles(dt: Float) {
        if (fireCooldownSeconds > 0f) {
            fireCooldownSeconds = (fireCooldownSeconds - dt).coerceAtLeast(0f)
        }

        val frame = currentControllerFrame()
        if (frame.connected && frame.profile != activeProfile) {
            activeProfile = frame.profile
        }

        // Dual-stick + 6-axis gyro aim
        aimYawRad = (aimYawRad - frame.rightStick.x * dt * 2.4f).coerceIn(-PI.toFloat(), PI.toFloat())
        aimPitchRad = (aimPitchRad + frame.rightStick.y * dt * 1.6f).coerceIn(-0.65f, 0.55f)
        if (gyroAimEnabled && frame.motion.isAvailable) {
            aimYawRad = (aimYawRad + frame.motion.gyroRadPerSecY * dt * 0.85f).coerceIn(-PI.toFloat(), PI.toFloat())
            aimPitchRad = (aimPitchRad + frame.motion.gyroRadPerSecX * dt * 0.85f).coerceIn(-0.65f, 0.55f)
        }

        // Left stick / dpad / manual arena movement via CharacterController3DModule
        var sx = frame.leftStick.x
        var fz = frame.leftStick.y
        if (abs(sx) < 0.1f) sx = frame.dpadVector.x
        if (abs(fz) < 0.1f) fz = frame.dpadVector.y
        if (abs(sx) < 0.05f) sx = manualStrafeX
        if (abs(fz) < 0.05f) fz = manualMoveForward

        val moveSpeed = 7.5f
        val moveVec = Vec3f(sx * moveSpeed * dt, -9.81f * dt * 0.15f, -fz * moveSpeed * dt)
        heroController.move(moveVec, dt)
        val clampedX = heroEntity.transform.position.x.coerceIn(-15.5f, 15.5f)
        val clampedZ = heroEntity.transform.position.z.coerceIn(-15.5f, 15.5f)
        heroEntity.transform.setPosition(clampedX, heroEntity.transform.position.y.coerceAtLeast(0.95f), clampedZ)
        heroEntity.transform.setEulerRadians(0f, -aimYawRad, 0f)

        val moving = abs(sx) > 0.15f || abs(fz) > 0.15f
        if (fireCooldownSeconds <= 0f) {
            val targetClip = if (moving) "run" else "walk"
            if (heroRigModule.activeClip !== heroRigModule.clips[targetClip]) {
                heroRigModule.playClip(targetClip, loop = true)
            }
        }

        if ((frame.rightTrigger > 0.45f || frame.isButtonJustPressed(GamepadButton.X) ||
                frame.isButtonJustPressed(GamepadButton.RIGHT_TRIGGER_BUTTON)) && fireCooldownSeconds <= 0f
        ) {
            firePlasmaBolt()
        }

        // Patrol enemy sentinels smoothly across the arena
        for (enemy in _enemies) {
            if (enemy.defeated) continue
            enemy.patrolPhase += dt * 1.4f
            val ex = enemy.patrolCenterX + sin(enemy.patrolPhase) * enemy.patrolRadius
            val ez = enemy.patrolCenterZ + cos(enemy.patrolPhase * 0.7f) * (enemy.patrolRadius * 0.5f)
            enemy.entity.transform.setPosition(ex, 1.0f, ez)
            enemy.entity.getModule<RigidBody3DModule>()?.ensureBodyCreated()?.transform?.position?.set(ex, 1.0f, ez)
        }

        // Step active projectiles & detect enemy sphere impacts
        val pIter = _projectiles.iterator()
        while (pIter.hasNext()) {
            val proj = pIter.next()
            proj.ttlSeconds -= dt
            val px = proj.entity.transform.position.x
            val py = proj.entity.transform.position.y
            val pz = proj.entity.transform.position.z

            var consumed = proj.ttlSeconds <= 0f || py < -0.5f
            if (!consumed) {
                for (enemy in _enemies) {
                    if (enemy.defeated) continue
                    val ex = enemy.entity.transform.position.x
                    val ey = enemy.entity.transform.position.y + 0.9f
                    val ez = enemy.entity.transform.position.z
                    val dist = hypot(hypot(px - ex, pz - ez), py - ey)
                    if (dist <= 1.25f) {
                        consumed = true
                        hitsLanded++
                        enemy.hp = (enemy.hp - proj.damage).coerceAtLeast(0)
                        score += 50
                        applyArenaFeedback(0.8f, 0.95f, 180, "Target Hit: ${enemy.name}")
                        if (enemy.hp <= 0) {
                            enemy.defeated = true
                            enemy.entity.isActive = false
                            score += 150
                            if (_enemies.all { it.defeated }) {
                                if (wave < 2) {
                                    wave++
                                    spawnWaveEnemies(wave)
                                } else {
                                    victory = true
                                    score += 400
                                }
                            }
                        }
                        break
                    }
                }
            }

            if (consumed) {
                proj.entity.isActive = false
                scene.removeEntity(proj.entity)
                pIter.remove()
            }
        }

        updateAimRaycast()
    }

    private fun applyArenaFeedback(low: Float, high: Float, durationMs: Int, reason: String) {
        gamepadManager.setRumble(
            0,
            GamepadRumbleRequest(low, high, low * 0.5f, high * 0.85f, durationMs)
        )
        gamepadManager.setPlayStationLightbarColor(
            0,
            if (victory) 250 else 236,
            if (victory) 204 else 72,
            if (victory) 21 else 153
        )
        gamepadManager.setPlayStationAdaptiveTrigger(
            0,
            AdaptiveTriggerSide.RIGHT,
            AdaptiveTriggerEffect.VibrationPulse(startPosition = 0.25f, amplitude = high, frequencyHz = 36f)
        )
        gamepadManager.setPlayStationAdaptiveTrigger(
            0,
            AdaptiveTriggerSide.LEFT,
            AdaptiveTriggerEffect.ContinuousResistance(startPosition = 0.2f, force = 0.6f)
        )
        gamepadManager.setSwitchHdRumble(
            0,
            SwitchHdRumbleCommand(
                leftHighFreqHz = 340f,
                leftHighAmp = high,
                leftLowFreqHz = 160f,
                leftLowAmp = low,
                rightHighFreqHz = 440f,
                rightHighAmp = high,
                rightLowFreqHz = 180f,
                rightLowAmp = low,
                durationMs = durationMs
            )
        )
        gamepadManager.setSwitchHomeLedPattern(0, SwitchHomeLedPattern.SLOW_BREATHE)
        gamepadManager.setXboxImpulseRumble(
            0,
            XboxImpulseRumbleCommand(low, high, leftTriggerMotor = low * 0.55f, rightTriggerMotor = high * 0.9f, durationMs = durationMs)
        )
        hapticTriggerCount++
        lastHapticTelemetry = "${activeProfile.family.name}: $reason (RT=${(high * 100).roundToInt()}%)"
    }
}

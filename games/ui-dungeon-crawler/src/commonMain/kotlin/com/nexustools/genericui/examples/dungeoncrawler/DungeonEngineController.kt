package com.nexustools.genericui.examples.dungeoncrawler

import com.nexustools.genericui.ext.engine.animation.AnimationWrapMode
import com.nexustools.genericui.ext.engine.animation.EasingFunction
import com.nexustools.genericui.ext.engine.dsl.gamepadBindings
import com.nexustools.genericui.ext.engine.dsl.stageScene
import com.nexustools.genericui.ext.engine.scene.GameScene
import com.nexustools.genericui.ext.engine.svg.HumanoidSkeleton2DFactory
import com.nexustools.genericui.ext.engine.svg.SkeletalAnimator2D
import com.nexustools.genericui.ext.engine.svg.Skeleton2D
import com.nexustools.genericui.ext.engine.threading.GameLoopScheduler
import com.nexustools.genericui.ext.gamepad.AdaptiveTriggerEffect
import com.nexustools.genericui.ext.gamepad.AdaptiveTriggerSide
import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile
import com.nexustools.genericui.ext.gamepad.GamepadAxis
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
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * Turn-based roguelike simulation controller bridging `:extensions:game:engine` ([GameScene],
 * [GameLoopScheduler], [SkeletalAnimator2D]) and `:extensions:game:gamepad` ([GamepadManager]
 * profiles, adaptive triggers, HD/impulse rumble, LEDs, and gyro cursor steering).
 */
class DungeonEngineController(
    val scene: GameScene = stageScene("AbyssalCryptScene") {
        gamepadBindings {
            axis2D("move", useRightStick = false, includeDpad = true)
            button("wait_turn", GamepadButton.Y)
            button("use_potion", GamepadButton.X)
            button("toggle_pause", GamepadButton.MENU)
            button("toggle_gyro", GamepadButton.RIGHT_STICK)
            button("cycle_profile", GamepadButton.VIEW)
            axis1D("left_trigger", GamepadAxis.LEFT_TRIGGER)
            axis1D("right_trigger", GamepadAxis.RIGHT_TRIGGER)
        }
    }
) {
    val gamepadManager: GamepadManager
        get() = scene.gamepadManager

    val scheduler: GameLoopScheduler = GameLoopScheduler(
        scene = scene,
        targetTicksPerSecond = 60,
        fixedStepSeconds = 1f / 60f
    )

    val heroHeraldrySkeleton: Skeleton2D = HumanoidSkeleton2DFactory.create(scale = 0.75f)
    val heroHeraldryAnimator: SkeletalAnimator2D = SkeletalAnimator2D(heroHeraldrySkeleton).apply {
        play(HumanoidSkeleton2DFactory.createWalkClip(1.2f))
    }

    private var currentFloorData: GeneratedDungeonFloor = DungeonFloorGenerator.generateFloor(1)
    private var floorNumber: Int = 1
    private var turnCount: Int = 0
    private var score: Int = 0
    private var heroHp: Int = 36
    private var heroMaxHp: Int = 36
    private var heroAtk: Int = 6
    private var heroDef: Int = 1
    private var heroX: Int = currentFloorData.heroStartX
    private var heroY: Int = currentFloorData.heroStartY
    private var potions: Int = 1
    private var relicsCollected: Int = 0
    private var heroAlive: Boolean = true
    private var victory: Boolean = false
    private var paused: Boolean = false
    private var gyroAimCursorEnabled: Boolean = false
    private var cursorFloatX: Float = heroX.toFloat()
    private var cursorFloatY: Float = heroY.toFloat()
    private var focusMode: DungeonFocusMode = DungeonFocusMode.CUSTOM_RING_FOCUS
    private var activeProfile: ControllerHardwareProfile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE

    private val _combatLog = mutableListOf<String>()
    val combatLog: List<String> get() = _combatLog

    var lastRumbleTelemetry: String = "Rumble idle"
        private set
    var lastHardwareEffectTelemetry: String = "DualSense Cyan Lightbar • Triggers Off"
        private set
    var hapticTriggerCount: Int = 0
        private set
    var stepFlashProgress: Float = 0f
        private set

    init {
        appendLog("Entered Floor 1 of the Abyssal Crypt. Reach '>' stairs on Floor 3!")
        applyControllerFeedback(low = 0f, high = 0f, durationMs = 0, reason = "Crypt entered")
        scene.animations.playLambda(
            durationSeconds = 2.0f,
            curve = EasingFunction.SINE_IN_OUT,
            wrapMode = AnimationWrapMode.LOOP
        ) { p ->
            heroHeraldryAnimator.evaluateAtProgress(p)
            stepFlashProgress = p
        }
    }

    fun getSnapshot(): DungeonStateSnapshot = DungeonStateSnapshot(
        floor = floorNumber,
        maxFloors = DungeonFloorGenerator.MAX_FLOORS,
        turnCount = turnCount,
        score = score,
        heroHp = heroHp,
        heroMaxHp = heroMaxHp,
        heroAtk = heroAtk,
        heroDef = heroDef,
        heroX = heroX,
        heroY = heroY,
        potions = potions,
        relicsCollected = relicsCollected,
        heroAlive = heroAlive,
        victory = victory,
        paused = paused,
        gyroAimCursorEnabled = gyroAimCursorEnabled,
        cursorGridX = cursorFloatX.roundToInt().coerceIn(0, currentFloorData.width - 1),
        cursorGridY = cursorFloatY.roundToInt().coerceIn(0, currentFloorData.height - 1),
        focusMode = focusMode,
        activeProfile = activeProfile
    )

    fun tileAt(x: Int, y: Int): DungeonTileType =
        currentFloorData.tiles[y.coerceIn(0, currentFloorData.height - 1)][x.coerceIn(0, currentFloorData.width - 1)]

    fun actorAt(x: Int, y: Int): DungeonActor? =
        currentFloorData.actors.firstOrNull { !it.consumedOrDefeated && it.x == x && it.y == y }

    fun activeActors(): List<DungeonActor> =
        currentFloorData.actors.filter { !it.consumedOrDefeated }

    fun resetRun() {
        floorNumber = 1
        currentFloorData = DungeonFloorGenerator.generateFloor(1)
        turnCount = 0
        score = 0
        heroMaxHp = 36
        heroHp = heroMaxHp
        heroAtk = 6
        heroDef = 1
        heroX = currentFloorData.heroStartX
        heroY = currentFloorData.heroStartY
        potions = 1
        relicsCollected = 0
        heroAlive = true
        victory = false
        paused = false
        cursorFloatX = heroX.toFloat()
        cursorFloatY = heroY.toFloat()
        _combatLog.clear()
        appendLog("New expedition started on Floor 1.")
        applyControllerFeedback(low = 0.25f, high = 0.35f, durationMs = 110, reason = "New run")
    }

    fun setPaused(paused: Boolean) {
        this.paused = paused
        scene.clock.paused = paused
        if (paused) {
            appendLog("Expedition paused.")
        } else {
            appendLog("Expedition resumed.")
        }
    }

    fun togglePaused(): Boolean {
        setPaused(!paused)
        return paused
    }

    fun toggleGyroAimCursor(): Boolean {
        gyroAimCursorEnabled = !gyroAimCursorEnabled
        if (gyroAimCursorEnabled) {
            cursorFloatX = heroX.toFloat()
            cursorFloatY = heroY.toFloat()
            appendLog("Gyro Aim Cursor enabled — tilt controller or steer Right Stick to inspect tiles.")
        } else {
            appendLog("Gyro Aim Cursor disabled.")
        }
        return gyroAimCursorEnabled
    }

    fun toggleFocusMode(): DungeonFocusMode {
        focusMode = if (focusMode == DungeonFocusMode.CUSTOM_RING_FOCUS) {
            DungeonFocusMode.ZERO_CONFIG_SPATIAL
        } else {
            DungeonFocusMode.CUSTOM_RING_FOCUS
        }
        appendLog("Switched focus mode to ${focusMode.displayName}.")
        return focusMode
    }

    fun setControllerProfile(profile: ControllerHardwareProfile) {
        activeProfile = profile
        val current = gamepadManager.snapshot(0)
        if (current.connected && current.deviceId.startsWith("sim-")) {
            val updated = current.copy(
                deviceId = "sim-${profile.name.lowercase()}",
                deviceName = "Simulated ${profile.name}",
                profile = profile,
                playStation = if (profile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.PLAYSTATION) {
                    PlayStationControllerFeatures()
                } else {
                    null
                },
                switchFeatures = if (profile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.NINTENDO_SWITCH) {
                    SwitchControllerFeatures()
                } else {
                    null
                },
                xbox = if (profile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.XBOX) {
                    XboxControllerFeatures()
                } else {
                    null
                }
            )
            gamepadManager.setSyntheticController(updated)
            gamepadManager.poll()
        }
        applyControllerFeedback(low = 0.2f, high = 0.4f, durationMs = 90, reason = "Profile -> ${profile.name}")
        appendLog("Controller prompts switched to ${profile.name} (${profile.family.name}).")
    }

    fun cycleControllerProfile(): ControllerHardwareProfile {
        val cycleOrder = listOf(
            ControllerHardwareProfile.XBOX_ONE_SERIES,
            ControllerHardwareProfile.PLAYSTATION_DUALSENSE,
            ControllerHardwareProfile.NINTENDO_SWITCH_PRO
        )
        val idx = cycleOrder.indexOf(activeProfile)
        val next = cycleOrder[(idx + 1) % cycleOrder.size]
        setControllerProfile(next)
        return next
    }

    /**
     * Publishes a synthetic controller snapshot configured with [profile] (optional sticks/gyro) so
     * tests and desktop demos without attached USB/Bluetooth hardware can exercise every mapping.
     */
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
        val motion = SixAxisMotionState(
            gyroRadPerSecX = gyroPitchRadPerSec,
            gyroRadPerSecY = gyroYawRadPerSec,
            gyroRadPerSecZ = 0f,
            pitchRad = gyroPitchRadPerSec * 0.2f,
            yawRad = gyroYawRadPerSec * 0.2f,
            rollRad = 0f,
            isAvailable = profile.family != com.nexustools.genericui.ext.gamepad.ControllerFamily.GENERIC
        )
        val snap = GamepadFrameSnapshot(
            playerIndex = 0,
            connected = true,
            deviceId = "sim-${profile.name.lowercase()}",
            deviceName = "Simulated ${profile.name}",
            profile = profile,
            pressedButtonsMask = pressedButtonsMask,
            leftStick = com.nexustools.genericui.ext.gamepad.GamepadAxis2D(leftStickX, leftStickY),
            rightStick = com.nexustools.genericui.ext.gamepad.GamepadAxis2D(rightStickX, rightStickY),
            motion = motion,
            playStation = if (profile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.PLAYSTATION) {
                PlayStationControllerFeatures()
            } else {
                null
            },
            switchFeatures = if (profile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.NINTENDO_SWITCH) {
                SwitchControllerFeatures()
            } else {
                null
            },
            xbox = if (profile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.XBOX) {
                XboxControllerFeatures()
            } else {
                null
            }
        )
        gamepadManager.setSyntheticController(snap)
    }

    fun stepEngineTick(deltaSeconds: Float) {
        scheduler.stepManualTick(deltaSeconds)
        val frame = currentControllerFrame()
        if (frame.connected && frame.profile != activeProfile) {
            activeProfile = frame.profile
        }
        if (gyroAimCursorEnabled) {
            val gyroDx = frame.motion.gyroRadPerSecY * deltaSeconds * 3.5f
            val gyroDy = -frame.motion.gyroRadPerSecX * deltaSeconds * 3.5f
            val stickDx = frame.rightStick.x * deltaSeconds * 6.0f
            val stickDy = frame.rightStick.y * deltaSeconds * 6.0f
            cursorFloatX = (cursorFloatX + gyroDx + stickDx).coerceIn(0f, (currentFloorData.width - 1).toFloat())
            cursorFloatY = (cursorFloatY + gyroDy + stickDy).coerceIn(0f, (currentFloorData.height - 1).toFloat())
        }
        scene.vblankDriver.onBeforeFramePresent(
            frameIndex = scheduler.totalLogicTicks,
            timestampMs = scene.clock.totalElapsedSeconds * 1000.0
        )
    }

    fun currentControllerFrame(): GamepadFrameSnapshot {
        val polled = gamepadManager.snapshot(0)
        return if (polled.connected) {
            polled
        } else {
            GamepadFrameSnapshot(
                playerIndex = 0,
                connected = false,
                deviceId = "preview-${activeProfile.name.lowercase()}",
                deviceName = activeProfile.name,
                profile = activeProfile,
                playStation = if (activeProfile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.PLAYSTATION) {
                    PlayStationControllerFeatures()
                } else {
                    null
                },
                switchFeatures = if (activeProfile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.NINTENDO_SWITCH) {
                    SwitchControllerFeatures()
                } else {
                    null
                },
                xbox = if (activeProfile.family == com.nexustools.genericui.ext.gamepad.ControllerFamily.XBOX) {
                    XboxControllerFeatures()
                } else {
                    null
                }
            )
        }
    }

    fun moveHero(dx: Int, dy: Int): String {
        if (!heroAlive || victory || paused) {
            return if (paused) "Game is paused." else "Expedition already ended."
        }
        val targetX = heroX + dx.coerceIn(-1, 1)
        val targetY = heroY + dy.coerceIn(-1, 1)
        if (targetX !in 0 until currentFloorData.width || targetY !in 0 until currentFloorData.height) {
            return "Blocked by outer crypt boundary."
        }

        val targetActor = actorAt(targetX, targetY)
        if (targetActor != null && targetActor.kind.isHostile) {
            val message = strikeEnemy(targetActor)
            runEnemyTurn()
            return message
        }

        val tile = tileAt(targetX, targetY)
        if (!tile.walkable) {
            applyControllerFeedback(low = 0.25f, high = 0.05f, durationMs = 60, reason = "Wall bump")
            return "Bumped into ${tile.displayName}."
        }

        heroX = targetX
        heroY = targetY
        if (!gyroAimCursorEnabled) {
            cursorFloatX = heroX.toFloat()
            cursorFloatY = heroY.toFloat()
        }
        turnCount++
        score += 2

        if (targetActor != null) {
            collectPickup(targetActor)
        }

        when (tile) {
            DungeonTileType.SPIKE_TRAP -> {
                val trapDmg = 5
                heroHp = (heroHp - trapDmg).coerceAtLeast(0)
                appendLog("Stepped on Spike Trap for $trapDmg damage! ($heroHp/$heroMaxHp HP)")
                applyControllerFeedback(low = 0.85f, high = 0.65f, durationMs = 200, reason = "Spike Trap hit!")
                if (heroHp <= 0) {
                    heroAlive = false
                    appendLog("Hero fell to a crypt spike trap on Floor $floorNumber!")
                    return "Hero slain by Spike Trap."
                }
            }
            DungeonTileType.HEALING_SHRINE -> {
                val healed = (heroMaxHp - heroHp).coerceAtMost(8)
                heroHp += healed
                currentFloorData.tiles[heroY][heroX] = DungeonTileType.FLOOR
                appendLog("Sun Shrine restored +$healed HP ($heroHp/$heroMaxHp).")
                applyControllerFeedback(low = 0.1f, high = 0.55f, durationMs = 140, reason = "Shrine blessing")
            }
            DungeonTileType.EXIT_STAIRS -> {
                return handleExitStairs()
            }
            else -> Unit
        }

        runEnemyTurn()
        return "Moved to ($heroX, $heroY)."
    }

    fun waitTurn(): String {
        if (!heroAlive || victory || paused) return "Cannot wait right now."
        turnCount++
        appendLog("Hero braces and waits (Turn $turnCount).")
        runEnemyTurn()
        return "Waited one turn."
    }

    fun interactWithTile(x: Int, y: Int): String {
        cursorFloatX = x.toFloat()
        cursorFloatY = y.toFloat()
        val dx = x - heroX
        val dy = y - heroY
        if (abs(dx) + abs(dy) == 1) {
            return moveHero(dx, dy)
        }
        val actor = actorAt(x, y)
        val tile = tileAt(x, y)
        val detail = if (actor != null) {
            "Inspect ($x, $y): ${actor.name} (HP ${actor.hp}/${actor.maxHp}, ATK ${actor.attack})"
        } else {
            "Inspect ($x, $y): ${tile.displayName}"
        }
        appendLog(detail)
        return detail
    }

    fun usePotion(): String {
        if (!heroAlive || victory || paused) return "Cannot drink potion right now."
        if (potions <= 0) {
            appendLog("No healing potions remaining!")
            return "No potions left."
        }
        potions--
        val healed = (heroMaxHp - heroHp).coerceAtMost(16)
        heroHp += healed
        turnCount++
        appendLog("Drank potion (+$healed HP -> $heroHp/$heroMaxHp). Potions left: $potions.")
        applyControllerFeedback(low = 0.15f, high = 0.5f, durationMs = 120, reason = "Potion heal")
        runEnemyTurn()
        return "Healed +$healed HP."
    }

    fun strikeAdjacentOrWait(): String {
        val adjacentEnemy = activeActors().firstOrNull {
            it.kind.isHostile && abs(it.x - heroX) + abs(it.y - heroY) == 1
        }
        if (adjacentEnemy != null) {
            val res = strikeEnemy(adjacentEnemy)
            runEnemyTurn()
            return res
        }
        if (tileAt(heroX, heroY) == DungeonTileType.EXIT_STAIRS) {
            return handleExitStairs()
        }
        return waitTurn()
    }

    private fun strikeEnemy(enemy: DungeonActor): String {
        turnCount++
        val dmg = (heroAtk - enemy.defense).coerceAtLeast(2)
        enemy.hp = (enemy.hp - dmg).coerceAtLeast(0)
        scene.events.publish("Hero struck ${enemy.name} for $dmg damage")
        if (enemy.hp <= 0) {
            enemy.consumedOrDefeated = true
            score += enemy.xpReward
            appendLog("Slain ${enemy.name} (+${enemy.xpReward} score)!")
            applyControllerFeedback(low = 0.7f, high = 0.9f, durationMs = 190, reason = "Slain ${enemy.name}")
            if (enemy.kind == DungeonEntityKind.LICH_ADEPT) {
                victory = true
                score += 300
                appendLog("Malakor the Lich vanquished! Abyssal Crypt cleared!")
            }
            return "Slain ${enemy.name}!"
        }
        appendLog("Struck ${enemy.name} for $dmg dmg (${enemy.hp}/${enemy.maxHp} HP left).")
        applyControllerFeedback(low = 0.45f, high = 0.7f, durationMs = 130, reason = "Sword strike")
        return "Hit ${enemy.name} for $dmg."
    }

    private fun collectPickup(item: DungeonActor) {
        item.consumedOrDefeated = true
        when (item.kind) {
            DungeonEntityKind.RELIC_CHEST -> {
                relicsCollected++
                heroAtk += 2
                score += item.xpReward
                appendLog("Opened ${item.name}: ATK +2 (now $heroAtk) & +${item.xpReward} score!")
                applyControllerFeedback(low = 0.35f, high = 0.8f, durationMs = 160, reason = "Relic chest")
            }
            DungeonEntityKind.HEALTH_POTION -> {
                potions++
                score += item.xpReward
                appendLog("Collected ${item.name} (+1 potion, total $potions).")
                applyControllerFeedback(low = 0.1f, high = 0.45f, durationMs = 100, reason = "Pickup potion")
            }
            else -> Unit
        }
    }

    private fun handleExitStairs(): String {
        if (floorNumber < DungeonFloorGenerator.MAX_FLOORS) {
            floorNumber++
            currentFloorData = DungeonFloorGenerator.generateFloor(floorNumber)
            heroX = currentFloorData.heroStartX
            heroY = currentFloorData.heroStartY
            cursorFloatX = heroX.toFloat()
            cursorFloatY = heroY.toFloat()
            score += 100
            appendLog("Descended to Floor $floorNumber!")
            applyControllerFeedback(low = 0.5f, high = 0.6f, durationMs = 180, reason = "Descended Floor $floorNumber")
            return "Descended to Floor $floorNumber."
        }
        victory = true
        score += 200
        appendLog("Reached final seal stairs and escaped victorious! Final score: $score.")
        applyControllerFeedback(low = 0.6f, high = 1.0f, durationMs = 260, reason = "Victory!")
        return "Dungeon Cleared!"
    }

    private fun runEnemyTurn() {
        if (!heroAlive || victory) return
        val hostiles = activeActors().filter { it.kind.isHostile }
        for (enemy in hostiles) {
            val manhattan = abs(enemy.x - heroX) + abs(enemy.y - heroY)
            if (manhattan == 1) {
                val incoming = (enemy.attack - heroDef).coerceAtLeast(1)
                heroHp = (heroHp - incoming).coerceAtLeast(0)
                appendLog("${enemy.name} hits Hero for $incoming dmg ($heroHp/$heroMaxHp HP)!")
                applyControllerFeedback(low = 0.9f, high = 0.4f, durationMs = 180, reason = "Damaged by ${enemy.name}")
                if (heroHp <= 0) {
                    heroAlive = false
                    appendLog("Hero fell on Floor $floorNumber! Press Restart Run to try again.")
                    return
                }
            } else if (manhattan <= 4) {
                val stepX = (heroX - enemy.x).sign
                val stepY = (heroY - enemy.y).sign
                val candidates = if (abs(heroX - enemy.x) >= abs(heroY - enemy.y)) {
                    listOf(enemy.x + stepX to enemy.y, enemy.x to enemy.y + stepY)
                } else {
                    listOf(enemy.x to enemy.y + stepY, enemy.x + stepX to enemy.y)
                }
                for ((nx, ny) in candidates) {
                    if (nx in 0 until currentFloorData.width &&
                        ny in 0 until currentFloorData.height &&
                        tileAt(nx, ny).walkable &&
                        !(nx == heroX && ny == heroY) &&
                        actorAt(nx, ny) == null
                    ) {
                        enemy.x = nx
                        enemy.y = ny
                        break
                    }
                }
            }
        }
    }

    private fun applyControllerFeedback(low: Float, high: Float, durationMs: Int, reason: String) {
        if (durationMs > 0) {
            gamepadManager.setRumble(
                playerIndex = 0,
                request = GamepadRumbleRequest(
                    lowFrequency = low,
                    highFrequency = high,
                    leftTrigger = low * 0.6f,
                    rightTrigger = high * 0.7f,
                    durationMs = durationMs
                )
            )
            hapticTriggerCount++
            lastRumbleTelemetry = "Rumble L=${(low * 100).roundToInt()}% H=${(high * 100).roundToInt()}% ($reason)"
        }

        val hpRatio = heroHp.toFloat() / heroMaxHp.coerceAtLeast(1).toFloat()
        val (r, g, b) = when {
            victory -> Triple(245, 195, 25)
            !heroAlive -> Triple(220, 38, 38)
            hpRatio <= 0.35f -> Triple(239, 68, 68)
            hpRatio <= 0.65f -> Triple(245, 158, 11)
            else -> Triple(34, 211, 238)
        }
        gamepadManager.setPlayStationLightbarColor(0, r, g, b)
        val rightEffect = if (hpRatio <= 0.35f) {
            AdaptiveTriggerEffect.ContinuousResistance(startPosition = 0.2f, force = 0.85f)
        } else {
            AdaptiveTriggerEffect.SectionResistance(startPosition = 0.35f, endPosition = 0.8f, force = 0.55f)
        }
        gamepadManager.setPlayStationAdaptiveTrigger(0, AdaptiveTriggerSide.RIGHT, rightEffect)
        gamepadManager.setSwitchPlayerLeds(0, (1 shl floorNumber.coerceIn(1, 4)) - 1)
        gamepadManager.setSwitchHomeLedPattern(
            0,
            if (hpRatio <= 0.35f) SwitchHomeLedPattern.SLOW_BREATHE else SwitchHomeLedPattern.SOLID_ON
        )
        if (durationMs > 0) {
            gamepadManager.setSwitchHdRumble(
                0,
                SwitchHdRumbleCommand(
                    leftHighFreqHz = 320f,
                    leftHighAmp = high,
                    leftLowFreqHz = 160f,
                    leftLowAmp = low,
                    rightHighFreqHz = 420f,
                    rightHighAmp = high,
                    rightLowFreqHz = 180f,
                    rightLowAmp = low,
                    durationMs = durationMs
                )
            )
            gamepadManager.setXboxImpulseRumble(
                0,
                XboxImpulseRumbleCommand(
                    lowFrequencyLeft = low,
                    highFrequencyRight = high,
                    leftTriggerMotor = low * 0.5f,
                    rightTriggerMotor = high * 0.8f,
                    durationMs = durationMs
                )
            )
        }

        lastHardwareEffectTelemetry = when (activeProfile.family) {
            com.nexustools.genericui.ext.gamepad.ControllerFamily.PLAYSTATION ->
                "DualSense RGB($r,$g,$b) • Trigger=${if (hpRatio <= 0.35f) "Heavy Tension" else "Sword Detent"}"
            com.nexustools.genericui.ext.gamepad.ControllerFamily.NINTENDO_SWITCH ->
                "Switch Pro LEDs=Floor$floorNumber • HD Rumble=${(high * 100).roundToInt()}%"
            else ->
                "Xbox Impulse Rumble L=${(low * 100).roundToInt()}% RT=${(high * 80).roundToInt()}%"
        }
    }

    private fun appendLog(entry: String) {
        _combatLog.add(0, "[T$turnCount] $entry")
        if (_combatLog.size > 14) {
            _combatLog.removeAt(_combatLog.lastIndex)
        }
    }
}

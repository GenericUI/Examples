package com.nexustools.genericui.examples.arena3d

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Text
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.ext.gamepad.GamepadButton
import com.nexustools.genericui.ext.gamepad.dsl.gamepadNavigation
import com.nexustools.genericui.ext.gamepad.ui.DefaultSpatialGamepadUiNavigationStrategy
import com.nexustools.genericui.ext.gamepad.ui.GamepadUiNavigator
import kotlin.math.roundToInt

/**
 * Primary [Fragment] for Vanguard Arena 3D hosting `Canvas3D`, HUD telemetry overlay, camera/gyro/fire
 * controls, multi-family gamepad prompts, and [GamepadUiNavigator] pause menu.
 */
@MainAppFragment
class ArenaFragment(
    val controller: ArenaEngineController = ArenaEngineController()
) : Fragment() {

    lateinit var uiNavigator: GamepadUiNavigator
        private set

    val pauseNavStrategy: DefaultSpatialGamepadUiNavigationStrategy =
        DefaultSpatialGamepadUiNavigationStrategy(
            scrollSpeedPxPerSecond = 520f,
            onUnhandledCancel = {
                if (controller.getSnapshot().paused) {
                    togglePauseMenu()
                }
            }
        )

    lateinit var waveBadge: Badge private set
    lateinit var scoreBadge: Badge private set
    lateinit var enemiesBadge: Badge private set
    lateinit var cameraBadge: Badge private set
    lateinit var aimBadge: Badge private set
    lateinit var promptsBadge: Badge private set
    lateinit var hapticsBadge: Badge private set
    lateinit var gyroBtn: Button private set
    lateinit var cameraBtn: Button private set
    lateinit var pauseMenuCard: Card private set
    lateinit var pauseResumeBtn: Button private set
    lateinit var rigTelemetryText: Text private set
    private var uiBuilt: Boolean = false

    init {
        ensureUiBuilt()
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        if (!uiBuilt || children.isEmpty()) {
            uiBuilt = false
            ensureUiBuilt()
        }
    }

    override fun onDestroy() {
        if (::uiNavigator.isInitialized) {
            uiNavigator.detach()
        }
        controller.host.dispose()
        super.onDestroy()
    }

    private fun ensureUiBuilt() {
        if (uiBuilt) return
        uiBuilt = true
        clearChildren()
        addClass("arena-shell")

        val rootStack = VStack(SpacingScale.TWO).apply {
            getBootstrap().padding = SpacingScale.ONE
        }

        // Top HUD Overlay Card
        val hudCard = Card().apply {
            addClass("arena-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SM
                padding = SpacingScale.TWO
            }
        }
        val hudRow = HStack(SpacingScale.TWO).apply {
            waveBadge = Badge("Wave 1", BootstrapType.INFO, pill = true).apply { id = "badge-wave" }
            scoreBadge = Badge("Score 0", BootstrapType.WARNING, pill = true).apply { id = "badge-score" }
            enemiesBadge = Badge("Sentinels 3", BootstrapType.DANGER, pill = true).apply { id = "badge-enemies" }
            cameraBadge = Badge("Follow Cam", BootstrapType.PRIMARY, pill = true).apply { id = "badge-camera" }
            aimBadge = Badge("Aim 0deg", BootstrapType.SECONDARY, pill = true).apply { id = "badge-aim" }
            addChild(Heading("Vanguard Arena 3D", 4))
            addChild(waveBadge)
            addChild(scoreBadge)
            addChild(enemiesBadge)
            addChild(cameraBadge)
            addChild(aimBadge)
        }
        promptsBadge = Badge("Prompts", BootstrapType.DARK).apply { id = "badge-prompts" }
        hapticsBadge = Badge("Haptics ready", BootstrapType.SECONDARY).apply { id = "badge-haptics" }
        hudCard.addChild(hudRow)
        hudCard.addChild(promptsBadge)
        hudCard.addChild(hapticsBadge)
        rootStack.addChild(hudCard)

        // Three.js Canvas3D Viewport Card
        val viewportCard = Card().apply {
            addClass("arena-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.MD
                padding = SpacingScale.ONE
            }
        }
        viewportCard.addChild(controller.canvas)
        rootStack.addChild(viewportCard)

        // Action & Telemetry Strip
        val controlsCard = Card().apply {
            addClass("arena-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SM
                padding = SpacingScale.TWO
            }
        }
        val fireBtn = Button("Fire Plasma", type = BootstrapType.DANGER, size = BootstrapSize.SM).apply {
            id = "btn-fire-bolt"
            onClick = {
                controller.firePlasmaBolt()
                refreshHud()
            }
        }
        val strafeLeftBtn = Button("Strafe Left", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-strafe-left"
            onClick = {
                controller.setManualMove(-1f, 0f)
                controller.stepSimulation(1f / 30f)
                refreshHud()
            }
        }
        val advanceBtn = Button("Advance", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-advance"
            onClick = {
                controller.setManualMove(0f, 1f)
                controller.stepSimulation(1f / 30f)
                refreshHud()
            }
        }
        cameraBtn = Button("Camera: Follow", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-toggle-camera"
            onClick = {
                controller.toggleCameraMode()
                refreshHud()
            }
        }
        gyroBtn = Button("Gyro Aim: ON", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-toggle-gyro"
            onClick = {
                controller.toggleGyroAim()
                refreshHud()
            }
        }
        val cyclePadBtn = Button("Cycle Pad", type = BootstrapType.SECONDARY, size = BootstrapSize.SM).apply {
            id = "btn-profile-cycle"
            onClick = {
                controller.cycleControllerProfile()
                refreshHud()
            }
        }
        val pauseBtn = Button("Pause Menu", type = BootstrapType.WARNING, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-menu"
            onClick = { togglePauseMenu() }
        }
        val resetBtn = Button("Reset Arena", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-reset-arena"
            onClick = {
                controller.resetArena()
                refreshHud()
            }
        }
        controlsCard.addChild(HStack(SpacingScale.ONE).apply {
            addChild(fireBtn)
            addChild(strafeLeftBtn)
            addChild(advanceBtn)
            addChild(cameraBtn)
            addChild(gyroBtn)
            addChild(cyclePadBtn)
            addChild(pauseBtn)
            addChild(resetBtn)
        })
        rigTelemetryText = Text("15-Bone Skeletal Rig Active").apply { id = "text-rig-telemetry" }
        controlsCard.addChild(rigTelemetryText)
        rootStack.addChild(controlsCard)

        // Pause Modal Card navigated via GamepadUiNavigator
        pauseMenuCard = Card().apply {
            id = "pause-menu-card"
            isVisible = false
            addClass("arena-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.LG
                padding = SpacingScale.TWO
            }
        }
        pauseResumeBtn = Button("Resume Combat", type = BootstrapType.SUCCESS, size = BootstrapSize.SM).apply {
            id = "btn-pause-resume"
            onClick = {
                controller.setPaused(false)
                refreshHud()
            }
        }
        val pauseCamBtn = Button("Switch Camera", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-camera"
            onClick = {
                controller.toggleCameraMode()
                refreshHud()
            }
        }
        val pauseResetBtn = Button("Restart Arena", type = BootstrapType.DANGER, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-reset"
            onClick = {
                controller.resetArena()
                refreshHud()
            }
        }
        pauseMenuCard.addChild(Heading("Arena Paused", 5))
        pauseMenuCard.addChild(HStack(SpacingScale.ONE).apply {
            addChild(pauseResumeBtn)
            addChild(pauseCamBtn)
            addChild(pauseResetBtn)
        })
        rootStack.addChild(pauseMenuCard)

        addChild(rootStack)

        uiNavigator = gamepadNavigation {
            gamepadManager = controller.gamepadManager
            strategy = pauseNavStrategy
            autoStart = false
        }
        refreshHud()
    }

    fun togglePauseMenu() {
        val paused = controller.togglePaused()
        refreshHud()
        if (paused) {
            uiNavigator.setFocusedComponent(pauseResumeBtn)
        }
    }

    fun stepGamepadUiAndSimulation(deltaSeconds: Float = 1f / 60f) {
        if (controller.getSnapshot().paused) {
            uiNavigator.step(deltaSeconds)
        } else {
            controller.stepSimulation(deltaSeconds)
        }
        refreshHud()
    }

    fun refreshHud() {
        val snap = controller.getSnapshot()
        val frame = controller.currentControllerFrame()
        val fireLabel = frame.resolveButtonLabel(GamepadButton.RIGHT_TRIGGER_BUTTON)
        val altFireLabel = frame.resolveButtonLabel(GamepadButton.X)
        val cancelLabel = frame.resolveButtonLabel(GamepadButton.B)

        waveBadge.text = if (snap.victory) "Wave ${snap.wave} (VICTORY)" else "Wave ${snap.wave}"
        scoreBadge.text = "Score ${snap.score} | Hits ${snap.hitsLanded}/${snap.shotsFired}"
        enemiesBadge.text = "Sentinels ${snap.enemiesRemaining}"
        cameraBadge.text = if (snap.cameraMode == ArenaCameraMode.THIRD_PERSON_FOLLOW) "Follow Cam" else "Orbit Cam"
        aimBadge.text = "Aim ${snap.aimYawDeg.roundToInt()}deg | Ray=${snap.raycastTargetName}"
        promptsBadge.text =
            "${snap.activeProfile.name}: [LStick] Move • [RStick+Gyro] Aim • [$fireLabel/$altFireLabel] Fire • [$cancelLabel] Pause Back"
        hapticsBadge.text = controller.lastHapticTelemetry
        cameraBtn.text = if (snap.cameraMode == ArenaCameraMode.THIRD_PERSON_FOLLOW) "Camera: Follow" else "Camera: Orbit"
        gyroBtn.text = "Gyro Aim: ${if (snap.gyroAimEnabled) "ON" else "OFF"}"
        pauseMenuCard.isVisible = snap.paused
        val distText = snap.raycastDistanceMeters?.let { "${(it * 10f).roundToInt() / 10f}m" } ?: "none"
        rigTelemetryText.text =
            "Hero Rig Bones: ${controller.heroRigModule.skeleton?.bones?.size ?: 0} • RayDist=$distText • HeroPos=(${(snap.heroX * 10f).roundToInt() / 10f}, ${(snap.heroZ * 10f).roundToInt() / 10f})"
    }
}

package com.nexustools.genericui.examples.platformer2d

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
 * Primary [Fragment] hosting the Skyline Courier 2D SVG skeletal platformer canvas, Box2D fixed-step
 * controls, gamepad prompt/telemetry HUD, and [GamepadUiNavigator] pause menu.
 */
@MainAppFragment
class PlatformerFragment(
    val controller: PlatformerEngineController = PlatformerEngineController()
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

    lateinit var scoreBadge: Badge private set
    lateinit var coresBadge: Badge private set
    lateinit var animBadge: Badge private set
    lateinit var physicsBadge: Badge private set
    lateinit var promptsBadge: Badge private set
    lateinit var rumbleBadge: Badge private set
    lateinit var boneOverlayBtn: Button private set
    lateinit var pauseMenuCard: Card private set
    lateinit var pauseResumeBtn: Button private set
    lateinit var svgPreviewText: Text private set
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
        addClass("platformer-shell")

        val rootStack = VStack(SpacingScale.TWO).apply {
            getBootstrap().padding = SpacingScale.ONE
        }

        // Top HUD Card
        val hudCard = Card().apply {
            addClass("platformer-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SM
                padding = SpacingScale.TWO
            }
        }
        val hudRow = HStack(SpacingScale.TWO).apply {
            scoreBadge = Badge("Score 0", BootstrapType.WARNING, pill = true).apply { id = "badge-score" }
            coresBadge = Badge("Cores 0/4", BootstrapType.INFO, pill = true).apply { id = "badge-cores" }
            animBadge = Badge("IDLE", BootstrapType.PRIMARY, pill = true).apply { id = "badge-anim-state" }
            physicsBadge = Badge("Pos (160, 360)", BootstrapType.SECONDARY, pill = true).apply {
                id = "badge-physics"
            }
            addChild(Heading("Skyline Courier 2D", 4))
            addChild(scoreBadge)
            addChild(coresBadge)
            addChild(animBadge)
            addChild(physicsBadge)
        }
        promptsBadge = Badge("Prompts", BootstrapType.DARK).apply { id = "badge-prompts" }
        rumbleBadge = Badge("Haptics ready", BootstrapType.SECONDARY).apply { id = "badge-rumble" }
        hudCard.addChild(hudRow)
        hudCard.addChild(promptsBadge)
        hudCard.addChild(rumbleBadge)
        rootStack.addChild(hudCard)

        // Interactive Canvas2D Stage Card
        val stageCard = Card().apply {
            addClass("platformer-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.MD
                padding = SpacingScale.ONE
            }
        }
        stageCard.addChild(controller.canvas)
        rootStack.addChild(stageCard)

        // Simulation & Gamepad Controls Bar
        val controlCard = Card().apply {
            addClass("platformer-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SM
                padding = SpacingScale.TWO
            }
        }

        val moveLeftBtn = Button("Run Left", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-run-left"
            onClick = {
                controller.setManualMoveInput(-1f)
                controller.stepSimulation(1f / 60f)
                refreshHud()
            }
        }
        val moveRightBtn = Button("Run Right", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-run-right"
            onClick = {
                controller.setManualMoveInput(1f)
                controller.stepSimulation(1f / 60f)
                refreshHud()
            }
        }
        val stopRunBtn = Button("Coast", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-coast"
            onClick = {
                controller.setManualMoveInput(0f)
                refreshHud()
            }
        }
        val jumpBtn = Button("Jump", type = BootstrapType.SUCCESS, size = BootstrapSize.SM).apply {
            id = "btn-jump"
            onClick = {
                controller.triggerJump()
                refreshHud()
            }
        }
        val dashBtn = Button("Jet Dash", type = BootstrapType.DANGER, size = BootstrapSize.SM).apply {
            id = "btn-dash"
            onClick = {
                controller.triggerDash()
                refreshHud()
            }
        }
        boneOverlayBtn = Button("Bones: OFF", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-toggle-bones"
            onClick = {
                controller.toggleBoneOverlay()
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
        val resetBtn = Button("Reset Stage", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-reset-stage"
            onClick = {
                controller.resetStage()
                refreshHud()
            }
        }

        val btnRow = HStack(SpacingScale.ONE).apply {
            addChild(moveLeftBtn)
            addChild(moveRightBtn)
            addChild(stopRunBtn)
            addChild(jumpBtn)
            addChild(dashBtn)
            addChild(boneOverlayBtn)
            addChild(cyclePadBtn)
            addChild(pauseBtn)
            addChild(resetBtn)
        }
        svgPreviewText = Text("SVG Rig Bones: 11 | Parts: 14").apply { id = "text-svg-rig" }
        controlCard.addChild(btnRow)
        controlCard.addChild(svgPreviewText)
        rootStack.addChild(controlCard)

        // Pause Menu Card navigated by GamepadUiNavigator
        pauseMenuCard = Card().apply {
            id = "pause-menu-card"
            isVisible = false
            addClass("platformer-card")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.LG
                padding = SpacingScale.TWO
            }
        }
        pauseResumeBtn = Button("Resume Flight", type = BootstrapType.SUCCESS, size = BootstrapSize.SM).apply {
            id = "btn-pause-resume"
            onClick = {
                controller.setPaused(false)
                refreshHud()
            }
        }
        val pauseBonesBtn = Button("Toggle Bone Debug", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-bones"
            onClick = {
                controller.toggleBoneOverlay()
                refreshHud()
            }
        }
        val pauseResetBtn = Button("Restart Skyline", type = BootstrapType.DANGER, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-reset"
            onClick = {
                controller.resetStage()
                refreshHud()
            }
        }
        pauseMenuCard.addChild(Heading("Skyline Courier Paused", 5))
        pauseMenuCard.addChild(HStack(SpacingScale.ONE).apply {
            addChild(pauseResumeBtn)
            addChild(pauseBonesBtn)
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

    fun stepPauseNavigator(deltaSeconds: Float = 1f / 60f) {
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
        val jumpLbl = frame.resolveButtonLabel(GamepadButton.A)
        val dashLbl = frame.resolveButtonLabel(GamepadButton.RIGHT_BUMPER)
        val cancelLbl = frame.resolveButtonLabel(GamepadButton.B)

        scoreBadge.text = if (snap.completed) "Score ${snap.score} (COMPLETE)" else "Score ${snap.score}"
        coresBadge.text = "Cores ${snap.coresCollected}/${snap.totalCores}"
        animBadge.text = "${snap.animState.name} | Aim ${snap.aimAngleDeg.roundToInt()}deg"
        physicsBadge.text = "Pos (${snap.playerX.roundToInt()}, ${snap.playerY.roundToInt()}) Grounded=${snap.grounded}"
        promptsBadge.text =
            "${snap.activeProfile.name}: [$jumpLbl] Jump • [$dashLbl] Dash • [RStick/Gyro] TwoBoneIK Aim • [$cancelLbl] Pause Back"
        rumbleBadge.text = controller.lastRumbleTelemetry
        boneOverlayBtn.text = "Bones: ${if (snap.drawBoneOverlay) "ON" else "OFF"}"
        pauseMenuCard.isVisible = snap.paused
        svgPreviewText.text =
            "SVG Rig Bones: ${controller.playerCharacterModule.skeleton.bones.size} | Parts: ${controller.playerCharacterModule.skeleton.parts.size} | Camera: (${controller.cameraEntity.transform.position.x.roundToInt()}, ${controller.cameraEntity.transform.position.y.roundToInt()})"
    }
}

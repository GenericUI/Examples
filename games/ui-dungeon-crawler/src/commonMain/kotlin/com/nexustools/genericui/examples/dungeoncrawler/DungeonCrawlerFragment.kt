package com.nexustools.genericui.examples.dungeoncrawler

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.ScrollView
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.surface.ListGroup
import com.nexustools.genericui.component.surface.ListGroupItem
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.component.typography.Text
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FontWeight
import com.nexustools.genericui.enums.bootstrap.ShadowElevation
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.ext.engine.ui.UiStageHost
import com.nexustools.genericui.ext.gamepad.GamepadButton
import com.nexustools.genericui.ext.gamepad.dsl.gamepadNavigation
import com.nexustools.genericui.ext.gamepad.ui.DefaultSpatialGamepadUiNavigationStrategy
import com.nexustools.genericui.ext.gamepad.ui.GamepadUiNavigator
import com.nexustools.genericui.style.color.RgbaColor
import kotlin.math.roundToInt

/**
 * Primary [Fragment] rendering the turn-based Abyssal Crypt dungeon crawler using GenericUI
 * components, `:extensions:game:engine` ([UiStageHost], [com.nexustools.genericui.ext.engine.scene.GameScene]),
 * and `:extensions:game:gamepad` ([GamepadUiNavigator], zero-config + custom focus strategies,
 * multi-family prompts, haptics, and gyro aim).
 */
@MainAppFragment
class DungeonCrawlerFragment(
    val controller: DungeonEngineController = DungeonEngineController()
) : Fragment() {

    lateinit var stageHost: UiStageHost
        private set

    lateinit var uiNavigator: GamepadUiNavigator
        private set

    val customFocusStrategy: DungeonFocusNavigationStrategy = DungeonFocusNavigationStrategy(
        preferredActionIds = listOf("btn-move-up", "btn-strike", "btn-pause-resume"),
        onPauseCancelRequested = { togglePauseMenu() },
        onInspectTileRequested = { comp ->
            val cid = comp.id ?: ""
            if (cid.startsWith("tile-")) {
                val parts = cid.removePrefix("tile-").split("-")
                if (parts.size == 2) {
                    val tx = parts[0].toIntOrNull()
                    val ty = parts[1].toIntOrNull()
                    if (tx != null && ty != null) {
                        controller.interactWithTile(tx, ty)
                        refreshUi()
                    }
                }
            }
        }
    )

    val defaultSpatialStrategy: DefaultSpatialGamepadUiNavigationStrategy =
        DefaultSpatialGamepadUiNavigationStrategy(
            scrollSpeedPxPerSecond = 520f,
            onUnhandledCancel = { togglePauseMenu() }
        )

    private val tileButtons = Array(DungeonFloorGenerator.GRID_HEIGHT) {
        arrayOfNulls<Button>(DungeonFloorGenerator.GRID_WIDTH)
    }

    lateinit var floorBadge: Badge private set
    lateinit var turnBadge: Badge private set
    lateinit var hpBadge: Badge private set
    lateinit var scoreBadge: Badge private set
    lateinit var familyBadge: Badge private set
    lateinit var promptsBadge: Badge private set
    lateinit var gyroBadge: Badge private set
    lateinit var rumbleBadge: Badge private set
    lateinit var focusStatusBadge: Badge private set
    lateinit var hpProgressBar: ProgressBar private set
    lateinit var statsSummaryText: Text private set
    lateinit var actionMoveUpBtn: Button private set
    lateinit var actionStrikeBtn: Button private set
    lateinit var actionPotionBtn: Button private set
    lateinit var gyroToggleBtn: Button private set
    lateinit var focusModeBtn: Button private set
    lateinit var profileCycleBtn: Button private set
    lateinit var pauseMenuCard: Card private set
    lateinit var pauseResumeBtn: Button private set
    lateinit var combatLogList: ListGroup private set
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
        if (::stageHost.isInitialized) {
            stageHost.dispose()
        }
        super.onDestroy()
    }

    private fun ensureUiBuilt() {
        if (uiBuilt) return
        uiBuilt = true
        clearChildren()
        addClass("dungeon-shell")
        buildUi()
        refreshUi()
    }

    private fun buildUi() {
        stageHost = UiStageHost(scene = controller.scene, targetTicksPerSecond = 60)

        val rootStack = VStack(SpacingScale.TWO).apply {
            getBootstrap().padding = SpacingScale.ONE
        }

        // Top HUD Card
        val hudCard = Card().apply {
            addClass("dungeon-panel")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SM
                padding = SpacingScale.TWO
            }
        }

        val topRow = HStack(SpacingScale.TWO).apply {
            floorBadge = Badge("Floor 1/3", BootstrapType.INFO, pill = true).apply { id = "badge-floor" }
            turnBadge = Badge("Turn 0", BootstrapType.SECONDARY, pill = true).apply { id = "badge-turn" }
            hpBadge = Badge("HP 36/36", BootstrapType.SUCCESS, pill = true).apply { id = "badge-hp" }
            scoreBadge = Badge("Score 0", BootstrapType.WARNING, pill = true).apply { id = "badge-score" }
            familyBadge = Badge("PLAYSTATION_DUALSENSE", BootstrapType.PRIMARY, pill = true).apply {
                id = "badge-controller-family"
            }
            addChild(Heading("Abyssal Crypt", 4))
            addChild(floorBadge)
            addChild(turnBadge)
            addChild(hpBadge)
            addChild(scoreBadge)
            addChild(familyBadge)
        }

        hpProgressBar = ProgressBar(1.0f, label = "Hero Vitality").apply {
            id = "progress-hero-hp"
        }
        promptsBadge = Badge("Prompts", BootstrapType.DARK, pill = false).apply {
            id = "badge-prompts"
        }
        hudCard.addChild(topRow)
        hudCard.addChild(hpProgressBar)
        hudCard.addChild(promptsBadge)
        rootStack.addChild(hudCard)

        // Main Workspace Split: Grid Map (Left) + Controls & Telemetry (Right)
        val workspaceRow = HStack(SpacingScale.THREE)

        val mapCard = Card().apply {
            addClass("dungeon-panel")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.MD
                padding = SpacingScale.TWO
            }
        }
        mapCard.addChild(Label("Crypt Tactical Grid (11x9) — Click or Gamepad Navigate/Select"))

        val gridContainer = VStack(SpacingScale.ONE).apply { id = "dungeon-grid" }
        for (y in 0 until DungeonFloorGenerator.GRID_HEIGHT) {
            val row = HStack(SpacingScale.ONE)
            for (x in 0 until DungeonFloorGenerator.GRID_WIDTH) {
                val tileBtn = Button(".", type = BootstrapType.SECONDARY, size = BootstrapSize.SM).apply {
                    id = "tile-$x-$y"
                    addClass("dungeon-tile-btn")
                    getBootstrap().fontWeight = FontWeight.BOLD
                    onClick = {
                        controller.interactWithTile(x, y)
                        refreshUi()
                    }
                }
                tileButtons[y][x] = tileBtn
                row.addChild(tileBtn)
            }
            gridContainer.addChild(row)
        }
        mapCard.addChild(gridContainer)

        val sideColumn = VStack(SpacingScale.TWO)

        // Action & Pad Card
        val controlsCard = Card().apply {
            addClass("dungeon-panel")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SM
                padding = SpacingScale.TWO
            }
        }
        statsSummaryText = Text("Hero ready").apply { id = "text-hero-stats" }

        actionMoveUpBtn = Button("North", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-move-up"
            onClick = { controller.moveHero(0, -1); refreshUi() }
        }
        val moveLeftBtn = Button("West", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-move-left"
            onClick = { controller.moveHero(-1, 0); refreshUi() }
        }
        val moveDownBtn = Button("South", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-move-down"
            onClick = { controller.moveHero(0, 1); refreshUi() }
        }
        val moveRightBtn = Button("East", type = BootstrapType.PRIMARY, size = BootstrapSize.SM).apply {
            id = "btn-move-right"
            onClick = { controller.moveHero(1, 0); refreshUi() }
        }
        val dpadRow = HStack(SpacingScale.ONE).apply {
            addChild(actionMoveUpBtn)
            addChild(moveLeftBtn)
            addChild(moveDownBtn)
            addChild(moveRightBtn)
        }

        actionStrikeBtn = Button("Strike / Wait", type = BootstrapType.DANGER, size = BootstrapSize.SM).apply {
            id = "btn-strike"
            onClick = { controller.strikeAdjacentOrWait(); refreshUi() }
        }
        actionPotionBtn = Button("Potion", type = BootstrapType.SUCCESS, size = BootstrapSize.SM).apply {
            id = "btn-potion"
            onClick = { controller.usePotion(); refreshUi() }
        }
        val pauseBtn = Button("Pause Menu", type = BootstrapType.WARNING, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-menu"
            onClick = { togglePauseMenu() }
        }
        val restartBtn = Button("Restart Run", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-restart-run"
            onClick = { controller.resetRun(); refreshUi() }
        }
        val actionRow = HStack(SpacingScale.ONE).apply {
            addChild(actionStrikeBtn)
            addChild(actionPotionBtn)
            addChild(pauseBtn)
            addChild(restartBtn)
        }

        // Gamepad Strategy, Gyro Cursor & Hardware Profile Controls
        gyroToggleBtn = Button("Gyro Cursor: OFF", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-gyro-cursor"
            onClick = { controller.toggleGyroAimCursor(); refreshUi() }
        }
        focusModeBtn = Button("Focus: Custom Ring", type = BootstrapType.PRIMARY, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-focus-mode"
            onClick = { switchFocusMode(); refreshUi() }
        }
        profileCycleBtn = Button("Cycle Pad Profile", type = BootstrapType.SECONDARY, size = BootstrapSize.SM).apply {
            id = "btn-profile-cycle"
            onClick = { controller.cycleControllerProfile(); refreshUi() }
        }
        val gamepadConfigRow = HStack(SpacingScale.ONE).apply {
            addChild(gyroToggleBtn)
            addChild(focusModeBtn)
            addChild(profileCycleBtn)
        }

        gyroBadge = Badge("Cursor (1, 7) Gyro OFF", BootstrapType.SECONDARY, pill = true).apply {
            id = "badge-gyro-cursor"
        }
        rumbleBadge = Badge("Rumble idle", BootstrapType.DARK, pill = false).apply {
            id = "badge-rumble"
        }
        focusStatusBadge = Badge("Focus: ready", BootstrapType.INFO, pill = false).apply {
            id = "badge-focus-status"
        }

        controlsCard.addChild(Heading("Tactical Pad & Controller", 5))
        controlsCard.addChild(statsSummaryText)
        controlsCard.addChild(dpadRow)
        controlsCard.addChild(actionRow)
        controlsCard.addChild(gamepadConfigRow)
        controlsCard.addChild(gyroBadge)
        controlsCard.addChild(rumbleBadge)
        controlsCard.addChild(focusStatusBadge)

        // Modal Pause Menu Card showcasing GamepadUiNavigator modal/pause handling
        pauseMenuCard = Card().apply {
            id = "pause-menu-card"
            isVisible = false
            addClass("dungeon-panel")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.LARGE
                shadow = ShadowElevation.LG
                padding = SpacingScale.TWO
            }
        }
        pauseResumeBtn = Button("Resume Expedition", type = BootstrapType.SUCCESS, size = BootstrapSize.SM).apply {
            id = "btn-pause-resume"
            onClick = {
                controller.setPaused(false)
                refreshUi()
            }
        }
        val pauseGyroBtn = Button("Toggle Gyro Aim", type = BootstrapType.INFO, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-gyro"
            onClick = {
                controller.toggleGyroAimCursor()
                refreshUi()
            }
        }
        val pauseRestartBtn = Button("Restart Expedition", type = BootstrapType.DANGER, size = BootstrapSize.SM, outline = true).apply {
            id = "btn-pause-restart"
            onClick = {
                controller.resetRun()
                refreshUi()
            }
        }
        val pauseActionRow = HStack(SpacingScale.ONE).apply {
            addChild(pauseResumeBtn)
            addChild(pauseGyroBtn)
            addChild(pauseRestartBtn)
        }
        pauseMenuCard.addChild(Heading("Expedition Paused", 5))
        pauseMenuCard.addChild(Text("Use Gamepad D-Pad / Left Stick + Confirm to navigate Pause actions."))
        pauseMenuCard.addChild(pauseActionRow)

        // Combat & Hardware Log Card
        val logCard = Card().apply {
            addClass("dungeon-panel")
            getBootstrap().apply {
                borderRadius = BorderRadiusStyle.MEDIUM
                shadow = ShadowElevation.SM
                padding = SpacingScale.TWO
            }
        }
        combatLogList = ListGroup().apply { id = "list-combat-log" }
        val logScroll = ScrollView().apply {
            addChild(combatLogList)
        }
        logCard.addChild(Heading("Crypt & Telemetry Log", 5))
        logCard.addChild(logScroll)

        sideColumn.addChild(controlsCard)
        sideColumn.addChild(pauseMenuCard)
        sideColumn.addChild(logCard)
        workspaceRow.addChild(mapCard)
        workspaceRow.addChild(sideColumn)
        rootStack.addChild(workspaceRow)
        stageHost.rootContainer.addChild(rootStack)
        addChild(stageHost.rootContainer)

        // Attach GamepadUiNavigator via :extensions:game:gamepad:dsl using our custom strategy
        uiNavigator = gamepadNavigation {
            gamepadManager = controller.gamepadManager
            strategy = customFocusStrategy
            autoStart = false
        }
        uiNavigator.setFocusedComponent(actionMoveUpBtn)
    }

    fun togglePauseMenu() {
        controller.togglePaused()
        refreshUi()
        if (controller.getSnapshot().paused) {
            uiNavigator.setFocusedComponent(pauseResumeBtn)
        } else {
            uiNavigator.setFocusedComponent(actionMoveUpBtn)
        }
    }

    fun switchFocusMode(): DungeonFocusMode {
        val next = controller.toggleFocusMode()
        uiNavigator.strategy = if (next == DungeonFocusMode.CUSTOM_RING_FOCUS) {
            customFocusStrategy
        } else {
            defaultSpatialStrategy
        }
        uiNavigator.setFocusedComponent(actionMoveUpBtn)
        refreshUi()
        return next
    }

    fun stepGamepadUiAndEngine(deltaSeconds: Float = 1f / 60f) {
        if (controller.getSnapshot().paused) {
            uiNavigator.step(deltaSeconds)
        } else {
            controller.stepEngineTick(deltaSeconds)
            uiNavigator.processFrame(controller.gamepadManager.snapshot(0), deltaSeconds)
        }
        refreshUi()
    }

    fun refreshUi() {
        val snap = controller.getSnapshot()
        val frame = controller.currentControllerFrame()

        val confirmLabel = frame.resolveButtonLabel(GamepadButton.A)
        val cancelLabel = frame.resolveButtonLabel(GamepadButton.B)
        val potionLabel = frame.resolveButtonLabel(GamepadButton.X)
        val waitLabel = frame.resolveButtonLabel(GamepadButton.Y)
        val lbLabel = frame.resolveButtonLabel(GamepadButton.LEFT_BUMPER)
        val rbLabel = frame.resolveButtonLabel(GamepadButton.RIGHT_BUMPER)

        floorBadge.text = "Floor ${snap.floor}/${snap.maxFloors}"
        turnBadge.text = "Turn ${snap.turnCount}"
        hpBadge.text = "HP ${snap.heroHp}/${snap.heroMaxHp}"
        hpBadge.getBootstrap().type = when {
            !snap.heroAlive -> BootstrapType.DANGER
            snap.heroHp <= snap.heroMaxHp / 3 -> BootstrapType.WARNING
            else -> BootstrapType.SUCCESS
        }
        scoreBadge.text = "Score ${snap.score}"
        familyBadge.text = "${snap.activeProfile.name} (${snap.activeProfile.family.name})"
        promptsBadge.text =
            "[$confirmLabel] Move/Act • [$cancelLabel] Pause/Back • [$potionLabel] Potion • [$waitLabel] Wait • [$lbLabel/$rbLabel] Zone"

        hpProgressBar.progress = (snap.heroHp.toFloat() / snap.heroMaxHp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
        val stateWord = when {
            snap.victory -> "VICTORY"
            !snap.heroAlive -> "FALLEN"
            snap.paused -> "PAUSED"
            else -> "ACTIVE"
        }
        statsSummaryText.text =
            "State=$stateWord • ATK ${snap.heroAtk} • DEF ${snap.heroDef} • Potions ${snap.potions} • Relics ${snap.relicsCollected}"

        actionStrikeBtn.text = "[$confirmLabel] Strike/Wait"
        actionPotionBtn.text = "[$potionLabel] Potion (${snap.potions})"
        gyroToggleBtn.text = "Gyro Cursor: ${if (snap.gyroAimCursorEnabled) "ON" else "OFF"}"
        focusModeBtn.text = "Focus: ${if (snap.focusMode == DungeonFocusMode.CUSTOM_RING_FOCUS) "Custom Ring" else "Zero-Config"}"
        pauseMenuCard.isVisible = snap.paused

        val gyroDegYaw = (frame.motion.yawRad * 57.2958f * 10f).roundToInt() / 10f
        gyroBadge.text = "Cursor (${snap.cursorGridX}, ${snap.cursorGridY}) • Gyro ${if (snap.gyroAimCursorEnabled) "ON ${gyroDegYaw}deg" else "OFF"}"
        rumbleBadge.text = "${controller.lastRumbleTelemetry} | ${controller.lastHardwareEffectTelemetry}"
        val focusedId = uiNavigator.focusedComponent?.id ?: "none"
        focusStatusBadge.text = "Focused=#$focusedId • ${customFocusStrategy.lastNavigationEvent}"

        for (y in 0 until DungeonFloorGenerator.GRID_HEIGHT) {
            for (x in 0 until DungeonFloorGenerator.GRID_WIDTH) {
                val btn = tileButtons[y][x] ?: continue
                val tile = controller.tileAt(x, y)
                val actor = controller.actorAt(x, y)
                val isHero = x == snap.heroX && y == snap.heroY
                val isCursor = x == snap.cursorGridX && y == snap.cursorGridY

                val glyph = when {
                    isHero -> "@"
                    actor != null -> actor.kind.glyph
                    else -> tile.glyph
                }
                btn.text = if (isCursor) "[$glyph]" else glyph

                if (isCursor) {
                    btn.addClass("dungeon-cursor-target")
                } else {
                    btn.removeClass("dungeon-cursor-target")
                }

                btn.getBootstrap().type = when {
                    isHero -> BootstrapType.WARNING
                    actor != null && actor.kind.isHostile -> BootstrapType.DANGER
                    actor != null -> BootstrapType.SUCCESS
                    tile == DungeonTileType.EXIT_STAIRS -> BootstrapType.INFO
                    tile == DungeonTileType.SPIKE_TRAP -> BootstrapType.DANGER
                    tile == DungeonTileType.HEALING_SHRINE -> BootstrapType.SUCCESS
                    tile == DungeonTileType.WALL -> BootstrapType.DARK
                    else -> BootstrapType.SECONDARY
                }
                if (tile == DungeonTileType.WALL) {
                    btn.getStyle().backgroundColor = RgbaColor(30, 41, 59, 255)
                } else {
                    btn.getStyle().backgroundColor = null
                }
            }
        }

        combatLogList.clearChildren()
        for ((idx, entry) in controller.combatLog.withIndex()) {
            combatLogList.addChild(
                ListGroupItem(id = "log-entry-$idx").apply {
                    addChild(Text(entry))
                }
            )
        }
    }
}

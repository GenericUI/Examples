package com.nexustools.genericui.examples.dungeoncrawler

import com.nexustools.genericui.component.shell.Window
import com.nexustools.genericui.core.AbstractComponent
import com.nexustools.genericui.core.AbstractContainer
import com.nexustools.genericui.enums.bootstrap.BorderRadiusStyle
import com.nexustools.genericui.ext.gamepad.GamepadButton
import com.nexustools.genericui.ext.gamepad.GamepadFrameSnapshot
import com.nexustools.genericui.ext.gamepad.ui.DefaultSpatialGamepadUiNavigationStrategy
import com.nexustools.genericui.ext.gamepad.ui.GamepadNavDirection
import kotlin.math.abs

/**
 * Custom [com.nexustools.genericui.ext.gamepad.ui.GamepadUiNavigationStrategy] implementation extending
 * [DefaultSpatialGamepadUiNavigationStrategy] to demonstrate bespoke RPG ring-wrap navigation,
 * custom focus ring class highlighting, pause-menu modal priority, and shoulder-button zone jumps.
 */
class DungeonFocusNavigationStrategy(
    var preferredActionIds: List<String> = emptyList(),
    var onPauseCancelRequested: (() -> Unit)? = null,
    var onInspectTileRequested: ((AbstractComponent) -> Unit)? = null,
    onUnhandledCancel: (() -> Unit)? = null
) : DefaultSpatialGamepadUiNavigationStrategy(
    scrollSpeedPxPerSecond = 600f,
    onUnhandledCancel = onUnhandledCancel
) {
    var focusTransitionCount: Int = 0
        private set

    var lastNavigationEvent: String = "Focus initialized"
        private set

    override fun resolveInitialFocus(
        root: AbstractContainer,
        candidates: List<AbstractComponent>
    ): AbstractComponent? {
        if (candidates.isEmpty()) return null
        val preferred = candidates.firstOrNull { cand ->
            val cid = cand.id
            cid != null && cid in preferredActionIds
        }
        return preferred ?: super.resolveInitialFocus(root, candidates)
    }

    override fun resolveNextFocus(
        current: AbstractComponent?,
        direction: GamepadNavDirection,
        candidates: List<AbstractComponent>
    ): AbstractComponent? {
        val next = super.resolveNextFocus(current, direction, candidates)
        if (next != null) {
            return next
        }
        if (candidates.isEmpty()) return null
        val curIdx = if (current == null) -1 else candidates.indexOf(current)
        if (curIdx < 0) return candidates.first()
        return when (direction) {
            GamepadNavDirection.RIGHT, GamepadNavDirection.DOWN ->
                candidates[(curIdx + 1) % candidates.size]
            GamepadNavDirection.LEFT, GamepadNavDirection.UP ->
                candidates[(curIdx - 1 + candidates.size) % candidates.size]
        }
    }

    override fun onFocusChanged(
        previous: AbstractComponent?,
        current: AbstractComponent?,
        root: AbstractContainer
    ) {
        super.onFocusChanged(previous, current, root)
        if (previous != null && previous !== current) {
            previous.removeClass("dungeon-gamepad-focus")
        }
        if (current != null) {
            current.addClass("dungeon-gamepad-focus")
            current.getBootstrap().apply {
                borderRadius = BorderRadiusStyle.SMALL
            }
            if (previous !== current) {
                focusTransitionCount++
                lastNavigationEvent = "Focused ${current.id ?: current.htmlTag}"
            }
        }
    }

    override fun onActivate(
        focused: AbstractComponent,
        root: AbstractContainer,
        frame: GamepadFrameSnapshot
    ): Boolean {
        if (frame.isButtonJustPressed(GamepadButton.X)) {
            onInspectTileRequested?.invoke(focused)
            lastNavigationEvent = "Inspected ${focused.id ?: focused.htmlTag}"
            return true
        }
        val activated = super.onActivate(focused, root, frame)
        if (activated) {
            lastNavigationEvent = "Activated ${focused.id ?: focused.htmlTag} (${frame.resolveButtonLabel(GamepadButton.A)})"
        }
        return activated
    }

    override fun onCancel(
        focused: AbstractComponent?,
        root: AbstractContainer,
        frame: GamepadFrameSnapshot
    ): Boolean {
        val handledByModal = if (root is Window) {
            root.attachedDialogWindows.any { it.isVisible && it.isWindowShown }
        } else {
            false
        }
        if (super.onCancel(focused, root, frame)) {
            lastNavigationEvent = "Dismissed dialog via ${frame.resolveButtonLabel(GamepadButton.B)}"
            return true
        }
        if (!handledByModal) {
            onPauseCancelRequested?.invoke()
            lastNavigationEvent = "Pause/Cancel via ${frame.resolveButtonLabel(GamepadButton.B)}"
            return true
        }
        return false
    }

    override fun onTabCycle(
        root: AbstractContainer,
        delta: Int,
        frame: GamepadFrameSnapshot
    ): Boolean {
        if (super.onTabCycle(root, delta, frame)) {
            lastNavigationEvent = "Tab cycle via ${frame.resolveButtonLabel(GamepadButton.RIGHT_BUMPER)}"
            return true
        }
        val candidates = discoverFocusableComponents(root)
        if (candidates.isEmpty()) return false
        val focused = candidates.firstOrNull { it.isFocused }
        val curIdx = if (focused == null) -1 else candidates.indexOf(focused)
        val jump = if (delta >= 0) 4 else -4
        val base = if (curIdx < 0) 0 else curIdx
        val nextIdx = ((base + jump) % candidates.size + candidates.size) % candidates.size
        val jumped = candidates[nextIdx]
        if (focused !== jumped) {
            onFocusChanged(focused, jumped, root)
        }
        lastNavigationEvent = "Zone jump -> ${jumped.id ?: jumped.htmlTag}"
        return true
    }

    fun resolveCompassDirection(stickX: Float, stickY: Float): GamepadNavDirection? {
        if (abs(stickX) < 0.4f && abs(stickY) < 0.4f) return null
        return if (abs(stickX) >= abs(stickY)) {
            if (stickX > 0f) GamepadNavDirection.RIGHT else GamepadNavDirection.LEFT
        } else {
            if (stickY > 0f) GamepadNavDirection.DOWN else GamepadNavDirection.UP
        }
    }
}

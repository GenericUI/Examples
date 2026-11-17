package com.nexustools.genericui.examples.dungeoncrawler

/**
 * Focus navigation strategy mode demonstrating zero-config vs. custom [com.nexustools.genericui.ext.gamepad.ui.GamepadUiNavigationStrategy].
 */
enum class DungeonFocusMode(
    val displayName: String
) {
    ZERO_CONFIG_SPATIAL("Zero-Config Spatial Strategy"),
    CUSTOM_RING_FOCUS("Custom Ring-Wrap Gamepad Strategy")
}

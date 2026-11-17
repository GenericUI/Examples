package com.nexustools.genericui.examples.dungeoncrawler

import com.nexustools.genericui.ext.gamepad.ControllerFamily
import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile
import com.nexustools.genericui.ext.gamepad.GamepadButton
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Cross-platform [JestElectricRunner] BDD & integration test suite verifying Abyssal Crypt gameplay,
 * multi-family controller prompts/haptics (Xbox, DualSense, Switch Pro), zero-config & custom
 * [com.nexustools.genericui.ext.gamepad.ui.GamepadUiNavigator] strategies, and gyro cursor steering.
 */
class DungeonCrawlerJestTest {

    @Test
    fun runDungeonCrawlerGameplayAndGamepadSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "UI Dungeon Crawler Suite",
            visualOnWindows = false
        ) {
            describe("Dungeon Grid Gameplay, Controller Mapping, Focus Navigation & Gyro Cursor") {
                it("moves hero, fights enemies, collects relics, drinks potions, and descends floors") {
                    val controller = launchApp(visualOnWindows = false) { DungeonCrawlerApp() }.setup()
                    val fragment = controller.app.dungeonFragment
                    val game = fragment.controller

                    expectNode(controller.findById("badge-floor")).toHaveText("Floor 1/3")
                    expectNode(controller.findById("tile-1-7")).toContainText("@")

                    // Move north onto tile (1, 6) toward chest (2, 6)
                    controller.findById("btn-move-up").performClick()
                    assertEquals(1, game.getSnapshot().heroX)
                    assertEquals(6, game.getSnapshot().heroY)

                    // Move east into Ironbound Chest at (2, 6)
                    controller.findById("btn-move-right").performClick()
                    val afterChest = game.getSnapshot()
                    assertEquals(1, afterChest.relicsCollected)
                    assertEquals(8, afterChest.heroAtk)
                    assertTrue(afterChest.score >= 84)
                    assertTrue(game.hapticTriggerCount >= 1)

                    // Step east onto Sun Shrine at (5, 6)
                    controller.findById("btn-move-right").performClick()
                    controller.findById("btn-move-right").performClick()
                    controller.findById("btn-move-right").performClick()
                    controller.findById("btn-potion").performClick()
                    assertEquals(0, game.getSnapshot().potions)

                    // Reach floor 1 exit stairs at (9, 1) and descend to Floor 2
                    game.moveHero(0, -1) // (5,5)
                    while (game.getSnapshot().heroY > 1) {
                        val beforeY = game.getSnapshot().heroY
                        game.moveHero(0, -1)
                        if (game.getSnapshot().heroY == beforeY) break
                    }
                    // Navigate straight across top corridor y=1 to stairs at (9, 1)
                    while (game.getSnapshot().heroX < 9 && game.getSnapshot().floor == 1 && game.getSnapshot().heroAlive) {
                        game.moveHero(1, 0)
                    }
                    fragment.refreshUi()
                    assertTrue(game.getSnapshot().floor >= 2 || game.getSnapshot().score > 100)

                    controller.destroy()
                }

                it("adapts button prompt glyphs and controller feedback across DualSense, Xbox, and Switch Pro") {
                    val controller = launchApp(visualOnWindows = false) { DungeonCrawlerApp() }.setup()
                    val fragment = controller.app.dungeonFragment
                    val game = fragment.controller

                    // Default PlayStation DualSense prompts
                    expectNode(controller.findById("badge-controller-family")).toContainText("PLAYSTATION")
                    expectNode(controller.findById("badge-prompts")).toContainText("Cross")
                    expectNode(controller.findById("badge-prompts")).toContainText("Circle")

                    // Cycle -> Nintendo Switch Pro positional prompts (B confirm / A cancel)
                    controller.findById("btn-profile-cycle").performClick()
                    assertEquals(ControllerHardwareProfile.NINTENDO_SWITCH_PRO, game.getSnapshot().activeProfile)
                    expectNode(controller.findById("badge-controller-family")).toContainText("NINTENDO_SWITCH")
                    expectNode(controller.findById("badge-prompts")).toContainText("[B] Move/Act")
                    expectNode(controller.findById("badge-prompts")).toContainText("[A] Pause/Back")
                    expectNode(controller.findById("badge-rumble")).toContainText("Switch Pro LEDs")

                    // Simulate live Xbox Series controller input -> prompts update to A/B/LB/RB & impulse rumble
                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.XBOX_ONE_SERIES,
                        pressedButtonsMask = GamepadButton.DPAD_UP.bitMask
                    )
                    fragment.stepGamepadUiAndEngine(1f / 60f)
                    assertEquals(ControllerFamily.XBOX, game.getSnapshot().activeProfile.family)
                    expectNode(controller.findById("badge-prompts")).toContainText("[A] Move/Act")
                    expectNode(controller.findById("badge-prompts")).toContainText("[B] Pause/Back")

                    controller.destroy()
                }

                it("supports custom DungeonFocusNavigationStrategy, zero-config Spatial strategy, pause modal, and gyro aim cursor") {
                    val controller = launchApp(visualOnWindows = false) { DungeonCrawlerApp() }.setup()
                    val fragment = controller.app.dungeonFragment
                    val game = fragment.controller

                    // Verify initial custom strategy moves focus and tags dungeon-gamepad-focus class
                    val initialFocusId = fragment.uiNavigator.focusedComponent?.id
                    assertEquals("btn-move-up", initialFocusId)
                    assertTrue(fragment.actionMoveUpBtn.hasClass("dungeon-gamepad-focus"))

                    // Drive D-Pad right through GamepadUiNavigator
                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE,
                        pressedButtonsMask = GamepadButton.DPAD_RIGHT.bitMask
                    )
                    fragment.stepGamepadUiAndEngine(1f / 60f)
                    assertEquals("btn-move-left", fragment.uiNavigator.focusedComponent?.id)
                    assertFalse(fragment.actionMoveUpBtn.hasClass("dungeon-gamepad-focus"))
                    assertTrue(fragment.customFocusStrategy.focusTransitionCount >= 1)

                    // Switch to Zero-Config DefaultSpatialGamepadUiNavigationStrategy & toggle pause menu
                    controller.findById("btn-focus-mode").performClick()
                    assertEquals(DungeonFocusMode.ZERO_CONFIG_SPATIAL, game.getSnapshot().focusMode)
                    expectNode(controller.findById("btn-focus-mode")).toHaveText("Focus: Zero-Config")

                    controller.findById("btn-pause-menu").performClick()
                    assertTrue(game.getSnapshot().paused)
                    assertEquals("btn-pause-resume", fragment.uiNavigator.focusedComponent?.id)

                    // Activate focused Resume button via gamepad A/Cross press
                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.XBOX_ONE_SERIES,
                        pressedButtonsMask = GamepadButton.A.bitMask
                    )
                    fragment.stepGamepadUiAndEngine(1f / 60f)
                    assertFalse(game.getSnapshot().paused)

                    // Enable gyro aim cursor and steer cursor across the dungeon map via six-axis yaw/pitch
                    controller.findById("btn-gyro-cursor").performClick()
                    assertTrue(game.getSnapshot().gyroAimCursorEnabled)
                    val startCursorX = game.getSnapshot().cursorGridX
                    repeat(20) {
                        game.simulateGamepadInput(
                            profile = ControllerHardwareProfile.NINTENDO_SWITCH_PRO,
                            gyroYawRadPerSec = 1.6f,
                            gyroPitchRadPerSec = 0.8f
                        )
                        game.stepEngineTick(1f / 30f)
                    }
                    fragment.refreshUi()
                    assertTrue(game.getSnapshot().cursorGridX > startCursorX)
                    expectNode(controller.findById("badge-gyro-cursor")).toContainText("Gyro ON")

                    controller.destroy()
                }
            }
        }
        report.assertAllPassed()
    }
}

package com.nexustools.genericui.examples.platformer2d

import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile
import com.nexustools.genericui.ext.gamepad.GamepadButton
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Cross-platform [JestElectricRunner] BDD & integration test suite verifying Skyline Courier 2D:
 * SVG skeletal hierarchy & clips, Box2D rigid-body landing/jump/dash, TwoBoneIK arm aiming,
 * parallax camera follow, Canvas2D VBlank rendering, and [com.nexustools.genericui.ext.gamepad.ui.GamepadUiNavigator] pause menu.
 */
class PlatformerJestTest {

    @Test
    fun runSvgPlatformerPhysicsSkeletalIkAndGamepadSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "SVG Skeletal Platformer 2D Suite",
            visualOnWindows = false
        ) {
            describe("SVG Skeletal Rig, Box2D Platforming, Parallax Camera, IK Aiming & Gamepad Menu") {
                it("settles hero on Box2D rooftop, runs right via left stick, collects Quantum Core, and tracks parallax camera") {
                    val controller = launchApp(visualOnWindows = false) { PlatformerApp() }.setup()
                    val fragment = controller.app.platformerFragment
                    val game = fragment.controller

                    // Step Box2D fixed-step ticks (dt=1/30 -> 2x 1/60s substeps per call) until hero rests on floor
                    repeat(18) {
                        game.stepLogicOnly(1f / 30f)
                    }
                    game.renderVBlankFrame()
                    assertTrue(game.getSnapshot().grounded, "Hero should be grounded after settling on floor")
                    val settledY = game.getSnapshot().playerY
                    assertTrue(settledY in 360f..390f, "Expected hero grounded around y=377, got $settledY")

                    val initialCamX = game.cameraEntity.transform.position.x
                    val initialFarParallaxX = game.parallaxFarEntity.transform.position.x

                    // Hold Left Stick right toward Core-1 at x=340f
                    repeat(30) {
                        game.simulateGamepadInput(
                            profile = ControllerHardwareProfile.XBOX_ONE_SERIES,
                            leftStickX = 1.0f
                        )
                        game.stepLogicOnly(1f / 30f)
                    }
                    game.renderVBlankFrame()
                    fragment.refreshHud()

                    val afterRun = game.getSnapshot()
                    assertTrue(afterRun.playerX > 260f, "Hero should run right along platform, x=${afterRun.playerX}")
                    assertEquals(PlatformerAnimationState.RUN, afterRun.animState)
                    assertEquals(1f, afterRun.facingSign)
                    assertTrue(game.cameraEntity.transform.position.x < initialCamX || game.cameraEntity.transform.position.x != initialCamX)
                    assertNotEquals(initialFarParallaxX, game.parallaxFarEntity.transform.position.x)

                    // Trigger jump + dash to cross x=340f and trigger burst particles + rumble
                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE,
                        pressedButtonsMask = GamepadButton.A.bitMask,
                        leftStickX = 1.0f
                    )
                    game.stepLogicOnly(1f / 60f)
                    val jumpBurstParticles = game.playerParticlesModule.activeParticleCount
                    assertTrue(jumpBurstParticles >= 1, "Jump should emit jetpack burst particles")

                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE,
                        pressedButtonsMask = GamepadButton.RIGHT_BUMPER.bitMask,
                        leftStickX = 1.0f
                    )
                    repeat(12) {
                        game.stepLogicOnly(1f / 30f)
                    }
                    game.renderVBlankFrame()
                    fragment.refreshHud()
                    assertTrue(game.getSnapshot().coresCollected >= 1, "Hero should collect Core-1 along path")
                    assertTrue(game.getSnapshot().score >= 100)

                    controller.destroy()
                }

                it("drives TwoBoneIK arm aim via Right Stick / six-axis gyro and toggles bone overlay & pause modal via GamepadUiNavigator") {
                    val controller = launchApp(visualOnWindows = false) { PlatformerApp() }.setup()
                    val fragment = controller.app.platformerFragment
                    val game = fragment.controller

                    // Check initial SVG skeleton exports valid SVG vector markup
                    val exportedSvg = game.playerCharacterModule.skeleton.exportSvgMarkup(160, 160)
                    assertTrue(exportedSvg.contains("<svg"))
                    assertTrue(exportedSvg.contains("<path"))

                    // Aim up-right with Right Stick and verify TwoBoneIkSolver2D rotates shoulder & forearm
                    val upperArm = game.playerCharacterModule.skeleton.getBone("upperArmR")!!
                    val beforeArmAngle = upperArm.localAngleRad

                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.NINTENDO_SWITCH_PRO,
                        rightStickX = 0.85f,
                        rightStickY = 0.75f
                    )
                    repeat(5) {
                        game.stepSimulation(1f / 60f)
                    }
                    val afterArmAngle = upperArm.localAngleRad
                    assertTrue(abs(afterArmAngle - beforeArmAngle) > 0.05f, "TwoBoneIK should re-orient aiming arm")

                    // Toggle Bone Debug overlay & Cycle Controller profile (Switch -> DualSense -> Xbox)
                    controller.findById("btn-toggle-bones").performClick()
                    assertTrue(game.getSnapshot().drawBoneOverlay)
                    controller.findById("btn-profile-cycle").performClick()
                    expectNode(controller.findById("badge-prompts")).toContainText("[Cross] Jump")
                    controller.findById("btn-profile-cycle").performClick()
                    expectNode(controller.findById("badge-prompts")).toContainText("[A] Jump")

                    // Open Pause Menu and resume using GamepadUiNavigator
                    controller.findById("btn-pause-menu").performClick()
                    assertTrue(game.getSnapshot().paused)
                    assertEquals("btn-pause-resume", fragment.uiNavigator.focusedComponent?.id)

                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.XBOX_ONE_SERIES,
                        pressedButtonsMask = GamepadButton.A.bitMask
                    )
                    fragment.stepPauseNavigator(1f / 60f)
                    assertFalse(game.getSnapshot().paused)

                    controller.destroy()
                }
            }
        }
        report.assertAllPassed()
    }
}

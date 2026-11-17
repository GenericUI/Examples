package com.nexustools.genericui.examples.arena3d

import com.nexustools.genericui.ext.canvas3d.Canvas3DRegistry
import com.nexustools.genericui.ext.gamepad.ControllerHardwareProfile
import com.nexustools.genericui.ext.gamepad.GamepadButton
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Cross-platform [JestElectricRunner] BDD & integration suite verifying Vanguard Arena 3D:
 * 15-bone [com.nexustools.genericui.ext.engine3d.ProceduralHumanoidRig3D] + additive bone overrides,
 * Box3D character controller & raycast + projectile impacts, Follow/Orbit camera switching,
 * dual-stick + six-axis gyro aim, multi-family haptics, and [com.nexustools.genericui.ext.gamepad.ui.GamepadUiNavigator] pause menu.
 */
class ArenaJestTest {

    @BeforeTest
    fun setUpCanvasSupport() {
        Canvas3DRegistry.overrideAllSupportForTesting()
    }

    @AfterTest
    fun tearDownCanvasSupport() {
        Canvas3DRegistry.resetForTesting()
    }

    @Test
    fun runSkeletalArena3DCombatPhysicsAndGamepadSuite() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Skeletal Arena 3D Suite",
            visualOnWindows = false
        ) {
            describe("3D Skeletal Rig, Box3D Combat Physics, Gyro Aim, Haptics & Pause UI") {
                it("aims toward Sentinel Bot with dual-stick/gyro, fires Box3D plasma bolt, scores hits, and drives additive bone overrides") {
                    val controller = launchApp(visualOnWindows = false) { ArenaApp() }.setup()
                    val fragment = controller.app.arenaFragment
                    val game = fragment.controller

                    assertEquals(3, game.getSnapshot().enemiesRemaining)
                    assertEquals(15, game.heroRigModule.skeleton?.bones?.size)

                    // Aim straight toward Vanguard Drone in wave 1 using exact delta yaw
                    val initialHeroSnap = game.getSnapshot()
                    val targetEnemy = game.enemies.first { !it.defeated }
                    val ePos = targetEnemy.entity.transform.position
                    val yawToEnemy = atan2(ePos.x - initialHeroSnap.heroX, initialHeroSnap.heroZ - ePos.z)
                    game.adjustAim(yawToEnemy, -0.02f)

                    // Fire via Right Trigger / DualSense input and step physics simulation
                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE,
                        rightTrigger = 0.9f,
                        gyroYawRadPerSec = 0.0f
                    )
                    game.stepSimulation(1f / 60f)
                    assertTrue(game.getSnapshot().shotsFired >= 1)
                    assertTrue(game.hapticTriggerCount >= 1)
                    assertTrue(game.boneOverrideEvaluations >= 1)

                    // Release trigger & advance simulation so projectile closes distance or hero runs toward arena center
                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.PLAYSTATION_DUALSENSE,
                        leftStickY = 1.0f,
                        rightTrigger = 0.0f
                    )
                    repeat(25) {
                        game.stepLogicOnly(1f / 60f)
                    }
                    game.renderVBlankFrame()
                    fragment.refreshHud()
                    assertTrue(game.getSnapshot().heroZ < initialHeroSnap.heroZ, "CharacterController3D should advance hero into arena")

                    // Exercise gyro aiming & verify bone orientation changes procedurally
                    val initialArmRotX = game.heroRigModule.getBone("upperArmR")!!.rotation.x
                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.NINTENDO_SWITCH_PRO,
                        gyroPitchRadPerSec = 1.5f,
                        gyroYawRadPerSec = 1.2f
                    )
                    repeat(10) {
                        game.stepLogicOnly(1f / 60f)
                    }
                    game.renderVBlankFrame()
                    val updatedArmRotX = game.heroRigModule.getBone("upperArmR")!!.rotation.x
                    assertTrue(abs(updatedArmRotX - initialArmRotX) > 0.02f)
                    assertNotEquals(0f, game.getSnapshot().aimYawDeg)

                    controller.destroy()
                }

                it("switches Follow/Orbit cameras, cycles DualSense/Xbox/Switch prompts, and resumes from Pause via GamepadUiNavigator") {
                    val controller = launchApp(visualOnWindows = false) { ArenaApp() }.setup()
                    val fragment = controller.app.arenaFragment
                    val game = fragment.controller

                    assertEquals(ArenaCameraMode.THIRD_PERSON_FOLLOW, game.getSnapshot().cameraMode)
                    controller.findById("btn-toggle-camera").performClick()
                    assertEquals(ArenaCameraMode.ORBIT_TACTICAL, game.getSnapshot().cameraMode)
                    expectNode(controller.findById("badge-camera")).toHaveText("Orbit Cam")

                    // Cycle profile -> Xbox Impulse triggers & Xbox button prompts
                    controller.findById("btn-profile-cycle").performClick()
                    assertEquals(ControllerHardwareProfile.XBOX_ONE_SERIES, game.getSnapshot().activeProfile)
                    expectNode(controller.findById("badge-prompts")).toContainText("[RT/X] Fire")

                    // Fire UI Plasma Bolt and verify direct score/shot increment + haptics
                    controller.findById("btn-fire-bolt").performClick()
                    assertTrue(game.getSnapshot().shotsFired >= 1)
                    expectNode(controller.findById("badge-haptics")).toContainText("XBOX")

                    // Open Pause Menu and confirm Resume via GamepadUiNavigator
                    controller.findById("btn-pause-menu").performClick()
                    assertTrue(game.getSnapshot().paused)
                    assertEquals("btn-pause-resume", fragment.uiNavigator.focusedComponent?.id)

                    game.simulateGamepadInput(
                        profile = ControllerHardwareProfile.XBOX_ONE_SERIES,
                        pressedButtonsMask = GamepadButton.A.bitMask
                    )
                    fragment.stepGamepadUiAndSimulation(1f / 60f)
                    assertFalse(game.getSnapshot().paused)

                    controller.destroy()
                }
            }
        }
        report.assertAllPassed()
    }
}

package com.nexustools.genericui.examples.physics2d

import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.css.CssCursor
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Cross-platform unit and UI component tests for [Box2DPhysicsPlaygroundApp] and
 * [Box2DPhysicsPlaygroundFragment] powered by [JestElectricRunner].
 */
class Box2DPhysicsPlaygroundJestTest {

    @Test
    fun box2DPhysicsWorldAndDebugDrawerSuitePasses() {
        val report = JestElectricRunner.runSuite(
            suiteName = "Box2DPhysicsPlayground Jest Suite",
            visualOnWindows = false
        ) {
            describe("Initial Pyramid Stack scene") {
                it("mounts controls, verifies getStyle() and getBootstrap(), and creates static boundaries plus 6 pyramid boxes") {
                    val controller = launchApp(visualOnWindows = false) { Box2DPhysicsPlaygroundApp() }
                    val fragment = controller.app.physicsFragment

                    expectNode(controller.findById("lbl-title")).toHaveText("Box2D Physics Playground")
                    expectNode(controller.findById("badge-scene-preset")).toHaveText("Scene: Pyramid Stack")
                    expect(fragment.presetBadge.getBootstrap().type).toBe(BootstrapType.PRIMARY)
                    expect(fragment.arenaSurface.getStyle().getCursor()).toBe(CssCursor.CROSSHAIR)
                    expect(fragment.arenaSurface.getStyle().borderRadiusPx).toBe(8f)
                    assertTrue(fragment.arenaSurface.hasClass("physics-arena-canvas"))
                    // 3 static boundary walls + 6 dynamic pyramid crates = 9 bodies
                    expect(fragment.world.bodies.size).toBe(9)
                    expect(fragment.dynamicBodies().size).toBe(6)
                    expectNode(controller.findById("badge-body-count")).toHaveText("Bodies: 9")
                }
            }

            describe("Spawning bodies and stepping rigid-body gravity simulation") {
                it("spawns dynamic boxes and circles and descends under gravity until resting on floor") {
                    val controller = launchApp(visualOnWindows = false) { Box2DPhysicsPlaygroundApp() }
                    val fragment = controller.app.physicsFragment

                    val spawnedCircle = fragment.spawnDynamicCircle(x = 0f, y = 2.8f, radius = 0.35f)
                    val initialY = spawnedCircle.position.y
                    expect(fragment.world.bodies.size).toBe(10)

                    // Step 45 frames via UI button + direct stepping and verify vertical descent
                    controller.findById("btn-step-world").performClick()
                    fragment.stepSimulation(steps = 44)

                    expect(fragment.totalStepCount >= 45).toBe(true)
                    expect(spawnedCircle.position.y < initialY - 0.5f).toBe(true)
                    // Floor top is at y = -3.2 + 0.35 = -2.85, so body stays above floor
                    expect(spawnedCircle.position.y > -3.0f).toBe(true)

                    // Verify debug drawer renders non-empty pixels to the Canvas2DExtSurface
                    fragment.arenaSurface.dispatchVBlankFrame(16.0)
                    val buffer = fragment.arenaSurface.toImageBuffer()
                    expect(buffer.width).toBe(560)
                    expect(buffer.height).toBe(380)
                    expect(buffer.getPixel(280, 190).a).toBe(255)
                }

                it("switches scene presets, creates revolute joints, and supports pause/resume and gravity slider") {
                    val controller = launchApp(visualOnWindows = false) { Box2DPhysicsPlaygroundApp() }
                    val fragment = controller.app.physicsFragment

                    controller.findById("btn-preset-pendulum").performClick()
                    expectNode(controller.findById("badge-scene-preset")).toHaveText("Scene: Pendulum Cradle")
                    expect(fragment.presetBadge.getBootstrap().type).toBe(BootstrapType.INFO)
                    expect(fragment.world.joints.size).toBe(1)

                    controller.findById("slider-gravity").performSetSliderValue(-4.5)
                    expect(fragment.world.gravity.y).toBe(-4.5f)

                    // Pause prevents VBlank frame from advancing step count
                    controller.findById("btn-toggle-pause").performClick()
                    expect(fragment.isPaused).toBe(true)
                    expect(fragment.pauseResumeButton.getBootstrap().type).toBe(BootstrapType.SUCCESS)
                    expect(fragment.pauseResumeButton.getBootstrap().outline).toBe(false)
                    val stepsBeforeVBlank = fragment.totalStepCount
                    fragment.arenaSurface.dispatchVBlankFrame(32.0)
                    expect(fragment.totalStepCount).toBe(stepsBeforeVBlank)

                    // Resume allows VBlank frame to advance step count
                    controller.findById("btn-toggle-pause").performClick()
                    expect(fragment.isPaused).toBe(false)
                    expect(fragment.pauseResumeButton.getBootstrap().type).toBe(BootstrapType.WARNING)
                    expect(fragment.pauseResumeButton.getBootstrap().outline).toBe(true)
                    fragment.arenaSurface.dispatchVBlankFrame(48.0)
                    expect(fragment.totalStepCount).toBe(stepsBeforeVBlank + 1)
                }
            }
        }

        report.assertAllPassed()
    }
}

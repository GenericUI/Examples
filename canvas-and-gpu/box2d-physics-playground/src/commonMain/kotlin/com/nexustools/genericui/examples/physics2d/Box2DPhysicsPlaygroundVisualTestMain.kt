package com.nexustools.genericui.examples.physics2d

import com.nexustools.genericui.tests.JestElectricRunner
import com.nexustools.genericui.tests.JestSuiteReport

/**
 * Interactive visual test runner entry point that opens a real platform window
 * on Windows / JVM and steps through rigid-body physics scenes live.
 */
object Box2DPhysicsPlaygroundVisualTestMain {

    fun runVisualSuite(args: Array<String> = emptyArray()): JestSuiteReport =
        JestElectricRunner.runVisualAppTestMain(
            suiteName = "Box2D Physics Playground Visual Suite",
            args = args
        ) {
            describe("Interactive Rigid-Body Spawning, Presets, and VBlank Stepping") {
                it("spawns dynamic bodies and steps through Pyramid, Pendulum, and Bouncy scenes") {
                    val controller = launchApp { Box2DPhysicsPlaygroundApp() }
                    val fragment = controller.app.physicsFragment

                    controller.findById("btn-spawn-box").performClick()
                    controller.findById("btn-spawn-circle").performClick()

                    repeat(12) { frame ->
                        fragment.arenaSurface.dispatchVBlankFrame(frame * 16.6)
                    }

                    controller.findById("btn-preset-pendulum").performClick()
                    expectNode(controller.findById("badge-scene-preset")).toHaveText("Scene: Pendulum Cradle")
                    repeat(12) { frame ->
                        fragment.arenaSurface.dispatchVBlankFrame((frame + 12) * 16.6)
                    }

                    controller.findById("btn-preset-bouncy").performClick()
                    expectNode(controller.findById("badge-scene-preset")).toHaveText("Scene: Bouncy Cascade")
                    repeat(12) { frame ->
                        fragment.arenaSurface.dispatchVBlankFrame((frame + 24) * 16.6)
                    }
                }
            }
        }
}

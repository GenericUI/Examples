package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ext.canvas2d.swing.SwingCanvas2DProvider
import com.nexustools.genericui.swing.SwingNativeFactory

actual object MdiShowcasePlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        SwingCanvas2DProvider.install()
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(SwingNativeFactory(), "MdiComponentAndScssShowcase")
        }
    }
}

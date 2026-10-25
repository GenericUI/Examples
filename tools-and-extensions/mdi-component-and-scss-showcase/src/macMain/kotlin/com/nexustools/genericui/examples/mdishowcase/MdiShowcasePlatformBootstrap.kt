package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtension
import com.nexustools.genericui.ext.canvas2d.desktop.DesktopCanvas2DProvider
import com.nexustools.genericui.macos.MacosCanvas2DNativeImpl
import com.nexustools.genericui.macos.MacosNativeFactory

actual object MdiShowcasePlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        DesktopCanvas2DProvider.install("CoreGraphics (CGContextRef) + CoreText")
        Canvas2DExtension.registerCanvas2DFactory { _, _ -> MacosCanvas2DNativeImpl() }
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(MacosNativeFactory(), "MdiComponentAndScssShowcase")
        }
    }
}

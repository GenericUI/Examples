package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtension
import com.nexustools.genericui.ext.canvas2d.desktop.DesktopCanvas2DProvider
import com.nexustools.genericui.windows.WindowsApplicationNativeImpl
import com.nexustools.genericui.windows.WindowsCanvas2DNativeImpl
import com.nexustools.genericui.windows.WindowsNativeFactory

actual object MdiShowcasePlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        DesktopCanvas2DProvider.install("Direct2D 1.1 + DirectWrite")
        Canvas2DExtension.registerCanvas2DFactory { _, _ -> WindowsCanvas2DNativeImpl() }
        if (GenericUI.activeFactory == null) {
            WindowsApplicationNativeImpl.interactiveWin32GuiEnabled = !args.contains("--headless-verify")
            GenericUI.initialize(WindowsNativeFactory(), "MdiComponentAndScssShowcase")
        }
    }
}

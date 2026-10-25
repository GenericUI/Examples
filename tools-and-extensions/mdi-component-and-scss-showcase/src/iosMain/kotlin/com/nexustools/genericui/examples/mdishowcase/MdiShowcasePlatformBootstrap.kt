package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtension
import com.nexustools.genericui.ext.canvas2d.mobile.MobileCanvas2DProvider
import com.nexustools.genericui.ios.IosCanvas2DImpl
import com.nexustools.genericui.ios.IosNativeFactory

actual object MdiShowcasePlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        MobileCanvas2DProvider.install()
        Canvas2DExtension.registerCanvas2DFactory { _, _ -> IosCanvas2DImpl() }
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(IosNativeFactory(), "MdiComponentAndScssShowcase")
        }
    }
}

package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.android.AndroidCanvas2DImpl
import com.nexustools.genericui.android.AndroidNativeFactory
import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtension
import com.nexustools.genericui.ext.canvas2d.mobile.MobileCanvas2DProvider

actual object MdiShowcasePlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        MobileCanvas2DProvider.install()
        Canvas2DExtension.registerCanvas2DFactory { _, _ -> AndroidCanvas2DImpl() }
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(AndroidNativeFactory(), "MdiComponentAndScssShowcase")
        }
    }
}

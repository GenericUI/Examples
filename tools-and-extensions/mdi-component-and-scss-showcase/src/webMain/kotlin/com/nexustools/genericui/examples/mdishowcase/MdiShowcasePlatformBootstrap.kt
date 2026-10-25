package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ext.canvas2d.web.WebCanvas2DProvider
import com.nexustools.genericui.web.WebNativeFactory

actual object MdiShowcasePlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        WebCanvas2DProvider.install()
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(WebNativeFactory(), "MdiComponentAndScssShowcase")
        }
    }
}

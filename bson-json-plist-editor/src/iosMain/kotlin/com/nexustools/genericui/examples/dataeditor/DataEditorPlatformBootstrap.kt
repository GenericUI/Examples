package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ios.IosNativeFactory

actual object DataEditorPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(IosNativeFactory(), "BsonJsonPlistEditor")
        }
    }
}

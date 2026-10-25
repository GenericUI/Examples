package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.swing.SwingNativeFactory

actual object DataEditorPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(SwingNativeFactory(), "BsonJsonPlistEditor")
        }
    }
}

package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.android.AndroidNativeFactory
import com.nexustools.genericui.core.GenericUI

actual object DataEditorPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(AndroidNativeFactory(), "BsonJsonPlistEditor")
        }
    }
}

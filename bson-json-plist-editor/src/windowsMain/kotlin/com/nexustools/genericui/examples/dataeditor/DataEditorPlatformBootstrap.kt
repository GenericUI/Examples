package com.nexustools.genericui.examples.dataeditor

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.windows.WindowsApplicationNativeImpl
import com.nexustools.genericui.windows.WindowsNativeFactory

actual object DataEditorPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        WindowsApplicationNativeImpl.interactiveWin32GuiEnabled = !args.contains("--headless-verify")
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(WindowsNativeFactory(), "BsonJsonPlistEditor")
        }
    }
}

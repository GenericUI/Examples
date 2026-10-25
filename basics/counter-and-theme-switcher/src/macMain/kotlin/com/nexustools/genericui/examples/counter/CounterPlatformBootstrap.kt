package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.macos.MacosNativeFactory

actual object CounterPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(MacosNativeFactory(), "CounterThemeExample")
        }
    }
}

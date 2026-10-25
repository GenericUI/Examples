package com.nexustools.genericui.examples.counter

import com.nexustools.genericui.android.AndroidNativeFactory
import com.nexustools.genericui.core.GenericUI

actual object CounterPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(AndroidNativeFactory(), "CounterThemeExample")
        }
    }
}

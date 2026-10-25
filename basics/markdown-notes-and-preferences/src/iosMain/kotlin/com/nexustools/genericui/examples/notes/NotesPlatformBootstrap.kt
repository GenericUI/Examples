package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ios.IosNativeFactory

actual object NotesPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(IosNativeFactory(), NoteItem.PREFERENCE_NAMESPACE)
        }
    }
}

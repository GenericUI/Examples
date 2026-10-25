package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.macos.MacOsNativeFactory

actual object NotesPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(MacOsNativeFactory(), NoteItem.PREFERENCE_NAMESPACE)
        }
    }
}

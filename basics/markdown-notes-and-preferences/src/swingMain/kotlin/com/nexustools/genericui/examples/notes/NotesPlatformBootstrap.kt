package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.swing.SwingNativeFactory

actual object NotesPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(SwingNativeFactory(), NoteItem.PREFERENCE_NAMESPACE)
        }
    }
}

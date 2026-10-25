package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.web.WebNativeFactory

actual object NotesPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(WebNativeFactory(), NoteItem.PREFERENCE_NAMESPACE)
        }
    }
}

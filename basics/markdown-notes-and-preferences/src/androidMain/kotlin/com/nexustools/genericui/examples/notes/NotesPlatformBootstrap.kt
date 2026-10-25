package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.android.AndroidNativeFactory
import com.nexustools.genericui.core.GenericUI

actual object NotesPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(AndroidNativeFactory(), NoteItem.PREFERENCE_NAMESPACE)
        }
    }
}

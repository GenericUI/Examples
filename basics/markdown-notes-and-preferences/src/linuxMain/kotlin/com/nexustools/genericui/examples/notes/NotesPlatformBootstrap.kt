package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.gtk.GtkNativeFactory
import com.nexustools.genericui.qt.QtNativeFactory

actual object NotesPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            val factory = if (args.any { it.equals("--qt", ignoreCase = true) }) {
                QtNativeFactory()
            } else {
                GtkNativeFactory()
            }
            GenericUI.initialize(factory, NoteItem.PREFERENCE_NAMESPACE)
        }
    }
}

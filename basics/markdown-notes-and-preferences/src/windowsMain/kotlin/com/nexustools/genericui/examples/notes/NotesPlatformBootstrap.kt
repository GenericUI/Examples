package com.nexustools.genericui.examples.notes

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.windows.WindowsApplicationNativeImpl
import com.nexustools.genericui.windows.WindowsNativeFactory

actual object NotesPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        WindowsApplicationNativeImpl.interactiveWin32GuiEnabled = !args.contains("--headless-verify")
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(WindowsNativeFactory(), NoteItem.PREFERENCE_NAMESPACE)
        }
    }
}

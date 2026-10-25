package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.windows.WindowsApplicationNativeImpl
import com.nexustools.genericui.windows.WindowsNativeFactory

actual object RegistrationWizardPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        WindowsApplicationNativeImpl.interactiveWin32GuiEnabled = !args.contains("--headless-verify")
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(WindowsNativeFactory(), "RegistrationWizardExample")
        }
    }
}

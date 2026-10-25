package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.macos.MacosNativeFactory

actual object RegistrationWizardPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(MacosNativeFactory(), "RegistrationWizardExample")
        }
    }
}

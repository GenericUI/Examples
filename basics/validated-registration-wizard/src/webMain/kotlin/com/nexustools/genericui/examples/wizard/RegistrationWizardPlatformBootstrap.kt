package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.web.WebNativeFactory

actual object RegistrationWizardPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(WebNativeFactory(), "RegistrationWizardExample")
        }
    }
}

package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.android.AndroidNativeFactory
import com.nexustools.genericui.core.GenericUI

actual object RegistrationWizardPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            GenericUI.initialize(AndroidNativeFactory(), "RegistrationWizardExample")
        }
    }
}

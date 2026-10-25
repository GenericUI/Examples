package com.nexustools.genericui.examples.wizard

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.gtk.GtkNativeFactory
import com.nexustools.genericui.qt.QtNativeFactory

actual object RegistrationWizardPlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        if (GenericUI.activeFactory == null) {
            val factory = if (args.any { it.equals("--qt", ignoreCase = true) }) {
                QtNativeFactory()
            } else {
                GtkNativeFactory()
            }
            GenericUI.initialize(factory, "RegistrationWizardExample")
        }
    }
}

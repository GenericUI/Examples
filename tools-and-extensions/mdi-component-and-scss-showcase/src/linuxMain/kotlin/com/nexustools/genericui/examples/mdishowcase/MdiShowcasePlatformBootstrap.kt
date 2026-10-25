package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtension
import com.nexustools.genericui.ext.canvas2d.desktop.DesktopCanvas2DProvider
import com.nexustools.genericui.gtk.GtkCanvas2DNativeImpl
import com.nexustools.genericui.gtk.GtkNativeFactory
import com.nexustools.genericui.qt.QtCanvas2DNativeImpl
import com.nexustools.genericui.qt.QtNativeFactory

actual object MdiShowcasePlatformBootstrap {
    actual fun initialize(args: Array<String>) {
        val useQt = args.any { it.equals("--qt", ignoreCase = true) }
        DesktopCanvas2DProvider.install(if (useQt) "Qt6 QPainter 2D" else "Cairo 2D (cairo_t) + Pango")
        Canvas2DExtension.registerCanvas2DFactory { _, _ ->
            if (useQt) QtCanvas2DNativeImpl() else GtkCanvas2DNativeImpl()
        }
        if (GenericUI.activeFactory == null) {
            val factory = if (useQt) QtNativeFactory() else GtkNativeFactory()
            GenericUI.initialize(factory, "MdiComponentAndScssShowcase")
        }
    }
}

package com.nexustools.genericui.examples.mdishowcase

/**
 * Multiplatform bootstrap that registers the active platform [com.nexustools.genericui.core.NativeFactory]
 * and [com.nexustools.genericui.ext.canvas2d.Canvas2DExtension] provider across all 8 GenericUI backends.
 */
expect object MdiShowcasePlatformBootstrap {
    fun initialize(args: Array<String> = emptyArray())
}

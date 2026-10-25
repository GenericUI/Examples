package com.nexustools.genericui.examples.counter

/**
 * Multiplatform bootstrap that registers the active platform [com.nexustools.genericui.core.NativeFactory]
 * across all 8 GenericUI backends (`windows`, `macos`, `gtk`, `qt`, `swing`, `android`, `ios`, and `web`).
 */
expect object CounterPlatformBootstrap {
    fun initialize(args: Array<String> = emptyArray())
}

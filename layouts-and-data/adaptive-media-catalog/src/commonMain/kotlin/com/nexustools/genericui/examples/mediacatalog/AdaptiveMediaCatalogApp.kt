package com.nexustools.genericui.examples.mediacatalog

import com.nexustools.genericui.component.shell.FragmentApplication

/**
 * Multiplatform [FragmentApplication] hosting [AdaptiveMediaCatalogFragment].
 */
class AdaptiveMediaCatalogApp(
    val catalogFragment: AdaptiveMediaCatalogFragment = AdaptiveMediaCatalogFragment()
) : FragmentApplication(
    initialFragment = catalogFragment,
    title = "GenericUI — Adaptive Media Catalog",
    initialThemeColor = "#7C3AED"
)

fun main(args: Array<String>) {
    if (args.contains("--visual-test")) {
        AdaptiveMediaCatalogVisualTestMain.runVisualSuite(args)
        return
    }
    AdaptiveMediaCatalogApp().run()
}

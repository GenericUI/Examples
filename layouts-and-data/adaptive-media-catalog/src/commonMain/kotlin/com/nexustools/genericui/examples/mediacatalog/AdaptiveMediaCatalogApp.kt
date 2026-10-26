package com.nexustools.genericui.examples.mediacatalog

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication

/**
 * Multiplatform [FragmentApplication] hosting [AdaptiveMediaCatalogFragment].
 */
@AppApplication
class AdaptiveMediaCatalogApp(
    val catalogFragment: AdaptiveMediaCatalogFragment = AdaptiveMediaCatalogFragment()
) : FragmentApplication(
    initialFragment = catalogFragment,
    title = "GenericUI — Adaptive Media Catalog",
    initialThemeColor = "#7C3AED"
)


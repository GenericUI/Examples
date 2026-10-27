package com.nexustools.genericui.examples.mediacatalog

import com.nexustools.genericui.component.fragment.AppApplication
import com.nexustools.genericui.component.shell.FragmentApplication
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.examples.mediacatalog.styles.StylesPrecompiledStyleSheet

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
) {
    init {
        setTheme(ThemeMode.AUTO)
        setThemeColor("#7C3AED")
        addStyleSheet(StylesPrecompiledStyleSheet)
        rootFragmentContainer.applyPrecompiledStyleSheet(StylesPrecompiledStyleSheet)
    }
}


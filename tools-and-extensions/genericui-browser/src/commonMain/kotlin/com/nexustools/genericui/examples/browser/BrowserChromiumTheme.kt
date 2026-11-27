package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.action.styled.StyledButton
import com.nexustools.genericui.component.form.styled.StyledTextField
import com.nexustools.genericui.component.shell.Application
import com.nexustools.genericui.core.GenericUI
import com.nexustools.genericui.enums.css.CssBorderStyle
import com.nexustools.genericui.enums.css.CssCursor
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.style.css.CssCornerRadii
import com.nexustools.genericui.style.css.CssEdgeLengths
import com.nexustools.genericui.style.theme.SystemThemeDetector

/**
 * Shared light and dark Chromium surface palette and control styling helpers for `GenericUIBrowser`.
 */
internal object BrowserChromiumTheme {

    data class Palette(
        val isDark: Boolean,
        val canvasBackground: RgbaColor,
        val toolbarSurface: RgbaColor,
        val toolbarBorder: RgbaColor,
        val omniboxSurface: RgbaColor,
        val omniboxHoverSurface: RgbaColor,
        val omniboxFocusSurface: RgbaColor,
        val omniboxBorder: RgbaColor,
        val omniboxHoverBorder: RgbaColor,
        val omniboxFocusBorder: RgbaColor,
        val omniboxFocusRing: RgbaColor,
        val textPrimary: RgbaColor,
        val textSecondary: RgbaColor,
        val buttonHoverSurface: RgbaColor,
        val buttonActiveSurface: RgbaColor,
        val tonalSurface: RgbaColor,
        val tonalHoverSurface: RgbaColor,
        val tonalActiveSurface: RgbaColor,
        val tonalBorder: RgbaColor,
        val accentSurface: RgbaColor,
        val accentHoverSurface: RgbaColor,
        val accentActiveSurface: RgbaColor,
        val accentText: RgbaColor,
        val cardSurface: RgbaColor,
        val cardBorder: RgbaColor
    )

    private val LIGHT = Palette(
        isDark = false,
        canvasBackground = RgbaColor(248, 249, 250, 255),
        toolbarSurface = RgbaColor(255, 255, 255, 255),
        toolbarBorder = RgbaColor(0, 0, 0, 26),
        omniboxSurface = RgbaColor(241, 243, 244, 255),
        omniboxHoverSurface = RgbaColor(232, 234, 237, 255),
        omniboxFocusSurface = RgbaColor(255, 255, 255, 255),
        omniboxBorder = RgbaColor(60, 64, 67, 36),
        omniboxHoverBorder = RgbaColor(60, 64, 67, 62),
        omniboxFocusBorder = RgbaColor(26, 115, 232, 255),
        omniboxFocusRing = RgbaColor(26, 115, 232, 64),
        textPrimary = RgbaColor(32, 33, 36, 255),
        textSecondary = RgbaColor(95, 99, 104, 255),
        buttonHoverSurface = RgbaColor(60, 64, 67, 22),
        buttonActiveSurface = RgbaColor(60, 64, 67, 38),
        tonalSurface = RgbaColor(241, 243, 244, 255),
        tonalHoverSurface = RgbaColor(232, 234, 237, 255),
        tonalActiveSurface = RgbaColor(218, 220, 224, 255),
        tonalBorder = RgbaColor(60, 64, 67, 32),
        accentSurface = RgbaColor(26, 115, 232, 255),
        accentHoverSurface = RgbaColor(23, 101, 204, 255),
        accentActiveSurface = RgbaColor(20, 88, 179, 255),
        accentText = RgbaColor(255, 255, 255, 255),
        cardSurface = RgbaColor(255, 255, 255, 255),
        cardBorder = RgbaColor(218, 220, 224, 255)
    )

    private val DARK = Palette(
        isDark = true,
        canvasBackground = RgbaColor(32, 33, 36, 255),
        toolbarSurface = RgbaColor(53, 54, 58, 255),
        toolbarBorder = RgbaColor(255, 255, 255, 24),
        omniboxSurface = RgbaColor(32, 33, 36, 255),
        omniboxHoverSurface = RgbaColor(41, 42, 45, 255),
        omniboxFocusSurface = RgbaColor(32, 33, 36, 255),
        omniboxBorder = RgbaColor(255, 255, 255, 28),
        omniboxHoverBorder = RgbaColor(255, 255, 255, 52),
        omniboxFocusBorder = RgbaColor(138, 180, 248, 255),
        omniboxFocusRing = RgbaColor(138, 180, 248, 72),
        textPrimary = RgbaColor(232, 234, 237, 255),
        textSecondary = RgbaColor(154, 160, 166, 255),
        buttonHoverSurface = RgbaColor(255, 255, 255, 26),
        buttonActiveSurface = RgbaColor(255, 255, 255, 42),
        tonalSurface = RgbaColor(48, 49, 52, 255),
        tonalHoverSurface = RgbaColor(60, 64, 67, 255),
        tonalActiveSurface = RgbaColor(72, 76, 80, 255),
        tonalBorder = RgbaColor(255, 255, 255, 34),
        accentSurface = RgbaColor(138, 180, 248, 255),
        accentHoverSurface = RgbaColor(174, 203, 250, 255),
        accentActiveSurface = RgbaColor(115, 165, 245, 255),
        accentText = RgbaColor(32, 33, 36, 255),
        cardSurface = RgbaColor(41, 42, 45, 255),
        cardBorder = RgbaColor(60, 64, 67, 255)
    )

    fun isDarkTheme(explicitMode: ThemeMode? = null): Boolean {
        val resolved = when (explicitMode) {
            ThemeMode.DARK -> ThemeMode.DARK
            ThemeMode.LIGHT -> ThemeMode.LIGHT
            else -> Application.current?.getResolvedTheme()
                ?: SystemThemeDetector.resolveTheme(GenericUI.defaultThemeMode)
        }
        return resolved == ThemeMode.DARK
    }

    fun resolvePalette(explicitMode: ThemeMode? = null): Palette =
        if (isDarkTheme(explicitMode)) DARK else LIGHT

    fun stylePillTextField(
        field: StyledTextField,
        palette: Palette,
        minWidthPx: Int = 220,
        maxWidthPx: Int? = null,
        heightPx: Int = 34,
        fontSizePx: Float = 13.5f,
        horizontalPaddingPx: Int = 14,
        flexGrow: Float? = 1f,
        radiusPx: Float = 999f
    ) {
        if (radiusPx >= 999f) {
            field.setPill(true)
        } else {
            field.setPill(false)
        }
        field.backgroundColor = palette.omniboxSurface
        field.placeholderColor = palette.textSecondary
        field.setBorder(
            widthPx = 1f,
            color = palette.omniboxBorder,
            style = CssBorderStyle.SOLID,
            radiusPx = radiusPx
        )
        field.setHoverStyle(
            backgroundColor = palette.omniboxHoverSurface,
            borderColor = palette.omniboxHoverBorder,
            borderWidthPx = 1f,
            textColor = palette.textPrimary
        )
        field.setFocusStyle(
            backgroundColor = palette.omniboxFocusSurface,
            borderColor = palette.omniboxFocusBorder,
            borderWidthPx = 2f,
            textColor = palette.textPrimary,
            focusRingColor = palette.omniboxFocusRing,
            focusRingWidthPx = 3f
        )
        field.focusBorderStyle = CssBorderStyle.SOLID
        field.focusBorderRadiusPx = radiusPx
        field.getStyle().apply {
            setColor(palette.textPrimary)
            setBorderRadius(CssCornerRadii.px(radiusPx))
            setMinWidthPx(minWidthPx)
            if (maxWidthPx != null) {
                setMaxWidthPx(maxWidthPx)
            }
            setHeightPx(heightPx)
            setPadding(CssEdgeLengths.px(0, horizontalPaddingPx, 0, horizontalPaddingPx))
            setFontSizePx(fontSizePx)
            if (flexGrow != null) {
                setFlexGrow(flexGrow)
            }
        }
    }

    fun styleCircleIconButton(
        button: StyledButton,
        palette: Palette,
        sizePx: Int = 30,
        fontSizePx: Float = 14f,
        activeTint: Boolean = false
    ) {
        val foreground = if (activeTint) palette.omniboxFocusBorder else palette.textPrimary
        button.backgroundColor = RgbaColor.TRANSPARENT
        button.setBorder(
            widthPx = 0f,
            color = RgbaColor.TRANSPARENT,
            style = CssBorderStyle.NONE,
            radiusPx = 999f
        )
        button.setHoverStyle(
            backgroundColor = palette.buttonHoverSurface,
            borderColor = RgbaColor.TRANSPARENT,
            borderWidthPx = 0f,
            textColor = foreground
        )
        button.setFocusStyle(
            backgroundColor = palette.buttonHoverSurface,
            borderColor = palette.omniboxFocusBorder,
            borderWidthPx = 1f,
            textColor = foreground,
            focusRingColor = palette.omniboxFocusRing,
            focusRingWidthPx = 2f
        )
        button.activeBackgroundColor = palette.buttonActiveSurface
        button.activeTextColor = foreground
        button.disabledOpacity = 0.38f
        button.getStyle().apply {
            setColor(foreground)
            setWidthPx(sizePx)
            setHeightPx(sizePx)
            setMinWidthPx(sizePx)
            setMinHeightPx(sizePx)
            setPadding(CssEdgeLengths.ZERO)
            setBorderRadius(CssCornerRadii.px(999))
            setBorderWidth(CssEdgeLengths.ZERO)
            setBorderStyle(CssBorderStyle.NONE)
            setFontSizePx(fontSizePx)
            setCursor(CssCursor.POINTER)
        }
    }

    fun styleSecurityBadgePill(
        button: StyledButton,
        palette: Palette,
        isSecure: Boolean = false,
        sizePx: Int = 28
    ) {
        val restBg = if (palette.isDark) RgbaColor(255, 255, 255, 18) else RgbaColor(60, 64, 67, 16)
        val foreground = if (isSecure) {
            if (palette.isDark) RgbaColor(129, 201, 149, 255) else RgbaColor(24, 128, 56, 255)
        } else {
            palette.textSecondary
        }
        button.backgroundColor = restBg
        button.setBorder(
            widthPx = 0f,
            color = RgbaColor.TRANSPARENT,
            style = CssBorderStyle.NONE,
            radiusPx = 999f
        )
        button.setHoverStyle(
            backgroundColor = palette.buttonHoverSurface,
            borderColor = RgbaColor.TRANSPARENT,
            borderWidthPx = 0f,
            textColor = foreground
        )
        button.setFocusStyle(
            backgroundColor = palette.buttonHoverSurface,
            borderColor = palette.omniboxFocusBorder,
            borderWidthPx = 1f,
            textColor = foreground,
            focusRingColor = palette.omniboxFocusRing,
            focusRingWidthPx = 2f
        )
        button.activeBackgroundColor = palette.buttonActiveSurface
        button.activeTextColor = foreground
        button.getStyle().apply {
            setColor(foreground)
            setWidthPx(sizePx)
            setHeightPx(sizePx)
            setMinWidthPx(sizePx)
            setMinHeightPx(sizePx)
            setPadding(CssEdgeLengths.ZERO)
            setBorderRadius(CssCornerRadii.px(999))
            setBorderWidth(CssEdgeLengths.ZERO)
            setBorderStyle(CssBorderStyle.NONE)
            setFontSizePx(12.5f)
            setCursor(CssCursor.POINTER)
        }
    }

    fun styleTonalPillButton(
        button: StyledButton,
        palette: Palette,
        heightPx: Int = 30,
        horizontalPaddingPx: Int = 12,
        fontSizePx: Float = 12.5f
    ) {
        button.backgroundColor = palette.tonalSurface
        button.setBorder(
            widthPx = 1f,
            color = palette.tonalBorder,
            style = CssBorderStyle.SOLID,
            radiusPx = 999f
        )
        button.setHoverStyle(
            backgroundColor = palette.tonalHoverSurface,
            borderColor = palette.omniboxHoverBorder,
            borderWidthPx = 1f,
            textColor = palette.textPrimary
        )
        button.setFocusStyle(
            backgroundColor = palette.tonalHoverSurface,
            borderColor = palette.omniboxFocusBorder,
            borderWidthPx = 1.5f,
            textColor = palette.textPrimary,
            focusRingColor = palette.omniboxFocusRing,
            focusRingWidthPx = 2f
        )
        button.activeBackgroundColor = palette.tonalActiveSurface
        button.activeTextColor = palette.textPrimary
        button.disabledOpacity = 0.45f
        button.getStyle().apply {
            setColor(palette.textPrimary)
            setHeightPx(heightPx)
            setMinHeightPx(heightPx)
            setPadding(CssEdgeLengths.px(0, horizontalPaddingPx, 0, horizontalPaddingPx))
            setBorderRadius(CssCornerRadii.px(999))
            setFontSizePx(fontSizePx)
            setCursor(CssCursor.POINTER)
        }
    }

    fun styleAccentPillButton(
        button: StyledButton,
        palette: Palette,
        heightPx: Int = 34,
        horizontalPaddingPx: Int = 16,
        fontSizePx: Float = 13f
    ) {
        button.backgroundColor = palette.accentSurface
        button.setBorder(
            widthPx = 0f,
            color = RgbaColor.TRANSPARENT,
            style = CssBorderStyle.NONE,
            radiusPx = 999f
        )
        button.setHoverStyle(
            backgroundColor = palette.accentHoverSurface,
            borderColor = RgbaColor.TRANSPARENT,
            borderWidthPx = 0f,
            textColor = palette.accentText
        )
        button.setFocusStyle(
            backgroundColor = palette.accentHoverSurface,
            borderColor = palette.omniboxFocusBorder,
            borderWidthPx = 1f,
            textColor = palette.accentText,
            focusRingColor = palette.omniboxFocusRing,
            focusRingWidthPx = 3f
        )
        button.activeBackgroundColor = palette.accentActiveSurface
        button.activeTextColor = palette.accentText
        button.disabledOpacity = 0.45f
        button.getStyle().apply {
            setColor(palette.accentText)
            setHeightPx(heightPx)
            setMinHeightPx(heightPx)
            setPadding(CssEdgeLengths.px(0, horizontalPaddingPx, 0, horizontalPaddingPx))
            setBorderRadius(CssCornerRadii.px(999))
            setBorderWidth(CssEdgeLengths.ZERO)
            setBorderStyle(CssBorderStyle.NONE)
            setFontSizePx(fontSizePx)
            setCursor(CssCursor.POINTER)
        }
    }
}

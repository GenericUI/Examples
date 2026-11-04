package com.nexustools.genericui.examples.paint

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.component.system.PreferenceStore

/**
 * Persistent user workspace preferences for `GenericUIPaint`, stored via `getPreferenceStore()`.
 *
 * Defaults match the Windows 11 Dark-Mode Paint screenshot (`131 × 128px` canvas, `#000000` foreground,
 * `#FFFFFF` background, `8px` brush size, `100%` opacity, `100%` zoom).
 */
data class PaintPreferences(
    val canvasWidthPx: Int = 131,
    val canvasHeightPx: Int = 128,
    val foregroundHex: String = "#000000",
    val backgroundHex: String = "#FFFFFF",
    val brushSizePx: Int = 8,
    val brushOpacityPercent: Int = 100,
    val zoomPercent: Int = 100,
    val layersPanelOpen: Boolean = false
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "canvasWidthPx" to canvasWidthPx,
        "canvasHeightPx" to canvasHeightPx,
        "foregroundHex" to foregroundHex,
        "backgroundHex" to backgroundHex,
        "brushSizePx" to brushSizePx,
        "brushOpacityPercent" to brushOpacityPercent,
        "zoomPercent" to zoomPercent,
        "layersPanelOpen" to layersPanelOpen
    )

    companion object {
        const val PREFERENCE_KEY: String = "genericui_paint_preferences"

        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceStore.registerObjectFactory(PaintPreferences::class) { fields ->
                fromElementMap(fields)
            }
        }

        fun fromElementMap(fields: Map<String, PreferenceElement>): PaintPreferences = PaintPreferences(
            canvasWidthPx = fields["canvasWidthPx"]?.asInt(131) ?: 131,
            canvasHeightPx = fields["canvasHeightPx"]?.asInt(128) ?: 128,
            foregroundHex = fields["foregroundHex"]?.asString("#000000") ?: "#000000",
            backgroundHex = fields["backgroundHex"]?.asString("#FFFFFF") ?: "#FFFFFF",
            brushSizePx = fields["brushSizePx"]?.asInt(8) ?: 8,
            brushOpacityPercent = fields["brushOpacityPercent"]?.asInt(100) ?: 100,
            zoomPercent = fields["zoomPercent"]?.asInt(100) ?: 100,
            layersPanelOpen = fields["layersPanelOpen"]?.asBoolean(false) ?: false
        )
    }
}

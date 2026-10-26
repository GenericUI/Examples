package com.nexustools.genericui.examples.filterlab

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.form.RangeSlider
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.ext.imageprocessing.context2d.ImageBuffer2DContext
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtSurface
import com.nexustools.genericui.ext.canvas2d.Canvas2DExtension
import com.nexustools.genericui.ext.canvas2d.dsl.canvas2DExt
import com.nexustools.genericui.ext.dsl.badge
import com.nexustools.genericui.ext.dsl.build
import com.nexustools.genericui.ext.dsl.button
import com.nexustools.genericui.ext.dsl.card
import com.nexustools.genericui.ext.dsl.h5
import com.nexustools.genericui.ext.dsl.hstack
import com.nexustools.genericui.ext.dsl.label
import com.nexustools.genericui.ext.dsl.rangeSlider
import com.nexustools.genericui.ext.dsl.vstack
import com.nexustools.genericui.ext.imageprocessing.BrightnessContrastFilter
import com.nexustools.genericui.ext.imageprocessing.GaussianBlurFilter
import com.nexustools.genericui.ext.imageprocessing.GpuImageProcessor
import com.nexustools.genericui.ext.imageprocessing.HueSaturationLightnessFilter
import com.nexustools.genericui.ext.imageprocessing.ImageHistogramResult
import com.nexustools.genericui.ext.imageprocessing.VignetteFilter
import com.nexustools.genericui.image.ImageBuffer
import com.nexustools.genericui.style.RgbaColor
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Interactive GPU-accelerated image processing laboratory demonstrating
 * `:extensions:imageprocessing` ([GpuImageProcessor], [BrightnessContrastFilter],
 * [HueSaturationLightnessFilter], [GaussianBlurFilter], [VignetteFilter],
 * [ImageHistogramResult]) rendered onto [Canvas2DExtSurface].
 */
@MainAppFragment
class GpuImageFilterLabFragment : Fragment() {

    val sourceImage: ImageBuffer = createProceduralTestImage(width = 160, height = 120)

    var filterState: FilterLabState = FilterLabState()
        private set

    var processedImage: ImageBuffer = sourceImage.copy()
        private set

    var histogram: ImageHistogramResult = GpuImageProcessor.computeHistogram(processedImage)
        private set

    var meanLuminance: Float = 0f
        private set

    lateinit var previewSurface: Canvas2DExtSurface
    lateinit var histogramSurface: Canvas2DExtSurface

    lateinit var presetBadge: Badge
    lateinit var meanLumaBadge: Badge
    lateinit var statusLabel: Label

    lateinit var vividPresetButton: Button
    lateinit var noirPresetButton: Button
    lateinit var cinemaPresetButton: Button
    lateinit var resetPresetButton: Button

    lateinit var brightnessSlider: RangeSlider
    lateinit var contrastSlider: RangeSlider
    lateinit var saturationSlider: RangeSlider
    lateinit var blurSlider: RangeSlider

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        addClass("filter-lab-root")
        buildUi()
        recomputePipeline()
    }

    private fun buildUi() {
        val self = this
        build(clearFirst = true) {
            vstack(classes = "gap-2") {
                card(classes = "filter-toolbar") {
                    hstack(classes = "gap-2 flex-wrap align-items-center") {
                        h5("GPU Image Filter Lab", id = "lbl-title", classes = "mb-0 me-2")

                        self.presetBadge = badge(
                            text = "Preset: Default",
                            type = BootstrapType.PRIMARY,
                            pill = true,
                            id = "badge-preset"
                        )
                        self.meanLumaBadge = badge(
                            text = "Mean Luma: 0.0",
                            type = BootstrapType.INFO,
                            pill = true,
                            id = "badge-mean-luma"
                        )

                        self.vividPresetButton = button(
                            text = "Vivid",
                            type = BootstrapType.SUCCESS,
                            size = BootstrapSize.SM,
                            id = "btn-preset-vivid",
                            onClick = { self.applyPresetVivid() }
                        )
                        self.noirPresetButton = button(
                            text = "Noir",
                            type = BootstrapType.SECONDARY,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-preset-noir",
                            onClick = { self.applyPresetNoir() }
                        )
                        self.cinemaPresetButton = button(
                            text = "Warm Cinema",
                            type = BootstrapType.WARNING,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-preset-cinema",
                            onClick = { self.applyPresetCinema() }
                        )
                        self.resetPresetButton = button(
                            text = "Reset",
                            type = BootstrapType.DANGER,
                            size = BootstrapSize.SM,
                            outline = true,
                            id = "btn-preset-reset",
                            onClick = { self.resetFilters() }
                        )
                    }
                }

                hstack(classes = "filter-workspace gap-3 align-items-start") {
                    vstack(classes = "gap-2") {
                        label("Filtered Output Preview (160x120 Buffer)", id = "lbl-preview-heading")
                        self.previewSurface = canvas2DExt(
                            width = 320,
                            height = 240,
                            id = "filter-preview-canvas",
                            classes = "filter-preview-canvas",
                            onRender = { ctx, _, _, _ ->
                                self.renderPreviewToContext(ctx)
                            }
                        )
                    }

                    vstack(classes = "filter-controls-card gap-2") {
                        label("Filter Pipeline Controls", id = "lbl-controls-heading")

                        label("Brightness (-0.8 to +0.8)", id = "lbl-brightness")
                        self.brightnessSlider = rangeSlider(
                            minValue = self.filterState.brightness.toDouble(),
                            maxValue = 0.8,
                            min = -0.8,
                            max = 0.8,
                            step = 0.05,
                            id = "slider-brightness"
                        ) {
                            onValueChanged = { updated ->
                                self.updateState(self.filterState.copy(brightness = updated.toFloat(), activePresetName = "Custom"))
                            }
                        }

                        label("Contrast (0.2 to 2.5)", id = "lbl-contrast")
                        self.contrastSlider = rangeSlider(
                            minValue = self.filterState.contrast.toDouble(),
                            maxValue = 2.5,
                            min = 0.2,
                            max = 2.5,
                            step = 0.05,
                            id = "slider-contrast"
                        ) {
                            onValueChanged = { updated ->
                                self.updateState(self.filterState.copy(contrast = updated.toFloat(), activePresetName = "Custom"))
                            }
                        }

                        label("Saturation (0.0 to 2.5)", id = "lbl-saturation")
                        self.saturationSlider = rangeSlider(
                            minValue = self.filterState.saturation.toDouble(),
                            maxValue = 2.5,
                            min = 0.0,
                            max = 2.5,
                            step = 0.05,
                            id = "slider-saturation"
                        ) {
                            onValueChanged = { updated ->
                                self.updateState(self.filterState.copy(saturation = updated.toFloat(), activePresetName = "Custom"))
                            }
                        }

                        label("Gaussian Blur Radius (0 to 8 px)", id = "lbl-blur")
                        self.blurSlider = rangeSlider(
                            minValue = self.filterState.blurRadius.toDouble(),
                            maxValue = 8.0,
                            min = 0.0,
                            max = 8.0,
                            step = 1.0,
                            id = "slider-blur"
                        ) {
                            onValueChanged = { updated ->
                                self.updateState(
                                    self.filterState.copy(
                                        blurRadius = updated.roundToInt().coerceIn(0, 8),
                                        activePresetName = "Custom"
                                    )
                                )
                            }
                        }

                        label("256-Bin Luminance & RGB Histogram", id = "lbl-histogram-heading")
                        self.histogramSurface = canvas2DExt(
                            width = 256,
                            height = 100,
                            id = "histogram-canvas",
                            classes = "filter-preview-canvas",
                            onRender = { ctx, _, _, _ ->
                                self.renderHistogramToContext(ctx)
                            }
                        )
                    }
                }

                self.statusLabel = label(
                    text = "Pipeline ready — BrightnessContrast -> HSL -> GaussianBlur -> Vignette",
                    id = "lbl-status"
                )
            }
        }
    }

    fun updateState(newState: FilterLabState) {
        filterState = newState
        recomputePipeline()
    }

    fun applyPresetVivid() {
        syncSlidersAndUpdate(
            FilterLabState(
                brightness = 0.08f,
                contrast = 1.35f,
                saturation = 1.65f,
                hueShiftDegrees = 8f,
                blurRadius = 0,
                vignetteStrength = 0.25f,
                activePresetName = "Vivid"
            )
        )
    }

    fun applyPresetNoir() {
        syncSlidersAndUpdate(
            FilterLabState(
                brightness = -0.05f,
                contrast = 1.6f,
                saturation = 0.0f,
                hueShiftDegrees = 0f,
                blurRadius = 0,
                vignetteStrength = 0.65f,
                activePresetName = "Noir"
            )
        )
    }

    fun applyPresetCinema() {
        syncSlidersAndUpdate(
            FilterLabState(
                brightness = 0.04f,
                contrast = 1.25f,
                saturation = 1.2f,
                hueShiftDegrees = -12f,
                blurRadius = 1,
                vignetteStrength = 0.45f,
                activePresetName = "Warm Cinema"
            )
        )
    }

    fun resetFilters() {
        syncSlidersAndUpdate(FilterLabState())
    }

    private fun syncSlidersAndUpdate(target: FilterLabState) {
        filterState = target
        if (::brightnessSlider.isInitialized) {
            brightnessSlider.value = target.brightness.toDouble()
            contrastSlider.value = target.contrast.toDouble()
            saturationSlider.value = target.saturation.toDouble()
            blurSlider.value = target.blurRadius.toDouble()
        }
        recomputePipeline()
    }

    fun recomputePipeline() {
        val current = filterState
        processedImage = GpuImageProcessor.process(sourceImage) {
            applyFilter(BrightnessContrastFilter(brightness = current.brightness, contrast = current.contrast))
            applyFilter(
                HueSaturationLightnessFilter(
                    hueShiftDegrees = current.hueShiftDegrees,
                    saturation = current.saturation,
                    lightness = 0f
                )
            )
            if (current.blurRadius > 0) {
                applyFilter(GaussianBlurFilter(radiusPx = current.blurRadius))
            }
            if (current.vignetteStrength > 0.001f) {
                applyFilter(
                    VignetteFilter(
                        innerRadius = 0.40f,
                        outerRadius = 0.95f,
                        intensity = current.vignetteStrength
                    )
                )
            }
        }
        histogram = GpuImageProcessor.computeHistogram(processedImage)
        meanLuminance = computeMeanLuminance(histogram)

        val roundedLuma = ((meanLuminance * 10f).roundToInt() / 10f)
        if (::meanLumaBadge.isInitialized) {
            meanLumaBadge.text = "Mean Luma: $roundedLuma"
            presetBadge.text = "Preset: ${current.activePresetName}"
            statusLabel.text =
                "Preset=${current.activePresetName} | B=${current.brightness} C=${current.contrast} S=${current.saturation} Blur=${current.blurRadius}px"
        }

        if (::previewSurface.isInitialized) {
            previewSurface.requestRender()
        }
        if (::histogramSurface.isInitialized) {
            histogramSurface.requestRender()
        }
    }

    fun renderPreviewToContext(ctx: ImageBuffer2DContext) {
        ctx.setFillColor(RgbaColor(11, 17, 30, 255))
        ctx.fillRect(0f, 0f, ctx.width.toFloat(), ctx.height.toFloat())
        Canvas2DExtension.drawImage(ctx, processedImage, dx = 0f, dy = 0f)
    }

    fun renderHistogramToContext(ctx: ImageBuffer2DContext) {
        val w = ctx.width.toFloat()
        val h = ctx.height.toFloat()
        ctx.setFillColor(RgbaColor(9, 13, 22, 255))
        ctx.fillRect(0f, 0f, w, h)

        val bins = histogram.luminanceBins
        val maxCount = (bins.maxOrNull() ?: 1).coerceAtLeast(1)

        ctx.setStrokeColor(RgbaColor(56, 189, 248, 255))
        ctx.setLineWidth(1f)
        for (i in 0 until 256) {
            val normalized = bins[i].toFloat() / maxCount.toFloat()
            val barHeight = (normalized * (h - 8f)).coerceAtLeast(1f)
            val x = i.toFloat()
            ctx.beginPath()
            ctx.moveTo(x, h)
            ctx.lineTo(x, h - barHeight)
            ctx.stroke()
        }
    }

    companion object {
        fun computeMeanLuminance(hist: ImageHistogramResult): Float {
            if (hist.totalPixels <= 0) return 0f
            var weightedSum = 0L
            for (bin in 0 until hist.luminanceBins.size) {
                weightedSum += bin.toLong() * hist.luminanceBins[bin].toLong()
            }
            return weightedSum.toFloat() / hist.totalPixels.toFloat()
        }

        /**
         * Generates a colorful synthetic test chart with gradients, geometric rings,
         * and high-frequency detail for evaluating brightness, contrast, saturation, and blur.
         */
        fun createProceduralTestImage(width: Int, height: Int): ImageBuffer {
            val buf = ImageBuffer(width, height)
            val cx = width * 0.5f
            val cy = height * 0.5f
            val maxRadius = sqrt(cx * cx + cy * cy).coerceAtLeast(1f)

            for (y in 0 until height) {
                val ny = y.toFloat() / height.toFloat()
                for (x in 0 until width) {
                    val nx = x.toFloat() / width.toFloat()
                    val dx = x - cx
                    val dy = y - cy
                    val dist = sqrt(dx * dx + dy * dy) / maxRadius
                    val wave = (sin(nx * 18.0) * cos(ny * 18.0)).toFloat()

                    val r = ((0.25f + 0.55f * nx + 0.20f * wave) * 255f).roundToInt().coerceIn(0, 255)
                    val g = ((0.20f + 0.60f * (1f - dist) + 0.15f * ny) * 255f).roundToInt().coerceIn(0, 255)
                    val b = ((0.30f + 0.50f * ny - 0.15f * wave) * 255f).roundToInt().coerceIn(0, 255)
                    buf.setPixel(x, y, RgbaColor(r, g, b, 255))
                }
            }
            return buf
        }
    }
}

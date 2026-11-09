package com.nexustools.genericui.examples.visualizer

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.action.Toolbar
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.media.AudioAnalyzer
import com.nexustools.genericui.component.media.AudioEnergyBands
import com.nexustools.genericui.component.media.AudioPlayer
import com.nexustools.genericui.component.media.Canvas2D
import com.nexustools.genericui.ext.imageprocessing.context2d.ImageBuffer2DContext
import com.nexustools.genericui.component.media.MediaCapabilitiesReport
import com.nexustools.genericui.component.media.SubtitleCue
import com.nexustools.genericui.component.media.WebVttSrtParser
import com.nexustools.genericui.component.shell.SplitPane
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.system.StatusBar
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.media.AudioCodec
import com.nexustools.genericui.enums.media.CodecSupportLevel
import com.nexustools.genericui.enums.media.MediaContainer
import com.nexustools.genericui.enums.media.VideoCodec
import com.nexustools.genericui.enums.ui.Orientation
import com.nexustools.genericui.ext.media.MediaExtension
import com.nexustools.genericui.nativeimpl.media.AudioAnalyzerNativeImpl
import com.nexustools.genericui.enums.css.CssCursor
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.style.css.CssEdgeLengths
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Primary [Fragment] demonstrating `:extensions:media` ([AudioPlayer], [AudioAnalyzer],
 * [WebVttSrtParser], [AudioEnergyBands], [MediaCapabilitiesReport]) paired with a real-time
 * `:extensions:canvas2d` ([Canvas2D]) visualizer stage.
 */
@MainAppFragment
class AudioSpectrumVisualizerFragment : Fragment() {

    private var activeFftSize: Int = 2048
    private var pcmRingBuffer: FloatArray = FloatArray(activeFftSize)
    private var frequencyBinsDb: FloatArray = FloatArray(activeFftSize / 2) { -100f }
    private var bandMagnitudes64: FloatArray = FloatArray(64)
    private var latestEnergyBands: AudioEnergyBands = AudioEnergyBands()

    var visualizerMode: VisualizerDisplayMode = VisualizerDisplayMode.FFT_SPECTRUM_BARS
        private set

    var isTransportPlaying: Boolean = false
        private set

    var playbackPositionSeconds: Double = 0.0
        private set

    var chordSynthesisCount: Int = 0
        private set

    val subtitleCues: List<SubtitleCue> = WebVttSrtParser.parse(SAMPLE_WEBVTT_SUBTITLES)

    val mediaCapabilities: MediaCapabilitiesReport = buildDefaultCapabilitiesReport()

    val audioAnalyzer: AudioAnalyzer = createBoundAudioAnalyzer()
    val audioPlayer: AudioPlayer = AudioPlayer("synth://harmonic-chord-progression.wav").apply {
        attachAudioAnalyzer(audioAnalyzer)
    }

    // Transport Toolbar Controls
    val topToolbar: Toolbar = Toolbar()

    val playPauseButton: Button = Button(
        text = "Play",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-play-pause"
    }

    val synthChordButton: Button = Button(
        text = "Synthesize Test Chord",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-synth-chord"
    }

    val seekForwardButton: Button = Button(
        text = "Seek +5s",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-seek-forward"
    }

    val resetTransportButton: Button = Button(
        text = "Reset",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-reset-transport"
    }

    val modeSelect: Select = Select(
        options = VisualizerDisplayMode.entries.map {
            SelectOption(it.name, it.displayName)
        },
        selectedValue = VisualizerDisplayMode.FFT_SPECTRUM_BARS.name
    ).apply {
        id = "select-viz-mode"
        widthPx = 220
    }

    val playbackStateBadge: Badge = Badge(
        text = "PAUSED",
        type = BootstrapType.SECONDARY,
        pill = true
    ).apply {
        id = "badge-playback-state"
        widthPx = 74
    }

    val playbackTimeBadge: Badge = Badge(
        text = "0.0s",
        type = BootstrapType.DARK,
        pill = true
    ).apply {
        id = "badge-playback-time"
        widthPx = 62
    }

    // Main Split Workspace
    val workspaceSplitPane: SplitPane = SplitPane(
        orientation = Orientation.HORIZONTAL,
        dividerPosition = 0.56f
    ).apply {
        id = "split-visualizer-workspace"
    }

    // Left Stage: Canvas2D + Active Subtitle Cue Banner
    val visualizerCanvas: Canvas2D = Canvas2D(width = 680, height = 380).apply {
        id = "canvas-spectrum-visualizer"
    }

    val activeModeBadge: Badge = Badge(
        text = VisualizerDisplayMode.FFT_SPECTRUM_BARS.displayName,
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "badge-active-viz-mode"
        widthPx = 216
    }

    val subtitleCueBadge: Badge = Badge(
        text = "No active subtitle cue",
        type = BootstrapType.INFO,
        pill = false
    ).apply {
        id = "badge-subtitle-cue"
        widthPx = 410
    }

    // Right Inspector: Real-time AudioEnergyBands Meters & MediaCapabilitiesReport Table
    val bassBadge: Badge = Badge(
        text = "Bass: 0%",
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "badge-bass"
        widthPx = 78
    }

    val midBadge: Badge = Badge(
        text = "Mid: 0%",
        type = BootstrapType.INFO,
        pill = true
    ).apply {
        id = "badge-mid"
        widthPx = 74
    }

    val trebleBadge: Badge = Badge(
        text = "Treble: 0%",
        type = BootstrapType.WARNING,
        pill = true
    ).apply {
        id = "badge-treble"
        widthPx = 90
    }

    val rmsBadge: Badge = Badge(
        text = "RMS: 0% | Beat: IDLE",
        type = BootstrapType.SECONDARY,
        pill = true
    ).apply {
        id = "badge-rms"
        widthPx = 148
    }

    val bassMeterBar: ProgressBar = ProgressBar(progress = 0f, type = BootstrapType.PRIMARY).apply {
        id = "meter-bass"
    }
    val midMeterBar: ProgressBar = ProgressBar(progress = 0f, type = BootstrapType.INFO).apply {
        id = "meter-mid"
    }
    val trebleMeterBar: ProgressBar = ProgressBar(progress = 0f, type = BootstrapType.WARNING).apply {
        id = "meter-treble"
    }

    val codecBackendBadge: Badge = Badge(
        text = mediaCapabilities.hardwareAccelerationBackend,
        type = BootstrapType.SUCCESS,
        pill = true
    ).apply {
        id = "badge-codec-backend"
        widthPx = 290
    }

    val capabilitiesTable: Table = Table().apply {
        id = "table-media-capabilities"
        isStriped = true
    }

    val statusBar: StatusBar = StatusBar().apply {
        heightPx = 28
    }
    val statusMessageLabel: Label = Label("Ready").apply {
        id = "label-status-bar"
        widthPx = 780
    }

    init {
        addClass("visualizer-root")
        buildWorkspaceLayout()
        populateCapabilitiesTable()
        wireEvents()
        updateSubtitleForTimestamp(playbackPositionSeconds)
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        renderVisualizerFrame()
    }

    private fun createBoundAudioAnalyzer(): AudioAnalyzer {
        MediaExtension.registerProviders(
            audioAnalyzer = { requestedFft ->
                activeFftSize = requestedFft.coerceAtLeast(64)
                object : AudioAnalyzerNativeImpl {
                    override var fftSize: Int
                        get() = activeFftSize
                        set(value) {
                            activeFftSize = value.coerceAtLeast(64)
                        }

                    override val frequencyBinCount: Int
                        get() = (activeFftSize / 2).coerceAtLeast(32)

                    override fun getByteTimeDomainData(destination: ByteArray) {
                        val count = minOf(destination.size, pcmRingBuffer.size)
                        for (i in 0 until count) {
                            val mapped = (((pcmRingBuffer[i] + 1f) * 127.5f).roundToInt()).coerceIn(0, 255)
                            destination[i] = mapped.toByte()
                        }
                    }

                    override fun getFloatTimeDomainData(destination: FloatArray) {
                        val count = minOf(destination.size, pcmRingBuffer.size)
                        pcmRingBuffer.copyInto(destination, 0, 0, count)
                    }

                    override fun getByteFrequencyData(destination: ByteArray) {
                        val count = minOf(destination.size, frequencyBinsDb.size)
                        for (i in 0 until count) {
                            val norm = ((frequencyBinsDb[i] + 100f) / 100f).coerceIn(0f, 1f)
                            destination[i] = (norm * 255f).roundToInt().coerceIn(0, 255).toByte()
                        }
                    }

                    override fun getFloatFrequencyData(destination: FloatArray) {
                        val count = minOf(destination.size, frequencyBinsDb.size)
                        frequencyBinsDb.copyInto(destination, 0, 0, count)
                    }

                    override fun computeEnergyBands(): AudioEnergyBands = latestEnergyBands

                    override fun dispose() {
                        pcmRingBuffer = FloatArray(0)
                        frequencyBinsDb = FloatArray(0)
                    }
                }
            }
        )
        return AudioAnalyzer(fftSize = 2048)
    }

    private fun buildWorkspaceLayout() {
        val rootStack = VStack(gap = SpacingScale.S2)

        // 1. Top Transport Toolbar (direct children so Toolbar distributes controls across the full width)
        topToolbar.addClass("transport-toolbar")
        topToolbar.heightPx = 38
        topToolbar.getStyle().apply {
            padding = CssEdgeLengths.px(6, 10)
            borderRadiusPx = 8f
        }
        visualizerCanvas.getStyle().apply {
            borderRadiusPx = 8f
            setCursor(CssCursor.CROSSHAIR)
        }
        topToolbar.addChild(playPauseButton)
        topToolbar.addChild(synthChordButton)
        topToolbar.addChild(seekForwardButton)
        topToolbar.addChild(resetTransportButton)
        topToolbar.addChild(Label("Visualizer Mode:").apply { widthPx = 104 })
        topToolbar.addChild(modeSelect)
        topToolbar.addChild(playbackStateBadge)
        topToolbar.addChild(playbackTimeBadge)
        topToolbar.addChild(
            Badge(text = "FFT: 2048-pt | 44.1 kHz", type = BootstrapType.INFO, pill = true).apply {
                widthPx = 156
            }
        )
        rootStack.addChild(topToolbar)

        // 2. Left Stage: Canvas2D Visualizer + Subtitle Cue Card
        val leftStack = VStack(gap = SpacingScale.S2)
        val canvasCard = Card("Real-Time 64-Band FFT & Waveform Stage")
        canvasCard.addClass("canvas-stage-card")
        val stageHeaderRow = HStack(gap = SpacingScale.S2).apply {
            heightPx = 26
            flexWrap = FlexWrap.WRAP
        }
        stageHeaderRow.addChild(Label("Active Renderer:").apply { widthPx = 106 })
        stageHeaderRow.addChild(activeModeBadge)
        stageHeaderRow.addChild(
            Badge(text = "64 Log Bands", type = BootstrapType.SECONDARY, pill = true).apply {
                widthPx = 92
            }
        )
        stageHeaderRow.addChild(
            Badge(text = "Window: Hann", type = BootstrapType.DARK, pill = true).apply {
                widthPx = 92
            }
        )
        canvasCard.addChild(stageHeaderRow)
        canvasCard.addChild(visualizerCanvas)
        leftStack.addChild(canvasCard)

        val subtitleCard = Card("Synchronized WebVTT / SRT Subtitle Cue Inspector").apply {
            heightPx = 96
        }
        subtitleCard.addClass("subtitle-inspector-card")
        val subtitleRow = HStack(gap = SpacingScale.S2).apply {
            heightPx = 26
            flexWrap = FlexWrap.WRAP
        }
        subtitleRow.addChild(Label("Active Cue:").apply { widthPx = 76 })
        subtitleRow.addChild(subtitleCueBadge)
        subtitleCard.addChild(subtitleRow)

        val cueScheduleRow = HStack(gap = SpacingScale.S2).apply {
            heightPx = 24
            flexWrap = FlexWrap.WRAP
        }
        cueScheduleRow.addChild(Label("Cue Track:").apply { widthPx = 76 })
        cueScheduleRow.addChild(
            Badge(text = "1.0s-4.5s: cue-intro", type = BootstrapType.DARK, pill = true).apply {
                widthPx = 140
            }
        )
        cueScheduleRow.addChild(
            Badge(text = "5.0s-9.5s: cue-fft", type = BootstrapType.DARK, pill = true).apply {
                widthPx = 128
            }
        )
        cueScheduleRow.addChild(
            Badge(text = "10.0s-15.0s: cue-gpu", type = BootstrapType.DARK, pill = true).apply {
                widthPx = 140
            }
        )
        subtitleCard.addChild(cueScheduleRow)
        leftStack.addChild(subtitleCard)

        // 3. Right Inspector: AudioEnergyBands Meters & MediaCapabilitiesReport
        val rightStack = VStack(gap = SpacingScale.S2)

        val metersCard = Card("Decomposed AudioEnergyBands Telemetry").apply {
            heightPx = 208
        }
        metersCard.addClass("energy-meters-card")
        val badgesRow = HStack(gap = SpacingScale.S2).apply {
            heightPx = 26
            flexWrap = FlexWrap.WRAP
        }
        badgesRow.addChild(bassBadge)
        badgesRow.addChild(midBadge)
        badgesRow.addChild(trebleBadge)
        badgesRow.addChild(rmsBadge)
        metersCard.addChild(badgesRow)
        metersCard.addChild(Label("Bass Energy (20 Hz - 250 Hz):"))
        metersCard.addChild(bassMeterBar)
        metersCard.addChild(Label("Midrange Energy (250 Hz - 4 kHz):"))
        metersCard.addChild(midMeterBar)
        metersCard.addChild(Label("Treble Energy (4 kHz - 20 kHz):"))
        metersCard.addChild(trebleMeterBar)
        rightStack.addChild(metersCard)

        val capabilitiesCard = Card("Platform MediaCapabilitiesReport Inspector")
        capabilitiesCard.addClass("codec-capabilities-card")
        val capHeaderRow = HStack(gap = SpacingScale.S2).apply {
            heightPx = 26
            flexWrap = FlexWrap.WRAP
        }
        capHeaderRow.addChild(Label("Decode Pipeline:").apply { widthPx = 106 })
        capHeaderRow.addChild(codecBackendBadge)
        capabilitiesCard.addChild(capHeaderRow)
        capabilitiesTable.setColumns(
            listOf(
                TableColumn(id = "category", headerText = "Category"),
                TableColumn(id = "codec", headerText = "Codec / Format"),
                TableColumn(id = "support", headerText = "Acceleration"),
                TableColumn(id = "details", headerText = "MIME / Profile")
            )
        )
        capabilitiesTable.setColumnWeights(1.1f, 1.2f, 1.1f, 1.6f)
        capabilitiesCard.addChild(capabilitiesTable)
        rightStack.addChild(capabilitiesCard)

        workspaceSplitPane.setPanes(leftStack, rightStack)
        rootStack.addChild(workspaceSplitPane)

        // 4. Bottom Status Bar
        statusBar.addClass("status-bar")
        statusBar.addChild(statusMessageLabel)
        rootStack.addChild(statusBar)

        addChild(rootStack)
    }

    private fun populateCapabilitiesTable() {
        val rows = mutableListOf<TableRow>()
        for ((videoCodec, level) in mediaCapabilities.videoCodecs) {
            val supportLabel = when (level) {
                CodecSupportLevel.HARDWARE_ACCELERATED -> "HW Accelerated"
                CodecSupportLevel.SUPPORTED -> "Software / OS"
                CodecSupportLevel.SOFTWARE_ONLY -> "Software Only"
                else -> "Unsupported"
            }
            val mimeSummary = when (videoCodec) {
                VideoCodec.H264 -> "video/mp4 (avc1)"
                VideoCodec.HEVC -> "video/mp4 (hvc1)"
                VideoCodec.AV1 -> "video/mp4 (av01)"
                VideoCodec.VP9 -> "video/webm (vp9)"
                else -> videoCodec.html5CanPlayProbes.firstOrNull()?.substringBefore(";") ?: "video/*"
            }
            rows.add(
                TableRow(
                    id = "video-${videoCodec.wireName}",
                    cells = listOf(
                        TableCell("Video Codec"),
                        TableCell(videoCodec.wireName.uppercase()),
                        TableCell(supportLabel),
                        TableCell(mimeSummary)
                    )
                )
            )
        }
        for ((audioCodec, level) in mediaCapabilities.audioCodecs) {
            val supportLabel = when (level) {
                CodecSupportLevel.HARDWARE_ACCELERATED -> "HW Accelerated"
                CodecSupportLevel.SUPPORTED -> "Software / OS"
                CodecSupportLevel.SOFTWARE_ONLY -> "Software Only"
                else -> "Unsupported"
            }
            val mimeSummary = when (audioCodec) {
                AudioCodec.AAC -> "audio/mp4 (mp4a)"
                AudioCodec.OPUS -> "audio/webm (opus)"
                AudioCodec.FLAC -> "audio/flac (24b)"
                AudioCodec.MP3 -> "audio/mpeg (mp3)"
                AudioCodec.EAC3 -> "audio/eac3 (5.1)"
                else -> audioCodec.html5CanPlayProbes.firstOrNull()?.substringBefore(";") ?: "audio/*"
            }
            rows.add(
                TableRow(
                    id = "audio-${audioCodec.wireName}",
                    cells = listOf(
                        TableCell("Audio Codec"),
                        TableCell(audioCodec.wireName.uppercase()),
                        TableCell(supportLabel),
                        TableCell(mimeSummary)
                    )
                )
            )
        }
        rows.add(
            TableRow(
                id = "containers-summary",
                cells = listOf(
                    TableCell("Containers"),
                    TableCell("MP4/MKV/WEBM/HLS"),
                    TableCell("Supported"),
                    TableCell("${mediaCapabilities.maxWidth}x${mediaCapabilities.maxHeight} @ ${mediaCapabilities.maxFrameRate}fps")
                )
            )
        )
        rows.add(
            TableRow(
                id = "hdr-color-summary",
                cells = listOf(
                    TableCell("HDR Pipeline"),
                    TableCell("HDR10 / HLG / DV"),
                    TableCell(if (mediaCapabilities.supportsHdr10) "HW Accelerated" else "SDR Only"),
                    TableCell("10-bit BT.2020")
                )
            )
        )
        rows.add(
            TableRow(
                id = "surround-audio-summary",
                cells = listOf(
                    TableCell("Spatial Audio"),
                    TableCell("5.1 / 7.1 PCM"),
                    TableCell(if (mediaCapabilities.supportsSurround71) "HW Accelerated" else "Stereo"),
                    TableCell("8-ch Discrete")
                )
            )
        )
        capabilitiesTable.setRows(rows)
    }

    private fun wireEvents() {
        visualizerCanvas.registerVBlankRender { ctx, _, _, _ ->
            drawSpectrumStage(ctx)
        }

        playPauseButton.setOnClick {
            togglePlayPause()
        }

        synthChordButton.setOnClick {
            synthesizeTestChord()
        }

        seekForwardButton.setOnClick {
            seekToSeconds(playbackPositionSeconds + 5.0)
        }

        resetTransportButton.setOnClick {
            resetTransport()
        }

        modeSelect.onSelectionChanged = { selectedModeName ->
            switchVisualizerMode(VisualizerDisplayMode.fromNameOrDefault(selectedModeName))
        }
    }

    /**
     * Toggles transport playback between `PLAYING` and `PAUSED`.
     */
    fun togglePlayPause(): Boolean {
        isTransportPlaying = !isTransportPlaying
        if (isTransportPlaying) {
            audioPlayer.play()
            playPauseButton.text = "Pause"
            playPauseButton.getBootstrap().type = BootstrapType.WARNING
            playbackStateBadge.text = "PLAYING"
            playbackStateBadge.getBootstrap().type = BootstrapType.SUCCESS
            if (playbackPositionSeconds == 0.0) {
                seekToSeconds(1.5)
            }
            if (chordSynthesisCount == 0) {
                synthesizeTestChord()
            }
            statusMessageLabel.text = "Playback started at ${formatSeconds(playbackPositionSeconds)}."
        } else {
            audioPlayer.pause()
            playPauseButton.text = "Play"
            playPauseButton.getBootstrap().type = BootstrapType.SUCCESS
            playbackStateBadge.text = "PAUSED"
            playbackStateBadge.getBootstrap().type = BootstrapType.SECONDARY
            statusMessageLabel.text = "Playback paused at ${formatSeconds(playbackPositionSeconds)}."
        }
        return isTransportPlaying
    }

    /**
     * Seeks transport playback to [targetSeconds] and synchronizes the active WebVTT/SRT subtitle cue.
     */
    fun seekToSeconds(targetSeconds: Double) {
        val clamped = targetSeconds.coerceIn(0.0, 30.0)
        playbackPositionSeconds = clamped
        audioPlayer.seekTo(clamped)
        playbackTimeBadge.text = formatSeconds(clamped)
        updateSubtitleForTimestamp(clamped)
        statusMessageLabel.text = "Seeked transport to ${formatSeconds(clamped)}."
    }

    /**
     * Stops playback and resets transport time, energy bands, and subtitle cue state.
     */
    fun resetTransport() {
        isTransportPlaying = false
        playbackPositionSeconds = 0.0
        audioPlayer.stop()
        playPauseButton.text = "Play"
        playPauseButton.getBootstrap().type = BootstrapType.SUCCESS
        playbackStateBadge.text = "STOPPED"
        playbackStateBadge.getBootstrap().type = BootstrapType.SECONDARY
        playbackTimeBadge.text = "0.0s"
        updateSubtitleForTimestamp(0.0)
        statusMessageLabel.text = "Transport reset to 0.0s."
    }

    /**
     * Switches the active [VisualizerDisplayMode] and triggers a canvas redraw.
     */
    fun switchVisualizerMode(newMode: VisualizerDisplayMode) {
        visualizerMode = newMode
        modeSelect.selectedValue = newMode.name
        activeModeBadge.text = newMode.displayName
        renderVisualizerFrame()
        statusMessageLabel.text = "Switched visualizer mode to ${newMode.displayName}."
    }

    /**
     * Synthesizes a multi-harmonic test chord (bass + midrange + treble sine waves),
     * populates the FFT spectrum and [AudioEnergyBands] via [audioAnalyzer], and updates UI badges.
     */
    fun synthesizeTestChord(
        bassFreqHz: Float = 110f,
        midFreqHz: Float = 880f,
        trebleFreqHz: Float = 5200f,
        bassAmp: Float = 0.68f,
        midAmp: Float = 0.54f,
        trebleAmp: Float = 0.42f
    ): AudioEnergyBands {
        chordSynthesisCount++
        val sampleRate = 44100f
        val n = activeFftSize.coerceAtLeast(256)
        if (pcmRingBuffer.size != n) {
            pcmRingBuffer = FloatArray(n)
        }
        val phaseShift = chordSynthesisCount * 0.35f

        var sumSquares = 0f
        for (i in 0 until n) {
            val t = i / sampleRate
            val bassWave = bassAmp * sin((2.0 * PI * bassFreqHz * t + phaseShift).toFloat())
            val midWave = midAmp * sin((2.0 * PI * midFreqHz * t + phaseShift * 1.7f).toFloat())
            val trebleWave = trebleAmp * cos((2.0 * PI * trebleFreqHz * t + phaseShift * 2.3f).toFloat())
            val sample = ((bassWave + midWave + trebleWave) / 1.64f).coerceIn(-1f, 1f)
            pcmRingBuffer[i] = sample
            sumSquares += sample * sample
        }

        val rms = sqrt(sumSquares / n).coerceIn(0f, 1f)
        val bassNorm = (bassAmp * 1.12f).coerceIn(0f, 1f)
        val lowMidNorm = ((bassAmp + midAmp) * 0.52f).coerceIn(0f, 1f)
        val midNorm = (midAmp * 1.15f).coerceIn(0f, 1f)
        val highMidNorm = ((midAmp + trebleAmp) * 0.55f).coerceIn(0f, 1f)
        val trebleNorm = (trebleAmp * 1.18f).coerceIn(0f, 1f)

        val binCount = (n / 2).coerceAtLeast(64)
        if (frequencyBinsDb.size != binCount) {
            frequencyBinsDb = FloatArray(binCount) { -100f }
        }

        for (bin in 0 until binCount) {
            val ratio = bin.toFloat() / binCount.toFloat()
            val bandEnergy = when {
                ratio < 0.12f -> bassNorm * (1f - ratio * 2f)
                ratio < 0.45f -> midNorm * (0.85f + 0.15f * sin(ratio * 18f))
                else -> trebleNorm * (0.75f + 0.25f * cos(ratio * 24f))
            }.coerceIn(0.02f, 1f)
            frequencyBinsDb[bin] = (20f * log10(bandEnergy)).coerceIn(-100f, 0f)
        }

        for (b in 0 until 64) {
            val binIdx = (b * binCount / 64).coerceIn(0, binCount - 1)
            bandMagnitudes64[b] = ((frequencyBinsDb[binIdx] + 100f) / 100f).coerceIn(0.05f, 1f)
        }

        latestEnergyBands = AudioEnergyBands(
            bass = bassNorm,
            lowMid = lowMidNorm,
            mid = midNorm,
            highMid = highMidNorm,
            treble = trebleNorm,
            overallRms = rms,
            peakBeatDetected = rms >= 0.35f || bassNorm >= 0.65f
        )

        val computed = audioAnalyzer.computeEnergyBands()
        updateEnergyBadgesAndMeters(computed)
        renderVisualizerFrame()
        statusMessageLabel.text = "Synthesized harmonic test chord #$chordSynthesisCount (${bassFreqHz.roundToInt()}Hz / ${midFreqHz.roundToInt()}Hz / ${trebleFreqHz.roundToInt()}Hz)."
        return computed
    }

    /**
     * Resolves and displays the active [SubtitleCue] at [timeSeconds] using [WebVttSrtParser] cues.
     */
    fun updateSubtitleForTimestamp(timeSeconds: Double): SubtitleCue? {
        val activeCue = subtitleCues.firstOrNull { it.isActiveAt(timeSeconds) }
        if (activeCue != null) {
            subtitleCueBadge.text = "[${activeCue.id}] ${activeCue.text}"
            subtitleCueBadge.getBootstrap().type = BootstrapType.INFO
        } else {
            subtitleCueBadge.text = "No active subtitle cue at ${formatSeconds(timeSeconds)}"
            subtitleCueBadge.getBootstrap().type = BootstrapType.SECONDARY
        }
        return activeCue
    }

    private fun updateEnergyBadgesAndMeters(bands: AudioEnergyBands) {
        val bassPct = (bands.bass * 100f).roundToInt().coerceIn(0, 100)
        val midPct = (bands.mid * 100f).roundToInt().coerceIn(0, 100)
        val treblePct = (bands.treble * 100f).roundToInt().coerceIn(0, 100)
        val rmsPct = (bands.overallRms * 100f).roundToInt().coerceIn(0, 100)

        bassBadge.text = "Bass: $bassPct%"
        midBadge.text = "Mid: $midPct%"
        trebleBadge.text = "Treble: $treblePct%"
        rmsBadge.text = "RMS: $rmsPct% | Beat: ${if (bands.peakBeatDetected) "PEAK" else "IDLE"}"
        rmsBadge.getBootstrap().type = if (bands.peakBeatDetected) BootstrapType.SUCCESS else BootstrapType.SECONDARY

        bassMeterBar.progress = bands.bass.coerceIn(0f, 1f)
        midMeterBar.progress = bands.mid.coerceIn(0f, 1f)
        trebleMeterBar.progress = bands.treble.coerceIn(0f, 1f)
    }

    /**
     * Immediately renders the current spectrum/waveform/ring visualization onto [visualizerCanvas].
     */
    fun renderVisualizerFrame() {
        val ctx = visualizerCanvas.resolveRenderingContext()
        if (ctx != null) {
            drawSpectrumStage(ctx)
        }
        visualizerCanvas.requestRender()
    }

    private fun drawSpectrumStage(ctx: ImageBuffer2DContext) {
        val w = ctx.width.toFloat().coerceAtLeast(320f)
        val h = ctx.height.toFloat().coerceAtLeast(200f)

        ctx.setFillColor(RgbaColor.fromHex("#090D16"))
        ctx.fillRect(0f, 0f, w, h)

        // Subtle grid lines
        ctx.setStrokeColor(RgbaColor(255, 255, 255, 22))
        ctx.setLineWidth(1f)
        for (row in 1..3) {
            val y = h * (row / 4f)
            ctx.beginPath()
            ctx.moveTo(0f, y)
            ctx.lineTo(w, y)
            ctx.stroke()
        }

        when (visualizerMode) {
            VisualizerDisplayMode.FFT_SPECTRUM_BARS -> drawFftSpectrumBars(ctx, w, h)
            VisualizerDisplayMode.OSCILLOSCOPE_WAVEFORM -> drawOscilloscopeWaveform(ctx, w, h)
            VisualizerDisplayMode.RADIAL_ENERGY_RINGS -> drawRadialEnergyRings(ctx, w, h)
        }
    }

    private fun drawFftSpectrumBars(ctx: ImageBuffer2DContext, w: Float, h: Float) {
        val barCount = bandMagnitudes64.size
        val slotWidth = (w - 24f) / barCount
        val barWidth = (slotWidth * 0.72f).coerceAtLeast(2f)
        val maxBarHeight = h - 36f

        for (i in 0 until barCount) {
            val mag = bandMagnitudes64[i].coerceIn(0.04f, 1f)
            val barH = mag * maxBarHeight
            val x = 12f + i * slotWidth
            val y = h - 18f - barH

            val color = when {
                i < 16 -> RgbaColor.fromHex("#10B981")
                i < 42 -> RgbaColor.fromHex("#38BDF8")
                else -> RgbaColor.fromHex("#A855F7")
            }
            ctx.setFillColor(color)
            ctx.fillRect(x, y, barWidth, barH)
        }
    }

    private fun drawOscilloscopeWaveform(ctx: ImageBuffer2DContext, w: Float, h: Float) {
        val midY = h * 0.5f
        val samples = pcmRingBuffer
        if (samples.isEmpty()) return

        ctx.setStrokeColor(RgbaColor.fromHex("#38BDF8"))
        ctx.setLineWidth(2.2f)
        ctx.beginPath()
        val points = 128
        for (i in 0 until points) {
            val sampleIdx = (i * samples.size / points).coerceIn(0, samples.size - 1)
            val x = 12f + (w - 24f) * (i.toFloat() / (points - 1))
            val y = midY - samples[sampleIdx] * (h * 0.38f)
            if (i == 0) ctx.moveTo(x, y) else ctx.lineTo(x, y)
        }
        ctx.stroke()
    }

    private fun drawRadialEnergyRings(ctx: ImageBuffer2DContext, w: Float, h: Float) {
        val cx = w * 0.5f
        val cy = h * 0.5f
        val minDim = minOf(w, h)
        val fullCircle = (2.0 * PI).toFloat()

        val bassRadius = minDim * (0.14f + latestEnergyBands.bass * 0.22f)
        val midRadius = minDim * (0.10f + latestEnergyBands.mid * 0.16f)
        val trebleRadius = minDim * (0.06f + latestEnergyBands.treble * 0.12f)

        ctx.setLineWidth(5f)
        ctx.setStrokeColor(RgbaColor.fromHex("#10B981"))
        ctx.beginPath()
        ctx.arc(cx, cy, bassRadius, 0f, fullCircle)
        ctx.stroke()

        ctx.setStrokeColor(RgbaColor.fromHex("#38BDF8"))
        ctx.beginPath()
        ctx.arc(cx, cy, midRadius, 0f, fullCircle)
        ctx.stroke()

        ctx.setStrokeColor(RgbaColor.fromHex("#A855F7"))
        ctx.beginPath()
        ctx.arc(cx, cy, trebleRadius, 0f, fullCircle)
        ctx.stroke()
    }

    private fun formatSeconds(sec: Double): String {
        val tenths = (sec * 10.0).roundToInt()
        return "${tenths / 10}.${tenths % 10}s"
    }

    companion object {
        val SAMPLE_WEBVTT_SUBTITLES: String = """
            WEBVTT

            cue-intro
            00:00:01.000 --> 00:00:04.500
            Welcome to GenericUI Audio Spectrum Visualizer

            cue-fft
            00:00:05.000 --> 00:00:09.500
            Real-time 64-band FFT & AudioEnergyBands decomposition

            cue-gpu
            00:00:10.000 --> 00:00:15.000
            Hardware-accelerated Vulkan & OpenGL ES media pipeline
        """.trimIndent()

        fun buildDefaultCapabilitiesReport(): MediaCapabilitiesReport = MediaCapabilitiesReport(
            videoCodecs = linkedMapOf(
                VideoCodec.H264 to CodecSupportLevel.HARDWARE_ACCELERATED,
                VideoCodec.HEVC to CodecSupportLevel.HARDWARE_ACCELERATED,
                VideoCodec.AV1 to CodecSupportLevel.HARDWARE_ACCELERATED,
                VideoCodec.VP9 to CodecSupportLevel.SUPPORTED
            ),
            audioCodecs = linkedMapOf(
                AudioCodec.AAC to CodecSupportLevel.HARDWARE_ACCELERATED,
                AudioCodec.OPUS to CodecSupportLevel.SUPPORTED,
                AudioCodec.FLAC to CodecSupportLevel.SUPPORTED,
                AudioCodec.MP3 to CodecSupportLevel.SUPPORTED,
                AudioCodec.EAC3 to CodecSupportLevel.HARDWARE_ACCELERATED
            ),
            containers = linkedSetOf(
                MediaContainer.MP4,
                MediaContainer.MKV,
                MediaContainer.WEBM,
                MediaContainer.HLS,
                MediaContainer.DASH
            ),
            maxWidth = 3840,
            maxHeight = 2160,
            maxFrameRate = 60,
            supportsHdr10 = true,
            supportsHlg = true,
            supportsDolbyVision = true,
            supportsSurround51 = true,
            supportsSurround71 = true,
            hardwareAccelerationBackend = "Vulkan #version 450 SPIR-V + Hardware Decode"
        )
    }
}

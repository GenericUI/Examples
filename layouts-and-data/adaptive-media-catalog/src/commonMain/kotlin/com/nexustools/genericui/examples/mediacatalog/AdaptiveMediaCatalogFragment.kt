package com.nexustools.genericui.examples.mediacatalog

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.helper.AdaptiveSplitTabFragment
import com.nexustools.genericui.component.layout.Container
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.window.WindowConfiguration

/**
 * Responsive media catalog [AdaptiveSplitTabFragment] demonstrating automatic layout adaptation
 * across three viewport postures:
 * - **Desktop / Tablet Landscape (`1280x800`)**: Multi-column split layout (`TABLET_DESKTOP_COLUMNS`).
 * - **Phone Landscape (`800x390`)**: Immersive 100% fullscreen primary media viewport (`PHONE_LANDSCAPE_FULLSCREEN`).
 * - **Phone Portrait (`390x844`)**: Single vertical flow with primary stage on top and tabbed panes below (`PORTRAIT_SINGLE_FLOW_TABS`).
 */
class AdaptiveMediaCatalogFragment : AdaptiveSplitTabFragment(
    phoneLandscapeFullscreen = true,
    primaryColumnWeight = 1.8f,
    secondaryColumnWeight = 1.0f
) {

    val catalogChannels: List<CatalogChannelItem> = DEFAULT_CHANNELS

    var selectedChannel: CatalogChannelItem = catalogChannels.first()
        private set

    private val _playbackQueue = mutableListOf(
        catalogChannels[0],
        catalogChannels[1]
    )
    val playbackQueue: List<CatalogChannelItem>
        get() = _playbackQueue

    var isPlaying: Boolean = true
        private set

    var activeViewportConfig: WindowConfiguration = WindowConfiguration(widthPx = 1280, heightPx = 800)
        private set

    val desktopPostureButton: Button = Button("Desktop Split (1280x800)", BootstrapType.PRIMARY, BootstrapSize.SM).apply {
        id = "btn-posture-desktop"
        onClick = { applyViewportPosture(1280, 800) }
    }

    val phoneLandscapePostureButton: Button = Button("Phone Landscape (800x390)", BootstrapType.SECONDARY, BootstrapSize.SM).apply {
        id = "btn-posture-phone-landscape"
        onClick = { applyViewportPosture(800, 390) }
    }

    val phonePortraitPostureButton: Button = Button("Phone Portrait (390x844)", BootstrapType.SECONDARY, BootstrapSize.SM).apply {
        id = "btn-posture-phone-portrait"
        onClick = { applyViewportPosture(390, 844) }
    }

    val layoutModeBadge: Badge = Badge("TABLET_DESKTOP_COLUMNS", BootstrapType.INFO, pill = true).apply {
        id = "badge-layout-mode"
    }

    val viewportSizeBadge: Badge = Badge("1280x800", BootstrapType.SECONDARY, pill = true).apply {
        id = "badge-viewport-size"
    }

    val nowPlayingTitleLabel: Label = Label(selectedChannel.title).apply {
        id = "label-now-playing-title"
    }

    val nowPlayingGenreBadge: Badge = Badge(selectedChannel.genre, BootstrapType.PRIMARY, pill = true).apply {
        id = "badge-now-playing-genre"
    }

    val nowPlayingDurationLabel: Label = Label(selectedChannel.durationLabel).apply {
        id = "label-now-playing-duration"
    }

    val nowPlayingDescriptionLabel: Label = Label(selectedChannel.description).apply {
        id = "label-now-playing-description"
    }

    val transportStateBadge: Badge = Badge("PLAYING", BootstrapType.SUCCESS, pill = true).apply {
        id = "badge-transport-state"
    }

    val queueCountBadge: Badge = Badge("Queue: 2", BootstrapType.WARNING, pill = true).apply {
        id = "badge-queue-count"
    }

    val playPauseButton: Button = Button("Pause", BootstrapType.SUCCESS, BootstrapSize.SM).apply {
        id = "btn-transport-play-pause"
        onClick = { togglePlayPause() }
    }

    val prevTrackButton: Button = Button("Prev Channel", BootstrapType.OUTLINE_SECONDARY, BootstrapSize.SM).apply {
        id = "btn-transport-prev"
        onClick = { skipToPreviousChannel() }
    }

    val nextTrackButton: Button = Button("Next Channel", BootstrapType.OUTLINE_PRIMARY, BootstrapSize.SM).apply {
        id = "btn-transport-next"
        onClick = { skipToNextChannel() }
    }

    val enqueueCurrentButton: Button = Button("Add Current to Queue", BootstrapType.INFO, BootstrapSize.SM).apply {
        id = "btn-queue-add-current"
        onClick = { enqueueChannelById(selectedChannel.id) }
    }

    val clearQueueButton: Button = Button("Clear Queue", BootstrapType.DANGER, BootstrapSize.SM).apply {
        id = "btn-queue-clear"
        onClick = { clearQueue() }
    }

    val queueSummaryLabel: Label = Label("").apply {
        id = "label-queue-summary"
    }

    val detailChannelIdLabel: Label = Label("ID: ${selectedChannel.id}").apply {
        id = "label-detail-channel-id"
    }

    val detailSynopsisLabel: Label = Label(selectedChannel.description).apply {
        id = "label-detail-synopsis"
    }

    private val queueItemsContainer = VStack(gap = SpacingScale.S2)

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)

        val primaryStage = buildPrimaryViewportStage()
        setPrimaryView(primaryStage)

        addTab("Channels", buildChannelsTabPane())
        addTab("Queue", buildQueueTabPane())
        addTab("Details", buildDetailsTabPane())

        refreshNowPlayingAndTabs()
        syncPostureBadges()
    }

    /**
     * Simulates or applies a viewport configuration change (`widthPx` x `heightPx`) and triggers
     * [AdaptiveSplitTabFragment.onConfigurationChanged].
     */
    fun applyViewportPosture(widthPx: Int, heightPx: Int) {
        val config = WindowConfiguration(
            widthPx = widthPx,
            heightPx = heightPx,
            smallestScreenWidthDp = minOf(widthPx, heightPx)
        )
        activeViewportConfig = config
        onConfigurationChanged(config)
        syncPostureBadges()
    }

    /**
     * Selects a channel by [channelId] as the active Now Playing item.
     */
    fun selectChannelById(channelId: String): Boolean {
        val found = catalogChannels.firstOrNull { it.id == channelId } ?: return false
        selectedChannel = found
        isPlaying = true
        refreshNowPlayingAndTabs()
        return true
    }

    /**
     * Appends the channel matching [channelId] to the playback queue if not already present.
     */
    fun enqueueChannelById(channelId: String): Boolean {
        val found = catalogChannels.firstOrNull { it.id == channelId } ?: return false
        if (_playbackQueue.none { it.id == found.id }) {
            _playbackQueue.add(found)
        }
        refreshNowPlayingAndTabs()
        return true
    }

    /**
     * Clears all items from the playback queue.
     */
    fun clearQueue() {
        _playbackQueue.clear()
        refreshNowPlayingAndTabs()
    }

    /**
     * Toggles media transport between `PLAYING` and `PAUSED`.
     */
    fun togglePlayPause() {
        isPlaying = !isPlaying
        refreshNowPlayingAndTabs()
    }

    /**
     * Advances to the next channel in the catalog.
     */
    fun skipToNextChannel() {
        val currentIdx = catalogChannels.indexOfFirst { it.id == selectedChannel.id }
        val nextIdx = if (currentIdx < 0) 0 else (currentIdx + 1) % catalogChannels.size
        selectedChannel = catalogChannels[nextIdx]
        isPlaying = true
        refreshNowPlayingAndTabs()
    }

    /**
     * Returns to the previous channel in the catalog.
     */
    fun skipToPreviousChannel() {
        val currentIdx = catalogChannels.indexOfFirst { it.id == selectedChannel.id }
        val prevIdx = if (currentIdx <= 0) catalogChannels.lastIndex else currentIdx - 1
        selectedChannel = catalogChannels[prevIdx]
        isPlaying = true
        refreshNowPlayingAndTabs()
    }

    override fun onConfigurationChanged(newConfig: WindowConfiguration) {
        super.onConfigurationChanged(newConfig)
        activeViewportConfig = newConfig
        syncPostureBadges()
    }

    private fun buildPrimaryViewportStage(): Container {
        val stage = VStack(gap = SpacingScale.S3).apply {
            id = "catalog-primary-stage"
            addClass("catalog-primary-stage")
        }

        val postureToolbar = HStack(gap = SpacingScale.S2).apply {
            addClass("catalog-posture-bar")
            addChild(desktopPostureButton)
            addChild(phoneLandscapePostureButton)
            addChild(phonePortraitPostureButton)
            addChild(layoutModeBadge)
            addChild(viewportSizeBadge)
        }

        val nowPlayingCard = Card("Now Playing Viewport").apply {
            val metaRow = HStack(gap = SpacingScale.S2).apply {
                addChild(nowPlayingTitleLabel)
                addChild(nowPlayingGenreBadge)
                addChild(nowPlayingDurationLabel)
                addChild(transportStateBadge)
                addChild(queueCountBadge)
            }
            addChild(metaRow)
            addChild(nowPlayingDescriptionLabel)
        }

        val transportBar = HStack(gap = SpacingScale.S2).apply {
            addClass("catalog-transport-bar")
            addChild(prevTrackButton)
            addChild(playPauseButton)
            addChild(nextTrackButton)
            addChild(enqueueCurrentButton)
            addChild(clearQueueButton)
        }

        stage.addChild(Heading("Adaptive Media Catalog", level = 4))
        stage.addChild(postureToolbar)
        stage.addChild(nowPlayingCard)
        stage.addChild(transportBar)
        return stage
    }

    private fun buildChannelsTabPane(): Container {
        val pane = VStack(gap = SpacingScale.S2).apply {
            id = "tab-pane-channels"
        }
        for (channel in catalogChannels) {
            val row = HStack(gap = SpacingScale.S2).apply {
                id = "channel-row-${channel.id}"
                addClass("catalog-channel-row")
            }
            val selectBtn = Button(
                text = "Play ${channel.title}",
                type = BootstrapType.PRIMARY,
                size = BootstrapSize.SM
            ).apply {
                id = "btn-select-channel-${channel.id}"
                onClick = { selectChannelById(channel.id) }
            }
            val queueBtn = Button(
                text = "+ Queue",
                type = BootstrapType.OUTLINE_INFO,
                size = BootstrapSize.SM
            ).apply {
                id = "btn-enqueue-channel-${channel.id}"
                onClick = { enqueueChannelById(channel.id) }
            }
            row.addChild(selectBtn)
            row.addChild(Badge(channel.genre, BootstrapType.SECONDARY, pill = true))
            row.addChild(Label(channel.durationLabel))
            row.addChild(queueBtn)
            pane.addChild(row)
        }
        return pane
    }

    private fun buildQueueTabPane(): Container {
        val pane = VStack(gap = SpacingScale.S2).apply {
            id = "tab-pane-queue"
        }
        pane.addChild(queueSummaryLabel)
        pane.addChild(queueItemsContainer)
        return pane
    }

    private fun buildDetailsTabPane(): Container {
        val pane = VStack(gap = SpacingScale.S2).apply {
            id = "tab-pane-details"
        }
        pane.addChild(detailChannelIdLabel)
        pane.addChild(detailSynopsisLabel)
        return pane
    }

    private fun refreshNowPlayingAndTabs() {
        nowPlayingTitleLabel.text = selectedChannel.title
        nowPlayingGenreBadge.text = selectedChannel.genre
        nowPlayingDurationLabel.text = selectedChannel.durationLabel
        nowPlayingDescriptionLabel.text = selectedChannel.description

        transportStateBadge.text = if (isPlaying) "PLAYING" else "PAUSED"
        transportStateBadge.type = if (isPlaying) BootstrapType.SUCCESS else BootstrapType.WARNING
        playPauseButton.text = if (isPlaying) "Pause" else "Play"

        queueCountBadge.text = "Queue: ${_playbackQueue.size}"
        queueSummaryLabel.text = if (_playbackQueue.isEmpty()) {
            "Queue is empty"
        } else {
            "Up Next (${_playbackQueue.size}): " + _playbackQueue.joinToString(", ") { it.title }
        }

        queueItemsContainer.clearChildren()
        for (item in _playbackQueue) {
            queueItemsContainer.addChild(Label("${item.title} (${item.durationLabel})"))
        }

        detailChannelIdLabel.text = "ID: ${selectedChannel.id} | Genre: ${selectedChannel.genre}"
        detailSynopsisLabel.text = selectedChannel.description
    }

    private fun syncPostureBadges() {
        val modeName = getActiveLayoutMode()?.name ?: LayoutMode.TABLET_DESKTOP_COLUMNS.name
        layoutModeBadge.text = modeName
        viewportSizeBadge.text = "${activeViewportConfig.widthPx}x${activeViewportConfig.heightPx}"

        desktopPostureButton.type =
            if (getActiveLayoutMode() == LayoutMode.TABLET_DESKTOP_COLUMNS) BootstrapType.PRIMARY else BootstrapType.SECONDARY
        phoneLandscapePostureButton.type =
            if (getActiveLayoutMode() == LayoutMode.PHONE_LANDSCAPE_FULLSCREEN) BootstrapType.PRIMARY else BootstrapType.SECONDARY
        phonePortraitPostureButton.type =
            if (getActiveLayoutMode() == LayoutMode.PORTRAIT_SINGLE_FLOW_TABS) BootstrapType.PRIMARY else BootstrapType.SECONDARY
    }

    companion object {
        val DEFAULT_CHANNELS: List<CatalogChannelItem> = listOf(
            CatalogChannelItem(
                id = "ch-synthwave",
                title = "Neon Horizon Live",
                genre = "Synthwave",
                durationLabel = "42:15",
                description = "4K HDR live visual synthwave broadcast with real-time shader backdrops."
            ),
            CatalogChannelItem(
                id = "ch-kotlin-conf",
                title = "Multiplatform UI Architecture",
                genre = "Engineering",
                durationLabel = "58:40",
                description = "Deep dive into single-codebase native rendering across 8 desktop, mobile, and web targets."
            ),
            CatalogChannelItem(
                id = "ch-lofi-studio",
                title = "Midnight Coding Beats",
                genre = "Lo-Fi",
                durationLabel = "01:24:00",
                description = "Continuous ambient instrumental stream curated for deep focus sessions."
            ),
            CatalogChannelItem(
                id = "ch-space-doc",
                title = "Orbital Telemetry Feed",
                genre = "Documentary",
                durationLabel = "36:10",
                description = "High-frame-rate earth observation camera feed with telemetry overlay."
            )
        )
    }
}

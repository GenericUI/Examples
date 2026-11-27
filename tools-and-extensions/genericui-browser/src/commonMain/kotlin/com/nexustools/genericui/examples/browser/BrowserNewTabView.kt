package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.action.styled.StyledButton
import com.nexustools.genericui.component.form.styled.StyledTextField
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.AlignItems
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.FlexDirection
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.JustifyContent
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.ThemeMode
import com.nexustools.genericui.style.css.CssCornerRadii
import com.nexustools.genericui.style.css.CssEdgeLengths
import com.nexustools.genericui.style.css.CssLength

/**
 * Clean empty-state and New Tab search surface (`GenericUI Browser`) shared between the
 * 0-open-pages browser landing view and unnavigated `about:newtab` tab pages.
 */
class BrowserNewTabView(
    showNewTabButton: Boolean = true,
    initialBookmarks: List<BrowserBookmark> = emptyList(),
    idPrefix: String = "browser-newtab"
) : VStack(gap = SpacingScale.S4) {

    private var bookmarksList: List<BrowserBookmark> = initialBookmarks.toList()
    private var explicitThemeMode: ThemeMode? = null

    val brandHeading: Heading = Heading(level = 2, text = "GenericUI Browser").apply {
        id = "$idPrefix-heading"
        addClass("browser-newtab-heading")
    }

    val subtitleLabel: Label = Label("Search the web or enter a URL to open a page").apply {
        id = "$idPrefix-subtitle"
        addClass("browser-newtab-subtitle")
    }

    val searchField: StyledTextField = StyledTextField(
        value = "",
        placeholder = "Search or enter web address",
        multilineMode = false
    ).apply {
        id = "$idPrefix-search"
        addClass("browser-omnibox-pill", "browser-newtab-search")
    }

    val searchButton: StyledButton = StyledButton(
        text = "Search",
        type = null,
        size = BootstrapSize.MD
    ).apply {
        id = "$idPrefix-search-btn"
        addClass("browser-newtab-search-btn")
        minWidthPx = 88
    }

    val newTabActionButton: StyledButton = StyledButton(
        text = "+ New Tab",
        type = null,
        size = BootstrapSize.MD
    ).apply {
        id = "$idPrefix-open-btn"
        addClass("browser-newtab-open-btn")
        isVisible = showNewTabButton
        minWidthPx = 104
    }

    val bookmarksHeaderLabel: Label = Label("Saved Bookmarks").apply {
        addClass("browser-newtab-subtitle")
        isVisible = initialBookmarks.isNotEmpty()
    }

    val bookmarksContainer: FlexBox = FlexBox().apply {
        flexDirection = FlexDirection.ROW
        flexWrap = FlexWrap.WRAP
        justifyContent = JustifyContent.CENTER
        alignItems = AlignItems.CENTER
        addClass("browser-newtab-bookmarks-grid", "gap-2")
        getStyle().apply {
            setWidth(CssLength.PERCENT_100)
            setMaxWidthPx(640)
            setGap(CssLength.px(10))
        }
        isVisible = initialBookmarks.isNotEmpty()
    }

    val contentCard: Card = Card().apply {
        addClass("browser-newtab-card")
        getStyle().apply {
            setWidth(CssLength.PERCENT_100)
            setMaxWidthPx(720)
            setPadding(CssEdgeLengths.px(36, 32, 36, 32))
            setBorderRadius(CssCornerRadii.px(20))
        }
    }

    /** Invoked when the user submits a URL or search query from [searchField] or [searchButton]. */
    var onSubmitQuery: ((query: String) -> Unit)? = null

    /** Invoked when the user clicks [newTabActionButton]. */
    var onNewTabClicked: (() -> Unit)? = null

    /** Invoked when the user clicks a saved bookmark shortcut. */
    var onBookmarkSelected: ((bookmark: BrowserBookmark) -> Unit)? = null

    init {
        addClass("browser-newtab-page")
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.CENTER
        getStyle().apply {
            setPadding(CssEdgeLengths.px(28, 24, 28, 24))
            setFlexGrow(1f)
        }

        val cardStack = VStack(gapPx = 16).apply {
            alignItems = AlignItems.CENTER
            getStyle().setWidth(CssLength.PERCENT_100)
        }
        cardStack.addChild(brandHeading)
        cardStack.addChild(subtitleLabel)

        val searchRow = HStack(gapPx = 8).apply {
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.CENTER
            flexWrap = FlexWrap.WRAP
            addClass("browser-newtab-search-row")
            getStyle().apply {
                setWidth(CssLength.PERCENT_100)
                setMaxWidthPx(640)
                setMargin(CssEdgeLengths.px(4, 0, 4, 0))
            }
        }
        searchRow.addChild(searchField)
        searchRow.addChild(searchButton)
        if (showNewTabButton) {
            searchRow.addChild(newTabActionButton)
        }
        cardStack.addChild(searchRow)

        cardStack.addChild(bookmarksHeaderLabel)
        cardStack.addChild(bookmarksContainer)

        contentCard.addChild(cardStack)
        addChild(contentCard)

        val submitQuery: (String) -> Unit = { raw ->
            val trimmed = raw.trim()
            if (trimmed.isNotEmpty()) {
                searchField.value = ""
                onSubmitQuery?.invoke(trimmed)
            }
        }
        searchField.onSubmit = submitQuery
        searchButton.setOnClick {
            submitQuery(searchField.value)
        }
        newTabActionButton.setOnClick {
            onNewTabClicked?.invoke()
        }

        applyThemeVisuals()
        rebuildBookmarksGrid()
    }

    /**
     * Updates the saved user bookmark shortcuts displayed below the search field.
     */
    fun setBookmarks(bookmarks: List<BrowserBookmark>) {
        bookmarksList = bookmarks.toList()
        rebuildBookmarksGrid()
    }

    /**
     * Synchronizes Chromium light/dark styling across the New Tab search pill, buttons, and tiles.
     */
    fun applyThemeVisuals(themeOverride: ThemeMode? = explicitThemeMode) {
        explicitThemeMode = themeOverride
        val palette = BrowserChromiumTheme.resolvePalette(themeOverride)
        BrowserChromiumTheme.stylePillTextField(
            field = searchField,
            palette = palette,
            minWidthPx = 220,
            maxWidthPx = 640,
            heightPx = 44,
            fontSizePx = 15f,
            horizontalPaddingPx = 18,
            flexGrow = 1f
        )
        BrowserChromiumTheme.styleAccentPillButton(
            button = searchButton,
            palette = palette,
            heightPx = 40,
            horizontalPaddingPx = 18,
            fontSizePx = 13.5f
        )
        BrowserChromiumTheme.styleTonalPillButton(
            button = newTabActionButton,
            palette = palette,
            heightPx = 40,
            horizontalPaddingPx = 16,
            fontSizePx = 13.5f
        )
        for (child in bookmarksContainer.children) {
            if (child is StyledButton) {
                BrowserChromiumTheme.styleTonalPillButton(
                    button = child,
                    palette = palette,
                    heightPx = 34,
                    horizontalPaddingPx = 14,
                    fontSizePx = 13f
                )
            }
        }
    }

    private fun rebuildBookmarksGrid() {
        bookmarksContainer.clearChildren()
        val hasBookmarks = bookmarksList.isNotEmpty()
        bookmarksHeaderLabel.isVisible = hasBookmarks
        bookmarksContainer.isVisible = hasBookmarks
        val palette = BrowserChromiumTheme.resolvePalette(explicitThemeMode)
        for (bookmark in bookmarksList) {
            val shortcutBtn = StyledButton(
                text = "${bookmark.iconLabel} ${bookmark.title}",
                type = null,
                size = BootstrapSize.SM
            ).apply {
                addClass("newtab-speed-dial-btn")
                BrowserChromiumTheme.styleTonalPillButton(
                    button = this,
                    palette = palette,
                    heightPx = 34,
                    horizontalPaddingPx = 14,
                    fontSizePx = 13f
                )
                setOnClick {
                    onBookmarkSelected?.invoke(bookmark)
                }
            }
            bookmarksContainer.addChild(shortcutBtn)
        }
    }
}

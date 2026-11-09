package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.fragment.AppFragment
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.layout.Column
import com.nexustools.genericui.component.layout.ColumnContainer
import com.nexustools.genericui.component.layout.Container
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.enums.bootstrap.AlignItems
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexDirection
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.JustifyContent
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.ext.webview.WebView

/**
 * Single browser tab content [Fragment] containing an embedded [WebView] alongside rich interactive
 * native preview surfaces for built-in showcase routes:
 * - `https://one.google.com/ai/activity` ("Google One — AI credits activity")
 * - `https://github.com/T-Crypt/berth` ("T-Crypt / berth")
 * - `chrome://newtab` (Speed-Dial & Search New Tab page)
 */
@AppFragment(route = "browser/tab", label = "Browser Tab Page")
class BrowserTabPageFragment(
    initialUrl: String = BrowserSessionState.DEFAULT_HOME_URL,
    initialCreditsBalance: Int = BrowserSessionState.DEFAULT_AI_CREDITS_BALANCE,
    initialBookmarks: List<BrowserBookmark> = BrowserBookmark.defaultBookmarks()
) : Fragment() {

    private val navigationHistory = mutableListOf<BrowserHistoryEntry>()
    private var navigationIndex: Int = -1
    private val _creditActivityRows = BrowserCreditActivityRow.defaultRows().toMutableList()
    private var bookmarksList: List<BrowserBookmark> = initialBookmarks.toList()
    private var topUpCounter: Int = 0
    private var githubStarred: Boolean = false
    private var githubStarCount: Int = 383

    /** Embedded `:extensions:webview` component synchronized with [currentUrl]. */
    val webView: WebView = WebView(initialUrl = initialUrl).apply {
        addClass("browser-tab-webview")
        isVisible = false
    }

    /** Currently active URL in this tab. */
    var currentUrl: String = initialUrl
        private set

    /** Resolved human-readable title for [currentUrl]. */
    var currentTitle: String = resolveTitleForUrl(initialUrl)
        private set

    /** Resolved icon/emoji badge for [currentUrl]. */
    var currentIcon: String = resolveIconForUrl(initialUrl)
        private set

    /** Number of times [reload] has been invoked on this tab. */
    var reloadCount: Int = 0
        private set

    /** Current Google One AI credits balance displayed on `https://one.google.com/ai/activity`. */
    var aiCreditsBalance: Int = initialCreditsBalance
        private set

    /** Read-only snapshot of the Google One AI credit activity rows. */
    val creditActivityRows: List<BrowserCreditActivityRow>
        get() = _creditActivityRows.toList()

    /** Read-only snapshot of this tab's navigation history stack. */
    val historyStack: List<BrowserHistoryEntry>
        get() = navigationHistory.toList()

    /** Current index within [historyStack]. */
    val historyIndex: Int
        get() = navigationIndex

    /** `true` if this tab can navigate backward in its per-tab history stack. */
    val canGoBack: Boolean
        get() = navigationIndex > 0

    /** `true` if this tab can navigate forward in its per-tab history stack. */
    val canGoForward: Boolean
        get() = navigationIndex >= 0 && navigationIndex < navigationHistory.lastIndex

    // --- 1. Google One "AI credits activity" Surface Controls ---
    val googleOneSurface: VStack = VStack(gap = SpacingScale.S2).apply {
        addClass("google-one-page")
    }
    val googleOneBrandLabel: Label = Label("Google One")
    val googleOneHeading: Heading = Heading(level = 2, text = "AI credits activity")
    val googleOneSubtitleLabel: Label = Label("View your AI credit balance and recent usage across Google products").apply {
        widthPx = 460
    }
    val creditsBalanceLabel: Label = Label(BrowserCreditActivityRow.formatNumberWithCommas(initialCreditsBalance)).apply {
        addClass("google-one-credits-balance")
    }
    val creditsCaptionLabel: Label = Label("AI credits available (of 10,000 monthly plan)")
    val creditsQuotaBar: ProgressBar = ProgressBar(
        progress = (initialCreditsBalance.toFloat() / 10_000f).coerceIn(0f, 1f),
        label = "${BrowserCreditActivityRow.formatNumberWithCommas(initialCreditsBalance)} / 10,000 credits",
        type = BootstrapType.PRIMARY
    ).apply {
        widthPx = 300
    }
    val addCreditsButton: Button = Button(
        text = "+ Add",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        addClass("google-one-add-btn")
        widthPx = 88
    }
    val recentActivityHeading: Heading = Heading(level = 4, text = "Your recent activity")
    val activityTable: Table = Table().apply {
        isStriped = true
    }

    // --- 2. GitHub "T-Crypt/berth" Dark Repository Surface Controls ---
    val githubBerthSurface: VStack = VStack(gap = SpacingScale.S3).apply {
        addClass("github-repo-page")
    }
    val githubCodeTabButton: Button = Button("<> Code", type = BootstrapType.DARK, size = BootstrapSize.SM)
    val githubIssuesTabButton: Button = Button("(!) Issues 9", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val githubPullRequestsTabButton: Button = Button("PR Pull requests", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val githubActionsTabButton: Button = Button("> Actions", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val githubOwnerRepoHeading: Heading = Heading(level = 3, text = "T-Crypt / berth")
    val repoVisibilityBadge: Badge = Badge("Public", type = BootstrapType.SECONDARY, pill = true)
    val notificationsButton: Button = Button("Notifications", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val forkButton: Button = Button("Fork 22", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val starButton: Button = Button("* Star 383", type = BootstrapType.SECONDARY, size = BootstrapSize.SM, outline = true)
    val repoDescriptionLabel: Label = Label(
        "Decides who gets your GPU. Chat, image and voice engines lease the card in turn instead of grabbing it, so two of them never load at once and crash each other. One Go binary, nothing else to install."
    )
    val repoWebsiteLabel: Label = Label("Link: t-crypt.github.io/berth/")
    val repoLicenseLabel: Label = Label("License: Apache-2.0 license")
    val repoStatsLabel: Label = Label("* 383 stars   |   22 forks")
    val githubTopicsContainer: FlexBox = FlexBox().apply {
        flexDirection = FlexDirection.ROW
        flexWrap = FlexWrap.WRAP
        addClass("gap-1")
    }
    val githubTopicBadges: List<Badge> = GITHUB_BERTH_TOPICS.map { topic ->
        Badge(topic, type = BootstrapType.INFO, pill = true).apply {
            addClass("github-topic-pill")
        }
    }

    // --- 3. Chrome New Tab ("chrome://newtab") Surface Controls ---
    val newTabSurface: VStack = VStack(gap = SpacingScale.S3).apply {
        addClass("chrome-newtab-page")
    }
    val newTabBrandHeading: Heading = Heading(level = 2, text = "Google")
    val newTabSearchField: TextField = TextField(
        value = "",
        placeholder = "Search Google or type a URL"
    ).apply {
        addClass("browser-omnibox-pill")
    }
    val newTabSearchButton: Button = Button("Search", type = BootstrapType.PRIMARY, size = BootstrapSize.SM)
    val speedDialContainer: FlexBox = FlexBox().apply {
        flexDirection = FlexDirection.ROW
        flexWrap = FlexWrap.WRAP
        addClass("gap-2")
    }

    // --- 4. Generic / Auxiliary Route Surface Controls ---
    val genericWebSurface: VStack = VStack(gap = SpacingScale.S2).apply {
        addClass("generic-web-page")
    }
    val genericPageTitleHeading: Heading = Heading(level = 3, text = "")
    val genericPageUrlLabel: Label = Label("")
    val genericPageSummaryLabel: Label = Label("")

    /** Invoked whenever this tab navigates to a new URL or updates its title. */
    var onPageNavigated: ((url: String, title: String, icon: String) -> Unit)? = null

    /** Invoked whenever [aiCreditsBalance] is incremented via [addCreditsButton] or [addAiCredits]. */
    var onCreditsBalanceChanged: ((newBalance: Int, addedRow: BrowserCreditActivityRow) -> Unit)? = null

    /** Invoked whenever this tab fragment receives an [onResize] callback. */
    var onViewportResized: ((widthPx: Int, heightPx: Int) -> Unit)? = null

    init {
        addClass("browser-tab-page-fragment")
        buildGoogleOneSurface()
        buildGithubBerthSurface()
        buildNewTabSurface()
        buildGenericWebSurface()

        addChild(googleOneSurface)
        addChild(githubBerthSurface)
        addChild(newTabSurface)
        addChild(genericWebSurface)
        addChild(webView)

        navigateTo(initialUrl, recordHistory = true)
    }

    private fun buildGoogleOneSurface() {
        val headerRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.BETWEEN
            flexWrap = FlexWrap.WRAP
        }
        headerRow.addChild(googleOneBrandLabel)
        headerRow.addChild(Badge("AI Premium 2 TB", type = BootstrapType.INFO, pill = true))
        headerRow.addChild(Badge("Google Account", type = BootstrapType.PRIMARY, pill = true))
        headerRow.addChild(Badge("Auto-Top-Up: Enabled", type = BootstrapType.SUCCESS, pill = true))
        googleOneSurface.addChild(headerRow)

        val creditsCard = Card().apply {
            addClass("google-one-credits-card")
        }
        val titleRow = HStack(gap = SpacingScale.S3).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        titleRow.addChild(googleOneHeading)
        titleRow.addChild(googleOneSubtitleLabel)
        creditsCard.addChild(titleRow)

        val creditsRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        creditsRow.addChild(Badge("BALANCE", type = BootstrapType.PRIMARY, pill = true))
        creditsRow.addChild(creditsBalanceLabel)
        creditsRow.addChild(creditsCaptionLabel)
        creditsRow.addChild(creditsQuotaBar)
        creditsRow.addChild(addCreditsButton)
        creditsRow.addChild(Badge("Renews Nov 27, 2026", type = BootstrapType.SECONDARY, pill = true))
        creditsCard.addChild(creditsRow)
        googleOneSurface.addChild(creditsCard)

        addCreditsButton.setOnClick {
            addAiCredits(1000)
        }

        val activityCard = Card().apply {
            addClass("google-one-activity-card")
        }
        val activityHeaderRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        activityHeaderRow.addChild(recentActivityHeading)
        activityHeaderRow.addChild(Badge("Hourly Metering", type = BootstrapType.SECONDARY, pill = true))
        activityHeaderRow.addChild(Badge("Export CSV", type = BootstrapType.INFO, pill = true))
        activityCard.addChild(activityHeaderRow)

        activityTable.setColumns(
            listOf(
                TableColumn(id = "date", headerText = "DATE"),
                TableColumn(id = "category", headerText = "CATEGORY"),
                TableColumn(id = "amount", headerText = "AMOUNT")
            )
        )
        activityTable.setColumnWeights(1.2f, 1.8f, 1.0f)
        refreshCreditsActivityTable()
        activityCard.addChild(activityTable)
        googleOneSurface.addChild(activityCard)

        // Bottom 2-column Quota Breakdown & Plan Details row so the viewport is balanced and informative
        val benefitsGrid = ColumnContainer()
        val breakdownCol = Column(span = 6, md = 6, lg = 6)
        val breakdownCard = Card()
        breakdownCard.addChild(Heading(level = 4, text = "Credit Usage by Google AI Product"))
        val agRow = HStack(gap = SpacingScale.S2).apply { alignItems = AlignItems.CENTER }
        agRow.addChild(Badge("Google Antigravity", type = BootstrapType.PRIMARY).apply { widthPx = 156 })
        agRow.addChild(ProgressBar(progress = 0.72f, label = "5,230 credits (72%)", type = BootstrapType.PRIMARY).apply { widthPx = 380 })
        breakdownCard.addChild(agRow)

        val geminiRow = HStack(gap = SpacingScale.S2).apply { alignItems = AlignItems.CENTER }
        geminiRow.addChild(Badge("Gemini 2.5 Pro", type = BootstrapType.INFO).apply { widthPx = 156 })
        geminiRow.addChild(ProgressBar(progress = 0.18f, label = "1,310 credits (18%)", type = BootstrapType.INFO).apply { widthPx = 380 })
        breakdownCard.addChild(geminiRow)

        val veoRow = HStack(gap = SpacingScale.S2).apply { alignItems = AlignItems.CENTER }
        veoRow.addChild(Badge("Veo 2 & Imagen 3", type = BootstrapType.SUCCESS).apply { widthPx = 156 })
        veoRow.addChild(ProgressBar(progress = 0.10f, label = "720 credits (10%)", type = BootstrapType.SUCCESS).apply { widthPx = 380 })
        breakdownCard.addChild(veoRow)
        breakdownCol.addChild(breakdownCard)

        val planCol = Column(span = 6, md = 6, lg = 6)
        val planCard = Card()
        planCard.addChild(Heading(level = 4, text = "Google One AI Plan & Credit Settings"))
        planCard.addChild(Label("Subscription: Google One AI Premium (2 TB Cloud + 10,000 Monthly AI Credits)"))
        planCard.addChild(Label("Instant Top-Up: Click '+ Add' above to purchase +1,000 bonus AI credits anytime"))
        val planBadgesRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        planBadgesRow.addChild(Badge("Status: Active", type = BootstrapType.SUCCESS, pill = true))
        planBadgesRow.addChild(Badge("Rollover: Up to 5,000", type = BootstrapType.INFO, pill = true))
        planBadgesRow.addChild(Badge("Family Sharing: On", type = BootstrapType.SECONDARY, pill = true))
        planCard.addChild(planBadgesRow)
        planCol.addChild(planCard)

        benefitsGrid.addColumn(breakdownCol)
        benefitsGrid.addColumn(planCol)
        googleOneSurface.addChild(benefitsGrid)
    }

    private fun buildGithubBerthSurface() {
        val repoTabsRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        repoTabsRow.addChild(githubCodeTabButton)
        repoTabsRow.addChild(githubIssuesTabButton)
        repoTabsRow.addChild(githubPullRequestsTabButton)
        repoTabsRow.addChild(githubActionsTabButton)
        githubBerthSurface.addChild(repoTabsRow)

        val repoHeaderCard = Container("div", "github-repo-header")
        val headerStack = VStack(gap = SpacingScale.S2)

        val titleAndBadgeRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        titleAndBadgeRow.addChild(githubOwnerRepoHeading)
        titleAndBadgeRow.addChild(repoVisibilityBadge)
        headerStack.addChild(titleAndBadgeRow)

        val actionButtonsRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
        }
        actionButtonsRow.addChild(notificationsButton)
        actionButtonsRow.addChild(forkButton)
        actionButtonsRow.addChild(starButton)
        headerStack.addChild(actionButtonsRow)

        starButton.setOnClick {
            githubStarred = !githubStarred
            githubStarCount = if (githubStarred) 384 else 383
            starButton.text = if (githubStarred) "★ Starred $githubStarCount" else "* Star $githubStarCount"
            repoStatsLabel.text = "* $githubStarCount stars   |   22 forks"
        }

        headerStack.addChild(repoDescriptionLabel)
        headerStack.addChild(repoWebsiteLabel)
        headerStack.addChild(repoLicenseLabel)
        headerStack.addChild(repoStatsLabel)

        for (badge in githubTopicBadges) {
            githubTopicsContainer.addChild(badge)
        }
        headerStack.addChild(githubTopicsContainer)
        repoHeaderCard.addChild(headerStack)
        githubBerthSurface.addChild(repoHeaderCard)

        val readmeCard = Card("README.md — berth")
        val readmeStack = VStack(gap = SpacingScale.S1)
        readmeStack.addChild(Label("berth — GPU Lease Coordinator for Local AI Workloads"))
        readmeStack.addChild(
            Label("Run Ollama, llama.cpp, vLLM, and ComfyUI on a single GPU without VRAM collisions.")
        )
        readmeCard.addChild(readmeStack)
        githubBerthSurface.addChild(readmeCard)
    }

    private fun buildNewTabSurface() {
        newTabSurface.addChild(newTabBrandHeading)

        val searchRow = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
        }
        searchRow.addChild(newTabSearchField)
        searchRow.addChild(newTabSearchButton)
        newTabSurface.addChild(searchRow)

        val submitNewTabQuery: (String) -> Unit = { raw ->
            val trimmed = raw.trim()
            if (trimmed.isNotEmpty()) {
                navigateTo(normalizeOmniboxInput(trimmed))
            }
        }
        newTabSearchField.onSubmit = submitNewTabQuery
        newTabSearchButton.setOnClick {
            submitNewTabQuery(newTabSearchField.value)
        }

        rebuildSpeedDialGrid()
        newTabSurface.addChild(speedDialContainer)
    }

    private fun buildGenericWebSurface() {
        val card = Card("Web Page Content")
        val stack = VStack(gap = SpacingScale.S2)
        stack.addChild(genericPageTitleHeading)
        stack.addChild(genericPageUrlLabel)
        stack.addChild(genericPageSummaryLabel)
        card.addChild(stack)
        genericWebSurface.addChild(card)
    }

    /**
     * Updates the bookmarks shown on the `chrome://newtab` speed-dial grid.
     */
    fun setSpeedDialBookmarks(bookmarks: List<BrowserBookmark>) {
        bookmarksList = bookmarks.toList()
        rebuildSpeedDialGrid()
    }

    private fun rebuildSpeedDialGrid() {
        speedDialContainer.clearChildren()
        for (bookmark in bookmarksList) {
            val shortcutBtn = Button(
                text = "${bookmark.iconLabel} ${bookmark.title}",
                type = BootstrapType.SECONDARY,
                size = BootstrapSize.SM,
                outline = true
            ).apply {
                addClass("newtab-speed-dial-btn")
                setOnClick {
                    navigateTo(bookmark.url)
                }
            }
            speedDialContainer.addChild(shortcutBtn)
        }
    }

    /**
     * Increments [aiCreditsBalance] by [delta] (default `1,000`), updates [creditsBalanceLabel],
     * and prepends a new top-up row to [activityTable].
     */
    fun addAiCredits(delta: Int = 1000): BrowserCreditActivityRow {
        topUpCounter += 1
        aiCreditsBalance += delta
        val formatted = BrowserCreditActivityRow.formatNumberWithCommas(aiCreditsBalance)
        creditsBalanceLabel.text = formatted
        creditsQuotaBar.progress = (aiCreditsBalance.toFloat() / 10_000f).coerceIn(0f, 1f)
        creditsQuotaBar.label = "$formatted / 10,000 credits"
        val hour = (6 + topUpCounter).coerceIn(1, 12)
        val newRow = BrowserCreditActivityRow(
            date = "Oct 27, 2026, $hour:00:00 AM",
            category = "Google One AI credits top-up",
            amount = delta
        )
        _creditActivityRows.add(0, newRow)
        refreshCreditsActivityTable()
        onCreditsBalanceChanged?.invoke(aiCreditsBalance, newRow)
        return newRow
    }

    /**
     * Synchronizes [aiCreditsBalance] from external session state without adding an activity row.
     */
    fun syncAiCreditsBalance(newBalance: Int) {
        aiCreditsBalance = newBalance
        val formatted = BrowserCreditActivityRow.formatNumberWithCommas(aiCreditsBalance)
        creditsBalanceLabel.text = formatted
        creditsQuotaBar.progress = (aiCreditsBalance.toFloat() / 10_000f).coerceIn(0f, 1f)
        creditsQuotaBar.label = "$formatted / 10,000 credits"
    }

    private fun refreshCreditsActivityTable() {
        activityTable.setRows(
            _creditActivityRows.mapIndexed { idx, row ->
                TableRow(
                    id = "credit-row-$idx",
                    cells = listOf(
                        TableCell(row.date),
                        TableCell(row.category),
                        TableCell(row.formattedAmount())
                    )
                )
            }
        )
    }

    /**
     * Navigates this tab to [targetUrl], updating [webView], active preview surface visibility,
     * and the per-tab history stack when [recordHistory] is `true`.
     */
    fun navigateTo(
        targetUrl: String,
        recordHistory: Boolean = true,
        searchPrefix: String = BrowserSessionState.DEFAULT_SEARCH_PREFIX
    ) {
        val normalized = normalizeOmniboxInput(targetUrl, searchPrefix)
        currentUrl = normalized
        currentTitle = resolveTitleForUrl(normalized)
        currentIcon = resolveIconForUrl(normalized)
        pageTitle = currentTitle

        webView.loadUrl(normalized)

        if (recordHistory) {
            val currentAtCursor = navigationHistory.getOrNull(navigationIndex)
            if (currentAtCursor?.url != normalized) {
                while (navigationHistory.lastIndex > navigationIndex) {
                    navigationHistory.removeAt(navigationHistory.lastIndex)
                }
                navigationHistory.add(
                    BrowserHistoryEntry(
                        url = normalized,
                        title = currentTitle,
                        timestampIso = "2026-10-27T06:00:00Z"
                    )
                )
                navigationIndex = navigationHistory.lastIndex
            }
        }

        syncActiveRouteSurface()
        onPageNavigated?.invoke(currentUrl, currentTitle, currentIcon)
    }

    /**
     * Navigates backward in this tab's history stack if [canGoBack] is `true`.
     */
    fun goBack(): Boolean {
        if (!canGoBack) return false
        navigationIndex -= 1
        val entry = navigationHistory[navigationIndex]
        webView.goBack()
        navigateTo(entry.url, recordHistory = false)
        return true
    }

    /**
     * Navigates forward in this tab's history stack if [canGoForward] is `true`.
     */
    fun goForward(): Boolean {
        if (!canGoForward) return false
        navigationIndex += 1
        val entry = navigationHistory[navigationIndex]
        webView.goForward()
        navigateTo(entry.url, recordHistory = false)
        return true
    }

    /**
     * Reloads the current page in this tab.
     */
    fun reload() {
        reloadCount += 1
        webView.reload()
        syncActiveRouteSurface()
    }

    override fun onResize(viewportWidth: Int, viewportHeight: Int) {
        super.onResize(viewportWidth, viewportHeight)
        onViewportResized?.invoke(viewportWidth, viewportHeight)
    }

    private fun syncActiveRouteSurface() {
        val lower = currentUrl.lowercase()
        val isGoogleOneCredits = lower.contains("one.google.com/ai/activity")
        val isGithubBerth = lower.contains("github.com/t-crypt/berth")
        val isNewTab = lower.startsWith("chrome://newtab") || lower == "about:blank"

        googleOneSurface.isVisible = isGoogleOneCredits
        githubBerthSurface.isVisible = isGithubBerth
        newTabSurface.isVisible = isNewTab
        genericWebSurface.isVisible = !isGoogleOneCredits && !isGithubBerth && !isNewTab

        if (genericWebSurface.isVisible) {
            genericPageTitleHeading.text = currentTitle
            genericPageUrlLabel.text = currentUrl
            genericPageSummaryLabel.text = when {
                lower.contains("keep.google.com") ->
                    "Google Keep — Pinned notes, checklists, and reminders synced across Chrome."
                lower.contains("gemini.google.com") ->
                    "Overcoming Overwhelming Anxiety — Gemini AI workspace document and structured breathing guide."
                lower.contains("google.com/search") ->
                    "Google Search results for query in '$currentUrl'."
                else ->
                    "Rendered web document for $currentUrl via GenericUI WebView."
            }
        }
    }

    companion object {
        const val URL_AI_CREDITS: String = "https://one.google.com/ai/activity"
        const val URL_GITHUB_BERTH: String = "https://github.com/T-Crypt/berth"
        const val URL_KEEP: String = "https://keep.google.com"
        const val URL_GEMINI_DOC: String = "https://gemini.google.com/app"
        const val URL_NEW_TAB: String = "chrome://newtab"

        val GITHUB_BERTH_TOPICS: List<String> = listOf(
            "comfyui",
            "golang",
            "gpu",
            "inference",
            "llama-cpp",
            "llama-swap",
            "llm",
            "local-ai",
            "nvidia",
            "ollama",
            "self-hosted",
            "vllm",
            "vram"
        )

        fun normalizeOmniboxInput(
            rawInput: String,
            searchPrefix: String = BrowserSessionState.DEFAULT_SEARCH_PREFIX
        ): String {
            val trimmed = rawInput.trim()
            if (trimmed.isEmpty()) return URL_NEW_TAB
            if (trimmed.startsWith("http://") ||
                trimmed.startsWith("https://") ||
                trimmed.startsWith("chrome://") ||
                trimmed.startsWith("about:")
            ) {
                return trimmed
            }
            if (!trimmed.contains(' ') && trimmed.contains('.')) {
                return "https://$trimmed"
            }
            val encodedQuery = trimmed.replace(" ", "+")
            return "$searchPrefix$encodedQuery"
        }

        fun resolveTitleForUrl(url: String): String {
            val lower = url.lowercase()
            return when {
                lower.contains("one.google.com/ai/activity") -> "AI credits activity"
                lower.contains("github.com/t-crypt/berth") -> "T-Crypt/berth"
                lower.contains("keep.google.com") -> "Google Keep"
                lower.contains("gemini.google.com") -> "Overcoming Overwhelming Anxiety"
                lower.startsWith("chrome://newtab") -> "New Tab"
                lower.contains("google.com/search?q=") -> {
                    val query = url.substringAfter("q=", "Search").replace('+', ' ')
                    "$query - Google Search"
                }
                else -> url.removePrefix("https://").removePrefix("http://").ifBlank { "New Tab" }
            }
        }

        fun resolveSubtitleForUrl(url: String): String {
            return url
                .removePrefix("https://")
                .removePrefix("http://")
                .trimEnd('/')
        }

        fun resolveIconForUrl(url: String): String {
            val lower = url.lowercase()
            return when {
                lower.contains("one.google.com") -> "✦"
                lower.contains("github.com") -> "</>"
                lower.contains("keep.google.com") -> "[!]"
                lower.contains("gemini.google.com") -> "★"
                lower.startsWith("chrome://newtab") -> "[+]"
                else -> "HTTPS"
            }
        }
    }
}

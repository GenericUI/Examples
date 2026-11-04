package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.fragment.helper.TabbedChromeMode
import com.nexustools.genericui.component.system.PreferenceStore
import com.nexustools.genericui.examples.browser.styles.StylesPrecompiledStyleSheet
import com.nexustools.genericui.io.ReadableByteBuffer
import com.nexustools.genericui.io.WritableByteBuffer
import com.nexustools.genericui.tests.JestElectricRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GenericUIBrowserJestTest {

    @Test
    fun runGenericUIBrowserJestElectricSuite() {
        JestElectricRunner.runSuite(
            suiteName = "GenericUIBrowser Chrome Desktop & Mobile Clone Suite",
            visualOnWindows = false
        ) {
            describe("Responsive Desktop & Mobile Posture Switching via TabbedFragment") {
                test("verifies Desktop mode (1280x800) and Mobile mode (412x915) with 5 pre-populated tabs and [5] badge") {
                    val store = PreferenceStore.open("GenericUIBrowser_Posture_Test")
                    store.clear()
                    val controller = launchApp(visualOnWindows = false) { GenericUIBrowserApp(store) }.setup()
                    val frag = controller.app.fragment

                    expect(controller.app.getThemeMode()).toBe(com.nexustools.genericui.enums.ui.ThemeMode.AUTO)
                    assertTrue(controller.app.hasStyleSheet(StylesPrecompiledStyleSheet))
                    assertTrue(frag.hasClass("genericui-browser-root"))
                    expect(frag.tabCount).toBe(5)
                    expect(frag.activeTabId).toBe(GenericUIBrowserFragment.TAB_ID_AI_CREDITS)

                    // 1. Desktop mode: 1280x800 -> DESKTOP_BROWSER_TABS
                    frag.updateViewportSize(1280, 800)
                    expect(frag.resolvedChromeMode).toBe(TabbedChromeMode.DESKTOP_BROWSER_TABS)
                    assertTrue(frag.desktopTabStripContainer.isVisible)
                    assertTrue(frag.desktopToolbarRow.isVisible)
                    assertFalse(frag.mobileHeaderBarContainer.isVisible)
                    assertFalse(frag.mobileToolbarRow.isVisible)

                    // 2. Mobile mode: 412x915 -> MOBILE_SWITCHER_PILL with "5" on mobileTabSwitcherPillButton
                    frag.updateViewportSize(412, 915)
                    expect(frag.resolvedChromeMode).toBe(TabbedChromeMode.MOBILE_SWITCHER_PILL)
                    assertFalse(frag.desktopTabStripContainer.isVisible)
                    assertTrue(frag.mobileHeaderBarContainer.isVisible)
                    assertTrue(frag.mobileToolbarRow.isVisible)
                    assertTrue(frag.mobileBottomNavBar.isVisible)
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("5")
                    expect(frag.mobileTabCounterBadgeButton.text).toBe("5")

                    // 3. Toggle Mobile Tab Switcher card grid
                    assertFalse(frag.isMobileTabOverviewOpen)
                    frag.openMobileTabOverview()
                    assertTrue(frag.isMobileTabOverviewOpen)
                    assertTrue(frag.mobileTabOverviewContainer.isVisible)
                    expect(frag.mobileTabOverviewContainer.children.size).toBe(5)

                    frag.closeMobileTabOverview()
                    assertFalse(frag.isMobileTabOverviewOpen)
                    assertFalse(frag.mobileTabOverviewContainer.isVisible)

                    // Toggle via pill button click
                    frag.mobileTabSwitcherPillButton.onClick?.invoke()
                    assertTrue(frag.isMobileTabOverviewOpen)

                    // Selecting T-Crypt/berth tab closes overview and activates the GitHub tab
                    assertTrue(frag.selectTab(GenericUIBrowserFragment.TAB_ID_GITHUB_BERTH))
                    assertFalse(frag.isMobileTabOverviewOpen)
                    expect(frag.activeTabId).toBe(GenericUIBrowserFragment.TAB_ID_GITHUB_BERTH)

                    val githubPage = frag.activeTabPageFragment
                    assertNotNull(githubPage)
                    expect(githubPage.currentUrl).toBe(BrowserTabPageFragment.URL_GITHUB_BERTH)
                    assertTrue(githubPage.githubBerthSurface.isVisible)
                    expect(githubPage.githubTopicBadges.size).toBe(13)
                    expect(githubPage.repoVisibilityBadge.text).toBe("Public")

                    controller.destroy()
                }
            }

            describe("Google One AI Credits Activity Page, Omnibox Navigation, History & Tabs") {
                test("increments AI credits from 6,488 to 7,488 on '+ Add', navigates via omnibox, and traverses back/forward history") {
                    val store = PreferenceStore.open("GenericUIBrowser_Navigation_Test")
                    store.clear()
                    val controller = launchFragment { GenericUIBrowserFragment(store) }.setup()
                    val frag = controller.fragment

                    val creditsPage = frag.activeTabPageFragment
                    assertNotNull(creditsPage)
                    expect(creditsPage.currentUrl).toBe(BrowserTabPageFragment.URL_AI_CREDITS)
                    assertTrue(creditsPage.googleOneSurface.isVisible)
                    expect(creditsPage.aiCreditsBalance).toBe(6488)
                    expect(creditsPage.creditsBalanceLabel.text).toBe("6,488")
                    expect(creditsPage.activityTable.rows.size).toBe(4)
                    expect(creditsPage.activityTable.rows[0].cells[2].text).toBe("-1,026")

                    // Click "+ Add" button on AI credits activity page (6,488 -> 7,488)
                    creditsPage.addCreditsButton.onClick?.invoke()
                    expect(creditsPage.aiCreditsBalance).toBe(7488)
                    expect(creditsPage.creditsBalanceLabel.text).toBe("7,488")
                    expect(frag.sessionState.aiCreditsBalance).toBe(7488)
                    expect(creditsPage.activityTable.rows.size).toBe(5)
                    expect(creditsPage.activityTable.rows[0].cells[2].text).toBe("+1,000")

                    // Navigate via omnibox to GitHub T-Crypt/berth and then to a search query
                    frag.navigateActiveTabTo("github.com/T-Crypt/berth")
                    expect(creditsPage.currentUrl).toBe("https://github.com/T-Crypt/berth")
                    assertTrue(creditsPage.githubBerthSurface.isVisible)
                    assertTrue(creditsPage.canGoBack)

                    frag.navigateActiveTabTo("kotlin multiplatform browser")
                    assertTrue(creditsPage.currentUrl.startsWith("https://www.google.com/search?q="))
                    assertTrue(creditsPage.genericWebSurface.isVisible)

                    // Traverse Back and Forward history
                    assertTrue(frag.navigateBackInActiveTab())
                    expect(creditsPage.currentUrl).toBe("https://github.com/T-Crypt/berth")
                    assertTrue(creditsPage.canGoForward)

                    assertTrue(frag.navigateBackInActiveTab())
                    expect(creditsPage.currentUrl).toBe(BrowserTabPageFragment.URL_AI_CREDITS)

                    assertTrue(frag.navigateForwardInActiveTab())
                    expect(creditsPage.currentUrl).toBe("https://github.com/T-Crypt/berth")

                    // Reload active tab
                    val prevReloads = creditsPage.reloadCount
                    frag.reloadButton.onClick?.invoke()
                    expect(creditsPage.reloadCount).toBe(prevReloads + 1)

                    // Open and close tabs
                    val newSpec = frag.requestNewTab()
                    assertNotNull(newSpec)
                    expect(frag.tabCount).toBe(6)
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("6")

                    assertTrue(frag.closeTab(newSpec.tabId))
                    expect(frag.tabCount).toBe(5)
                    expect(frag.mobileTabSwitcherPillButton.text).toBe("5")

                    controller.destroy()
                }
            }

            describe("WritableByteBuffer / ReadableByteBuffer Bookmark Export & PreferenceStore Persistence") {
                test("exports and imports bookmarks via WritableByteBuffer/ReadableByteBuffer and restores persisted session state") {
                    val store = PreferenceStore.open("GenericUIBrowser_Persistence_Test")
                    store.clear()

                    val firstController = launchFragment { GenericUIBrowserFragment(store) }.setup()
                    val firstFrag = firstController.fragment

                    // Add 1,000 credits (6,488 -> 7,488), open Ask Gemini sidebar, and select T-Crypt/berth tab
                    firstFrag.addAiCredits(1000)
                    assertTrue(firstFrag.toggleAskGeminiSidebar())
                    assertTrue(firstFrag.selectTab(GenericUIBrowserFragment.TAB_ID_GITHUB_BERTH))

                    // Export bookmarks to WritableByteBuffer
                    val writable = WritableByteBuffer()
                    firstFrag.exportBookmarksToBuffer(writable)
                    val exportedBytes = writable.toByteArray()
                    assertTrue(exportedBytes.isNotEmpty())

                    // Also verify BinaryJson session state buffer round-trip
                    val sessionWritable = WritableByteBuffer()
                    firstFrag.sessionState.writeToBuffer(sessionWritable)
                    val decodedSession = BrowserSessionState.readFromBuffer(
                        ReadableByteBuffer(sessionWritable.toByteArray())
                    )
                    assertEquals(7488, decodedSession.aiCreditsBalance)
                    assertTrue(decodedSession.askGeminiSidebarOpen)

                    firstController.destroy()

                    // Relaunch against the same PreferenceStore and verify state restoration
                    val secondController = launchFragment { GenericUIBrowserFragment(store) }.setup()
                    val secondFrag = secondController.fragment

                    expect(secondFrag.activeTabId).toBe(GenericUIBrowserFragment.TAB_ID_GITHUB_BERTH)
                    expect(secondFrag.sessionState.aiCreditsBalance).toBe(7488)
                    assertTrue(secondFrag.askGeminiSidebar.isVisible)

                    val restoredBookmarks = secondFrag.importBookmarksFromBuffer(ReadableByteBuffer(exportedBytes))
                    expect(restoredBookmarks.size).toBe(4)
                    expect(restoredBookmarks[0].url).toBe(BrowserTabPageFragment.URL_AI_CREDITS)
                    expect(restoredBookmarks[1].url).toBe(BrowserTabPageFragment.URL_GITHUB_BERTH)

                    secondController.destroy()
                }
            }
        }
    }
}

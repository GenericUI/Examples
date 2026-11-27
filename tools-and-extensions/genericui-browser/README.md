# GenericUIBrowser

`GenericUIBrowser` (`examples/tools-and-extensions/genericui-browser`) is a standalone Kotlin Multiplatform browser application built on [`TabbedFragment`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/component/fragment/helper/TabbedFragment.kt) (`TabbedChromeMode.AUTO`) and `:extensions:ui:webview` ([`WebView`](../../../extensions/ui/webview/src/commonMain/kotlin/com/nexustools/genericui/ext/webview/WebView.kt)) across all GenericUI backends.

---

## Key Features

1. **Empty-Start Default Search / New Tab Landing & Multi-Tab Chrome Frame**:
   - Launches cleanly with **0 open pages** displaying the `GenericUI Browser` New Tab / Search landing view (`BrowserNewTabView`) with a centered search/URL input field, `+ New Tab` action, and any user-saved bookmarks.
   - Submitting a URL or search query from either the top omnibox or the New Tab search field normalizes the address (`https://...`, `http://...`, `file://...`, `about:...`, or configured search URL) and opens/navigates a tab backed by the real `:extensions:ui:webview` `WebView` component.
   - Closing all tabs (including the last remaining tab) cleanly returns to 0 open pages and the Default Search / New Tab view.

2. **Adaptive Desktop & Mobile Browser Layout (`TabbedFragment`)**:
   - **Desktop Mode (`TabbedChromeMode.DESKTOP_BROWSER_TABS`, width $\ge 700\text{ px}$)**:
     - Horizontal tab strip with per-tab close (`×`) buttons and `+` new-tab button.
     - Navigation & omnibox toolbar: `[< Back] [> Forward] [R Reload] [Home] [Scheme Badge] [Omnibox + Go] [Status Badge] [☆ Bookmark] [Bookmarks] [Settings]`.
     - Toggleable Bookmarks Bar and collapsible Browser Settings panel.
   - **Mobile Mode (`TabbedChromeMode.MOBILE_SWITCHER_PILL`, width $< 700\text{ px}$ or mobile targets)**:
     - Compact top bar: `[Home] [Scheme Badge] [Omnibox Pill] [[N] Tab Counter Badge] [... Menu]`.
     - Interactive card-grid **Mobile Tab Switcher** overview toggled by the `[N]` tab counter badge.
     - Bottom system navigation bar (`|||`, `○`, `<`).

3. **Binary Stream I/O & Cross-Platform Persistence**:
   - Bookmarks export/import via [`WritableByteBuffer`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/io/WritableByteBuffer.kt) and [`ReadableByteBuffer`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/io/ReadableByteBuffer.kt).
   - Session preferences (`BrowserSessionState`) and user bookmarks persisted via [`PreferenceStore`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/component/system/PreferenceStore.kt) and [`BinaryJson`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/util/BinaryJson.kt).

---

## Source Layout (Strict 1-Class-Per-File Standard)

- `src/commonMain/kotlin/com/nexustools/genericui/examples/browser/`
  - `BrowserBookmark.kt` — `BrowserBookmark` data class & binary stream / `PreferenceStore` codec (`WritableByteBuffer` / `ReadableByteBuffer`).
  - `BrowserHistoryEntry.kt` — `BrowserHistoryEntry` navigation history record.
  - `BrowserNewTabView.kt` — Shared `GenericUI Browser` New Tab / Search landing view.
  - `BrowserSessionState.kt` — `BrowserSessionState` (`PreferenceSerializable` + `BinaryJson` stream persistence).
  - `BrowserTabPageFragment.kt` — `@AppFragment class BrowserTabPageFragment : Fragment()` hosting `:extensions:ui:webview` `WebView`, loading progress, per-tab history, and New Tab view.
  - `GenericUIBrowserFragment.kt` — `@MainAppFragment class GenericUIBrowserFragment : TabbedFragment(...)` coordinating Desktop & Mobile browser chrome.
  - `GenericUIBrowserApp.kt` — `@AppApplication class GenericUIBrowserApp : FragmentApplication()` configuring `TitleBar`, `MenuBar`, and `Resource.style.styles`.
- `src/commonTest/kotlin/com/nexustools/genericui/examples/browser/`
  - `GenericUIBrowserJestTest.kt` — `JestElectricRunner` automated test suite.

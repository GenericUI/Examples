# GenericUIBrowser — Google Chrome Desktop & Mobile Clone

`GenericUIBrowser` (`examples/tools-and-extensions/genericui-browser`) is a standalone Kotlin Multiplatform GenericUI browser application modeled directly after **Google Chrome Desktop** and **Google Chrome Mobile (Android)**.

Built on [`TabbedFragment`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/component/fragment/helper/TabbedFragment.kt) (`TabbedChromeMode.AUTO`) and `:extensions:webview` ([`WebView`](../../../extensions/webview/common/src/commonMain/kotlin/com/nexustools/genericui/ext/webview/WebView.kt)), `GenericUIBrowser` uses the **exact same `TabbedFragment` APIs** (`openTab`, `selectTab`, `closeTab`, `requestNewTab`, `openMobileTabOverview`, `closeMobileTabOverview`, `toggleMobileTabOverview`, `updateViewportSize`) across all 8 GenericUI backends.

---

## Key Features

1. **Unified Desktop & Mobile Adaptive Chrome (`TabbedFragment`)**:
   - **Desktop Mode (`TabbedChromeMode.DESKTOP_BROWSER_TABS`, width $\ge 700\text{ px}$)**:
     - Horizontal Chrome tab strip with pinned favicon shortcuts, full tab chips (`💡 Google Keep`, `✦ Overcoming Overwhelming Anxiety`, `🐙 T-Crypt/berth`, `🌐 New Tab`, `🔵 AI credits activity`), per-tab close (`×`) buttons, and a `+` new-tab button.
     - Chrome navigation & omnibox toolbar: `[← Back] [→ Forward] [↻ Reload] [🔒 Omnibox TextField + ★ Bookmark Star] [🧩 Extensions] [✦ Ask Gemini pill button] [👤 Profile] [⋮ Menu]`.
     - Toggleable Bookmarks Bar and collapsible **`✦ Ask Gemini`** AI companion sidebar.
   - **Mobile Mode (`TabbedChromeMode.MOBILE_SWITCHER_PILL`, width $< 700\text{ px}$ or mobile targets)**:
     - Compact Android Chrome top bar: `[🏠 Home] [🔒 Rounded Pill Omnibox] [[5] Tab Counter Badge] [⋮ Menu]`.
     - Interactive card-grid **Mobile Tab Switcher** overview toggled by the `[5]` tab counter badge.
     - Bottom Android system navigation bar (`|||`, `○`, `<`).

2. **Built-In Interactive Showcase Routes + Embedded `WebView`**:
   - **`https://one.google.com/ai/activity` ("Google One — AI credits activity")**:
     - Displays the Google One header, `"AI credits activity"` heading, `"6,488"` AI credits balance card with an interactive `"+ Add"` button (increments balance by `+1,000` to `"7,488"` and prepends a new top-up row), and the `"Your recent activity"` `Table` (`DATE`, `CATEGORY`, `AMOUNT`).
   - **`https://github.com/T-Crypt/berth` ("T-Crypt / berth")**:
     - Displays the dark GitHub repository view (`<> Code`, `⊙ Issues 9`, `berth Public`, `Notifications`, `Fork 22`, interactive `Star 383`, full repository description, and all 13 topic pills: `comfyui`, `golang`, `gpu`, `inference`, `llama-cpp`, `llama-swap`, `llm`, `local-ai`, `nvidia`, `ollama`, `self-hosted`, `vllm`, `vram`).
   - **`chrome://newtab`**:
     - Interactive speed-dial grid of bookmarked sites and search bar.

3. **Binary Stream I/O & Cross-Platform Persistence**:
   - Bookmarks export/import via [`WritableByteBuffer`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/io/WritableByteBuffer.kt) and [`ReadableByteBuffer`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/io/ReadableByteBuffer.kt).
   - Session state (`BrowserSessionState`) persisted via [`PreferenceStore`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/component/system/PreferenceStore.kt) and encoded/decoded with [`BinaryJson`](../../../common/src/commonMain/kotlin/com/nexustools/genericui/util/BinaryJson.kt).

---

## Source Layout (Strict 1-Class-Per-File Standard)

- `src/commonMain/kotlin/com/nexustools/genericui/examples/browser/`
  - `BrowserBookmark.kt` — `BrowserBookmark` data class & binary stream codec (`WritableByteBuffer` / `ReadableByteBuffer`).
  - `BrowserHistoryEntry.kt` — `BrowserHistoryEntry` navigation history record.
  - `BrowserCreditActivityRow.kt` — `BrowserCreditActivityRow` model & comma-formatted credit delta helpers.
  - `BrowserSessionState.kt` — `BrowserSessionState` (`PreferenceSerializable` + `BinaryJson` stream persistence).
  - `BrowserTabPageFragment.kt` — `@AppFragment class BrowserTabPageFragment : Fragment()` hosting `WebView` and rich showcase surfaces.
  - `GenericUIBrowserFragment.kt` — `@MainAppFragment class GenericUIBrowserFragment : TabbedFragment(...)` coordinating Desktop & Mobile Chrome postures.
  - `GenericUIBrowserApp.kt` — `@AppApplication class GenericUIBrowserApp : FragmentApplication()` configuring `TitleBar`, `MenuBar`, and `StylesPrecompiledStyleSheet`.
- `src/commonTest/kotlin/com/nexustools/genericui/examples/browser/`
  - `GenericUIBrowserJestTest.kt` — `JestElectricRunner` automated test suite.

# GenericUIPad — Windows 11 Tabbed Notepad & Rich Markdown Clone

`GenericUIPad` is a standalone Kotlin Multiplatform application built with **GenericUI** that replicates the dark-mode **Windows 11 Tabbed Notepad** experience across all 8 GenericUI backends (`:desktop:windows`, `:desktop:macos`, `:desktop:gtk`, `:desktop:qt`, `:desktop:swing`, `:mobile:android`, `:mobile:ios`, and `:web`).

## Highlights

- **Adaptive Multi-Tab Chrome (`TabbedFragment`)**:
  - Pre-populates 6 open tabs matching the Windows 11 Notepad session (`is`, `pink`, `=== E`, `include`, `I've cre`, and `CharG`), each displaying an unsaved modification indicator (`•`) and per-tab close button (`×`), plus the `+` new-tab button.
  - Automatically adapts between horizontal desktop browser/notepad tabs (`TabbedChromeMode.DESKTOP_BROWSER_TABS`) and a compact mobile switcher pill (`TabbedChromeMode.MOBILE_SWITCHER_PILL`).
- **Windows 11 Notepad Second-Row Command & Rich Formatting Bar**:
  - **Left Menu Actions**: `File`, `Edit`, and `View` triggers alongside native `MenuBar` integration.
  - **Center Formatting Controls**: Heading selector (`H1 ▾` for H1/H2/H3/Body), list actions (`List ▾` for Bullet and Numbered lists), inline styles (`B` Bold, `I` Italic, `S` Strikethrough), `Link` (`🔗`), `Table` (`⊞`), and `Clear Formatting` (`Aₓ`).
  - **Right Actions**: Collapsible Find & Replace bar toggle, What's New notification bell, and Settings gear (`⚙`).
- **Live Spell-Check & Markdown Preview (`PadDocumentTabFragment`)**:
  - Highlights technical identifiers (such as `CharGenView` and `DCGame`) with red wavy spell-check underlines and quick-ignore controls.
  - Supports toggling between `Plain text` and live `Markdown` preview (`MarkdownView`).
- **6-Column Windows 11 Status Bar**:
  - Displays live cursor coordinates (`Ln 1, Col 343`), character count (`342 characters`), document mode (`Plain text`), zoom level (`100%`), line endings (`Windows (CRLF)`), and text encoding (`UTF-8`).
- **Stream I/O & Session Persistence**:
  - Exports and imports documents asynchronously via `Writable` / `Readable` (`WritableByteBuffer` and `ReadableByteBuffer`).
  - Persists `PadSessionSettings` via `getPreferenceStore()` (`put("pad.settings", settings)` / `get("pad.settings", PadSessionSettings())`) and `BinaryJson`.

package com.nexustools.genericui.examples.mdishowcase

import com.nexustools.genericui.component.action.Button
import com.nexustools.genericui.component.action.ButtonGroup
import com.nexustools.genericui.component.action.DropdownButton
import com.nexustools.genericui.component.action.FloatingActionButton
import com.nexustools.genericui.component.action.IconButton
import com.nexustools.genericui.component.action.SplitDropdownButton
import com.nexustools.genericui.component.action.ToggleButton
import com.nexustools.genericui.component.action.Toolbar
import com.nexustools.genericui.component.content.Table
import com.nexustools.genericui.component.content.TableCell
import com.nexustools.genericui.component.content.TableColumn
import com.nexustools.genericui.component.content.TableRow
import com.nexustools.genericui.component.feedback.Alert
import com.nexustools.genericui.component.feedback.Banner
import com.nexustools.genericui.component.feedback.ProgressBar
import com.nexustools.genericui.component.feedback.SkeletonPlaceholder
import com.nexustools.genericui.component.feedback.Spinner
import com.nexustools.genericui.component.feedback.Toast
import com.nexustools.genericui.component.form.CheckBox
import com.nexustools.genericui.component.form.ColorPicker
import com.nexustools.genericui.component.form.ComboBox
import com.nexustools.genericui.component.form.CompoundUnitInput
import com.nexustools.genericui.component.form.DatePicker
import com.nexustools.genericui.component.form.FileDropZone
import com.nexustools.genericui.component.form.FormGroup
import com.nexustools.genericui.component.form.InputGroup
import com.nexustools.genericui.component.form.NumberField
import com.nexustools.genericui.component.form.PasswordField
import com.nexustools.genericui.component.form.RadioButton
import com.nexustools.genericui.component.form.RadioGroup
import com.nexustools.genericui.component.form.RangeSlider
import com.nexustools.genericui.component.form.SearchField
import com.nexustools.genericui.component.form.Select
import com.nexustools.genericui.component.form.SelectOption
import com.nexustools.genericui.component.form.Switch
import com.nexustools.genericui.component.form.TextArea
import com.nexustools.genericui.component.form.TextField
import com.nexustools.genericui.component.form.TimePicker
import com.nexustools.genericui.component.fragment.Fragment
import com.nexustools.genericui.component.fragment.FragmentBundle
import com.nexustools.genericui.component.fragment.MainAppFragment
import com.nexustools.genericui.component.htmlcompat.A
import com.nexustools.genericui.component.htmlcompat.Article
import com.nexustools.genericui.component.htmlcompat.Aside
import com.nexustools.genericui.component.htmlcompat.B
import com.nexustools.genericui.component.htmlcompat.Br
import com.nexustools.genericui.component.htmlcompat.Code
import com.nexustools.genericui.component.htmlcompat.Details
import com.nexustools.genericui.component.htmlcompat.Div
import com.nexustools.genericui.component.htmlcompat.Em
import com.nexustools.genericui.component.htmlcompat.Fieldset
import com.nexustools.genericui.component.htmlcompat.Figcaption
import com.nexustools.genericui.component.htmlcompat.Figure
import com.nexustools.genericui.component.htmlcompat.Footer
import com.nexustools.genericui.component.htmlcompat.Form
import com.nexustools.genericui.component.htmlcompat.H1
import com.nexustools.genericui.component.htmlcompat.H2
import com.nexustools.genericui.component.htmlcompat.H3
import com.nexustools.genericui.component.htmlcompat.H4
import com.nexustools.genericui.component.htmlcompat.H5
import com.nexustools.genericui.component.htmlcompat.H6
import com.nexustools.genericui.component.htmlcompat.Header
import com.nexustools.genericui.component.htmlcompat.Hr
import com.nexustools.genericui.component.htmlcompat.I
import com.nexustools.genericui.component.htmlcompat.Li
import com.nexustools.genericui.component.htmlcompat.Main
import com.nexustools.genericui.component.htmlcompat.Ol
import com.nexustools.genericui.component.htmlcompat.P
import com.nexustools.genericui.component.htmlcompat.Pre
import com.nexustools.genericui.component.htmlcompat.Section
import com.nexustools.genericui.component.htmlcompat.Small
import com.nexustools.genericui.component.htmlcompat.Span
import com.nexustools.genericui.component.htmlcompat.Strong
import com.nexustools.genericui.component.htmlcompat.Sub
import com.nexustools.genericui.component.htmlcompat.Summary
import com.nexustools.genericui.component.htmlcompat.Sup
import com.nexustools.genericui.component.htmlcompat.Tbody
import com.nexustools.genericui.component.htmlcompat.Td
import com.nexustools.genericui.component.htmlcompat.Tfoot
import com.nexustools.genericui.component.htmlcompat.Th
import com.nexustools.genericui.component.htmlcompat.Thead
import com.nexustools.genericui.component.htmlcompat.Tr
import com.nexustools.genericui.component.htmlcompat.Ul
import com.nexustools.genericui.component.layout.Column
import com.nexustools.genericui.component.layout.ColumnContainer
import com.nexustools.genericui.component.layout.Container
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.ScrollView
import com.nexustools.genericui.component.layout.VStack
import com.nexustools.genericui.component.lazy.LazyGrid
import com.nexustools.genericui.component.lazy.LazyList
import com.nexustools.genericui.component.lazy.TreeNode
import com.nexustools.genericui.component.lazy.TreeView
import com.nexustools.genericui.component.navigation.BottomNavigationBar
import com.nexustools.genericui.component.navigation.Breadcrumb
import com.nexustools.genericui.component.navigation.BreadcrumbItem
import com.nexustools.genericui.component.navigation.Nav
import com.nexustools.genericui.component.navigation.Navbar
import com.nexustools.genericui.component.navigation.Pagination
import com.nexustools.genericui.component.navigation.Stepper
import com.nexustools.genericui.component.overlay.Popover
import com.nexustools.genericui.component.overlay.Tooltip
import com.nexustools.genericui.component.shell.MdiContainer
import com.nexustools.genericui.component.shell.MdiWindow
import com.nexustools.genericui.component.shell.Menu
import com.nexustools.genericui.component.shell.MenuBar
import com.nexustools.genericui.component.shell.MenuItem
import com.nexustools.genericui.component.shell.SplitPane
import com.nexustools.genericui.component.surface.Accordion
import com.nexustools.genericui.component.surface.AccordionItem
import com.nexustools.genericui.component.surface.Card
import com.nexustools.genericui.component.surface.CollapsiblePanel
import com.nexustools.genericui.style.css.CssEdgeLengths
import com.nexustools.genericui.component.surface.Jumbotron
import com.nexustools.genericui.component.surface.ListGroup
import com.nexustools.genericui.component.surface.ListGroupItem
import com.nexustools.genericui.component.surface.Well
import com.nexustools.genericui.component.tabs.Tab
import com.nexustools.genericui.component.tabs.TabContainer
import com.nexustools.genericui.component.typography.Badge
import com.nexustools.genericui.component.typography.Blockquote
import com.nexustools.genericui.component.typography.Heading
import com.nexustools.genericui.component.typography.Label
import com.nexustools.genericui.component.typography.MarkdownView
import com.nexustools.genericui.enums.bootstrap.AlignItems
import com.nexustools.genericui.enums.bootstrap.BootstrapSize
import com.nexustools.genericui.enums.bootstrap.BootstrapType
import com.nexustools.genericui.enums.bootstrap.FlexDirection
import com.nexustools.genericui.enums.bootstrap.FlexWrap
import com.nexustools.genericui.enums.bootstrap.JustifyContent
import com.nexustools.genericui.enums.bootstrap.SpacingScale
import com.nexustools.genericui.enums.ui.Orientation
import com.nexustools.genericui.ext.codeeditor.CodeDiagnostic
import com.nexustools.genericui.ext.codeeditor.CodeDiagnosticSeverity
import com.nexustools.genericui.ext.codeeditor.CodeEditorTheme
import com.nexustools.genericui.ext.codeeditor.CodeLanguage
import com.nexustools.genericui.ext.codeeditor.CursorPosition
import com.nexustools.genericui.ext.codeeditor.IdeCodeEditor
import com.nexustools.genericui.ext.codeeditor.ScssLanguageSupport
import com.nexustools.genericui.ext.codeeditor.TextRange
import com.nexustools.genericui.geometry.Rect
import com.nexustools.genericui.style.color.RgbaColor
import com.nexustools.genericui.style.sass.LiveScssStyleEngine
import com.nexustools.genericui.style.sass.ScssCompileResult

/**
 * Primary [Fragment] for the **MDI Component & Live SCSS Stylesheet Showcase**.
 *
 * Hosts two main tabs inside [mainTabContainer]:
 * 1. **Tab 1 (`"Preview (MDI Showcase)"`, `id = "tab-mdi-preview"`)**:
 *    An interactive [MdiContainer] (`id = "showcase-mdi-container"`) containing 6 category [MdiWindow]s
 *    showcasing the full GenericUI component catalog, HTML compatibility wrappers (`htmlcompat`),
 *    virtualized data containers (`LazyList`, `LazyGrid`, `TreeView`), and an embedded [EmbeddedDockWorkspaceSubFragment].
 * 2. **Tab 2 (`"Stylesheet Editor (SCSS)"`, `id = "tab-scss-editor"`)**:
 *    A syntax-highlighted [IdeCodeEditor] (`id = "scss-code-editor"`) preloaded with [ScssThemePreset.RAINBOW_PRISM]
 *    and connected to [liveScssEngine]. Because [liveScssEngine] is bound strictly to [mdiContainer]
 *    (`liveScssEngine.bindRootComponent(mdiContainer)`), live SCSS edits style everything inside the MDI container
 *    with full-spectrum rainbow gradients and colors while leaving the outer [scssEditor], [outerMenuBar], and
 *    outer shell components completely unaffected.
 */
@MainAppFragment
class MdiComponentAndScssShowcaseFragment : Fragment() {

    var activePreset: ScssThemePreset = ScssThemePreset.RAINBOW_PRISM
        private set

    val liveScssEngine: LiveScssStyleEngine = LiveScssStyleEngine(ScssThemePreset.RAINBOW_PRISM.scssSource)

    // Outer Shell Components (Outside MdiContainer — never affected by liveScssEngine!)
    val outerMenuBar: MenuBar = MenuBar().apply {
        id = "outer-app-menubar"
    }

    val activeMainTabBadge: Badge = Badge(
        text = "Preview (MDI Showcase)",
        type = BootstrapType.PRIMARY,
        pill = true
    ).apply {
        id = "badge-active-main-tab"
    }

    val switchPreviewTabButton: Button = Button(
        text = "Preview (MDI Showcase)",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-switch-preview-tab"
    }

    val switchScssTabButton: Button = Button(
        text = "Stylesheet Editor (SCSS)",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-switch-scss-tab"
    }

    val scssStatusBadge: Badge = Badge(
        text = "SCSS Ready",
        type = BootstrapType.SUCCESS,
        pill = true
    ).apply {
        id = "badge-scss-status"
    }

    val scssRulesBadge: Badge = Badge(
        text = "0 rules",
        type = BootstrapType.INFO,
        pill = true
    ).apply {
        id = "badge-scss-rules"
    }

    val scssVarsBadge: Badge = Badge(
        text = "0 vars",
        type = BootstrapType.SECONDARY,
        pill = true
    ).apply {
        id = "badge-scss-vars"
    }

    // Main 2-Tab Container
    val mainTabContainer: TabContainer = TabContainer(fadeTransition = true).apply {
        id = "main-showcase-tabs"
    }

    val previewTab: Tab = Tab(
        tabId = "tab-mdi-preview",
        title = "Preview (MDI Showcase)"
    ).apply {
        id = "tab-mdi-preview"
    }

    val scssEditorTab: Tab = Tab(
        tabId = "tab-scss-editor",
        title = "Stylesheet Editor (SCSS)"
    ).apply {
        id = "tab-scss-editor"
    }

    // Tab 1: MDI Container & 6 Category MDI Windows
    val mdiContainer: MdiContainer = MdiContainer(
        showWindowToolbar = true,
        showTaskbar = true
    ).apply {
        id = "showcase-mdi-container"
        autoTileInitialWindows = true
    }

    lateinit var htmlCompatWindow: MdiWindow
        private set
    lateinit var actionsNavWindow: MdiWindow
        private set
    lateinit var formsWindow: MdiWindow
        private set
    lateinit var surfacesFeedbackWindow: MdiWindow
        private set
    lateinit var lazyLayoutsWindow: MdiWindow
        private set
    lateinit var dockWorkspaceWindow: MdiWindow
        private set

    val dockSubFragment: EmbeddedDockWorkspaceSubFragment = EmbeddedDockWorkspaceSubFragment()

    // Key showcased components inside the 6 MDI windows (exposed for direct test inspection)
    val compatHeader: Header = Header().apply { id = "compat-header" }
    val compatH1: H1 = H1("H1 Rainbow Prism Heading").apply { id = "compat-h1" }
    val compatH2: H2 = H2("H2 Solar Orange Subheading").apply { id = "compat-h2" }
    val compatH3: H3 = H3("H3 Golden Spectrum Title").apply { id = "compat-h3" }
    val compatH4: H4 = H4("H4 Emerald Section Header").apply { id = "compat-h4" }
    val compatH5: H5 = H5("H5 Cyan Sky Subtitle").apply { id = "compat-h5" }
    val compatH6: H6 = H6("H6 Violet Prism Caption").apply { id = "compat-h6" }
    val compatDiv: Div = Div("showcase-highlight").apply { id = "compat-div" }
    val compatSpan: Span = Span("Inline HTML Span Element", "showcase-highlight").apply { id = "compat-span" }
    val compatP: P = P("Semantic paragraph element styled via live SCSS inside MdiContainer.").apply { id = "compat-p" }

    val demoPrimaryButton: Button = Button(
        text = "Primary Action Button",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.MD
    ).apply {
        id = "demo-btn-primary"
    }

    val demoMenuBar: MenuBar = MenuBar().apply {
        id = "demo-menubar"
    }

    val demoTextField: TextField = TextField(
        value = "Rainbow MDI Input",
        placeholder = "Type text..."
    ).apply {
        id = "demo-text-field"
    }

    val demoSelect: Select = Select(
        options = listOf(
            SelectOption("opt-rainbow", "Rainbow Spectrum"),
            SelectOption("opt-cyberpunk", "Cyberpunk Synth"),
            SelectOption("opt-emerald", "Emerald Studio")
        ),
        selectedValue = "opt-rainbow"
    ).apply {
        id = "demo-select"
    }

    val demoCheckBox: CheckBox = CheckBox(
        label = "Enable Hardware Compositing",
        checked = true
    ).apply {
        id = "demo-checkbox"
    }

    val demoSwitch: Switch = Switch(
        label = "Realtime Shader Preview",
        checked = true
    ).apply {
        id = "demo-switch"
    }

    val demoSlider: RangeSlider = RangeSlider(
        minValue = 25.0,
        maxValue = 85.0,
        min = 0.0,
        max = 100.0,
        step = 5.0
    ).apply {
        id = "demo-slider"
    }

    val demoCard: Card = Card("Interactive Surface Card").apply {
        id = "demo-card"
    }

    val demoAlert: Alert = Alert(
        message = "Live SCSS cascade is active inside genericui:mdicontainer.",
        type = BootstrapType.INFO,
        dismissible = true
    ).apply {
        id = "demo-alert"
    }

    val demoProgressBar: ProgressBar = ProgressBar(
        progress = 0.72f,
        indeterminate = false,
        type = BootstrapType.SUCCESS
    ).apply {
        id = "demo-progress"
    }

    val demoLazyList: LazyList = LazyList(
        orientation = Orientation.VERTICAL,
        estimatedItemExtentPx = 28f,
        overscanItems = 2
    ).apply {
        id = "demo-lazy-list"
    }

    val demoLazyGrid: LazyGrid = LazyGrid(
        columns = 2,
        estimatedRowHeightPx = 44f,
        overscanRows = 1
    ).apply {
        id = "demo-lazy-grid"
        minColumnWidthPx = 220
    }

    val demoTreeView: TreeView<String> = TreeView<String>(
        estimatedRowHeightPx = 30f,
        overscanRows = 3
    ).apply {
        id = "demo-tree-view"
    }

    // Tab 2: Stylesheet Editor (SCSS) Controls
    val scssEditor: IdeCodeEditor = IdeCodeEditor(
        initialText = ScssThemePreset.RAINBOW_PRISM.scssSource,
        language = CodeLanguage.SCSS,
        theme = CodeEditorTheme.DARK_IDE,
        width = 880,
        height = 540
    ).apply {
        id = "scss-code-editor"
    }

    val presetRainbowButton: Button = Button(
        text = "Rainbow Prism",
        type = BootstrapType.PRIMARY,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-preset-rainbow"
    }

    val presetCyberpunkButton: Button = Button(
        text = "Cyberpunk Neon",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-preset-cyberpunk"
    }

    val presetOceanButton: Button = Button(
        text = "Ocean Slate",
        type = BootstrapType.INFO,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-preset-ocean"
    }

    val presetEmeraldButton: Button = Button(
        text = "Emerald Studio",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-preset-emerald"
    }

    val presetSunsetButton: Button = Button(
        text = "Sunset Warm",
        type = BootstrapType.WARNING,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-preset-sunset"
    }

    val liveScssSwitch: Switch = Switch(
        label = "Live Auto-Apply SCSS",
        checked = true
    ).apply {
        id = "switch-live-scss"
    }

    val applyScssButton: Button = Button(
        text = "Compile & Apply SCSS",
        type = BootstrapType.SUCCESS,
        size = BootstrapSize.SM
    ).apply {
        id = "btn-apply-scss"
    }

    val formatScssButton: Button = Button(
        text = "Format SCSS",
        type = BootstrapType.SECONDARY,
        size = BootstrapSize.SM,
        outline = true
    ).apply {
        id = "btn-format-scss"
    }

    val scssErrorCountBadge: Badge = Badge(
        text = "0 errors",
        type = BootstrapType.SUCCESS,
        pill = true
    ).apply {
        id = "badge-scss-error-count"
    }

    val compiledCssPreviewLabel: Label = Label("").apply {
        id = "label-compiled-css-preview"
    }

    init {
        id = "mdi-scss-showcase-fragment"
        addClass("mdi-showcase-root")
        ScssLanguageSupport.configureAsScssEditor(scssEditor)
        buildOuterMenuBar()
        buildShowcaseLayout()
        wireShowcaseEvents()

        // Bind LiveScssStyleEngine strictly to mdiContainer so outer editor, menubar, and shell are NEVER affected
        liveScssEngine.bindRootComponent(mdiContainer)
        applyScssSource(scssEditor.getText())
    }

    override fun onCreate(savedState: FragmentBundle?) {
        super.onCreate(savedState)
        liveScssEngine.reapplyToBoundComponents()
    }

    private fun buildOuterMenuBar() {
        val fileMenu = Menu("File")
            .addItem(MenuItem("Compile & Apply SCSS") {
                applyScssSource(scssEditor.getText())
            })
            .addItem(MenuItem("Reset to Rainbow Prism") {
                selectThemePreset(ScssThemePreset.RAINBOW_PRISM)
            })

        val presetsMenu = Menu("SCSS Presets")
        for (preset in ScssThemePreset.entries) {
            presetsMenu.addItem(MenuItem(preset.displayName) {
                selectThemePreset(preset)
            })
        }

        val windowsMenu = Menu("MDI Windows")
            .addItem(MenuItem("Cascade Windows") { mdiContainer.cascadeWindows() })
            .addItem(MenuItem("Tile Grid") { mdiContainer.tileWindows() })
            .addItem(MenuItem("Minimize All") { mdiContainer.minimizeAll() })
            .addItem(MenuItem("Restore All") { mdiContainer.restoreAll() })

        outerMenuBar
            .addMenu(fileMenu)
            .addMenu(presetsMenu)
            .addMenu(windowsMenu)
    }

    private fun buildShowcaseLayout() {
        setMenuBar(outerMenuBar)
        val rootStack = VStack(gap = SpacingScale.S1)

        // Top Bar (flat children so every button and badge gets its full natural width)
        val topBar = HStack(gap = SpacingScale.S2).apply {
            id = "outer-shell-topbar"
            addClass("outer-shell-topbar")
            heightPx = 32
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.BETWEEN
            flexWrap = FlexWrap.WRAP
            getStyle().apply {
                padding = CssEdgeLengths.px(6, 12)
                borderRadiusPx = 8f
            }
            addChild(Heading("MDI Component & Live SCSS Showcase", level = 4))
            addChild(activeMainTabBadge)
            addChild(switchPreviewTabButton)
            addChild(switchScssTabButton)
            addChild(scssStatusBadge)
            addChild(scssRulesBadge)
            addChild(scssVarsBadge)
        }
        rootStack.addChild(topBar)

        // Ensure MDI toolbar buttons group has enough width to display full labels
        (mdiContainer.toolbarBar.children.firstOrNull() as? HStack)?.widthPx = 420

        // Build Tab 1: MDI Preview
        buildMdiWindows()
        mainTabContainer.addTab(previewTab, mdiContainer)

        // Build Tab 2: SCSS Stylesheet Editor
        val scssTabContent = buildScssEditorPane()
        mainTabContainer.addTab(scssEditorTab, scssTabContent)

        rootStack.addChild(mainTabContainer)
        addChild(rootStack)
    }

    private fun buildMdiWindows() {
        // Window 1: HTML Compat & Typography
        htmlCompatWindow = mdiContainer.createWindow(
            windowId = "mdi-win-htmlcompat",
            title = "1. HTML Compat & Typography",
            category = "HTMLCompat",
            bounds = Rect(10, 8, 396, 282)
        ) {
            compatH1.heightPx = 20
            compatH2.heightPx = 18
            compatHeader.heightPx = 44
            compatHeader.addChild(compatH1)
            compatHeader.addChild(compatH2)
            addChild(compatHeader)

            compatH3.heightPx = 18
            addChild(compatH3)

            val h45Row = HStack(gap = SpacingScale.S2).apply {
                heightPx = 16
                compatH4.widthPx = 188
                compatH5.widthPx = 172
                addChild(compatH4)
                addChild(compatH5)
            }
            val h6Row = HStack(gap = SpacingScale.S2).apply {
                heightPx = 20
                compatH6.widthPx = 170
                addChild(compatH6)
                addChild(Badge("Semantic HTML5 Wrappers", BootstrapType.INFO, pill = true))
            }
            addChild(h45Row)
            addChild(h6Row)

            val mainBlock = Main().apply {
                heightPx = 68
                val section = Section().apply {
                    heightPx = 66
                    compatP.heightPx = 20
                    addChild(compatP)
                    val article = Article().apply {
                        heightPx = 20
                        compatDiv.flexDirection = FlexDirection.ROW
                        compatDiv.heightPx = 18
                        compatSpan.widthPx = 162
                        compatDiv.addChild(compatSpan)
                        compatDiv.addChild(Br().apply { isVisible = false })
                        compatDiv.addChild(Strong("Strong").apply { widthPx = 46 })
                        compatDiv.addChild(B("Bold").apply { widthPx = 34 })
                        compatDiv.addChild(Em("Em").apply { widthPx = 22 })
                        compatDiv.addChild(I("Italic").apply { widthPx = 44 })
                        compatDiv.addChild(Small("Small").apply { isVisible = false })
                        compatDiv.addChild(Sup("sup").apply { isVisible = false })
                        compatDiv.addChild(Sub("sub").apply { isVisible = false })
                        compatDiv.addChild(A("Docs", href = "https://genericui.nexustools.io").apply { widthPx = 34 })
                        addChild(compatDiv)
                    }
                    addChild(article)
                    val aside = Aside().apply {
                        flexDirection = FlexDirection.ROW
                        heightPx = 18
                        addChild(Pre("val scss = LiveScssStyleEngine()").apply { widthPx = 206 })
                        addChild(Code("mdi { ... }").apply { widthPx = 140 })
                    }
                    addChild(aside)
                }
                addChild(section)
            }
            addChild(mainBlock)

            val ul = Ul().apply {
                flexDirection = FlexDirection.ROW
                heightPx = 18
                addChild(Li("• Rainbow Item 1", id = "compat-li-1").apply { widthPx = 175 })
                addChild(Li("• Rainbow Item 2", id = "compat-li-2").apply { widthPx = 175 })
            }
            addChild(ul)

            val ol = Ol().apply {
                flexDirection = FlexDirection.ROW
                heightPx = 18
                addChild(Li("1. Step A").apply { widthPx = 175 })
                addChild(Li("2. Step B").apply { widthPx = 175 })
            }
            addChild(ol)

            addChild(Blockquote("Every component inside MdiContainer responds to live SCSS rules.", "GenericUI Style Engine"))
            addChild(MarkdownView("**MarkdownView** rendered inside `mdi-win-htmlcompat`."))

            val figure = Figure().apply {
                addChild(Span("Vector Diagram Placeholder"))
                addChild(Figcaption("Figure 1: Scoped SCSS Cascade Tree"))
            }
            addChild(figure)

            val details = Details().apply {
                addChild(Summary("Expand HTML Compat Details"))
                addChild(P("Details body content inside HTML compat showcase window."))
            }
            addChild(details)
            addChild(Hr())
            val footer = Footer().apply {
                addChild(Small("HTML Compat Footer • GenericUI"))
            }
            addChild(footer)
        }

        // Window 2: Actions, Buttons & Navigation
        actionsNavWindow = mdiContainer.createWindow(
            windowId = "mdi-win-actions-nav",
            title = "2. Actions, Buttons & Navigation",
            category = "Actions & Nav",
            bounds = Rect(416, 8, 396, 282)
        ) {
            val editMenu = Menu("Edit")
                .addItem(MenuItem("Copy Style"))
                .addItem(MenuItem("Paste Style"))
            val viewMenu = Menu("View")
                .addItem(MenuItem("Toggle Grid"))
            demoMenuBar.heightPx = 26
            demoMenuBar.addMenu(editMenu).addMenu(viewMenu)
            addChild(demoMenuBar)

            val navBrandAndPills = HStack(gap = SpacingScale.S2).apply {
                heightPx = 26
                alignItems = AlignItems.CENTER
                val navBar = Navbar("MDI NavBrand").apply {
                    flexDirection = FlexDirection.ROW
                    widthPx = 128
                    heightPx = 24
                    addChild(Badge("MDI NavBrand", BootstrapType.INFO))
                }
                val navPills = Nav(pills = true).apply {
                    flexDirection = FlexDirection.ROW
                    widthPx = 220
                    heightPx = 24
                    addChild(Badge("Overview", BootstrapType.PRIMARY, pill = true))
                    addChild(Badge("Metrics", BootstrapType.SECONDARY, pill = true))
                }
                addChild(navBar)
                addChild(navPills)
            }
            addChild(navBrandAndPills)

            val btnToolbar = Toolbar().apply {
                heightPx = 32
                val group = ButtonGroup().apply {
                    flexDirection = FlexDirection.ROW
                    widthPx = 368
                    heightPx = 26
                    val starBtn = IconButton("star", tooltipText = "Favorite").apply {
                        text = "★ Star"
                    }
                    val pinBtn = ToggleButton("Pin Window", selected = true).apply {
                        text = "Pin Window"
                    }
                    addChild(demoPrimaryButton)
                    addChild(starBtn)
                    addChild(pinBtn)
                }
                addChild(group)
            }
            addChild(btnToolbar)

            val dropdownRow = HStack(gap = SpacingScale.S2).apply {
                heightPx = 28
                val exportDropdown = DropdownButton("Export Format", BootstrapType.INFO)
                    .addItem("png", "Export PNG")
                    .addItem("svg", "Export SVG")
                    .apply {
                        widthPx = 126
                        heightPx = 26
                        addChild(Button("Export Format v", BootstrapType.INFO, BootstrapSize.SM))
                    }
                val splitDropdown = SplitDropdownButton("Quick Build", BootstrapType.SUCCESS)
                    .addItem("dev", "Development")
                    .addItem("prod", "Production")
                    .apply {
                        widthPx = 122
                        heightPx = 26
                        addChild(Button("Quick Build | v", BootstrapType.SUCCESS, BootstrapSize.SM))
                    }
                val fab = FloatingActionButton("plus", BootstrapType.WARNING).apply {
                    text = "+ Action"
                }
                addChild(exportDropdown)
                addChild(splitDropdown)
                addChild(fab)
            }
            addChild(dropdownRow)

            val breadcrumbAndPages = HStack(gap = SpacingScale.S2).apply {
                heightPx = 24
                val breadcrumb = Breadcrumb().setItems(
                    listOf(
                        BreadcrumbItem("bc-root", "Workspace"),
                        BreadcrumbItem("bc-mdi", "MDI"),
                        BreadcrumbItem("bc-nav", "Navigation", isActive = true)
                    )
                ).apply {
                    flexDirection = FlexDirection.ROW
                    widthPx = 215
                    heightPx = 22
                    addChild(Label("Workspace / MDI / Navigation"))
                }
                val pagination = Pagination(currentPage = 2, totalPages = 8).apply {
                    flexDirection = FlexDirection.ROW
                    widthPx = 145
                    heightPx = 22
                    addChild(Badge("< 1 [2] 3 .. 8 >", BootstrapType.SECONDARY))
                }
                addChild(breadcrumb)
                addChild(pagination)
            }
            addChild(breadcrumbAndPages)

            val stepper = Stepper(steps = listOf("Edit SCSS", "Compile", "Apply to MDI"), activeStepIndex = 2).apply {
                flexDirection = FlexDirection.ROW
                heightPx = 24
                addChild(Badge("1. Edit SCSS", BootstrapType.SECONDARY, pill = true))
                addChild(Badge("2. Compile", BootstrapType.INFO, pill = true))
                addChild(Badge("3. Apply to MDI", BootstrapType.SUCCESS, pill = true))
            }
            addChild(stepper)

            val bottomNav = BottomNavigationBar()
                .addItem("nav-home", "Home", "home")
                .addItem("nav-layers", "Layers", "layers")
                .addItem("nav-settings", "Settings", "settings")
                .apply {
                    isVisible = false
                }
            addChild(bottomNav)
            val bottomNavStrip = HStack(gap = SpacingScale.S2).apply {
                heightPx = 26
                addChild(Badge("Home", BootstrapType.PRIMARY).apply { widthPx = 112 })
                addChild(Badge("Layers", BootstrapType.SECONDARY).apply { widthPx = 112 })
                addChild(Badge("Settings", BootstrapType.SECONDARY).apply { widthPx = 112 })
            }
            addChild(bottomNavStrip)
        }

        // Window 3: Forms, Inputs & Validation
        formsWindow = mdiContainer.createWindow(
            windowId = "mdi-win-forms",
            title = "3. Forms, Inputs & Validation",
            category = "Forms",
            bounds = Rect(822, 8, 418, 282)
        ) {
            val form = Form().apply {
                val fieldset = Fieldset().apply {
                    val labelFieldGroup = FormGroup("Component Label:", demoTextField, "Live reactive text field").apply {
                        flexDirection = FlexDirection.ROW
                        heightPx = 28
                        label.widthPx = 118
                        helpLabel?.isVisible = false
                    }
                    addChild(labelFieldGroup)

                    val inputGroup = InputGroup().apply {
                        flexDirection = FlexDirection.ROW
                        heightPx = 28
                        addChild(Badge("@ Tag Filter", BootstrapType.INFO))
                        addChild(SearchField(query = "genericui:mdiwindow"))
                    }
                    addChild(inputGroup)

                    val passAndRadiusRow = HStack(gap = SpacingScale.S2).apply {
                        heightPx = 28
                        val passGroup = FormGroup("Password:", PasswordField("secret123")).apply {
                            flexDirection = FlexDirection.ROW
                            widthPx = 195
                            heightPx = 26
                            label.widthPx = 68
                        }
                        val radiusGroup = FormGroup("Radius:", NumberField(numericValue = 12.0, minValue = 0.0, maxValue = 48.0)).apply {
                            flexDirection = FlexDirection.ROW
                            widthPx = 195
                            heightPx = 26
                            label.widthPx = 56
                        }
                        addChild(passGroup)
                        addChild(radiusGroup)
                    }
                    addChild(passAndRadiusRow)

                    val presetGroup = FormGroup("Preset Selector:", demoSelect).apply {
                        flexDirection = FlexDirection.ROW
                        heightPx = 28
                        label.widthPx = 118
                    }
                    addChild(presetGroup)

                    val togglesRow = HStack(gap = SpacingScale.S2).apply {
                        heightPx = 24
                        demoCheckBox.widthPx = 205
                        demoSwitch.widthPx = 185
                        addChild(demoCheckBox)
                        addChild(demoSwitch)
                    }
                    addChild(togglesRow)

                    val radioGroup = RadioGroup().apply {
                        heightPx = 24
                        addRadioButton(RadioButton("cascade", "Cascade Mode", checked = true))
                        addRadioButton(RadioButton("tile", "Tile Grid Mode", checked = false))
                    }
                    addChild(radioGroup)

                    val sliderGroup = FormGroup("Opacity Range:", demoSlider).apply {
                        flexDirection = FlexDirection.ROW
                        heightPx = 26
                        label.widthPx = 110
                    }
                    addChild(sliderGroup)

                    // Additional form controls preserved in hierarchy
                    addChild(FormGroup("Theme Notes:", TextArea(value = "Scoped rainbow variables", rows = 2)))
                    addChild(
                        FormGroup(
                            "ComboBox:",
                            ComboBox(
                                options = listOf(
                                    SelectOption("scss", "SCSS Stylesheet"),
                                    SelectOption("css", "Compiled CSS")
                                ),
                                selectedValue = "scss"
                            )
                        )
                    )
                    addChild(FormGroup("Accent Color:", ColorPicker(RgbaColor.fromHex("#FF2A6D"))))
                    addChild(FormGroup("Release Date:", DatePicker("2026-10-25")))
                    addChild(FormGroup("Build Time:", TimePicker("14:30")))
                    addChild(FormGroup("Canvas Size:", CompoundUnitInput("ft.", "in.").apply { totalValue = 5.5 }))
                    addChild(FileDropZone(promptText = "Drop .scss theme files here"))
                }
                addChild(fieldset)
            }
            addChild(form)
        }

        // Window 4: Surfaces, Cards, Tables & Feedback
        surfacesFeedbackWindow = mdiContainer.createWindow(
            windowId = "mdi-win-surfaces-feedback",
            title = "4. Surfaces, Cards, Tables & Feedback",
            category = "Surfaces",
            bounds = Rect(10, 298, 396, 286)
        ) {
            demoAlert.heightPx = 28
            demoProgressBar.heightPx = 18
            demoCard.heightPx = 104
            demoCard.addChild(Label("Styled via genericui:card selector in SCSS."))
            demoCard.addChild(demoAlert)
            demoCard.addChild(demoProgressBar)
            addChild(demoCard)

            addChild(CollapsiblePanel("Collapsible Style Metrics", expanded = true).apply {
                heightPx = 48
                addChild(Label("Specificity & cascade resolved in < 1ms."))
            })

            val dataTable = Table().apply {
                heightPx = 70
                setColumns(
                    listOf(
                        TableColumn("token", "Token"),
                        TableColumn("value", "Hex Value")
                    )
                )
                setColumnWeights(1.2f, 1.0f)
                setRows(
                    listOf(
                        TableRow("row-red", listOf(TableCell("\$rainbow-red"), TableCell("#FF2A6D"))),
                        TableRow("row-cyan", listOf(TableCell("\$rainbow-cyan"), TableCell("#00D2FF")))
                    )
                )
            }
            addChild(dataTable)

            val accordion = Accordion(allowMultipleExpanded = true)
                .addItem(AccordionItem("acc-vars", "SCSS Variables").apply {
                    addChild(Label("Supports \$rainbow-red through \$rainbow-violet."))
                })
                .addItem(AccordionItem("acc-mixins", "SCSS Mixins").apply {
                    addChild(Label("Supports @mixin elevated-surface and @include."))
                })
            addChild(accordion)

            addChild(Jumbotron().apply { addChild(Label("Hero Jumbotron Surface")) })
            addChild(Well().apply { addChild(Label("Inset Well Container")) })

            val listGroup = ListGroup().apply {
                addChild(ListGroupItem(id = "lg-item-1", isActive = true).apply { +("Active Surface Item") })
                addChild(ListGroupItem(id = "lg-item-2", isActive = false).apply { +("Secondary Surface Item") })
            }
            addChild(listGroup)

            val htmlTableCompat = Container("table", "table").apply {
                val thead = Thead().apply {
                    addChild(Tr().apply {
                        addChild(Th("Tag"))
                        addChild(Th("Scope"))
                    })
                }
                val tbody = Tbody().apply {
                    addChild(Tr().apply {
                        addChild(Td("genericui:mdiwindow"))
                        addChild(Td("MDI Child"))
                    })
                }
                val tfoot = Tfoot().apply {
                    addChild(Tr().apply {
                        addChild(Td("Total"))
                        addChild(Td("6 Windows"))
                    })
                }
                addChild(thead)
                addChild(tbody)
                addChild(tfoot)
            }
            addChild(htmlTableCompat)

            addChild(Banner("Live SCSS Compilation Active", BootstrapType.WARNING))
            addChild(Toast("SCSS Engine", "Stylesheet applied to MdiContainer.", BootstrapType.SUCCESS))
            addChild(Spinner(growStyle = false, label = "Syncing styles..."))
            addChild(SkeletonPlaceholder(widthPx = 180, heightPx = 18))
            addChild(Popover("Inspector Popover").apply { addChild(Label("Popover content")) })
            addChild(Tooltip("Hover tooltip preview"))
        }

        // Window 5: Virtualized Data & Layouts
        lazyLayoutsWindow = mdiContainer.createWindow(
            windowId = "mdi-win-lazy-layouts",
            title = "5. Virtualized Data & Layouts",
            category = "Lazy & Layout",
            bounds = Rect(416, 298, 396, 286)
        ) {
            val topLayoutStrip = HStack(gap = SpacingScale.S2).apply {
                heightPx = 24
                val gridRow = ColumnContainer().apply {
                    widthPx = 240
                    heightPx = 22
                    val colLeft = Column(span = 6).apply {
                        addChild(Badge("row > col (6)", BootstrapType.PRIMARY))
                    }
                    val colRight = Column(span = 6).apply {
                        addChild(Badge("row > col (6)", BootstrapType.INFO))
                    }
                    addChild(colLeft)
                    addChild(colRight)
                }
                val flexSample = FlexBox().apply {
                    flexDirection = FlexDirection.ROW
                    widthPx = 132
                    heightPx = 22
                    addChild(Badge("FlexBox Row", BootstrapType.SUCCESS, pill = true))
                }
                addChild(gridRow)
                addChild(flexSample)
            }
            addChild(topLayoutStrip)

            demoLazyList.setItems(
                count = 24,
                onLoadItem = { idx ->
                    HStack(gap = SpacingScale.S1).apply {
                        addChild(Badge("#${idx + 1}", BootstrapType.PRIMARY, pill = true))
                        addChild(Label("LazyRow #${idx + 1}"))
                    }
                }
            )

            demoLazyGrid.setItems(
                count = 12,
                onLoadItem = { idx ->
                    Card("Cell #${idx + 1}").apply {
                        addChild(Label("Grid #${idx + 1}"))
                    }
                }
            )

            demoTreeView.setRootNodes(
                listOf(
                    TreeNode(
                        id = "tree-mdi-root",
                        label = "MdiContainer (genericui:mdicontainer)",
                        data = "root",
                        isExpanded = true,
                        children = mutableListOf(
                            TreeNode("tree-win-1", "1. HTML Compat & Typography", "win1"),
                            TreeNode("tree-win-2", "2. Actions, Buttons & Navigation", "win2"),
                            TreeNode("tree-win-6", "6. Dock Workspace Studio Fragment", "win6")
                        )
                    )
                )
            )

            val splitPreview = SplitPane(Orientation.HORIZONTAL, dividerPosition = 0.48f).apply {
                setPanes(demoLazyList, demoLazyGrid)
            }
            addChild(splitPreview)
            addChild(demoTreeView)
        }

        // Window 6: Embedded DockWorkspaceFragment inside MdiWindow
        dockWorkspaceWindow = mdiContainer.createFragmentWindow(
            windowId = "mdi-win-dock-workspace",
            title = "6. Dock Workspace Studio Fragment",
            fragment = dockSubFragment,
            category = "DockStudio",
            bounds = Rect(822, 298, 418, 286)
        )

        // Tile initial 6 MDI windows into a clean 3x2 grid and activate Window 1 initially
        mdiContainer.tileWindows(
            columns = 3,
            viewportWidth = 1250,
            viewportHeight = 590,
            gapPx = 10,
            paddingPx = 10,
            startY = 8
        )
        mdiContainer.activateWindow(htmlCompatWindow)
    }

    private fun buildScssEditorPane(): Container {
        val paneStack = VStack(gap = SpacingScale.S2)

        val toolbarRow = HStack(gap = SpacingScale.S2).apply {
            addClass("scss-editor-toolbar")
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.BETWEEN
            flexWrap = FlexWrap.WRAP
        }

        val presetsGroup = HStack(gap = SpacingScale.S1).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
            addChild(Label("Theme Presets:"))
            addChild(presetRainbowButton)
            addChild(presetCyberpunkButton)
            addChild(presetOceanButton)
            addChild(presetEmeraldButton)
            addChild(presetSunsetButton)
        }

        val actionsGroup = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            flexWrap = FlexWrap.WRAP
            addChild(liveScssSwitch)
            addChild(applyScssButton)
            addChild(formatScssButton)
            addChild(scssErrorCountBadge)
        }

        toolbarRow.addChild(presetsGroup)
        toolbarRow.addChild(actionsGroup)
        paneStack.addChild(toolbarRow)

        val editorSplit = SplitPane(
            orientation = Orientation.HORIZONTAL,
            dividerPosition = 0.64f
        ).apply {
            id = "split-scss-editor-inspector"
        }

        val editorCard = Card("Live SCSS Stylesheet Editor (Scoped to genericui:mdicontainer)").apply {
            addChild(scssEditor)
        }

        val inspectorCard = Card("Compiled CSS & SCSS Diagnostics").apply {
            addClass("scss-diagnostics-card")
            val inspectorScroll = ScrollView()
            val inspectorStack = VStack(gap = SpacingScale.S2).apply {
                addChild(Label("Scope Isolation: Bound strictly to #showcase-mdi-container (Outer IDE & MenuBar unaffected)"))
                addChild(compiledCssPreviewLabel)
            }
            inspectorScroll.addChild(inspectorStack)
            addChild(inspectorScroll)
        }

        editorSplit.setPanes(editorCard, inspectorCard)
        paneStack.addChild(editorSplit)
        return paneStack
    }

    private fun wireShowcaseEvents() {
        switchPreviewTabButton.setOnClick {
            switchToMainTab("tab-mdi-preview")
        }
        switchScssTabButton.setOnClick {
            switchToMainTab("tab-scss-editor")
        }

        mainTabContainer.onTabEntrySelected = { entry ->
            activeMainTabBadge.text = entry.tab.title
            switchPreviewTabButton.getBootstrap().outline = (entry.id != "tab-mdi-preview")
            switchScssTabButton.getBootstrap().outline = (entry.id != "tab-scss-editor")
        }

        presetRainbowButton.setOnClick { selectThemePreset(ScssThemePreset.RAINBOW_PRISM) }
        presetCyberpunkButton.setOnClick { selectThemePreset(ScssThemePreset.CYBERPUNK_NEON) }
        presetOceanButton.setOnClick { selectThemePreset(ScssThemePreset.OCEAN_SLATE) }
        presetEmeraldButton.setOnClick { selectThemePreset(ScssThemePreset.EMERALD_STUDIO) }
        presetSunsetButton.setOnClick { selectThemePreset(ScssThemePreset.SUNSET_WARM) }

        scssEditor.setOnTextChanged { updatedText ->
            if (liveScssSwitch.isChecked) {
                applyScssSource(updatedText)
            } else {
                val syntaxErrors = liveScssEngine.validateScss(updatedText)
                syncEditorDiagnostics(syntaxErrors.size)
            }
        }

        applyScssButton.setOnClick {
            applyScssSource(scssEditor.getText())
        }

        formatScssButton.setOnClick {
            val formatted = formatScssSource(scssEditor.getText())
            scssEditor.setText(formatted)
            if (liveScssSwitch.isChecked) {
                applyScssSource(formatted)
            }
        }
    }

    fun switchToMainTab(tabId: String) {
        mainTabContainer.setActiveTab(tabId)
        val tabTitle = mainTabContainer.getTab(tabId)?.title ?: tabId
        activeMainTabBadge.text = tabTitle
        switchPreviewTabButton.getBootstrap().outline = (tabId != "tab-mdi-preview")
        switchScssTabButton.getBootstrap().outline = (tabId != "tab-scss-editor")
    }

    fun selectThemePreset(preset: ScssThemePreset): ScssCompileResult {
        activePreset = preset
        presetRainbowButton.getBootstrap().outline = (preset != ScssThemePreset.RAINBOW_PRISM)
        presetCyberpunkButton.getBootstrap().outline = (preset != ScssThemePreset.CYBERPUNK_NEON)
        presetOceanButton.getBootstrap().outline = (preset != ScssThemePreset.OCEAN_SLATE)
        presetEmeraldButton.getBootstrap().outline = (preset != ScssThemePreset.EMERALD_STUDIO)
        presetSunsetButton.getBootstrap().outline = (preset != ScssThemePreset.SUNSET_WARM)

        scssEditor.setText(preset.scssSource)
        return applyScssSource(preset.scssSource)
    }

    fun applyScssSource(scssSource: String): ScssCompileResult {
        val result = liveScssEngine.updateScss(scssSource)
        val diagnostics = result.errors.map { err ->
            val lineIdx = (err.line - 1).coerceAtLeast(0)
            val colIdx = (err.column - 1).coerceAtLeast(0)
            CodeDiagnostic(
                range = TextRange(
                    CursorPosition(lineIdx, colIdx),
                    CursorPosition(lineIdx, colIdx + 1)
                ),
                message = err.message,
                severity = CodeDiagnosticSeverity.ERROR,
                code = "SCSS_SYNTAX_ERROR"
            )
        }
        scssEditor.setDiagnostics(diagnostics)
        syncEditorDiagnostics(result.errors.size)

        if (result.isSuccess) {
            val ruleCount = result.styleSheet.rules.size
            val varCount = result.variables.size
            scssStatusBadge.text = "SCSS Applied (Rev #${liveScssEngine.revisionCount})"
            scssStatusBadge.getBootstrap().type = BootstrapType.SUCCESS
            scssRulesBadge.text = "$ruleCount rules"
            scssVarsBadge.text = "$varCount vars"
            compiledCssPreviewLabel.text = result.compiledCss
        } else {
            val firstError = result.errors.firstOrNull()?.message ?: "Syntax error"
            scssStatusBadge.text = "SCSS Error: $firstError"
            scssStatusBadge.getBootstrap().type = BootstrapType.DANGER
        }
        return result
    }

    private fun syncEditorDiagnostics(errorCount: Int) {
        scssErrorCountBadge.text = "$errorCount errors"
        scssErrorCountBadge.getBootstrap().type = if (errorCount == 0) BootstrapType.SUCCESS else BootstrapType.DANGER
    }

    private fun formatScssSource(rawScss: String): String =
        rawScss.lines().joinToString("\n") { it.trimEnd() }.trim()
}

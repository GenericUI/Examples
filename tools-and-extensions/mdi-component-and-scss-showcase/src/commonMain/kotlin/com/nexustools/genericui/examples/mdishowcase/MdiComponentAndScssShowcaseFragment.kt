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
import com.nexustools.genericui.component.layout.Col
import com.nexustools.genericui.component.layout.Container
import com.nexustools.genericui.component.layout.FlexBox
import com.nexustools.genericui.component.layout.HStack
import com.nexustools.genericui.component.layout.Row
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
import com.nexustools.genericui.style.RgbaColor
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
        estimatedItemExtentPx = 44f,
        overscanItems = 2
    ).apply {
        id = "demo-lazy-list"
    }

    val demoLazyGrid: LazyGrid = LazyGrid(
        columns = 3,
        estimatedRowHeightPx = 96f,
        overscanRows = 1
    ).apply {
        id = "demo-lazy-grid"
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
        val rootStack = VStack(gap = SpacingScale.S2)
        rootStack.addChild(outerMenuBar)

        // Top Bar
        val topBar = HStack(gap = SpacingScale.S2).apply {
            id = "outer-shell-topbar"
            addClass("outer-shell-topbar")
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.BETWEEN
        }
        val titleGroup = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            addChild(Heading("MDI Component & Live SCSS Showcase", level = 4))
            addChild(activeMainTabBadge)
        }
        val actionsGroup = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
            addChild(switchPreviewTabButton)
            addChild(switchScssTabButton)
            addChild(scssStatusBadge)
            addChild(scssRulesBadge)
            addChild(scssVarsBadge)
        }
        topBar.addChild(titleGroup)
        topBar.addChild(actionsGroup)
        rootStack.addChild(topBar)

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
            bounds = Rect(16, 16, 380, 310)
        ) {
            compatHeader.addChild(compatH1)
            compatHeader.addChild(compatH2)
            addChild(compatHeader)
            addChild(compatH3)
            addChild(compatH4)
            addChild(compatH5)
            addChild(compatH6)

            val mainBlock = Main().apply {
                val section = Section().apply {
                    addChild(compatP)
                    val article = Article().apply {
                        compatDiv.addChild(compatSpan)
                        compatDiv.addChild(Br())
                        compatDiv.addChild(Strong("Strong text "))
                        compatDiv.addChild(B("Bold "))
                        compatDiv.addChild(Em("Emphasized "))
                        compatDiv.addChild(I("Italic "))
                        compatDiv.addChild(Small("Small "))
                        compatDiv.addChild(Sup("sup"))
                        compatDiv.addChild(Sub("sub"))
                        compatDiv.addChild(A("Documentation Link", href = "https://genericui.nexustools.io"))
                        addChild(compatDiv)
                    }
                    addChild(article)
                    val aside = Aside().apply {
                        addChild(Pre("val engine = LiveScssStyleEngine(scss)"))
                        addChild(Code("genericui:mdicontainer { ... }"))
                    }
                    addChild(aside)
                }
                addChild(section)
            }
            addChild(mainBlock)

            addChild(Blockquote("Every component inside MdiContainer responds to live SCSS rules.", "GenericUI Style Engine"))
            addChild(Badge("Semantic HTML5 Wrappers", BootstrapType.INFO, pill = true))
            addChild(MarkdownView("**MarkdownView** rendered inside `mdi-win-htmlcompat`."))

            val ul = Ul().apply {
                addChild(Li("Unordered Rainbow Item 1", id = "compat-li-1"))
                addChild(Li("Unordered Rainbow Item 2", id = "compat-li-2"))
            }
            val ol = Ol().apply {
                addChild(Li("Ordered Step A"))
                addChild(Li("Ordered Step B"))
            }
            addChild(ul)
            addChild(ol)

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
            bounds = Rect(412, 16, 380, 310)
        ) {
            val editMenu = Menu("Edit")
                .addItem(MenuItem("Copy Style"))
                .addItem(MenuItem("Paste Style"))
            val viewMenu = Menu("View")
                .addItem(MenuItem("Toggle Grid"))
            demoMenuBar.addMenu(editMenu).addMenu(viewMenu)
            addChild(demoMenuBar)

            addChild(Navbar("MDI NavBrand"))
            val navPills = Nav(pills = true).apply {
                addChild(Badge("Overview", BootstrapType.PRIMARY, pill = true))
                addChild(Badge("Metrics", BootstrapType.SECONDARY, pill = true))
            }
            addChild(navPills)

            val btnToolbar = Toolbar().apply {
                val group = ButtonGroup().apply {
                    addChild(demoPrimaryButton)
                    addChild(IconButton("star", tooltipText = "Favorite"))
                    addChild(ToggleButton("Pin Window", selected = true))
                }
                addChild(group)
            }
            addChild(btnToolbar)

            val dropdownRow = HStack(gap = SpacingScale.S2).apply {
                addChild(
                    DropdownButton("Export Format", BootstrapType.INFO)
                        .addItem("png", "Export PNG")
                        .addItem("svg", "Export SVG")
                )
                addChild(
                    SplitDropdownButton("Quick Build", BootstrapType.SUCCESS)
                        .addItem("dev", "Development")
                        .addItem("prod", "Production")
                )
                addChild(FloatingActionButton("plus", BootstrapType.WARNING))
            }
            addChild(dropdownRow)

            val breadcrumb = Breadcrumb().setItems(
                listOf(
                    BreadcrumbItem("bc-root", "Workspace"),
                    BreadcrumbItem("bc-mdi", "MDI Container"),
                    BreadcrumbItem("bc-nav", "Navigation", isActive = true)
                )
            )
            addChild(breadcrumb)
            addChild(Pagination(currentPage = 2, totalPages = 8))
            addChild(Stepper(steps = listOf("Edit SCSS", "Compile", "Apply to MDI"), activeStepIndex = 2))
            addChild(
                BottomNavigationBar()
                    .addItem("nav-home", "Home", "home")
                    .addItem("nav-layers", "Layers", "layers")
                    .addItem("nav-settings", "Settings", "settings")
            )
        }

        // Window 3: Forms, Inputs & Validation
        formsWindow = mdiContainer.createWindow(
            windowId = "mdi-win-forms",
            title = "3. Forms, Inputs & Validation",
            category = "Forms",
            bounds = Rect(808, 16, 380, 310)
        ) {
            val form = Form().apply {
                val fieldset = Fieldset().apply {
                    addChild(FormGroup("Component Label:", demoTextField, "Live reactive text field"))
                    val inputGroup = InputGroup().apply {
                        addChild(Label("@", "span", "input-group-text"))
                        addChild(SearchField(query = "genericui:mdiwindow"))
                    }
                    addChild(inputGroup)
                    addChild(FormGroup("Password:", PasswordField("secret123")))
                    addChild(FormGroup("Border Radius (px):", NumberField(numericValue = 12.0, minValue = 0.0, maxValue = 48.0)))
                    addChild(FormGroup("Theme Notes:", TextArea(value = "Scoped rainbow variables", rows = 2)))
                    addChild(FormGroup("Preset Selector:", demoSelect))
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
                    addChild(demoCheckBox)
                    addChild(demoSwitch)

                    val radioGroup = RadioGroup().apply {
                        addRadioButton(RadioButton("cascade", "Cascade Mode", checked = true))
                        addRadioButton(RadioButton("tile", "Tile Grid Mode", checked = false))
                    }
                    addChild(radioGroup)
                    addChild(FormGroup("Opacity Range:", demoSlider))
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
            bounds = Rect(16, 344, 380, 310)
        ) {
            demoCard.addChild(Label("Styled via genericui:card selector in SCSS."))
            demoCard.addChild(demoAlert)
            demoCard.addChild(demoProgressBar)
            addChild(demoCard)

            addChild(CollapsiblePanel("Collapsible Style Metrics", expanded = true).apply {
                addChild(Label("Specificity & cascade resolved in < 1ms."))
            })

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

            val dataTable = Table().apply {
                setColumns(
                    listOf(
                        TableColumn("token", "Token"),
                        TableColumn("value", "Hex Value")
                    )
                )
                setRows(
                    listOf(
                        TableRow("row-red", listOf(TableCell("\$rainbow-red"), TableCell("#FF2A6D"))),
                        TableRow("row-cyan", listOf(TableCell("\$rainbow-cyan"), TableCell("#00D2FF")))
                    )
                )
            }
            addChild(dataTable)

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
            bounds = Rect(412, 344, 380, 310)
        ) {
            val gridRow = Row().apply {
                val colLeft = Col(span = 6).apply {
                    addChild(Badge("genericui:row > genericui:col (6)", BootstrapType.PRIMARY))
                }
                val colRight = Col(span = 6).apply {
                    addChild(Badge("genericui:row > genericui:col (6)", BootstrapType.INFO))
                }
                addChild(colLeft)
                addChild(colRight)
            }
            addChild(gridRow)

            val flexSample = FlexBox().apply {
                addChild(Label("FlexBox Container"))
            }
            addChild(flexSample)

            demoLazyList.setItems(
                count = 24,
                onLoadItem = { idx ->
                    HStack(gap = SpacingScale.S2).apply {
                        addChild(Badge("#${idx + 1}", BootstrapType.PRIMARY, pill = true))
                        addChild(Label("Virtualized LazyList Row #${idx + 1}"))
                    }
                }
            )

            demoLazyGrid.setItems(
                count = 12,
                onLoadItem = { idx ->
                    Card("Cell #${idx + 1}").apply {
                        addChild(Label("Grid Item ${idx + 1}"))
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

            val splitPreview = SplitPane(Orientation.HORIZONTAL, dividerPosition = 0.5f).apply {
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
            bounds = Rect(808, 344, 420, 330)
        )

        // Activate Window 1 initially so activeWindow is deterministic
        mdiContainer.activateWindow(htmlCompatWindow)
    }

    private fun buildScssEditorPane(): Container {
        val paneStack = VStack(gap = SpacingScale.S2)

        val toolbarRow = HStack(gap = SpacingScale.S2).apply {
            addClass("scss-editor-toolbar")
            alignItems = AlignItems.CENTER
            justifyContent = JustifyContent.BETWEEN
        }

        val presetsGroup = HStack(gap = SpacingScale.S1).apply {
            alignItems = AlignItems.CENTER
            addChild(Label("Theme Presets:"))
            addChild(presetRainbowButton)
            addChild(presetCyberpunkButton)
            addChild(presetOceanButton)
            addChild(presetEmeraldButton)
            addChild(presetSunsetButton)
        }

        val actionsGroup = HStack(gap = SpacingScale.S2).apply {
            alignItems = AlignItems.CENTER
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
            switchPreviewTabButton.outline = (entry.id != "tab-mdi-preview")
            switchScssTabButton.outline = (entry.id != "tab-scss-editor")
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
        switchPreviewTabButton.outline = (tabId != "tab-mdi-preview")
        switchScssTabButton.outline = (tabId != "tab-scss-editor")
    }

    fun selectThemePreset(preset: ScssThemePreset): ScssCompileResult {
        activePreset = preset
        presetRainbowButton.outline = (preset != ScssThemePreset.RAINBOW_PRISM)
        presetCyberpunkButton.outline = (preset != ScssThemePreset.CYBERPUNK_NEON)
        presetOceanButton.outline = (preset != ScssThemePreset.OCEAN_SLATE)
        presetEmeraldButton.outline = (preset != ScssThemePreset.EMERALD_STUDIO)
        presetSunsetButton.outline = (preset != ScssThemePreset.SUNSET_WARM)

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
            scssStatusBadge.type = BootstrapType.SUCCESS
            scssRulesBadge.text = "$ruleCount rules"
            scssVarsBadge.text = "$varCount vars"
            compiledCssPreviewLabel.text = result.compiledCss
        } else {
            val firstError = result.errors.firstOrNull()?.message ?: "Syntax error"
            scssStatusBadge.text = "SCSS Error: $firstError"
            scssStatusBadge.type = BootstrapType.DANGER
        }
        return result
    }

    private fun syncEditorDiagnostics(errorCount: Int) {
        scssErrorCountBadge.text = "$errorCount errors"
        scssErrorCountBadge.type = if (errorCount == 0) BootstrapType.SUCCESS else BootstrapType.DANGER
    }

    private fun formatScssSource(rawScss: String): String =
        rawScss.lines().joinToString("\n") { it.trimEnd() }.trim()
}

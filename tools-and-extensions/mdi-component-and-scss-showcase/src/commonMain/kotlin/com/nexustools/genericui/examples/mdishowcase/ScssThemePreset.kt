package com.nexustools.genericui.examples.mdishowcase

/**
 * Built-in live SCSS stylesheet presets for the MDI Component & Stylesheet Showcase.
 *
 * All presets are scoped inside `genericui:mdicontainer` and applied to the MDI container root so
 * they style all 6 MDI windows and their nested components without affecting the outer [IdeCodeEditor],
 * top-level [com.nexustools.genericui.component.shell.MenuBar], or outer shell tabs.
 */
enum class ScssThemePreset(
    val id: String,
    val displayName: String,
    val description: String,
    val scssSource: String
) {
    RAINBOW_PRISM(
        id = "rainbow-prism",
        displayName = "Rainbow Prism (Default)",
        description = "Full-spectrum rainbow gradients and prismatic hues across all MDI windows without affecting the outer editor or menubar.",
        scssSource = """
            // Rainbow Prism SCSS Theme — Scoped strictly to genericui:mdicontainer
            ${'$'}rainbow-red: #ff2a6d;
            ${'$'}rainbow-orange: #ff7b00;
            ${'$'}rainbow-yellow: #ffd000;
            ${'$'}rainbow-green: #00e676;
            ${'$'}rainbow-cyan: #00d2ff;
            ${'$'}rainbow-blue: #3a86ff;
            ${'$'}rainbow-violet: #9d4edd;

            ${'$'}primary-color: #ff2a6d;
            ${'$'}surface-bg: #12172e;
            ${'$'}window-border: #00d2ff;
            ${'$'}heading-color: #ff2a6d;
            ${'$'}btn-bg: #ff2a6d;
            ${'$'}btn-hover-bg: #00d2ff;
            ${'$'}card-radius: 14px;
            ${'$'}rainbow-gradient: linear-gradient(90deg, #ff2a6d, #ff7b00, #ffd000, #00e676, #00d2ff, #9d4edd);

            @mixin elevated-surface(${'$'}bg, ${'$'}border, ${'$'}radius) {
              background-color: ${'$'}bg;
              border: 2px solid ${'$'}border;
              border-radius: ${'$'}radius;
            }

            genericui:mdicontainer {
              background-color: #0d1024;
              background-image: linear-gradient(135deg, #1b0c30, #0d1b32, #0d2b22);
              border: 2px solid ${'$'}rainbow-violet;
              border-radius: 12px;

              genericui:mdiwindow {
                @include elevated-surface(${'$'}surface-bg, ${'$'}window-border, 12px);
                box-shadow: 0 10px 28px #000000;

                .mdi-window-header {
                  background-color: ${'$'}rainbow-violet;
                  background-image: ${'$'}rainbow-gradient;
                  color: #ffffff;
                  border-radius: 10px;
                }
              }

              header {
                background-color: #1c1438;
                background-image: ${'$'}rainbow-gradient;
                border: 1px solid ${'$'}rainbow-red;
                border-radius: 10px;
                padding: 8px;
              }

              h1 {
                color: ${'$'}rainbow-red;
                font-size: 28px;
                font-weight: bold;
              }

              h2 {
                color: ${'$'}rainbow-orange;
                font-size: 24px;
              }

              h3 {
                color: ${'$'}rainbow-yellow;
                font-size: 20px;
              }

              h4 {
                color: ${'$'}rainbow-green;
                font-size: 18px;
              }

              h5 {
                color: ${'$'}rainbow-cyan;
                font-size: 16px;
              }

              h6 {
                color: ${'$'}rainbow-violet;
                font-size: 14px;
              }

              button.btn {
                background-color: ${'$'}btn-bg;
                background-image: ${'$'}rainbow-gradient;
                color: #ffffff;
                border: 1px solid ${'$'}rainbow-yellow;
                border-radius: 10px;

                &:hover {
                  background-color: ${'$'}btn-hover-bg;
                  border-color: ${'$'}rainbow-green;
                }

                &:active {
                  background-color: ${'$'}rainbow-violet;
                }
              }

              genericui:card {
                @include elevated-surface(#161d3a, ${'$'}rainbow-green, ${'$'}card-radius);
              }

              genericui:dockpanel {
                background-color: #141c36;
                border: 1px solid ${'$'}rainbow-cyan;
                border-radius: 10px;
              }

              genericui:menubar {
                background-color: #23153c;
                background-image: ${'$'}rainbow-gradient;
                border: 1px solid ${'$'}rainbow-orange;
                border-radius: 8px;
              }

              table {
                background-color: #151e38;
                border: 1px solid ${'$'}rainbow-blue;
              }

              ul {
                border-color: ${'$'}rainbow-orange;
              }

              li {
                color: ${'$'}rainbow-yellow;
              }

              .showcase-highlight {
                color: ${'$'}rainbow-cyan;
                background-color: #1e1836;
              }
            }
        """.trimIndent()
    ),

    CYBERPUNK_NEON(
        id = "cyberpunk-neon",
        displayName = "Cyberpunk Neon",
        description = "High-contrast magenta, electric cyan, and deep synthwave indigo surfaces.",
        scssSource = """
            ${'$'}primary-color: #f72585;
            ${'$'}surface-bg: #100b20;
            ${'$'}window-border: #4cc9f0;
            ${'$'}heading-color: #f72585;
            ${'$'}btn-bg: #7209b7;
            ${'$'}btn-hover-bg: #f72585;
            ${'$'}card-radius: 10px;

            @mixin elevated-surface(${'$'}bg, ${'$'}border, ${'$'}radius) {
              background-color: ${'$'}bg;
              border: 2px solid ${'$'}border;
              border-radius: ${'$'}radius;
            }

            genericui:mdicontainer {
              background-color: #090514;
              border: 2px solid ${'$'}primary-color;

              genericui:mdiwindow {
                @include elevated-surface(${'$'}surface-bg, ${'$'}window-border, 10px);

                .mdi-window-header {
                  background-color: #240046;
                  color: #4cc9f0;
                }
              }

              header {
                background-color: #190d33;
                border: 1px solid ${'$'}primary-color;
              }

              h1 {
                color: ${'$'}heading-color;
                font-size: 26px;
              }

              h2 {
                color: #4cc9f0;
              }

              h3, h4, h5, h6 {
                color: #b5179e;
              }

              button.btn {
                background-color: ${'$'}btn-bg;
                color: #ffffff;
                border-radius: 8px;

                &:hover {
                  background-color: ${'$'}btn-hover-bg;
                }
              }

              genericui:card {
                @include elevated-surface(#181030, #4cc9f0, ${'$'}card-radius);
              }

              genericui:dockpanel {
                background-color: #140c29;
                border: 1px solid #f72585;
              }

              genericui:menubar {
                background-color: #240046;
                border: 1px solid #4cc9f0;
              }

              table, ul, li {
                color: #e0aaff;
              }

              .showcase-highlight {
                color: #4cc9f0;
              }
            }
        """.trimIndent()
    ),

    OCEAN_SLATE(
        id = "ocean-slate",
        displayName = "Ocean Slate",
        description = "Calm deep-sea slate blues, aqua accents, and crisp glassmorphic borders.",
        scssSource = """
            ${'$'}primary-color: #38bdf8;
            ${'$'}surface-bg: #0f172a;
            ${'$'}window-border: #0284c7;
            ${'$'}heading-color: #38bdf8;
            ${'$'}btn-bg: #0284c7;
            ${'$'}btn-hover-bg: #0ea5e9;
            ${'$'}card-radius: 12px;

            @mixin elevated-surface(${'$'}bg, ${'$'}border, ${'$'}radius) {
              background-color: ${'$'}bg;
              border: 1px solid ${'$'}border;
              border-radius: ${'$'}radius;
            }

            genericui:mdicontainer {
              background-color: #080d1a;
              border: 1px solid ${'$'}window-border;

              genericui:mdiwindow {
                @include elevated-surface(${'$'}surface-bg, ${'$'}window-border, 12px);

                .mdi-window-header {
                  background-color: #0369a1;
                  color: #f0f9ff;
                }
              }

              header {
                background-color: #1e293b;
                border: 1px solid ${'$'}primary-color;
              }

              h1 {
                color: ${'$'}heading-color;
                font-size: 26px;
              }

              h2, h3, h4, h5, h6 {
                color: #7dd3fc;
              }

              button.btn {
                background-color: ${'$'}btn-bg;
                color: #f8fafc;
                border-radius: 8px;

                &:hover {
                  background-color: ${'$'}btn-hover-bg;
                }
              }

              genericui:card {
                @include elevated-surface(#1e293b, #38bdf8, ${'$'}card-radius);
              }

              genericui:dockpanel {
                background-color: #0f172a;
                border: 1px solid #0284c7;
              }

              genericui:menubar {
                background-color: #0c4a6e;
                border: 1px solid #38bdf8;
              }

              table, ul, li {
                color: #bae6fd;
              }

              .showcase-highlight {
                color: #38bdf8;
              }
            }
        """.trimIndent()
    ),

    EMERALD_STUDIO(
        id = "emerald-studio",
        displayName = "Emerald Studio",
        description = "Botanical emerald greens, mint highlights, and dark forest studio panels.",
        scssSource = """
            ${'$'}primary-color: #10b981;
            ${'$'}surface-bg: #06281e;
            ${'$'}window-border: #34d399;
            ${'$'}heading-color: #34d399;
            ${'$'}btn-bg: #059669;
            ${'$'}btn-hover-bg: #10b981;
            ${'$'}card-radius: 16px;

            @mixin elevated-surface(${'$'}bg, ${'$'}border, ${'$'}radius) {
              background-color: ${'$'}bg;
              border: 2px solid ${'$'}border;
              border-radius: ${'$'}radius;
            }

            genericui:mdicontainer {
              background-color: #031710;
              border: 2px solid ${'$'}primary-color;

              genericui:mdiwindow {
                @include elevated-surface(${'$'}surface-bg, ${'$'}window-border, 14px);

                .mdi-window-header {
                  background-color: #047857;
                  color: #ecfdf5;
                }
              }

              header {
                background-color: #064e3b;
                border: 1px solid ${'$'}window-border;
              }

              h1 {
                color: ${'$'}heading-color;
                font-size: 26px;
              }

              h2, h3, h4, h5, h6 {
                color: #6ee7b7;
              }

              button.btn {
                background-color: ${'$'}btn-bg;
                color: #ecfdf5;
                border-radius: 10px;

                &:hover {
                  background-color: ${'$'}btn-hover-bg;
                }
              }

              genericui:card {
                @include elevated-surface(#063829, #10b981, ${'$'}card-radius);
              }

              genericui:dockpanel {
                background-color: #06281e;
                border: 1px solid #34d399;
              }

              genericui:menubar {
                background-color: #065f46;
                border: 1px solid #34d399;
              }

              table, ul, li {
                color: #a7f3d0;
              }

              .showcase-highlight {
                color: #34d399;
              }
            }
        """.trimIndent()
    ),

    SUNSET_WARM(
        id = "sunset-warm",
        displayName = "Sunset Warm",
        description = "Solar amber, coral orange, and warm crimson studio accents.",
        scssSource = """
            ${'$'}primary-color: #f97316;
            ${'$'}surface-bg: #27120b;
            ${'$'}window-border: #fb923c;
            ${'$'}heading-color: #fb923c;
            ${'$'}btn-bg: #ea580c;
            ${'$'}btn-hover-bg: #f97316;
            ${'$'}card-radius: 18px;

            @mixin elevated-surface(${'$'}bg, ${'$'}border, ${'$'}radius) {
              background-color: ${'$'}bg;
              border: 2px solid ${'$'}border;
              border-radius: ${'$'}radius;
            }

            genericui:mdicontainer {
              background-color: #180905;
              border: 2px solid ${'$'}primary-color;

              genericui:mdiwindow {
                @include elevated-surface(${'$'}surface-bg, ${'$'}window-border, 16px);

                .mdi-window-header {
                  background-color: #9a3412;
                  color: #fff7ed;
                }
              }

              header {
                background-color: #431407;
                border: 1px solid ${'$'}window-border;
              }

              h1 {
                color: ${'$'}heading-color;
                font-size: 26px;
              }

              h2, h3, h4, h5, h6 {
                color: #fdba74;
              }

              button.btn {
                background-color: ${'$'}btn-bg;
                color: #fff7ed;
                border-radius: 12px;

                &:hover {
                  background-color: ${'$'}btn-hover-bg;
                }
              }

              genericui:card {
                @include elevated-surface(#36180d, #fb923c, ${'$'}card-radius);
              }

              genericui:dockpanel {
                background-color: #27120b;
                border: 1px solid #fb923c;
              }

              genericui:menubar {
                background-color: #7c2d12;
                border: 1px solid #fdba74;
              }

              table, ul, li {
                color: #fed7aa;
              }

              .showcase-highlight {
                color: #fb923c;
              }
            }
        """.trimIndent()
    );

    companion object {
        fun fromId(id: String): ScssThemePreset =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
                ?: RAINBOW_PRISM
    }
}

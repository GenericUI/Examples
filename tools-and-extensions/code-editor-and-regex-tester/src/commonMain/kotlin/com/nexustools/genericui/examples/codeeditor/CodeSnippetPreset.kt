package com.nexustools.genericui.examples.codeeditor

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceSerializable
import com.nexustools.genericui.component.system.PreferenceStore

/**
 * Reusable syntax-highlighted code snippet and default regular expression preset
 * persisted via [PreferenceStore].
 */
data class CodeSnippetPreset(
    val id: String,
    val title: String,
    val language: String,
    val code: String,
    val defaultRegex: String
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "id" to id,
        "title" to title,
        "language" to language,
        "code" to code,
        "defaultRegex" to defaultRegex
    )

    companion object {
        init {
            registerWithPreferenceStore()
        }

        fun registerWithPreferenceStore() {
            PreferenceStore.registerObjectFactory<CodeSnippetPreset> { fields: Map<String, PreferenceElement> ->
                CodeSnippetPreset(
                    id = fields["id"]?.asString("custom") ?: "custom",
                    title = fields["title"]?.asString("Untitled Snippet") ?: "Untitled Snippet",
                    language = fields["language"]?.asString("KOTLIN") ?: "KOTLIN",
                    code = fields["code"]?.asString("") ?: "",
                    defaultRegex = fields["defaultRegex"]?.asString("") ?: ""
                )
            }
        }

        val BUILT_IN_PRESETS: List<CodeSnippetPreset> = listOf(
            CodeSnippetPreset(
                id = "kotlin-functions",
                title = "Kotlin Functions & Annotations",
                language = "KOTLIN",
                code = """
                    package com.example.service

                    @Serializable
                    data class InvoiceItem(val sku: String, val priceCents: Int)

                    fun calculateSubtotal(items: List<InvoiceItem>, discountRate: Double): Int {
                        return items.sumOf { it.priceCents }
                    }

                    fun formatCurrency(amountCents: Int, currencyCode: String): String {
                        val whole = amountCents / 100
                        val cents = amountCents % 100
                        return "${'$'}currencyCode ${'$'}whole.${'$'}cents"
                    }
                """.trimIndent(),
                defaultRegex = """fun\s+(\w+)\(([^)]*)\):\s*(\w+)"""
            ),
            CodeSnippetPreset(
                id = "typescript-endpoints",
                title = "TypeScript REST Endpoints",
                language = "TYPESCRIPT",
                code = """
                    export interface ApiRoute {
                        method: "GET" | "POST" | "DELETE";
                        path: string;
                    }

                    export const routes: ApiRoute[] = [
                        { method: "GET", path: "/api/v1/users" },
                        { method: "POST", path: "/api/v1/orders" },
                        { method: "DELETE", path: "/api/v1/sessions" }
                    ];
                """.trimIndent(),
                defaultRegex = """method:\s*"(GET|POST|DELETE)",\s*path:\s*"([^"]+)""""
            ),
            CodeSnippetPreset(
                id = "json-dependencies",
                title = "JSON Package Versions",
                language = "JSON",
                code = """
                    {
                      "name": "genericui-Regex-suite",
                      "version": "2.4.0",
                      "dependencies": {
                        "genericui-core": "1.2.0",
                        "genericui-codeeditor": "1.2.0",
                        "kotlinx-coroutines": "1.10.1"
                      }
                    }
                """.trimIndent(),
                defaultRegex = """"([a-z0-9-]+)"\s*:\s*"(\d+\.\d+\.\d+)""""
            ),
            CodeSnippetPreset(
                id = "css-theme-tokens",
                title = "CSS Design Tokens & Hex Colors",
                language = "CSS",
                code = """
                    :root {
                        --color-bg-primary: #0F172A;
                        --color-bg-surface: #1E293B;
                        --color-accent-indigo: #6366F1;
                        --color-accent-emerald: #10B981;
                    }
                """.trimIndent(),
                defaultRegex = """--([a-z-]+):\s*(#[0-9A-Fa-f]{6});"""
            )
        )
    }
}

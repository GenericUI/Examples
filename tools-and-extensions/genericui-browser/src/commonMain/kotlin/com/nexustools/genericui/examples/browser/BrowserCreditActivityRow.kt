package com.nexustools.genericui.examples.browser

import com.nexustools.genericui.component.system.PreferenceElement
import com.nexustools.genericui.component.system.PreferenceObjectCodec
import com.nexustools.genericui.component.system.PreferenceSerializable
import kotlin.math.abs

/**
 * Single row in the Google One `AI credits activity` table (`https://one.google.com/ai/activity`).
 */
data class BrowserCreditActivityRow(
    val date: String,
    val category: String,
    val amount: Int
) : PreferenceSerializable {

    override fun toPreferenceMap(): Map<String, Any?> = linkedMapOf(
        "date" to date,
        "category" to category,
        "amount" to amount
    )

    /**
     * Formats [amount] with a thousands separator matching the Google One table (e.g. `"-1,026"` or `"+1,000"`).
     */
    fun formattedAmount(): String {
        val absFormatted = formatNumberWithCommas(abs(amount))
        return when {
            amount < 0 -> "-$absFormatted"
            amount > 0 -> "+$absFormatted"
            else -> "0"
        }
    }

    companion object {
        init {
            registerFactory()
        }

        fun registerFactory() {
            PreferenceObjectCodec.registerObjectFactory(BrowserCreditActivityRow::class) { fields ->
                BrowserCreditActivityRow(
                    date = fields["date"]?.asString("") ?: "",
                    category = fields["category"]?.asString("Google Antigravity hourly activity")
                        ?: "Google Antigravity hourly activity",
                    amount = fields["amount"]?.asInt(0) ?: 0
                )
            }
        }

        /**
         * Formats an integer with comma thousands separators (e.g. `6488` -> `"6,488"`).
         */
        fun formatNumberWithCommas(value: Int): String {
            val negative = value < 0
            val digits = abs(value.toLong()).toString()
            val sb = StringBuilder()
            for (i in digits.indices) {
                if (i > 0 && (digits.length - i) % 3 == 0) {
                    sb.append(',')
                }
                sb.append(digits[i])
            }
            return if (negative) "-$sb" else sb.toString()
        }

        /**
         * Returns the 4 initial Google One `AI credits activity` rows from the Chrome Desktop reference screenshot.
         */
        fun defaultRows(): List<BrowserCreditActivityRow> = listOf(
            BrowserCreditActivityRow(
                date = "Oct 27, 2026, 6:00:00 AM",
                category = "Google Antigravity hourly activity",
                amount = -1026
            ),
            BrowserCreditActivityRow(
                date = "Oct 27, 2026, 5:00:00 AM",
                category = "Google Antigravity hourly activity",
                amount = -2322
            ),
            BrowserCreditActivityRow(
                date = "Oct 27, 2026, 4:00:00 AM",
                category = "Google Antigravity hourly activity",
                amount = -1207
            ),
            BrowserCreditActivityRow(
                date = "Oct 27, 2026, 3:00:00 AM",
                category = "Google Antigravity hourly activity",
                amount = -675
            )
        )
    }
}

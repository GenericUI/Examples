package com.nexustools.genericui.examples.codeeditor

/**
 * Represents a single regular expression match discovered in the active code editor buffer.
 *
 * @property matchIndex Zero-based index of the match within the current evaluation pass.
 * @property fullMatch The entire substring matched by the regular expression (`groupValues[0]`).
 * @property rangeText Human-readable character offset and line:column range (e.g. `"L2:C5..L2:C24 (18..37)"`).
 * @property groups Ordered list of captured subgroup strings (`groupValues.drop(1)`).
 */
data class RegexMatchRecord(
    val matchIndex: Int,
    val fullMatch: String,
    val rangeText: String,
    val groups: List<String>
)

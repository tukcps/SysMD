package com.github.tukcps.sysmd.model.util


/**
 * Helper class that represents a constraint for a type.
 *
 * Supported syntax:
 *   true | false
 *   value := "*" | number
 *   quantity := ["("] value [".." value] ("," value [".." value])* [")"] ["[" unit "]"]
 *
 * Examples:
 *   true
 *   false
 *   5
 *   1..2
 *   *..2
 *   1..*
 *   *..*
 *   (1, 2..3, *..5, 6..*, *..*) [kg]
 */
data class TypeConstraint(
    var value: MutableList<String>,
    var unit: String
) {

    companion object {
        /** Integer or floating point number with optional exponent. */
        private const val NUMBER =
            """[-+]?(?:\d+\.\d*|\.\d+|\d+)(?:[eE][-+]?\d+)?"""

        /** Either a number or '*' for an open range boundary. */
        private const val NUMBER_OR_STAR =
            """(?:$NUMBER|\*)"""

        /** Validates one value or one range. */
        private val VALUE_REGEX =
            Regex("""^$NUMBER_OR_STAR(?:\s*\.\.\s*$NUMBER_OR_STAR)?$""")

        private val UNIT_REGEX =
            Regex("""\[\s*(.*?)\s*]""")
    }

    constructor(quantity: String) : this(mutableListOf(), "") {

        val normalized = quantity.trim().trim('"')

        when (normalized) {
            "true" -> {
                value.add("true")
                return
            }

            "false" -> {
                value.add("false")
                return
            }
        }

        unit = UNIT_REGEX
            .find(quantity)
            ?.groupValues
            ?.getOrNull(1)
            ?: ""

        val valuesPart = quantity
            .substringBefore('[')
            .trim()
            .removePrefix("(")
            .removeSuffix(")")

        value = valuesPart
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .onEach {
                require(VALUE_REGEX.matches(it)) {
                    "Illegal range '$it' in '$quantity'"
                }
            }
            .toMutableList()
    }

    /**
     * Converts the internal representation back to a string.
     */
    override fun toString(): String = when {
        value.isEmpty() ->
            ""

        value.size == 1 ->
            value.first() + if (unit.isNotEmpty()) " [$unit]" else ""

        else ->
            "(${value.joinToString(", ")})" +
                    if (unit.isNotEmpty()) " [$unit]" else ""
    }
}
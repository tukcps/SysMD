package com.github.tukcps.sysmd.model.util

/**
 * Represents a SysML v2 multiplicity range with a lower and an upper bound.
 *
 * @property min The lower bound of the multiplicity. Must be greater than or equal to 0.
 * @property max The upper bound of the multiplicity. A value of `null` represents infinity (`*`).
 */
data class MultiplicityRange(
    var min: Long,
    var max: Long? // null represents "*" (infinity)
) {
    constructor(text: String) : this(
        min = parse(text).first,
        max = parse(text).second
    )

    init {
        require(min >= 0) { "Lower bound of multiplicity cannot be negative: $min" }
        max?.let { require(it >= min) { "Upper bound ($max) must be greater than or equal to lower bound ($min)." } }
    }

    /**
     * Checks if a given value falls within this multiplicity range.
     *
     * @param value The numerical count to validate against the multiplicity limits.
     * @return `true` if the value is within the bounds (inclusive), `false` otherwise.
     */
    operator fun contains(value: Long): Boolean {
        if (value < min) return false
        if (max != null && value > (max ?: Long.MAX_VALUE)) return false
        return true
    }

    /**
     * Checks if a given value falls within this multiplicity range.
     *
     * @param value The numerical count to validate against the multiplicity limits.
     * @return `true` if the value is within the bounds (inclusive), `false` otherwise.
     */
    operator fun contains(other: MultiplicityRange): Boolean {
        if (other.min < min) return false
        if ((other.max ?: Long.MAX_VALUE) > (max ?: Long.MAX_VALUE)) return false
        return true
    }

    fun toLongRange() = LongRange(min, max?:Long.MAX_VALUE)

    /**
     * Returns the multiplicity formatted as a standard SysML v2 textual string.
     *
     * Single-value multiplicities (where lower equals upper) are formatted as `[X]`.
     * Ranges are formatted as `[lower .. upper]`.
     *
     * @return The standard SysML v2 string representation (e.g., ``, `0 .. *`).
     */
    override fun toString(): String {
        val upperStr = max?.toString() ?: "*"
        return if (min == max) "$min" else "$min .. $upperStr"
    }

    companion object {
        /**
         * Default multiplicity for specific usages (e.g., `part`, `attribute`, `item`).
         * Formats to `` (exactly one).
         */
        val USAGE_DEFAULT = MultiplicityRange(1L, 1L)

        /**
         * Default multiplicity for general features, types, and classifiers.
         * Formats to `[0..*]` (zero or more).
         */
        val TYPE_DEFAULT = MultiplicityRange(0L, null)

        private fun parse(text: String): Pair<Long, Long?> {
            val normalized = text
                .trim()
                .removePrefix("[")
                .removeSuffix("]")
                .trim()

            // Einzelwert, z.B. "1"
            normalized.toLongOrNull()?.let {
                return it to it
            }

            // "1 .. 2", "1..2", "1 ... 2", "1...2", "1 .. *"
            val regex = Regex("""^\s*(\d+)\s*\.\.\.?\s*(\d+|\*)\s*$""")
            val match = regex.matchEntire(normalized)
                ?: throw IllegalArgumentException("Invalid multiplicity: '$text'")

            val min = match.groupValues[1].toLong()
            val max = match.groupValues[2].let {
                if (it == "*") null else it.toLong()
            }

            return min to max
        }

    }
}


/**
 * Represents an integer range with a lower and an upper bound.
 * @property min The lower bound. A value of `null` represents infinity (`*`).
 * @property max The upper bound. A value of `null` represents infinity (`*`).
 */
data class IntegerRange(
    var min: Long?,
    var max: Long? // null represents "*" (infinity)
) {
    constructor(text: String) : this(
        min = parse(text).first,
        max = parse(text).second
    )

    init {
        max?.let { require(it >= (min ?: Long.MIN_VALUE)) {
            "Upper bound ($max) must be greater than or equal to lower bound ($min)." }
        }
    }

    /**
     * Checks if a given value falls within this multiplicity range.
     *
     * @param value The numerical count to validate against the multiplicity limits.
     * @return `true` if the value is within the bounds (inclusive), `false` otherwise.
     */
    operator fun contains(value: Long): Boolean {
        if (value < (min ?: Long.MIN_VALUE)) return false
        if (value > (max ?: Long.MAX_VALUE)) return false
        return true
    }

    /**
     * Checks if a given value falls within this range.
     * @param other Other range ...
     * @return `true` if the value is within the bounds (inclusive), `false` otherwise.
     */
    operator fun contains(other: IntegerRange): Boolean {
        if ((other.min ?: Long.MIN_VALUE) < (min ?: Long.MIN_VALUE)) return false
        if ((other.max ?: Long.MAX_VALUE) > (max ?: Long.MAX_VALUE)) return false
        return true
    }

    fun toLongRange() = LongRange(min?:Long.MIN_VALUE, max?:Long.MAX_VALUE)

    /**
     * Returns the multiplicity formatted as a standard SysML v2 textual string.
     *
     * Single-value multiplicities (where lower equals upper) are formatted as `[X]`.
     * Ranges are formatted as `[lower .. upper]`.
     *
     * @return The standard SysML v2 string representation (e.g., ``, `0 .. *`).
     */
    override fun toString(): String {
        val upperStr = max?.toString() ?: "*"
        return if (min == max) "$min" else "$min .. $upperStr"
    }

    companion object {
        /**
         * Default multiplicity for specific usages (e.g., `part`, `attribute`, `item`).
         * Formats to `` (exactly one).
         */
        val USAGE_DEFAULT = MultiplicityRange(1L, 1L)

        /**
         * Default multiplicity for general features, types, and classifiers.
         * Formats to `[0..*]` (zero or more).
         */
        val TYPE_DEFAULT = MultiplicityRange(0L, null)

        private fun parse(text: String): Pair<Long, Long?> {
            val normalized = text
                .trim()
                .removePrefix("[")
                .removeSuffix("]")
                .trim()

            // Einzelwert, z.B. "1"
            normalized.toLongOrNull()?.let {
                return it to it
            }

            // "1 .. 2", "1..2", "1 ... 2", "1...2", "1 .. *"
            val regex = Regex("""^\s*(\d+)\s*\.\.\.?\s*(\d+|\*)\s*$""")
            val match = regex.matchEntire(normalized)
                ?: throw IllegalArgumentException("Invalid multiplicity: '$text'")

            val min = match.groupValues[1].toLong()
            val max = match.groupValues[2].let {
                if (it == "*") null else it.toLong()
            }

            return min to max
        }

    }
}


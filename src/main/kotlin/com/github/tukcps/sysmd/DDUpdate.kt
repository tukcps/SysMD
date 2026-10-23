package com.github.tukcps.sysmd

import io.github.tukcps.aadd.values.integer.IntegerRange
import io.github.tukcps.aadd.values.real.ia.RealRange

/**
 * Parses a RealRange from a SysML/KerML range string specification.
 * Handles SysML/KerML infinity syntax where `*..*` or `*` means unbounded (all reals, `RealRange.Reals`),
 * and `*..x` means unbounded below (`-*..x`).
 */
@Deprecated("Both *..* syntax and expression re-parsing should be avoided")
fun parseRealRange(spec: String): RealRange {
    val s = spec.trim('"', '(', ')', '[', ']', ' ')
    val trimmed = s.replace(" ", "")
    return when {
        // FIXME: When adopting -*..*, ensure "-*..*" and "-*" are handled explicitly alongside "*..*"
        trimmed.isBlank() || trimmed == "*" || trimmed == "*..*" || trimmed == "-*..*" -> RealRange.Reals
        trimmed.startsWith("*..") -> RealRange.parse("-$trimmed")
        else -> RealRange.parse(trimmed)
    }
}

/**
 * Parses an IntegerRange from a SysML/KerML range string specification.
 * Handles SysML/KerML infinity syntax where `*..*` or `*` means unbounded (`IntegerRange.All`),
 * and `*..x` means unbounded below (`-*..x`).
 */
@Deprecated("Both *..* syntax and expression re-parsing should be avoided")
fun parseIntegerRange(spec: String): IntegerRange {
    val s = spec.trim('"', '(', ')', '[', ']', ' ')
    val trimmed = s.replace(" ", "")
    return when {
        // FIXME: When adopting -*..*, ensure "-*..*" and "-*" are handled explicitly alongside "*..*"
        trimmed.isBlank() || trimmed == "*" || trimmed == "*..*" || trimmed == "-*..*" -> IntegerRange.All
        trimmed.startsWith("*..") -> IntegerRange.parse("-$trimmed")
        else -> IntegerRange.parse(trimmed)
    }
}

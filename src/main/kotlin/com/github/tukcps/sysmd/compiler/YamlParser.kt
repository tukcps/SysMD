package com.github.tukcps.sysmd.compiler

import java.io.File

/**
 * Gets the YAML information from the header of a SysMD file
 * Format:
 * ---
 * key: value
 * key: value ...
 * ---
 */
fun getYaml(lines: Sequence<String>): HashMap<String, String>? {
    val map = hashMapOf<String, String>()
    var inYaml = false
    var noOfLimiters = 0
    lines.forEach {
        if (it == "---") {
            inYaml = !inYaml
            noOfLimiters++
            if (noOfLimiters == 2)
                return map
        }
        if (inYaml) {
            val list = it.split(":")
            val pair = if (list.size == 2 && list[1].isNotBlank()) Pair(list[0].trim(), list[1].trimIndent()) else null
            if (pair != null) map[pair.first] = pair.second
        }
    }
    return null
}

fun getYaml(f: File): HashMap<String, String>? {
    val lines = f.bufferedReader().lineSequence()
    return getYaml(lines)
}

fun getYaml(string: String): HashMap<String, String>? = getYaml(string.lineSequence())
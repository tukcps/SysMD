package com.github.tukcps.sysmd.services

fun <T> replaceByReference(list: MutableList<T>, target: T, replacement: T) {
    for (i in list.indices) {
        if (list[i] === target) {
            list[i] = replacement
        }
    }
}
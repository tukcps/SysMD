package com.github.tukcps.sysmd.model.util

typealias SimpleName = String
typealias QualifiedName=String

fun QualifiedName(str: String): QualifiedName {
    return str
}

/**
 * Removes the first segment name of a qualified name.
 */
fun QualifiedName.dropFirstName() : QualifiedName {
    val asArray = this.split("::")
    val reducedArray =  asArray.subList(1, asArray.lastIndex+1)
    val result = reducedArray.joinToString(separator = "::")
    return QualifiedName(result)
}

/**
 * Removes last segment name of a qualified name.
 */
fun QualifiedName.qualification() : QualifiedName? {
    val asArray = this.split("::")
    val reducedArray = asArray.subList(0, asArray.lastIndex)
    val result = reducedArray.joinToString(separator = "::")
    return QualifiedName(result)
}

/**
 * Returns the name (unqualified name) of a Qualified Name.
 */
fun QualifiedName.unqualifiedName() : SimpleName {
    val asArray = this.split("::")
    return asArray.last()
}

/** returns the starting name of a qualified name, e.g. 'a' for 'a::b' */
fun QualifiedName.firstName() : QualifiedName =
    QualifiedName(split("::")[0])

fun QualifiedName.size(): Int =
    this.split("::").size


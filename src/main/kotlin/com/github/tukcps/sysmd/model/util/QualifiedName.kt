package com.github.tukcps.sysmd.model.util

typealias SimpleName = String
typealias QualifiedName=String

fun QualifiedName(str: String): QualifiedName {
    return str
}

fun QualifiedName.dropFirstName() : QualifiedName {
    val asArray = this.split("::")
    val reducedArray =  asArray.subList(1, asArray.lastIndex+1)
    var result = ""
    for(name in reducedArray) {
        result += "::$name"
    }
    result = result.removePrefix("::")
    return QualifiedName(result)
}


/**
 * Returns true if the name has '::' in it; then it is a qualified name.
 * @return true if it is a qualified name
 */
fun QualifiedName.isSimpleName() : Boolean =
    this.split("::").size == 1

/** returns the starting name of a qualified name, e.g. 'a' for 'a::b' */
fun QualifiedName.firstName() : QualifiedName =
    QualifiedName(split("::")[0])

fun QualifiedName.size(): Int =
    this.split("::").size

fun QualifiedName.hasNoName(): Boolean =
    this.split("::").isEmpty()

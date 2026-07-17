package com.github.tukcps.sysmd.model.datamodel

// from build generated
import com.github.tukcps.sysmd.model.generated.ElementHierarchy.directSuperTypes
import com.github.tukcps.sysmd.model.generated.ElementType

/**
 * Returns whether the given type is equal to or specializes another type.
 *
 * @param type Candidate subtype.
 * @param superType Candidate supertype.
 * @return Whether {@code type} specializes {@code superType}.
 */
fun isSubclassOf(
    type: ElementType,
    superType: ElementType,
): Boolean {

    if (type == superType)
        return true

    return directSuperTypes[type]
        ?.any { isSubclassOf(it, superType) }
        ?: false
}

/**
 * Returns whether the given metamodel type specializes another type.
 *
 * @param typeName Candidate subtype.
 * @param superTypeName Candidate supertype.
 * @return Whether the first type specializes the second.
 */
fun isSubclassOf(
    typeName: String,
    superTypeName: String,
): Boolean {

    val type = ElementType.fromString(typeName) ?: return false
    val superType = ElementType.fromString(superTypeName) ?: return false

    return isSubclassOf(type, superType)
}
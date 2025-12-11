package com.github.tukcps.sysmd.services.resolve

import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.findRecursive
import com.github.tukcps.sysmd.model.util.QualifiedName


/**
 * Resolves a qualified name in a namespace.
 * Furthermore, the method has a template parameter to which it, if possible, casts the result.
 * @param T Type for which will be searched.
 * @param qualifiedName QualifiedName that will be searched.
 * @param searchInSuperClass Whether to search in superclasses as well to consider inheritance
 * @param resolveReferences Whether to follow reference if the element found is a reference
 */
@Deprecated("Use direct function of membership")
inline fun <reified T: Element> Namespace.resolveOld(
    qualifiedName: QualifiedName,
    searchInSuperClass: Boolean = true,
    resolveReferences: Boolean = true
): T? {

    // For redefinitions ?
    // if (this is Feature && this.redefining != null)
    //     return redefining!!.findRecursive(qualifiedName, emptySet(), emptySet(), true, searchInSuperClass, resolveReferences ) as T?

    if (qualifiedName == "Global" && this == model!!.global) return model!!.global as T

    var found = findRecursive(qualifiedName, emptySet(), emptySet(),true, searchInSuperClass)

    if (found?.memberElement is T?)
        return found?.memberElement as T?

    model?.status?.error("'$qualifiedName' could be resolved, but is of wrong type", element = found,
        cause = ElementNotFoundException(this, "'$qualifiedName' could be resolved, but is of wrong type"),
        kind = Issue.Kind.ERROR_UNRESOLVED_NAME
    )
    return null
}


/**
 * Resolution of feature chains is easier than name resolution; it only searches in owned features
 * @param relativeName a feature chain
 */
fun Namespace.resolveFeatureChain(relativeName: String): Feature? {
    var segments = relativeName.split(".")
    val start = segments.first()
    var feature = resolve(start)?.memberElement as Feature?
    segments = segments.drop(1)
    for (segment in segments) {
        feature = feature?.resolveLocal(segment)?.memberElement as Feature?
    }
    return feature
}

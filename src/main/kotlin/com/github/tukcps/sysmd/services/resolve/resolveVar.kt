package com.github.tukcps.sysmd.services.resolve

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.util.QualifiedName


/**
 * A method to resolve a feature and return its variable in the solver.
 */
fun Namespace.resolveVar(qualifiedName: QualifiedName, searchInSuperClass: Boolean = true)
    = resolveVars(qualifiedName, searchInSuperClass).firstOrNull()


fun Namespace.resolveVars(qualifiedName: QualifiedName, searchInSuperClass: Boolean = true): List<Variable?> {
    var found = findRecursive(qualifiedName, emptySet(), emptySet(),true, searchInSuperClass)
    if (found is Feature && found.referencedFeature?.ref != null) {
        found = found.referencedFeature?.ref
    }
    if (found is Variable?)
        return mutableListOf(found)
    if (found is Feature && found.variable is Variable)
        return found.variables

    model?.status?.error("'$qualifiedName' could be resolved, but is of wrong type", element = found,
        cause = ElementNotFoundException(this, "'$qualifiedName' could be resolved, but is of wrong type"))
    return emptyList()
}


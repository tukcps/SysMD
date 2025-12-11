package com.github.tukcps.sysmd.services.resolve

import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.implementation.findRecursive
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.qualification


/**
 * A method to resolve a feature and return its variable in the solver.
 */
fun Namespace.resolveVar(qualifiedName: QualifiedName, searchInSuperClass: Boolean = true)
    = resolveVars(qualifiedName, searchInSuperClass).firstOrNull()
//     = model!!.solver.resolveVar(this.qualifiedName, qualifiedName)

fun Namespace.resolveVars(qualifiedName: QualifiedName, searchInSuperClass: Boolean = true): List<Variable?> {

    var found = findRecursive(qualifiedName, emptySet(), emptySet(),true, searchInSuperClass)

    while ( (found?.memberElement is Feature) && (found.memberElement as Feature).referencedFeature != null) {
        found = (found.memberElement as Feature).referencedFeature?.owningRelationship
    }

    if (found?.memberElement is Feature)
        return model!!.solver.getVariables(found.memberElement.path())?.toList()?:emptyList()

    if (found == null)
        return emptyList()

    model?.status?.error("'$qualifiedName' could be resolved, but is of wrong type",
        element = found,
        cause = ElementNotFoundException(this, "'$qualifiedName' could be resolved, but is of wrong type"))
    return emptyList()
}

/**
 * Searches in the list variables for a match.
 * @param namespace Namespace in which the variable will be searched
 * @param qualifiedName The qualified name that must match in the given namespace.
 * @return The variable for the respective feature
 */
tailrec fun Solver.resolveVar(namespace: QualifiedName?, qualifiedName: QualifiedName): Variable? {
    val key = if (namespace.isNullOrEmpty()) qualifiedName else "$namespace::$qualifiedName"
    return getVariable(key)
        ?: if (namespace.isNullOrEmpty()) null
           else resolveVar(namespace.qualification(), qualifiedName)
}

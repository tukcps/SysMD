package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Predicate
import com.github.tukcps.sysmd.model.util.SimpleName

class PredicateImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Predicate",
): Predicate, FunctionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    override fun clone(): PredicateImplementation =
        PredicateImplementation(declaredName, declaredShortName).also { klon-> updateFrom(this) }

}
package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassifierImplementation
import com.github.tukcps.sysmd.model.sysml.Definition
import com.github.tukcps.sysmd.model.util.SimpleName

open class DefinitionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    elementType: String = "Definition",
): Definition, ClassifierImplementation(declaredName, declaredShortName, elementType) {
    override var isVariation = false
}
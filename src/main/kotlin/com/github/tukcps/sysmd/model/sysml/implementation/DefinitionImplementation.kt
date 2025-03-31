package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.kerml.implementation.ClassifierImplementation
import com.github.tukcps.sysmd.model.sysml.Definition
import com.github.tukcps.sysmd.model.util.SimpleName

open class DefinitionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
): Definition, ClassifierImplementation(declaredName, declaredShortName) {
    override var isVariation = false
}
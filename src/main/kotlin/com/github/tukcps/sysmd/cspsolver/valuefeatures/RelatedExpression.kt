package com.github.tukcps.sysmd.cspsolver.valuefeatures

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.Variable.BaseType
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation

class RelatedExpression(
    dependency: String,
    valueSpecs: MutableList<Any?>,
    val createdBy: Variable,
    val relatedIndex: Int,
    val reason: String = ""
): VariableImplementation(FeatureImplementation(), BaseType.Bool){
    init {
        this.feature.expression = dependency
        this.valueSpecs = valueSpecs
    }
}
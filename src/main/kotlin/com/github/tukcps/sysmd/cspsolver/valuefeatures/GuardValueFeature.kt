package com.github.tukcps.sysmd.cspsolver.valuefeatures

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation

class GuardValueFeature(
    name: String? = null,
    dependency: String,
    valueSpecs: MutableList<Any?>,
    val createdBy: Variable,
    paths: HashMap<Int, Boolean>,
    val reason: String = ""
): VariableImplementation(FeatureImplementation(), Variable.BaseType.Bool) {
    init {
        this.feature.expression = dependency
        this.valueSpecs = valueSpecs
    }
    val pathsToInfeasibility = paths
}


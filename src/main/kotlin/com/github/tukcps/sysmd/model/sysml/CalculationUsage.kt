package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function

interface CalculationUsage: ActionUsage {

    val calculationDefinition: Function?

    // override
    fun modelLevelEvaluable(visited: Set<Feature>): Boolean

}
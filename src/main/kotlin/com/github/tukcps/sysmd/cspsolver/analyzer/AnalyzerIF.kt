package com.github.tukcps.sysmd.cspsolver.analyzer

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session

interface AnalyzerIF {
    val model: Session

    fun updateProperty(updatedProperty: Variable)

    //fun conditionInAPath(conditionIndex: Int, property: Property): Boolean

    //fun conditionInAllPaths(conditionIndex: Int, property: Property): Boolean

    //fun getAllocationFromAllPaths(index: Int, property: Property): DD
}
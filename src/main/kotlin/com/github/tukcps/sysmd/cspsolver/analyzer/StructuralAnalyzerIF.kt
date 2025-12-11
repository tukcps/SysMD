package com.github.tukcps.sysmd.cspsolver.analyzer

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.DD

interface StructuralAnalyzerIF: AnalyzerIF {
    override val model: Session

    override fun updateProperty(updatedProperty: Variable) {
        TODO("Not yet implemented")
    }

    fun conditionInAPath(conditionIndex: Int, variable: Variable): Boolean {
        TODO("Not yet implemented")
    }

    fun conditionInAllPaths(conditionIndex: Int, variable: Variable): Boolean {
        TODO("Not yet implemented")
    }

    fun getAllocationFromAllPaths(index: Int, variable: Variable): DD<*> {
        TODO("Not yet implemented")
    }
}

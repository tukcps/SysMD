package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.services.session.Session

class SATBasedDiscreteSolver(
    val solver: Solver
) : DiscreteSolverIF {

    override fun isInitialized(): Boolean {
        TODO("Not yet implemented")
    }

    override fun initialize(model: Session) {
        TODO("Not yet implemented")
    }

    override fun advanceState() {
        TODO("Not yet implemented")
    }

    override fun update(updatedProperty: Variable) {
        TODO("Not yet implemented")
    }

    override fun update(scheduledProperties: List<Variable>) {
        TODO("Not yet implemented")
    }

    override fun assertConstraints() {
        TODO("Not yet implemented")
    }
}
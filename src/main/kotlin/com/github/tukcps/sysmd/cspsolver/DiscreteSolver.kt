package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.services.session.Session

class DiscreteSolver(
    val solver: Solver,
    val useDDs: Boolean = true
) : DiscreteSolverIF {

    private val internalSolver: DiscreteSolverIF = if (useDDs) DDBasedDiscreteSolver(solver) else SATBasedDiscreteSolver(solver)

    override fun isInitialized(): Boolean {
        return internalSolver.isInitialized()
    }

    override fun initialize(model: Session) {
        internalSolver.initialize(model)
    }

    override fun update(updatedProperty: Variable) {
        internalSolver.update(updatedProperty)
    }

    override fun update(scheduledProperties: List<Variable>) {
        internalSolver.update(scheduledProperties)
    }

    override fun advanceState() {
        internalSolver.advanceState()
    }

    override fun assertConstraints() {
        internalSolver.assertConstraints()
    }
}

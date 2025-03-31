package com.github.tukcps.sysmd.cspsolver

import com.github.tukcps.sysmd.services.session.Session

interface DiscreteSolverIF {

    fun isInitialized(): Boolean

    fun initialize(model: Session)

    fun update(scheduledProperties: List<Variable>)

    fun update(updatedProperty: Variable)

    fun advanceState()

    fun assertConstraints()
}
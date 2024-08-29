package com.github.tukcps.sysmd.cspsolver.propagator

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session

interface PropagatorIF {
    val model: Session

    fun execute(updatedValueFeature: Variable)
}
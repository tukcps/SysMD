package com.github.tukcps.sysmd.cspsolver.propagator

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session

abstract class Propagator(override val model: Session):PropagatorIF {

    abstract override fun execute(updatedValueFeature: Variable)

}
package com.github.tukcps.sysmd.cspsolver.analyzer

import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session

interface QuantorAnalyzer: AnalyzerIF {
    override val model: Session

    fun forAll(property: Variable, classifier: TypeImplementation): Boolean

    fun oneExists(property: Variable, classifier: TypeImplementation): List<Variable>
}
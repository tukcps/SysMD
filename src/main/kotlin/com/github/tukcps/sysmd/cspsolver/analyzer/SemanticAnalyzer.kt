package com.github.tukcps.sysmd.cspsolver.analyzer

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.Type

class SemanticAnalyzer(override val model: Session): SemanticAnalyzerIF {

    override fun updateProperty(updatedProperty: Variable) {
        //TODO!
    }

    override fun getClassifier(property: Variable): Type? {
        //TODO
        return property.feature.generalization.firstOrNull() as Type?
    }

    override fun getAnnotations(property: Variable): List<Annotation> {
        //TODO
        return listOf()
    }
}
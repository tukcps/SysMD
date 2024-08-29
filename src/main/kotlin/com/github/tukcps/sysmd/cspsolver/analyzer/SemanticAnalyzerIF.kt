package com.github.tukcps.sysmd.cspsolver.analyzer

import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.Type

interface SemanticAnalyzerIF: AnalyzerIF {

    override fun updateProperty(updatedProperty: Variable)

    fun getAnnotations(property: Variable): List<Annotation>

    fun getClassifier(property: Variable): Type?
}
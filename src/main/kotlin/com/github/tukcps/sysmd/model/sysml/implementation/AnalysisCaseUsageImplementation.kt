package com.github.tukcps.sysmd.model.sysml.implementation

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.sysml.AnalysisCaseDefinition
import com.github.tukcps.sysmd.model.sysml.AnalysisCaseUsage
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class AnalysisCaseUsageImplementation(model : Session,elementId : Uuid = Uuid.random()) : AnalysisCaseUsage, CaseUsageImplementation(model,elementId = elementId) {
    
    val analysisCaseDefinition: AnalysisCaseDefinition? = TODO()
    val resultExpression: Expression? = TODO()
    
}

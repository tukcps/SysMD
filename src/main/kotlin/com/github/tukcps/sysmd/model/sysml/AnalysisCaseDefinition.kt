package com.github.tukcps.sysmd.model.sysml

import com.github.tukcps.sysmd.model.expression.Expression

interface AnalysisCaseDefinition : CaseDefinition {
    val resultExpression: Expression?
}

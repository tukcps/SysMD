package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.semantics.kerml.AssociationActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.ConditionalExpressionActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.FunctionActions
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.implementation.AssociationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FunctionImplementation
import com.github.tukcps.sysmd.services.session.Session
import java.util.*


class SemanticActions(
    model: Session,
    compiler: KerML,
    owners: Stack<Resolved<Element>> = Stack<Resolved<Element>>(), // A stack of all nested element's owners
): ActionsContextImplementation(
    model=model,
    compiler=compiler,
    owners = owners,
) {
    fun functionActions() = FunctionActions<Function>(this, ::FunctionImplementation)
    fun associationActions() = AssociationActions<Association>(this, ::AssociationImplementation)
    fun constraintActions() = FeatureActions<Feature>(this,creator = ::FeatureImplementation, mutableListOf("ScalarValues::Boolean"))
    fun conditionalExpressionActions() = ConditionalExpressionActions(this)
    fun conditionalExpressionActions(i: AstLeaf, t: AstNode, e: AstNode) = ConditionalExpressionActions(this, i, t, e)
}
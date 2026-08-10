@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.ClassAction
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureAction
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.util.QualifiedName
import java.util.*

open class ActionDefinitionAction(
    context: ActionsContext,
    type: ElementType = ElementType.ActionDefinition,
    isImplicit: QualifiedName = "Actions::Action",
): ClassAction(
    context = context,
    type = type,
    isImplicit = isImplicit,
)

open class ActionUsageAction(
    context: ActionsContext,
    type: ElementType = ElementType.ActionUsage,
    isImplicit: QualifiedName = "Actions::Action",
): FeatureAction(
    context = context,
    type = type,
    isImplicit = isImplicit,
)

class AcceptActionUsageAction(
    context: ActionsContext,
    var payloadParameter : ReferenceUsage? = null,
): FeatureAction(
    context,
    type = ElementType.AcceptActionUsage,
    isImplicit = "Actions::Action",
) {
    override fun afterProduction() {
        element.declaredName = "accept_" + UUID.randomUUID().toString()
        payloadParameter?.let {
            // Hmm ... payloadParameter never written?
            //     context.addOwnedElement(payloadParameter)
        }
        super.afterProduction()
    }
}
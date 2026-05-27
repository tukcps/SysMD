@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.model.sysml.implementation.PartDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.PartUsageImplementation


class PartDefinitionActions(
    context: ActionsContext
): TypeActions<PartDefinitionImplementation>(
    context,
    creator = ::PartDefinitionImplementation,
) {
    override fun finish() {
        if (created.specialization.isEmpty()) {
            context.addSubclassification("Parts::Part")
        }
        super.finish()
    }
}



class PartUsageActions(
    context: ActionsContext,
): FeatureActions<PartUsageImplementation>(
    context,
    creator = ::PartUsageImplementation,
) {
    override fun finish() {
        if (created.specialization.isEmpty()) {
            context.addTyping("Parts::Part")
        }
        super.finish()
    }
}
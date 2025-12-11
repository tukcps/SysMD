@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.ClassActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.ReferenceUsage
import com.github.tukcps.sysmd.model.sysml.implementation.AcceptActionUsageImplementation
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*

class ActionDefinitionActions<T: Class>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: QualifiedName = "Base::Anything",
): ClassActions<T>(
    context = context,
    creator = creator,
    isImplicit = specializes,
)

class ActionUsageActions<T: ActionUsage>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    defaultType: QualifiedName = "Occurrences::Occurrence",
): FeatureActions<T>(
    context = context,
    creator = creator,
    defaultType = defaultType,
)

class AcceptActionUsageActions(
    context: ActionsContext,
    var payloadParameter : ReferenceUsage? = null,
): FeatureActions<AcceptActionUsageImplementation>(
    context,
    creator = ::AcceptActionUsageImplementation,
    defaultType = "Occurrences::Occurrence",
) {
    fun create() {
        super.create(Identification(name = "accept_" + UUID.randomUUID().toString()))
        payloadParameter?.let {
            context.model.addOwnedMember(it, created)
        }
    }
}
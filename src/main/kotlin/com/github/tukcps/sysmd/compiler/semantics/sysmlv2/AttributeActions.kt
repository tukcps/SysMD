package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.DataTypeActions
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import com.github.tukcps.sysmd.model.sysml.implementation.AttributeUsageImplementation
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*


class AttributeDefinitionActions<T: AttributeDefinition>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<String> = mutableListOf("Base::DataValue")
): DataTypeActions<AttributeDefinition>(context, creator, specializes)


class AttributeUsageActions(
    context: ActionsContext
): FeatureActions<AttributeUsage>(context, ::AttributeUsageImplementation, mutableListOf("Base::DataValue"))


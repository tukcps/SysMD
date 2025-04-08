package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation


class AssertActions(context: ActionsContext): FeatureActions<InvariantImplementation>(
    context = context,
    creator = ::InvariantImplementation,
    defaultType = mutableListOf("ScalarValues::Boolean"),
){
    var isNegated = false

    override fun create(identification: Identification) {
        super.create(identification)
        created?.isNegated = isNegated
    }
}
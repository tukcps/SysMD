@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.sysmlv2

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.expression.implementation.InvariantImplementation


class AssertActions(
    context: ActionsContext,
): FeatureActions<InvariantImplementation>(
    context = context,
    creator = ::InvariantImplementation,
) {
    override fun finish() {
        context.addTyping("ScalarValues::Boolean")
        super.finish()
    }
}
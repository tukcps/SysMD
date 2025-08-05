@file:Suppress("UNCHECKED_CAST")

package com.github.tukcps.sysmd.compiler.semantics.expression

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.util.SimpleName


class InvariantActions<T: Invariant>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
): FeatureActions<Invariant>(
    context, creator, defaultType = "ScalarValues::Boolean"
)
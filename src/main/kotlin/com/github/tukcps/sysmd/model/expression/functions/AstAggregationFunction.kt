package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.services.session.Session

/**
 * A function call of a user-defined function.
 */
abstract class AstAggregationFunction(
    name: String,
    model: Session
) :
    AstFunction(name, model, 0, ArrayList()) {
    abstract fun getDependentPropertyStrings(): Set<String>
}

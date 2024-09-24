package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.resolve.resolve


/**
 * Pre-defined SysMD function that checks if an element owns an element with a name given as parameter.
 * Example:
 *
 * Global hasA a: Boolean = hasA(x::y)
 *
 * evaluates to true, iff x::y is resolvable.
 */
class AstHastype(
    model: Session,
    private val owningNamespace: Namespace,
    private val subclassName: QualifiedName,
    private val superclassName: QualifiedName
) : AstFunction("hastype", model, 0) {

    override fun initialize() {
        upQuantity = Quantity(model.builder.Bool)
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        // Search for
        val subtype = owningNamespace.resolve<Element>(subclassName)
        val supertype = owningNamespace.resolve<Element>(superclassName)

        if (subtype is Type && supertype is Type) {
            upQuantity = Quantity(
                if (supertype in subtype.allSupertypes(true)) model.builder.True else model.builder.False
            )
        } else
            model.report(owningNamespace, "Evaluation of hastype not possible as parameters are no types.")
    }

    override fun evalDown() {
        // getParam(0).downQuantity = downQuantity.log()
    }

    override fun toExpressionString() = "hasA(${getParam(0).toExpressionString()}, ${getParam(1).toExpressionString()})"
}

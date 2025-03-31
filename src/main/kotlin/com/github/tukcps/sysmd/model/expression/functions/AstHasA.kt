package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.session.report
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session

class AstHasA(
    model: Session,
    private val owningNamespace: Namespace,
    private val ownerName: QualifiedName,
    private val ownedName: QualifiedName
) : AstFunction("hasA", model, 0) {

    override fun initialize() {
        upQuantity = Quantity(model.builder.Bool)
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        // Search for
        val owner = owningNamespace.resolve<Element>(ownerName)
        if (owner is Namespace) {
            upQuantity = if (owner.resolve<Element>(ownedName) != null)
                Quantity(model.builder.True)
            else Quantity(model.builder.False)
        } else
            model.report(null, "Evaluation of hasA() not possible as parameter not a namespace.")
    }

    override fun evalDown() {}
}

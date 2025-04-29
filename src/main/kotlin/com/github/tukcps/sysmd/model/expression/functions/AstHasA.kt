package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session

class AstHasA(
    model: Session,
    param: ArrayList<AstNode>,
    semantics: SemanticActions,
) : AstFunction("owns", model, 0) {
    val nameSpace = semantics.namespace
    val ownerName = (param[0] as AstLeaf).qualifiedName!!
    val ownedName = (param[1] as AstLeaf).qualifiedName!!

    override fun initialize() {
        upQuantity = Quantity(model.builder.Bool)
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        // Search for
        val owner = nameSpace.resolve<Element>(ownerName)
        if (owner is Namespace) {
            upQuantity = if (owner.resolve<Element>(ownedName) != null)
                Quantity(model.builder.True)
            else Quantity(model.builder.False)
        } else
            model.status.error("Evaluation of hasA() not possible as parameter not a namespace.")
    }

    override fun evalDown() {}
}

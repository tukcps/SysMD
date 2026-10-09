package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.util.mapToArrayList
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session

class AstHasA(
    model: Session,
    private val param: ArrayList<AstNode>,
    private val semantics: ActionsContext,
) : AstFunction("owns", model, 0) {
    val nameSpace = semantics.namespace
    val ownerName = (param[0] as AstLeaf).qualifiedName!!
    val ownedName = (param[1] as AstLeaf).qualifiedName!!

    override fun initialize() {
        upQuantity = VectorQuantity(model.builder.Bool.All)
        evalUp()
        downQuantity = upQuantity.clone()
    }

    override fun evalUp() {
        // Search for
        val owner = if (ownerName == "Global") model.global else nameSpace.resolve(ownerName)?.memberElement
        if (owner is Namespace) {
            upQuantity = when {
                owner.resolve(ownedName)?.memberElement !== null -> VectorQuantity(model.builder.Bool.True)
                else -> VectorQuantity(model.builder.Bool.False)
            } 
        } else
            model.status.error("Evaluation of hasA() not possible as parameter not a namespace.")
    }

    override fun evalDown() {}

    override fun clone() = AstHasA(model, param.mapToArrayList { it.clone() }, semantics)
}

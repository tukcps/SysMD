package com.github.tukcps.sysmd.cspsolver.propagator

import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.expression.functions.AstNot
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.session.Session

class RequirementPropagator(override val model: Session) : Propagator(model) {

    override fun execute(updatedValueFeature: Variable) {
        // require(value = updatedValueFeature.ofClass?.str == "Requirement") //FIXME: Or non-safe !! operator?

        //Special propagation for compare ops downward (see issues #184 and #202) goes here...
        TODO()
    }

    private fun getLeafs(ast: AstRoot) {
        val dependency = ast.dependency
        when (dependency) {
            is com.github.tukcps.sysmd.model.expression.AstBinOp -> {} //We're only interested in compare ops
            is AstNot -> {} //case: Not(comparison)
            else -> throw Exception("RequirementPropagator: Unknown AST Node.") //FIXME: Cases for every other ASTNode to do nothing?
        }
    }
}
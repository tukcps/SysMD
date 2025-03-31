package com.github.tukcps.sysmd.model.expression.functions

import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.services.session.Session

/**
 * A function call of a user-defined function.
 */
abstract class AstFunction(
    val name: String,                   // name of function
    model: Session,                // reference to the symbol table
    /** maximum number of parameters; will be checked in constructor. */
    parNo: Int = 1,                     // Number of expected parameters
    var parameters: ArrayList<AstNode> = ArrayList()
)  // Array with parameters
    : AstNode(model) {

    /** Initialization of Fields, simple semantic checks */
    init {
        if (parameters.size < parNo)
            throw SemanticError("Expecting at least $parNo parameters for function $name")

        for (p in parameters) {
            p.parent = this
        }
    }

    /** Root is next-higher statement of other kind or null if this is overall root */
    override var root: AstNode? = null
        set(r) {
            field = r
            for (p in parameters)
                p.root = r
        }

    /** Executes a block of statements on each AstNode in an Ast */
    override fun <T> runDepthFirst(block: AstNode.() -> T): T {
        for (p in parameters)
            p.runDepthFirst(block)
        return run(block)
    }

    override fun <R> withDepthFirst(receiver: AstNode, block: AstNode.() -> R): R =
        with(receiver) {
            for (p in parameters) withDepthFirst(p, block)
            return block()
        }

    /** Evaluate recursively all parameters, then this */
    override fun evalUpRec() {
        if (this is AstSumI) {
            sum!!.evalUpRec()
        } else {
            for (p in parameters) p.evalUpRec()
        }
        evalUp()
    }


    /** Recursively evaluate all parameters, then this */
    override fun evalDownRec() {
        evalDown()
        for (p in parameters)
            // if (!p.isBool) // uncomment for hotfix of the latest issue with ITE
            if(this !is AstIte) //evalDown for ITE params makes no sense
                p.evalDownRec()
    }

    /** No default method yet */
    override fun evalDown() {
        TODO("Inverse function missing for function $name")
    }

    /** gets the parameter no. nr, starting from 0 */
    fun getParam(nr: Int): AstNode {
        if (nr >= parameters.size) throw SemanticError("Not enough parameters")
        return parameters[nr]
    }

    /** gets the parameter no. nr, starting from 0, and throws exception if not of type BDD */
    open fun getBDDParam(nr: Int): AstNode {
        if (nr >= parameters.size) throw SemanticError("Not enough parameters")
        if (!parameters[nr].isBool) throw SemanticError("Expected Boolean parameters")
        return parameters[nr]
    }

    /** gets the parameter no. nr, starting from 0, and throws and exception if not of type AADD */
    fun getAADDParam(nr: Int): AstNode {
        if (nr >= parameters.size) throw SemanticError("Not enough parameters")
        if (!parameters[nr].isReal) throw SemanticError("Expected Real-valued parameters")
        return parameters[nr]
    }

    /** gets the parameter no. nr, starting from 0, and throws and exception if not of type IDD */
    fun getIDDParam(nr: Int): AstNode {
        if (nr >= parameters.size) throw SemanticError("Not enough parameters")
        if (!parameters[nr].isInt) throw SemanticError("Expected Integer parameters")
        return parameters[nr]
    }

    override fun clone(): AstFunction {
        // clone this first, creates clone with same type as this and shallow copy
        val clone: AstFunction = super.clone() as AstFunction

        // deep-clone parameters
        val parClone = ArrayList<AstNode>()
        for (p in parameters) parClone.add(p)

        // add cloned parameters to clone
        clone.parameters = parClone
        return clone
    }

    override fun toString(): String {
        var s = "$name("
        for (par in parameters) {
            s += "$par "
        }
        s += ")"
        return s
    }

    override fun toExpressionString() = "$name(${parameters.joinToString(", ") { it.toExpressionString() }})"
}

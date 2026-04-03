package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureTypingImplementation
import com.github.tukcps.sysmd.model.util.*
import io.github.tukcps.aadd.*

abstract class LiteralExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "LiteralExpression"
) : LiteralExpression, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
)
{
    override var isModelLevelEvaluable: Boolean = true

    final override var internalValue: AstNode?
        get() = literalValue
        set(value) {
            // TODO
            // require(literalValue == value) { "Cannot modify value of literal" }
        }

    abstract val literalValue: AstLeaf? //can't narrow value field down to Leaf, so here we go...

    override var domain: DD<*>? = null
        get() {
            //literalValue?.dd?.evaluate() }
            val dom = literalValue?.dd?.evaluate()
            when (dom) {
                is BDD -> (if (dom.height() == 0) return dom else return this.model?.builder?.Bool)
                is AADD -> return dom
                is IDD -> return dom
                //is String -> throw Exception("TODO")
                else -> TODO()
            }
        }

    /** Qualified name of this literal's type */
    abstract val typeName : QualifiedName
    protected abstract val cachedType : Type?

    /** Propagates type information around this  */
    override fun learnType() : List<Type> = listOf(cachedType ?: UnresolvedType(typeName))


    final override fun initialize()
    {
        evalUp()
        downQuantity = upQuantity
    }

    final override fun evalUp()
    {
        upQuantity = literalValue!!.literalVal!!
    }

    final override fun evalDown()
    {}

    override fun toAstString(b : StringBuilder, precedence : Int)
    {
        b.append(value ?: "unknown")
    }

    abstract override fun clone(): LiteralExpressionImplementation
}
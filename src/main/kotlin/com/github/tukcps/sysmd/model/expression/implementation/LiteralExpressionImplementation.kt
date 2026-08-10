package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.LiteralExpression
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD
import kotlin.uuid.Uuid

abstract class LiteralExpressionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
) : LiteralExpression, ExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression,
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
        get() =
            //literalValue?.dd?.evaluate() }
            when (val dom = literalValue?.dd?.evaluate()) {
                is BDD -> if (dom.height() == 0) dom else this.model.builder.Bool
                is AADD -> dom
                is IDD -> dom
                //is String -> throw Exception("TODO")
                else -> TODO()
            }

    /** Qualified name of this literal's type */
    abstract val typeName : QualifiedName
    protected abstract val cachedType : Type?

    /** Propagates type information around this  */
    override fun learnType() : List<Type> = listOf(
        cachedType ?: UnresolvedType(model, typeName)
    )


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
    {
        downQuantity = upQuantity
    }

    override fun toAstString(b : StringBuilder, precedence : Int)
    {
        b.append(value ?: "unknown")
    }

    abstract override fun clone(): LiteralExpressionImplementation
}
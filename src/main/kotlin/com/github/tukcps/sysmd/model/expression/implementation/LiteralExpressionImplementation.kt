package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.*
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.*
import io.github.tukcps.aadd.values.ScalarValue
import io.github.tukcps.aadd.values.bounds.LongBound
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

    override var domain: DD<ScalarValue>? = null
        get() =
            //literalValue?.dd?.evaluate() }
            when (val dom = literalValue?.dd?.evaluate()) {
                is BDD -> if (dom.height() == 0) dom else this.model.builder.Bool.All
                is AADD -> dom
                is IDD -> dom
                //is String -> throw Exception("TODO")
                else -> TODO()
            } as DD<ScalarValue>

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
        b.append(when (val v = value) {
            null -> "unknown"
            is LongBound -> v.toString()
            else -> v
        })
    }

    abstract override fun clone(): LiteralExpressionImplementation
}
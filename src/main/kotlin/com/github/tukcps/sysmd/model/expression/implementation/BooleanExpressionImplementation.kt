package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.datamodel.ElementReference
import com.github.tukcps.sysmd.model.expression.BooleanExpression
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class BooleanExpressionImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
): BooleanExpression, ExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression,
) {
    override fun clone(): BooleanExpressionImplementation = BooleanExpressionImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        expression = expression,
    ).also {
        it.updateFrom(this)
    }

    override fun learnType() = listOf(
        model.repo.booleanType ?: UnresolvedType(model, ElementReference.ByName("ScalarValues::Boolean"))
    )

    override fun initialize() {
        val result = this.result ?: return
        result.initialize()
        upQuantity = result.upQuantity
        downQuantity = result.downQuantity
    }

    override fun evalUp() {
        val result = this.result ?: return
        result.evalUp()
        upQuantity = result.upQuantity
    }

    override fun evalDown() {
        val result = this.result ?: return
        result.evalDown()
        downQuantity = result.downQuantity
    }

    override fun toAstString(b: StringBuilder, precedence: Int) {
        name?.let {
            b.append(' ')
            b.append(it)
        }

        this.result?.let {
            b.append("{ ")
            it.toAstString(b, 0)
            b.append(" }")
        }
    }
}
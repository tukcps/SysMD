package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralString
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import kotlinx.serialization.json.Json
import kotlin.uuid.Uuid

class LiteralStringImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    expression: String? = null,
) : LiteralString, LiteralExpressionImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    expression = expression
) {
    override var isNameLiteral : Boolean = false
    override var value: String? = null

    override val literalValue : AstLeaf?
        get() {
            val v = value ?: return null
            return AstLeaf(model, VectorQuantity(listOf(model.builder.string(v))))
        }

    override val cachedType get() = model.repo.stringType
    override val typeName = "ScalarValues::String"

    override fun toAstString(b : StringBuilder, precedence : Int)
    {
        if(isNameLiteral)
            b.append(value)
        else
            b.append(Json.encodeToString(value))
    }

    override fun clone() = LiteralStringImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        expression = expression
    ).also {
        it.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        if(template is LiteralString)
        {
            value = template.value
            isNameLiteral = template.isNameLiteral
        }

        super.updateFrom(template)
    }
}
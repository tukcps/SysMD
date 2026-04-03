package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.RawNameExpression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName

class RawNameExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    elementType: String = "RawNameExpression",
) : RawNameExpression, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
) {
    override var rawName: String? = null

    override fun initialize() { }

    override fun evalUp() { }

    override fun evalDown() { }

    override fun toAstString(b: StringBuilder, precedence: Int) {
        b.append(rawName)
    }

    override fun learnType(): List<Nothing> = emptyList()

    override fun updateFrom(template: Element) {
        super.updateFrom(template)

        if(template is RawNameExpression)
            this.rawName = template.rawName
    }

    override fun clone(): RawNameExpressionImplementation = RawNameExpressionImplementation(
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        typeConstraint = typeConstraint,
        expression = expression,
        elementType = elementType
    ).also {
        it.updateFrom(this)
    }
}
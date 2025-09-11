package com.github.tukcps.sysmd.model.expression.implementation

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.LiteralExpression
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.util.SimpleName
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DD
import io.github.tukcps.aadd.IDD

open class LiteralExpressionImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    direction: Feature.FeatureDirectionKind = Feature.FeatureDirectionKind.INOUT,
    isEnd: Boolean = false,
    typeConstraint: MutableList<String> = mutableListOf(),
    expression: String? = null,
    textualRepresentation:  MutableList<TextualRepresentation> = mutableListOf(),
    elementType: String = "LiteralExpression"
) : LiteralExpression, ExpressionImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    direction = direction,
    isEnd = isEnd,
    textualRepresentation = textualRepresentation,
    typeConstraint = typeConstraint,
    expression = expression,
    elementType = elementType
)
{
    override var isModelLevelEvaluable: Boolean = true

    override var internalValue: AstNode?
        get() {return super.internalValue as AstLeaf}
        set(value) {super.internalValue = value as AstLeaf}

    var literalValue: AstLeaf? //can't narrow value field down to Leaf, so here we go...
        get() {return internalValue as AstLeaf}
        set(value) {this.internalValue = value} //FIXME: Unit test to check for overshadowing

    override var domain: DD<*>? = null
        get() {
            //literalValue?.dd?.evaluate() }
            val dom = literalValue?.dd?.evaluate()
            when (dom) {
                is BDD -> (if (dom.height() == 0) return dom else return this.model?.builder?.Bool)
                is AADD -> return dom
                is IDD -> return dom
                //is String -> throw Exception("TODO")
                else -> throw Exception("TODO")
            }
        }

    override fun checkCondition(target: Element): Boolean {
        return super.checkCondition(target)
    }

    override fun evaluate(target: Element): Set<Element> {
        return super.evaluate(target)
    }

    override fun modelLevelEvaluable(visited: Set<Feature>): Boolean {
        return super.modelLevelEvaluable(visited)
    }
}
package models.expression

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.FeatureReferenceExpression
import com.github.tukcps.sysmd.model.expression.LiteralInteger
import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind.IN
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.services.session.Session
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

fun Session.literalExpression(value : Long) = literalExpression(value.toString(), value)
fun Session.literalExpression(value : Int) = literalExpression(value.toLong())
fun Session.literalExpression(value : Boolean) = literalExpression(value.toString(), value)

fun Session.featureReferenceExpression(name : QualifiedName, resolve : Boolean = true) : FeatureReferenceExpression = FeatureReferenceExpressionImplementation(this).also {
	it.declaredName = it.elementId.toString()
	it.declaredShortName = it.declaredName
	it.direction = IN

	it.referent = if(resolve) global.resolve(name)!!.member()!! else UnresolvedFeature(this, name)
}

fun Session.operatorExpression(op : String, vararg operands : Expression) : OperatorExpression = OperatorExpressionImplementation(this).also {
	it.declaredName = it.elementId.toString()
	it.declaredShortName = it.declaredName
	it.operator = op
	it.direction = IN

	for(op in operands)
		addOwnedMember(op, it)
}

fun Session.operatorExpression(op : String, name : String, vararg operands : Expression)
= operatorExpression(op, *operands).also {
	it.declaredName = name
	it.declaredShortName = name
}

fun Session.literalExpression(name : String?, value : Long) = LiteralIntegerImplementation(
	this, declaredName = name
).also {
	it.value = value
	it.direction = IN
}

fun Session.literalExpression(name : String?, value : String) = LiteralStringImplementation(
	this, declaredName = name
).also {
	it.value = value
	it.direction = IN
}

fun Session.literalExpression(name : String?, value : Boolean) = LiteralBooleanImplementation(
	this, declaredName = name
).also {
	it.value = value
	it.direction = IN
}

fun Session.twoPlusTwo() = operatorExpression(
	"+", "2+2",
	literalExpression("left 2", 2),
	literalExpression("right 2", 2)
)

fun assertIsTwoPlusTwo(x : Element) : OperatorExpression
{
	val tt2 = assertIs<OperatorExpression>(x)
	assertEquals("+", tt2.operator)
	// FIXME: Direction changes to INOUT on deserialization?
	//assertEquals(IN, tt2.direction)

	assertEquals(2, tt2.argument.size)
	assertNotEquals(tt2.argument[0], tt2.argument[1])

	for(a in tt2.argument)
	{
		assertIs<LiteralInteger>(a)
		// assertEquals(IN, a.direction)
		// FIXME: value not exposed yet
	}

	return tt2
}
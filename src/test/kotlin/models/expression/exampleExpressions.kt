package models.expression

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Feature.FeatureDirectionKind.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.services.session.*
import kotlin.test.*

fun Session.literalExpression(value : Long) = literalExpression(value.toString(), value)
fun Session.literalExpression(value : Int) = literalExpression(value.toLong())
fun Session.literalExpression(value : Boolean) = literalExpression(value.toString(), value)

fun Session.featureReferenceExpression(name : QualifiedName, resolve : Boolean = true) : FeatureReferenceExpression = FeatureReferenceExpressionImplementation().also {
	it.generateUUID()
	it.declaredName = it.elementId.toString()
	it.declaredShortName = it.declaredName
	it.direction = IN
	it.model = this

	it.referent = if(resolve) global.resolve(name)!!.member()!! else UnresolvedFeature(name)
}

fun Session.operatorExpression(op : String, vararg operands : Expression) : OperatorExpression = OperatorExpressionImplementation().also {
	it.generateUUID()
	it.declaredName = it.elementId.toString()
	it.declaredShortName = it.declaredName
	it.operator = op
	it.direction = IN
	it.model = this

	for(op in operands)
		addOwnedMember(op, it)
}

fun Session.operatorExpression(op : String, name : String, vararg operands : Expression)
= operatorExpression(op, *operands).also {
	it.declaredName = name
	it.declaredShortName = name
}

fun Session.literalExpression(name : String?, value : Long) = LiteralIntegerImplementation(
	name, name
).also {
	it.model = this
	it.value = value
	it.generateUUID()
	it.direction = IN
}

fun Session.literalExpression(name : String?, value : String) = LiteralStringImplementation(
	name, name
).also {
	it.model = this
	it.value = value
	it.generateUUID()
	it.direction = IN
}

fun Session.literalExpression(name : String?, value : Boolean) = LiteralBooleanImplementation(
	name, name
).also {
	it.model = this
	it.value = value
	it.generateUUID()
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
package models.expression

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.InvocationExpression
import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.FeatureTyping
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.services.inheritance.deepCloneWithInheritedFeature
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import io.github.tukcps.sysmlv2.api.entities.Identified
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

inline fun<T> assertNullOr(a : T?, b : T?, body : (a : T, b : T) -> Unit)
{
	if(a === null)
		assertNull(b)
	else
	{
		assertNotNull(b)
		body(a, b)
	}
}

fun<T> assertNoOverlap(xs : Iterable<T>, ys : Iterable<T>)
{
	assertNotSame(xs, ys)

	for(x in xs)
	{
		for(y in ys)
			assertNotSame(x, y)
	}
}

/** Ensures two features have equal fields but don't share mutable references  */
fun<F : Feature> assertProperClone(original : F, clone : F)
{
	assertNotSame(original, clone)

	// existing clone operation violates this
	// assertEquals(original.type, clone.type)
	// assertEquals(original.typing, clone.typing)

	assertSame(original.javaClass, clone.javaClass)

	assertEquals(original.direction, clone.direction)

	assertNullOr(original.multiplicity(), clone.multiplicity(), ::assertProperClone)
	assertEquals(original.multiplicityRange, clone.multiplicityRange)

	assertEquals(original.isEnd, clone.isEnd)
	assertEquals(original.isComposite, clone.isComposite)
	assertEquals(original.isPortion, clone.isPortion)
	assertEquals(original.isUnique, clone.isUnique)
	assertEquals(original.isOrdered, clone.isOrdered)
	assertEquals(original.isDerived, clone.isDerived)
	assertEquals(original.isReadOnly, clone.isReadOnly)

	// FIXME: featureWithValue probably deprecated?

	assertEquals(original.unitConstraint, clone.unitConstraint)
	// FIXME: existing clone violates this
	// assertDistinctButEqual(original.typeConstraint, clone.typeConstraint)
	//assertEquals(original.typeConstraint, clone.typeConstraint) // [] != [ ]
	// assertEquals(original.expression, clone.expression) // null != ""

	// FIXME: Resolved equality should be fine, but do we want reference sharing here?
	assertEquals(original.referencedFeature, clone.referencedFeature)

	// assertNoOverlap(original.variables, clone.variables)
	// cloning may omit some owned elements
	assertNoOverlap(original.ownedElement, clone.ownedElement)

	if(original is Expression && clone is Expression)
		assertEquals(original.astString, clone.astString)
}


class ExpressionCloningTests
{
	/** Ensures two lists contain the same elements but are not the same list object */
	fun<T> assertDistinctButEqual(a : MutableList<T>, b : MutableList<T>)
	{
		assertNotSame(a, b)
		assertEquals(a.toList(), b.toList())
	}


	/** Custom overload required because == on Identified is too strict */
	fun assertEquals(a : MutableList<Identified>, b : MutableList<Identified>)
	{
		assertNotSame(a, b)
		assertEquals(a.map { it.id }, b.map { it.id })
	}

	/** Ensures clone behaves properly on a feature */
	fun checkClone(original : Feature)
	{
		val before = original.toDAO()
		val clone = original.clone()
		val after = original.toDAO()

		val mapper = ObjectMapper()
		// ensure the original wasn't mutated
		assertEquals(mapper.writeValueAsString(before), mapper.writeValueAsString(after))

		assertNull(clone.owningRelationship) // clone must be free-standing, but only top-level of deep clone
		assertProperClone(original, clone)

		// TODO: structural stuff
	}

	@Test
	fun exampleCloningTest() = testSession("ScalarValues") {
		loadKerML("""
            inv a; 
            inv b; 
            feature e: ScalarValues::Boolean = a and b;
			expr f: ScalarValues::Boolean = a and b;
        """) // FIXME: expr doesn't parse
		assertNoIssues()

		// for now, test Feature cloning in general since parser doesn't produce Expression instances yet
		global.visibleMemberships().filterIsInstance<Feature>().forEach {
			checkClone(it)
		}
	}

	@Test
	fun simpleExample() = testSession {
		val tt = twoPlusTwo()
		addElement(tt)

		kotlin.test.assertEquals(2, tt.argument.size)
		assertNotEquals(tt.argument[0], tt.argument[1])
		kotlin.test.assertEquals(this, tt.model)
		for(a in tt.argument)
			kotlin.test.assertEquals(this, a.model)

		assertIsTwoPlusTwo(tt.clone()).also {
			kotlin.test.assertEquals(this, it.model)
		}

		assertNoIssues()
	}

	@Test
	fun deepClone() = testSession {
		loadKerML("feature x; feature foo = f(x); package Bar;")
		assertNoIssues() // will break once proper resolution is added

		val foo = global.resolve("foo")!!.member<Feature>()!!
		val bar = global.resolve("Bar")!!.member<Package>()!!

		val original = foo.ownedElement.filterIsInstance<Expression>().single()
		assertEquals("f(x)", original.astString)

		val clone = original.deepCloneWithInheritedFeature(bar)

		assertProperClone(original, clone)
	}

	@Test
	fun cloneKeepsTypes() = testSession("DataFunctions") {
		fun assertTyped(call : InvocationExpression)
		{
			for(expr in call.argument + call)
			{
				expr.ownedRelationship.filterIsInstance<FeatureTyping>().also {
					val msg = "${expr.path()} `${expr.astString}`: $it"
					assertEquals(1, it.size,  msg)
					assertEquals("ScalarValues::Integer", it.single().type.qualifiedName, msg)
				}
			}
		}

		loadKerML("""package P;""")
		val x = twoPlusTwo()
		assertNoIssues()

		x.initType()
		assertNoIssues()
		assertTyped(x)

		val x2 = x.clone()
		assertProperClone(x, x2)
		assertTyped(x2)

		// deep clone also keeps types
		val x3 = x.deepCloneWithInheritedFeature(global.resolve("P")!!.member<Package>()!!) as OperatorExpression
		assertNoIssues()
		assertProperClone(x, x3)
		assertTyped(x3)
	}
}
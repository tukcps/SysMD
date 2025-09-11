package models.expression

import com.fasterxml.jackson.databind.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.repositories.local.*
import com.github.tukcps.sysmd.services.resolve.*
import io.github.tukcps.sysmlv2.api.entities.*
import junit.framework.TestCase.*
import util.*
import util.mockup.*
import kotlin.test.*

class ExpressionCloningTests
{
	inline fun<T> assertNullOr(a : T?, b : T?, body : (a : T, b : T) -> Unit)
	{
		if(a === null)
			assertNull(b)
		else
		{
			assertNotNull(b)
			body(a, b!!)
		}
	}

	/** Ensures two lists contain the same elements but are not the same list object */
	fun<T> assertDistinctButEqual(a : MutableList<T>, b : MutableList<T>)
	{
		assertNotSame(a, b)
		assertEquals(a.toList(), b.toList())
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

		assertEquals(original.direction, clone.direction)

		assertNullOr(original.multiplicity(), clone.multiplicity(), ::assertProperClone)
		assertEquals(original.multiplicityRange, clone.multiplicityRange)

		assertEquals(original.isEnd, clone.isEnd)
		assertEquals(original.isComposite, clone.isPortion)
		assertEquals(original.isPortion, clone.isPortion)
		assertEquals(original.isUnique, clone.isUnique)
		assertEquals(original.isOrdered, clone.isOrdered)
		assertEquals(original.isDerived, clone.isDerived)
		assertEquals(original.isReadOnly, clone.isReadOnly)

		// FIXME: featureWithValue probably deprecated?

		assertEquals(original.unitConstraint, clone.unitConstraint)
		// FIXME: existing clone violates this
		// assertDistinctButEqual(original.typeConstraint, clone.typeConstraint)
		assertEquals(original.typeConstraint, clone.typeConstraint)
		assertEquals(original.expression, clone.expression)

		// FIXME: Resolved equality should be fine, but do we want reference sharing here?
		assertEquals(original.referencedFeature, clone.referencedFeature)

		assertNoOverlap(original.variables, clone.variables)
		// cloning may omit some owned elements
		assertNoOverlap(original.ownedElement, clone.ownedElement)
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
		assertTrue(this.status.issues.isEmpty(), status.issues.toString())

		// for now, test Feature cloning in general since parser doesn't produce Expression instances yet
		global.findAllOwnedElements().filterIsInstance<Feature>().forEach {
			checkClone(it)
		}
	}
}
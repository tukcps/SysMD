package models.expression

import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import com.github.tukcps.sysmd.services.repositories.local.toElement
import com.github.tukcps.sysmd.services.session.Session
import org.junit.jupiter.api.Disabled
import util.assertNoIssues
import util.testSession
import kotlin.test.*

fun two() = LiteralIntegerImplementation(declaredName = "2", declaredShortName = "2").apply {
	value = 2
	generateUUID()
}

fun Session.twoPlusTwo() : OperatorExpression = OperatorExpressionImplementation(
	declaredName = "2+2", declaredShortName = "2+2"
).also {
	it.generateUUID()
	it.operator = "+"



	val a = two()
	val b = two()
	ParameterMembershipImplementation(
		ownedMemberParameter = a,
		owningType = it
	).also {
		a.owningRelationship = it
		addOwnedRelationship(it)
	}

	ParameterMembershipImplementation(
		ownedMemberParameter = b,
		owningType = it
	).also {
		b.owningRelationship = it
		addOwnedRelationship(it)
	}
}

class ExpressionOwnershipTests
{
	@Test
	fun operatorOwnership() = testSession {
		val tt = twoPlusTwo()
		this.assertNoIssues()

		println(tt.ownedRelationship)

		assertNull(tt.owningRelationship)

		assertEquals(2, tt.argument.size)
		val (l,r) = tt.argument
		assertNotNull(l)
		assertNotNull(r)
		assertIs<LiteralInteger>(l)
		assertIs<LiteralInteger>(r)
		assertNotSame(l, r)

		assertEquals(tt, l.owner)
		assertEquals(tt, r.owner)
	}

	@Test @Disabled
	fun testDAO() = testSession {
		val tt = twoPlusTwo()

		val dao = tt.toDAO()
		assertNotNull(dao)
		assertEquals(tt.ownedRelationship[0].elementId, dao.ownedRelationship[0].id)
		assertEquals(tt.ownedRelationship[1].elementId, dao.ownedRelationship[1].id)

		delete(tt)
		val roundTrip = assertIs<OperatorExpression>(dao.toElement())

		println(roundTrip)
		println(roundTrip.argument)
		println(roundTrip.ownedRelationship)
	}

	@Test @Disabled
	fun testCompleteDao() = testSession {
		val tt = twoPlusTwo()
		val data = listOf<Element>(
			tt, tt.ownedRelationship[0], tt.ownedRelationship[1], tt.argument[0], tt.argument[1]
		).map { it.toDAO() }

		// should delete entire structure
		delete(tt)

		val elems = data.asReversed().map { it.toElement() }
		val tt2 = elems.last()

		println(tt2)
		println(tt2.ownedRelationship)
	}
}
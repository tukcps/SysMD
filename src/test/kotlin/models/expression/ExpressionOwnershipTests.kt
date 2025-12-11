package models.expression

import com.github.tukcps.sysmd.model.expression.LiteralInteger
import util.assertNoIssues
import util.testSession
import kotlin.test.*

class ExpressionOwnershipTests
{
	@Test
	fun exampleOwnership() = testSession {
		val tt = twoPlusTwo()
		this.assertNoIssues()

		// println(tt.ownedRelationship)

		assertEquals(global, tt.owningNamespace)

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
}
package models.expression

import com.github.tukcps.sysmd.model.expression.LiteralInteger
import com.github.tukcps.sysmd.model.expression.OperatorExpression
import util.assertNoIssues
import util.mockup.loadSysMLv2
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

	@Test
	fun ownedBySysMlAssert() = testSession("ScalarValues") {
		loadSysMLv2("""
			attribute x : ScalarValues::Integer;
			assert constraint invariant { x > 0 }
		""".trimIndent())
		// assertNoIssues()

		val cmp = get().filterIsInstance<OperatorExpression>().single()
		assertEquals("invariant", cmp.owner?.path())
	}
}
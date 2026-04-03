package models.expression

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.expression.*
import com.github.tukcps.sysmd.model.expression.implementation.InvocationExpressionImplementation
import com.github.tukcps.sysmd.model.kerml.ParameterMembership
import util.*
import util.mockup.loadKerML
import kotlin.test.*

class InvocationExpressionsTests {
	@Test
	fun namedArgumentResolution() = testSession("ScalarValues") {
		loadKerML("""
			private import ScalarValues::*;
			function foo { in x : Integer[1]; in y : Boolean[1]; return : String[1] }
		""".trimIndent())
		assertNoIssues()

		val expr = InvocationExpressionImplementation(
			"result", "result"
		).also {
			it.model = this
			it.functionName = "foo"
			addOwnedMember(global, it)

			val y = literalExpression(true).apply {
				declaredName = "y"
				declaredShortName = "y"
			}
			val x = literalExpression(17).apply {
				declaredName = "x"
				declaredShortName = "x"
			}

			// setting negative indices signals that name-based reordering is required
			assertIs<ParameterMembership>(
				addOwnedMember(y, it).owningRelationship
			).apply {
				parameterIndex = -1
			}

			assertIs<ParameterMembership>(
				addOwnedMember(x, it).owningRelationship
			).apply {
				parameterIndex = -1
			}
		}

		// reorder arguments based on names
		expr.initType()

		expr.argument.let { argument ->
			assertEquals(2, argument.size)
			assertIs<LiteralInteger>(argument[0]).also {
				assertEquals(Identification("x"), Identification(it))
			}
			assertIs<LiteralBoolean>(argument[1]).also {
				assertEquals(Identification("y"), Identification(it))
			}
		}

		assertEquals("foo", expr.function?.qualifiedName)
	}

}
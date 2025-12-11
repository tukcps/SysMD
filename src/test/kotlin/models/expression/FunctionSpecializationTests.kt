package models.expression

import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.kerml.Function
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class FunctionSpecializationTests
{
	@Test
	fun abstractFunctionTest() = testSession("ScalarValues") {
		loadKerML("""
			private import ScalarValues::*;
			abstract function foo { in x : Integer[0..1]; in y : Real[0..2]; return : String[1..3] }			
			function fooImpl specializes foo { in x : Integer[1]; in y : Real[1..2]; return : String[2] }
		""")
		assertNoIssues()
		val foo = assertNotNull(global.resolve("foo")).memberElement as Function
		val fooImpl = assertNotNull(global.resolve("fooImpl")).memberElement as Function

		assertTrue(foo.isAbstract)
		assertFalse(fooImpl.isAbstract)

		assertEquals(3, foo.features().size, foo.features().joinToString())
		assertEquals(3, fooImpl.features().size, fooImpl.features().joinToString())
		assertEquals(3, foo.parameter.size, foo.parameter.joinToString())
		assertEquals(3, fooImpl.parameter.size, fooImpl.parameter.joinToString())

		assertEquals(foo.parameter.last(), foo.result)
		assertEquals(fooImpl.parameter.last(), fooImpl.result)
	}

	@Test
	fun outOfOrder() = testSession("ScalarValues") {
		loadKerML("""
			private import ScalarValues::*;
			abstract function foo { in x : Integer[0..1]; in y : Real[0..2]; return : String[1..3] }			
			function fooImpl specializes foo { return : String[2]; in y : Real[1..2]; in x : Integer[1]; }
		""".trimIndent())
		assertNoIssues()
		val foo = assertNotNull(global.resolve("foo")).memberElement as Function
		val fooImpl = assertNotNull(global.resolve("fooImpl")).memberElement as Function

		listOf(foo, fooImpl).forEach {
			val p = it.parameter
			assertEquals("x", p[0].name, p[0].qualifiedName)
			assertEquals("y", p[1].name, p[0].qualifiedName)
			assertNull(p[2].name, p[0].qualifiedName)
		}
	}

	@Test
	fun identificationHashCode()
	{
		val a = Identification("a", "c")
		val b = Identification("b", "c")

		assertEquals(a, b)
		assertEquals(a.hashCode(), b.hashCode())
	}
}
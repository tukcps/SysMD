package models.expression

import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.util.firstName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class DataFunctionTests
{
	@Test
	fun operatorResolutionTest() = testSession("DataFunctions") {
		assertNoIssues()
		val expr = twoPlusTwo()
		val f = expr.function

		assertNotNull(f)
		assertEquals("IntegerFunctions", f.qualifiedName!!.firstName())

		assertEquals(3, f.parameter.size)
		assertNotNull(f.result)
		assertEquals(f.parameter[2], f.result)

		assertEquals(1, expr.type.size)
		assertEquals("ScalarValues::Integer", expr.type.single().qualifiedName)
	}

	@Test
	fun testOverloadByArity() = testSession("ScalarValues") {
		loadKerML("""
			private import ScalarValues::*;
			abstract function f { in xs : Integer[1..10]; return : Real }
			function f3 :> f { in xs : Integer[3] }
			function f5 :> f { in xs : Integer[5] }
		""".trimIndent())
		assertNoIssues()

		val call1 = InvocationExpressionImplementation(
			"call1", "call1",
		).apply {
			model = this@testSession
			functionName = "f"
		}
		for(i in 1..5)
			addOwnedMember(literalExpression("call1_arg$i", i.toLong()), call1)

		val call2 = InvocationExpressionImplementation(
			"call2", "call2",
		).apply {
			model = this@testSession
			functionName = "f"
		}
		for(i in 1..3)
			addOwnedMember(literalExpression("call2_arg$i", i.toLong()), call2)

		assertEquals("f(1, 2, 3, 4, 5)", call1.toAstString())
		assertEquals("f(1, 2, 3)", call2.toAstString())

		val f = assertNotNull(call1.functionName)
		assertEquals(f, assertNotNull(call2.functionName))

		val c1 = assertNotNull(call1.function)
		val c2 = assertNotNull(call2.function)
		assertNotSame(c1, c2)
		assertEquals("f5", c1.qualifiedName)
		assertEquals("f3", c2.qualifiedName)
	}

	@Test
	fun testOverloadByType() = testSession("ScalarValues") {
		loadKerML("""
			private import ScalarValues::*;
			abstract function f { in xs : Base::DataValue; return : Real }
			function fI :> f { in xs : Integer }
			function fS :> f { in xs : String }
		""".trimIndent())
		assertNoIssues()

		val callS = InvocationExpressionImplementation(
			"call1", "call1",
		).apply {
			model = this@testSession
			functionName = "f"
		}
		addOwnedMember(literalExpression("call1_arg", "foobar"), callS)

		val callI = InvocationExpressionImplementation(
			"call2", "call2",
		).apply {
			model = this@testSession
			functionName = "f"
		}
		addOwnedMember(literalExpression("call2_arg", 4711L), callI)

		assertEquals("f(\"foobar\")", callS.toAstString())
		assertEquals("f(4711)", callI.toAstString())

		val f = assertNotNull(callS.functionName)
		assertEquals(f, assertNotNull(callI.functionName))

		val fS = assertNotNull(callS.function)
		val fI = assertNotNull(callI.function)
		assertNotSame(fS, fI)
		assertEquals("fS", fS.qualifiedName)
		assertEquals("fI", fI.qualifiedName)
	}

	@Test @Ignore // FIXME: the import+resolution hack broke
	fun testNameResolution() = testSession("ScalarValues") {
		loadKerML("""
			private import ScalarValues::*;
			package pkg {
				feature p : Real = 4.2;
				feature q : Real = 52.09;
				feature a : Integer = 9999;
			
				function f { in a : Integer; in b : String; return : Real }
			}
			
			feature b : String = "dummy";
			feature q : String = "dummy"
		""".trimIndent())
		assertNoIssues()

		val call = InvocationExpressionImplementation(
			"call", "call",
		).apply {
			model = this@testSession
			functionName = "pkg::f"
		}
		addOwnedMember(literalExpression(null, 47), call)
		addOwnedMember(literalExpression(null, "11"), call)
		addOwnedMember(call, global)

		assertNotNull(call.resolve("a")).also {
			val li = assertIs<LiteralIntegerImplementation>(it.memberElement)
			assertEquals(47, li.value)
		}
		assertNotNull(call.resolve("b")).also {
			val ls = assertIs<LiteralStringImplementation>(it.memberElement)
			assertEquals("11", ls.value)
		}

		call.function // this triggers import generation

		// ensure context of function expansion is present
		assertNotNull(call.resolve("p")).also {
			val f = assertIs<Feature>(it.memberElement)
			assertEquals("4.2", f.expression)
		}

		assertNotNull(call.resolve("q")).also {
			val f = assertIs<Feature>(it.memberElement)
			assertEquals("52.09", f.expression)
		}
	}

	@Test
	fun iteTest() = testSession("DataFunctions") {
		val expr = operatorExpression("if",
			literalExpression(true), literalExpression(20), literalExpression(10))
		expr.initialize()
		expr.evalUpRec()
		expr.evalDownRec()

		assertEquals(VectorQuantity(builder.integer(20)), expr.upQuantity)
	}

	@Test
	fun iteTest2() = testSession("DataFunctions") {
		loadKerML("""
			private import ScalarValues::*;
			feature x : Boolean;
		""".trimIndent())
		assertNoIssues()

		val x = featureReferenceExpression("x")
		val expr = operatorExpression("if", x,
			literalExpression(20), literalExpression(30)
		)
		val twenty = VectorQuantity(builder.integer(20))

		expr.initialize()
		expr.downQuantity = twenty

		expr.evalDownRec()

		// boolean variables aren't written back to variables
		assertEquals(VectorQuantity(builder.True), x.downQuantity)
		assertEquals(twenty, expr.downQuantity)
	}
}
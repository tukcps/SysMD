package models.expression

import com.github.tukcps.sysmd.model.expression.implementation.*
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.util.firstName
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.Runlevel
import org.junit.jupiter.api.assertAll
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class DataFunctionTests
{
	private fun Function.acceptsArity(n : Int) : Boolean
	{
		var n = n.toLong()

		result.let { r -> parameter.filter { it !== r } }.map { it.multiplicityRange }.forEach {
			when {
				n in it -> n = 0
				n > it.max -> n -= it.max
				else -> return false
			}
		}

		return true
	}

	@Test @Ignore // TODO: istype, hastype, @, @@, as, meta, !=, ===, !==, and, or, implies, ??, all
	fun testStdlibComplete() = testSession("DataFunctions") {
		assertNoIssues()
		val ops = UnaryOperatorInformation.bySymbol.entries + BinaryOperatorInformation.bySymbol.entries +
				TernaryOperatorInformation.bySymbol.entries + CallLikeOperatorInformation.bySymbol.entries

		assertAll(ops.map { (op,info) ->
			{
				val name = "${info.namespace}::$op"
				val f = global.resolve(name)?.memberElement
				assertIs<Function>(f, "Could not resolve $name")

				val arity = when(info)
				{
					is BinaryOperatorInformation -> 2
					is TernaryOperatorInformation -> 3
					is UnaryOperatorInformation -> 1
					is CallLikeOperatorInformation -> 2
				}

				assertSame(assertNotNull(f.result), f.parameter.last(),
					"$name has improper result parameter")
				assertTrue(f.acceptsArity(arity), "$name cannot be invoked with $arity operands")
			}
		})
	}

	@Test
	fun operatorResolutionTest() = testSession("DataFunctions") {
		assertNoIssues()
		val expr = twoPlusTwo()
		expr.initType()
		assertNoIssues()

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

		assertEquals("f(1, 2, 3, 4, 5)", call1.astString)
		assertEquals("f(1, 2, 3)", call2.astString)

		call1.initType()
		call2.initType()
		assertNoIssues()
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

		assertEquals("f(\"foobar\")", callS.astString)
		assertEquals("f(4711)", callI.astString)

		callS.initType()
		callI.initType()
		assertNoIssues()

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

		call.initType()
		assertNoIssues()

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
		""", Runlevel.VARIABLES)
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
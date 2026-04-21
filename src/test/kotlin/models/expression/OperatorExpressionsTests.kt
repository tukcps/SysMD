package models.expression

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.implementation.FeatureReferenceExpressionImplementation
import com.github.tukcps.sysmd.model.expression.implementation.LiteralBooleanImplementation
import com.github.tukcps.sysmd.model.expression.implementation.LiteralIntegerImplementation
import com.github.tukcps.sysmd.model.expression.implementation.OperatorExpressionImplementation
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.IDD
import org.junit.jupiter.api.Disabled
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class OperatorExpressionsTests {

    @Test @Ignore
    fun operatorExpressionOperatorTypeResolutionTest() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Boolean = false;
                feature b: ScalarValues::Boolean = true;
                feature c: ScalarValues::Boolean = a and b;
            """)

            val orExpression = OperatorExpressionImplementation(
                declaredName = null,
                declaredShortName = null,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                "false or true",
                elementType = "OperatorExpression"
            )
            orExpression.model = this
            orExpression.operator = "Max" //Name Wrong

            val tst = null // orExpression.instantiatedType()
            assertNotNull(tst, "Operator could not be resolved")
    }

    @Test
    fun literalBooleanUnaryOperatorTest() {
        testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Boolean = false;
                feature b: ScalarValues::Boolean = true;
                feature c: ScalarValues::Boolean = a and b;
            """)

            val a = global.resolveVar("a")!!
            val b = global.resolveVar("b")!!
            val c = global.resolveVar("c")!!

            val falseLiteral = LiteralBooleanImplementation(
                declaredName = null,
                declaredShortName = null,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                "false",
                elementType = "LiteralBoolean"
            )
            falseLiteral.value = false
            falseLiteral.internalValue = a.ast!!.leaves.first()

            val trueLiteral = LiteralBooleanImplementation(
                declaredName = null,
                declaredShortName = null,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                "true",
                elementType = "LiteralBoolean"
            )
            trueLiteral.value = true
            trueLiteral.internalValue = b.ast!!.leaves.first()

            //TODO: Invoke unary not, execute it, test result
            val operatorExpr = OperatorExpressionImplementation(
                declaredName = null,
                declaredShortName = null,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                expression = "falseLiteral and trueLiteral",
                elementType = "OperatorExpression"
            )
            operatorExpr.operator = "and"
            operatorExpr.internalValue = c.ast
            val breakpoint = true
        }
    }

    @Test
    fun literalIntegerUnaryOperatorTest() {
        //TODO()
        val oneIntLiteral = LiteralIntegerImplementation(
            declaredName = null,
            declaredShortName = null,
            typeConstraint = mutableListOf("ScalarValues::Integer"),
            "1",
            elementType = "LiteralInteger"
        )
        oneIntLiteral.value = 1

        //TODO: Unary - should result in -1
    }

    fun literalBooleanBinaryOperatorTest() {
        //TODO()
    }

    fun nestedBooleanBinaryOperatorTest() {
        //TODO()
    }

    @Test
    fun literalInfinityOperationsTest() {
        //TODO()
    }

	@Test
	fun additionResolutionTest()  = testSession("DataFunctions") {
		val tt = twoPlusTwo()
		tt.initType()
		assertNoIssues()
		val f = assertNotNull(tt.function)
		assertEquals("IntegerFunctions::+", f.qualifiedName)
		assertFalse(f.isAbstract)
	}

	@Test
	fun evalTest() = testSession("DataFunctions") {
		val tt = twoPlusTwo()
		tt.initType()
		assertNoIssues()
		tt.initialize()
		tt.evalUp()
		tt.evalDown()

		assertEquals(VectorQuantity(builder.integer(4)), tt.upQuantity)
	}

    @Test
    fun printTest() = testSession("DataFunctions") {
		fun sum() = operatorExpression("+", "addition",
            literalExpression(2),
            literalExpression(3)
        )
        val right = operatorExpression("*", "right-mul",
            sum(),
            literalExpression(4)
        )
        val left = operatorExpression("*", "left-mul",
            literalExpression(1),
            sum()
        )
        val both = operatorExpression("*", "mul#1",
            literalExpression(1),
            operatorExpression("*", "mul#2",
                sum(),
                literalExpression(4),
            )
        )

        // test that precedence is properly parenthesized
        assertEquals("2 + 3", sum().astString)
        assertEquals("(2 + 3) * 4", right.astString)
        assertEquals("1 * (2 + 3)", left.astString)
        assertEquals("1 * (2 + 3) * 4", both.astString)
    }

    @Test
    fun printTest2() = testSession("DataFunctions") {
        // test that non-commutative operators are properly parenthesized
		fun inner() = operatorExpression("-", "inner",
            literalExpression(2),
            literalExpression(3),
        )
        val rightAssociating = operatorExpression("-", "right-assoc",
            literalExpression(1),
            inner()
        )
        val leftAssociating = operatorExpression("-", "left-assoc",
            inner(),
            literalExpression(4)
        )
        assertNoIssues()

        assertEquals("2 - 3", inner().astString)
        assertEquals("1 - (2 - 3)", rightAssociating.astString)
        assertEquals("2 - 3 - 4", leftAssociating.astString)
    }

	@Test
	fun printTest3() = testSession("DataFunctions") {
		val expr = operatorExpression("-", operatorExpression("if",
			operatorExpression("not", featureReferenceExpression("a", false)),
				operatorExpression("+",
					operatorExpression("*",
						literalExpression(7),
						featureReferenceExpression("b", false)
					),
				),
				literalExpression(8),
			))

		assertEquals("-(if not a ? +(7 * b) else 8)", expr.astString)
	}

	@Test
	fun printTest4() = testSession("DataFunctions") {
		val left = operatorExpression("^",
			operatorExpression("^", literalExpression(1), literalExpression(2)),
			literalExpression(3),
		)
		val right = operatorExpression("^",
			literalExpression(1),
			operatorExpression("^", literalExpression(2), literalExpression(3)),
		)

		assertEquals("1^2^3", right.astString)
		assertEquals("(1^2)^3", left.astString)
	}

    @Test
    fun bigSum() = testSession("DataFunctions") {
        var total : Expression = literalExpression(1)
		val n = 10

        for(i in 2..n)
            total = operatorExpression("+", total, literalExpression(i))

		assertEquals((1..n).joinToString(" + "), total.astString)
    }

	fun assertQuantityEquals(want : Long, q : VectorQuantity)
	{
		val r = assertIs<IDD>(q.values.single()).getRange()

		assertEquals(r.min, r.max, "Expected a point range")
		assertEquals(want, r.min)
	}

	fun assertExpressionEquals(want : Long, of : Expression)
	{
		assertQuantityEquals(want, of.upQuantity)
		assertQuantityEquals(want, of.downQuantity)
	}

	@Test
	fun propagation() = testSession("DataFunctions") {
		loadKerML("""
			feature x : ScalarValues::Integer;
		""")
		assertNoIssues()

		assertNotNull(global.resolve("x")).also {
			assertEquals(global, it.owningNamespace)
		}

		val two = literalExpression(2)
		val xRef = featureReferenceExpression("x")
		// TODO: Spec
		val sum = operatorExpression("+", two, xRef)
		val four = VectorQuantity(builder.integer(4L))

		addOwnedMember(sum, global)
		assertEquals(global, sum.owningNamespace)

		/*fun quantities()
		{
			println("up: ${two.upQuantity} + ${xRef.upQuantity} = ${sum.upQuantity}")
			println("down: ${two.downQuantity} + ${xRef.downQuantity} = ${sum.downQuantity}")
		}*/

		sum.initialize()

		repeat(2) {
			//println("EVAL DOWN")
			sum.downQuantity = four
			//sum.upQuantity = four
			sum.evalDownRec()
			//quantities()

			//println("EVAL UP")
			sum.downQuantity = four
			//sum.upQuantity = four
			sum.evalUpRec()
			//quantities()

		}

		assertExpressionEquals(2, two)
		assertExpressionEquals(2, xRef)
		assertExpressionEquals(4, sum)
	}

	@Test
	fun complexPropagation() = testSession("DataFunctions") {
		loadKerML("""
			feature x : ScalarValues::Integer;
		""".trimIndent())

		assertNoIssues()

		// 5x + 7y = 2  (x,y in Z)
		val five = literalExpression(5)
		val x = FeatureReferenceExpressionImplementation("&x", "&x").apply {
			model = this@testSession
			referent = global.resolve("x")!!.member()!!
		}
		val seven = literalExpression(7)
		val y = /*FeatureReferenceExpressionImplementation("&y", "&y").apply {
			identifier = "y"
		}*/ literalExpression(1)


		assertSame(global.resolve("x")?.member(), x.referent)

		val mulL = operatorExpression("*", "left mul", five, x)
		val mulR = operatorExpression("*", "right mul", seven, y)
		val sum = operatorExpression("+", "sum", mulL, mulR)

		addOwnedMember(sum, global)
		assertEquals(global, sum.owningNamespace)
		sum.initialize()

		/*fun quantities()
		{
			println("up: ${five.upQuantity}·${x.upQuantity} = ${mulL.upQuantity};    " +
					"${seven.upQuantity}·${y.upQuantity} = ${mulR.upQuantity};    " +
					"${mulL.upQuantity}+${mulR.upQuantity} = ${sum.upQuantity}")

			println("down: ${five.downQuantity}·${x.downQuantity} = ${mulL.downQuantity};    " +
					"${seven.downQuantity}·${y.downQuantity} = ${mulR.downQuantity};    " +
					"${mulL.downQuantity}+${mulR.downQuantity} = ${sum.downQuantity}")
		}*/

		//quantities()
		val two = VectorQuantity(builder.integer(2L))

		repeat(2) {
			sum.downQuantity = two
			sum.evalDownRec()
			//println("EVAL DOWN")
			//quantities()

			sum.downQuantity = two
			sum.evalUpRec()
			//println("EVAL UP")
			//quantities()
		}

		assertExpressionEquals(5, five)
		assertExpressionEquals(-1, x)
		assertExpressionEquals(7, seven)
		assertExpressionEquals(1, y)
		assertExpressionEquals(-5, mulL)
		assertExpressionEquals(7, mulR)
		assertExpressionEquals(2, sum)

		assertQuantityEquals(-1, global.resolveVar("x")!!.vectorQuantity)
	}


	@Test
	fun unaryMinus() = testSession("DataFunctions") {
		val expr = operatorExpression("-", literalExpression(42))

		expr.initialize()
		expr.evalUpRec()
		expr.evalDownRec()

		assertEquals(VectorQuantity(builder.integer(-42)), expr.upQuantity)
		println(expr.astString)
	}

	@Test
	fun unaryPlus() = testSession("DataFunctions") {
		val expr = operatorExpression("+", literalExpression(29))

		expr.initialize()
		expr.evalUpRec()
		expr.evalDownRec()

		assertEquals(VectorQuantity(builder.integer(29)), expr.upQuantity)
	}

	@Test
	fun booleanNegation() = testSession("DataFunctions") {
		val expr = operatorExpression("not", literalExpression(true))
		expr.initialize()
		expr.evalUpRec()
		expr.evalDownRec()

		assertEquals(VectorQuantity(builder.False), expr.upQuantity)
	}
}
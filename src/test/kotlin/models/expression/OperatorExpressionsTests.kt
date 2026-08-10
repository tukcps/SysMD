package models.expression

import com.github.tukcps.sysmd.model.expression.Expression
import com.github.tukcps.sysmd.model.expression.FeatureReferenceExpression
import com.github.tukcps.sysmd.model.expression.LiteralInteger
import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.expression.implementation.FeatureReferenceExpressionImplementation
import com.github.tukcps.sysmd.model.expression.implementation.OperatorExpressionImplementation
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.IDD
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class OperatorExpressionsTests {

	fun OperatorExpression.binary() : Pair<Expression, Expression>
	{
		val pos = positionalArguments
		assertEquals(2, pos.size, "$this is not binary")
		val (x,y) =  pos.map { assertIs<Expression>(it) }

		return Pair(x,y)
	}

    @Test @Ignore
    fun operatorExpressionOperatorTypeResolutionTest() = testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Boolean = false;
                feature b: ScalarValues::Boolean = true;
                feature c: ScalarValues::Boolean = a and b;
            """)

            val orExpression = OperatorExpressionImplementation(this,
                expression = "false or true"
            ).also {
				// it.typeConstraint = mutableListOf("ScalarValues::Boolean")
			}
            orExpression.operator = "Max" //Name Wrong

            val tst = null // orExpression.instantiatedType()
            assertNotNull(tst, "Operator could not be resolved")
    }

	/*
    @Test
    fun literalBooleanUnaryOperatorTest() {
        testSession("ScalarValues") {
            loadKerML("""
                feature a: ScalarValues::Boolean = false;
                feature b: ScalarValues::Boolean = true;
                feature c: ScalarValues::Boolean = a and b;
            """, Runlevel.ALL)

            val a = solver.getVariable("a")!!
            val b = solver.getVariable("b")!!
            val c = solver.getVariable("c")!!

            val falseLiteral = LiteralBooleanImplementation(
                declaredName = null,
                declaredShortName = null,
                "false",
                elementType = "LiteralBoolean"
            ).also {
				it.typeConstraint = mutableListOf("ScalarValues::Boolean")
			}
            falseLiteral.value = false
            falseLiteral.internalValue = a.ast!!.leaves.first()

            val trueLiteral = LiteralBooleanImplementation(
                declaredName = null,
                declaredShortName = null,
                "true",
                elementType = "LiteralBoolean"
            ).also {
				it.typeConstraint = mutableListOf("ScalarValues::Boolean")
			}
            trueLiteral.value = true
            trueLiteral.internalValue = b.ast!!.leaves.first()

            //TODO: Invoke unary not, execute it, test result
            val operatorExpr = OperatorExpressionImplementation(
                declaredName = null,
                declaredShortName = null,
                expression = "falseLiteral and trueLiteral",
                elementType = "OperatorExpression"
            ).also {
				it.typeConstraint = mutableListOf("ScalarValues::Boolean")
			}
            operatorExpr.operator = "and"
            operatorExpr.internalValue = c.ast
            val breakpoint = true
        }
    }

	 */

	@Test
	fun additionResolutionTest()  = testSession("DataFunctions") {
		val tt = parseExpr("2+2")
		tt.initType()
		assertNoIssues()
		val f = assertNotNull(tt.function)
		assertEquals("IntegerFunctions::+", f.qualifiedName)
		assertFalse(f.isAbstract)
	}

	@Test
	fun evalTest() = testSession("DataFunctions") {
		val tt = parseExpr("2+2")
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

		assertEquals(r.min, r.max, "Expected a point range of value $want")
		assertEquals(want, r.min)
	}

	fun assertExpressionEquals(want : Long, of : Expression)
	{
		// assertQuantityEquals(want, of.upQuantity)
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

		val sum = parseExpr("2 + x")
		assertIs<OperatorExpression>(sum)

		val (two,xRef) = sum.binary()

		val four = VectorQuantity(builder.integer(4L))

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

	@Test @Ignore // fixme: something broke propagation with new invocations, check after release
	fun complexPropagation() = testSession("DataFunctions") {
		loadKerML("""
			feature x : ScalarValues::Integer;
			feature y : ScalarValues::Integer;
		""", Runlevel.VARIANCE_CHECKED)

		assertNoIssues()

		val sum = parseExpr("5*x + 7*y")


		assertIs<OperatorExpression>(sum)
		val (mulL, mulR) = sum.binary()
		assertIs<OperatorExpression>(mulL)
		assertIs<OperatorExpression>(mulR)

		val (five,x) = mulL.binary()
		assertIs<LiteralInteger>(five)
		assertIs<FeatureReferenceExpression>(x)

		val (seven,y) = mulR.binary()
		assertIs<LiteralInteger>(seven)
		assertIs<FeatureReferenceExpression>(y)


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

		sum.initialize()

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

		// fixme somehow somewhere the propagation pipeline broke
		assertExpressionEquals(5, five)
		assertExpressionEquals(-1, x)
		assertExpressionEquals(7, seven)
		assertExpressionEquals(1, y)
		assertExpressionEquals(-5, mulL)
		assertExpressionEquals(7, mulR)
		assertExpressionEquals(2, sum)

		assertQuantityEquals(-1, solver.getVariable("x")!!.vectorQuantity)
	}


	@Test
	fun unaryMinus() = testSession("DataFunctions") {
		val expr = parseExpr("-42")

		expr.initialize()
		expr.evalUpRec()
		expr.evalDownRec()

		assertEquals(VectorQuantity(builder.integer(-42)), expr.upQuantity)
		println(expr.astString)
	}

	@Test
	fun unaryPlus() = testSession("DataFunctions") {
		val expr = parseExpr("+29")

		expr.initialize()
		expr.evalUpRec()
		expr.evalDownRec()

		assertEquals(VectorQuantity(builder.integer(29)), expr.upQuantity)
	}

	@Test
	fun booleanNegation() = testSession("DataFunctions") {
		val expr = parseExpr("not true")
		expr.initialize()
		expr.evalUpRec()
		expr.evalDownRec()

		assertEquals(VectorQuantity(builder.False), expr.upQuantity)
	}
}
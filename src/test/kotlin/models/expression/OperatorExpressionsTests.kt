package models.expression

import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.LiteralBoolean
import com.github.tukcps.sysmd.model.expression.OperatorExpression
import com.github.tukcps.sysmd.model.expression.implementation.LiteralBooleanImplementation
import com.github.tukcps.sysmd.model.expression.implementation.LiteralIntegerImplementation
import com.github.tukcps.sysmd.model.expression.implementation.OperatorExpressionImplementation
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.DDBuilder
import org.junit.jupiter.api.Disabled
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class OperatorExpressionsTests {

    @Test @Disabled
    fun operatorExpressionOperatorTypeResolutionTest() {
        val testSession = testSession("ScalarValues") {
            loadKerML(
                """
                feature a: ScalarValues::Boolean = false;
                feature b: ScalarValues::Boolean = true;
                feature c: ScalarValues::Boolean = a and b;
            """.trimIndent()
            )

            val orExpression = OperatorExpressionImplementation(
                declaredName = null,
                declaredShortName = null,
                direction = Feature.FeatureDirectionKind.IN,
                isEnd = false,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                "false or true",
                textualRepresentation = mutableListOf(), //FIXME?
                elementType = "OperaorExpression"
            )
            orExpression.model = this
            orExpression.operator = "Max" //Name Wrong

            val tst = orExpression.instantiatedType()
            assertNotNull(tst, "Operator could not be resolved")
        }
    }

    @Test
    fun literalBooleanUnaryOperatorTest() {
        //TODO()
        val testsession = testSession("ScalarValues") {loadKerML("""
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
                direction = Feature.FeatureDirectionKind.IN,
                isEnd = false,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                "false",
                textualRepresentation = mutableListOf(), //FIXME?
                elementType = "LiteralBoolean"
            )
            falseLiteral.value = false
            falseLiteral.internalValue = a.ast!!.leaves.first()

            val trueLiteral = LiteralBooleanImplementation(
                declaredName = null,
                declaredShortName = null,
                direction = Feature.FeatureDirectionKind.IN,
                isEnd = false,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                "true",
                textualRepresentation = mutableListOf(), //FIXME?
                elementType = "LiteralBoolean"
            )
            trueLiteral.value = true
            trueLiteral.internalValue = b.ast!!.leaves.first()

            //TODO: Invoke unary not, execute it, test result
            val operatorExpr = OperatorExpressionImplementation(
                declaredName = null,
                declaredShortName = null,
                direction = Feature.FeatureDirectionKind.IN, //or inout?
                isEnd = false,
                typeConstraint = mutableListOf("ScalarValues::Boolean"),
                expression = "falseLiteral and trueLiteral",
                textualRepresentation = mutableListOf(),
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
            direction = Feature.FeatureDirectionKind.IN,
            isEnd = false,
            typeConstraint = mutableListOf("ScalarValues::Integer"),
            "1",
            textualRepresentation = mutableListOf(), //FIXME?
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
}
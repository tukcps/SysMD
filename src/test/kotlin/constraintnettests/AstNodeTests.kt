package constraintnettests

import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.model.expression.AstBinOp
import com.github.tukcps.sysmd.model.expression.AstLeaf
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.AstUnaryOp
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.dd.DD
import util.assertBounds
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Unit tests on the AST nodes, building the trees directly. */
class AstNodeTests {

    private fun Session.leaf(dd: DD<*>) = AstLeaf(this, VectorQuantity(dd))
    private fun Session.binOp(l: AstNode, op: com.github.tukcps.sysmd.compiler.scanner.Token.Kind, r: AstNode) =
        AstBinOp(l, op, r).also { it.initialize() }

    private fun AstNode.downBdd(): BDD = downQuantity.values[0].asBdd()

    // ---- AND ----

    @Test
    fun andTrueForcesBothOperandsTrue() = testSession {
        val b = builder.Bool
        val node = binOp(leaf(b.All), AND, leaf(b.All))
        node.downQuantity = VectorQuantity(b.True)
        node.evalDown()
        assertEquals(b.True, node.l.downBdd())
        assertEquals(b.True, node.r.downBdd())
    }

    @Test
    fun andFalseWithTrueOperandForcesOtherFalse() = testSession {
        val b = builder.Bool
        val node = binOp(leaf(b.All), AND, leaf(b.True))
        node.downQuantity = VectorQuantity(b.False)
        node.evalDown()
        assertEquals(b.False, node.l.downBdd())
    }

    @Test
    fun andFalseWithFalseOperandLeavesOtherOpen() = testSession {
        val b = builder.Bool
        val node = binOp(leaf(b.All), AND, leaf(b.False))
        node.downQuantity = VectorQuantity(b.False)
        node.evalDown()
        assertEquals(b.All, node.l.downBdd())
    }

    // ---- OR ----

    @Test
    fun orFalseForcesBothOperandsFalse() = testSession {
        val b = builder.Bool
        val node = binOp(leaf(b.All), OR, leaf(b.All))
        node.downQuantity = VectorQuantity(b.False)
        node.evalDown()
        assertEquals(b.False, node.l.downBdd())
        assertEquals(b.False, node.r.downBdd())
    }

    @Test
    fun orTrueWithFalseOperandForcesOtherTrue() = testSession {
        val b = builder.Bool
        val node = binOp(leaf(b.False), OR, leaf(b.All))
        node.downQuantity = VectorQuantity(b.True)
        node.evalDown()
        assertEquals(b.True, node.r.downBdd())
    }

    @Test
    fun orTrueWithTrueOperandLeavesOtherOpen() = testSession {
        val b = builder.Bool
        val node = binOp(leaf(b.True), OR, leaf(b.All))
        node.downQuantity = VectorQuantity(b.True)
        node.evalDown()
        assertEquals(b.All, node.r.downBdd())
    }

    // ---- EE on integers ----

    @Test
    fun integerEqualityTrueIntersectsOperands() = testSession {
        val node = binOp(leaf(builder.integer(1L..5L)), EE, leaf(builder.integer(3L..8L)))
        node.downQuantity = VectorQuantity(builder.Bool.True)
        node.evalDown()
        assertBounds(3L..5L, node.l.downQuantity)
        assertBounds(3L..5L, node.r.downQuantity)
    }

    @Test
    fun integerEqualityFalseKeepsOperands() = testSession {
        val node = binOp(leaf(builder.integer(1L..5L)), EE, leaf(builder.integer(3L..8L)))
        node.downQuantity = VectorQuantity(builder.Bool.False)
        node.evalDown()
        assertBounds(1L..5L, node.l.downQuantity)
        assertBounds(3L..8L, node.r.downQuantity)
    }

    // ---- Unary operations ----

    @Test
    fun unaryCloneKeepsOperatorAndType() = testSession {
        val minus = AstUnaryOp(MINUS, leaf(builder.real(2.0))).also { it.initialize() }
        val clone = minus.clone()
        assertIs<AstUnaryOp>(clone)
        assertEquals(MINUS, clone.op)
        clone.initialize()
        assertBounds(-2.0..-2.0, clone.upQuantity)

        val plus = AstUnaryOp(PLUS, leaf(builder.real(2.0))).also { it.initialize() }
        assertEquals(PLUS, plus.clone().op)
    }

    @Test
    fun unaryExpressionStringShowsSign() = testSession {
        assertTrue(AstUnaryOp(MINUS, leaf(builder.real(2.0))).toExpressionString().startsWith("-"))
        assertTrue(AstUnaryOp(PLUS, leaf(builder.real(2.0))).toExpressionString().startsWith("+"))
    }

    @Test
    fun unaryOperandKnowsItsParent() = testSession {
        val operand = leaf(builder.real(2.0))
        val node = AstUnaryOp(MINUS, operand)
        assertEquals(node, operand.parent)
    }

    // ---- Leaves and traversal ----

    @Test
    fun literalLeafExpressionStringHasNoListBrackets() = testSession {
        val s = leaf(builder.real(3.0)).toExpressionString()
        assertFalse(s.contains("["), s)
        assertFalse(s.contains("]"), s)
    }

    @Test
    fun depthFirstTraversalVisitsEveryNodeOnce() = testSession {
        val node = binOp(leaf(builder.real(1.0)), PLUS, AstUnaryOp(MINUS, leaf(builder.real(2.0))).also { it.initialize() })
        var count = 0
        node.withDepthFirst(node) { count++ }
        assertEquals(4, count)
        assertEquals(2, node.getLeaves().size)
    }
}

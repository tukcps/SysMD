package compiler.kerml.examples

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.OwnedExpression
import com.github.tukcps.sysmd.compiler.parser.util.DataModelWithEdges
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.expression.InvocationExpression
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.Runlevel
import org.junit.jupiter.api.Assertions.assertEquals
import util.*
import kotlin.test.*

class ExpressionTests
{
    /** @param body Receives the singular expression that was parsed */
    private inline fun parseTest(code : String, unresolvedNames : Boolean = false, body : KerML.(ElementData) -> Unit) = KerML(uuidPolicy = UuidPolicies.RandomUuids).settings {
        addImplied = false
        addConstraints = false
        addDefaultMultiplicity = false
    }.run {
        input = code
        semantics.initOwningNamespaces(null)
        val e = withUnresolvedNames(unresolvedNames) { OwnedExpression() }
        semantics.elementsBuilt.add(e)

        assertEquals(Token.Kind.EOF, token.kind,
            "Unparsed garbage after expression starting at index ${token.indices.first}"
        )

        body(e)
    }

    private val KerML.elements get() = semantics.elementsBuilt

    fun KerML.printIndentedString(from : ElementData? = null)
    {
        val m = DataModelWithEdges(elements)
        if(from === null)
            m.print(System.out)
        else
            m.print(from.elementId, System.out)
    }

    fun KerML.toIndentedString() = DataModelWithEdges(elements).toString()

    @Test
    fun lambdaSyntax() = parseTest("foo -> bar{ in x; x }") {
        assertNoIssues()
        printIndentedString()

        // InvocationExpression + TypeReference + 2*ParameterMembership
        //      + (FeatureReferenceExpression + Membership)
        //      + (glue + BodyExpression
        //          + Feature
        //          + ResultExpressionMembership + (FeatureReferenceExpression + Membership)
        //        )
        assertEquals(15, elements.size) { toIndentedString() }
        assertEquals(ElementType.InvocationExpression, it.type)
    }

    @Test
    fun fieldAccess() = parseTest("processData.massProcessed") {
        assertNoIssues()
        // printIndentedString()

        // FeatureChainExpression +  (FeatureReferenceExpression + Membership) + ParameterMembership
        assertEquals(4, elements.size) { toIndentedString() }
        assertEquals(ElementType.FeatureChainExpression, it.type)
    }

    @Test
    fun metaAccess() = parseTest("foo.metadata") {
        assertNoIssues()
        // printIndentedString()

        // MetadataAccessExpression + Membership
        assertEquals(2, elements.size) { toIndentedString() }
        assertEquals(ElementType.MetadataAccessExpression, it.type)
    }

    @Test
    fun legacyFunction() = parseTest("sumOverSubclasses(2 + x)") {
        assertNoIssues()
        // printIndentedString()

        // invocationExpression + operatorExpression + 2 operands + memberships
        assertEquals(7, elements.size) { toIndentedString() }

        assertEquals(ElementType.InvocationExpression, it.type)
    }

    @Test
    fun unresolvedFeatureChain() = parseTest("foo . bar . baz", true) {
        assertNoIssues()
        //printIndentedString()
        assertEquals(listOf(it), elements)

        assertEquals(ElementType.LiteralString, it.type)
        assertEquals(true, it.isNameLiteral)
        assertEquals("foo.bar.baz", it.literalStringValue)
    }

    @Test
    fun simpleInvocation() = parseTest("AddMember(org, who)") {
        assertNoIssues()
        // printIndentedString()

        val model = DataModelWithEdges(elements)

        assertEquals(7, elements.size) { model.toString() }
        val rels = model.edgesOwnedBy(model.get(it)).map { e -> e.to as DataModelWithEdges.ActualElement }
        assertEquals(2, rels.size, rels.toString())
        val args = rels.flatMap { r -> model.edgesOwnedBy(r) }.map { e -> e.to as DataModelWithEdges.ActualElement }

        testSession(runlevel = Runlevel.NONE) {
            import(this@parseTest.elements)
            assertNoIssues()

            val expr = get(it.elementId)!!
            assertIs<InvocationExpression>(expr)
            assertEquals(
                rels.map { r -> r.id },
                expr.ownedRelationship.map(Element::elementId),
                "import() added superfluous owned relationships"
            )
            assertEquals(
                args.map { a -> a.id },
                expr.ownedElement.map(Element::elementId),
                "import() added superfluous owned elements"
            )

            assertEquals("AddMember(org, who)", expr.astString)
        }
    }
}
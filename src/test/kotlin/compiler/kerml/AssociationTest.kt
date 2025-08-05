package compiler.kerml

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import io.github.tukcps.aadd.values.IntegerRange
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

/**
 * Test of Associations.
 * The library Links and redefinitions must work properly.
 */
class AssociationTest {

    /**
     * Associations have end features that re-define the source and target of a Link.
     * Source and target of the relationship are the related types (features are types) of source and target end.
     * The ends are NOT references.
     */
    @Test
    fun associationTest1() = testSession("Links") {
        loadKerML("""
            type A :> Base::Anything;
            type B :> Base::Anything;
            assoc rel {
                end a: A [1 .. 2] :>> source; 
                end b: B [3 .. 4] :>> target;
            }
        """)
        assertNoIssues()
        val rel = global.resolve<Association>("rel")
        val a = global.resolve<Feature>("rel::a")
        val b = global.resolve<Feature>("rel::b")
        assertNotNull(rel)
        assertNotNull(a)
        assertNotNull(b)
        assertTrue(a.isEnd)
        assertTrue(b.isEnd)
        assertTrue(a.referencedFeature == null)
        assertTrue(b.referencedFeature == null)
        assertEquals(IntegerRange(1, 2), a.multiplicity)
        assertEquals(IntegerRange(3, 4), b.multiplicity)
        assertEquals(a,rel.sourceType)
        assertEquals(b,rel.targetType.first())
    }

    @Test
    fun associationTest2() = testSession( "Links") {
        loadKerML("""
            type A :> Base::Anything; 
            type B :> Base::Anything;
            assoc rel :> Links::Link {
                end feature b: B :>> source [2 .. 3];
                end feature a: A redefines target [1..5];
            }
        """)
        val rel = global.resolve<Association>("rel")
        assertNotNull(rel)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * An association inherits from Links::BinaryLink and gets end features source, target.
     */
    @Test
    fun associationTest3() = testSession("Links") {
        val link = global.resolve<Association>("Links::BinaryLink")
        assertNotNull(link)
        assertEquals(1, link.source.size)
        assertEquals(1, link.target.size)

        loadKerML("""
            assoc a; 
        """)
        val a = global.resolve<Association>("a")
        assertNotNull(a)
        assertNotNull(a.sourceType)
        assertNotNull(a.targetType)
        assertTrue(link in a.generalization)
    }

    /**
     * associations inherit from Link
     */
    @Test
    fun relationshipDefinitionInheritsLink() = testSession( "Links") {
        loadKerML("""
            assoc rel :> Links::Link; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val rel = global.resolve<Association>("rel")
        val link = global.resolve<Element>("Links::Link")
        assertNotNull(rel)
        assertNotNull(link)
        val source = global.resolve<Feature>("rel::source")
        assertNotNull(source)
        val target = global.resolve<Feature>("rel::target")
        assertNotNull(target)
        assertTrue(link in rel.generalization)
    }

    @Test fun associationTest() = testSession {
        loadKerML("""
            package ScalarValues { datatype Natural :> Base::Anything; datatype Integer :> Base::Anything; }
            assoc Link specializes Base::Anything {
                end feature source: Base::Anything [1..*];
                end feature target: Base::Anything [1..*];
            }
        """)
        assertNoIssues()
    }

    @Test fun associationTestWithRedefinition() = testSession {
        loadKerML("""
            package ScalarValues { datatype Natural :> Base::Anything;  datatype Integer :> Base::Anything; }
            assoc Link specializes Base::Anything {
                end feature source: Base::Anything [1..*];
                end feature target: Base::Anything [1..*];
            }
            assoc LinkRedef specializes Link {
                feature x; 
                end feature xx : Base::Anything [2..*] redefines source;
                end feature yy : Base::Anything [2..*] redefines target;
            }
        """)
        assertNoIssues()
        val linkRedef = global.resolve<Association>("LinkRedef")
        assertNotNull(linkRedef)
    }
}
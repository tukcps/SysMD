package compiler.kerml.core

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.model.datamodel.IdentifiedByName
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.services.check.checkOwnership
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class ClassifierTests {

    /**
     * Basic test that element data is correctly parsed in simple context of root namespace.
     */
    @Test
    fun parseClassifierTest() {
        val compiler  = KerML().settings {
                addImplied = false
                addDefaultMultiplicity = false
                addConstraints  = false }

        val elements = compiler.parse("classifier < shortName > longName;")
        val nr = if (compiler.settings.includeOwningRelationshipsToRoot) 2 else 1

        assertEquals(nr, elements.size) // classifier + "global owns classifier"
        val classifier = elements.single { it.type == ElementType.Classifier }
        // Identification OK?
        assertEquals("shortName", classifier.declaredShortName)
        assertEquals("longName", classifier.declaredName)
        // owningMembership OK?
        if ( compiler.settings.includeOwningRelationshipsToRoot )
            elements.single { it.type == ElementType.OwningMembership }
        else
            assertNull(classifier.owningRelationship?.id)
    }


    /**
     * Basic test that element data is correctly parsed in simple context of root namespace.
     */
    @Test
    fun parseClassifierTest2() {
        val compiler = KerML()
            .settings {
                addImplied = true
                addDefaultMultiplicity = false
                addConstraints  = false }
        val elements = compiler.parse("""
                classifier < shortName > longName :> classifier1;
             """)
        val nr = if (compiler.settings.includeOwningRelationshipsToRoot) 3 else 2
        assertEquals(nr, elements.size)      // Only the classifier, specialization, maybe owning membership root
        val classifier = elements.single { it.type == ElementType.Classifier }
        val subclassifier = elements.single { it.type == ElementType.Subclassification }
        // Identification OK?
        assertEquals("shortName", classifier.declaredShortName)
        assertEquals("longName", classifier.declaredName)
        // subclassifier OK?
        assertEquals("classifier1", (subclassifier.target.first() as IdentifiedByName).name)
        // owningMembership OK?
        if ( compiler.settings.includeOwningRelationshipsToRoot ) {
            assertNotNull(classifier.owningRelationship?.id)
            assertNotNull(elements.singleOrNull {it.type == ElementType.OwningMembership})
        } else
            assertNull(classifier.owningRelationship?.id)
    }

    @Test
    fun classifierTest() = testSession {
        loadKerML("""
            abstract classifier a; 
            classifier b :> a;
            classifier c :> a, b;
        """)
        checkOwnership()
        assertNoIssues()
        val a = global.resolve("a")?.member<Classifier>()
        assertEquals(a?.isAbstract, true)
        val b = global.resolve("b")?.member<Classifier>()
        assertNotNull(b)
        val c = global.resolve("c")?.member<Classifier>()
        assertNotNull(c)
        assertTrue(c.specializes(global.resolve("b")?.member<Classifier>()))
        assertTrue(c.specializes(global.resolve("a")?.member<Classifier>()))
    }
}
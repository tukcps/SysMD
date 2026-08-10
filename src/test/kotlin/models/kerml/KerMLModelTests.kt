package models.kerml

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.getOwnedElement
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.UnresolvedType
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.findRelationshipsFrom
import com.github.tukcps.sysmd.services.findRelationshipsTo
import com.github.tukcps.sysmd.services.initialize
import util.mockup.loadKerML
import util.testProjectSession
import util.testSession
import kotlin.test.*

class KerMLModelTests {

    /**
     * The predefined elements are
     * - Base::Anything (root of the type tree)
     * - Global (root of the ownership tree)
     */
    @Test
    fun predefinedAnyGlobalPackageTest() = testSession {
        initialize(Runlevel.NAMES_RESOLVED)
        assertNotNull(get(global.elementId))
        assertNotNull(get(repo.anything?.elementId!!))
        assertNotNull(global.resolve("Base::Anything")?.member<Classifier>())
    }

    /**
     * 1. A missing id is completed when a package is created with a given name.
     * 2. The package is added to the repository of Elements.
     */
    @Test
    fun createPackageTest() = testSession {
            val pkg = PackageImplementation(this, declaredShortName="name")
            val pkgId = addOwnedMember(pkg, global).elementId

            // 1.
            assertNotNull(pkgId)

            // 2.
            assertEquals("name", this[pkgId]?.declaredShortName)
    }


    @Test fun createPackageInPackageTest() = testSession {
            val pkg = PackageImplementation(this, declaredName="name")
            val pkg2 = addOwnedMember(pkg, global)
            assertNotNull(pkg2)

            val elem = PackageImplementation(this, declaredName="name2")
            val elemUId = addOwnedMember(elem, pkg2)
            assertNotNull( elemUId.elementId )
    }

    /**
     * Check the delete function of the model.
     */
    @Test fun deleteElementTest() = testProjectSession {
        initialize(Runlevel.NAMES_RESOLVED)
        val pkg = PackageImplementation(this, declaredName="pkg")
        val pkgCreated = addOwnedMember(pkg, global)

        val elem = addOwnedMember(TypeImplementation(this, declaredName="elem1"), pkgCreated)
        addOwnedRelationship(SpecializationImplementation(this, specific = elem, general = repo.anything!!))

        // Owned element to be removed as well
        val elem2 = FeatureImplementation(this, declaredName="elem2")
        addOwnedMember(elem2, elem)

        checkConsistencyOfBuilders()
        checkConsistency(repo, global.elementId)
        delete(elem)
        checkConsistencyOfBuilders()
        checkConsistency(repo, global.elementId)
        assertNull( this[elem.elementId] )
        assertNull( this[elem2.elementId] )
    }

    /**
     * create adds Element to both has-a and is-a relationship hierarchies.
     */
    @Test fun hasATest() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val class1 = addOwnedMember(TypeImplementation(this, declaredName="name"), global)     // new class or package in global.
        addOwnedRelationship(SpecializationImplementation(this, specific = class1, general = repo.anything!!), class1)
        val class2inClass1 = addOwnedMember(TypeImplementation(this, declaredName="name2"), class1)
        addOwnedRelationship(SpecializationImplementation(this, specific = class2inClass1, general = repo.anything!!), class2inClass1)      // class in name package/element
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Element>().size)
        assertEquals(2, class1.getOwnedElementsOfType<Element>().size)
        assertEquals(1, class2inClass1.getOwnedElementsOfType<Element>().size)
        assertEquals("name", global.getOwnedElement("name")?.declaredName)
        assertEquals("name2", class1.getOwnedElement("name2")?.declaredName)
        assertEquals("name2", class1.resolve("name2")?.memberElement?.declaredName)
    }


    @Test
    fun createFeatureTwice() = testSession {
        val p = FeatureImplementation(this, declaredName = "name")
        val pCreated = addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(this, specific = pCreated, general = UnresolvedType(this, "ScalarValues::Real")), pCreated)
        val p2 = FeatureImplementation(this, declaredName = "name")
        val p2Created = addOwnedMember(p2, global)
        addOwnedRelationship(SpecializationImplementation(this, specific = p2Created, general = UnresolvedType(this, "ScalarValues::Real")), p2Created)
        assertEquals(1, status.updatedValues.size)
    }


    /**
     * A second owned element with the same id in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwiceId() = testSession {
        val pkg = addOwnedMember(PackageImplementation(this, declaredName = "pkg"), global)
        pkg.updated = false
        val pkg2 = PackageImplementation(this, elementId = pkg.elementId, declaredName = "pkg2")
        addOwnedMember(pkg2, global)
        assertEquals(pkg, repo[pkg.elementId] )
    }

    /**
     * A second owned element with the same id in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwiceName() = testSession {
        val pkg1 = addOwnedMember(PackageImplementation(this, declaredName = "pkg"), global)
        val pkg2 = addOwnedMember(PackageImplementation(this, declaredName = "pkg"), global)
        assertEquals(pkg1, pkg2 )
    }


    /**
     * A second owned element with the same name in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwiceWithUpdatedShortName() = testSession {
        val pkg = addOwnedMember(PackageImplementation(this, declaredName = "Pkg"), global)
        val pkg2 = PackageImplementation(this, declaredName = "Pkg")
        pkg2.declaredShortName = "test"
        val updated = addOwnedMember(pkg2, global)
        assertEquals(pkg, updated)
        assertEquals("test", pkg.declaredShortName)
        assertEquals(1, status.updatedValues.size)
    }

    /** A second class with the same name in the same element is not allowed. */
    @Test
    fun defineClassTwice() = testSession {
        loadKerML("package X;")
        loadKerML("X hasA class B :> Base::Anything.")
        loadKerML("X hasA class A :> B.")
        val x = global.resolve("X")?.memberElement
        val no = x!!.ownedElement.size
        loadKerML("X hasA class A :> B.")
        val no2 = x.ownedElement.size
        assertEquals(no, no2)
    }


    /** A name can only be used once in a namespace, otherwise create will warn. */
    @Test fun defineElementTwiceWithNoChange() = testSession {
        addOwnedMember(ElementImplementation(this, declaredName="a"), global)
        addOwnedMember(ElementImplementation(this, declaredName="a"), global)
        assertEquals(1, status.updatedValues.size)
    }

    /**
     * A name can only be used once in a namespace,
     * otherwise create will update the first element, and add the element to updated elements.
     */
    @Test fun defineElementTwiceWithUpdate() = testSession {
        val elem1 = addOwnedMember(ElementImplementation(this, declaredName="a"), global)
        elem1.updated = false
        val elem2 = addOwnedMember(ElementImplementation(this, declaredName="a", declaredShortName = "short"), global)
        assertEquals(1, status.updatedValues.size)
        assertEquals(elem1, elem2)
    }

    /**
     * An id can only be used once; create will update the element name if element with the
     * same id exists.
     */
    @Test fun defineElementTwiceWithUpdateOfName() = testSession {
        val elem1 = addOwnedMember(ElementImplementation(this, declaredName="a"), global)
        elem1.updated = false
        val update = ElementImplementation(this, elementId = elem1.elementId, declaredName = "b")
        val elem2 = addOwnedMember(update, global)
        assertEquals(1, status.updatedValues.size)
        assertEquals( "b", elem1.declaredName )
        assertEquals(elem1, elem2)
    }

    /**
     * Test function to create and find a relationship.
     */
    @Test
    fun createFindRelationshipTest() = testSession {
        val a = addOwnedMember(ElementImplementation(this, declaredName="a"), global)
        val b = addOwnedMember(ElementImplementation(this, declaredName="b"), global)
        val rel = addOwnedRelationship(AnnotationImplementation(
            this, declaredName="rel", owningRelatedElement = global, annotatingElement = a, annotatedElement = b
        ), global)
        initialize(Runlevel.MODEL)
        val relsA = findRelationshipsFrom(a, "rel")
        // assertSame(createdRel, relsA.first())
        assertTrue(relsA.contains(rel))
        val relsB = findRelationshipsTo(b, "rel")
        // assertSame(createdRel, relsB.first())
        assertTrue(relsB.contains(rel))
        // Direction considered?
        val empty1 = findRelationshipsFrom(b, "rel")
        assertTrue(empty1.isEmpty())
        val empty2 = findRelationshipsTo(a, "rel")
        assertTrue(empty2.isEmpty())
    }

    /**
     * Check that createOrUpdate will update an element.
     */
    @Test fun createOrUpdateTest() = testSession {
        val initial = FeatureImplementation(this, declaredName="prop1")
        val created = addOwnedMember(initial, global)
        addOwnedRelationship(SpecializationImplementation(this, specific = created, general = repo.anything!!), created)
        val update = FeatureImplementation(this, elementId = created.elementId, declaredName="prop2")
        val updated = addOwnedMember(update, global)
        addOwnedRelationship(SpecializationImplementation(this, specific = updated, general = repo.anything!!), updated)
        initialize(Runlevel.ALL)
        assertEquals("prop2", updated.declaredName)
        assertEquals(created.elementId, updated.elementId)
    }


    /**
     * Check that 'create' will update a property and not create a new one if one with the same
     * identification exists.
     */
    @Test fun createOrUpdateTest2( ) = testSession {
        val initial = FeatureImplementation(this, declaredName="prop1")
        val created = addOwnedMember(initial, global)
        val update = FeatureImplementation(this, declaredName="prop1")
        val updated = addOwnedMember(update, global)
        assertSame(created, updated)
    }

}

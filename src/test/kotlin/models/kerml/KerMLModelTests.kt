package models.kerml

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.findRelationshipsFrom
import com.github.tukcps.sysmd.services.findRelationshipsTo
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import io.github.tukcps.aadd.values.IntegerRange
import util.mockup.loadKerML
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
        assertNotNull(get(global.elementId!!))
        assertNotNull(get(anything.elementId!!))
        assertNotNull(global.resolve<Anything>("Base::Anything"))
    }

    /**
     * 1. A missing id is completed when a package is created with a given name.
     * 2. The package is added to the repository of Elements.
     */
    @Test
    fun createPackageTest() = testSession {
            val pkg = PackageImplementation(declaredShortName="name")
            val pkgId = addOwnedMember(pkg, global).elementId

            // 1.
            assertNotNull(pkgId)

            // 2.
            assertEquals("name", this[pkgId]?.declaredShortName)
    }


    @Test fun createPackageInPackageTest() = testSession {
            val pkg = PackageImplementation(declaredName="name")
            val pkg2 = addOwnedMember(pkg, global)
            assertNotNull(pkg2)

            val elem = PackageImplementation(declaredName="name2")
            val elemUId = addOwnedMember(elem, pkg2)
            assertNotNull( elemUId.elementId )
    }

    /**
     * Check the delete function of the model.
     */
    @Test fun deleteElementTest() = testSession {
        val pkg = PackageImplementation(declaredName="pkg")
        val pkgCreated = addOwnedMember(pkg, global)

        val elem = addOwnedMember(TypeImplementation( declaredName="elem1"), pkgCreated)
        addOwnedRelationship(SpecializationImplementation(elem, anything))

        // Owned element to be removed as well
        val elem2 = FeatureImplementation(declaredName="elem2")
        addOwnedMember(elem2, elem)

        checkConsistencyOfBuilders()
        checkConsistency(repo.elements, global.elementId!!)
        delete(elem)
        checkConsistencyOfBuilders()
        checkConsistency(repo.elements, global.elementId!!)
        assertNull( this[elem.elementId!!] )
        assertNull( this[elem2.elementId!!] )
    }

    /**
     * create adds Element to both has-a and is-a relationship hierarchies.
     */
    @Test fun hasATest() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val class1 = addOwnedMember(TypeImplementation(declaredName="name"), global)     // new class or package in global.
        addOwnedRelationship(SpecializationImplementation(class1, anything), class1)
        val class2inClass1 = addOwnedMember(TypeImplementation(declaredName="name2"), class1)
        addOwnedRelationship(SpecializationImplementation(class2inClass1, anything), class2inClass1)      // class in name package/element
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Element>().size)
        assertEquals(2, class1.getOwnedElementsOfType<Element>().size)
        assertEquals(1, class2inClass1.getOwnedElementsOfType<Element>().size)
        assertEquals("name", global.getOwnedElement("name")?.declaredName)
        assertEquals("name2", class1.getOwnedElement("name2")?.declaredName)
        assertEquals("name2", class1.resolve<Element>("name2")?.declaredName)
    }


    @Test
    fun createFeatureTwice() = testSession {
        val p = FeatureImplementation(declaredName = "name")
        val pCreated = addOwnedMember(p, global)
        addOwnedRelationship(SpecializationImplementation(pCreated, UnresolvedType("ScalarValues::Real")), pCreated)
        val p2 = FeatureImplementation(declaredName = "name")
        val p2Created = addOwnedMember(p2, global)
        addOwnedRelationship(SpecializationImplementation(p2Created, UnresolvedType("ScalarValues::Real")), p2Created)
        assertEquals(1, status.updatedValues.size)
    }


    /**
     * A second owned element with the same id in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwiceId() = testSession {
        val pkg = addOwnedMember(PackageImplementation(declaredName = "pkg"), global)
        pkg.updated = false
        val pkg2 = PackageImplementation( declaredName = "pkg2").also {
            it.elementId = pkg.elementId!!
        }
        addOwnedMember(pkg2, global)
        assertEquals(pkg, repo.elements[pkg.elementId!!] )
    }

    /**
     * A second owned element with the same id in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwiceName() = testSession {
        val pkg1 = addOwnedMember(PackageImplementation(declaredName = "pkg"), global)
        val pkg2 = addOwnedMember(PackageImplementation(declaredName = "pkg"), global)
        assertEquals(pkg1, pkg2 )
    }


    /**
     * A second owned element with the same name in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwiceWithUpdatedShortName() = testSession {
        val pkg = addOwnedMember(PackageImplementation(declaredName = "Pkg"), global)
        val pkg2 = PackageImplementation(declaredName = "Pkg")
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
        val x = global.resolve<Element>("X")
        val no = x!!.ownedElement.size
        loadKerML("X hasA class A :> B.")
        val no2 = x.ownedElement.size
        assertEquals(no, no2)
    }


    /** A name can only be used once in a namespace, otherwise create will warn. */
    @Test fun defineElementTwiceWithNoChange() = testSession {
        val a1 = addOwnedMember(ElementImplementation(declaredName="a"), global)
        addOwnedMember(ElementImplementation(declaredName="a"), global)
        assertEquals(1, status.updatedValues.size)
    }

    /**
     * A name can only be used once in a namespace,
     * otherwise create will update the first element, and add the element to updated elements.
     */
    @Test fun defineElementTwiceWithUpdate() = testSession {
        val elem1 = addOwnedMember(ElementImplementation(declaredName="a"), global)
        elem1.updated = false
        val elem2 = addOwnedMember(ElementImplementation(declaredName="a", declaredShortName = "short"), global)
        assertEquals(1, status.updatedValues.size)
        assertEquals(elem1, elem2)
    }

    /**
     * An id can only be used once; create will update the element name if element with the
     * same id exists.
     */
    @Test fun defineElementTwiceWithUpdateOfName() = testSession {
        val elem1 = addOwnedMember(ElementImplementation(declaredName="a"), global)
        elem1.updated = false
        val update = ElementImplementation(elementId = elem1.elementId, declaredName = "b")
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
        val a = addOwnedMember(ElementImplementation(declaredName="a"), global)
        val b = addOwnedMember(ElementImplementation(declaredName="b"), global)
        val rel = addOwnedRelationship(AnnotationImplementation(declaredName="rel",
            owningRelatedElement = global, annotatingElement = a, annotatedElement = b
        ), global)
        initialize()
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
        val initial = FeatureImplementation(declaredName="prop1")
        val created = addOwnedMember(initial, global)
        addOwnedRelationship(SpecializationImplementation(created, anything), created)
        val update = FeatureImplementation(declaredName="prop2").also {
            it.elementId = created.elementId
        }
        val updated = addOwnedMember(update, global)
        addOwnedRelationship(SpecializationImplementation(updated, anything), updated)
        initialize()
        assertEquals("prop2", updated.declaredName)
        assertEquals(created.elementId, updated.elementId)
    }


    /**
     * Check that 'create' will update a property and not create a new one if one with the same
     * identification exists.
     */
    @Test fun createOrUpdateTest2( ) = testSession {
        settings.catchExceptions = false
        val initial = FeatureImplementation(declaredName="prop1")
        val created = addOwnedMember(initial, global)
        val update = FeatureImplementation(declaredName="prop1")
        val updated = addOwnedMember(update, global)
        assertSame(created, updated)
    }

}

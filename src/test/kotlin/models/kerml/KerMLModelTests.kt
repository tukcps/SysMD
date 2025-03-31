package models.kerml

import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.check.checkConsistency
import com.github.tukcps.sysmd.services.check.checkConsistencyOfBuilders
import com.github.tukcps.sysmd.services.findRelationshipsFrom
import com.github.tukcps.sysmd.services.findRelationshipsTo
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
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
            val pkgId = create(pkg, global).elementId

            // 1.
            assertNotNull(pkgId)

            // 2.
            assertEquals("name", this[pkgId]?.declaredShortName)
    }


    @Test fun createPackageInPackageTest() = testSession {
            val pkg = PackageImplementation(declaredName="name")
            val pkg2 = create(pkg, global)
            assertNotNull(pkg2)

            val elem = PackageImplementation(declaredName="name2")
            val elemUId = create(elem, pkg2)
            assertNotNull( elemUId.elementId )
    }

    /**
     * Check the delete function of the model.
     */
    @Test fun deleteElementTest() = testSession {
        val pkg = PackageImplementation(declaredName="packageName")
        val pkgCreated = create(pkg, global)

        val elem = create(TypeImplementation( declaredName="elementName"), pkgCreated)
        create(SpecializationImplementation(elem, anything), elem)

        var prop = FeatureImplementation(declaredName="prop")
        prop = create(prop, elem)

        delete(elem)
        checkConsistencyOfBuilders()
        checkConsistency(repo.elements, global.elementId!!)
        assertNull( this[elem.elementId!!] )
        assertNull( this[prop.elementId!!] )
    }




    /**
     * create adds Element to both has-a and is-a relationship hierarchies.
     */
    @Test fun hasATest() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val class1 = create(TypeImplementation(declaredName="name"), global)     // new class or package in global.
        create(SpecializationImplementation(class1, anything), class1)
        val class2inClass1 = create(TypeImplementation(declaredName="name2"), class1)
        create(SpecializationImplementation(class2inClass1, anything), class2inClass1)      // class in name package/element
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
        val pCreated = create(p, global)
        create(SpecializationImplementation(pCreated, "ScalarValues::Real"), pCreated)
        val p2 = FeatureImplementation(declaredName = "name")
        val p2Created = create(p2, global)
        create(SpecializationImplementation(p2Created, "ScalarValues::Real"), p2Created)
        assertEquals(1, status.updates.size)
    }


    /**
     * A second owned element with the same id in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwice() = testSession {
        val pkg = create(PackageImplementation(declaredName = "Pkg"), global)
        pkg.updated = false
        val pkg2 = PackageImplementation( declaredName = "pkg").also {
            it.elementId = pkg.elementId!!
        }
        create(pkg2, global)
        assertEquals(1, status.updates.size)
    }


    /**
     * A second owned element with the same name in the same element is not allowed.
     * It is updated with the newer version.
     */
    @Test
    fun declarePackageTwiceWithUpdatedShortName() = testSession {
        val pkg = create(PackageImplementation(declaredName = "Pkg"), global)
        val pkg2 = PackageImplementation(declaredName = "Pkg")
        pkg2.declaredShortName = "test"
        val updated = create(pkg2, global)
        assertEquals(pkg, updated)
        assertEquals("test", pkg.declaredShortName)
        assertEquals(1, status.updates.size)
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
        val elem1 = create(ElementImplementation(declaredName="a"), global)
        elem1.updated = false
        create(ElementImplementation(declaredName="a"), global)
        assertEquals(0, status.updates.size)
    }

    /**
     * A name can only be used once in a namespace,
     * otherwise create will update the first element, and add the element to updated elements.
     */
    @Test fun defineElementTwiceWithUpdate() = testSession {
        val elem1 = create(ElementImplementation(declaredName="a"), global)
        elem1.updated = false
        val elem2 = create(ElementImplementation(declaredName="a", declaredShortName = "short"), global)
        assertEquals(1, status.updates.size)
        assertEquals(elem1, elem2)
    }

    /**
     * An id can only be used once; create will update the element name if element with the
     * same id exists.
     */
    @Test fun defineElementTwiceWithUpdateOfName() = testSession {
        val elem1 = create(ElementImplementation(declaredName="a"), global)
        elem1.updated = false
        val update = ElementImplementation(elementId = elem1.elementId, declaredName = "b")
        val elem2 = create(update, global)
        assertEquals(1, status.updates.size)
        assertEquals( "b", elem1.declaredName )
        assertEquals(elem1, elem2)
    }

    /**
     * Test function to create and find a relationship.
     */
    @Test
    fun createFindRelationshipTest() = testSession {
        val a = create(ElementImplementation(declaredName="a"), global)
        val b = create(ElementImplementation(declaredName="b"), global)
        val rel = RelationshipImplementation(declaredName="rel",
            source = mutableListOf(Resolved("a")), target = mutableListOf(Resolved("b")) )
        val createdRel = create(rel, global)
        initialize()
        val relsA = findRelationshipsFrom(a, "rel")
        assertSame(createdRel, relsA.first())
        assertTrue(relsA.contains(rel))
        val relsB = findRelationshipsTo(b, "rel")
        assertSame(createdRel, relsB.first())
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
        val created = create(initial, global)
        create(SpecializationImplementation(created, anything), created)
        val update = FeatureImplementation(declaredName="prop2").also {
            it.elementId = created.elementId
        }
        val updated = create(update, global)
        create(SpecializationImplementation(updated, anything), updated)
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
        val created = create(initial, global)
        val update = FeatureImplementation(declaredName="prop1")
        val updated = create(update, global)
        assertSame(created, updated)
    }

    /**
     * Inheritance creates a copy of a feature, and as well of its information:
     * - Multiplicity
     * - Specialization
     * There also shall be no duplicates, and the inherited features shall be marked as transient.
     */
    @Test fun createInheritedExpression() = testSession("ScalarValues") {
        settings.catchExceptions = false
        loadKerML("""
            type a :> Base::Anything { feature x [1..2]; }
            type b :> a; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val ax = global.resolve<Feature>("a::x")!!
        val bx = global.resolve<Feature>("b::x")!!
        assertEquals(1, ax.ownedElement.filter { it.ref is Specialization }.size)
        assertEquals(2, ax.ownedElement.size)
        assertEquals(1, ax.ownedElement.filter { it.ref is Multiplicity }.size)
        assertEquals(IntegerRange(1,2), ax.multiplicity)

        assertEquals(1, bx.ownedElement.filter { it.ref is Multiplicity }.size)
        assertEquals(IntegerRange(1,2), bx.multiplicity)
        assertEquals(1, bx.ownedElement.filter { it.ref is Specialization }.size)
        assertEquals(2, bx.ownedElement.size)
        assertTrue(bx.getOwnedElementOfType<Multiplicity>()!!.isTransient)
        assertTrue(bx.getOwnedElementOfType<Specialization>()!!.isTransient)
        assertNotNull(get(bx.getOwnedElementOfType<Multiplicity>()!!.elementId!!))
        assertNotNull(get(bx.getOwnedElementOfType<Specialization>()!!.elementId!!))
        assertNotEquals(ax, bx)
        assertNotEquals(ax.elementId, bx.elementId)
        export()
    }
}

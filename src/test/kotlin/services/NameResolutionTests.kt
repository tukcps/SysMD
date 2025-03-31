package services

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import kotlin.test.*
import util.testSession

class NameResolutionTests {

    /**
     * Simple direct hit in global.
     */
    @Test
    fun findSimpleNameTestInGlobal() = testSession {
        create(ClassImplementation(declaredName="x"), global)
        val found = global.resolve<Element>("x")
        assertTrue(found is ClassImplementation)
        assertEquals("x", found.declaredName)
    }

    /**
     * Search in package.
     */
    @Test
    fun findSimpleNameTestInPackage1() = testSession {
        // Setting: p owns x
        val p = create(PackageImplementation(declaredName="p"), global)
        val x = create(ClassImplementation(declaredName="x"), p)

        // Search for p::x from global
        val px1 = global.resolve<Element>("p::x")
        assertEquals(x, px1)

        // Search x from p
        val px2 = p.resolve<Element>("x")
        assertEquals(x, px2)

        // search p from x; hierarchically upwards
        val pFromX = x.resolve<Element>("p")
        assertEquals(p, pFromX)
    }

    @Test
    fun findViaImport() = testSession {
        val pkg = create(PackageImplementation(declaredName="pkg"), global)
        val a = create(ClassImplementation(declaredName="a"), pkg)
        val import = create(NamespaceImportImplementation(importedNamespace = Resolved(id=null, str="pkg", ref=pkg)), global)
        val found = global.resolve<Element>("a")
        assertNotNull(found)
        assertNotNull(import)
        assertNotNull(a)
    }

    /**
     *  Search by name in imports, when the package has nested elements
     */
    @Test fun findViaImport2() =testSession {
        val test = create(PackageImplementation(declaredName = "test"), global)
        val test2 = create(PackageImplementation(declaredName = "test2"), global)
        val test1 = create(ClassImplementation(declaredName = "test1"), test)
        create(NamespaceImportImplementation(importingNamespace = Resolved(global), importedNamespace = Resolved<Namespace>(ref=test)), global)
        create(NamespaceImportImplementation(importedNamespace = Resolved(global), importingNamespace = Resolved<Namespace>(ref=test2)), global)
        val found = test2.resolve<Element>("test1")
        assertEquals(test1, found)
    }

    /**
     * Search from specialization
     */
    @Test
    fun findFromSpecialization() = testSession("ScalarValues") {
        loadKerML("""
                type a :> Base::Anything { feature X: Base::Anything; } 
                type b :> a;
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val bX = global.resolve<Element>("b::X")
        assertNotNull(bX)
    }


    @Test
    fun findPropertyInGlobal() {
        testSession {
            // A property of Global.
            val id = create(FeatureImplementation(declaredName = "test"), global).elementId
            // Three ways to get it:
            val found = global.resolve<Element>("test")
            assertNotNull(found)
            val found2 = global.resolve<Feature>("test")
            assertNotNull((found2))
            val found3 = global.resolve<Feature>("test")
            assertEquals(id, found3!!.elementId)
            assertEquals(id, found.elementId)
        }
    }

    @Test fun findPredefinedClasses()  = testSession("ScalarValues") {
        val real = global.resolve<Element>("ScalarValues::Real")
        val int = global.resolve<Element>("ScalarValues::Integer")
        val bool = global.resolve<Element>("ScalarValues::Boolean")
        val str = global.resolve<Element>("ScalarValues::String")
        assertNotNull(real)
        assertNotNull(int)
        assertNotNull(bool)
        assertNotNull(str)
    }



    /**
     * Test of the findElement function by name, directly in the same namespace.
     */
    @Test fun findHasAElementByNameDirectTest() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Type>().size
        val name = create(TypeImplementation(declaredName="name"), global) // new class or package in global.
        create(SpecializationImplementation(name, anything), name)
        val name2 = create(TypeImplementation(declaredName="name2"), name)  // class in name package/element
        create(SpecializationImplementation(name2, anything), name2)
        initialize()
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Type>().size)
        assertEquals(1, name.getOwnedElementsOfType<Type>().size)
        assertEquals(1, name2.ownedElement.size)
        assertEquals("name2", name.resolve<Element>("name2")!!.declaredName)
        assertEquals("name2", global.resolve<Element>("name::name2")!!.declaredName)
    }

    /** Test of the findElement function by name - additional test cases*/
    @Test fun findHasAElementByNameDirectTestNested() = testSession {
        val c0 = create(PackageImplementation(declaredName="test"), global)  // new class or package in global.
        val c1 = create(PackageImplementation(declaredName="test1"), c0)     // creation of a test1 element in test package/element
        create(ElementImplementation(declaredName="test2"), c1)              // creation of a test2 element in test package/element
        assertEquals("test1", global.resolve<Element>("Global::test::test1")!!.declaredName)  // test of the qualified name 'Global::test::test1'
        assertEquals("test1", global.resolve<Element>("test::test1")!!.declaredName)
        assertEquals("test2", c0.resolve<Element>("test1::test2")!!.declaredName)
    }

    /** Test of the findElement function by id **/
    @Test fun findHasAElementByIdDirectTest() = testSession {
        val obj = create(PackageImplementation(declaredName = "testID"), global) // new class or package in global.
        create(ElementImplementation(declaredName = "name2"), obj)   // creation of a test2 element in test package/element
        create(ElementImplementation(declaredName = "test"), global)
        initialize()
        val foundInGlobal = global.resolve<Element>("testID")
        val found = obj.resolve<Element>("testID")
        assertEquals("testID", found!!.declaredName) // test by user defined ID
        assertNotNull(foundInGlobal)
    }

    /** Test of the findElement function by name, visibility test  */
    @Test fun findElementTest() = testSession {
        val pkg = create(PackageImplementation(declaredName="name"), global) // new class or package in global.
        create(ElementImplementation(declaredName="name2"), pkg) // creation of a name2 element in test package/element
        assertEquals(1, pkg.getOwnedElementsOfType<Element>().size)
        assertEquals("name2", global.resolve<Element>("name::name2")!!.declaredName) // search for the name2 element
    }


    /**
     * When a name is not resolved in the given namespace, search it in the owning namespace.
     **/
    @Test fun findHasAElementByNameParentTest() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val name = create(NamespaceImplementation(declaredName="name"),  global) // new class or package in global.
        val name2 = create(NamespaceImplementation(declaredName="name2"),  name)  // class in name package/element
        val name3 = create(NamespaceImplementation(declaredName="name3"), name2)
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Element>().size)
        assertEquals("name", name2.resolve<Element>("name")!!.declaredName) // searching in name2 for the root element name
        assertEquals("name2", name3.resolve<Element>("name::name2")!!.declaredName) // searching in name3 for the root element name2
    }


    /**
     * When the element isn't found, findElement returns null, and does not throw an error.
     **/
    @Test fun findHasAElementNotFound() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val name = create(NamespaceImplementation(declaredName="name"), global)    // new class or package in global.
        val name2 = create(NamespaceImplementation(declaredName="name2"), name)     // class in name package/element
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Element>().size)
        assertNull(name2.resolveVar("test"))
        assertEquals(1, name.getOwnedElementsOfType<Element>().size)
        assertEquals(0, name2.getOwnedElementsOfType<Element>().size)
    }


    /**
     * Test of the findProperty function.
     */
    @Test fun findHasAPropertyByNameDirectTest() =  testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val name = create(NamespaceImplementation(declaredName="name"), global)  // new class or package in global.
        @Suppress("UNUSED_VARIABLE")
        create(FeatureImplementation(declaredName="name2"), name)
        // class in name package/element
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Element>().size)
        assertEquals(1, name.getOwnedElementsOfType<Element>().size)
        // assertEquals(0, getHasA(id2).size) --- Nonsense? nonsense? do properties have hasA???
        assertEquals("name", global.getOwnedElementsOfType<Element>().last().declaredName)
        assertEquals("name2", name.getOwnedElementsOfType<Element>().first().declaredName)
        assertEquals("name2", name.resolve<Feature>("name2")!!.declaredName)
        assertEquals("name2", global.resolve<Feature>("name::name2")!!.declaredName)
    }


    /**
     * Test of the name resolution function 'find':
     * - define: name, name::name2.
     * - Global imports name
     * - search in imported namespace and global must
     */
    @Test fun findElementByNameFromImportedPackageTest() = testSession {
        val name = create(NamespaceImplementation(declaredName="name"), global)    // new class 'name' of the type anything in global.
        create(NamespaceImportImplementation(importingNamespace = Resolved(global), importedNamespace = Resolved<Namespace>("name")), global)
        create(ElementImplementation(declaredName="name2"),  name)   // class in class name.

        initialize()
        // Now, we have Global::name.name2, and import Global::name into Global scope.
        // Hence, name2 should be found from global scope via HasA-Relation (!!!)

        assertEquals("name2", name.resolve<Element>("name2")!!.declaredName)
        assertEquals("name2", global.resolve<Element>("name2")!!.declaredName)
    }


    /**
     * Test if the find function searches in IMPORT:
     * Global has an Element name, that has a ValueFeature name2.
     * Global imports name, hence name2 shall be visible from global.
     */
    @Test fun findPropertyByNameFromImportedPackageTest() = testSession {
        val name = create(NamespaceImplementation(declaredName="name"),  global)    // new class or package in global.
        create(NamespaceImportImplementation(importingNamespace = Resolved(global), importedNamespace = Resolved<Namespace>("name")), global)
        val feat = create(FeatureImplementation(declaredName="name2"), name)            // class in name package/element
        create(SpecializationImplementation(feat, anything), feat)
        initialize()
        assertEquals("name2", name.resolve<Feature>("name2")!!.declaredName)
        assertEquals("name2", global.resolve<Feature>("name2")!!.declaredName)
    }
}
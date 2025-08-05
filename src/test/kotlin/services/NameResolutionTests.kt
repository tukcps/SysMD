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
        addOwnedMember(ClassImplementation(declaredName="x"), global)
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
        val p = addOwnedMember(PackageImplementation(declaredName="p"), global)
        val x = addOwnedMember(ClassImplementation(declaredName="x"), p)

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
        val pkg = addOwnedMember(PackageImplementation(declaredName="pkg"), global)
        val a = addOwnedMember(ClassImplementation(declaredName="a"), pkg)
        val import = addOwnedRelationship(NamespaceImportImplementation(importedNamespace = pkg), global)
        val found = global.resolve<Element>("a")
        assertNotNull(found)
        assertNotNull(import)
        assertNotNull(a)
    }

    /**
     *  Search by name in imports, when the package has nested elements
     */
    @Test fun findViaImport2() =testSession {
        val test = addOwnedMember(PackageImplementation(declaredName = "test"), global)
        val test2 = addOwnedMember(PackageImplementation(declaredName = "test2"), global)
        val test1 = addOwnedMember(ClassImplementation(declaredName = "test1"), test)
        addOwnedRelationship(NamespaceImportImplementation(importingNamespace = global, importedNamespace = test), global)
        addOwnedRelationship(NamespaceImportImplementation(importedNamespace = global, importingNamespace = test2), global)
        val found = test2.resolve<Element>("test1")
        assertEquals(test1, found)
    }

    /**
     * Search from specialization
     */
    @Test
    fun findFromSpecialization() = testSession {
        loadKerML("""
                type a :> Base::Anything { namespace X; } 
                type b :> a;
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val bX = global.resolve<Element>("b::X")
        assertNotNull(bX)
    }


    @Test
    fun findPropertyInGlobal() {
        testSession {
            // A property of Global.
            val id = addOwnedMember(FeatureImplementation(declaredName = "test"), global).elementId
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
        val name = addOwnedMember(TypeImplementation(declaredName="name"), global) // new class or package in global.
        addOwnedRelationship(SpecializationImplementation(name, anything), name)
        val name2 = addOwnedMember(TypeImplementation(declaredName="name2"), name)  // class in name package/element
        addOwnedRelationship(SpecializationImplementation(name2, anything), name2)
        initialize()
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Type>().size)
        assertEquals(1, name.getOwnedElementsOfType<Type>().size)
        assertEquals(1, name2.ownedElement.size)
        assertEquals("name2", name.resolve<Element>("name2")!!.declaredName)
        assertEquals("name2", global.resolve<Element>("name::name2")!!.declaredName)
    }

    /** Test of the findElement function by name - additional test cases*/
    @Test fun findHasAElementByNameDirectTestNested() = testSession {
        val c0 = addOwnedMember(PackageImplementation(declaredName="test"), global)  // new class or package in global.
        val c1 = addOwnedMember(PackageImplementation(declaredName="test1"), c0)     // creation of a test1 element in test package/element
        addOwnedMember(ElementImplementation(declaredName="test2"), c1)              // creation of a test2 element in test package/element
        assertEquals("test1", global.resolve<Element>("Global::test::test1")!!.declaredName)  // test of the qualified name 'Global::test::test1'
        assertEquals("test1", global.resolve<Element>("test::test1")!!.declaredName)
        assertEquals("test2", c0.resolve<Element>("test1::test2")!!.declaredName)
    }

    /** Test of the findElement function by id **/
    @Test fun findHasAElementByIdDirectTest() = testSession {
        val obj = addOwnedMember(PackageImplementation(declaredName = "testID"), global) // new class or package in global.
        addOwnedMember(ElementImplementation(declaredName = "name2"), obj)   // creation of a test2 element in test package/element
        addOwnedMember(ElementImplementation(declaredName = "test"), global)
        initialize()
        val foundInGlobal = global.resolve<Element>("testID")
        val found = obj.resolve<Element>("testID")
        assertEquals("testID", found!!.declaredName) // test by user defined ID
        assertNotNull(foundInGlobal)
    }

    /** Test of the findElement function by name, visibility test  */
    @Test fun findElementTest() = testSession {
        val pkg = addOwnedMember(PackageImplementation(declaredName="name"), global) // new class or package in global.
        addOwnedMember(ElementImplementation(declaredName="name2"), pkg) // creation of a name2 element in test package/element
        assertEquals(1, pkg.getOwnedElementsOfType<Element>().size)
        assertEquals("name2", global.resolve<Element>("name::name2")!!.declaredName) // search for the name2 element
    }


    /**
     * When a name is not resolved in the given namespace, search it in the owning namespace.
     **/
    @Test fun findHasAElementByNameParentTest() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val name = addOwnedMember(NamespaceImplementation(declaredName="name"),  global) // new class or package in global.
        val name2 = addOwnedMember(NamespaceImplementation(declaredName="name2"),  name)  // class in name package/element
        val name3 = addOwnedMember(NamespaceImplementation(declaredName="name3"), name2)
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Element>().size)
        assertEquals("name", name2.resolve<Element>("name")!!.declaredName) // searching in name2 for the root element name
        assertEquals("name2", name3.resolve<Element>("name::name2")!!.declaredName) // searching in name3 for the root element name2
    }


    /**
     * When the element isn't found, findElement returns null, and does not throw an error.
     **/
    @Test fun findHasAElementNotFound() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Element>().size
        val name = addOwnedMember(NamespaceImplementation(declaredName="name"), global)    // new class or package in global.
        val name2 = addOwnedMember(NamespaceImplementation(declaredName="name2"), name)     // class in name package/element
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
        val name = addOwnedMember(NamespaceImplementation(declaredName="name"), global)  // new class or package in global.
        @Suppress("UNUSED_VARIABLE")
        addOwnedMember(FeatureImplementation(declaredName="name2"), name)
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
        val name = addOwnedMember(NamespaceImplementation(declaredName="name"), global)    // new class 'name' of the type anything in global.
        addOwnedRelationship(NamespaceImportImplementation(importingNamespace = global, importedNamespace = name), global)
        addOwnedMember(ElementImplementation(declaredName="name2"),  name)   // class in class name.

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
        val name = addOwnedMember(NamespaceImplementation(declaredName="name"),  global)    // new class or package in global.
        addOwnedRelationship(NamespaceImportImplementation(importingNamespace = global, importedNamespace = name), global)
        val feat = addOwnedMember(FeatureImplementation(declaredName="name2"), name)            // class in name package/element
        addOwnedRelationship(SpecializationImplementation(feat, anything), feat)
        initialize()
        assertEquals("name2", name.resolve<Feature>("name2")!!.declaredName)
        assertEquals("name2", global.resolve<Feature>("name2")!!.declaredName)
    }


    @Test
    fun createFindPackage() = testSession {
        loadKerML("package x;")
        assertNotNull(global.resolve<Package>("x"))
    }

    @Test
    fun createFindElement() = testSession {
        loadKerML(
            """
                    package x { package y; } 
            """
        )
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val x1 = global.resolve<Package>("x")!!
        assertNotNull(x1)
        assertNotNull(x1.resolve<Package>("y"))
    }

    @Test
    fun createFindHasAElement() = testSession {
        loadKerML("""
            namespace x {
                doc y /* doc */ ; 
                doc z /* doc */ ; 
            } 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val x = global.resolve<Namespace>("x")!!
        assertNotNull(x)
        val y = global.resolve<Element>("x::y")
        val y2 = x.resolve<Element>("y")
        assertNotNull(y)
        assertNotNull(y2)
        val z = global.resolve<Element>("x::z")
        val z2 = x.resolve<Element>("z")
        assertNotNull(z)
        assertNotNull(z2)
    }

    /**
     * Properties can be found.
     */
    @Test
    fun createFindHasAProperty() = testSession {
        loadKerML("""
            package x {
                package y { 
                    type z :> Base::Anything;    // Shall be visible as x::y::z from root namespace. 
                }
                type z :> Base::Anything;     // Shall be visible in x via x::z
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val xyz = global.resolve<Type>("x::y::z")
        assertNotNull(xyz)
        val xz = global.resolve<Type>("x::z")
        assertNotNull(xz)
        val x = global.resolve<Package>("x")
        assertNotNull(x)
        assertNotNull(x.resolve<Package>("y"))
    }
}
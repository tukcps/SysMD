package services

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class NameResolutionTests {

    /**
     * Simple direct hit in global.
     */
    @Test
    fun findSimpleNameTestInGlobal() = testSession {
        addOwnedMember(ClassImplementation(declaredName="x"), global)
        val found = global.resolve("x")?.memberElement
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
        val px1 = global.resolve("p::x")?.memberElement
        assertEquals(x, px1)

        // Search x from p
        val px2 = p.resolve("x")?.memberElement
        assertEquals(x, px2)

        // search p from x; hierarchically upwards
        val pFromX = x.resolve("p")?.memberElement
        assertEquals(p, pFromX)
    }

    @Test
    fun findViaImport() = testSession {
        val pkg = addOwnedMember(PackageImplementation(declaredName="pkg"), global)
        val a = addOwnedMember(NamespaceImplementation(declaredName="a"), pkg)
        val import = addOwnedRelationship(NamespaceImportImplementation(importedNamespace = pkg), global)
        val found = global.resolve("a")?.memberElement
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
        val found = test2.resolve("test1")?.memberElement
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
        assertNoIssues()
        val bX = global.resolve("b::X")?.memberElement
        assertNotNull(bX)
    }


    @Test
    fun findPropertyInGlobal() {
        testSession {
            // A property of Global.
            val id = addOwnedMember(FeatureImplementation(declaredName = "test"), global).elementId
            // Three ways to get it:
            val found = global.resolve("test")?.member<Feature>()
            assertNotNull(found)
            val found2 = global.resolve("test")?.member<Feature>()
            assertNotNull((found2))
            val found3 = global.resolve("test")?.member<Feature>()
            assertEquals(id, found3!!.elementId)
            assertEquals(id, found.elementId)
        }
    }

    /**
     * Specifically test that ScalarValues can be resolved.
     */
    @Test fun resolveLibraryClasses()  = testSession("ScalarValues") {
        val real = global.resolve("ScalarValues::Real")?.member<DataType>()
        val int = global.resolve("ScalarValues::Integer")?.member<DataType>()
        val bool = global.resolve("ScalarValues::Boolean")?.member<DataType>()
        val str = global.resolve("ScalarValues::String")?.member<DataType>()
        assertNotNull(real)
        assertNotNull(int)
        assertNotNull(bool)
        assertNotNull(str)
    }



    /**
     * Test of the name resolution, in the same namespace.
     */
    @Test fun findHasAElementByNameDirectTest() = testSession {
        val sizeBefore = global.getOwnedElementsOfType<Type>().size
        val name = addOwnedMember(TypeImplementation(declaredName="name"), global) // new class or package in global.
        addOwnedRelationship(SpecializationImplementation(name, anything), name)
        val name2 = addOwnedMember(TypeImplementation(declaredName="name2"), name)  // class in name package/element
        addOwnedRelationship(SpecializationImplementation(name2, anything), name2)
        initialize(Runlevel.MODEL)
        assertEquals(sizeBefore+1, global.getOwnedElementsOfType<Type>().size)
        assertEquals(1, name.getOwnedElementsOfType<Type>().size)
        assertEquals(1, name2.ownedElement.size)
        assertEquals("name2", name.resolve("name2")!!.memberElement.declaredName)
        assertEquals("name2", global.resolve("name::name2")!!.memberElement.declaredName)
    }

    /** Test of the findElement function by name - additional test cases*/
    @Test fun findHasAElementByNameDirectTestNested() = testSession {
        val c0 = addOwnedMember(PackageImplementation(declaredName="test"), global)  // new class or package in global.
        val c1 = addOwnedMember(PackageImplementation(declaredName="test1"), c0)     // creation of a test1 element in test package/element
        addOwnedMember(ElementImplementation(declaredName="test2"), c1)              // creation of a test2 element in test package/element
        assertEquals("test1", global.resolve("test::test1")!!.memberElement.declaredName)  // test of the qualified name 'Global::test::test1'
        assertEquals("test1", global.resolve("test::test1")!!.memberElement.declaredName)
        assertEquals("test2", c0.resolve("test1::test2")!!.memberElement.declaredName)
    }

    /** Test of the findElement function by id **/
    @Test fun findHasAElementByIdDirectTest() = testSession {
        val obj = addOwnedMember(PackageImplementation(declaredName = "testID"), global) // new class or package in global.
        addOwnedMember(ElementImplementation(declaredName = "name2"), obj)   // creation of a test2 element in test package/element
        addOwnedMember(ElementImplementation(declaredName = "test"), global)
        initialize(Runlevel.MODEL)
        val foundInGlobal = global.resolve("testID")?.memberElement
        val found = obj.resolve("testID")?.memberElement
        assertEquals("testID", found!!.declaredName) // test by user defined ID
        assertNotNull(foundInGlobal)
    }

    /** Test of the findElement function by name, visibility test  */
    @Test fun findElementTest() = testSession {
        val pkg = addOwnedMember(PackageImplementation(declaredName="name"), global) // new class or package in global.
        addOwnedMember(ElementImplementation(declaredName="name2"), pkg) // creation of a name2 element in test package/element
        assertEquals(1, pkg.getOwnedElementsOfType<Element>().size)
        assertEquals("name2", global.resolve("name::name2")?.memberElement!!.declaredName) // search for the name2 element
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
        assertEquals("name", name2.resolve("name")!!.memberElement.declaredName) // searching in name2 for the root element name
        assertEquals("name2", name3.resolve("name::name2")!!.memberElement.declaredName) // searching in name3 for the root element name2
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
        assertEquals("name2", name.resolve("name2")!!.memberElement.declaredName)
        assertEquals("name2", global.resolve("name::name2")!!.memberElement.declaredName)
    }


    /**
     * Test of the name resolution function 'find':
     * - define: name, name::name2.
     * - Global imports name
     * - search in imported namespace and global
     */
    @Test fun findElementByNameFromImportedPackageTest() = testSession {
        val name = addOwnedMember(NamespaceImplementation(declaredName="name"), global)    // new class 'name' of the type anything in global.
        addOwnedRelationship(NamespaceImportImplementation(importingNamespace = global, importedNamespace = name), global)
        addOwnedMember(ElementImplementation(declaredName="name2"),  name)   // class in class name.

        initialize(Runlevel.MODEL)

        assertEquals("name2", name.resolve("name2")!!.memberElement.declaredName)
        assertEquals("name2", global.resolve("name2")!!.memberElement.declaredName)
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
        initialize(Runlevel.MODEL)
        assertEquals("name2", name.resolve("name2")!!.member<Feature>()?.declaredName)
        assertEquals("name2", global.resolve("name2")!!.member<Feature>()?.declaredName)
    }


    @Test
    fun createFindPackage() = testSession {
        loadKerML("package x;")
        assertNotNull(global.resolve("x")?.memberElement)
    }

    @Test
    fun resolveInNamespaceOtherThanGlobal() = testSession {
        loadKerML("""
            package x { package y; } 
        """)
        assertNoIssues()
        val x = global.resolve("x")!!.member<Package>()
        assertNotNull(x)
        assertNotNull(x.resolve("y")?.member<Package>()?.declaredName)
    }

    @Test
    fun resolveQualifiedNameFromGlobal() = testSession {
        loadKerML("""
            namespace x {
                doc y /* doc */ ; 
                doc z /* doc */ ; 
            } 
        """)
        assertNoIssues()
        val x = global.resolve("x")?.member<Namespace>()
        assertNotNull(x)
        val y = global.resolve("x::y")?.member<Documentation>()
        val y2 = x.resolve("y")?.member<Documentation>()
        assertNotNull(y)
        assertNotNull(y2)
        val z = global.resolve("x::z")?.member<Documentation>()
        val z2 = x.resolve("z")?.member<Documentation>()
        assertNotNull(z)
        assertNotNull(z2)
    }

    /**
     * Properties can be found.
     */
    @Test
    fun resolveQualifiedNameFromGlobal2() = testSession {
        loadKerML("""
            namespace x {
                namespace y { 
                    namespace z;    // Shall be visible as x::y::z from root namespace. 
                }
                namespace z;        // Shall be visible in x via x::z
            }
        """)
        assertNoIssues()
        val xyz = global.resolve("x::y::z")?.member<Namespace>()
        assertNotNull(xyz)
        val xz = global.resolve("x::z")?.member<Namespace>()
        assertNotNull(xz)
        val x = global.resolve("x")?.member<Namespace>()
        assertNotNull(x)
        assertNotNull(x.resolve("y")?.member<Namespace>())
    }

    /**
     * Imports that are public import namespaces public.
     */
    @Test
    fun importTest() = testSession {
        loadKerML("""
            namespace A { public import B; }  
            namespace B;
        """)
        assertNoIssues()
        val b = global.resolve("A::B")?.member<Namespace>()
        assertNotNull(b)
    }

    /**
     * Imports that are public import namespaces public.
     */
    @Test
    fun importTest2() = testSession {
        loadKerML("""
            namespace A { private import B::*; }  
            namespace B {
                doc b /* doc */ ;
            }
        """)
        assertNoIssues()
        val b = global.resolve("A::b")?.memberElement
        assertNotNull(b, "Import failed.")
    }
}
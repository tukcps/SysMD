package services

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.services.session.getAllOfClass
import com.github.tukcps.sysmd.services.session.loadLibrary
import com.github.tukcps.sysmd.services.session.loadProject
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SessionTests {

    @Test fun testStartSession() {
        val session = SessionImplementation(libraries = mutableListOf())
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString())
    }

    @Test fun testSessionResolveElement() {
        val session = SessionImplementation(libraries = mutableListOf())
        val base = session.global.resolve("Base")?.member<Package>()
        assertNotNull(base)
        assertEquals("Base", base.name)
        assertEquals(session.global, base.owner)
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString())
    }

    @Test
    fun countOwnedTest1() {
        val session = SessionImplementation(libraries = mutableListOf())
        val e1 = ElementImplementation(declaredName = "e1")
        val e2 = AnnotationImplementation(declaredName = "e2")
        session.addOwnedMember(e1, session.global)
        session.addOwnedRelationship( e2, session.global)
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString() )
        assertEquals(3, session.global.ownedElement.size) // Base, e1, e2
        assertEquals(3, session.global.ownedRelationship.size) // Owning, Annotation
    }

    @Test
    fun countOwnedTest2() {
        val session = SessionImplementation(libraries = mutableListOf())
        val e = NamespaceImplementation(declaredName = "e")
        val e1 = ElementImplementation(declaredName = "e1")
        val e2 = ElementImplementation(declaredName = "e2")
        session.addOwnedMember(e, session.global)
        session.addOwnedMember(e1, e)
        session.addOwnedMember(e2, e)
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString() )
        assertEquals(2, session.global.ownedElement.size)
    }

    @Test
    fun addOwnedRelationshipDoesNotAddDuplicateSpecialization() = testSession {
        val type = addOwnedMember(TypeImplementation("t"), global)
        addOwnedRelationship(SpecializationImplementation(), type)
        initialize()
        addOwnedRelationship(SpecializationImplementation(), type)
        initialize()
        assertEquals(1, type.ownedRelationship.size)
        assertNoIssues()
    }

    /**
     * In a session, addOwnedRelationship does not create duplicate entries of a re-definition.
     */
    @Test
    fun addOwnedRelationshipDoesNotAddDuplicateRedefinition() = testSession {
        // Feature with redefinition
        loadKerML("""
            type t :> Base::Anything { feature f; } 
            type t2 :> t { :>> f; }
        """)
        assertNoIssues()
        val t2 = global.resolve("t2")?.member<Type>()!!
        val tf = global.resolve("t::f")?.member<Feature>()!!
        val t2f = global.resolve("t2::f")?.member<Feature>()!!
        assertEquals(2,t2f.ownedRelationship.size)
        assertTrue(tf !== t2f)
        // Try to make a duplicate
        addOwnedRelationship(RedefinitionImplementation(t2f, tf), t2f)
        initialize()
        assertEquals(2,t2f.ownedRelationship.size)
        assertNoIssues()
    }

    /**
     * In a session, addOwnedRelationship does not create duplicate entries of a re-definition.
     */
    @Test
    fun addOwnedRelationshipDoesNotAddDuplicateRedefinition2() = testSession {
        // Feature with redefinition
        loadKerML("""
            type t :> Base::Anything { feature f; } 
            type t2 :> t { :>> f; }
        """)
        assertNoIssues()
        val t: Type? = global.resolve("t")?.member()
        val t2: Type? = global.resolve("t2")?.member()
        val tf: Feature? = global.resolve("t::f")!!.member()
        val t2f: Feature = global.resolve("t2::f")!!.member()!!
        assertEquals(2,t2f.ownedRelationship.size)
        assertTrue(tf !== t2f)
        // Try to make a duplicate
        addOwnedRelationship(RedefinitionImplementation(t2f, UnresolvedFeature("f")), t2f)
        initialize()
        assertEquals(2,t2f.ownedRelationship.size)
        assertNoIssues()
    }

    /**
     * reset of a session creates new repo, new libraries, that are of similar size as before.
     */
    @Test
    fun resetTest() = testSession("ScalarValues") {
        val size = repo.elements.size // Before
        @Suppress("UNCHECKED_CAST")
        val elements = repo.elements.clone() as HashMap<*, Element>
        reset()
        loadLibrary("ScalarValues")
        val elements2 = repo.elements
        val diff = mutableListOf<Element>()
        val diff2 = mutableListOf<Element>()
        elements2.forEach { if (it.key !in elements.keys) diff.add(it.value) }
        elements.forEach { if (it.key !in elements2.keys) diff2.add(it.value) }
        val libs = global.ownedElement
        assertTrue(2 <= libs.size)
        assertEquals(size, repo.elements.size)
    }

    @Test
    fun loadKerMLBasic() = testSession {
        loadKerML("""
            package Base {
                type t :> Base::Anything; 
                feature f: Base::Anything;
            }
        """)
        val t = global.resolve("Base::t")?.memberElement
        val f = global.resolve("Base::f")?.memberElement
        assertNotNull(t)
        assertNotNull(f)
        val ex = export()
        val s = SessionImplementation(libraries = mutableListOf())
        val inp = ex.map {
            it.payloadElementSnapshot?:
            ElementData(java.util.UUID.randomUUID(), "null")
        }
        s.import(inp)
        assertTrue(status.issues.isEmpty(), status.issues.toString() )
    }

    @Test
    fun loadLibraryBase() = testSession {
        loadLibrary("Base")
        assertNoIssues()
        assertEquals(1, global.ownedRelationship.size, "Only the Base library ownership must exist, once")
        assertNotNull(global.resolve("Base::Anything")?.member<Classifier>())
        assertNotNull(global.resolve("Base::things")?.member<Feature>())
    }

    @Test
    fun loadProject() {
        val session = SessionImplementation(libraries = mutableListOf())
        session.loadProject("Base")
        assertEquals(1, session.global.ownedRelationship.size)
        assertTrue(session.status.issues.isEmpty())
    }

    @Test
    fun loadKerMLTest() = testSession {
        loadKerML("""
            namespace test; 
        """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test: Namespace? = global.resolve("test")?.member()
        assertNotNull(test)
    }

    @Test
    fun loadSysMLTest() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part test; 
        """)
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val test: PartUsage? = global.resolve("test")?.member()
        assertNotNull(test)
    }

    @Test
    fun loadSysMDTest() = testSession("SysMD") {
        loadKerML("""
            metadata p: SysMD::Project {
                name : ScalarValues::String        = "name"; 
                maintainer : ScalarValues::String  = "maintainer"; 
                license : ScalarValues::String     = "license";
                files : ScalarValues::String[1..*] = ("file1", "file2");
            }
        """)
        assertNoIssues()
        val p: MetadataFeature? = global.resolve("p")?.member()
        assertNotNull(p)
        assertEquals("p", p.name)
        assertEquals("name", p.getOwned<Feature>("name")?.variable?.valueStr)
        assertEquals("maintainer", p.getOwned<Feature>("maintainer")?.variable?.valueStr)
        assertEquals("license", p.getOwned<Feature>("license")?.variable?.vectorQuantity?.toString())
        assertEquals("file1", p.getOwned<Feature>("files")!!.variable!!.vectorQuantity.values[0].toString())
        assertEquals("file2", p.getOwned<Feature>("files")!!.variable!!.vectorQuantity.values[1].toString())
    }


    /** Re-creation of a type shall not lead to duplications, e.g., after a re-load.  */
    @Test
    fun typesNotAppearTwice() = testSession {
        loadKerML("type t :> Base::Anything; ")
        assertNoIssues()
        val elem1 = getAllOfClass<Element>().toSet()
        loadKerML("type t :> Base::Anything; ")
        assertTrue( status.issues.isEmpty(), status.issues.toString())
        val elem2 = getAllOfClass<Element>().toSet()
        val diff = elem2 - elem1
        assertTrue( elem2.containsAll(elem1), diff.toString() )
        assertTrue( elem1.containsAll(elem2), diff.toString() )
    }



    /**
     * Repeated execution of initialize() or parsing a model shall not
     * add duplicate elements.
     */
    @Test
    fun featuresAndMultiplicitiesNotRecreated() = testSession("ScalarValues") {
        loadKerML("""
            datatype i [1..2];   
        """)
        assertNoIssues()
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val imports1        = getAllOfClass<Import>()
        val specs1          = getAllOfClass<Specialization>()
        val elem1           = getAllOfClass<Element>()

        loadKerML("""
            datatype i [1..3];   
        """)
        assertNoIssues()
        val multiplicities2 = getAllOfClass<Multiplicity>()
        val imports2 = getAllOfClass<Import>()
        val elem2 = getAllOfClass<Element>()
        val specs2 = getAllOfClass<Specialization>()

        checkOwnership()
        assertEquals(multiplicities1.size, multiplicities2.size, "added multiplicities: ${multiplicities2- multiplicities1.toSet()}")
        assertEquals(specs1.size, specs2.size, "added specialization: ${specs2- specs1.toSet()}")
        assertEquals(imports1.size, imports2.size,"added import: ${imports2- imports1.toSet()}")
        assertEquals(elem1.size, elem2.size, "added elements: ${elem2- elem1.toSet()}")
        assertNoIssues()
    }


    /** Re-creation of a feature shall not lead to duplications, e.g., after a re-load. */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice1() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> Base::Any;  }; feature x; ")
        assertNoIssues()
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val elem1 = getAllOfClass<Element>()
        loadKerML("feature x; ")
        assertNoIssues()
        val multiplicities2 = getAllOfClass<Multiplicity>()
        val elem2 = getAllOfClass<Element>()
        assertEquals(multiplicities1.size, multiplicities2.size)
        assertEquals(elem1.size, elem2.size)
    }



    /**
     * Annotations, if unnamed, are not created twice.
     * Allows us to re-execute a parse run.
     */
    @Test
    fun createAnnotationNotTwice(): Unit = testSession {
        val a = AnnotationImplementation(annotatedElement = global, annotatingElement = anything)
        val b = AnnotationImplementation(annotatedElement = global, annotatingElement = anything)
        addOwnedRelationship(a, global)
        b.elementId = a.elementId
        val aa = addOwnedRelationship(b, global)
        initialize(1)
        assertEquals(a, aa)
    }

    @Test
    fun connectorNotCreatedTwice(): Unit = testSession("Links") {
        loadKerML("""
            feature a; 
            feature b;
            connector c from a to b; 
        """)
        val elements = get()
        loadKerML("""
            feature a; 
            feature b;
            connector c from a to b; 
        """)
        initialize()
        val elements2 = get()
        assertEquals(elements.size, elements2.size)
    }
}
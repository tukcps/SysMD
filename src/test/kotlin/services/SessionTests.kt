package services

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.model.util.UnresolvedFeature
import com.github.tukcps.sysmd.rest.ProjectImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.implementation.ProjectSessionImplementation
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import com.github.tukcps.sysmd.services.session.implementation.getAllOfClass
import com.github.tukcps.sysmd.services.session.loadProject
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*
import kotlin.uuid.Uuid

class SessionTests {

    @Test fun testStartSession() {
        val session = SessionImplementation()
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString())
    }

    @Test
    fun testStartSessionWithBase() {
        val session = SessionImplementation("Base")
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString())
        val anything = session.global.resolve("Base::Anything")?.member<Element>()
        assertTrue(anything is Classifier)
        val base = session.global.resolve("Base")?.member<Package>()
        assertNotNull(base)
        assertEquals("Base", base.name)
        assertEquals(session.global, base.owner)
    }

    @Test
    fun testStartSessionWithBaseTwice() {
        val session = SessionImplementation("Base")
        session.loadLibrary("Base")
        session.assertNoIssues()
        assertEquals(1, session.global.ownedRelationship.size)
        val anything = session.global.resolve("Base::Anything")?.member<Element>()
        assertTrue(anything is Classifier)
    }

    /**
     * Check whether a prefix of a notebook-cell (or a Sysmd statement) generates
     * packages in the output.
     * NOTE: Ths is done during import of generated elements, not by parser anymore.
     */
    @Test
    fun initOwningPackagesTest() = testSession {
        val parser = KerML()
        val elements = parser.parse("")
        import(elements, "foo::bar")
        assertNoIssues()
        assertNotNull(global.resolve("foo")?.member<Package>())
        assertNotNull(global.resolve("foo::bar")?.member<Package>())
    }

    @Test
    fun countOwnedTest1() {
        val session = SessionImplementation("Base")
        val e1 = ElementImplementation(session, declaredName = "e1")
        val e2 = AnnotationImplementation(session, declaredName = "e2")
        session.addOwnedMember(e1, session.global)
        session.addOwnedRelationship( e2, session.global)
        assertTrue(session.status.issues.isEmpty(), session.status.issues.toString() )
        assertEquals(3, session.global.ownedElement.size) // Base, e1, e2
        assertEquals(3, session.global.ownedRelationship.size) // Owning, Annotation
    }

    @Test
    fun countOwnedTest2() {
        val session = SessionImplementation()
        val e = NamespaceImplementation(session, declaredName = "e")
        val e1 = ElementImplementation(session, declaredName = "e1")
        val e2 = ElementImplementation(session, declaredName = "e2")
        session.addOwnedMember(e, session.global)
        session.addOwnedMember(e1, e)
        session.addOwnedMember(e2, e)
        assertEquals(1, session.global.ownedElement.size)
    }

    @Test
    fun addOwnedRelationshipDoesNotAddDuplicateSpecialization() = testSession {
        val type = addOwnedMember(TypeImplementation(this, declaredName = "t"), global)
        addOwnedRelationship(SpecializationImplementation(this), type)
        initialize(Runlevel.MODEL)
        addOwnedRelationship(SpecializationImplementation(this), type)
        initialize(Runlevel.MODEL)
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
        """, Runlevel.MODEL)
        assertNoIssues()
        val t2 = global.resolve("t2")?.member<Type>()
        assertNotNull(t2)
        val tf = global.resolve("t::f")?.member<Feature>()!!
        val t2f = global.resolve("t2::f")?.member<Feature>()!!
        assertEquals(2,t2f.ownedRelationship.size)
        assertTrue(tf !== t2f)
        // Try to make a duplicate
        addOwnedRelationship(RedefinitionImplementation(this, redefiningFeature = t2f, redefinedFeature = tf), t2f)
        initialize(Runlevel.MODEL)
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
        """, Runlevel.MODEL)
        assertNoIssues()
        val t: Type? = global.resolve("t")?.member()
        assertNotNull(t)
        val t2: Type? = global.resolve("t2")?.member()
        assertNotNull(t2)
        val tf: Feature? = global.resolve("t::f")!!.member()
        val t2f: Feature = global.resolve("t2::f")!!.member()!!
        assertEquals(2, t2f.ownedRelationship.size)
        assertTrue(tf !== t2f)
        // Try to make a duplicate
        addOwnedRelationship(RedefinitionImplementation(this, redefiningFeature = t2f, redefinedFeature = UnresolvedFeature(this, "f")), t2f)
        initialize(Runlevel.MODEL)
        assertEquals(2,t2f.ownedRelationship.size)
        assertNoIssues()
    }


    @Test
    fun loadKerMLBasic() = testSession("ScalarValues") {
        assertNoIssues()
        loadKerML("""
            package p {
                type t :> Base::Anything; 
                feature f: Base::Anything;
            }
        """, Runlevel.MODEL)
        assertNoIssues()
        val t = global.resolve("p::t")?.memberElement
        val f = global.resolve("p::f")?.memberElement
        assertNotNull(t)
        assertNotNull(f)
        val ex = export()
        val s = SessionImplementation()
        val inp = ex.map {
            it.payloadElementSnapshot
                ?: ElementData(Uuid.random(), ElementType.Element)
        }
        s.import(inp)
        assertTrue(status.issues.isEmpty(), status.issues.toString() )
    }

    @Test
    fun loadLibraryBase() = testSession {
        assertNoIssues()
        assertEquals(1, global.ownedRelationship.size, "Only the Base library ownership must exist, once")
        assertNotNull(global.resolve("Base::Anything")?.member<Classifier>())
        assertNotNull(global.resolve("Base::things")?.member<Feature>())
    }

    @Ignore // Test is incomplete; no project exists.
    @Test
    fun loadProject() {
        val project = ProjectData(ProjectImplementation(defaultBranchId = Uuid.random()))
        val session = ProjectSessionImplementation(project, runlevel = Runlevel.NONE)
        session.loadProject("xx")
        assertEquals(1, session.global.ownedRelationship.size)
        assertTrue(session.status.issues.isEmpty())
    }

    @Test
    fun loadKerMLTest() = testSession {
        loadKerML("""
            namespace test; 
        """)
        initialize(Runlevel.MODEL)
        assertNoIssues()
        val test: Namespace? = global.resolve("test")?.member()
        assertNotNull(test)
    }

    @Test
    fun loadSysMLTest() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part test; 
        """)
        initialize(Runlevel.MODEL)
        assertNoIssues()
        val test: PartUsage? = global.resolve("test")?.member()
        assertNotNull(test)
    }

    @Test
    fun loadSysMDTest() = testSession("SysMD", "Metadata") {
        loadKerML("""
            metadata p: SysMD::Project {
                name : ScalarValues::String        = "name"; 
                maintainer : ScalarValues::String  = "maintainer"; 
                license : ScalarValues::String     = "license";
                files : ScalarValues::String[1..*] = ("file1", "file2");
            }
        """, Runlevel.VARIABLES)
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
        assertNoIssues()
        val elem2 = getAllOfClass<Element>().toSet()
        val diff = elem2 - elem1
        assertTrue( elem2.containsAll(elem1), "Added: $diff")
        assertTrue( elem1.containsAll(elem2), "Added: $diff")
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
    @Ignore // TODO: Adapt to SysMD interactive
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
        val a = AnnotationImplementation(this, annotatingElement = repo.anything!!, annotatedElement = global)
        addOwnedRelationship(a, global)
        val b = AnnotationImplementation(this, elementId = a.elementId, annotatingElement = repo.anything!!, annotatedElement = global)
        val aa = addOwnedRelationship(b, global)
        initialize(Runlevel.NAMES_RESOLVED)
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
        initialize(Runlevel.MODEL)
        val elements2 = get()
        assertEquals(elements.size, elements2.size)
    }
}
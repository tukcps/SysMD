package services

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotationImplementation
import com.github.tukcps.sysmd.model.sysml.PartUsage
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.startSession
import com.github.tukcps.sysmd.services.session.loadLibrary
import com.github.tukcps.sysmd.services.session.loadProject
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SessionTests {

    /**
     * reset of a session creates new repo, new libraries, that are of similar size as before.
     */
    @Test
    fun resetTest() = testSession("Parts") {
        val size = repo.elements.size // Before
        val elements = repo.elements.clone() as HashMap<*, *>
        reset()
        val elements2 = repo.elements
        val diff = mutableListOf<Element>()
        elements2.forEach {
            if (it.key !in elements.keys) diff.add(it.value)
        }
        val libs = global.ownedElement
        assertTrue(5 <= libs.size)
        assertEquals(size, repo.elements.size)
    }



    /**
     * loadLibrary
     * - loads a library from a file;
     * - standard libraries get UUID v5, not v4 and is the same for all qualified names.
     */
    @Test
    fun loadLibrary() {
        val session = startSession()
        session.loadLibrary("ScalarValues")
        session.initialize()
        val real = session.global.resolve<DataType>("ScalarValues::Real")
        val sv = session.global.resolve<Package>("ScalarValues")
        assertTrue(sv!!.isLibraryElement)
        assertEquals(Generators.nameBasedGenerator().generate("ScalarValues"), sv.elementId)
        assertEquals(Generators.nameBasedGenerator().generate("ScalarValues::Real"), real!!.elementId)
    }

    @Test
    fun loadProject() {
        val session = startSession()
        session.loadProject("Base")
        assertTrue(session.status.exceptions.isEmpty())
    }

    @Test
    fun loadKerMLTest() = testSession {
        loadKerML("""
            namespace test; 
        """)
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val test = global.resolve<Namespace>("test")
        assertNotNull(test)
    }

    @Test
    fun loadSysMLTest() = testSession("SysMLLibraries") {
        loadSysMLv2("""
            part test; 
        """)
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val test = global.resolve<PartUsage>("test")
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
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val p = global.resolve<MetadataFeature>("p")
        assertNotNull(p)
        assertEquals("p", p.name)
        assertEquals("name", p.getOwned<Feature>("name")?.variable?.valueStr)
        assertEquals("maintainer", p.getOwned<Feature>("maintainer")?.variable?.valueStr)
        assertEquals("license", p.getOwned<Feature>("license")?.variable?.vectorQuantity?.toString())
        assertEquals("file1", p.getOwned<Feature>("files")!!.variable!!.vectorQuantity.values[0].toString())
        assertEquals("file2", p.getOwned<Feature>("files")!!.variable!!.vectorQuantity.values[1].toString())
    }

    /**
     * Annotations, if unnamed, are not created twice.
     * Allows us to re-execute a parse run.
     */
    @Test
    fun createElement(): Unit = startSession().run {
        val a = AnnotationImplementation()
        a.source.add(Resolved(global.elementId))
        a.target.add(Resolved(anything.elementId))
        val b = AnnotationImplementation()
        b.source.add(Resolved(global.elementId))
        b.target.add(Resolved(anything.elementId))
        create(a, global)
        val aa = create(b, global)
        assertEquals(a, aa)
    }
}
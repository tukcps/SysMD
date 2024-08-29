package services

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotationImplementation
import com.github.tukcps.sysmd.compiler.loadLibrary
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.services.session.SessionManager.startSession
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SessionTests {
    @Test
    fun resetTest() = testSession {
        val size = repo.elements.size
        val elements = repo.elements.clone() as HashMap<*, *>
        reset()
        val elements2 = repo.elements
        val diff = mutableListOf<Element>()
        elements2.forEach {
            if (it.key !in elements.keys) diff.add(it.value)
        }
        val libs = global.ownedElement
        assertEquals(6, libs.size)
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
        session.loadLibrary("ScalarValues.md")
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

    /**
     * Annotations, if unnamed, are not created twice.
     * Allows us to re-execute a parse run.
     */
    @Test
    fun createElement(): Unit = startSession().run {
        val a = AnnotationImplementation()
        a.source.add(Resolved(global.elementId))
        a.target.add(Resolved(any.elementId))
        val b = AnnotationImplementation()
        b.source.add(Resolved(global.elementId))
        b.target.add(Resolved(any.elementId))
        create(a, global)
        val aa = create(b, global)
        assertEquals(a, aa)
    }
}
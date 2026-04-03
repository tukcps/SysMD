package ui

import com.github.tukcps.sysmd.rest.RESTRepository
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class BoardViewModelTest {

    /**
     * Simple check if all elements with errors from the same cell/textual
     * representation appear in the agenda
     */
    @Test
    fun elementsInAgenda() = testSession {
        val sysMdViewModel = SysMDViewModel(this)
        loadKerML("""
            class A specializes B; 
        """)
        solver.propagate()
        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(1, agenda.size())
        assertEquals(false, agenda.isEmpty())
        // assertEquals(true, agenda.contains("A"))
    }

    @Test
    fun partDefSuperclassNotIdentified() = testSession("ScalarValues") {
        loadKerML("""
          package test {
                import ScalarValues;
                part def testCPU isA Controller {
                    ;     
                }
           }
        """)
        initialize()
        // solver.propagate()
        assertTrue(status.issues.isNotEmpty())
    }

    /**
     * Check if all errors related with a single element occur in the agenda
     */
    @Test
    fun multipleErrorOnSingleElement() = testSession {
        RESTRepository.internalSessionId = id
        val sysMdViewModel = SysMDViewModel(this)

        loadKerML("""
            type A :> B; 
        """)
        solver.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(1, agenda.size())
        // assertEquals(true, agenda.contains("A"))

        status.issues.clear()
        agenda.clear()

        loadKerML("""
            type A :> C; 
        """)
        solver.propagate()

        agenda.update()

        assertEquals(2, agenda.size())
        // assertEquals(true, agenda.contains("A"))
    }

    @Test
    fun statusIsNotNullError() = testSession {
        val sysMdViewModel = SysMDViewModel(this)
        loadKerML("""
            class A :> B; 
            class B :> A;
        """)
        val agenda = sysMdViewModel.agenda
        agenda.update()
        assertNotNull(agenda.status)
    }

    @Test
    fun emptyOnNoInput() = testSession {
        RESTRepository.internalSessionId = id
        val sysMdViewModel = SysMDViewModel(this)
        val agenda = sysMdViewModel.agenda
        assertEquals(0, agenda.size())
        assertEquals(true, agenda.isEmpty())
        agenda.update()
        assertEquals(0, agenda.size())
        assertEquals(true, agenda.isEmpty())
        loadKerML(
            """
        """.trimIndent()
        )
        solver.propagate()
        agenda.update()
        assertEquals(0, agenda.size())
        assertEquals(true, agenda.isEmpty())
    }

    @Test
    fun emptyAfterClear() = testSession {
        RESTRepository.internalSessionId = id
        val sysMdViewModel = SysMDViewModel(this)

        loadKerML("""
            type A :> B; 
            type B :> A; 
        """)

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(1, agenda.size())
        agenda.clear()
        assertEquals(0, agenda.size())
    }

    @Test
    fun getIssues() = testSession("Occurrences") {
        RESTRepository.internalSessionId = id
        val sysMdViewModel = SysMDViewModel(this)

        loadKerML("""
            class A :> B; 
            class C :> D; 
        """)
        solver.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        val issues = agenda.issues()

        assertEquals(2, agenda.size())
        assertEquals("A", issues.find { it.qualifiedName == "A" }!!.qualifiedName)
        assertEquals("C", issues.find { it.qualifiedName == "C" }!!.qualifiedName)
    }

    @Test
    fun errorCorrection() = testSession {
        RESTRepository.internalSessionId = id
        val sysMdViewModel = SysMDViewModel(this)

        loadKerML("""
            type A :> B; 
        """)
        solver.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(true, agenda.contains(issue = status.issues.first()))
        assertEquals(1, agenda.size())

        status.issues.clear()
        agenda.clear()
        loadKerML("""
            type B :> Base::Anything; 
        """)
        solver.propagate()
        agenda.update()
        assertEquals(true, agenda.isEmpty())
    }

    @Test
    fun size() = testSession {
        RESTRepository.internalSessionId = id
        val sysMdViewModel = SysMDViewModel(this)

        loadKerML("""
            type C :> D; 
            type A :> B; 
        """)
        solver.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(2, agenda.size())

        status.issues.clear()
        agenda.clear()

        loadKerML("""
            type B :> Base::Anything; 
        """)
        solver.propagate()
        agenda.update()
        assertEquals(1, agenda.size())
        status.issues.clear()
        agenda.clear()

        loadKerML("""
            type D :> Base::Anything; 
        """.trimIndent())
        solver.propagate()

        agenda.update()

        assertEquals(true, agenda.isEmpty())
    }


    @Test
    fun issue235elementReclassification() {
        val session = SessionManager.startSession()
        RESTRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session)

        session.loadKerML("""
            class A :> B; 
            class B; 
            class A :> C; 
        """)
        session.solver.propagate()
        assertEquals(1, session.status.issues.size, session.status.issues.toString())
        assertTrue(session.status.issues.any { it.message.contains("C") }, "Error message is expected reporting C as undefined")
        val agenda = sysMdViewModel.agenda
        agenda.update()

        //The Agenda must not be empty as the reassignment of "A" to an unknown
        // superclass is recognized as error
        assertFalse(agenda.isEmpty())
    }

    @Ignore
    @Test
    fun removeElement() = testSession {
        RESTRepository.internalSessionId = id
        val sysMdViewModel = SysMDViewModel(this)

        loadKerML("""
            type C :> D; 
            type A :> B;
        """)
        solver.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()
        assertEquals(2, agenda.size())
        agenda.removeElement(qualifiedName = "A")
        assertEquals(1, agenda.size())
       // val error = agenda.issues().first()
        // agenda.removeElement(error.qualifiedName, error.textualRepresentation, error.line)
        assertEquals(0, agenda.size())
        agenda.update()
        assertEquals(2, agenda.size())
    }
}
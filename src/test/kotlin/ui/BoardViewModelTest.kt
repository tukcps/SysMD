package ui

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.SessionManager.sessionService
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import util.mockup.loadKerML
import util.testProjectSession
import kotlin.test.*

class BoardViewModelTest {

    /**
     * Simple check if all elements with errors from the same cell/textual
     * representation appear in the agenda
     */
    @Test
    fun elementsInBoard() = testProjectSession {
        val sysMdViewModel = SysMDViewModel()
        sysMdViewModel.sessionId = this.id
        loadKerML("""
            class A specializes B; 
        """)
        solver.propagate()
        val agenda = sysMdViewModel.boardViewModel
        agenda.update()

        assertEquals(1, agenda.size())
        assertEquals(false, agenda.isEmpty())
        // assertEquals(true, agenda.contains("A"))
    }

    @Test
    fun partDefSuperclassNotIdentified() = testProjectSession("ScalarValues") {
        loadKerML("""
          package test {
                import ScalarValues;
                part def testCPU isA Controller {
                    ;     
                }
           }
        """)
        initialize(Runlevel.MODEL)
        // solver.propagate()
        assertTrue(status.issues.isNotEmpty())
    }

    /**
     * Check if all errors related with a single element occur in the agenda
     */
    @Test
    fun multipleErrorOnSingleElement() = testProjectSession {
        val ui = SysMDViewModel()
        ui.sessionId = this.id
        loadKerML("""type A :> B; """)
        solver.propagate()

        ui.boardViewModel.update()

        assertEquals(1, ui.boardViewModel.size())
        // assertEquals(true, agenda.contains("A"))

        status.issues.clear()
        ui.boardViewModel.clear()

        loadKerML("""type A :> C; """)
        solver.propagate()

        ui.boardViewModel.update()

        assertEquals(2, ui.boardViewModel.size())
        // assertEquals(true, agenda.contains("A"))
    }

    @Test
    fun statusIsNotNullError() = testProjectSession {
        val ui = SysMDViewModel()
        loadKerML("""
            class A :> B; 
            class B :> A;
        """)
        ui.boardViewModel.update()
        assertNotNull(ui.boardViewModel.status)
    }

    @Test
    fun emptyOnNoInput() = testProjectSession {
        val ui = SysMDViewModel()
        assertEquals(0, ui.boardViewModel.size())
        assertTrue(ui.boardViewModel.isEmpty())
        ui.boardViewModel.update()
        assertEquals(0, ui.boardViewModel.size())
        assertTrue(ui.boardViewModel.isEmpty())
        loadKerML(
            """
        """.trimIndent()
        )
        solver.propagate()
        ui.boardViewModel.update()
        assertTrue(ui.boardViewModel.isEmpty())
    }

    @Test
    fun emptyAfterClear() = testProjectSession {
        val ui = SysMDViewModel().also { it.sessionId = this.id }

        loadKerML("""
            type A :> B; 
            type B :> A; 
        """, Runlevel.MODEL)

        ui.boardViewModel.update()

        assertEquals(1, ui.boardViewModel.size())
        ui.boardViewModel.clear()
        assertEquals(0, ui.boardViewModel.size())
    }

    @Test
    fun getIssues() = testProjectSession {
        val ui = SysMDViewModel().also { it.sessionId = this.id }

        loadKerML("""
            class A :> B; 
            class C :> D; 
        """, Runlevel.MODEL)

        ui.boardViewModel.update()

        val issues = ui.boardViewModel.issues()

        assertEquals(2, ui.boardViewModel.size())
        assertEquals("A", issues.find { it.qualifiedName == "A" }!!.qualifiedName)
        assertEquals("C", issues.find { it.qualifiedName == "C" }!!.qualifiedName)
    }

    @Test
    fun errorCorrection() = testProjectSession {
        val ui = SysMDViewModel()
        ui.sessionId = this.id

        loadKerML("""type A :> B; """, Runlevel.MODEL)

        ui.boardViewModel.update()

        assertEquals(true, ui.boardViewModel.contains(issue = status.issues.first()))
        assertEquals(1, ui.boardViewModel.size())

        status.issues.clear()
        ui.boardViewModel.clear()
        loadKerML("""
            type B :> Base::Anything; 
        """)
        solver.propagate()
        ui.boardViewModel.update()
        assertEquals(true, ui.boardViewModel.isEmpty())
    }

    @Test
    fun size() = testProjectSession {
        val ui = SysMDViewModel()
        ui.sessionId = this.id

        loadKerML("""
            type C :> D; 
            type A :> B; 
        """, Runlevel.MODEL)

        ui.boardViewModel.update()

        assertEquals(2, ui.boardViewModel.size())

        status.issues.clear()
        ui.boardViewModel.clear()

        loadKerML("""type B :> Base::Anything; """)
        solver.propagate()
        ui.boardViewModel.update()
        assertEquals(1, ui.boardViewModel.size())
        status.issues.clear()
        ui.boardViewModel.clear()

        loadKerML("""
            type D :> Base::Anything; 
        """)
        solver.propagate()

        ui.boardViewModel.update()

        assertEquals(true, ui.boardViewModel.isEmpty())
    }


    @Test
    fun issue235elementReclassification() = testProjectSession("Occurrences") {
        val ui = SysMDViewModel()
        ui.sessionIdState.value = this.id
        loadKerML("""
            class A :> B; 
            class B; 
            class A :> C; 
        """, Runlevel.MODEL)
        assertEquals(1, status.issues.size, status.issues.toString())
        assertTrue(status.issues.any { it.message.contains("C") }, "Error message is expected reporting C as undefined")
        ui.boardViewModel.update()

        //The Board must not be empty as the reassignment of "A" to an unknown
        // superclass is recognized as error
        assertFalse(ui.boardViewModel.isEmpty())
    }

    @Ignore
    @Test
    fun removeElement() = with(SysMDViewModel()) {
        val session = sessionService.getSession(sessionId) !!

        session.loadKerML("""
            type C :> D; 
            type A :> B;
        """)
        session.solver.propagate()

        boardViewModel.update()
        assertEquals(2, boardViewModel.size())
        boardViewModel.removeElement(qualifiedName = "A")
        assertEquals(1, boardViewModel.size())
       // val error = agenda.issues().first()
        // agenda.removeElement(error.qualifiedName, error.textualRepresentation, error.line)
        assertEquals(0, boardViewModel.size())
        boardViewModel.update()
        assertEquals(2, boardViewModel.size())
    }
}
package services

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.rest.AgilaRepository
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AgendaTest {

    /**
     * Simple check if all elements with errors from same cell/textual representation appear in agenda
     */
    @Test
    fun elementsInAgenda() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD(
            """
            class A specializes B; 
        """.trimIndent()
        )
        session.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()
        agenda.issues().forEach { println(it) }

        assertEquals(1, agenda.size())
        assertEquals(false, agenda.isEmpty())
        assertEquals(true, agenda.contains("A"))
    }

    @Test
    fun partDefSuperclassNotIdentified() = testSession("ScalarValues") {
        loadSysMD("""
          package test {
                import ScalarValues;
                part def testCPU isA Controller {
                    ;     
                }
           }
        """.trimIndent())
        initialize()
        // propagate()
        assertTrue(status.exceptions.isNotEmpty())
    }

    /**
     * Check if all errors related with a single element occur in agenda
     */
    @Test
    fun multipleErrorOnSingleElement() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD("""
            class A isA B; 
        """.trimIndent())
        session.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(1, agenda.size())
        assertEquals(true, agenda.contains("A"))

        session.status.exceptions.clear()
        agenda.clear()

        session.loadSysMD("""
            class A isA C; 
        """.trimIndent())
        session.propagate()

        agenda.update()

        assertEquals(1, agenda.size())
        assertEquals(true, agenda.contains("A"))
    }

    @Test
    fun statusIsNotNullError() = testSession {
        val sysMdViewModel = SysMDViewModel(session = this)
        loadSysMD("""
            class A isA B; 
            class B isA A; 
        """.trimIndent())
        val agenda = sysMdViewModel.agenda
        agenda.update()
        assertNotEquals(null, agenda.status)
    }

    @Test
    fun emptyOnNoInput() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        val agenda = sysMdViewModel.agenda

        assertEquals(0, agenda.size())
        assertEquals(true, agenda.isEmpty())

        agenda.update()

        assertEquals(0, agenda.size())
        assertEquals(true, agenda.isEmpty())

        session.loadSysMD(
            """
        """.trimIndent()
        )
        session.propagate()

        agenda.update()

        assertEquals(0, agenda.size())
        assertEquals(true, agenda.isEmpty())
    }

    @Test
    fun emptyAfterClear() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD(
            """
            class A isA B; 
            class B isA A; 
        """.trimIndent())

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(1, agenda.size())
        agenda.clear()
        assertEquals(0, agenda.size())
    }

    @Test
    fun getIssues() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD(
            """
            class A isA B; 
            class C isA D; 
        """.trimIndent()
        )
        session.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        val issues = agenda.issues()

        assertEquals(2, agenda.size())
        assertEquals("A", issues.find { it.qualifiedName == "A" }!!.qualifiedName)
        assertEquals("C", issues.find { it.qualifiedName == "C" }!!.qualifiedName)
    }

    @Test
    fun errorCorrection() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD("""
            class A isA B; 
        """.trimIndent())
        session.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(true, agenda.contains("A"))
        assertEquals(1, agenda.size())

        session.status.exceptions.clear()
        agenda.clear()
        session.loadSysMD("""
            class B; 
        """.trimIndent())
        session.propagate()

        agenda.update()

        assertEquals(true, agenda.isEmpty())
    }

    @Test
    fun size() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD("""
            class C isA D; 
            class A isA B; 
        """.trimIndent())
        session.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(2, agenda.size())

        session.status.exceptions.clear()
        agenda.clear()

        session.loadSysMD("""
            class B; 
        """.trimIndent())
        session.propagate()

        agenda.update()


        assertEquals(1, agenda.size())

        session.status.exceptions.clear()
        agenda.clear()

        session.loadSysMD("""
            class D; 
        """.trimIndent())
        session.propagate()

        agenda.update()

        assertEquals(true, agenda.isEmpty())
    }

    /**
     * TODO!
     * The model below must throw an error.
     * To fix the issue, we need to add an update function for elements of the model.
     * It would have to compare two versions for equality and/or a valid refinement.
     */
    @Test @Disabled
    fun issue235elementReclassification() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD("""
            class A :> B; 
            class B; 
            class A :> C; 
        """.trimIndent())
        session.propagate()
        assertTrue(session.status.exceptions.isNotEmpty())
        val agenda = sysMdViewModel.agenda
        agenda.update()

        //The Agenda must not be empty as the reassignment of "A" to an unknown
        // superclass is recognized as error
        assertFalse(agenda.isEmpty())
    }

    @Test
    fun removeElement() {
        val session = SessionManager.startSession()
        AgilaRepository.internalSessionId = session.id
        val sysMdViewModel = SysMDViewModel(session = session)

        session.loadSysMD("""
            class C isA D; 
            class A isA B;
        """.trimIndent())
        session.propagate()

        val agenda = sysMdViewModel.agenda
        agenda.update()

        assertEquals(2, agenda.size())

        agenda.removeElement(qualifiedName = "A")

        assertEquals(1, agenda.size())

        val error = agenda.issues().first()

        agenda.removeElement(error.qualifiedName, error.textualRepresentation, error.line)

        assertEquals(0, agenda.size())

        agenda.update()

        assertEquals(2, agenda.size())
    }
}
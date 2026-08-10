package ui

import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.util.Unresolved
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import util.assertNoIssues
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
        assertEquals(1, status.issues.size, status.issues.toString())
        ui.boardViewModel.update()

        assertEquals(1, ui.boardViewModel.size())
        // assertEquals(true, agenda.contains("A"))

        status.issues.clear()
        ui.boardViewModel.clear()

        loadKerML("""type A :> C; """)
        solver.propagate()
        assertEquals(1, status.issues.size, status.issues.toString())
        ui.boardViewModel.update()

        /* The previous implementation merged both specializations into `type A :> B, C`.
            This is impractical, e.g. if the user types `A :> Int`, and then corrects themselves to `A :> Integer`,
            a sane tool should accept that new supertype instead of continuously producing errors related to the `Int` typo.

            The new implementation makes the later specialization overwrite the previous specialization entirely.
            Case like `A :> B, C` edited to `A :> B` still need to be investigated.
         */
        //assertEquals(2, ui.boardViewModel.size())
        assertEquals(1, ui.boardViewModel.size())
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
        val aId = global.resolve("A")?.member<Class>()?.elementId
        val cId = global.resolve("C")?.member<Class>()?.elementId

        assertEquals(2, ui.boardViewModel.size())
        assertNotNull(issues.find { it.element == aId})
        assertNotNull(issues.find { it.element == cId })
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
    fun issue235elementReclassification() = testProjectSession {
        val ui = SysMDViewModel()
        ui.sessionIdState.value = this.id
        loadKerML("""
            classifier A :> B; 
            classifier B; 
        """, Runlevel.MODEL)
        assertNoIssues()

        fun classifier(name : String) = global.resolve(name).let {
            assertNotNull(it, "classifier '$name' got deleted")
        }.memberElement.let {
            assertIs<Classifier>(it, "'$name' was assigned the wrong type")
        }

        assertEquals(listOf(classifier("B")), classifier("A").supertypes(excludeImplied = true))

        loadKerML("""
            classifier A :> C;
        """.trimIndent())

        classifier("A").supertypes(excludeImplied = true).filterIsInstance<Unresolved>().let {
            assertNotEquals(emptyList(), it, "new specialization was not respected")
            assertEquals("C", it.single().relativeName)
        }

        assertNoIssues {
            "C" !in it.message
        }

        val expected = status.issues.filter { "C" in it.message }
        assertNotEquals(emptyList(), expected, "C should be reported as undefined")
        assertEquals(1, expected.size, "Duplicate errors:\n${expected.joinToString("\n")}")

        ui.boardViewModel.update()

        //The Board must not be empty as the reassignment of "A" to an unknown
        // superclass is recognized as error
        assertFalse(ui.boardViewModel.isEmpty())
    }
}
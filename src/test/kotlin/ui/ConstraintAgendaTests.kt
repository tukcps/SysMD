package ui

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testProjectSession
import kotlin.test.*

class ConstraintAgendaTests {

    @Test
    fun unfulfilledAssertConstraintShowsInAgendaWithYellowColor() = testProjectSession("Constraints", "ScalarValues") {
        val ui = SysMDViewModel().also { it.sessionId = this.id }

        loadSysMLv2("""
            attribute v1: ScalarValues::Real = 10.0;
            attribute v2: ScalarValues::Real = 5.0;
            assert constraint c1 { v1 < v2 }
        """, Runlevel.ALL)

        assertEquals(1, status.issues.size, "Expected 1 issue for unfulfilled constraint: ${status.issues}")
        val issue = status.issues.first()
        assertEquals(Issue.Kind.WARN_INCONSISTENCY, issue.kind)
        assertTrue(issue.message.contains("c1"))
        assertTrue(issue.message.contains("not fulfilled") || issue.message.contains("not satisfiable"))

        ui.boardViewModel.update()
        assertEquals(1, ui.boardViewModel.size())
        assertFalse(ui.boardViewModel.isEmpty())

        val agendaIssue = ui.boardViewModel.issues().first()
        assertEquals(AppTheme.colors.warning, agendaIssue.errorTypeToColor(agendaIssue.issue!!.kind))
        assertEquals("Inconsistency", agendaIssue.getTitle())
    }

    @Test
    fun fulfilledAssertConstraintDoesNotShowInAgenda() = testProjectSession("Constraints", "ScalarValues") {
        val ui = SysMDViewModel().also { it.sessionId = this.id }

        loadSysMLv2("""
            attribute v1: ScalarValues::Real = 2.0;
            attribute v2: ScalarValues::Real = 5.0;
            assert constraint c1 { v1 < v2 }
        """, Runlevel.ALL)

        assertNoIssues()
        ui.boardViewModel.update()
        assertTrue(ui.boardViewModel.isEmpty())
        assertEquals(0, ui.boardViewModel.size())
    }

    @Test
    fun unfulfilledAssertNotConstraintShowsInAgenda() = testProjectSession("Constraints", "ScalarValues") {
        val ui = SysMDViewModel().also { it.sessionId = this.id }

        loadSysMLv2("""
            attribute v1: ScalarValues::Real = 2.0;
            attribute v2: ScalarValues::Real = 5.0;
            assert not constraint cNot { v1 < v2 }
        """, Runlevel.ALL)

        assertEquals(1, status.issues.size, "Expected 1 issue: ${status.issues}")
        val issue = status.issues.first()
        assertEquals(Issue.Kind.WARN_INCONSISTENCY, issue.kind)
        assertTrue(issue.message.contains("cNot"))

        ui.boardViewModel.update()
        assertEquals(1, ui.boardViewModel.size())
        val agendaIssue = ui.boardViewModel.issues().first()
        assertEquals(AppTheme.colors.warning, agendaIssue.errorTypeToColor(agendaIssue.issue!!.kind))
    }

    @Test
    fun rangeContradictionShowsInAgendaWithYellowColor() = testProjectSession("Ranges") {
        val ui = SysMDViewModel().also { it.sessionId = this.id }

        loadSysMLv2("""
            attribute a: Ranges::RealInRange {:>> range = -10.0 .. -5.0;}
            attribute b: ScalarValues::Real = sqrt(a);
        """, Runlevel.ALL)

        assertEquals(1, status.issues.size, "Expected 1 issue for invalid sqrt range: ${status.issues}")
        val issue = status.issues.first()
        assertEquals(Issue.Kind.WARN_INCONSISTENCY, issue.kind)

        ui.boardViewModel.update()
        assertEquals(1, ui.boardViewModel.size())
        val agendaIssue = ui.boardViewModel.issues().first()
        assertEquals(AppTheme.colors.warning, agendaIssue.errorTypeToColor(agendaIssue.issue!!.kind))
    }

    @Test
    fun unfulfilledConstraintInSubclassShowsSubclassInfo() = testProjectSession("Parts", "Constraints", "ScalarValues") {
        val ui = SysMDViewModel().also { it.sessionId = this.id }

        loadSysMLv2("""
            part def Vehicle {
                attribute speed: ScalarValues::Real;
                assert constraint maxSpeed { speed <= 100.0 }
            }
            part def FastVehicle :> Vehicle {
                :>> speed = 150.0;
            }
        """, Runlevel.ALL)

        ui.boardViewModel.update()
        assertFalse(ui.boardViewModel.isEmpty())

        val agendaIssues = ui.boardViewModel.issues()
        val fastVehicleIssue = agendaIssues.firstOrNull { it.issue?.message?.contains("FastVehicle") == true }
        assertNotNull(fastVehicleIssue, "Expected an issue mentioning subclass FastVehicle in agenda: ${agendaIssues.map { it.issue?.message }}")
        assertEquals(AppTheme.colors.warning, fastVehicleIssue.errorTypeToColor(fastVehicleIssue.issue!!.kind))
        assertTrue(fastVehicleIssue.issue.message.contains("maxSpeed"))
        assertTrue(fastVehicleIssue.issue.message.contains("FastVehicle"))
    }
}

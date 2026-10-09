package ui.viewmodel

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.ProjectData.Companion.fromInterchangeFiles
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.ui.paneleft.projectlist.ProjectViewModel
import com.github.tukcps.sysmd.ui.viewmodel.CompileProgress
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import kotlinx.io.files.Path
import util.mockup.MockupSysMDProjectService
import java.io.File
import java.util.Collections
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests of [SysMDViewModel.compileProject], i.e., of what the buttons "Compile" and "Solve" do:
 * the project is compiled in the background into a new session, to which the UI switches at the end.
 */
class CompileProjectTests {

    private val notebook = """
        # A notebook

        ```SysML
        package Demo {
            private import ScalarValues::*;
            part def Vehicle {
                attribute mass: Real = 1500.0;
                attribute payload: Real = 500.0;
                attribute total: Real = mass + payload;
            }
        }
        ```

        Some documentation between the cells.

        ```SysML::Demo
        part def Truck :> Vehicle {
            :>> payload = 2000.0;
        }
        ```
    """.trimIndent()

    /** Runs a test with a project that is opened as in the UI; the project is in a temporary folder. */
    private fun withOpenProject(test: (SysMDViewModel, ProjectViewModel) -> Unit) {
        val directory = createTempDirectory("sysmd-compile-test").toFile()
        try {
            File(directory, "Demo.md").writeText(notebook)
            File(directory, ".project.json").writeText(
                """{ "name" : "Demo", "version" : "*", "description" : "", "usage" : [ ], "id" : "8d3cf6b4-6f0c-4f2f-9d0e-1f2a3b4c5d6e" }"""
            )
            File(directory, ".meta.json").writeText(
                """{ "index" : { "Demo.md" : "Demo.md" }, "created" : "2020-01-01T00:00:00Z" }"""
            )
            val service = MockupSysMDProjectService()
            SessionManager.projectService = service
            service.setProjects(listOf(assertNotNull(fromInterchangeFiles(Path(directory.absolutePath)))))

            val sysMDViewModel = SysMDViewModel()
            val projectViewModel = sysMDViewModel.projectListViewModel.projectViewModels.value.single()
            projectViewModel.createProjectSession()
            try {
                test(sysMDViewModel, projectViewModel)
            } finally {
                SessionManager.kill(sysMDViewModel.sessionId)
            }
        } finally {
            directory.deleteRecursively()
        }
    }

    /** The result lines that the cells of all tabs show. */
    private fun SysMDViewModel.resultLines(): List<String> =
        editorTabsViewModel.editorTabs.flatMap { tab -> tab.cells.flatMap { cell -> cell.displayItems.map { it.text } } }

    @Test
    fun compileProjectShowsTheSameAsCompilingOnTheCallingThread() = withOpenProject { sysMDViewModel, projectViewModel ->
        sysMDViewModel.reset()
        projectViewModel.compile(Runlevel.ALL)
        val expected = sysMDViewModel.resultLines()
        val expectedIssues = sysMDViewModel.boardViewModel.issues().map { it.issue?.message }
        assertTrue(expected.any { "total = 2000" in it }, "Vehicle::total is not shown in $expected")
        assertTrue(expected.any { "total = 3500" in it }, "Truck::total is not shown in $expected")

        runBlocking { sysMDViewModel.compileProject(Runlevel.ALL) }

        assertEquals(expected, sysMDViewModel.resultLines())
        assertEquals(expectedIssues, sysMDViewModel.boardViewModel.issues().map { it.issue?.message })
    }

    @Test
    fun compileProjectSwitchesToANewSessionAndReportsProgress() = withOpenProject { sysMDViewModel, _ ->
        val previousSessionId = sysMDViewModel.sessionId
        val seen = Collections.synchronizedList(mutableListOf<CompileProgress?>())

        runBlocking {
            val collector = launch(Dispatchers.Unconfined) { sysMDViewModel.compileProgress.collect { seen += it } }
            sysMDViewModel.compileProject(Runlevel.ALL)
            collector.cancel()
        }

        // The UI works with a new session that holds the solved model; the previous one is gone.
        assertNotEquals(previousSessionId, sysMDViewModel.sessionId)
        assertNull(SessionManager.getSession(previousSessionId))
        val session = assertNotNull(SessionManager.getSession(sysMDViewModel.sessionId))
        assertNotNull(session.solver.getVariable("Demo::Truck::total"))

        // Progress was reported from the start to the end, and is cleared when done.
        assertNull(sysMDViewModel.compileProgress.value)
        val progress = seen.filterNotNull()
        assertEquals(CompileProgress.preparing, progress.first())
        assertEquals(CompileProgress.showingResults, progress.last())
        assertTrue(progress.any { it.message == "Compiling cell 2 of 2" }, "cells are not reported in $progress")
        assertTrue(progress.any { it.message == "Solving" }, "solving is not reported in $progress")
        assertEquals(progress.map { it.fraction }.sorted(), progress.map { it.fraction }, "progress goes backwards")
        assertTrue(progress.all { it.fraction in 0f..1f })
    }

    @Test
    fun compileOnlyBuildsTheModel() = withOpenProject { sysMDViewModel, _ ->
        val seen = Collections.synchronizedList(mutableListOf<CompileProgress?>())
        runBlocking {
            val collector = launch(Dispatchers.Unconfined) { sysMDViewModel.compileProgress.collect { seen += it } }
            sysMDViewModel.compileProject(Runlevel.MODEL)
            collector.cancel()
        }
        val progress = seen.filterNotNull()
        assertTrue(progress.any { it.message == "Checking model" }, "model check is not reported in $progress")
        assertTrue(progress.none { it.message == "Solving" }, "nothing is solved for runlevel MODEL")
        assertEquals(progress.map { it.fraction }.sorted(), progress.map { it.fraction }, "progress goes backwards")
        assertNotNull(SessionManager.getSession(sysMDViewModel.sessionId)?.global?.resolve("Demo::Truck"))
    }

    @Test
    fun compileProjectIsDiscardedIfTheUserMovedToAnotherSession() = withOpenProject { sysMDViewModel, projectViewModel ->
        val sessionsBefore = SessionManager.getAllSessions().toSet()
        val project = assertNotNull(projectViewModel.project)
        val otherSession = SessionManager.createSession(project, *SessionManager.SYSML_LIBRARIES)
        try {
            runBlocking {
                val compile = launch { sysMDViewModel.compileProject(Runlevel.ALL) }
                yield() // compileProject runs until it waits for the background work
                assertNotNull(sysMDViewModel.compileProgress.value)
                // The user opens another session before the result is taken over (which needs this thread).
                sysMDViewModel.sessionId = otherSession.id
                compile.join()
            }
            // The UI keeps its session and shows no results of the discarded compile run.
            assertEquals(otherSession.id, sysMDViewModel.sessionId)
            assertNull(sysMDViewModel.compileProgress.value)
            assertTrue(sysMDViewModel.resultLines().isEmpty())
            // The session built in the background is not left behind.
            assertEquals(sessionsBefore + otherSession, SessionManager.getAllSessions().toSet())
        } finally {
            SessionManager.kill(otherSession.id)
            sessionsBefore.forEach { SessionManager.kill(it.id) }
        }
    }

    @Test
    fun compileProjectDoesNothingWhileAnotherOneRuns() = withOpenProject { sysMDViewModel, _ ->
        runBlocking {
            val first = launch { sysMDViewModel.compileProject(Runlevel.ALL) }
            yield() // the first call now waits for its background work
            val sessionId = sysMDViewModel.sessionId
            sysMDViewModel.compileProject(Runlevel.ALL) // returns immediately
            assertEquals(sessionId, sysMDViewModel.sessionId)
            assertNotNull(sysMDViewModel.compileProgress.value)
            first.join()
            assertNotEquals(sessionId, sysMDViewModel.sessionId)
        }
    }
}

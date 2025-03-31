package util

import com.github.tukcps.sysmd.services.repositories.local.ProjectData.Companion.fromInterchangeFiles
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.sessions
import com.github.tukcps.sysmd.services.session.loadLibrary
import util.mockup.MockupSysMDProjectService
import kotlin.io.path.toPath


/**
 * Starts a fake session for testing purposes.
 * - It creates a mock repository for projects
 * - The session is terminated after the test has run.
 * The parameters permit setting up test cases:
 * @param arrangement arrangements of in-itself consistent preloaded libraries that are loaded into the test session prior to the test.
 * @param initialize whether to run initialize or not.
 * @param testDirectory the path where a test project is located in the test resources
 * @param test a lambda with the test.
 */
inline fun testSession(
    vararg arrangement: String,
    initialize: Boolean = true,
    testDirectory: String = "/testSession",
    test: SessionImplementation.() -> Unit,
) = SessionImplementation(
    libraries = arrangement.toMutableList(),
).run {
    SessionManager.projectService = MockupSysMDProjectService()

    val dir = (javaClass.getResource(testDirectory)?.toURI()?.toPath())
        ?: javaClass.classLoader.getResource(testDirectory)?.toURI()?.toPath()
                ?: throw Exception("Could not load project from $testDirectory")

    project = fromInterchangeFiles(dir)
        ?: throw Exception("Could not create project from .project.json and .meta.json in $testDirectory")

    (SessionManager.projectService as MockupSysMDProjectService).setProjects(listOf(project!!))
    project!!.id = id
    sessions[id] = this
    settings.initialize = initialize

    arrangement.forEach { arrangement ->
        loadLibrary(arrangement)
    }

    test()
    sessions.remove(id)
    settings.initialize = initialize
}


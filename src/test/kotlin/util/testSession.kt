package util

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.ProjectData.Companion.fromInterchangeFiles
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.implementation.ProjectSessionImplementation
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import com.github.tukcps.sysmd.services.session.loadLibrary
import kotlinx.io.files.Path
import util.mockup.MockupSysMDProjectService
import java.nio.file.Paths


/**
 * Starts a mockup project session for testing purposes.
 * - It creates a mock repository for projects
 * - The session is terminated after the test has run.
 * The parameters permit setting up test cases:
 * @param arrangement arrangements of in-itself consistent preloaded libraries that are loaded into the test session prior to the test.
 * @param runlevel how far to initialize the session.
 * @param testDirectory the path where a test project is located in the test resources
 * @param test a lambda with the test.
 */
inline fun testProjectSession(
    vararg arrangement: String,
    runlevel: Runlevel = Runlevel.NAMES_RESOLVED,
    testDirectory: String = "/testSession",
    test: ProjectSessionImplementation.() -> Unit,
)  {
    // For test, only use mockup project service
    SessionManager.projectService = MockupSysMDProjectService()

    val dir = (SessionManager.javaClass.getResource(testDirectory)?.toURI() )
        ?: SessionManager.javaClass.classLoader.getResource(testDirectory)?.toURI()
                ?: throw Exception("Could not load project from $testDirectory")

    val project = fromInterchangeFiles(Path(Paths.get(dir).toString()) )
        ?: throw Exception("Could not create project from .project.json and .meta.json in $testDirectory")

    with (SessionManager.createSession(project = project, mutableListOf(), runlevel) ) {
        (SessionManager.projectService as MockupSysMDProjectService).setProjects(listOf(project))
        arrangement.forEach { arrangement -> loadLibrary(arrangement) }
        initialize(settings.runlevel)
        (this as ProjectSessionImplementation).test()
        SessionManager.kill(id)
    }
}

/**
 * Starts a mockup project session for testing purposes.
 * - It creates a mock repository for projects
 * - The session is terminated after the test has run.
 * The parameters permit setting up test cases:
 * @param arrangement arrangements of in-itself consistent preloaded libraries that are loaded into the test session prior to the test.
 * @param runlevel how far to initialize the session.
 * @param test a lambda with the test.
 */
inline fun testSession(
    vararg arrangement: String,
    runlevel: Runlevel = Runlevel.NAMES_RESOLVED,
    test: SessionImplementation.() -> Unit,
)  {

    with (SessionImplementation(libraries = arrangement.toList()) ) {
        settings.runlevel = runlevel
        arrangement.forEach { arrangement -> loadLibrary(arrangement) }
        initialize(settings.runlevel)
        this.test()
    }
}
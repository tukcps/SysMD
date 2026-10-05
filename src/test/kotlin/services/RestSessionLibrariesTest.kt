package services

import com.github.tukcps.sysmd.rest.ProjectImplementation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.implementation.SessionServiceImplementation
import com.github.tukcps.sysmd.services.repositories.local.Language
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

/**
 * `POST /session` goes through `SessionService.createSession`. It used to create a session without any standard library,
 * so every model failed with "Semantic analysis failed (java.lang.NullPointerException)".
 */
class RestSessionLibrariesTest {
    @Test
    fun sessionCreatedByTheServiceHasTheStandardLibraries() {
        val project = ProjectData(ProjectImplementation(defaultBranchId = Uuid.random()))
        val service = SessionServiceImplementation()
        val session = service.createSession(project)
        try {
            val status = service.updateModel(
                session.id,
                "attribute a: ScalarValues::Real = 2.0; attribute b: ScalarValues::Real = a * 3.0;",
                Language.SYS_ML, null, Runlevel.ALL,
            )
            assertTrue(status!!.issues.isEmpty(), "issues: ${status.issues}")
            val b = service.getVariables(session.id)!!.single { it.path?.endsWith("b") == true }
            assertTrue(b.valueStr.startsWith("6"), "b = ${b.valueStr}")
        } finally {
            SessionManager.kill(session.id)
        }
    }
}

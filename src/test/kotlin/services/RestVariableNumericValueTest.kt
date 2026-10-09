package services

import com.github.tukcps.sysmd.rest.ProjectImplementation
import com.github.tukcps.sysmd.rest.entities.response.VariableResponse
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.implementation.SessionServiceImplementation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/** The Representer rounds the display string; the REST response must additionally carry the numeric value. */
class RestVariableNumericValueTest {
    @Test
    fun responseCarriesNumericValueNotRoundedByTheDisplayString() {
        val service = SessionServiceImplementation()
        val session = service.createSession(ProjectData(ProjectImplementation(defaultBranchId = Uuid.random())))
        try {
            val status = service.updateModel(
                session.id,
                "attribute a: ScalarValues::Real = 2.000001; attribute flag: ScalarValues::Boolean = true;",
                Language.SYS_ML, null, Runlevel.ALL,
            )
            assertEquals(emptyList(), status!!.issues.toList())
            val variables = service.getVariables(session.id)!!
            val a = VariableResponse(variables.single { it.path?.endsWith("a") == true })
            assertNotNull(a.min); assertNotNull(a.max)
            assertEquals(2.000001, a.min!!, 1e-12)
            assertEquals(2.000001, a.max!!, 1e-12)
            val flag = VariableResponse(variables.single { it.path?.endsWith("flag") == true })
            assertNull(flag.min); assertNull(flag.max)
        } finally {
            SessionManager.kill(session.id)
        }
    }
}

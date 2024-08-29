package kermltests

import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypeTests {

    @Test
    fun testTwoSupertypes() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            type A :> Base::Anything { feature a; }
            type B :> Base::Anything { feature b; }
            type AB :> A, B; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val ab = global.resolve<TypeImplementation>("AB")
        assertNotNull(ab)
        assertEquals(2, ab.allSupertypes().size)
    }
}
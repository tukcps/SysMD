package models.kerml

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.compiler.loadSysMDFromFile
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.resolve.resolve
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SessionTests {


    /**
     * Initialization test
     */
    @Test
    fun initScalarValuesTest() = testSession("ScalarValues", catchExceptions = false, initialize = false) {
        initialize()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val real = global.resolve<Element>("ScalarValues::Real")
        assertNotNull(real)
    }

    @Test
    fun initScalarValuesModelOK() = testSession(catchExceptions = false, loadKerML = false) {
        loadSysMD("""
                package ScalarValues {
                    datatype ScalarValue;
                    datatype String specializes ScalarValue;
                    datatype Number specializes ScalarValue;
                    datatype Real specializes Number;
                    datatype Integer specializes Number;
                    datatype Natural specializes Integer; // TODO: (0 .. *)  
                    datatype Boolean specializes Number;
                    datatype Requirement specializes Boolean;
                    datatype Performance specializes Real;
                    datatype Quality specializes Real; 
                }
        """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val real = global.resolve<Element>("ScalarValues::Real")
        assertNotNull(real)
    }

    /**
     * Test of standard use case:
     * - start session, commit session, close session.
     * No specific features are tested here, just the proper implementation of the protocol.
     */
    @Test
    fun useCaseTest() {
        testSession {
            loadSysMDFromFile("ScalarValues.md")
            SessionManager.commitSession(this, UUID.randomUUID())
            SessionManager.kill(this.id)
        }
    }
}
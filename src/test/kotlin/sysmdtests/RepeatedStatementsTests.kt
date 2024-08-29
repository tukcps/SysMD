package sysmdtests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RepeatedStatementsTests {

    /**
     * Repeated classifications shall not change the model.
     * Also take care of unnamed owned elements that are owned by 2nd classification.
     * Here: Specialization element; must be merged as well.
     */
    @Test
    fun repeatedClassDeclaration() = testSession(loadKerML = false) {
        settings.catchExceptions=false
        loadSysMD(""" 
                class A; 
                class A; 
        """)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun repeatedClassDeclaration2() = testSession(loadKerML = false) {
        settings.catchExceptions=false
        loadSysMD("class A;")
        loadSysMD("class A;")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun repeatedFeatureDeclaration() = testSession(loadKerML = false) {
        settings.catchExceptions=false
        loadSysMD(""" 
                package ScalarValues {
                    datatype Integer; 
                }
                feature A; 
                feature A; 
        """)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }
}

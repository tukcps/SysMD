package sysmdtests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StringTests {

    /**
     * Syntactic variants should be parsed correctly.
     */
    @Test
    fun stringSyntax() = testSession {
        +"""feature label1: ScalarValues::String = "string value1";"""
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val label1 = global.resolve<Feature>("label1")!!.variable!!
        assertEquals(label1.dependency, "\"string value1\"")
    }
}
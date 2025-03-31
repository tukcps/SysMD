package sysmdtests

import util.mockup.loadKerML
import kotlin.test.*
import util.testSession

class RepeatedStatementsTests {

    /**
     * Repeated classifications shall not change the model.
     * Also take care of unnamed owned elements that are owned by 2nd classification.
     * Here: Specialization element; must be merged as well.
     */
    @Test
    fun repeatedClassDeclaration() = testSession("Occurrences") {
        settings.catchExceptions=false
        loadKerML(""" 
                class A; 
                class A; 
        """)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun repeatedClassDeclaration2() = testSession("Occurrences") {
        settings.catchExceptions=false
        loadKerML("class A;")
        loadKerML("class A;")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun repeatedFeatureDeclaration() = testSession("ScalarValues") {
        settings.catchExceptions=false
        loadKerML(""" 
                feature A; 
                feature A; 
        """)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }
}

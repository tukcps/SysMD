package compiler

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class RepeatedStatementsTests {

    /**
     * Repeated classifications shall not change the model.
     * Also take care of unnamed owned elements that are owned by 2nd classification.
     * Here: Specialization element; must be merged as well.
     */
    @Test
    fun repeatedClassDeclaration() = testSession("Occurrences") {
        settings.reportDoubleNames = false
        loadKerML(""" 
            class A; 
            class A; 
        """)
        assertNoIssues()
    }

    @Test
    fun repeatedClassDeclaration2() = testSession("Occurrences") {
        settings.reportDoubleNames = false
        loadKerML("class A;")
        loadKerML("class A;")
        assertNoIssues()
    }

    @Test
    fun repeatedFeatureDeclaration() = testSession("ScalarValues") {
        settings.reportDoubleNames = false
        loadKerML(""" 
            feature A; 
            feature A; 
        """)
        assertNoIssues()
    }
}
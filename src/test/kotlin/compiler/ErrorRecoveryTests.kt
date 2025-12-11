package compiler

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests of error recovery capabilities of the parsers
 */
class ErrorRecoveryTests {

    @Test
    fun errorRecoveryAfterSemicolon() = testSession("ScalarValues") {
        loadSysMLv2("""
            package x {
                attribute a; 
                error b; 
                attribute c; 
            } 
        """)
        assertEquals(1, status.issues.size, status.issues.toString())
        assertNotNull(global.resolve("x::a"))
        assertNotNull(global.resolve("x::c"))
    }

    @Test
    fun errorRecoveryAfterClosingCurlyBrace() = testSession("ScalarValues") {
        loadSysMLv2("""
            package x {
                attribute a; 
                error b; 
            }
            attribute c; 
        """)
        assertEquals(1, status.issues.size, status.issues.toString())
        assertNotNull(global.resolve("x::a"))
        assertNotNull(global.resolve("c"))
    }
}
package compiler.sysml.examples

import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Refer to Section: 7.4 - Annotations
 * Language Specification Document: https://www.omg.org/spec/SysML/2.0/Beta2/Language/PDF
 */

class AnnotationTests {

    @Test
    fun testComment() = testSession {
        loadSysMLv2("""
        comment Comment1
        /*This is a comment.*/
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun testOneLineComment() = testSession {
        loadSysMLv2("""
        /*This is a comment.*/
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun testCommentAbout() = testSession("Parts") {
        loadSysMLv2("""
            part part1 {
                part attribute1; 
            }
            comment about part1::attribute1
            /* The annotated element
             * is attribute1. */
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun testDocumentation() = testSession {
        loadSysMLv2("""
        doc Document1
        /*This is documentation.*/
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun testCommentAnnotation() = testSession("Parts") {
        loadSysMLv2("""
            part part1 {
                part attribute1; 
            }
            comment about part1::attribute1
            /* The annotated element * is attribute1. */
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

}
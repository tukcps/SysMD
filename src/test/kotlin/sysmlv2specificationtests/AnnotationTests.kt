package sysmlv2specificationtests

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
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun testOneLineComment() = testSession {
        loadSysMLv2("""
        /*This is a comment.*/
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun testCommentAbout() = testSession {
        loadSysMLv2("""
        comment about part1::attribute1
        /* The annotated element
        * is attribute1. */
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun testDocumentation() = testSession {
        loadSysMLv2("""
        doc Document1
        /*This is documentation.*/
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun testCommentAnnotation() = testSession {
        loadSysMLv2("""
        comment about part1::attribute1
        /* The annotated element * is attribute1. */
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

}
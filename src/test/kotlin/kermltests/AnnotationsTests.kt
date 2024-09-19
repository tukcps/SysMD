package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnnotationsTests {
    @Test
    fun testComment() = testSession(loadKerML = false) {
        loadSysMD("""
            comment /* comment on something */ 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals( "comment on something", comment.body)
    }

    @Test
    fun testCommentWidhId() = testSession(loadKerML = false) {
        loadSysMD("""
            comment test /* comment on something */ 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals( "comment on something", comment.body)
        assertEquals("test", comment.name)
    }

    @Test
    fun testCommentWidhIdOnSomething() = testSession(loadKerML = false) {
        loadSysMD("""
            class x; 
            comment test about x /* comment on something */ 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals( "comment on something", comment.body)
        assertEquals("test", comment.name)
        assertTrue(comment.ownedElement.first().ref is Annotation)
        val annotation = comment.ownedElement.first().ref as Annotation
        assertEquals("x", annotation.annotatedElement.ref?.name)
    }

    @Test
    fun testCommentWithIdAboutSomething2() = testSession(loadKerML = false) {
        loadSysMD("""
            class x; 
            class y; 
            comment test about x, y /* comment on something */
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals( "comment on something", comment.body)
        assertEquals("test", comment.name)
        assertTrue(comment.ownedElement.first().ref is Annotation)
        val annotation1 = comment.ownedElement.first().ref as Annotation
        assertEquals("x", annotation1.annotatedElement.ref?.name)
        val annotation2 = comment.ownedElement[1].ref as Annotation
        assertEquals("y", annotation2.annotatedElement.ref?.name)
    }


    @Test
    fun testDocWidhId() = testSession(loadKerML = false) {
        loadSysMD("""
            doc test /* comment on something */ 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val doc = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(doc is Documentation)
        assertEquals( "comment on something", doc.body)
        assertEquals("test", doc.name)
    }

    @Test
    fun testRepWidhId() = testSession(loadKerML = false) {
        loadSysMD("""
            rep test /* code on something */ 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals( "code on something", rep.body)
        assertEquals("test", rep.name)
    }


    @Test
    fun testRepWidhIdAndLanguage() = testSession(loadKerML = false) {
        loadSysMD("""
            rep test language sysmd /* code on something */ 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals( "code on something", rep.body)
        assertEquals("test", rep.name)
    }


    @Test
    fun testRepWidhIdAndLanguage2() = testSession(loadKerML = false) {
        loadSysMD("""
            rep test language ltl /* ltl expressions */ 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals( "ltl", rep.language)
        assertEquals("ltl expressions", rep.body)
        assertEquals("test", rep.name)
    }
}
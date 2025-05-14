package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnnotationsTests {
    @Test
    fun testComment() = testSession {
        loadKerML("""
            comment /* comment on something */ 
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals( "comment on something", comment.body)
    }

    @Test
    fun testCommentWidhId() = testSession {
        loadKerML("""
            comment test /* comment on something */ 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals( "comment on something", comment.body)
        assertEquals("test", comment.name)
    }

    @Test
    fun testCommentWidhIdOnSomething() = testSession {
        loadKerML("""
                namespace x; 
                comment test about x /* comment on something */ 
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals( "comment on something", comment.body)
        assertEquals("test", comment.name)
        assertTrue(comment.ownedElement.first().ref is Annotation)
        val annotation = comment.ownedElement.first().ref as Annotation
        assertEquals("x", annotation.annotatedElement.ref?.name)
    }

    @Test
    fun testCommentWithIdAboutSomething2() = testSession {
        loadKerML("""
            namespace x; 
            namespace y; 
            comment test about x, y /* comment on something */
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
    fun testDocWidhId() = testSession {
        loadKerML("""
            doc test /* comment on something */ 
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val doc = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(doc is Documentation)
        assertEquals( "comment on something", doc.body)
        assertEquals("test", doc.name)
    }

    @Test
    fun testRepWidhId() = testSession {
        loadKerML("""
            rep test language some /* code on something */ 
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals( "code on something", rep.body)
        assertEquals("test", rep.name)
    }


    @Test
    fun testRepWidhLanguage() = testSession {
        loadKerML("""
            language sysmd /* code on something */ 
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals( "code on something", rep.body)
        assertNull(rep.escapedName())
    }


    @Test
    fun testRepWidhIdAndLanguage2() = testSession {
        loadKerML("""
            rep test language ltl /* ltl expressions */ 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals( "ltl", rep.language)
        assertEquals("ltl expressions", rep.body)
        assertEquals("test", rep.name)
    }
}
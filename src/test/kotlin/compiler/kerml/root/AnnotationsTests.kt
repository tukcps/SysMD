package compiler.kerml.root

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.session.SessionSettings
import com.github.tukcps.sysmd.services.session.SessionStatus
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class AnnotationsTests {

    @Test
    fun testCommentBasic() {
        val settings = SessionSettings()
        val status   = SessionStatus()
        val elements = KerML(settings = settings, status = status)
            .parse("""
                comment /* comment on something */
            """)

        assertTrue(status.issues.isEmpty())
        val comment = elements.firstOrNull { it.type == ElementType.Comment }
        assertNotNull(comment)
        assertEquals("comment on something", comment.body)
    }

    @Test
    fun testComment() = testSession {
        loadKerML("comment /* comment on something */")
        assertNoIssues()
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals("comment on something", comment.body)
    }

    @Test
    fun testComment2() = testSession {
        loadKerML("/* comment on something */")
        assertNoIssues()
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals("comment on something", comment.body)
    }

    @Test
    fun testCommentWithId() = testSession {
        loadKerML("comment test /* comment on something */")
        assertNoIssues()
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals("comment on something", comment.body)
        assertEquals("test", comment.name)
    }

    @Test
    fun testCommentWithIdOnSomething() = testSession {
        loadKerML("""
            namespace x; 
            comment test about x /* comment on something */ 
        """)
        assertNoIssues()
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals("comment on something", comment.body)
        assertEquals("test", comment.name)
        assertTrue(comment.ownedElement.first() is Annotation)
        val annotation = comment.ownedElement.first() as Annotation
        assertEquals("x", annotation.annotatedElement.name)
    }

    @Test
    fun testCommentWithIdAboutSomething2() = testSession {
        loadKerML("""
            namespace x; 
            namespace y; 
            comment test about x, y /* comment on something */
        """)
        assertNoIssues()
        val comment = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(comment is Comment)
        assertEquals("comment on something", comment.body)
        assertEquals("test", comment.name)
        assertTrue(comment.ownedElement.first() is Annotation)
        val annotation1 = comment.ownedElement.first() as Annotation
        assertEquals("x", annotation1.annotatedElement.name)
        val annotation2 = comment.ownedElement[1] as Annotation
        assertEquals("y", annotation2.annotatedElement.name)
    }

    @Test
    fun testDocWithId() = testSession {
        loadKerML("""
            doc test /* comment on something */ 
        """)
        assertNoIssues()
        val doc = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(doc is Documentation)
        assertEquals("comment on something", doc.body)
        assertEquals("test", doc.name)
    }

    @Test
    fun testRepWithId() = testSession {
        loadKerML("""
            rep test language some /* code on something */ 
        """)
        assertNoIssues()
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals("code on something", rep.body)
        assertEquals("test", rep.name)
    }

    @Test
    fun testRepWithLanguage() = testSession {
        loadKerML(" language sysmd /* code on something */")
        assertNoIssues()
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals("code on something", rep.body)
        assertNull(rep.escapedName())
    }

    @Test
    fun testRepWithIdAndLanguage2() = testSession {
        loadKerML("""
            rep test language ltl /* ltl expressions */ 
        """)
        assertNoIssues()
        val rep = global.getOwnedElementOfType<AnnotatingElement>()
        assertTrue(rep is TextualRepresentation)
        assertEquals("ltl", rep.language)
        assertEquals("ltl expressions", rep.body)
        assertEquals("test", rep.name)
    }

    @Test
    fun dependencyTest() = testSession {
        loadKerML("""
            namespace n {
                doc a /* comment a something */ 
                doc b /* comment b something */
                dependency d from a to b; 
            }
        """, Runlevel.NAMES_RESOLVED)
        assertNoIssues()
        val d = global.resolve("n")?.member<Namespace>()?.ownedRelationship?.get(2)
        assertNotNull(d)
    }
}
package kermlspecificationstests

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class AnnotationsTests {

    /**
     * This test validates the declaration of comments associated with specific classifiers.
     * It demonstrates how to use the "comment" keyword to annotate elements in KerML, where the comment is related to classifiers A and B.
     * Reference: Section 7.2.4 Annotations
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testCommentDeclaration() = testSession("Occurrences") {
        loadKerML("""
            classifier A;
            classifier B;
            comment Comment1 about A, B
                /* This is the comment body text. */
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * This test checks the declaration of owned comments within a namespace. It verifies the attachment of comments to a namespace without specifying annotated elements.
     * In this case, the comment implicitly relates to the containing namespace (N), and it illustrates the default behavior when annotated elements are omitted.
     * Reference: Section 7.2.4 Annotations
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testOwnedCommentDeclaration() = testSession {
        loadKerML("""
            namespace N {
                comment C /* This is a comment about N. */
                /* This is also a comment about N. */
            }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}

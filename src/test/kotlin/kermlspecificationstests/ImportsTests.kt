package kermlspecificationstests

import util.mockup.loadKerML
import org.junit.jupiter.api.Disabled
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class ImportsTests {

    /**
     * Tests membership import within namespaces.
     * Ref: Section 7.2.5.4 - Imports
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testMembershipImport() = testSession("Occurrences") {
        loadKerML("""
            namespace N4 {
                class A;
                class B;
                class C;
            }
            
            namespace N6 {
                private import N4::A;
                private import N4::C;
                namespace M {
                    import C; // "C" is re-imported from N4 into M.
                }
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Tests namespace import using wildcard.
     * Ref: Section 7.2.5.4 - Imports
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testNamespaceImport() = testSession("Occurrences") {
        loadKerML("""
            namespace N4 {
                class A;
                class B;
                class C;
            }
            
            namespace N7 {
                // Memberships A, B and C are all imported from N4.
                private import N4::*;
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Tests recursive import with various patterns.
     * Ref: Section 7.2.5.4 - Imports
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Disabled
    @Test
    fun testRecursiveImport() = testSession("Occurrences") {
        loadKerML("""
            namespace N8 {
                class A;
                class B;
                namespace M {
                    class C;
                }
            }
            namespace N9 {
                private import N8::**;
                // The above recursive import is equivalent to all
                // of the following taken together:
                // import N8;
                // import N8::*;
                // import N8::M::*;
            }
            namespace N10 {
                private import N8::*::**;
                // The above recursive import is equivalent to all
                // of the following taken together:
                // import N8::*;
                // import N8::M::*;
                // (Note that N8 itself is not imported.)
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Tests visibility of imported elements within a namespace.
     * Ref: Section 7.2.5.4 - Imports
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Disabled
    @Test
    fun testVisibilityOfImport() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            namespace N4 {
                class A;
                class B;
                class C;
            }
            
            namespace N5 {
                class A;
                comment Comment1 about A
                    /* This is a comment about class A. */
                    
                comment Comment2
                    /* This is a comment about namespace N5. */
                    
            }
            
            namespace N11 {
                public import N4::A {
                    /* The imported membership is visible outside N11. */
                }
                private import N5::* {
                    doc /* None of the imported memberships are visible
                    * outside of N11. */
                }
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}

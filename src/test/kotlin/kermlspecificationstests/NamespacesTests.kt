package kermlspecificationstests

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class NamespacesTests {

    /**
     * Tests basic namespace declaration with nested elements.
     * Ref: Section 7.2.5 - Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testNamespaceDeclaration() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            namespace <'1.1'> N1; // This is an empty namespace.
            namespace <'1.2'> N2 {
                doc /* This is an example of a namespace body. */
                class C;
                datatype D;
                feature f : C;
                namespace N3; // This is a nested namespace.
            }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests visibility of elements within a namespace.
     * Ref: Section 7.2.5 Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testElementVisibilityDeclaration() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            namespace N3 {
                public class C;
                private datatype D;
                feature f : C; // public by default
            }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests alias declaration within a namespace.
     * Ref: Section 7.2.5 Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testAliasElementDeclaration() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            namespace N4 {
                class A;
                class B;
                alias <C> CCC for B {
                    doc /* Documentation of the alias. */
                }
                private alias D for B;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests comments as owned members within a namespace.
     * Ref: Section 7.2.5 - Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testCommentAsOwnedMemberOfNamespace() = testSession("Occurrences") {
        loadKerML("""
            namespace N5 {
                class A;
                comment Comment1 about A
                    /* This is a comment about class A. */
                comment Comment2
                    /* This is a comment about namespace N5. */
                    
                /* This is also a comment about namespace N5. */
                
                doc N9_Doc
                    /* This is documentation about namespace N5. */
            }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests root namespace with various elements.
     * Ref: Section 7.2.5 - Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testRootNamespaceDeclaration() = testSession("Occurrences") {
        loadKerML("""
            doc /* This is a model notated in KerML concrete syntax. */
            classifier A {
                feature c : C;
            }
            class C;
            datatype D;
            feature f: C;
            package P;
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}

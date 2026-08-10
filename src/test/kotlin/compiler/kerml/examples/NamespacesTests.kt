package compiler.kerml.examples

import com.github.tukcps.sysmd.compiler.parser.util.toIndentedString
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Membership
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.services.check.reportDoubleNamesInNamespace
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

class NamespacesTests {

    /**
     * Tests basic namespace declaration with nested elements.
     * Ref: Section 7.2.5 - Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testNamespaceDeclaration() = testSession("Occurrences",) {
        loadKerML("""
            namespace <'1.1'> N1; // This is an empty namespace.
            namespace <'1.2'> N2 {
                doc /* This is an example of a namespace body. */
                class C;
                datatype D;
                feature f : C;
                namespace N3; // This is a nested namespace.
            }
        """)
        assertNoIssues()
    }

    /**
     * Tests visibility of elements within a namespace.
     * Ref: Section 7.2.5 Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testElementVisibilityDeclaration() = testSession {
        loadKerML("""
            namespace N3 {
                public class C;
                private datatype D;
                feature f : C; // public by default
            }
        """)
        assertNoIssues()
    }

    /**
     * Tests alias declaration within a namespace.
     * Ref: Section 7.2.5 Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testAliasElementDeclaration() = testSession {
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
        assertNoIssues()
    }

    /**
     * Tests comments as owned members within a namespace.
     * Ref: Section 7.2.5 - Namespaces
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testCommentAsOwnedMemberOfNamespace() = testSession {
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
        """)
        assertNoIssues()
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
        """)
        assertNoIssues()
    }

    @Test
    fun testAliases() = testSession {
        loadKerML("""
            package a {
                feature foo;
            }
            package b {
                alias bar for a::foo;
                alias abc for a::foo;
                alias xyz for a::foo;
            }
        """.trimIndent())

        val foo = global.resolve("a::foo")!!.member<Feature>()!!

        for(name in listOf("bar", "abc", "xyz"))
        {
            val rel = assertNotNull( global.resolve("b::$name"), "resolve couldn't find aliased member")
            assertSame(foo, rel.memberElement, "alias resolved to wrong element")

            // assertEquals(name, rel.memberName) // fixme: SysMD implementation incorrect
        }

        assertNull(global.resolve("b::foo"), "Alias shouldn't expose aliased member")
        assertNull(global.resolve("b::a::foo"), "Alias shouldn't expose aliased member")

        assertNoIssues()
        reportDoubleNamesInNamespace()
        assertNoIssues()
    }
}

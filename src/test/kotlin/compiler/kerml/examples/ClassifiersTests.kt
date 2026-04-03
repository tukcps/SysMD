package compiler.kerml.examples

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

class ClassifiersTests {

    /**
     * Tests basic Classifier declaration with feature `age` and inheritance.
     * Ref: Section 7.3.3 - Classifiers
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testClassifierDeclaration() =
        testSession("Occurrences") {
            loadKerML("""
                classifier Person { // Default superclassifier is Base::Anything.
                    feature age : ScalarValues::Integer;
                }
                classifier Child specializes Person;
            """)
            assertNoIssues()
        }

    /**
     * Tests subclassification where `C` specializes `A` and `B`.
     * Ref: Section 7.3.3 Classifiers
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Ignore
    @Test
    fun testSubclassification() =
        testSession("Occurrences") {
            loadKerML("""
                classifier A;
                classifier B;
                subclassifier C specializes A;
                subclassifier C specializes B;
            """.trimIndent())
            assertNoIssues()
        }

    /**
     * Tests subclassification with `C` specializing both `A` and `B`.
     * Ref: Section 7.3.3 Classifiers
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testSubclassificationWithSpecialization() =
        testSession("Occurrences") {
            loadKerML(
                """
                classifier A;
                classifier B;
                classifier C specializes A, B;
            """)
            assertNoIssues()
        }

}

package kermlspecificationstests

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class ElementsTests {

    /**
     * Tests basic element declaration with a classifier and feature.
     * Ref: Section 7.2.2 - Elements and Relationships
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testElementDeclaration() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            classifier <c123> AClassifier;
            feature aFeature;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Tests element representation within a namespace with classifiers and features.
     * Ref: Section 7.2.2 - Elements and Relationships
     */
    @Test
    fun testElementRepresentation() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            namespace P {
                // This is the body of the namespace, declaring its owned members.
                classifier A;
                classifier B {
                    // This is the body of the classifier, declaring its owned features.
                    feature x;
                    feature y;
                }
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}

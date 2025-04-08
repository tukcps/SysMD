package kermlspecificationstests

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class DependencyTest {

    /**
     * Tests basic dependency declaration between layers.
     * Ref: Section 7.2.3 - Dependencies
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testDependencyDeclaration() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            dependency Use
                from 'Application Layer' to 'Service Layer';
            
            // 'Service Layer' is the client of this dependency, not its name.
            dependency 'Service Layer'
                to 'Data Layer', 'External Interface Layer';
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests dependency declaration with a relationship body.
     * Ref: Section 7.2.3 - Dependencies
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testDependencyDeclarationWithRelationshipBody() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            dependency 'Service Layer'
            to 'Data Layer', 'External Interface Layer' {
                /* 'Service Layer' is the client of this dependency,
                * not its name. */
            }
        """.trimIndent())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}

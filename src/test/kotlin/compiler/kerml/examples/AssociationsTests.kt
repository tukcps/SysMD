package compiler.kerml.examples

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class AssociationsTests {

    /**
     * This test validates the declaration of a basic association with two end features: `x` and `y`.
     * It demonstrates how associations are declared in KerML, where each association has endpoints
     * that define the elements it relates. The multiplicity of the `y` end feature is set to `[1..*]`,
     * meaning it must have at least one related element.
     * Reference: Section 7.4.5 Associations
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testAssociationDeclaration() =
        testSession("Links") {
            loadKerML("""
            	assoc A {
                    end x;
                    end y[1..*];
                }
            """)
            assertNoIssues()
        }

    /**
     * This test checks an association with specialization, where association `B` specializes association `A`.
     * It includes redefinition of an end feature (`y1` redefines `y`) and demonstrates the inheritance of
     * association end features in KerML. The `y1` end feature in `B` is redefined with a different multiplicity `[0..*]`.
     * Reference: Section 7.4.5 Associations
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testAssociationWithSpecialization() =
        testSession("Links") {
            loadKerML("""
                assoc A {
                    end x;
                    end y[1..*];
                }
                
                assoc B specializes A {
                    end x1;
                    end y1[0..*] redefines y;
                }
            """)
            assertNoIssues()
        }
}

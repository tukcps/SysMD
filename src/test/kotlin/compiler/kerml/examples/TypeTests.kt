package compiler.kerml.examples

import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test

class TypeTests {

    /**
     * Tests basic type declaration.
     * Ref: Section 7.3.2 - Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testTypeDeclaration() = testSession("ScalarValues") {
        loadKerML("""
            type A specializes Base::Anything;
        """)
        assertNoIssues()
    }

    /**
     * Tests abstract type declaration with specialization.
     * Ref: Section 7.3.2 Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testAbstractTypeDeclaration() = testSession("ScalarValues") {
        loadKerML("""
            abstract type A specializes Base::Anything;
            type A1 specializes A;
            type A2 specializes A;
        """)
        assertNoIssues()
    }

    /**
     * Tests multiplicity in type declaration.
     * Ref: Section 7.3.2 Type
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testMultiplicityOfATypeDeclaration() = testSession("ScalarValues") {
        loadKerML("""
            // This Type has exactly one instance.
            type Singleton[1] specializes Base::Anything;
        """)
        assertNoIssues()
    }

    /**
     * Tests type specialization.
     * Ref: Section 7.3.2 Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testSpecialization() = testSession("ScalarValues") {
        loadKerML("""
            type A :> Base::Anything;
            type B :> Base::Anything;
            
            type C specializes A, B;
            type f :> Base::things;
        """)
        assertNoIssues()
    }

    /**
     * Tests inheritance of memberships through specialization.
     * Ref: Section 7.3.2 Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testInheritanceOfMembershipsOfASpecialization() = testSession("ScalarValues") {
        loadKerML("""
            type A specializes Base::Anything {
                feature f; // Public by default.
                protected feature g;
                private feature h;
            }
            
            type B specializes A {
                // B inherits feature memberships for
                // f and g, but not h.
            }
        """)
        assertNoIssues()
    }

    /**
     * Tests type conjugation.
     * Ref: Section 7.3.2 Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testConjugationOfTypes() = testSession("ScalarValues") {
        loadKerML("""
            type Original specializes Base::Anything;
            type Conjugate1 specializes Base::Anything;
            
            conjugation c1 conjugate Conjugate1 conjugates Original;
        """)
        assertNoIssues()
    }

    /**
     * Tests disjoining relationship between types.
     * Ref: Section 7.3.2 Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testDisjoiningOfTypes() = testSession("ScalarValues") {
        loadKerML("""
            type A :> Base::Anything;
            type B :> Base::Anything;
            
            disjoining Disj disjoint A from B;
        """)
        assertNoIssues()
    }

    /**
     * Tests feature membership within a classifier.
     * Ref: Section 7.3.2 Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testFeatureMembership() = testSession("Occurrences") {
        loadKerML("""
            feature person : Person;
            
            classifier Person {
                // This declares an owned feature using a feature membership.
                feature age[1] : ScalarValues::Integer;
            }
        """)
        assertNoIssues()
    }

    /**
     * Tests ownership of features within a membership relationship.
     * Ref: Section 7.3.2 Types
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Ignore
    @Test
    fun testOwnedFeatureViaMembershipRelationship() = testSession("Occurrences") {
        loadKerML("""
            classifier A;
            
            classifier B {
                // Feature f has B as its featuring type.
                feature f;
                // Feature g has A as its featuring type, not B.
                member feature g featured by A;
            }
        """)
        assertNoIssues()
    }

}

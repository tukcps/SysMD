package compiler.kerml.examples

import org.junit.jupiter.api.Disabled
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test

class FeatureTests {

    /**
     * Tests basic feature declaration.
     * Ref: Section 7.3.4 - Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testFeatureDeclaration() = testSession("ScalarValues") {
        loadKerML("""
            feature f;
            feature g;
        """)
        assertNoIssues()
    }

    /**
     * Tests feature specialization and typing.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testFeatureSpecializationDeclaration() = testSession("ScalarValues") {
        loadKerML("""
            classifier A;
            classifier B;
            
            feature f;
            feature g;
            
            feature x typed by A, B references f subsets g;
            
            // Equivalent declaration:
            feature x1 subsets g typed by A subsets f typed by B;
        """)
        assertNoIssues()
    }

    /**
     * Tests feature with default subsetting.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testFeatureWithNoSpecialization() = testSession("Occurrences") {
        loadKerML("""
            classifier Person;
                
            abstract feature person : Person; // Default subsets Base::things.
            feature child subsets person;
        """)
        assertNoIssues()
    }

    /**
     * Tests feature multiplicity with specialization.
     * Ref: Section 7.3.4.2 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Ignore
    @Test
    fun testMultiplicityInFeaturesWithSpecialization() = testSession("ScalarValues") {
        loadKerML("""
            classifier Person;
            feature parent[2] : Person;
            feature mother : Person[1] subsets parent;
            // Unclear whether part of model is missing or what redefines means hare  
            feature redefines children[0];
        """)
        assertNoIssues()
    }

    /**
     * Tests feature typing for different classifiers.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Ignore
    @Test
    fun testFeatureTyping() = testSession("Occurrences") {
        loadKerML("""
            classifier A;
            classifier B;
            
            feature f;
            feature g;
            typing f typed by B;
            typing g : A;
        """)
        assertNoIssues()
    }

    /**
     * Tests subsetting relationship for features.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Disabled
    @Test
    fun testFeatureSubsetting() = testSession("Occurrences") {
        loadKerML("""
            classifier Person;
            
            feature person : Person; // Default subsets Base::things.
            feature child subsets person;
            
            feature parent : Person;
            feature mother : Person :> parent;
            
            specialization Sub subset parent subsets person;
            specialization subset mother subsets parent;
        """)
        assertNoIssues()
    }

    /**
     * Tests redefinition of features.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Disabled
    @Test
    fun testFeatureRedefinition() = testSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        loadKerML("""
            classifier Person;
            
            feature parent[1..2] : Person;
            
            classifier LegalRecord {
                feature guardian[1];
            }
            
            redefinition LegalRecord::guardian redefines parent;
        """)
        assertNoIssues()
    }

    /**
     * Tests feature membership within a classifier.
     * Ref: Section 7.3.4 Features
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
     * Tests feature membership relationship with ownership.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Disabled
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

    /**
     * Tests inheritance through feature subsetting.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testFeatureInheritance() = testSession("Occurrences") {
        loadKerML("""
            feature s {
                feature t;
            }
            
            feature u subsets s;
        """)
        assertNoIssues()
    }

}

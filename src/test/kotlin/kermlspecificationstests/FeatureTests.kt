package kermlspecificationstests

import util.mockup.loadKerML
import org.junit.jupiter.api.Disabled
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class FeatureTests {

    /**
     * Tests basic feature declaration.
     * Ref: Section 7.3.4 - Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testFeatureDeclaration() = testSession("Occurrences") {
        loadKerML("""
            feature f;
            feature g;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests feature specialization and typing.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testFeatureSpecializationDeclaration() = testSession("Occurrences") {
        loadKerML("""
            classifier A;
            classifier B;
            
            feature f;
            feature g;
            
            feature x typed by A, B references f subsets g;
            
            // Equivalent declaration:
            feature x1 subsets g typed by A subsets f typed by B;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests feature multiplicity with specialization.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Test
    fun testMultiplicityInFeaturesWithSpecialization() = testSession("Occurrences") {
        loadKerML("""
            classifier Person;
            feature parent[2] : Person;
            feature mother : Person[1] subsets parent;
            // specializes is not possible following standard -- type? Bug in test? 
            feature redefines children[0];
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Tests feature typing for different classifiers.
     * Ref: Section 7.3.4 Features
     * Kernel Modeling Language: https://www.omg.org/spec/KerML/1.0/Beta2/PDF/changebar
     */
    @Disabled
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

}

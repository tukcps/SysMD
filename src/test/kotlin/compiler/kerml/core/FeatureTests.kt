package compiler.kerml.core

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.cspsolver.getRange
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import com.github.tukcps.sysmd.services.Runlevel
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertNotNull
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class FeatureTests {
    /**
     * Basic test that element data is correctly parsed in simple context of root namespace.
     */
    @Test
    fun parseFeatureTest() {
        val compiler = KerML()
            .settings {
                addImplied = true
                addDefaultMultiplicity = false
                includeOwningRelationshipsToRoot = false
                addConstraints  = false }
        val elements = compiler.parse("feature < shortName > longName;")
        val nr = if (compiler.settings.includeOwningRelationshipsToRoot) 3 else 2
        assertEquals(nr, elements.size) // feature + membership
        val feature = elements.single { it.type == ElementType.Feature }
        // Identification OK?
        assertEquals("shortName", feature.declaredShortName)
        assertEquals("longName", feature.declaredName)
        // owningMembership OK?
        if (compiler.settings.includeOwningRelationshipsToRoot)
            assertNotNull(elements.singleOrNull { it.type == ElementType.FeatureMembership})
        else
            assertNull(feature.owningRelationship?.id)
    }

    /**
     * Check owning membership, feature using model.
     */
    @Test
    fun basicFeatureTest() = testSession {
        loadKerML("feature f;")
        assertNoIssues()
        val fm = global.resolve("f")    // only iff includeOwnningRelationshipToRoot
        val f = global.resolve("f")?.memberElement
        assertIs<Feature>(f)
        if (settings.includeOwningRelationshipsToRoot)
            assertIs<FeatureMembership>(fm)
    }

    @Test
    fun basicFeatureTest2() = testSession {
        loadKerML("""
            classifier t; 
            feature f: t;
        """)
        assertNoIssues()
        val f = global.resolve("f")
        assertNotNull(f)
    }

    @Test
    fun prefixesTest() = testSession {
        loadKerML("""
           in abstract composite const derived feature f; 
           out portion feature all g; 
        """)
        assertNoIssues()
        val f = global.resolve("f")?.member<Feature>()
        assertNotNull(f)
        assertEquals(Feature.FeatureDirectionKind.IN, f.direction)
        assertTrue(f.isAbstract)
        assertTrue(f.isComposite)
        assertFalse(f.isPortion)
        assertTrue(f.isConstant)
        assertTrue(f.isDerived)

        val g = global.resolve("g")?.member<Feature>()
        assertNotNull(g)
        assertEquals(Feature.FeatureDirectionKind.OUT, g.direction)
        assertFalse(g.isAbstract)
        assertFalse(g.isComposite)
        assertTrue(g.isPortion)
        assertFalse(g.isReadOnly)
        assertFalse(g.isDerived)
        assertTrue(g.isSufficient)
    }

    /**
     * Some checks of prefixes
     */
    @Test
    fun testValueFeaturePrefixes() = testSession("ScalarValues") {
        loadKerML("""
            out feature f1;
            inout feature f2; 
            end feature f3; 
            composite feature f4;
            out portion feature f5; 
        """)
        assertNoIssues()
        val f1 = global.resolve("f1")?.member<Feature>()
        assertEquals(Feature.FeatureDirectionKind.OUT, f1?.direction)
        val f2 = global.resolve("f2")?.member<Feature>()
        assertEquals(Feature.FeatureDirectionKind.INOUT, f2?.direction)
        val f3 = global.resolve("f3")?.member<Feature>()
        assertTrue(f3!!.isEnd)
        val f4 = global.resolve("f4")?.member<Feature>()
        assertTrue(f4!!.isComposite)
        val f5 = global.resolve("f5")?.member<Feature>()
        assertTrue(f5!!.isPortion)
        assertEquals(Feature.FeatureDirectionKind.OUT, f5.direction)
    }

    /**
     * Typed by two types.
     */
    @Test
    fun testInheritance() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything; 
            type b :> Base::Anything {
                feature c: a; 
            }; 
            feature f : a, b;
        """)
        assertNoIssues()
        val f = global.resolve("f")?.member<Feature>()
        assertEquals(2, f!!.type.size)
        val fc = global.resolve("f::c")?.member<Feature>()
        assertNotNull(fc)
    }

    @Test
    fun testMultiplicity() = testSession {
        loadKerML(" feature f: Base::Anything [1..2];")
        assertNoIssues()
        val f = global.resolve("f")!!.memberElement as Feature
        val multiplicity = f.multiplicityRange
        assertEquals(MultiplicityRange(1, 2), multiplicity)
    }

    @Test
    fun testMultiplicity2() = testSession {
        loadKerML("feature f [1 .. 2];")
        assertNoIssues()
        val f = global.resolve("f")!!.memberElement as Feature
        val multiplicity = f.multiplicity()?.getRange()
        assertEquals("1 .. 2", multiplicity?.first())
    }

    @Test
    fun testTypeFeaturing()  = testSession {
        loadKerML("""
           type a [2 ..4] :> Base::Anything ; 
           type b :> Base::Anything {  
               feature x: a [1 .. 3]; 
           }
        """)
        assertNoIssues()
        val b1 = global.resolve("b::x")?.member<Feature>()
        assertEquals(MultiplicityRange(1, 3), b1?.multiplicityRange, "Multiplicity must be 1..3")
    }

    @Test
    fun testRedefineMultiplicity() = testSession("ScalarValues") {
        loadKerML("""
            type c :> Base::Anything {
                feature f [1 ..*]; 
            }
            type c2 :> c {
                feature f2 redefines c::f [1];
            }
        """, Runlevel.MODEL)
        assertNoIssues()
        val c: Type? = global.resolve("c")?.member()
        val c2: Type? = global.resolve("c2")?.member()
        val c2f: Feature? = global.resolve("c2::f2")?.member()
        val cf: Feature? = global.resolve("c::f")?.member()
        val redefinition = c2f?.getOwnedElementsOfType<Redefinition>()?.firstOrNull()
        assertNotNull(redefinition, "There must be a redefinition element")
        assertEquals(cf, redefinition.redefinedFeature)
        assertEquals(c2f, redefinition.redefiningFeature)
        assertNotSame(c2f, cf, "The redefinition shall be independent KerML Element")
        assertEquals(cf?.owner, c)
        assertEquals(c2f.owner, c2)
    }

    @Test
    fun redefinesValueTest()  = testSession("ScalarValues") {
        loadKerML("""
            type f1 :> Base::Anything {
                feature a: ScalarValues::Real default 1.0; 
            }
            type f2 :> f1 {
                :>> a = 3.0;
            }
        """, Runlevel.ALL)
        assertNoIssues()
        Assertions.assertEquals(1.0, solver.getVariable("f1::a")!!.min(), 0.000001)
        Assertions.assertEquals(1.0, solver.getVariable("f1::a")!!.max(), 0.000001)
        Assertions.assertEquals(3.0, solver.getVariable("f2::a")!!.min(), 0.000001)
        Assertions.assertEquals(3.0, solver.getVariable("f2::a")!!.max(), 0.000001)
    }

    @Test
    fun testRedefineSameName() = testSession("ScalarValues") {
        loadKerML("""
            type c :> Base::Anything {
                feature f [1 ..*]; 
            }
            type c2 :> c {
                // f inherited --> should be redefined
                feature redefines f [2]; // c::f not inherited, instead replaced by redefinition. 
            }
        """)
        assertNoIssues()
        val c: Type? = global.resolve("c")?.member()
        val c2: Type? = global.resolve("c2")?.member()
        val c2f: Feature? = global.resolve("c2::f")?.member()
        val cf: Feature? = global.resolve("c::f")?.member()
        val redefinition = c2f?.getOwnedElementsOfType<Redefinition>()?.firstOrNull()
        assertNotNull(redefinition, "There must be a redefinition element")

        // BUG: should refer to superclass feature, and new feature should be created instead.
        // Currently, we simply clone.
        // assertEquals(cf, redefinition.redefinedFeature.ref)
        // assertEquals(c2f, redefinition.redefiningFeature.ref)
        assertNotSame(c2f, cf, "The redefinition shall be independent KerML Element")
        assertEquals(cf?.owner, c)
        assertEquals(c2f.owner, c2)
    }

    @Test
    fun referenceSubsettingTest() = testSession {
        loadKerML("""
            feature referencedFeature; 
            feature referencingFeature references referencedFeature; 
        """, Runlevel.MODEL)
        val f2 = global.getOwned<Feature>("referencingFeature")
        val reference = f2!!.getOwnedElementsOfType<ReferenceSubsetting>()
        assertTrue(reference.isNotEmpty())
        assertNoIssues()
    }

    /**
     * "property" must be a feature that owns a redefinition, and name resolution
     * must resolve the target to the feature of the superclass, not itself.
     */
    @Test
    fun redefinitionTest() = testSession("Ranges", runlevel = Runlevel.MODEL) {
        loadKerML("""
            type Test :> Base::Anything {
                feature property: Ranges::RealInRange { :>> range = 0 .. *; }
            }
        """)
        assertNoIssues()
    }

    @Test
    fun subsettingTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a; 
            feature b; 
            feature Type; 
  			derived feature f : Type[0..1] subsets a, b;
        """)
        assertNoIssues()
        val f = global.resolve("f")?.memberElement
        assertNotNull(f)
        val subsetting = f.getOwnedElementOfType<Subsetting>()
        assertNotNull(subsetting)
        assertNoIssues()
    }
}
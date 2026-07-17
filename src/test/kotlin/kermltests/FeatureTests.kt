package kermltests

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.IntegerRange
import org.junit.jupiter.api.Assertions
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*
import kotlin.test.DefaultAsserter.assertEquals

class FeatureTests {

    @Test
    fun basicTest() = testSession {
        loadKerML("""
            feature f;   
        """)
        assertNoIssues()
        val f = global.resolve("f")
        assertNotNull(f)
    }

    /**
     * We can use "string"s that are recognized as a literal.
     * Strings are saved in the AST as leaf with idType=STRING.
     * The string literal is in the field id.
     */
    @Test
    fun stringLiteralsTest() = testSession("ScalarValues") {
        loadKerML("""feature x: ScalarValues::String = "test2" ;""")
        assertNoIssues()
        val feature = global.resolve("x")?.member<Feature>()
        assertEquals("x", feature!!.declaredName)
        assertEquals("\"test2\"", feature.expression?.trim())
        assertNoIssues()
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
        assertTrue(f.isReadOnly)
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
        assertEquals( Feature.FeatureDirectionKind.OUT, f1?.direction)
        val f2 = global.resolve("f2")?.member<Feature>()
        assertEquals( Feature.FeatureDirectionKind.INOUT, f2?.direction)
        val f3 = global.resolve("f3")?.member<Feature>()
        assertTrue(f3!!.isEnd)
        val f4 = global.resolve("f4")?.member<Feature>()
        assertTrue(f4!!.isComposite)
        val f5 = global.resolve("f5")?.member<Feature>()
        assertTrue(f5!!.isPortion)
        assertEquals( Feature.FeatureDirectionKind.OUT, f5.direction)
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
        assertEquals( 2, f!!.type.size)
        val fc = global.resolve("f::c")?.member<Feature>()
        assertNotNull(fc)
    }

    @Test
    fun testValueFeatureType() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertEquals( builder.Reals, f?.vectorQuantity?.value)
    }

    @Test
    fun testValueFeature1() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real = 1.0;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertEquals(1.0 , f?.max())
    }

    @Test
    fun testValueFeatureCompute() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real = 1.0;
            feature g: ScalarValues::Real = f+1.0;
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val g: Feature? = global.resolve("g")?.member()
        assertNotNull(g)
        assertEquals(2.0, solver.getVariable("g")?.max()!!, 0.000001)
    }


    @Test
    fun testFeatureWithUnitConstraint() = testSession("ISQ") {
        loadKerML("""
                feature f: ISQ::LengthValue [mm] = 1.0 [m];
            """)
        assertNoIssues()
        val f: Feature? = global.resolve("f")?.member()
        assertNotNull(f)
        assertTrue(f.isFeatureWithValue())
        assertEquals("f", f.declaredName)
        assertEquals("mm", f.unitConstraint)
        assertEquals("1.0 [m]", f.expression)
    }

    @Test
    fun testFeatureWithTypeAndUnitConstraint() = testSession("ISQ") {
        loadKerML("""
            feature f: ISQ::LengthValue = 1.0 m {
                :>> range = "1..2000"; 
                :>> unit  = "mm"; 
            }
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertEquals(1000.0 , f!!.max(), 0.000001)
        assertEquals("m", f.vectorQuantity.unit.toString())
    }


    @Test
    fun testFeatureWithConstraintsOfProfile() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Integer[2 .. 4] (1 .. 2) [m] = 1; 
        """, Runlevel.MODEL)
        val f: Feature? = global.resolve("f")?.member()
        assertNotNull(f)
        assertNoIssues()
        assertEquals("Declared name not correct", "f", f.declaredName)
        assertTrue(f.isFeatureWithValue())
        assertEquals("Unit saved incorrectly", "m", f.unitConstraint)
        assertEquals("Expression saved incorrectly", "1", f.expression)
    }

    @Test
    fun testFeatureWithConstraintsOfProfilePropagate() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Integer(0 .. 2) = 1; 
        """, Runlevel.VARIABLES)
        val f: Feature? = global.resolve("f")?.member()
        assertNoIssues()
        assertNotNull(f)
        assertEquals("Declared name not correct", "f", f.declaredName)
        assertTrue(f.isFeatureWithValue())
        assertEquals("Expression saved incorrectly", "1", f.expression)
        assertEquals(IntegerRange(0,2), f.variable?.intSpecs?.firstOrNull())
        assertNotNull(f.variable?.ast)
        solver.propagate()
        assertEquals(IntegerRange(1,1), f.variable?.vectorQuantity?.values?.first()?.asIdd()?.getRange())
    }

    @Test
    fun testRedefine() = testSession("ScalarValues") {
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
        assertEquals(cf?.owner, c )
        assertEquals(c2f.owner, c2 )
    }


    @Test
    fun redefinesTest()  = testSession("Ranges") {
        loadKerML("""
            type f1 :> Base::Anything {
                feature a: Ranges::RealInRange { :>> range="0..20"; }
            }
            type f2 :> f1 {
                :>> a = 3.0;
            }
        """)
        solver.propagate()
        assertNoIssues()
        Assertions.assertEquals(0.0, global.resolveVar("f1::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        Assertions.assertEquals(20.0, global.resolveVar("f1::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        Assertions.assertEquals(3.0, global.resolveVar("f2::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        Assertions.assertEquals(3.0, global.resolveVar("f2::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
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
        assertEquals(cf?.owner, c )
        assertEquals(c2f.owner, c2 )
    }

    @Test
    fun referenceSubsettingTest() = testSession("ScalarValues") {
        loadKerML("""
            feature referencedFeature; 
            feature referencingFeature references referencedFeature; 
        """, Runlevel.MODEL)
        val f2 = global.getOwned<Feature>("referencingFeature")
        val reference = f2!!.getOwnedElementsOfType<ReferenceSubsetting>()
        assertTrue(reference.isNotEmpty())
        assertNoIssues()
    }


    @Test
    fun redefineWithInheritance()  = testSession( "Base") {
        loadKerML("""
            standard library package ScalarValues {
                datatype ScalarValue; 
                datatype String :> ScalarValue;
                datatype Real :> String { 
                    feature range: String; 
                }	
                datatype Integer :> Real;
                datatype Natural :> Integer; 
            }	
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val integerRange: Feature? = global.resolve("ScalarValues::Integer::range")?.member()
        assertNotNull(integerRange)
        assertEquals("String",global.resolveVar("ScalarValues::Real::range")?.baseType?.name)
        assertEquals("String",global.resolve("ScalarValues::Integer::range")?.member<Feature>()?.type[0]?.name)
        assertEquals(1, global.resolve("ScalarValues::Integer::range")?.member<Feature>()?.type?.size)
    }

    /**
     * Was issue: parser stuck; might become preferred syntax?
     */
    @Test
    fun typeWithConstraintTest() = testSession("Base", runlevel = Runlevel.NONE) {
        loadKerML("""
            feature f : ScalarValues::Real(1.0 .. 2.0 [km]);
        """)
        assertTrue(status.issues.isNotEmpty(), "Syntax error, but parser must not hang")
    }

    /**
     * Test that isInitial and isDefault flags are set correctly based on assignment operator
     */
    @Test
    fun testIsInitialAndDefaultFlags() = testSession("ISQ", "Ranges") {
        // Test regular assignment (=)
        loadKerML("feature a: ISQ::LengthValue = 5.0 m;")
        val a = global.resolve("a")?.memberElement as Feature
        assertFalse(a.isDefaultValue, "Regular assignment should not be default")
        assertFalse(a.isInitialValue, "Regular assignment should not be initial")

        // Test initial assignment (:=)
        loadKerML("feature b: ISQ::LengthValue := 10.0 m;")
        val b = global.resolve("b")?.memberElement as Feature
        assertFalse(b.isDefaultValue, "Initial assignment should not be default")
        assertTrue(b.isInitialValue, "Initial assignment should be initial")

        // Test default assignment (default)
        loadKerML("feature c: ISQ::LengthValue default 15.0 m;")
        val c = global.resolve("c")?.memberElement as Feature
        assertTrue(c.isDefaultValue, "Default assignment should be default")
        assertFalse(c.isInitialValue, "Default assignment should not be initial")

        // Test default initial assignment (default :=)
        loadKerML("feature d: ISQ::LengthValue default := 20.0 m;")
        val d = global.resolve("d")?.memberElement as Feature
        assertTrue(d.isDefaultValue, "Default initial assignment should be default")
        assertTrue(d.isInitialValue, "Default initial assignment should be initial")

        initialize(Runlevel.MODEL)
        assertNoIssues()
    }
}
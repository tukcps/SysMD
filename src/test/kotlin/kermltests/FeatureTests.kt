package kermltests

import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions
import util.testSession
import kotlin.test.*
import kotlin.test.DefaultAsserter.assertEquals

class FeatureTests {

    @Test
    fun basicTest() = testSession("ScalarValues") {
        loadKerML("""
                feature f;   
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * We can use "string"s that are recognized as a literal.
     * Strings are saved in the AST as leaf with idType=STRING.
     * The string literal is in the field id.
     */
    @Test
    fun stringLiteralsTest() = testSession("ScalarValues") {
        loadKerML("""feature x: ScalarValues::String = "test2" ;""")
        assertEquals(0, status.issues.size, "error messages: ${status.issues}")
        val feature = global.resolve<Feature>("x")
        assertEquals("x", feature!!.declaredName)
        assertEquals("\"test2\"", feature.variable!!.dependency.trim())
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    @Test
    fun prefixesTest() = testSession {
        loadKerML(
            """
               in abstract composite readonly derived feature f; 
               out portion feature all g; 
            """)
        val f = global.resolve<Feature>("f")
        assertNotNull(f)
        assertEquals(Feature.FeatureDirectionKind.IN, f.direction)
        assertTrue(f.isAbstract)
        assertTrue(f.isComposite)
        assertFalse(f.isPortion)
        assertTrue(f.isReadOnly)
        assertTrue(f.isDerived)

        val g = global.resolve<Feature>("g")
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
    fun testValueFeaturePrefixes() = testSession("Base") {
        loadKerML("""
            standard library package ScalarValues { datatype Natural; } // For multiplicity 
            out feature f1;
            inout feature f2; 
            end feature f3; 
            composite feature f4;
            out portion feature f5; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f1 = global.resolve<Feature>("f1")
        assertEquals( Feature.FeatureDirectionKind.OUT, f1?.direction)
        val f2 = global.resolve<Feature>("f2")
        assertEquals( Feature.FeatureDirectionKind.INOUT, f2?.direction)
        val f3 = global.resolve<Feature>("f3")
        assertTrue(f3!!.isEnd)
        val f4 = global.resolve<Feature>("f4")
        assertTrue(f4!!.isComposite)
        val f5 = global.resolve<Feature>("f5")
        assertTrue(f5!!.isPortion)
        assertEquals( Feature.FeatureDirectionKind.OUT, f5.direction)
    }

    /**
     * Typed by two types.
     */
    @Test
    fun testInheritance() = testSession("Base") {
        loadKerML("""
                package ScalarValues { datatype Natural; } // For multiplicity 
                type a :> Base::Anything; 
                type b :> Base::Anything {
                    feature c: a; 
                }; 
                feature f : a, b;
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve<Feature>("f")
        assertEquals( 2, f!!.type.size)
        val fc = global.resolve<Feature>("f::c")
        assertNotNull(fc)
    }

    @Test
    fun testValueFeatureType() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve<Feature>("f")
        assertEquals( builder.Reals, f?.variable?.vectorQuantity?.value)
    }

    @Test
    fun testValueFeature1() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real = 1.0;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve<Feature>("f")
        assertEquals(1.0 , f?.variable?.max())
    }

    @Test
    fun testValueFeatureCompute() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real = 1.0;
            feature g: ScalarValues::Real = f+1.0;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val g = global.resolve<Feature>("g")
        assertEquals(2.0 , g?.variable?.max()!!, 0.000001)
    }


    @Test
    fun testFeatureWithUnitConstraint() = testSession("SI") {
        loadKerML("""
                feature f: SI::Length [mm] = 1.0 [m];
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve<Feature>("f")
        assertNotNull(f)
        assertTrue(f.isFeatureWithValue())
        assertEquals("f", f.declaredName)
        assertEquals("mm", f.unitConstraint)
        assertEquals("1.0 [m]", f.expression)
    }

    @Test
    fun testFeatureWithTypeAndUnitConstraint() = testSession("SI") {
        loadKerML("""
                feature f: SI::Length(1.0 .. 2000.0) [mm] = 1.0 m;
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve<Feature>("f")
        assertEquals(1000.0 , f!!.variable!!.max(), 0.000001)
        assertEquals("m", f.variable!!.vectorQuantity.unit.toString())
    }


    @Test
    fun testFeatureWithConstraintsOfProfile() = testSession("ScalarValues") {
        loadKerML("""
                feature f: ScalarValues::Integer[2 .. 4] (1 .. 2) [m] = 1; 
            """)
        val f = global.resolve<Feature>("f")!!
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals("Declared name not correct", "f", f.declaredName)
        assertTrue(f.isFeatureWithValue())
        assertEquals("Unit saved incorrectly", "m", f.unitConstraint)
        assertEquals("Expression saved incorrectly", "1", f.expression)
    }

    @Test
    fun testFeatureWithConstraintsOfProfilePropagate() = testSession("ScalarValues") {
        loadKerML("""
                feature f: ScalarValues::Integer(0 .. 2) = 1; 
            """)
        val f = global.resolve<Feature>("f")!!
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals("Declared name not correct", "f", f.declaredName)
        assertTrue(f.isFeatureWithValue())
        assertEquals("Expression saved incorrectly", "1", f.expression)
        assertEquals(IntegerRange(0,2), f.variable?.intSpecs?.firstOrNull())
        assertNotNull(f.variable?.ast)
        propagate()
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
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.resolve<Type>("c")
        val c2 = global.resolve<Type>("c2")
        val c2f = global.resolve<Feature>("c2::f2")
        val cf = global.resolve<Feature>("c::f")
        val redefinition = c2f?.getOwnedElementsOfType<Redefinition>()?.firstOrNull()
        assertNotNull(redefinition, "There must be a redefinition element")
        assertEquals(cf, redefinition.redefinedFeature.ref)
        assertEquals(c2f, redefinition.redefiningFeature.ref)
        assertNotSame(c2f, cf, "The redefinition shall be independent KerML Element")
        assertEquals(cf?.owner?.ref, c )
        assertEquals(c2f.owner.ref, c2 )
    }


    @Test
    fun redefinesTest()  = testSession("ScalarValues") {
        loadKerML("""
                type f1 :> Base::Anything {
                    feature a: ScalarValues::Real(0..20); 
                }
                type f2 :> f1 {
                    // feature a: ScalarValues::Real(3.0); 
                    :>> a = 3.0;
                }
            """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
                    feature redefines f [2]; // c::f not inherited, instead replaced by redef. 
                }
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val c = global.resolve<Type>("c")
        val c2 = global.resolve<Type>("c2")
        val c2f = global.resolve<Feature>("c2::f")
        val cf = global.resolve<Feature>("c::f")
        val redefinition = c2f?.getOwnedElementsOfType<Redefinition>()?.firstOrNull()
        assertNotNull(redefinition, "There must be a redefinition element")

        // BUG: should refer to superclass feature, and new feature should be created instead.
        // Currently, we simply clone.
        // assertEquals(cf, redefinition.redefinedFeature.ref)
        // assertEquals(c2f, redefinition.redefiningFeature.ref)
        assertNotSame(c2f, cf, "The redefinition shall be independent KerML Element")
        assertEquals(cf?.owner?.ref, c )
        assertEquals(c2f.owner.ref, c2 )
    }

    @Test
    fun referenceSubsettingTest() = testSession("ScalarValues") {
        loadKerML("""
            feature referencedFeature; 
            feature referencingFeature references referencedFeature; 
        """.trimIndent())
        val f2 = global.getOwned<Feature>("referencingFeature")
        val reference = f2!!.getOwnedElementsOfType<ReferenceSubsetting>()
        assertTrue(reference.isNotEmpty())
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }


    @Test
    fun redefineWithInheritance()  = testSession( "Base") {
        loadKerML("""
            standard library package ScalarValues {
                datatype String;
                datatype Real :> String { 
                    feature range: String; 
                }	
                datatype Integer :> Real;
                datatype Natural :> Integer; 
            }	
        """)
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        val integerRange = global.resolve<Feature>("ScalarValues::Integer::range")
        assertNotNull(integerRange)
        assertEquals("String",global.resolveVar("ScalarValues::Real::range")!!.baseType.name)
        assertEquals("String",global.resolve<Feature>("ScalarValues::Integer::range")!!.type[0].ref!!.name)
        assertEquals(1, global.resolve<Feature>("ScalarValues::Integer::range")!!.type.size)
    }
}
package compiler.kerml.kernel

import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElement
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class FeatureExpressionTests {

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
        assertNotNull(feature)
        val typing = feature.typing.first()
        assertEquals("String", typing.target.first().escapedName())
        assertEquals("x", feature.declaredName)
        assertEquals("\"test2\"", feature.expression?.trim())
        assertNoIssues()
    }

    @Test
    fun testValueFeatureTyping() = testSession("ScalarValues") {
        loadKerML("feature f: ScalarValues::Real;", Runlevel.MODEL)
        assertNoIssues()
        val f = global.resolve("f")?.member<Feature>()
        assertNotNull(f)
        val typing = f.typing.firstOrNull()
        assertNotNull(typing)
        assertNotNull(typing.target.firstOrNull())
        val real = global.resolve("ScalarValues::Real")?.member<DataType>()
        assertNotNull(real)
        assertEquals(real, typing.target.firstOrNull())
    }

    /**
     * Test values with units
     */
    @Test
    fun testValueConstraint() = testSession("ScalarValues") {
        loadKerML("feature f: ScalarValues::Real(1.0 .. 3.0);")
        assertNoIssues()
        val f = global.resolve("f")?.member<Feature>()
        assertNotNull(f)
        val constraint = global.resolve("f::range")?.member<Feature>()
        assertNotNull(constraint)
        val expression = constraint.expression
        assertEquals("1 .. 3", expression?.trim())
        val viaFeature = f.typeConstraint.first()
        assertEquals("1 .. 3", viaFeature)
    }

    @Test
    fun testFeatureWithUnitConstraint() = testSession("ISQ") {
        loadKerML("feature f: ISQ::LengthValue( *..* [mm]) = 1.0 [m];")
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
                :>> range = 1..2000 [mm];
            }
        """, Runlevel.MODEL)
        assertNoIssues()
        val f = global.resolve("f")?.member<Feature>()
        assertNotNull(f)
        assertEquals("1..2000", f.typeConstraint.first())
        assertEquals("mm", f.unitConstraint)
    }


    @Test
    fun testFeatureWithConstraintsOfProfile() = testSession("ScalarValues") {
        loadKerML("feature f: ScalarValues::Integer[2 .. 4] (1 .. 2 [m]) = 1; ", Runlevel.MODEL)
        val f: Feature? = global.resolve("f")?.member()
        assertNotNull(f)
        assertNoIssues()
        assertEquals("f", f.declaredName)
        assertTrue(f.isFeatureWithValue())
        assertEquals("m", f.unitConstraint)
        assertEquals("1", f.expression)
    }

    @Test
    fun testFeatureWithConstraintsOfProfilePropagate() = testSession("ScalarValues") {
        loadKerML("feature f: ScalarValues::Integer(0 .. 2) = 1;", Runlevel.ALL)
        val f: Feature? = global.resolve("f")?.member()
        assertNoIssues()
        assertNotNull(f)
        assertEquals("f", f.declaredName)
        assertEquals("1", f.expression)
        val range = f.getOwnedElement("range") as? Feature
        assertEquals("0 .. 2", range?.expression)
    }

    @Test
    fun redefineWithInheritance()  = testSession {
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
        assertEquals("String", solver.getVariable("ScalarValues::Real::range")?.baseType?.name)
        assertEquals("String", global.resolve("ScalarValues::Integer::range")?.member<Feature>()?.type[0]?.name)
        assertEquals(1, global.resolve("ScalarValues::Integer::range")?.member<Feature>()?.type?.size)
    }

    /**
     * Test that isInitial and isDefault flags are set correctly based on assignment operator
     */
    @Test
    fun testIsInitialAndDefaultFlags() = testSession("ISQ") {
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
package kermltests

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Redefinition
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.*
import kotlin.test.DefaultAsserter.assertEquals

class FeatureTests {


    /**
     * Some checks of prefixes
     */
    @Test
    fun testValueFeaturePrefixes() = testSession {
        loadSysMD("""
            out feature f1;
            inout feature f2; 
            end feature f3; 
            composite feature f4;
            out portion feature f5; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
    fun testInheritance() = testSession {
        +"""
            type a :> Base::Anything; 
            type b :> Base::Anything {
                feature c: a; 
            }; 
            feature f typed by a, b;  
        """
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val f = global.resolve<Feature>("f")
        assertEquals( 2, f!!.type.size)
        val fc = global.resolve<Feature>("f::c")
        assertNotNull(fc)
    }

    @Test
    fun testValueFeatureType() = testSession {
        loadSysMD("""
            feature f: ScalarValues::Real;
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val f = global.resolve<Feature>("f")
        assertEquals( builder.Reals, f?.variable?.vectorQuantity?.value)
    }

    @Test
    fun testValueFeature1() = testSession {
        loadSysMD("""
            feature f: ScalarValues::Real = 1.0;
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val f = global.resolve<Feature>("f")
        assertEquals(1.0 , f?.variable?.max())
    }

    @Test
    fun testValueFeatureCompute() = testSession {
        loadSysMD("""
            feature f: ScalarValues::Real = 1.0;
            feature g: ScalarValues::Real = f+1.0;
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val g = global.resolve<Feature>("g")
        assertEquals(2.0 , g?.variable?.max()!!, 0.000001)
    }


    @Test
    fun testFeatureWithUnitConstraint() = testSession {
        loadSysMD("""
            feature f: ScalarValues::Real [mm] = 1.0 m;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val f = global.resolve<Feature>("f")
        assertNotNull(f)
        assertTrue(f.isFeatureWithValue())
        assertEquals("f", f.declaredName)
        assertEquals("mm", f.unitConstraint)
        assertEquals("1.0 m", f.expression)
    }

    @Test
    fun testFeatureWithTypeAndUnitConstraint() = testSession {
        loadSysMD("""
            feature f: ScalarValues::Real(1.0 .. 2000.0) [mm] = 1.0 m;
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val f = global.resolve<Feature>("f")
        assertEquals(1000.0 , f!!.variable!!.max(), 0.000001)
        assertEquals("m", f.variable!!.vectorQuantity.unit.toString())
    }


    @Test
    fun testFeatureWithConstraintsOfProfile() = testSession {
        loadSysMD("""
            feature f: ScalarValues::Integer (1 .. 2) [2 .. 4][m] = 1; 
        """.trimIndent())
        val f = global.resolve<Feature>("f")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals("Declared name not correct", "f", f?.declaredName)
        assertTrue(f?.isFeatureWithValue() == true)
        assertEquals("Unit saved incorrectly", "m", f?.unitConstraint)
        assertEquals("Expression saved incorrectly", "1", f?.expression)
    }


    @Test
    fun testFeatureWithConstraintsOfProfilePropagate() = testSession {
        loadSysMD("""
            feature f: ScalarValues::Integer(0 .. 2) = 1; 
        """.trimIndent())
        val f = global.resolve<Feature>("f")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals("Declared name not correct", "f", f?.declaredName)
        assertTrue(f?.isFeatureWithValue() == true)
        assertEquals("Expression saved incorrectly", "1", f?.expression)
        assertEquals(IntegerRange(0,2), f?.variable?.intSpecs?.firstOrNull())
        assertNotNull(f?.variable?.ast)
        propagate()
        assertEquals(IntegerRange(1,1), f?.variable?.vectorQuantity?.values?.first()?.asIdd()?.getRange())
    }

    @Test
    fun testRedefine() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            class c {
                feature f [1 ..*]; 
            }
            class c2 :> c {
                feature f2 redefines c::f [1];
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<Class>("c")
        val c2 = global.resolve<Class>("c2")
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
    fun testRedefineSameName() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            class c {
                feature f [1 ..*]; 
            }
            class c2 :> c {
                // f inherited --> should be redefined
                feature redefines f [2]; // c::f not inherited, instead replaced by redef. 
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val c = global.resolve<Class>("c")
        val c2 = global.resolve<Class>("c2")
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
}
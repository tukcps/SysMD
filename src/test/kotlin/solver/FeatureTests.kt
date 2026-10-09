package solver

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.bounds.DoubleBound
import io.github.tukcps.aadd.values.integer.IntegerRange
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class FeatureTests {

    @Test
    fun testValueFeature1() = testSession("ScalarValues") {
        loadKerML("feature f: ScalarValues::Real = 1.0;", Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertBounds(1.0, f!!)
    }

    @Test
    fun testValueFeatureCompute() = testSession("ScalarValues") {
        loadKerML("""
            feature f: ScalarValues::Real = 1.0;
            feature g: ScalarValues::Real = f+1.0;
        """, Runlevel.ALL)
        assertNoIssues()
        val f: Feature? = global.resolve("f")?.member()
        assertNotNull(f)
        val g: Feature? = global.resolve("g")?.member()
        assertNotNull(g)
        assertBounds(2.0, solver.variable("g"))
    }

    @Test
    fun testFeatureWithTypeAndUnitConstraint() = testSession("ISQ") {
        loadKerML("""
            feature f: ISQ::LengthValue = 1.0 m { :>> range = 1..2000 [mm];}
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertBounds(1000.0, f!!, unit = "mm")
        assertEquals("m", f.vectorQuantity.unit.toString())
    }

    @Test
    fun testFeatureWithConstraintsOfProfilePropagate() = testSession("ScalarValues") {
        loadKerML("feature f: ScalarValues::Integer(0 .. 2) = 1;", Runlevel.ALL)
        val f: Feature? = global.resolve("f")?.member()
        assertNoIssues()
        assertNotNull(f)
        assertEquals("f", f.declaredName)
        assertEquals("1", f.expression)
        assertEquals(IntegerRange(0, 2), f.variable?.intSpecs?.firstOrNull())
        assertNotNull(f.variable?.ast)
        assertBounds(1L, f.variable!!)
    }
}
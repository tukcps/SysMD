package solver

import com.github.tukcps.sysmd.model.kerml.DataType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.values.IntegerRange
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FeatureTests {

    @Test
    fun testValueFeature1() = testSession("ScalarValues") {
        loadKerML("feature f: ScalarValues::Real = 1.0;", Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertEquals(1.0, f?.max())
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
        val gvar = solver.getVariable("g")
        assertEquals(2.0, solver.getVariable("g")?.max()!!, 0.000001)
    }

    @Test
    fun testFeatureWithTypeAndUnitConstraint() = testSession("ISQ") {
        val l = global.resolve("ISQ::LengthValue")?.member<DataType>()
        loadKerML("""
            feature f: ISQ::LengthValue = 1.0 m { :>> range = 1..2000 [mm];}
        """, Runlevel.VARIABLES)
        assertNoIssues()
        val f = solver.getVariable("f")
        assertEquals(1000.0, f!!.max(), 0.000001)
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
        assertEquals(IntegerRange(1, 1), f.variable?.vectorQuantity?.values?.first()?.asIdd()?.getRange())
    }
}
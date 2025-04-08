package constraintnettests

import util.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertTrue

class AllOnePropagationTests {

    @Test fun allOnePropagationTestReal() = testSession("ScalarValues") {
        loadKerML("""
                // a is a Real from 1..2, and is assigned a value from 1.2 to 2.5
                feature all a: ScalarValues::Real = oneOf(1.5 .. 2.5) {:>> range = "1 .. 2";}
                feature b: ScalarValues::Real = oneOf(1.5 .. 2.5) {:>> range = "1 .. 2";}
            """)
        propagate()
        assertTrue(status.issues.isNotEmpty(), "an error shall be reported as the constraints cannot be satisfied all")
        val a = global.resolve<Feature>("a")!!.variable!!
        val b = global.resolve<Feature>("b")!!.variable!!
        assertEquals(1.0, a.min(), 0.000001)
        assertEquals(2.0, a.max(), 0.000001)
        assertEquals(1.5, b.min(), 0.000001)
        assertEquals(2.0, b.max(), 0.000001)
        assertEquals(1, status.issues.size, status.issues.toString())
    }

    @Test fun allOnePropagationTestInt() = testSession("ScalarValues") {
        loadKerML("""
            // Contradiction ...         
            feature all a: ScalarValues::Integer = oneOf(5 .. 15) {:>> range = "1 .. 10";}
            feature b: ScalarValues::Integer = oneOf(5 .. 15) {:>> range = "1 .. 10";}
        """)
        propagate()
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
        val a = global.resolve<Feature>("a")!!.variable!!
        val b = global.resolve<Feature>("b")!!.variable!!
        assertEquals(1.0, a.min(), 0.000001)
        assertEquals(10.0, a.max(), 0.000001)
        assertEquals(5.0, b.min(), 0.000001)
        assertEquals(10.0, b.max(), 0.000001)
        assertEquals(1, status.issues.size)
    }


    @Test fun allOnePropagationTestRealNew() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                // Contradiction ... 
                feature all a: Ranges::RealInRange {
                    :>> min = 1.0; 
                    :>> max = 10.0;
                }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<Feature>("a")!!.variable!!
        assertEquals(1.0, a.min(), 0.000001)
        assertEquals(10.0, a.max(), 0.000001)
    }

    @Test fun allOnePropagationTestIntNew() = testSession("Ranges") {
        loadKerML("""
                // Contradiction ... 
                feature all a: Ranges::IntegerInRange = oneOf(5 .. 15) {
                    :>> min = 1; 
                    :>> max = 10;
                }
                feature b: ScalarValues::Integer = oneOf(5 .. 15) {:>> range = "1 .. 10";}
        """)
        propagate()
        assertTrue(status.issues.isNotEmpty(), "Not satisfiability for all shall be reported")
        val a = global.resolve<Feature>("a")!!.variable!!
        val b = global.resolve<Feature>("b")!!.variable!!
        assertEquals(1.0, a.min(), 0.000001)
        assertEquals(10.0, a.max(), 0.000001)
        assertEquals(5.0, b.min(), 0.000001)
        assertEquals(10.0, b.max(), 0.000001)
        assertEquals(1, status.issues.size, status.issues.toString())
    }
}
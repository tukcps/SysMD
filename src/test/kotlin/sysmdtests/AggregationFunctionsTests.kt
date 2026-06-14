package sysmdtests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class AggregationFunctionsTests {
    /**
     * The function sum("hasElements", property) computes the sum of a property via the
     * decomposition relation hasElements.
     */
    @Test
    fun sumAggregationTest1() = testSession("Occurrences", "Ranges") {
        loadKerML(""" 
            class c1 { feature p: Ranges::RealInRange {:>> range = "1..1";} }
            class c2 { feature p: Ranges::RealInRange {:>> range = "2..2";} }
            class c3 {
                feature a: c1[1..1]; 
                feature b: c2[2..2]; 
                feature p3: ScalarValues::Real = sumOverParts(p); 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        // println(resolveName<Expression>(global, "c3::p3"))
        assertEquals(5.0, solver.getVariable("c3::p3")!!.min(), 0.000001)
    }

    /**
     * The function sum("hasElements", property) computes the sum of a property via the
     * decomposition relation hasElements.
     */
    @Test
    fun sumAggregationTest2()   = testSession("Occurrences", "Ranges") {
        loadKerML("""
            class c1 { feature p: Ranges::RealInRange {:>> range = "1..1";}}
            class c2 { feature p: Ranges::RealInRange {:>> range = "2..3";}}
            class c3 {
                feature a:  c1[1..1];
                feature b:  c2[2..3];
                feature p3: ScalarValues::Real = sumOverParts(p); 
            }""", Runlevel.ALL)
        assertNoIssues()
        assertEquals(5.0, solver.getVariable("c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(10.0, solver.getVariable("c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }


    /**
     * The function sumHasA(property) computes the sum of a property via the
     * decomposition relation hasA, and if an element is not there, and has no parts, the value of the
     * property is assumed to be 0.
     */
    @Test
    fun sumAggregationTest3() = testSession("Occurrences", "Ranges") {
        loadKerML("""
            class c1 {
                feature p: Ranges::RealInRange { :>> range = "1..2"; }
            }  
            class c2; 
            class c3 {
                feature a: c1 [1..2];    // 1..2 * 1..2 
                feature b: c2 [2..3];    // shall be 0 as no property p is not defined.
                feature p3: ScalarValues::Real = sumOverParts(p). 
            }
        """)
        assertNoIssues()
        solver.propagate()
        // println(resolveName<Expression>("l::c3::p3"))
        assertEquals(1.0, solver.getVariable("c3::p3")!!.min(), 0.0001)
        assertEquals(4.0, solver.getVariable("c3::p3")!!.max(), 0.0001)
    }

    /**
     * The function sumHasA(property) computes the sum of a property via the
     * decomposition relation hasA, and if an element is not there, and has  parts, the sum of the
     * property in its parts shall be used.
     */
    @Test
    fun sumAggregationTest4() = testSession( "Ranges") {
        loadKerML("""
            classifier c1 {
                feature p: Ranges::RealInRange { :>> range = "1..2";} 
            }
            classifier c2 {
                feature c: c1[5 .. 6];
            }
            classifier c3 {
                feature a:  c1[1..2];          // 1..2 +
                feature b:  c2[2..3];          // 2..3 * (1..2 * 5..6) 
                                                        // = 1..2 + 2..3 * (1..2*5..12)
                                                        // = 1..2 + 2..3 * 5..24
                                                        // = 1..2 + 10..
                feature p3: ScalarValues::Real = sumOverParts(p); 
            }
        """)
        assertNoIssues()
        solver.propagate()
        // println(resolveName<Expression>("l::c3::p3"))
        assertEquals(11.0, solver.getVariable("c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(40.0, solver.getVariable("c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }
}
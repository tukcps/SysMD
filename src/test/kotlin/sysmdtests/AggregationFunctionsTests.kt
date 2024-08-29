package sysmdtests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class AggregationFunctionsTests {
    /**
     * The function sum("hasElements", property) computes the sum of a property via the
     * decomposition relation hasElements.
     */
    @Test
    fun sumAggregationTest1() = testSession {
        loadSysMD(""" 
            class c1 { feature p: ScalarValues::Real(1..1); }
            class c2 { feature p: ScalarValues::Real(2..2); }
            class c3 {
                feature a: c1[1..1]; 
                feature b: c2[2..2]; 
                feature p3: ScalarValues::Real = sumOverParts(p); 
            }
        """)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        // println(resolveName<Expression>(global, "c3::p3"))
        assertEquals(5.0, global.resolveVar("c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.000001)
    }

    /**
     * The function sum("hasElements", property) computes the sum of a property via the
     * decomposition relation hasElements.
     */
    @Test
    fun sumAggregationTest2()   = testSession {
        loadSysMD("""
            class c1 { feature p: ScalarValues::Real(1..1); }
            class c2 { feature p: ScalarValues::Real(2..3); }
            class c3 {
                feature a:  c1[1..1];
                feature b:  c2[2..3];
                feature p3: ScalarValues::Real = sumOverParts(p); 
            }""")
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(5.0, global.resolveVar("c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(10.0, global.resolveVar("c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }


    /**
     * The function sumHasA(property) computes the sum of a property via the
     * decomposition relation hasA, and if an element is not there, and has no parts, the value of the
     * property is assumed to be 0.
     */
    @Test
    fun sumAggregationTest3() = testSession("ScalarValues") {
        loadSysMD(input = """
            package l.
            l defines class c1;  class c2; class c3. 
            l::c1 hasA 
                feature p: ScalarValues::Real(1..2).
            l::c3 hasA 
                feature a: Global::l::c1 [1..2];    // 1..2 * 1..2 
                feature b: Global::l::c2 [2..3];    // shall be 0 as no property p is not defined.
                feature p3: ScalarValues::Real = sumOverParts(p). 
            """)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        // println(resolveName<Expression>("l::c3::p3"))
        assertEquals(1.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    /**
     * The function sumHasA(property) computes the sum of a property via the
     * decomposition relation hasA, and if an element is not there, and has  parts, the sum of the
     * property in its parts shall be used.
     */
    @Test
    fun sumAggregationTest4() = testSession("ScalarValues") {
        loadSysMD(input = """
            package l {
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA 
                feature p: ScalarValues::Real(1..2).
            l::c2 hasA 
                feature c: c1[5 .. 6] . 
            l::c3 hasA 
                feature a:  Global::l::c1[1..2];          // 1..2 +
                feature b: Global::l::c2[2..3];          // 2..3 * (1..2 * 5..6) 
                                                            // = 1..2 + 2..3 * (1..2*5..12)
                                                            // = 1..2 + 2..3 * 5..24
                                                            // = 1..2 + 10..
                feature p3: ScalarValues::Real = sumOverParts(p).
            """)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        propagate()
        // println(resolveName<Expression>("l::c3::p3"))
        assertEquals(11.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(40.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }



    /**
     * The function forAll(BoolExpression): Bool evaluates
     */
    @Disabled
    @Test fun forAllElementsTest() {
    }


    /**
     * The function existsProperty(scope: String, name: String),
     */
    @Disabled
    @Test fun existsPropertyTest() {
    }

    @Disabled
    @Test fun existsInstanceTest() {
    }
}
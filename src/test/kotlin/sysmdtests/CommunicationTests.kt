package sysmdtests

import io.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class CommunicationTests {

    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorXtoY() = testSession("Occurrences", "Links") {
        loadKerML("""
            package tst {
                class a {
                    feature x: ScalarValues::Real = 2.0;
                }

                class b { 
                    feature y: ScalarValues::Real;
                }

                assoc c {
                    private import tst::a;
                    private import tst::b;
                    inv val { x == y }
                }
            }
            """
        )
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(Range(2.0..2.0), global.resolve<Feature>(qualifiedName = "tst::a::x")!!.variable!!.aadd().getRange())
        assertEquals(Range(2.0..2.0), global.resolve<Feature>(qualifiedName = "tst::b::y")!!.variable!!.aadd().getRange())
    }


    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorXtoYtoZ() {
        testSession("Occurrences", "Links") {
            loadKerML(
                input = """
                package tst { 
                    private import ScalarValues; 
                    class a {
                        feature x: ScalarValues::Real = 2.0;
                    }
                    class b {
                        feature y: ScalarValues::Real; 
                    }
                    class c {
                        feature z: ScalarValues::Real;
                    }
                    assoc ab {
                        private import tst::a; 
                        private import tst::b;  
                        inv val { x == y }
                    }        
                    assoc bc {
                        private import tst::b;
                        private import tst::c;
                        inv val { y == z }
                    }
                }
            """)
            assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
            propagate()
            assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
            assertTrue(global.resolveVar("tst::b::y")!!.aadd().getRange() in Range(1.99 .. 2.01))
            assertTrue( global.resolveVar("tst::c::z")!!.aadd().getRange() in Range(1.99 .. 2.01))
            // println(global.resolveName<Expression>("tst::a::x"))
            // println(global.resolveName<Expression>("tst::b::y"))
        }
    }


    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorYtoX() = testSession("ScalarValues", "Links") {
        loadKerML("""
            package tst {
                feature a {
                    feature x: ScalarValues::Real; 
                }

                feature b { 
                    feature y: ScalarValues::Real = 2.0;
                }
                
                connector c : Links::Link from a to b {
                    // todo -- use end features and connector
                    private import a;
                    private import b;
                    inv name { x == y }
                }
            }
        """)
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(Range(2.0..2.0), global.resolve<Feature>("tst::a::x")!!.variable!!.aadd().getRange())
        assertEquals(Range(2.0..2.0), global.resolve<Feature>("tst::b::y")!!.variable!!.aadd().getRange())
        // println(global.resolveName<Expression>("tst::a::x"))
        // println(global.resolveName<Expression>("tst::b::y"))
    }
}
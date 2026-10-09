package solver

import util.variable
import util.assertBounds
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.values.real.ia.RealRange
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommunicationTests {

    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorXtoY() = testSession("Occurrences", "Links") {
        loadKerML("""
            class a {
                feature x: ScalarValues::Real = 2.0;
            }

            class b { 
                feature y: ScalarValues::Real;
            }

            assoc c {
                private import a::x;
                private import b::y;
                inv { x == y }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(2.0, solver.variable("a::x"))
        assertBounds(2.0, solver.variable("b::y"))
    }


    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorXtoYtoZ() {
        testSession("Occurrences") {
            loadKerML("""
                private import ScalarValues::*; 
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
                    private import a::x; 
                    private import b::y;  
                    inv val { x == y }
                }        
                assoc bc {
                    private import b::y;
                    private import c::z;
                    inv { y == z }
                }
            """, Runlevel.ALL)
            assertNoIssues()
            assertBounds(2.0, solver.variable("b::y"))
            assertBounds(2.0, solver.variable("c::z"))
        }
    }


    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorYtoX() = testSession("Links") {
        loadKerML("""
            feature a {
                feature x: ScalarValues::Real; 
            }

            feature b { 
                feature y: ScalarValues::Real = 2.0;
            }
            
            connector c : Links::Link from a to b {
                // todo -- use end features and connector
                private import a::x;
                private import b::y;
                inv { x == y }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(2.0, solver.variable("a::x"))
        assertBounds(2.0, solver.variable("b::y"))
    }
}
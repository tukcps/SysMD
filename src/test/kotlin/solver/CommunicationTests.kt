package solver

import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.Range
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
        loadKerML(
            """
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
        """
        )
        assertNoIssues()
        solver.propagate()
        assertEquals(Range(2.0..2.0), solver.getVariable("a::x")!!.range())
        assertEquals(Range(2.0..2.0), solver.getVariable("b::y")!!.range())
    }


    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorXtoYtoZ() {
        testSession("Occurrences", "Links") {
            loadKerML(
                """
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
            """, Runlevel.ALL
            )
            assertNoIssues()
            assertTrue(global.resolveVar("b::y")!!.aadd().getRange() in Range(1.99..2.01))
            assertTrue(global.resolveVar("c::z")!!.aadd().getRange() in Range(1.99..2.01))
        }
    }


    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorYtoX() = testSession("ScalarValues", "Links") {
        loadKerML(
            """
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
        """, Runlevel.ALL
        )
        assertNoIssues()
        assertEquals(Range(2.0..2.0), solver.getVariable("a::x")!!.range())
        assertEquals(Range(2.0..2.0), solver.getVariable("b::y")!!.range())
    }
}
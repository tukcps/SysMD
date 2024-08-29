package sysmdtests

import com.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CommunicationTests {

    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorXtoY() = testSession {
        loadSysMD(
            """
            package tst {
                class a {
                    feature x: ScalarValues::Real = 2.0;
                }

                class b { 
                    feature y: ScalarValues::Real;
                }

                assoc c {
                    import tst::a;
                    import tst::b;
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
        testSession {
            loadSysMD(
                input = """
                package tst { 
                    import ScalarValues; 
                    class a {
                        attribute x: ScalarValues::Real = 2.0;
                    }
                    class b {
                        attribute y: ScalarValues::Real; 
                    }
                    class c {
                        attribute z: ScalarValues::Real;
                    }
                    assoc ab {
                        import tst::a; 
                        import tst::b;  
                        inv val { x == y }
                    }
                    
                    assoc bc {
                        import tst::b;
                        import tst::c;
                        inv val { y == z }
                    }
                }
            """.trimIndent()
            )
            assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
            propagate()
            Assertions.assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
            assertEquals("2..2", global.resolveVar("tst::b::y")!!.aadd().getRange().toString())
            assertEquals("2..2", global.resolveVar("tst::c::z")!!.aadd().getRange().toString())
            // println(global.resolveName<Expression>("tst::a::x"))
            // println(global.resolveName<Expression>("tst::b::y"))
        }
    }


    /**
     * We test the propagation of values in one direction from x to y.
     */
    @Test
    fun propTestConnectorYtoX() = testSession {
        loadSysMD(
            """
            package tst {
                feature a {
                    feature x: ScalarValues::Real; 
                }

                feature b { 
                    feature y: ScalarValues::Real = 2.0;
                }
                
                connector c : Links::Link from a to b {
                    // todo -- use end features and connector
                    import a;
                    import b;
                    inv name { x == y }
                }
            }
            """
        )
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        propagate()
        assertEquals(Range(2.0..2.0), global.resolve<Feature>("tst::a::x")!!.variable!!.aadd().getRange())
        assertEquals(Range(2.0..2.0), global.resolve<Feature>("tst::b::y")!!.variable!!.aadd().getRange())
        // println(global.resolveName<Expression>("tst::a::x"))
        // println(global.resolveName<Expression>("tst::b::y"))
    }
}
package compiler

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.services.session.SessionImplementation
import org.junit.jupiter.api.assertTimeoutPreemptively
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Generic tests of the parser(s), without semantic actions.
 */
class ParserTests {

    private fun getParser(): SysMLv2 {
        val model = SessionImplementation(libraries = mutableListOf())
        return SysMLv2(model = model)
    }

    /** Short name is given in <> */
    @Test
    fun shortNameTest12() = testSession(initialize = false) {
        loadKerML("""class < abc >; """)
        assertEquals(0, status.issues.size, status.issues.toString() )
        assertEquals(0, astNodes.size)
    }

    /** Short name is given in <> */
    @Test
    fun shortNameTest21() = testSession(initialize = false) {
        loadKerML("class < shortName > longName;")
        val abc = global.ownedElement.firstOrNull { it is Class }
        assertEquals("shortName", abc!!.shortName)
        assertEquals("longName", abc.name)
        assertEquals(0, status.issues.size, status.issues.toString() )
        assertEquals(0, astNodes.size)
    }

    @Test
    fun commentTest() = getParser().run {
        input = """
            /* Test 1 */
            package p; 
            /*123*/
        """
        parse()
        model.assertNoIssues()
    }


    @Test
    fun syntaxTest() = getParser().run {
        input = """
            package p {
                //*123*/
                part def Vehicle {
                    part eng : Component;
                    attribute weight: Real = 5.0 + 3.0 + 5.0; 
                    part wheels : Component; 
                }
                part car : Vehicle;  
            }
            part def car :> p::Vehicle; 
        """
        semantics.initOwningNamespaces()
        parse()
        model.assertNoIssues()
    }

    @Test
    fun syntaxTestMultiplicity(): Unit = getParser().run {
        input = """
             package tree {          
                part def trunk;  
                part branch [1 .. 8];
             }
        """
        semantics.initOwningNamespaces()
        parse()
        model.assertNoIssues()
    }

    @Test
    fun testExprKeyword() : Unit = assertTimeoutPreemptively(5.seconds.toJavaDuration()) {
        KerML(model = SessionImplementation(libraries = mutableListOf())).run {
            input = """
                package p {
                    expr x : ScalarValues::Boolean = a and b;
                    // examples from standard:
                    expr : Dynamics {
                        in initialState;
                        in time;
                        return result : VehicleState =
                        vehicleComputation(initialState, time);
                    }
                    expr vehicleComputation subsets computation {
                        return : VehicleState;
                    }
                    expr computation : ComputeDynamics {
                        in state;
                        in dt;
                        return result;
                    }
                }
            """
            semantics.initOwningNamespaces()
            assertTimeoutPreemptively(5.seconds.toJavaDuration()) {
                parse()
            }
            model.assertNoIssues()
        }
    }
}
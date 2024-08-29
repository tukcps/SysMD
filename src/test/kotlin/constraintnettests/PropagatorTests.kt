package sysmltests.constraintnettests

import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.functions.numInternalNodes
import com.github.tukcps.sysmd.cspsolver.DiscreteSolver
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PropagatorTests {

    //Test to find remaining Null pointers... just make sure no exception is thrown.
    @Test
    fun propagationByPropagatorsTest() = testSession {
        loadSysMD(input = """
                    attribute a: ScalarValues::Boolean;
                    attribute b: ScalarValues::Boolean;
                    attribute c: ScalarValues::Boolean;
                    attribute y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
                    attribute z: ScalarValues::Boolean(true).
                """
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    //Test if all don't cares are found
    @Test
    fun propagationByPropagatorsDontCareTest()  = testSession {
        loadSysMD(input = """
                attribute a: ScalarValues::Boolean;
                attribute b: ScalarValues::Boolean(false);
                attribute c: ScalarValues::Boolean(true);
                attribute y: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a));
                attribute z: ScalarValues::Boolean(true);
            """
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        initialize()
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(1, global.resolveVar("a")!!.vectorQuantity.value.numInternalNodes())
    }

    @Test
    fun propagationByPropagatorsUnitClausesTest() = testSession {
        loadSysMD(input = """
                    attribute a: ScalarValues::Boolean;
                    attribute b: ScalarValues::Boolean;
                    attribute c: ScalarValues::Boolean;
                    attribute d: ScalarValues::Boolean;
                    attribute f: ScalarValues::Boolean;
                    attribute g: ScalarValues::Boolean(true) = (a or b or c or d) and f.
                """
        ).run {
            propagate()
            assertEquals(0, status.exceptions.size, status.exceptions.toString())
            assertEquals(true, global.resolveVar("f")!!.vectorQuantity.value is BDD.Leaf)
        }
    }

    @Test @Disabled
    fun propagationByPropagatorsUnitClausesConflictTest() = testSession {
        loadSysMD(input = """
                    attribute a: ScalarValues::Boolean;
                    attribute b: ScalarValues::Boolean;
                    attribute c: ScalarValues::Boolean;
                    attribute d: ScalarValues::Boolean;
                    attribute f: ScalarValues::Boolean(false);
                    attribute g: ScalarValues::Boolean(true) = (a or b or c or d) and f.
                """
        ).run {
            settings.catchExceptions = false // otherwise, errors will be caught and reported in status.errors ...
            assertThrows<DiscreteSolver.DiscreteConflictDetectedException> {
                initialize()
                propagate()
            }
            //Assertions.assertEquals(1, status.errors.size, status.errors.toString())
            //Assertions.assertEquals(true, resolveName<ValueFeature>(global.uid!!, "f")!!.quantity.value.asBdd().isLeaf)
        }
    }
}
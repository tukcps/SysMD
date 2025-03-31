package constraintnettests

import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.functions.numInternalNodes
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.testSession

class PropagatorTests {

    //Test to find remaining Null pointers... make sure no exception is thrown.
    @Test
    fun propagationByPropagatorsTest() = testSession("ScalarValues") {
        loadKerML(input = """
                    feature a: ScalarValues::Boolean;
                    feature b: ScalarValues::Boolean;
                    feature c: ScalarValues::Boolean;
                    feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
                    feature z: ScalarValues::Boolean {:>> spec = "true";}
                """
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    //Test if all don't cares are found
    @Test
    fun propagationByPropagatorsDontCareTest()  = testSession("ScalarValues") {
        loadKerML(input = """
                feature a: ScalarValues::Boolean;
                feature b: ScalarValues::Boolean {:>> spec = "false";}
                feature c: ScalarValues::Boolean {:>> spec = "true";}
                feature y: ScalarValues::Boolean  = (a and c) or (not(b) and not(a)) {:>> spec = "true";}
                feature z: ScalarValues::Boolean {:>> spec = "true";}
            """
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        initialize()
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(1, global.resolveVar("a")!!.vectorQuantity.value.numInternalNodes())
    }

    @Test
    fun propagationByPropagatorsUnitClausesTest() = testSession("ScalarValues") {
        loadKerML(input = """
                    feature a: ScalarValues::Boolean;
                    feature b: ScalarValues::Boolean;
                    feature c: ScalarValues::Boolean;
                    feature d: ScalarValues::Boolean;
                    feature f: ScalarValues::Boolean;
                    feature g: ScalarValues::Boolean = (a or b or c or d) and f {:>> spec = "true";}
                """
        ).run {
            propagate()
            assertEquals(0, status.exceptions.size, status.exceptions.toString())
            assertEquals(true, global.resolveVar("f")!!.vectorQuantity.value is BDD.Leaf)
        }
    }

}
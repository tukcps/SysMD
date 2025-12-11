package constraintnettests.bddtests

import com.github.tukcps.sysmd.model.kerml.Feature
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession

class ConditionsTests {

    /**
     * Boolean variables are created and entered into the (model.)builder.conds table.
     */
    @Test
    fun conditionCreatedTest() = testSession("ScalarValues") {
        loadKerML("""
                feature x: ScalarValues::Boolean;
         """)
        assertEquals(0, status.issues.size, status.issues.toString())
        val x = global.resolve("x")!!.member<Feature>()!!
        val indexX = builder.conds.indexes[x.variable!!.elementId.toString()]
        assertNotNull(indexX)
    }

    /**
     * Boolean variables are created and entered into the (model.)builder.conds table.
     */
    @Test
    fun conditionCreatedTest2() = testSession("ScalarValues") {
        loadKerML("""                
            feature x: ScalarValues::Boolean;
            feature y: ScalarValues::Boolean = not(x);
        """)
        assertNoIssues()
        val x = global.resolve("x")
        val y = global.resolve("y")
        val indexX = builder.conds.indexes[x?.elementId.toString()]
        val indexY = builder.conds.indexes[y?.elementId.toString()]
        assertNotNull(indexX)
        assertNotNull(indexY)
    }
}
package constraintnettests.bddtests

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
        assertNoIssues()
        val x = solver.getVariable("x")!!
        val indexX = builder.conds.indexes[x.path]
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
        val x = solver.getVariable("x")
        val y = solver.getVariable("y")
        val indexX = builder.conds.indexes[x?.path]
        val indexY = builder.conds.indexes[y?.path]
        assertNotNull(indexX)
        assertNotNull(indexY)
    }
}
package constraintnettests.bddtests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
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
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolve<Feature>("x")!!
        val indexX = builder.conds.indexes[x.variable!!.elementId.toString()]
        assertNotNull(indexX)
    }

    /**
     * Boolean variables are created and entered into the (model.)builder.conds table.
     */
    @Test
    fun conditionCreatedTest2() = testSession("ScalarValues") {
        loadKerML("""                
            feature x: ScalarValues::Boolean.
            feature y: ScalarValues::Boolean = not(x).
            """.trimIndent(),
            catchExceptions = false
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolve<Feature>("x")!!
        val y = global.resolve<Feature>("y")!!
        val indexX = builder.conds.indexes[x.elementId.toString()]
        val indexY = builder.conds.indexes[y.elementId.toString()]
        assertNotNull(indexX)
        assertNotNull(indexY)
    }
}
package models.expression

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.datamodel.toElement
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class VariableTests {

    @Test
    fun testSerialization1() = testSession("ISQ") {
        loadKerML("""
            feature f: ISQ::LengthValue = 2.0 m { :>> range = "2..3" ;}
            // serialized in body-field: 
            // 2..3 $$ m $$ 1.0 m;
        """, Runlevel.MODEL)
        val f = global.resolve("f")?.memberElement as Feature?
        assertNotNull(f)
        val fdao = f.toElementData()
        assertEquals("2.0 m", fdao.body?.trim() )
        val f2 = fdao.toElement(this)
        assertTrue(f2 is Feature)
        assertEquals("2.0 m", f2.expression?.trim())
        // assertEquals(f.unitConstraint?.trim(), f2.unitConstraint?.trim())
    }

    @Test
    fun pipelineTest() {
        assertTrue(true)
    }
}
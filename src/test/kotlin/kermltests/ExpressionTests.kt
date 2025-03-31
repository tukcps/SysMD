package kermltests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


class ExpressionTests {
    @Test
    fun testExpression() = testSession("ScalarValues") {
        loadKerML("""
                feature f: ScalarValues::Real = oneOf(1.0 .. 2.0);    
            """)
        val f = global.resolve<Feature>("f")
        assertEquals(2.0, f!!.variable!!.max())
    }
}
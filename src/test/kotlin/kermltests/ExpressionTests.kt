package kermltests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.Test
import kotlin.test.assertEquals


class ExpressionTests {
    @Test
    fun testExpression() = testSession {
        +("""
            feature f: ScalarValues::Real = 1.0 .. 2.0;    
        """.trimIndent())
        val f = global.resolve<Feature>("f")
        assertEquals(2.0, f!!.variable!!.max())
    }
}
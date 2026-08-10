package compiler.kerml.core

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RangeUnitConstraintTests {
    @Test
    fun constraintIsGeneratedTest() {
        val elements = KerML(SessionImplementation()).parse("""
            feature f(1.0 .. 2.0): ScalarValues::Real; 
        """)
        val range = elements.first { it.declaredName == "range" }
        assertEquals("1 .. 2", range.body)
    }

    @Test
    fun constraintIsGeneratedTestWithRedef() {
        val session = SessionImplementation()
        val elements = KerML(session).parse(
            """
            feature f: ScalarValues::Real {
              :>> range = 1.0 .. 2.0; 
            }
        """
        )
        session.assertNoIssues()
        val range = elements.first { it.declaredName == "range" }
        assertEquals("1.0 .. 2.0", range.body)
    }

    @Test
    fun constraintIsGeneratedTestWithRedef2() {
        val session = SessionImplementation()
        val elements = KerML(session).parse("""
            feature f: ScalarValues::Real {
              :>> range = (1.0 .. 2.0, 3..4) [m]; 
            }
        """)
        session.assertNoIssues()
        val range = elements.first { it.declaredName == "range" }
        assertEquals("(1.0 .. 2.0, 3..4) [m]", range.body)
    }

    @Test
    fun constraintIsGeneratedTestInSession() = testSession("Ranges") {
        loadKerML("""
            feature f: ScalarValues::Real {
              :>> range = (1.0 .. 2.0, 3..4) [m]; 
            }
        """)
        assertNoIssues()
        val f = global.resolve("f")!!.member<Feature>()
        assertNotNull(f)
        val range = global.resolve("f::range")!!.member<Feature>()
        assertNotNull(range)
        val unit = f.unitConstraint
        assertEquals("m", unit)
        val const = f.typeConstraint
        assertEquals(2, const.size)
    }
}
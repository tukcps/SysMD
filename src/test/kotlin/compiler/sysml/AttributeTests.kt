package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.MultiplicityRange
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AttributeTests {
    @Test
    fun testSimpleAttribute() = testSession("ScalarValues") {
        loadSysMLv2("attribute <aa> a;")
        assertNoIssues()
        val a = global.resolve("a")?.member<Feature>()
        val aa = global.resolve("aa")?.member<Feature>()
        assertNotNull(a)
        assertEquals(aa, a)
        assertEquals("a", a.declaredName)
        assertEquals("aa", a.declaredShortName)
    }

    @Test
    fun testTypedAttribute() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute <aa> a: ScalarValues::Real; 
        """)
        assertNoIssues()
        val a = global.resolve("a")?.member<Feature>()
        assertNotNull(a)
        assertEquals("a", a.declaredName)
        assertEquals("aa", a.declaredShortName)
        assertEquals(repo.realType as Type, a.type.first())
    }

    @Test
    fun testAttributeMultiplicityDefault() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute <aa> a: ScalarValues::Real; 
        """)
        assertNoIssues()
        val a = global.resolve("a")?.member<Feature>()
        assertNotNull(a)
        assertEquals(MultiplicityRange(1, 1), a.multiplicityRange)
    }

    @Test
    fun testTypedAttributeWithMultiplicity() = testSession("Attributes") {
        loadSysMLv2("attribute <aa> a: ScalarValues::Real [1 .. 3];")
        assertNoIssues()
        val a = global.resolve("a")?.member<Feature>()
        assertNotNull(a)
        assertEquals(MultiplicityRange(1, 3), a.multiplicityRange)
    }

    @Test
    fun parseUnitTest() = testSession("ISQ") {
        loadSysMLv2(""" 
            attribute x: ISQ::SpeedValue = 10.0 [m/s];
        """)
        assertNoIssues()
        val x = global.resolve("x")?.member<Feature>()
        assertNotNull(x)
        val expr = x.expression
        assertEquals("10.0 [m/s]", expr)
    }
}
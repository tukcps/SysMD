package compiler.sysml

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.IntegerRange
import org.junit.jupiter.api.Assertions
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
        loadSysMLv2(
            """
            attribute <aa> a: ScalarValues::Real; 
        """
        )
        assertNoIssues()
        val a = global.resolve("a")?.member<Feature>()
        assertNotNull(a)
        assertEquals("a", a.declaredName)
        assertEquals("aa", a.declaredShortName)
        assertEquals(repo.realType, a.type.first())
        assertEquals(IntegerRange(1, 1), a.multiplicityRange)
    }

    @Test
    fun testTypedAttributeWithMultiplicity() = testSession("ScalarValues") {
        loadSysMLv2(
            """
            attribute <aa> a: ScalarValues::Real [1 .. 3]; 
        """
        )
        assertNoIssues()
        val a = global.resolve("a")?.member<Feature>()
        assertNotNull(a)
        assertEquals("a", a.declaredName)
        assertEquals("aa", a.declaredShortName)
        assertEquals(repo.realType, a.type.first())
        assertEquals(IntegerRange(1, 3), a.multiplicityRange)
    }

    @Test
    fun parseUnitTest() = testSession("ISQ") {
        loadSysMLv2(
            """ 
            attribute x: ISQ::SpeedValue = 10.0 [m/s];
        """
        )
        solver.propagate()
        assertNoIssues()
        val x = global.resolve("x")?.member<Feature>()
        assertNotNull(x)
        val unit = x.variable?.vectorQuantity?.unit
        Assertions.assertEquals("m / s", unit.toString())
        Assertions.assertEquals(
            0.01, global.resolveVar("x")!!.vectorQuantity.valuesIn("km/s")[0].asAadd().getRange().min, 0.000001
        )
    }
}
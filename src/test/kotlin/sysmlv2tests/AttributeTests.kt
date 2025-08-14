package sysmlv2tests

import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadSysMLv2
import org.junit.jupiter.api.Assertions
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AttributeTests {
    @Test
    fun testSimpleAttribute() = testSession("ScalarValues") {
        loadSysMLv2("attribute <aa> a;")
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<Feature>("a")
        assertNotNull(a)
        assertEquals("a", a.declaredName)
        assertEquals("aa", a.declaredShortName)
    }

    @Test
    fun testTypedAttribute() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute <aa> a: ScalarValues::Real; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<Feature>("a")
        assertNotNull(a)
        assertEquals("a", a.declaredName)
        assertEquals("aa", a.declaredShortName)
        assertEquals(repo.realType, a.type.first())
        assertEquals(IntegerRange(1, 1) , a.multiplicityRange)
    }

    @Test
    fun testTypedAttributeWithMultiplicity() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute <aa> a: ScalarValues::Real [1 .. 3]; 
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<Feature>("a")
        assertNotNull(a)
        assertEquals("a", a.declaredName)
        assertEquals("aa", a.declaredShortName)
        assertEquals(repo.realType, a.type.first())
        assertEquals(IntegerRange(1, 3) , a.multiplicityRange)
    }

    @Test
    fun parseUnitTest() = testSession("SI") {
        loadSysMLv2(""" 
            attribute x: SI::Speed = 10.0 [m/s];
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val x = global.resolve<Feature>("x")
        assertNotNull(x)
        val unit= x.variable?.vectorQuantity?.unit
        Assertions.assertEquals("m / s", unit.toString())
        Assertions.assertEquals(0.01, global.resolveVar("x")!!.vectorQuantity.valuesIn("km/s")[0].asAadd().getRange().min, 0.000001
        )
    }
}
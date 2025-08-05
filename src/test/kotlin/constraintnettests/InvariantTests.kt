package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Assertions
import util.mockup.loadKerML
import util.testSession
import util.assertNoIssues
import util.mockup.loadSysMLv2
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InvariantTests {

    @Test
    fun restrictInteger1() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight >= 30 }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }
    @Test
    fun restrictInteger1b() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { 30 >= weight }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger2() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight > 30 }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(31, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger2b() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { 30 > weight }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(29, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger3() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight <= 30 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger3a() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { 30 <= weight }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger4() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight < 30 }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(29, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger4a() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { 30 < weight }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(31, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictIntegerEmpty() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight <= -10 }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }
    @Test
    fun restrictIntegerEmpty1() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight < -10 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }
    @Test
    fun restrictIntegerEmpty2() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight >= 60 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }
    @Test
    fun restrictIntegerEmpty3() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = "0..50";}
                assert constraint r { weight > 60 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }

    @Test
    fun assertTestIntDiv() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: Ranges::IntegerInRange = oneOf(1 .. 4); 
            assert constraint ass { 12 / f < 6 } 
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        Assertions.assertEquals(2, global.resolveVar("f")!!.idd().getRange().min)
        Assertions.assertEquals(4, global.resolveVar("f")!!.idd().getRange().max)
    }

    @Test
    fun assertTestIntMultiplication() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: Ranges::IntegerInRange = oneOf(1 .. 4); 
            assert constraint ass { 12 * f > 24 } 
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        Assertions.assertEquals(2, global.resolveVar("f")!!.idd().getRange().min)
        Assertions.assertEquals(4, global.resolveVar("f")!!.idd().getRange().max)
    }
    
    @Test
    fun restrictReal1() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = "0..50";}
                inv r { weight <= 30.0 }
        """)
        assertNoIssues()
        propagate()
        assertEquals(0.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test  // Problem with evalDown of Requirement vs. Expression
    fun restrictReal2() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = "0..50";} 
                inv r { weight >= 30.0 }
        """)
        assertNoIssues()
        propagate()
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(50.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test // Problem with evalDown of ScalarValues::Requirement vs. Expression
    fun restrictReal3() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = "0..50";}
                inv r { weight > 30.0 }
        """)
        propagate()
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(50.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun restrictReal4() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = "0..50";}
                inv r { weight < 30.0 }
        """)
        propagate()
        assertEquals(0.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun assertTestReal() = testSession("Ranges") {
        loadKerML("""   
                feature a: Ranges::RealInRange {:>> range = "1..5";}
                feature b: Ranges::RealInRange {:>> range = "4..6";}
                inv c { a == b }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(4.0, global.resolve<Feature>("a")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(5.0, global.resolve<Feature>("a")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
        assertEquals(4.0, global.resolve<Feature>("b")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(5.0, global.resolve<Feature>("b")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun evalUpITEEquality() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 4;
            feature b: ScalarValues::Integer = 5;
            feature b1: ScalarValues::Boolean = a == 5;
            feature b2: ScalarValues::Boolean = a == 4;
            feature c: ScalarValues::Integer = if b == 6 ? 7 else 6;"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(6, global.resolve<Feature>("c")!!.variable!!.idd().getRange().min)
        assertEquals(6, global.resolve<Feature>("c")!!.variable!!.idd().getRange().max)
    }

    @Test
    fun evalUpRealITEEquality() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 4.0;
            feature b: ScalarValues::Real = 5.0;
            feature b1: ScalarValues::Boolean = a == 5.0;
            feature b2: ScalarValues::Boolean = a == 4.0;
            feature c: ScalarValues::Real = if b == 6.0 ? 7.0 else 6.0; 
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(6.0, global.resolve<Feature>("c")!!.variable!!.aadd().getRange().min)
        assertEquals(6.0, global.resolve<Feature>("c")!!.variable!!.aadd().getRange().max)
    }

    @Test
    fun evalDownEquality() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer; 
            feature b: ScalarValues::Integer = 5; 
            inv { a == b } 
        """)
        propagate()
        assertNoIssues()
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().min)
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().max)
    }

    @Test @Ignore
    fun restrictRealMultiplication() = testSession("Ranges") {
        loadSysMLv2(
            """    
                private import ScalarValues::*;
                attribute result: Real = 1.0..4.0 * 1.0..2.0;
                assert constraint range {result<=2.0}
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(2.0, global.resolve<Feature>("result")!!.variable!!.vectorQuantity.value.asAadd().max,0.00001)
    }
}

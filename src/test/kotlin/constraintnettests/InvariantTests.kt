package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import org.junit.jupiter.api.Assertions
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.testSession
import org.junit.jupiter.api.Assertions.assertTrue
import util.mockup.loadSysMLv2

class InvariantTests {

    @Test
    fun restrictInteger1() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight >= 30 }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }
    @Test
    fun restrictInteger1b() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { 30 >= weight }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger2() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight > 30 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(31, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger2b() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { 30 > weight }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(29, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger3() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight <= 30 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger3a() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { 30 <= weight }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger4() = testSession("ScalarValues") {
        loadSysMLv2("""    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight < 30 }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(29, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictInteger4a() = testSession("ScalarValues") {
        loadSysMLv2("""    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { 30 < weight }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(31, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictIntegerEmpty() = testSession("ScalarValues") {
        loadSysMLv2("""    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight <= -10 }
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }
    @Test
    fun restrictIntegerEmpty1() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight < -10 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }
    @Test
    fun restrictIntegerEmpty2() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight >= 60 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }
    @Test
    fun restrictIntegerEmpty3() = testSession("ScalarValues") {
        loadSysMLv2(
            """    
                attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                assert r { weight > 60 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assert(global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().isEmpty())
    }

    @Test
    fun assertTestIntDiv() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Integer = oneOf(1 .. 4); 
            assert ass { 12 / f < 6 } 
        """)
        propagate()
        kotlin.test.assertTrue(status.issues.isEmpty(), status.issues.toString())
        Assertions.assertEquals(2, global.resolveVar("f")!!.idd().getRange().min)
        Assertions.assertEquals(4, global.resolveVar("f")!!.idd().getRange().max)
    }

    @Test
    fun assertTestIntMult() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Integer = oneOf(1 .. 4); 
            assert ass { 12 * f > 24 } 
        """)
        propagate()
        kotlin.test.assertTrue(status.issues.isEmpty(), status.issues.toString())
        Assertions.assertEquals(2, global.resolveVar("f")!!.idd().getRange().min)
        Assertions.assertEquals(4, global.resolveVar("f")!!.idd().getRange().max)
    }
    @Test
    fun restrictReal1() = testSession("ScalarValues") {
        loadKerML("""   
                feature weight: ScalarValues::Real {:>> range = "0..50";}
                inv r { weight <= 30.0 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test  // Problem with evalDown of Requirement vs. Expression
    fun restrictReal2() = testSession("ScalarValues") {
        loadKerML("""   
                feature weight: ScalarValues::Real {:>> range = "0..50";} 
                inv r { weight >= 30.0 }
        """.trimIndent()
        )
        propagate()
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(50.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test // Problem with evalDown of ScalarValues::Requirement vs. Expression
    fun restrictReal3() = testSession("ScalarValues") {
        loadKerML("""   
                feature weight: ScalarValues::Real {:>> range = "0..50";}
                inv r { weight > 30.0 }
        """.trimIndent())
        propagate()
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(50.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun restrictReal4() = testSession("ScalarValues") {
        loadKerML("""   
                feature weight: ScalarValues::Real {:>> range = "0..50";}
                inv r { weight < 30.0 }
        """.trimIndent())
        propagate()
        assertEquals(0.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun assertTestReal() = testSession("ScalarValues") {
        loadKerML("""   
                feature a: ScalarValues::Real {:>> range = "1..5";}
                feature b: ScalarValues::Real {:>> range = "4..6";}
                inv c { a == b }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        propagate()
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
            feature c: ScalarValues::Real = if b == 6.0 ? 7.0 else 6.0."""
        )
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
            feature c: ScalarValues::Boolean = (a == b) {:>> spec = "true";}"""
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().min)
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().max)
    }
}

package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import util.testSession
import org.junit.jupiter.api.Assertions.assertTrue

class InvariantTests {

    @Test @Disabled //ITE does not work for IDD in jAADD (See issue #34 in jAADD)
    fun restrictInteger() = testSession {
        loadKerML(
            """                
            package a {
                class b {
                    attribute weight: ScalarValues::Integer {:>> range = "0..50";}
                    inv r { weight <= 30 } 
                }
            }
        """)
        assertTrue(status.issues.isEmpty())
        propagate()
        assertEquals(0, global.resolve<Feature>("a::b::weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("a::b::weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test @Disabled //ITE does not work for IDD in jAADD (See issue #34 in jAADD)
    fun restrictInteger2() = testSession {
        loadKerML(
            """                
            package a {
                class b;
            }
          
            a::b hasA
                attribute weight: Integer {:>> range = "0..50";},
                inv r { weight >= 30 }
        """.trimIndent()
        )
        propagate()
        assertEquals(30, global.resolveVar("a::b::weight")!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolveVar("a::b::weight")!!.vectorQuantity.value.asIdd().max)
    }


    @Test @Disabled //ITE does not work for IDD in jAADD (See issue #34 in jAADD)
    fun restrictInteger4() = testSession {
        loadKerML(
            """  
                feature weight: ScalarValues::Integer {:>> range = "0..50";}
                feature r: ScalarValues::Requirement = weight < 30.0.
        """.trimIndent()
        )
        propagate()
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
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

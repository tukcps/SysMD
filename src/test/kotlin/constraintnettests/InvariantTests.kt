package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class InvariantTests {

    @Test @Disabled //ITE does not work for IDD in jAADD (See issue #34 in jAADD)
    fun restrictInteger() = testSession(catchExceptions = false) {
        loadSysMD(
            """                
            package a {
                class b {
                    attribute weight: ScalarValues::Integer(0..50); 
                    inv r { weight <= 30 } 
                }
            }
        """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty())
        propagate()
        assertEquals(0, global.resolve<Feature>("a::b::weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("a::b::weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test @Disabled //ITE does not work for IDD in jAADD (See issue #34 in jAADD)
    fun restrictInteger2() = testSession(catchExceptions = false) {
        loadSysMD(
            """                
            package a {
                class b;
            }
          
            a::b hasA
                attribute weight: Integer(0..50),
                inv r { weight >= 30 }
        """.trimIndent()
        )
        propagate()
        assertEquals(30, global.resolveVar("a::b::weight")!!.vectorQuantity.value.asIdd().min)
        assertEquals(50, global.resolveVar("a::b::weight")!!.vectorQuantity.value.asIdd().max)
    }


    @Test @Disabled //ITE does not work for IDD in jAADD (See issue #34 in jAADD)
    fun restrictInteger4() = testSession(catchExceptions = false) {
        loadSysMD(
            """  
                feature weight: ScalarValues::Integer(0..50);
                feature r: ScalarValues::Requirement = weight < 30.0.
        """.trimIndent()
        )
        propagate()
        assertEquals(0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().min)
        assertEquals(30, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun restrictReal1() = testSession(catchExceptions = false) {
        loadSysMD("""   
                feature weight: ScalarValues::Real(0..50);
                inv r { weight <= 30.0 }
        """.trimIndent()
        )
        propagate()
        assertEquals(0.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test  // Problem with evalDown of Requirement vs. Expression
    fun restrictReal2() = testSession(catchExceptions = false) {
        loadSysMD(
            """   
                feature weight: ScalarValues::Real(0..50); 
                inv r { weight >= 30.0 }
        """.trimIndent()
        )
        propagate()
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(50.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test // Problem with evalDown of ScalarValues::Requirement vs Expression
    fun restrictReal3() = testSession(catchExceptions = false) {
        loadSysMD("""   
                feature weight: ScalarValues::Real(0..50);
                inv r { weight > 30.0 }
        """.trimIndent())
        propagate()
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(50.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun restrictReal4() = testSession(catchExceptions = false) {
        loadSysMD("""   
                feature weight: ScalarValues::Real(0..50);
                inv r { weight < 30.0 }
        """.trimIndent())
        propagate()
        assertEquals(0.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(30.0, global.resolve<Feature>("weight")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun assertTestReal() = testSession(catchExceptions = false) {
        loadSysMD("""   
                feature a: ScalarValues::Real(1..5);
                feature b: ScalarValues::Real(4..6);
                assert c {a == b}
        """.trimIndent())
        propagate()
        assertEquals(4.0, global.resolve<Feature>("a")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(5.0, global.resolve<Feature>("a")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
        assertEquals(4.0, global.resolve<Feature>("b")!!.variable!!.vectorQuantity.value.asAadd().min, 0.00001)
        assertEquals(5.0, global.resolve<Feature>("b")!!.variable!!.vectorQuantity.value.asAadd().max, 0.00001)
    }

    @Test
    fun evalUpITEEquality() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Integer = 4;
            feature b: ScalarValues::Integer = 5;
            feature b1: ScalarValues::Boolean = a == 5;
            feature b2: ScalarValues::Boolean = a == 4;
            feature c: ScalarValues::Integer = if b == 6 ? 7 else 6;"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(6, global.resolve<Feature>("c")!!.variable!!.idd().getRange().min)
        assertEquals(6, global.resolve<Feature>("c")!!.variable!!.idd().getRange().max)
    }

    @Test
    fun evalUpRealITEEquality() = testSession {
        loadSysMD("""
            feature a: ScalarValues::Real = 4.0;
            feature b: ScalarValues::Real = 5.0;
            feature b1: ScalarValues::Boolean = a == 5.0;
            feature b2: ScalarValues::Boolean = a == 4.0;
            feature c: ScalarValues::Real = if b == 6.0 ? 7.0 else 6.0."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(6.0, global.resolve<Feature>("c")!!.variable!!.aadd().getRange().min)
        assertEquals(6.0, global.resolve<Feature>("c")!!.variable!!.aadd().getRange().max)
    }

    @Test @Disabled
    fun evalDownEquality() = testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Integer,
            attribute b: ScalarValues::Integer = 5,
            attribute c: ScalarValues::Boolean(true) = a == b."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().min)
        assertEquals(5, global.resolve<Feature>("a")!!.variable!!.idd().getRange().max)
    }
}

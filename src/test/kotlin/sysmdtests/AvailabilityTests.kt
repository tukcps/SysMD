package sysmdtests

import io.github.tukcps.aadd.values.XBool
import io.github.tukcps.aadd.values.XBool.Companion.True
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.estimateFeature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AvailabilityTests {


    /**
     * We model the availability of a component at a given time with the ITE function.
     * T is a Real-valued property that is part of a class Context.
     */
    @Test @Ignore
    fun computeAvailabilitySimple() = testSession("ISO26262") {
        loadKerML("""
            package t {
                class Context :> ISO26262::Element {
                    expression T: ScalarValues::Real; 
                }
                class c1 isA Component {
                    import t::Context::*; 
                    expression Availability: ScalarValues::Boolean = ITE(T>2035.0, true, false); 
                }
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        // println(global.resolveName<Namespace>(qualifiedName = "t::c1") !!.resolveName<Expression>("Availability")!!.bdd().toIteString())
        assertEquals(3, global.resolve<Namespace>(qualifiedName = "t::c1") !!.resolveVar("Availability")!!.bdd().height())
    }

    /**
     * Check the correct behavior of Availability in case of inheritance.
     * An element is available if at least one subclass of it is available.
     */
    @Test
    fun computeAvailabilityDerivedFrom1() = testSession("Occurrences") {
        loadKerML("""
            class Context  {
                feature T: ScalarValues::Real; 
            }
            class c1;             
            class c2 :> c1 {
                feature Availability: ScalarValues::Boolean(true) = true; 
            }
            class c3 :> c1 {              // Possible Refinements of c1
                feature Availability: ScalarValues::Boolean(true) = true; 
            }
        """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val tc1 = global.resolve<Type>("c1")!!
        assertEquals(builder.True, estimateFeature(tc1, "Availability").bdd())
    }


    @Test @Ignore
    fun computeAvailabilityDerivedFrom2() = testSession("ISO26262") {
        loadKerML(""" 
            package t {
                class c1 :> ISO26262::Component; 
                class c2 :> c1 { 
                    feature Availability: ScalarValues::Boolean = true; 
                }
                class c3 :> c1 {
                    feature Availability: ScalarValues::Boolean = true; 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val tc1 = global.resolve<Type>("t::c1")!!
        assertEquals(builder.True, estimateFeature(tc1, "Availability").bdd())
    }

    @Test
    fun computeAvailabilityDerivedWithSubclasses() = testSession("Occurrences") {
        loadKerML(""" 
            feature T: ScalarValues::Real(1000 .. 3000);
            class c1 {
                feature Availability: ScalarValues::Boolean = bySpecializations(Availability); 
            }
            class c2 :> c1 {
                feature Availability: ScalarValues::Boolean = if T>2030.0 ? true else false;
            }
            class c3 :> c1 {
                feature Availability: ScalarValues::Boolean = if T>2040.0 ? true else false; 
            }
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val tc1 = global.resolve<Namespace>("c1") !!
        global.resolve<Namespace>("c2") !!
        assertEquals(XBool.X, tc1.resolveVar("Availability")?.vectorQuantity?.value as XBool)
        loadKerML("feature T: ScalarValues::Real(2020); ")
        assertEquals(2020.0 ,global.resolveVar("T")!!.min(), 0.01)
        var tc1Availability = global.resolveVar("c1::Availability")?.bdd()
        assertEquals(XBool.False, tc1Availability as XBool)
        loadKerML("feature T: ScalarValues::Real(2050); ")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        tc1Availability = global.resolveVar("c1::Availability")?.bdd()
        assertEquals(True, tc1Availability as XBool)
        assertEquals(2050.0, global.resolveVar("T")!!.min(), 0.01)
    }

    @Test @ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE) @Ignore
    fun computeCarOverTime() = testSession("ISO26262", "Context") {
        loadKerML("""
            class test :> ISO26262::Element {
                feature a: ScalarValues::Real = ITE(Context::timeOfProcurement > 2030.0, 10.0, 20.0); 
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
package sysmdtests

import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.aadd.values.XBool.Companion.True
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.estimateProperty
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class AvailabilityTests {


    /**
     * We model the availability of a component at a given time with the ITE function.
     * T is a Real-valued property that is part of a class Context.
     */
    @Test @Disabled
    fun computeAvailabilitySimple() = testSession("ISO26262") {
        loadSysMD("""
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
    fun computeAvailabilityDerivedFrom1() = testSession {
        loadSysMD("""
            class Context  {
                feature T: ScalarValues::Real; 
            }
            class c1;             
            class c2 isA c1; 
            class c3 isA c1;              // Possible Refinements of c1
            c2 hasA feature Availability: ScalarValues::Boolean(true) = true.
            c3 hasA feature Availability: ScalarValues::Boolean(true) = true.
        """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val tc1 = global.resolve<Namespace>("c1")!!
        assertEquals(builder.True, estimateProperty(tc1, "Availability").bdd())
    }


    @Test
    fun computeAvailabilityDerivedFrom2() = testSession("ISO26262") {
        loadSysMD(""" 
            package t {
                class c1 isA ISO26262::Component; 
                class c2 isA c1 { 
                    feature Availability: ScalarValues::Boolean = true; 
                }
                class c3 isA c1 {
                    feature Availability: ScalarValues::Boolean = true; 
                }
            }
        """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val tc1 = global.resolve<Namespace>("t::c1")!!
        assertEquals(builder.True, estimateProperty(tc1, "Availability").bdd())
    }

    @Test
    fun computeAvailabilityDerivedWithSubclasses() = testSession("ISO26262"){
        loadSysMD(""" 
            attribute T: ScalarValues::Real(1000 .. 3000); 
            class c1; class c2 isA c1; class c3 isA c1; 
            c1 hasA attribute Availability: ScalarValues::Boolean = bySubclasses(Availability). 
            c2 hasA attribute Availability: ScalarValues::Boolean = if T>2030.0 ? true else false.
            c3 hasA attribute Availability: ScalarValues::Boolean = if T>2040.0 ? true else false.
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val tc1 = global.resolve<Namespace>("c1") !!
        global.resolve<Namespace>("c2") !!
        assertEquals(XBool.X, tc1.resolveVar("Availability")?.vectorQuantity?.value as XBool)
        loadSysMD("attribute T: Real(2020)")
        assertEquals(2020.0 ,global.resolveVar("T")!!.min(), 0.01)
        var tc1Availability = global.resolveVar("c1::Availability")?.bdd()
        assertEquals(XBool.False, tc1Availability as XBool)
        loadSysMD("attribute T: Real(2050)")
        tc1Availability = global.resolveVar("c1::Availability")?.bdd()
        assertEquals(True, tc1Availability as XBool)
        assertEquals(2050.0, global.resolveVar("T")!!.min(), 0.01)
    }

    @Test
    fun computeCarOverTime() = testSession("ISO26262", "Context") {
        loadSysMD("""
            class test :> ISO26262::Element {
                feature a: ScalarValues::Real = ITE(Context::timeOfProcurement > 2030.0, 10.0, 20.0); 
            }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
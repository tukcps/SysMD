package solver

import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.estimateFeature
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.XBool
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityTests {

    /**
     * We model the availability of a component at a given time with the ITE function.
     * T is a Real-valued property that is part of a class Context.
     */
    @Test
    fun computeAvailabilitySimple() = testSession("Occurrences") {
        loadKerML(
            """
            package t {
                class Context  {
                    feature T: ScalarValues::Real; 
                }
                class c1 :> Context {
                    feature Availability: ScalarValues::Boolean = ITE(T>2035.0, true, false); 
                }
            }
        """
        )
        assertNoIssues()
        solver.propagate()
        val availability = solver.getVariable("t::c1::Availability")
        assertEquals(availability!!.bool(), XBool.X)
    }

    /**
     * Check the correct behavior of Availability in case of inheritance.
     * An element is available if at least one subclass of it is available.
     */
    @Test
    fun computeAvailabilityDerivedFrom1() = testSession("Occurrences") {
        loadKerML(
            """
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
        """
        )
        assertNoIssues()
        solver.propagate()
        val tc1 = global.resolve("c1")!!.member<Type>()!!
        assertEquals(builder.True, estimateFeature(tc1, "Availability").bdd())
    }


    @Test
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
        """, Runlevel.ALL)
        assertNoIssues()
        val tc1 = global.resolve("t::c1")!!.member<Type>()!!
        assertEquals(builder.True, estimateFeature(tc1, "Availability").bdd())
    }

    // Needs adaption to SysMD for interactive work.
    @Ignore
    @Test
    fun computeAvailabilityDerivedWithSubclasses() = testSession("Occurrences", "Ranges") {
        loadKerML(""" 
            feature T: Ranges::RealInRange [1 ..1] {:>> range = 1000 .. 3000;}
            class c1 {
                feature Availability: ScalarValues::Boolean [1 ..1] = bySpecializations(Availability); 
            }
            class c2 :> c1 {
                feature Availability: ScalarValues::Boolean [1 ..1] = if T>2030.0 ? true else false;
            }
            class c3 :> c1 {
                feature Availability: ScalarValues::Boolean [1 ..1] = if T>2040.0 ? true else false; 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val tc1 = global.resolve("c1")!!.member<Type>()!!
        global.resolve("c2")!!.member<Namespace>()
        assertEquals(XBool.X, tc1.resolveVar("Availability")?.vectorQuantity?.value as XBool)
        loadKerML("feature T: ScalarValues::Real(2020) [1 ..1]; ")
        assertEquals(2020.0, solver.getVariable("T")!!.min(), 0.01)
        var tc1Availability = solver.getVariable("c1::Availability")?.bdd()
        assertEquals(XBool.False, tc1Availability as XBool)
        loadKerML("feature T: ScalarValues::Real(2050) [1 ..1]; ")
        assertNoIssues()
        tc1Availability = solver.getVariable("c1::Availability")?.bdd()
        assertEquals(XBool.True, tc1Availability as XBool)
        assertEquals(2050.0, solver.getVariable("T")!!.min(), 0.01)
    }

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE) @Ignore
    fun computeCarOverTime() = testSession("ISO26262", "Context") {
        loadKerML(
            """
            class test :> ISO26262::Element {
                feature a: ScalarValues::Real = ITE(Context::timeOfProcurement > 2030.0, 10.0, 20.0); 
            }
        """
        )
        assertNoIssues()
    }
}
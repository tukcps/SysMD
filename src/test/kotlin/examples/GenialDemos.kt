package examples

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class Demos  {

    @Test
    fun neuralNetworkEstimation() {
        testSession  {
            +"feature C: ScalarValues::Real(8.0..8.0)."
            +"feature C_w: ScalarValues::Real(2.0..2.0)."
            +"feature K: ScalarValues::Real(2.0..2.0)."
            +"feature F: ScalarValues::Real(15.0..15.0)."
            +"feature ns: ScalarValues::Real(2.0..2.0)."
            +"feature p: ScalarValues::Boolean(true)."
            +"feature i: ScalarValues::Real(0.0)."

            +"feature s: ScalarValues::Real = power2(ns)"
            +"feature C_w_hat: ScalarValues::Real = ITE(p, C_w + F, C_w)"                           // eq. 8
            +"feature a_w: ScalarValues::Real = ((C_w_hat - F)/s + 1.0)"                            // eq. 9
            +"feature C_wb: ScalarValues::Real = F/2.0"                                             // eq. 10
            +"feature a_pb: ScalarValues::Real = ITE(p, (C_wb - 1.0)/s + 1.0, 0.0)"                 // eq. 11
            +"feature MAC_notb: ScalarValues::Real = sum_i(0.0, a_pb-1.0, F/2.0-s*i )"             // eq. 12
            +"feature Fw: ScalarValues::Real = a_w * s + F - s"                                     // eq. 13
            +"feature C_we: ScalarValues::Real = Fw - C_w - C_wb"                                   // eq. 14
            +"feature a_pe: ScalarValues::Real = ITE(p, (C_we - 1.0)/s + 1.0, 0.0)"                 // eq. 15
            // +"Value MAC_note: ScalarValues::Real = sum_i(0.0, a_pe-1.0, F/2.0-s*i-(C_wb - C_we))"     // eq. 16
            // +"Value t_l: ScalarValues::Real = 1.0+C/8.0 * K/8.0 * (a_w*F-MAC_notb - MAC_note)"        // eq. 17

            assertEquals(4.0, global.resolve<Feature>("s")!!.variable!!.aadd().getRange().min, 0.00001)
            // println("s = " + getVar("s") + " ")

            // println("C_w_hat = " + getVar("C_w_hat") + " ")
            assertEquals(17.0, global.resolve<Feature>("C_w_hat")!!.variable!!.aadd().getRange().min, 0.00001)

            // println("a_w = " + getVar("a_w") + " ")
            assertEquals(1.5, global.resolve<Feature>("a_w")!!.variable!!.aadd().getRange().min, 0.00001)
            assertEquals(7.5, global.resolve<Feature>("C_wb")!!.variable!!.aadd().getRange().min, 0.00001)

            // println("a_pb = " + resolveName<Expression>("a_pb"))
            assertEquals(6.5/4.0+1, global.resolve<Feature>("a_pb")!!.variable!!.aadd().getRange().min, 0.00001)

        }
    }
}

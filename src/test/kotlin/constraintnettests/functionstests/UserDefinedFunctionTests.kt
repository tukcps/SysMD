package constraintnettests.functionstests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UserDefinedFunctionTests {

        @Test
        fun evalDownUDF() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2("""
            calc def calcASIL {
                in attribute severity:        Ranges::IntegerInRange { :>> range = 0..3;}
                in attribute exposure:        Ranges::IntegerInRange { :>> range = 0..4;}
                in attribute controllability: Ranges::IntegerInRange { :>> range = 0..3;}
                attribute    sum:             ScalarValues::Integer = severity + exposure + controllability;
                attribute    sumAdapted:      ScalarValues::Integer = if controllability == 0 ? 0 else if severity == 0 ? 0 else sum; // special case for S0 and C0 the ASIL is always QM (0)
                return       result:          ScalarValues::Integer = max(sum-6,0);   
            }  

            attribute severity: ScalarValues::Integer = 3;     
            attribute exposure: ScalarValues::Integer = 4;
            attribute controllability: Ranges::IntegerInRange {:>> range = 0..3;}
            attribute ASILCalculated: Ranges::IntegerInRange = calcASIL(severity,exposure,controllability) {:>> range = 4;}           
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("controllability")
            assertEquals(3L, test2!!.min())
        }

        @Test
        fun evalDownUDF2() = testSession("Calculations", "ISQ") {
            loadSysMLv2("""
                calc def calcASIL {
                    in attribute severity:        Ranges::IntegerInRange { :>> range = 0..3; }
                    in attribute exposure:        Ranges::IntegerInRange { :>> range = 0..4; }
                    in attribute controllability: Ranges::IntegerInRange { :>> range = 0..3; }
                    attribute    sum:             ScalarValues::Integer = severity + exposure + controllability;
                    attribute    sumAdapted:      ScalarValues::Integer = if controllability == 0 ? 0 else if severity == 0 ? 0 else sum; // special case for S0 and C0 the ASIL is always QM (0)
                    return       result:          ScalarValues::Integer = max(sum-6,0);   
                }  
                attribute severity: Ranges::IntegerInRange {:>> range = 2; }
                attribute exposure: Ranges::IntegerInRange {:>> range = 3; }
                attribute controllability: Ranges::IntegerInRange {:>> range = 0..3; }
                attribute ASILCalculated: Ranges::IntegerInRange = calcASIL(severity,exposure,controllability) {:>> range = 0;}
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("controllability")
            assertNotNull(test2)
            assertEquals(0L, test2.min())
            assertEquals(1L, test2.max())
        }

        @Test
        fun evalUpAndDownUDFInt() = testSession("ISQ", "Calculations") {
            loadSysMLv2(""" 
                attribute a: Ranges::IntegerInRange {:>> range = 0..4;}
                attribute ASILFromAvailability: Ranges::IntegerInRange = a {:>> range = 0..4;}
                assert constraint ASIL {ASILFromAvailability == 4}   
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("ASILFromAvailability")
            assertEquals(4, test2!!.vectorQuantity.value.asIdd().min)
            assertEquals(4, test2.vectorQuantity.value.asIdd().max)
            //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
            //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
        }

        @Test
        fun evalUpAndDownUDFReal() = testSession("ISQ", "Calculations") {
            loadSysMLv2(""" 
                attribute a: Ranges::RealInRange {:>> range = 0.0..4.0;}
                attribute ASILFromAvailability: Ranges::RealInRange = a {:>> range = 0.0..4.0;}
                assert constraint ASIL {ASILFromAvailability == 4.0}   
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("ASILFromAvailability")
            assertEquals(4.0, test2!!.vectorQuantity.value.asAadd().min)
            assertEquals(4.0, test2.vectorQuantity.value.asAadd().max)
            //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
            //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
        }

        @Test
        fun evalDownUDF3() = testSession("Calculations", "ISQ", "Parts", "Ranges") {
            loadSysMLv2(""" 
                calc def ASIL_from_Avail {
                    in attribute Avail: Quantities::ScalarQuantityValue  { :>> range = * [%];} 
                    return level: ScalarValues::Integer =  stepInterpolation(Avail, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4);
                }  

                calc def ASIL_from_Reliab {
                    in attribute Reliab: Quantities::ScalarQuantityValue { :>> range = * [%];} 
                    return level: ScalarValues::Integer = stepInterpolation(Reliab, 0.0, 1, 0.99, 2, 0.995, 3, 0.999, 4);
                }
                attribute availability: Quantities::ScalarQuantityValue { :>> range = 90.0..100.0 [%];}
                attribute reliability: Quantities::ScalarQuantityValue { :>> range = 99.9..100.0 [%];}
                attribute ASILFromAvailability: Ranges::IntegerInRange = ASIL_from_Avail(availability) {:>> range = 0..4;}
                attribute ASIlFromReliability: Ranges::IntegerInRange = ASIL_from_Reliab(reliability) {:>> range = 0..4;}
                assert constraint ASIL {ASIlFromReliability == ASILFromAvailability}  
            """, Runlevel.ALL)
            assertNoIssues()
            val test2 = solver.getVariable("ASILFromAvailability")
            assertEquals(4, test2!!.vectorQuantity.value.asIdd().min)
            assertEquals(4, test2.vectorQuantity.value.asIdd().max)
            //assertEquals(0.999, test2!!.vectorQuantity.value.asAadd().min)
            //assertEquals(1.0, test2.vectorQuantity.value.asAadd().max)
        }

    @Test
    fun udfWithTypes() = testSession("Ranges", "Calculations", "ISQ","Attributes") {
        loadSysMLv2("""
        private import ISQ::*;
        calc def calcMTBF {
            in attribute inMTTF: DurationValue;
            in attribute inMTTR: DurationValue;
            return MTBF: DurationValue = inMTTF + inMTTR;    
        }
    """, Runlevel.ALL)
        println("UDFWITHTYPES ISSUES: ${status.issues}")
        assertNoIssues()

    }
}

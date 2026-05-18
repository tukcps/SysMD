package compiler.sysml.examples

import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test

class VerificationTests {

    /**
     * Example from OMG Tutorial,
     * Copyright OMG, LGPL !!!
     */
    @Test
    fun verificationDefTest() = testSession("Parts") {
        loadSysMLv2(""" 
            verification def VehicleMassTest {
                private import VerificationCases::*;
                subject testVehicle : Vehicle;
                objective vehicleMassVerificationObjective {
                    verify vehicleMassRequirement;
                }
                action collectData {
                    in part testVehicle : Vehicle = VehicleMassTest::testVehicle;
                    out massMeasured :> ISQ::mass;
                }
                action processData {
                    in massMeasured :> ISQ::mass = collectData.massMeasured;
                    out massProcessed :> ISQ::mass;
                }
                action evaluateData {
                    in massProcessed :> ISQ::mass = processData.massProcessed;
                    out verdict : VerdictKind =
                    PassIf(
                        vehicleMassRequirement(
                            vehicle = testVehicle,
                            massActual = massProcessed
                        )
                    );
                }
                // return verdict : VerdictKind =  // BUG in examples!  
                evaluateData.verdict
            }
        """)
        assertNoIssues { !it.message.contains("resolved") && !it.message.contains("Warning") }
    }
}
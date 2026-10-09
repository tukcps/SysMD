package compiler.sysml

import com.github.tukcps.sysmd.model.expression.Invariant
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.values.bounds.DoubleBound
import io.github.tukcps.aadd.values.bounds.DoubleBoundMath.toDouble
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class AssertAndConstraintsTests {

    @Test
    fun assertTestSyntax1() = testSession("Constraints") {
        loadSysMLv2("assert { true or false }", Runlevel.MODEL)
        assertNoIssues()
        val a = global.getOwnedElementOfType<Feature>()
        assertNotNull(a)
    }

    @Test
    fun assertTestSyntax1b() = testSession("Constraints") {
        loadSysMLv2("assert constraint { true or false }", Runlevel.MODEL)
        assertNoIssues()
        val constraint = global.getOwnedElementOfType<Invariant>()
        assertNotNull(constraint)
    }

    @Test
    fun assertTestSyntax3() = testSession("Constraints") {
        loadSysMLv2("assert not { true and false }", Runlevel.MODEL)
        assertNoIssues()
        val a = global.getOwnedElementOfType<Invariant>()
        assertNotNull(a)
        assertTrue(a.isNegated)
    }

    @Test
    fun testAssertionContradictionPreservesValues() = testSession("ISQ", "Quantities", "Ranges", "Constraints", "ScalarValues") {
        loadSysMLv2("""
            package OpenBoardnet {
                part def ControlUnit {
                    attribute fclk: ISQ::FrequencyValue {:>> range default = 0.1 .. 100000.0 [MHz];}
                    attribute opsPerCyle: Ranges::IntegerInRange {:>> range default = 1 .. 1000;}
                    attribute FLOPS_Hardware: ISQ::FrequencyValue = fclk * ToReal(opsPerCyle) {:>> unit = "GFLOPS";}
                }
                part def Yolov5n {
                    attribute FLOPsTotal: Quantities::ScalarQuantityValue = 4.51913 [GFLOPs];
                    attribute MemoryTotal: ISQ::StorageCapacityValue = 7.54565 [MB];
                }
                part def ADASController :> ControlUnit {
                    part runningModel : Yolov5n;
                    attribute FLOPs_Total: Quantities::ScalarQuantityValue = runningModel::FLOPsTotal {:>> unit = "GFLOPs";}
                    attribute Memory_Total: ISQ::StorageCapacityValue = runningModel::MemoryTotal {:>> unit = "MB";}
                    attribute T : ISQ::DurationValue = FLOPs_Total / FLOPS_Hardware {:>> unit = "ms";}
                    attribute R : ISQ::DurationValue = 33.0[ms] {:>> unit = "ms";} 
                    assert constraint TimeRequirement { R >= T }
                    attribute Memory_Hardware : ISQ::StorageCapacityValue {:>> range default = 1.0 .. 100000.0 [MB];}
                    assert constraint MemoryRequirement { Memory_Hardware >= Memory_Total }
                }
                part def ARMCortex :> ADASController {
                    :>> fclk {:>> range = 240.0 [MHz];}
                    :>> opsPerCyle = 2;
                    :>> Memory_Hardware = 2.0 [MB];
                }
            }
        """, Runlevel.SOLVED)

        val arm = "OpenBoardnet::ARMCortex"
        val tVar = solver.getVariable("$arm::T")
        val rVar = solver.getVariable("$arm::R")
        val timeReq = solver.getVariable("$arm::TimeRequirement")
        val memHw = solver.getVariable("$arm::Memory_Hardware")
        val memTot = solver.getVariable("$arm::Memory_Total")
        val memReq = solver.getVariable("$arm::MemoryRequirement")

        kotlin.test.assertNotNull(tVar)
        kotlin.test.assertNotNull(rVar)
        kotlin.test.assertNotNull(timeReq)
        kotlin.test.assertNotNull(memHw)
        kotlin.test.assertNotNull(memTot)
        kotlin.test.assertNotNull(memReq)

        // R and T should retain their calculated/assigned quantities rather than being empty
        util.assertBounds(33.0, rVar, unit = "ms")
        util.assertBounds(9414.85416666666..9414.854166666673, tVar, unit = "ms")
        kotlin.test.assertEquals("Contradiction", timeReq.valueStr)

        // Memory variables should also retain their quantities
        util.assertBounds(2.0, memHw, unit = "MB")
        util.assertBounds(7.54565, memTot, unit = "MB")
        kotlin.test.assertEquals("Contradiction", memReq.valueStr)
    }

    @Test
    fun testConstraintInheritanceAndCloning() = testSession("Constraints", "ScalarValues", "Parts") {
        loadSysMLv2("""
            part def SuperDef {
                attribute x : ScalarValues::Integer;
                constraint c1 { x > 0 }
                assert constraint c2 { x < 100 }
            }
            part def SubDef :> SuperDef {
                :>> x = 50;
            }
        """, Runlevel.SOLVED)
        assertNoIssues()
        val superDef = global.resolve("SuperDef")?.member<com.github.tukcps.sysmd.model.kerml.Type>()
        val subDef = global.resolve("SubDef")?.member<com.github.tukcps.sysmd.model.kerml.Type>()
        kotlin.test.assertNotNull(superDef)
        kotlin.test.assertNotNull(subDef)
        val subC1 = global.resolve("SubDef::c1")?.member<com.github.tukcps.sysmd.model.kerml.Feature>()
        val subC2 = global.resolve("SubDef::c2")?.member<com.github.tukcps.sysmd.model.kerml.Feature>()
        kotlin.test.assertNotNull(subC1, "SubDef should inherit c1")
        kotlin.test.assertNotNull(subC2, "SubDef should inherit c2")
    }
}
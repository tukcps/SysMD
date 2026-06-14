package solver

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.BDD
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AttributeTests {
    @Test
    fun booleanAttributeTest() = testSession("Attributes") {
        loadSysMLv2(
            """
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean; 
        """, Runlevel.ALL
        )
        assertNoIssues()
        solver.propagate()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        assertTrue((a!!.vectorQuantity.value is BDD.Internal))
        assertTrue((b!!.vectorQuantity.value is BDD.Internal))
        assertTrue((a.vectorQuantity.value as BDD.Internal).index != (b.vectorQuantity.value as BDD.Internal).index)
    }

    @Test
    fun booleanAttributeTest2() = testSession("Attributes", "Ranges") {
        loadSysMLv2(
            """
            attribute a: Ranges::BooleanInSpec{:>> range="true";}
            attribute b: Ranges::BooleanInSpec{:>> range="false";}
        """, Runlevel.ALL
        )
        assertNoIssues()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        assertTrue((a!!.vectorQuantity.value === builder.True))
        assertTrue((b!!.vectorQuantity.value === builder.False))
    }

    @Test
    fun booleanAttributeTest3() = testSession("Attributes") {
        loadSysMLv2(
            """
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean = a;
        """
        )
        solver.propagate()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        assertTrue(a!!.vectorQuantity.value is BDD.Leaf && a.vectorQuantity.value == builder.Bool)
        assertTrue(b!!.vectorQuantity.value is BDD.Leaf && b.vectorQuantity.value == builder.Bool)
        solver.propagate()
        assertTrue(a.vectorQuantity.value is BDD.Leaf && a.vectorQuantity.value == builder.Bool)
        assertTrue(b.vectorQuantity.value is BDD.Leaf && b.vectorQuantity.value == builder.Bool)
    }

    @Test
    fun nestedAttributesTest() = testSession("Attributes") {
        loadSysMLv2(
            """
                attribute def a{
                    attribute b: ScalarValues::Boolean;
                    attribute c: ScalarValues::Boolean;
                }
        """
        )
        solver.propagate()
        assertNoIssues()
    }

    @Test
    fun redefinesTest() = testSession("Parts", "Ranges") {
        loadSysMLv2(
            """
            part def P1 {
                attribute a: Ranges::RealInRange {:>> range="0..20";}
            }
            part def P2 :> P1 {
                :>> a = 3.0;
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(0.0, solver.getVariable("P1::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(20.0, solver.getVariable("P1::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals(3.0, solver.getVariable("P2::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(3.0, solver.getVariable("P2::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals("Real", solver.getVariable("P2::a")!!.baseType.name)
    }

    @Test
    fun redefinesTestOtherSyntax() = testSession("Parts", "Ranges") {
        loadSysMLv2(
            """
            part def P1 {
                attribute a: Ranges::RealInRange {:>> range="0..20";}
            }
            part def P2 :> P1 {
                attribute :>> a = 3.0; 
            }
            part def P3 :> P1; 
        """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(0.0, solver.getVariable("P1::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(20.0, solver.getVariable("P1::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals(3.0, solver.getVariable("P2::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(3.0, solver.getVariable("P2::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals("Real", solver.getVariable("P2::a")!!.baseType.name)
    }


    @Test  // In some executions property of P2: 'a' is written to P1:a, which is not correct.
    // Issue 289
    fun redefinesTestOtherSyntax2() = testSession("Parts", "Ranges") {
        loadSysMLv2(
            """
            part def P1 {
                attribute a: Ranges::RealInRange { :>> range="0..20"; }
            }
            part def P2 :> P1 {
                :>> a: Ranges::RealInRange = 3.0 { :>> range="0..10"; }
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        val p1a = global.resolve("P1::a")
        val p2a = global.resolve("P2::a")
        assertTrue(p1a !== p2a)

        assertEquals(0.0, solver.getVariable("P1::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(20.0, solver.getVariable("P1::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals(3.0, solver.getVariable("P2::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(3.0, solver.getVariable("P2::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertNoIssues()
        assertEquals("Real", solver.getVariable("P2::a")!!.baseType.name)
    }

    @Test
    fun nestedAttributeTest() = testSession("Occurrences", "Ranges") {
        loadSysMLv2(
            """
            attribute def a {
                attribute b: Ranges::RealInRange {:>> range="0..20";}
                attribute c: Ranges::RealInRange {:>> range="0..40";}
            }
            attribute aa : a {
                :>> b = 10.0;
                :>> c = 20.0; 
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        assertNoIssues()
        assertEquals("Real", solver.getVariable("aa::b")!!.baseType.name)
        assertEquals("Real", solver.getVariable("aa::c")!!.baseType.name)
    }

    @Test
    fun nestedAttributeTest2() = testSession("Attributes", "Ranges") {
        loadSysMLv2(
            """
            package test{
                 attribute def QuantityPowerFactor {
                    attribute quantity: Ranges::RealInRange {:>> range="0.0..5.0";}
                }
                attribute def QuantityDimension {
                    attribute quantityPowerFactors: QuantityPowerFactor;
                }
                attribute lengthPF: QuantityPowerFactor { :>> quantity = 3.0; }
                attribute massPF: QuantityPowerFactor { :>> quantity = 1.0; }
                attribute quantityDimension: QuantityDimension { :>> quantityPowerFactors = lengthPF; }
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(
            "Real",
            solver.getVariable("test::quantityDimension::quantityPowerFactors::quantity")!!.baseType.name
        )
        assertEquals(
            3.0,
            solver.getVariable("test::quantityDimension::quantityPowerFactors::quantity")!!.vectorQuantity.aadd()
                .getRange().max,
            0.000001
        )
    }

    /**
     * Note: There is also a redefinition test in KerML tests.
     */
    @Test
    fun nestedAttributeTest2b() = testSession("Attributes", "Ranges") {
        loadSysMLv2("""
            attribute def QuantityPowerFactor {
                attribute exponent: Ranges::IntegerInRange {:>> range="-10..10";}
            }
            attribute def QuantityDimension {
                attribute quantityPowerFactors: QuantityPowerFactor;
            }
            attribute lengthPF: QuantityPowerFactor { 
                :>> exponent = 1;
            }
            attribute quantityDimension: QuantityDimension { 
                :>> quantityPowerFactors = lengthPF; 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(1, solver.getVariable("lengthPF::exponent")!!.vectorQuantity.idd().getRange().min)
        assertEquals(
            1,
            solver.getVariable("quantityDimension::quantityPowerFactors::exponent")!!.vectorQuantity.idd()
                .getRange().max
        )
        assertTrue(status.issues.isEmpty(), "${status.issues}")
    }

    @Test
    fun nestedAttributeTest3() = testSession("Attributes") {
        loadSysMLv2(
            """
            attribute def Old     { attribute a: ScalarValues::String default "old"; }
            attribute def OwnsOld { attribute ownedOld: Old; }
            attribute redefinedOld: Old { :>> a default "new"; }
            attribute ownsOld: OwnsOld  { :>> ownedOld default redefinedOld; }
        """
        )
        assertNoIssues()
        val redefinedOldA = global.resolve("redefinedOld::a")
        assertNotNull(redefinedOldA)
        solver.propagate()
        assertEquals("new", solver.getVariable("ownsOld::ownedOld::a")!!.vectorQuantity.value.asStrDD().toString())
    }

    @Test
    fun nestedAttributeTestWithListOfElements() = testSession("Attributes", "Ranges") {
        loadSysMLv2(
            """
            attribute def QuantityPowerFactor {
                attribute unit: ScalarValues::String;
                attribute exponent: Ranges::IntegerInRange {:>> range="-10..10";}
            }
            attribute def QuantityDimension {
                attribute quantityPowerFactors: QuantityPowerFactor;
            }
            attribute lengthPF: QuantityPowerFactor { :>> unit = "m";  :>> exponent = 1; }
            attribute massPF: QuantityPowerFactor { :>> unit = "kg";  :>> exponent = 1;  }
            attribute timePF: QuantityPowerFactor { :>> unit = "s";  :>> exponent = -2;  }
            attribute quantityDimension: QuantityDimension { :>> quantityPowerFactors = (lengthPF, massPF, timePF);  }
        """, Runlevel.ALL
        )
        assertNoIssues()
        assertEquals(
            "m",
            solver.getVariable("quantityDimension::quantityPowerFactors::unit", 0)!!.vectorQuantity.value.asStrDD()
                .toString()
        )
        assertEquals(
            "kg",
            solver.getVariable("quantityDimension::quantityPowerFactors::unit", 1)!!.vectorQuantity.value.asStrDD()
                .toString()
        )
        assertEquals(
            "s",
            solver.getVariable("quantityDimension::quantityPowerFactors::unit", 2)!!.vectorQuantity.value.asStrDD()
                .toString()
        )
        assertEquals(1, solver.getVariable("quantityDimension::quantityPowerFactors::exponent", 0)!!.idd().max)
        assertEquals(1, solver.getVariable("quantityDimension::quantityPowerFactors::exponent", 1)!!.idd().max)
        assertEquals(-2, solver.getVariable("quantityDimension::quantityPowerFactors::exponent", 2)!!.idd().max)
    }

    // In ISQ::Mass, there is not the right type stored for unit and range (Base::Anything instead of String
    @Test
    fun newRangeSpec() = testSession("Attributes", "Ranges") {
        loadSysMLv2(
            """
            attribute a: ISQ::MassValue {
                :>> range = "1..100";
                :>> unit = "kg";
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        val a = global.resolve("a")
        assertNotNull(a)
        // assertEquals("String",solver.getVariable("a::range")!!.baseType.name)
        // assertEquals("String",solver.getVariable("a::unit")!!.baseType.name)
        // assertEquals("kg",(solver.getVariable("a") as Variable).unitSpec)
        assertEquals(1.0, (solver.getVariable("a") as Variable).aadd().getRange().min, 0.00001)
        assertEquals(100.0, (solver.getVariable("a") as Variable).aadd().getRange().max, 0.00001)
    }

    @Test
    fun assertAttribute1() = testSession("Attributes", "Calculations", "Ranges") {
        loadSysMLv2(
            """
            attribute a: Ranges::RealInRange {:>> range="0..2";}
            attribute b: Ranges::RealInRange {:>> range="1..2";}
            assert constraint Test {isIn(a, b)}
            calc def isIn {
                in attribute a: ScalarValues::Real;
                in attribute b: ScalarValues::Real;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("a")!!.aadd().min, 0.000001)
    }


    @Test
    fun assertAttribute2() = testSession("Calculations", "Ranges") {
        loadSysMLv2(
            """
            attribute a: Ranges::RealInRange {:>> range="0..2";}
            assert constraint Test { isIn(a, 1.0..2.0) }
            calc def isIn {
                in attribute a: ScalarValues::Real;
                in attribute b: ScalarValues::Real;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("a")!!.aadd().min, 0.000001)
    }


    @Test
    fun assertAttribute3() = testSession("ISQ", "Calculations", "Ranges") {
        loadSysMLv2(
            """
            attribute a: ISQ::MassValue { :>> range = "0..2"; :>> unit = "kg";}
            assert constraint Test { isIn(a, 1000.0..2000.0 [g]) }
            calc def isIn {
                in attribute a: ISQ::MassValue;
                in attribute b: ISQ::MassValue;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, solver.getVariable("a")!!.aadd().min, 0.000001)
    }

}
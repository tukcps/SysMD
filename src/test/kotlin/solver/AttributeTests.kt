package solver

import util.variable
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.bounds.*
import util.assertNoIssues
import util.assertBounds
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*
import kotlin.test.assertEquals

class AttributeTests {
    @Test
    fun booleanAttributeTest() = testSession("Attributes") {
        loadSysMLv2("""
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean; 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        solver.propagate()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        assertTrue((a!!.vectorQuantity.value is BDD.Internal))
        assertTrue((b!!.vectorQuantity.value is BDD.Internal))
        assertTrue((a.vectorQuantity.value as BDD.Internal).index != (b.vectorQuantity.value as BDD.Internal).index)
    }

    @Test
    fun booleanAttributeTest2() = testSession("Attributes") {
        loadSysMLv2("""
            attribute a: Ranges::BooleanInSpec{ :>> range=true;}
            attribute b: Ranges::BooleanInSpec{ :>> range=false;}
        """, Runlevel.ALL)
        assertNoIssues()
        solver.propagate()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        assertTrue((a!!.vectorQuantity.value === builder.Bool.True))
        assertTrue((b!!.vectorQuantity.value === builder.Bool.False))
    }

    @Test
    fun booleanAttributeTest3() = testSession("Attributes") {
        loadSysMLv2("""
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean = a;
        """, Runlevel.ALL)
        assertNoIssues()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        solver.propagate()
        assertTrue(a!!.vectorQuantity.value is BDD.Leaf && a.vectorQuantity.value == builder.Bool.All)
        assertTrue(b!!.vectorQuantity.value is BDD.Leaf && b.vectorQuantity.value == builder.Bool.All)
        solver.propagate()
        assertTrue(a.vectorQuantity.value is BDD.Leaf && a.vectorQuantity.value == builder.Bool.All)
        assertTrue(b.vectorQuantity.value is BDD.Leaf && b.vectorQuantity.value == builder.Bool.All)
    }

    @Test
    fun nestedAttributesTest() = testSession("Attributes") {
        loadSysMLv2("""
            attribute def a{
                attribute b: ScalarValues::Boolean;
                attribute c: ScalarValues::Boolean;
            }
        """, Runlevel.MODEL)
        assertNoIssues()
    }

    @Test
    fun redefinesTest() = testSession("Parts", "Ranges") {
        loadSysMLv2("""
            part def P1 {
                attribute a: Ranges::RealInRange {:>> range=0..20;}
            }
            part def P2 :> P1 {
                :>> a = 3.0;
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0 .. 20.0, solver.variable("P1::a"))
        assertBounds(3.0 .. 3.0, solver.variable("P2::a"))
        assertEquals("Real", solver.variable("P2::a").baseType.name)
    }

    @Test
    fun redefinesTestOtherSyntax() = testSession("Parts", "Ranges") {
        loadSysMLv2("""
            part def P1 {
                attribute a: Ranges::RealInRange {:>> range=0..20;}
            }
            part def P2 :> P1 {
                attribute :>> a = 3.0; 
            }
            part def P3 :> P1; 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0 .. 20.0, solver.variable("P1::a"))
        assertBounds(3.0 .. 3.0, solver.variable("P2::a"))
        assertEquals("Real", solver.variable("P2::a").baseType.name)
    }


    @Test  // In some executions property of P2: 'a' is written to P1:a, which is not correct.
    // Issue 289
    fun redefinesTestOtherSyntax2() = testSession("Parts", "Ranges") {
        loadSysMLv2("""
            part def P1 {
                attribute a: Ranges::RealInRange { :>> range=0..20; }
            }
            part def P2 :> P1 {
                :>> a: Ranges::RealInRange = 3.0 { :>> range=0..10; }
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val p1a = global.resolve("P1::a")
        val p2a = global.resolve("P2::a")
        assertTrue(p1a !== p2a)

        assertBounds(0.0 .. 20.0, solver.variable("P1::a"))
        assertBounds(3.0 .. 3.0, solver.variable("P2::a"))
        assertNoIssues()
        assertEquals("Real", solver.variable("P2::a").baseType.name)
    }

    @Test
    fun nestedAttributeTest() = testSession("Attributes") {
        loadSysMLv2("""
            attribute def a {
                attribute b: Ranges::RealInRange {:>> range=0..20;}
                attribute c: Ranges::RealInRange {:>> range=0..40;}
            }
            attribute aa : a {
                :>> b = 10.0;
                :>> c = 20.0; 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals("Real", solver.variable("aa::b").baseType.name)
        assertEquals("Real", solver.variable("aa::c").baseType.name)
    }

    @Test
    fun nestedAttributeTest2() = testSession("Attributes") {
        loadSysMLv2("""
            package test{
                attribute def QuantityPowerFactor {
                    attribute quantity: Ranges::RealInRange {:>> range=0.0..5.0;}
                }
                attribute def QuantityDimension {
                    attribute quantityPowerFactors: QuantityPowerFactor;
                }
                attribute lengthPF: QuantityPowerFactor { :>> quantity = 3.0; }
                attribute massPF: QuantityPowerFactor { :>> quantity = 1.0; }
                attribute quantityDimension: QuantityDimension { :>> quantityPowerFactors = lengthPF; }
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val q = global.resolve("test::quantityDimension::quantityPowerFactors::quantity")
        assertEquals("Real",
            solver.variable("test::quantityDimension::quantityPowerFactors::quantity").baseType.name
        )
        assertBounds(3.0, solver.variable("test::quantityDimension::quantityPowerFactors::quantity"))
    }

    /**
     * Note: There is also a redefinition test in KerML tests.
     */
    @Test
    fun nestedAttributeTest2b() = testSession("Attributes") {
        loadSysMLv2("""
            attribute def QuantityPowerFactor {
                attribute exponent: Ranges::IntegerInRange {:>> range=-10..10;}
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
        assertBounds(1L, solver.variable("lengthPF::exponent"))
        assertBounds(1L, solver.variable("quantityDimension::quantityPowerFactors::exponent"))
        assertTrue(status.issues.isEmpty(), "${status.issues}")
    }

    @Test
    fun nestedAttributeTest3() = testSession("Attributes") {
        loadSysMLv2("""
            attribute def Old     { attribute a: ScalarValues::String default "old"; }
            attribute def OwnsOld { attribute ownedOld: Old; }
            attribute redefinedOld: Old { :>> a default "new"; }
            attribute ownsOld: OwnsOld  { :>> ownedOld default redefinedOld; }
        """, Runlevel.ALL)
        assertNoIssues()
        val redefinedOldA = global.resolve("redefinedOld::a")
        assertNotNull(redefinedOldA)
        assertEquals("new", solver.variable("ownsOld::ownedOld::a").vectorQuantity.value.asStrDD().toString())
    }

    @Test
    fun nestedAttributeTestWithListOfElements() = testSession("Attributes") {
        loadSysMLv2("""
            attribute def QuantityPowerFactor {
                attribute unit: ScalarValues::String;
                attribute exponent: Ranges::IntegerInRange {:>> range=-10..10;}
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
            solver.variable("quantityDimension::quantityPowerFactors::unit", 0).vectorQuantity.value.asStrDD()
                .toString()
        )
        assertEquals("kg",
            solver.variable("quantityDimension::quantityPowerFactors::unit", 1).vectorQuantity.value.asStrDD()
                .toString())
        assertEquals("s",
            solver.variable("quantityDimension::quantityPowerFactors::unit", 2).vectorQuantity.value.asStrDD()
                .toString())
        assertBounds(1L, solver.variable("quantityDimension::quantityPowerFactors::exponent", 0))
        assertBounds(1L, solver.variable("quantityDimension::quantityPowerFactors::exponent", 1))
        assertBounds(-2L, solver.variable("quantityDimension::quantityPowerFactors::exponent", 2))
    }

    // In ISQ::Mass, there is not the right type stored for unit and range (Base::Anything instead of String
    @Test
    fun newRangeSpec() = testSession("Attributes", "ISQ") {
        loadSysMLv2("""
            attribute a: ISQ::MassValue {
                :>> range = 1..100 [kg];
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        val a = global.resolve("a")
        assertNotNull(a)
        assertBounds(1.0 .. 100.0, (solver.getVariable("a") as Variable))
    }

    @Test
    fun assertAttribute1() = testSession("Calculations") {
        loadSysMLv2("""
            attribute a: Ranges::RealInRange {:>> range=0..2;}
            attribute b: Ranges::RealInRange {:>> range=1..2;}
            assert constraint Test {isIn(a, b)}
            calc def isIn {
                in attribute a: ScalarValues::Real;
                in attribute b: ScalarValues::Real;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0..2.0, solver.variable("a"))
    }


    @Test
    fun assertAttribute2() = testSession("Calculations") {
        loadSysMLv2(
            """
            attribute a: Ranges::RealInRange {:>> range=0..2;}
            assert constraint Test { isIn(a, 1.0..2.0) }
            calc def isIn {
                in attribute a: ScalarValues::Real;
                in attribute b: ScalarValues::Real;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(1.0..2.0, solver.variable("a"))
    }

    @Test
    fun assertAttribute3() = testSession("Calculations", "ISQ") {
        loadSysMLv2("""
            attribute a: ISQ::MassValue { :>> range = 0..2 [kg];}
            assert constraint Test { isIn(a, 1000.0..2000.0 [g]) }
            calc def isIn {
                in attribute a: ISQ::MassValue;
                in attribute b: ISQ::MassValue;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """, Runlevel.ALL)
        assertNoIssues()
        assertBounds(1.0..2.0, solver.variable("a"))
    }

}
package sysmdtests

import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.sysml.AttributeUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolveVars
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
        """
        )
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        assertTrue((a!!.vectorQuantity.value is BDD.Internal))
        assertTrue((b!!.vectorQuantity.value is BDD.Internal))
        assertTrue((a.vectorQuantity.value as BDD.Internal).index != (b.vectorQuantity.value as BDD.Internal).index)
    }

    @Test
    fun booleanAttributeTest2() = testSession("Attributes", "Ranges") {
        loadSysMLv2(
            """
            attribute a: Ranges::BooleanInSpec{:>> spec="true";}
            attribute b: Ranges::BooleanInSpec{:>> spec="false";}
        """
        )
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
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
        propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        assertTrue(a!!.vectorQuantity.value is BDD.Leaf && a.vectorQuantity.value == builder.Bool)
        assertTrue(b!!.vectorQuantity.value is BDD.Leaf && b.vectorQuantity.value == builder.Bool)
        propagate()
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
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
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
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(0.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(20.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals("Real", global.resolveVar("P2::a")!!.baseType.name)
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
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(0.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(20.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals("Real", global.resolveVar("P2::a")!!.baseType.name)
    }


    @Test  // In some executions property of P2:a is written to P1:a, which is not correct.
    // Issue 289
    fun redefinesTestOtherSyntax2() = testSession("Parts", "Ranges") {
        loadSysMLv2("""
            part def P1 {
                attribute a: Ranges::RealInRange {:>> range="0..20";}
            }
            part def P2 :> P1 {
                :>> a: Ranges::RealInRange = 3.0 {:>> range="0..10";}
            }
        """)
        propagate()
        assertNoIssues()
        val p1a = global.resolve<AttributeUsage>("P1::a")
        val p2a = global.resolve<Feature>("P2::a")
        assertTrue(p1a !== p2a)

        assertEquals(0.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(20.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().min, 0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().max, 0.000001)
        assertNoIssues()
        assertEquals("Real", global.resolveVar("P2::a")!!.baseType.name)
    }

    @Test
    fun nestedAttributeTest() = testSession("Occurrences", "Ranges") {
        loadSysMLv2("""
            attribute def a {
                attribute b: Ranges::RealInRange {:>> range="0..20";}
                attribute c: Ranges::RealInRange {:>> range="0..40";}
            }
            attribute aa : a {
                :>> b = 10.0;
                :>> c = 20.0; 
            }
        """)
        propagate()
        assertNoIssues()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals("Real", global.resolveVar("aa::b")!!.baseType.name)
        assertEquals("Real", global.resolveVar("aa::c")!!.baseType.name)
    }

    @Test
    fun nestedAttributeTest2() = testSession("Attributes", "Ranges") {
        loadSysMLv2("""
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
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(
            "Real",
            global.resolveVar("test::quantityDimension::quantityPowerFactors::quantity")!!.baseType.name
        )
        assertEquals(
            3.0,
            global.resolveVar("test::quantityDimension::quantityPowerFactors::quantity")!!.vectorQuantity.aadd()
                .getRange().max,
            0.000001
        )
    }

    /**
     * Note: There is also a redefinition test in KerML tests.
     */
    @Test
    fun nestedAttributeTest2b() = testSession("Attributes", "Ranges") {
        loadSysMLv2(
            """
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
        """
        )
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(1, global.resolveVar("lengthPF::exponent")!!.vectorQuantity.idd().getRange().min)
        assertEquals(
            1,
            global.resolveVar("quantityDimension::quantityPowerFactors::exponent")!!.vectorQuantity.idd().getRange().max
        )
        assertTrue(status.issues.isEmpty(), "${status.issues}")
    }

    @Test
    fun nestedAttributeTest3() = testSession("Attributes") {
        loadSysMLv2(
            """
            attribute def Old {
                attribute a: ScalarValues::String = "old";
            }
            attribute def OwnsOld {
                attribute ownedOld: Old;
            }
            attribute redefinedOld: Old { :>> a = "new"; }
            attribute ownsOld: OwnsOld { :>> ownedOld = redefinedOld; }
        """
        )
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        val redefinedOldA = global.resolve<Element>("redefinedOld::a")
        assertNotNull(redefinedOldA)
        propagate()
        assertEquals("new", global.resolveVars("ownsOld::ownedOld::a")[0]!!.vectorQuantity.value.asStrDD().toString())
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
        """.trimIndent()
        )
        propagate()
        assertEquals(
            "m",
            global.resolveVars("quantityDimension::quantityPowerFactors::unit")[0]!!.vectorQuantity.value.asStrDD()
                .toString()
        )
        assertEquals(
            "kg",
            global.resolveVars("quantityDimension::quantityPowerFactors::unit")[1]!!.vectorQuantity.value.asStrDD()
                .toString()
        )
        assertEquals(
            "s",
            global.resolveVars("quantityDimension::quantityPowerFactors::unit")[2]!!.vectorQuantity.value.asStrDD()
                .toString()
        )
        assertEquals(1, global.resolveVars("quantityDimension::quantityPowerFactors::exponent")[0]!!.idd().max)
        assertEquals(1, global.resolveVars("quantityDimension::quantityPowerFactors::exponent")[1]!!.idd().max)
        assertEquals(-2, global.resolveVars("quantityDimension::quantityPowerFactors::exponent")[2]!!.idd().max)
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
    }

    // In SI::Mass, there is not the right type stored for unit and range (Base::Anything instead of String
    @Test
    fun newRangeSpec() = testSession("SI", "Attributes", "Ranges") {
        loadSysMLv2(
            """
            attribute a: SI::Mass {
                :>> range = "1..100";
                :>> unit = "kg";
            }
        """
        )
        propagate()
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        val a = global.resolve<Feature>("a")
        assertNotNull(a)
        // assertEquals("String",global.resolveVar("a::range")!!.baseType.name)
        // assertEquals("String",global.resolveVar("a::unit")!!.baseType.name)
        // assertEquals("kg",(global.resolveVar("a") as Variable).unitSpec)
        assertEquals(1.0, (global.resolveVar("a") as Variable).aadd().getRange().min, 0.00001)
        assertEquals(100.0, (global.resolveVar("a") as Variable).aadd().getRange().max, 0.00001)
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
        propagate()
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertEquals(1.0, global.resolveVar("a")!!.aadd().min, 0.000001)
    }


    @Test
    fun assertAttribute2() = testSession("Calculations", "Ranges") {
        loadSysMLv2("""
            attribute a: Ranges::RealInRange {:>> range="0..2";}
            assert constraint Test { isIn(a, 1.0..2.0) }
            calc def isIn {
                in attribute a: ScalarValues::Real;
                in attribute b: ScalarValues::Real;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """)
        propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("a")!!.aadd().min, 0.000001)
    }


    @Test
    fun assertAttribute3() = testSession("SI", "Calculations", "Ranges") {
        loadSysMLv2(
            """
            attribute a: SI::Mass { :>> range = "0..2"; :>> unit = "kg";}
            assert constraint Test {isIn(a, 1000.0..2000.0 [g])}
            calc def isIn {
                in attribute a: SI::Mass;
                in attribute b: SI::Mass;
                return result: ScalarValues::Boolean = (a <= max(b)) and (a >= min(b));
            }
        """
        )
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(1.0, global.resolveVar("a")!!.aadd().min, 0.000001)
    }

}

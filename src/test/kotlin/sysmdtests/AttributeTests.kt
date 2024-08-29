package sysmdtests

import com.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolveVars
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertTrue


class AttributeTests {
    @Test
    fun booleanPropertyTest()  = testSession  {
        loadSysMD("""
                attribute a: ScalarValues::Boolean;
                attribute b: ScalarValues::Boolean; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        assertTrue((a?.vectorQuantity?.value is BDD.Internal))
        assertTrue((b?.vectorQuantity?.value is BDD.Internal))
        assertTrue((a?.vectorQuantity?.value as BDD.Internal).index != (b?.vectorQuantity?.value as BDD.Internal).index)
    }

    @Test
    fun booleanPropertyTest2()  = testSession {
        loadSysMD("""
                attribute a: ScalarValues::Boolean(true);
                attribute b: ScalarValues::Boolean(false);
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        assertTrue((a?.vectorQuantity?.value is BDD.Leaf))
        assertTrue((b?.vectorQuantity?.value is BDD.Leaf))
        assertTrue((a?.vectorQuantity?.value === builder.True))
        assertTrue((b?.vectorQuantity?.value === builder.False))
    }

    @Test
    fun booleanPropertyTest3()  = testSession {
        loadSysMD("""
                attribute a: ScalarValues::Boolean;
                attribute b: ScalarValues::Boolean = a;
        """.trimIndent())
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        assertTrue((a?.vectorQuantity?.value is BDD.Internal))
        assertTrue((b?.vectorQuantity?.value is BDD.Internal))
        propagate()
        assertTrue((a?.vectorQuantity?.value is BDD.Internal))
        assertTrue((b?.vectorQuantity?.value is BDD.Internal))
    }

    @Test
    fun nestedAttributesTest()  = testSession {
        loadSysMD("""
                attribute def a{
                    attribute b: ScalarValues::Boolean;
                    attribute c: ScalarValues::Boolean;
                }
        """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun redefinesTest()  = testSession("Parts") {
        loadSysMD("""
                part def P1 :> Base::Anything{
                    attribute a: ScalarValues::Real(0..20).
                }
                part def P2 :> P1{
                    :>> a = 3.0.
                }
                part def P3 :> P1.
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(0.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(20.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")

    }

    @Test
    fun redefinesTestOtherSyntax()  = testSession {
        loadSysMD("""
                part def P1 :> Base::Anything{
                    attribute a: ScalarValues::Real(0..20).
                }
                part def P2 :> P1{
                    attribute :>> a = 3.0.
                }
                part def P3 :> P1.
        """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(20.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().max,0.000001)
    }

    @Test
    fun redefinesTestOtherSyntax2()  = testSession {
        loadSysMD("""
                part def P1 :> Base::Anything{
                    attribute a: ScalarValues::Real(0..20).
                }
                part def P2 :> P1{
                    :>> a: ScalarValues::Real(0..10) = 3.0.
                }
                part def P3 :> P1.
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(0.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(20.0, global.resolveVar("P1::a")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().min,0.000001)
        assertEquals(3.0, global.resolveVar("P2::a")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }


    @Test
    fun nestedAttributeTest()  = testSession {
        loadSysMD("""
                 attribute def a{
                    attribute b: ScalarValues::Real(0..20);
                    attribute c: ScalarValues::Real(0..40).
                 }
                 attribute aa : a{
                    :>> b = 10.0;
                    :>> c = 20.0.
                 }
        """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun nestedAttributeTest2()  = testSession {
        loadSysMD("""
            package test{
                 attribute def QuantityPowerFactor {
                    attribute quantity: ScalarValues::Real(0.0..5.0);
                }
                attribute def QuantityDimension {
                    attribute quantityPowerFactors: QuantityPowerFactor;
                }
                attribute lengthPF: QuantityPowerFactor { :>> quantity = 3.0; }
                attribute massPF: QuantityPowerFactor { :>> quantity = 1.0; }
                attribute quantityDimension: QuantityDimension { :>> quantityPowerFactors = lengthPF; }
            }
        """.trimIndent())
        propagate()
        assertEquals(3.0, global.resolveVar("test::quantityDimension::quantityPowerFactors::quantity")!!.vectorQuantity.aadd().getRange().max,0.000001)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun nestedAttributeTest2b()  = testSession {
        loadSysMD("""
            package test{
                 attribute def QuantityPowerFactor {
                    attribute unit: ScalarValues::String;
                    attribute exponent: ScalarValues::Integer(-10..10);
                }
                attribute def QuantityDimension {
                    attribute quantityPowerFactors: QuantityPowerFactor;
                }
                attribute lengthPF: QuantityPowerFactor { :>> unit = "m";  :>> exponent = 1; }
                attribute massPF: QuantityPowerFactor { :>> unit = "kg";  :>> exponent = 1;  }
                attribute quantityDimension: QuantityDimension { :>> quantityPowerFactors = lengthPF; }
            }
        """.trimIndent())
        propagate()
        assertEquals(1, global.resolveVar("test::quantityDimension::quantityPowerFactors::exponent")!!.vectorQuantity.idd().getRange().max)
        assertEquals("m", global.resolveVar("test::quantityDimension::quantityPowerFactors::unit")!!.vectorQuantity.value.asStrDD().toString())
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }

    @Test
    fun nestedAttributeTest3()  = testSession {
        loadSysMD("""
            package test{
                 attribute def QuantityPowerFactor {
                    attribute unit: ScalarValues::String;
                    attribute exponent: ScalarValues::Integer(-10..10);
                }
                attribute def QuantityDimension {
                    attribute quantityPowerFactors: QuantityPowerFactor;
                }
                attribute lengthPF: QuantityPowerFactor { :>> unit = "m";  :>> exponent = 1; }
                attribute massPF: QuantityPowerFactor { :>> unit = "kg";  :>> exponent = 1;  }
                attribute timePF: QuantityPowerFactor { :>> unit = "s";  :>> exponent = -2;  }
                attribute quantityDimension: QuantityDimension { :>> quantityPowerFactors = (lengthPF, massPF, timePF);  }
            }
        """.trimIndent())
        propagate()
        assertEquals("m", global.resolveVars("test::quantityDimension::quantityPowerFactors::unit")[0]!!.vectorQuantity.value.asStrDD().toString())
        assertEquals("kg", global.resolveVars("test::quantityDimension::quantityPowerFactors::unit")[1]!!.vectorQuantity.value.asStrDD().toString())
        assertEquals("s", global.resolveVars("test::quantityDimension::quantityPowerFactors::unit")[2]!!.vectorQuantity.value.asStrDD().toString())
        assertEquals(1, global.resolveVars("test::quantityDimension::quantityPowerFactors::exponent")[0]!!.idd().max)
        assertEquals(1, global.resolveVars("test::quantityDimension::quantityPowerFactors::exponent")[1]!!.idd().max)
        assertEquals(-2, global.resolveVars("test::quantityDimension::quantityPowerFactors::exponent")[2]!!.idd().max)
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
    }
}
@file:Suppress("PrivatePropertyName")

package quantitytests

import util.assertBounds
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.UnknownUnitError
import com.github.tukcps.sysmd.quantities.VectorDimensionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import io.github.tukcps.aadd.DDBuilder
import com.github.tukcps.sysmd.quantities.DDError
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.dd.BDD
import io.github.tukcps.aadd.dd.IDD
import io.github.tukcps.aadd.util.Assertions.assertEquals
import io.github.tukcps.aadd.values.bounds.LongBound
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class VectorQuantityTests {
    private lateinit var DDdummy0: AADD
    private lateinit var DDdummy0_5: AADD
    private lateinit var DDdummy1: AADD
    private lateinit var DDdummyNeg1: AADD
    private lateinit var DDdummy1_5: AADD
    private lateinit var DDdummy2: AADD
    private lateinit var DDdummy2_5: AADD
    private lateinit var DDdummy3: AADD
    private lateinit var DDdummy4: AADD
    private lateinit var DDdummy5: AADD
    private lateinit var DDdummy8: AADD
    private lateinit var DDdummy10: AADD
    private lateinit var DDdummy20: AADD
    private lateinit var DDdummy30: AADD
    private lateinit var DDdummy36: AADD
    private lateinit var DDdummy50: AADD
    private lateinit var DDdummy99: AADD
    private lateinit var DDdummy100: AADD
    private lateinit var DDdummy200: AADD
    private lateinit var DDdummy1000: AADD
    private lateinit var DDdummy1024: AADD
    private lateinit var DDdummyminus50: AADD
    private lateinit var AADDdummy100: AADD
    private lateinit var AADDdummy1: AADD
    private lateinit var AADDdummy5: AADD
    private lateinit var IDDdummy0: IDD
    private lateinit var IDDdummy1: IDD
    private lateinit var IDDdummy2: IDD
    private lateinit var IDDdummy3: IDD
    private lateinit var IDDdummy4: IDD
    private lateinit var IDDdummy5: IDD
    private lateinit var IDDdummy10: IDD
    private lateinit var IDDdummy100: IDD
    private lateinit var BDDdummyT: BDD
    private lateinit var BDDdummyF: BDD

    @BeforeTest
    fun setUp() {
        DDBuilder {
            DDdummy0 = real(0.0)
            DDdummy0_5 = real(0.5)
            DDdummy1 = real(1.0)
            DDdummyNeg1 = real(-1.0)
            DDdummy1_5 = real(1.5)
            DDdummy2 = real(2.0)
            DDdummy2_5 = real(2.5)
            DDdummy3 = real(3.0)
            DDdummy4 = real(4.0)
            DDdummy5 = real(5.0)
            DDdummy8 = real(8.0)
            DDdummy10 = real(10.0)
            DDdummy20 = real(20.0)
            DDdummy30 = real(30.0)
            DDdummy36 = real(36.0)
            DDdummy50 = real(50.0)
            DDdummy99 = real(99.0)
            DDdummy100 = real(100.0)
            DDdummy200 = real(200.0)
            DDdummy1000 = real(1000.0)
            DDdummy1024 = real(1024.0)
            DDdummyminus50 = real(-50.0)
            AADDdummy100 = real(-100.0..100.0)
            AADDdummy1 = real(-1.0..1.0)
            AADDdummy5 = real(-5.0..5.0)
            IDDdummy0 = integer(0)
            IDDdummy1 = integer(1)
            IDDdummy2 = integer(2)
            IDDdummy3 = integer(3)
            IDDdummy4 = integer(4)
            IDDdummy5 = integer(5)
            IDDdummy10 = integer(10)
            IDDdummy100 = integer(100)
            BDDdummyF = Bool.False
            BDDdummyT = Bool.True
        }
    }

    @Test
    fun vectorPlus() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1,DDdummy5,DDdummy0,DDdummy10)
        val vec2 = listOf(DDdummy5,DDdummy1,DDdummy10,DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.plus(quantity2)
        assertBounds(6.0, result.values[0].asAadd())
        assertBounds(6.0, result.values[1].asAadd())
        assertBounds(10.0, result.values[2].asAadd())
        assertBounds(10.0, result.values[3].asAadd())

        val quantity3 = VectorQuantity(DDdummy0, u1)
        val result2 = quantity1.plus(quantity3)
        assertBounds(1.0, result2.values[0].asAadd())
        assertBounds(5.0, result2.values[1].asAadd())
        assertBounds(0.0, result2.values[2].asAadd())
        assertBounds(10.0, result2.values[3].asAadd())
    }

    @Test
    fun vectorPlusIntZero() {
        val vec1 = listOf(IDDdummy1,IDDdummy5,IDDdummy100)
        val quantity1 = VectorQuantity(vec1)
        val quantity2 = VectorQuantity(IDDdummy0)
        val result = quantity1.plus(quantity2)
        assertBounds(1, result.values[0].asIdd())
        assertBounds(5, result.values[1].asIdd())
        assertBounds(100, result.values[2].asIdd())

        val result2 = quantity1.plus(quantity2)
        assertBounds(1, result2.values[0].asIdd())
        assertBounds(5, result2.values[1].asIdd())
        assertBounds(100, result2.values[2].asIdd())
    }

    @Test
    fun vectorMinus() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1,DDdummy5,DDdummy0,DDdummy10)
        val vec2 = listOf(DDdummy5,DDdummy1,DDdummy10,DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.minus(quantity2)
        assertBounds(-4.0, result.values[0].asAadd())
        assertBounds(4.0, result.values[1].asAadd())
        assertBounds(-10.0, result.values[2].asAadd())
        assertBounds(10.0, result.values[3].asAadd())
    }


    @Test
    fun vectorTimesScalar() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1,DDdummy5,DDdummy0,DDdummy10)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(DDdummy10, u1)
        val result = quantity1.times(quantity2)
        assertBounds(10.0, result.values[0].asAadd())
        assertBounds(50.0, result.values[1].asAadd())
        assertBounds(0.0, result.values[2].asAadd())
        assertBounds(100.0, result.values[3].asAadd())

        //other order
        val result1 = quantity2.times(quantity1)
        assertBounds(10.0, result1.values[0].asAadd())
        assertBounds(50.0, result1.values[1].asAadd())
        assertBounds(0.0, result1.values[2].asAadd())
        assertBounds(100.0, result1.values[3].asAadd())
    }

    @Test
    fun vectorScalarDiv() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy10,DDdummy100,DDdummy1,DDdummy200)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(DDdummy2, u1)
        val result = quantity1.div(quantity2)
        assertBounds(5.0, result.values[0].asAadd())
        assertBounds(50.0, result.values[1].asAadd())
        assertBounds(0.5, result.values[2].asAadd())
        assertBounds(100.0, result.values[3].asAadd())
    }

    @Test
    fun vectorNegate() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1,DDdummy5,DDdummy0,DDdummy10)
        val quantity = VectorQuantity(vec1, u1)
        val result = quantity.negate()
        assertBounds(-1.0, result.values[0].asAadd())
        assertBounds(-5.0, result.values[1].asAadd())
        assertBounds(0.0, result.values[2].asAadd())
        assertBounds(-10.0, result.values[3].asAadd())
    }

    @Test
    fun vectorNegateInt() {
        val vec1 = listOf(IDDdummy1,IDDdummy0,IDDdummy100,IDDdummy5)
        val quantity = VectorQuantity(vec1)
        val result = quantity.negate()
        assertBounds(-1, result.values[0].asIdd())
        assertBounds(0, result.values[1].asIdd())
        assertBounds(-100, result.values[2].asIdd())
        assertBounds(-5, result.values[3].asIdd())
    }

    @Test
    fun vectorAbsTest() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy3,DDdummy0,DDdummy4)
        val quantity = VectorQuantity(vec1, u1)
        val result = quantity.abs()
        assertBounds(5.0, result)
    }

    @Test
    fun vectorAbsTestInt() {
        val vec1 = listOf(IDDdummy3,IDDdummy0,IDDdummy4)
        val quantity = VectorQuantity(vec1)
        val result = quantity.abs()
        assertBounds(5, result.value.asIdd())
    }

    @Test
    fun vectorCeilTest() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1_5,DDdummy0,DDdummy2_5,DDdummy0_5)
        val quantity = VectorQuantity(vec1, u1)
        val result = quantity.ceil()
        assertBounds(2.0, result.values[0].asAadd())
        assertBounds(0.0, result.values[1].asAadd())
        assertBounds(3.0, result.values[2].asAadd())
        assertBounds(1.0, result.values[3].asAadd())
    }

    @Test
    fun vectorFloorTest() {
        val vec1 = listOf(DDdummy1_5,DDdummy2_5,DDdummy0_5)
        val u1 = Unit("m")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.floor()
        assertBounds(1.0, result.values[0].asAadd())
        assertBounds(2.0, result.values[1].asAadd())
        assertBounds(0.0, result.values[2].asAadd())
    }

    @Test
    fun vectorSqrtTest() {
        val vec1 = listOf(DDdummy100,DDdummy1,DDdummy4,DDdummy36)
        val u1 = Unit("m^2")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.sqrt()
        assertBounds(10.0, result.values[0].asAadd())
        assertBounds(1.0, result.values[1].asAadd())
        assertBounds(2.0, result.values[2].asAadd())
        assertBounds(6.0, result.values[3].asAadd())
        assertEquals("m",result.unit.toString())
    }

    @Test
    fun vectorSqrtIntTest() {
        val vec1 = listOf(IDDdummy4,IDDdummy1,IDDdummy0)
        val quantity = VectorQuantity(vec1)
        val result = quantity.sqrt()
        assertBounds(listOf(2L, 1L, 0L), result)
    }

    @Test
    fun vectorInverseSqrTest() {
        val vec1 = listOf(DDdummy100, DDdummy1, DDdummy4, DDdummy36)
        val u1 = Unit("m^2")
        val quantity = VectorQuantity(vec1, u1)
        val result = quantity.inverseSqr()
        assertBounds(-10.0..10.0, result.values[0].asAadd())
        assertEquals("m", result.unit.toString())
    }

    @Test
    fun vectorInverseSqrIntTest() {
        val vec1 = listOf(IDDdummy4, IDDdummy1, IDDdummy0)
        val quantity = VectorQuantity(vec1)
        val result = quantity.inverseSqr()
        assertBounds(listOf(-2L..2L, -1L..1L, 0L..0L), result)
    }

    @Test
    fun vectorSqrTest() {
        val vec1 = listOf(DDdummy10,DDdummy1,DDdummy4,DDdummy2_5)
        val u1 = Unit("m")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.sqr()
        assertBounds(100.0, result.values[0].asAadd())
        assertBounds(1.0, result.values[1].asAadd())
        assertBounds(16.0, result.values[2].asAadd())
        assertBounds(6.25, result.values[3].asAadd())
    }

    @Test
    fun vectorSqrIntTest() {
        val vec1 = listOf(IDDdummy4,IDDdummy100,IDDdummy0,IDDdummy5)
        val quantity = VectorQuantity(vec1)
        val result = quantity.sqr()
        assertBounds(16, result.values[0].asIdd())
        assertBounds(10000, result.values[1].asIdd())
        assertBounds(0, result.values[2].asIdd())
        assertBounds(25, result.values[3].asIdd())
    }

    @Test
    fun vectorLogTest() {
        val vec1 = listOf(DDdummy10,DDdummy1,DDdummy4,DDdummy2_5)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.ln()
        assertBounds(2.3025850929940455, result.values[0].asAadd())
        assertBounds(0.0, result.values[1].asAadd())
        assertBounds(1.3862943611198904, result.values[2].asAadd())
        assertBounds(0.916290731874155, result.values[3].asAadd())
    }

    @Test
    fun vectorLogBaseTest() {
        val vec1 = listOf(DDdummy10,DDdummy1,DDdummy1000,DDdummy100)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.log(DDdummy10)
        assertBounds(1.0, result.values[0].asAadd())
        assertBounds(0.0, result.values[1].asAadd())
        assertBounds(3.0, result.values[2].asAadd())
        assertBounds(2.0, result.values[3].asAadd())
    }

    @Test
    fun vectorLogBaseIDDTest() {
        val vec1 = listOf(IDDdummy100,IDDdummy1,IDDdummy10)
        val quantity = VectorQuantity(vec1)
        val result = quantity.log(IDDdummy10)
        assertBounds(2, result.values[0].asIdd())
        assertBounds(0, result.values[1].asIdd())
        assertBounds(1, result.values[2].asIdd())
    }

    @Test
    fun vectorExpTest() {
        val vec1 = listOf(DDdummy1,DDdummy0,DDdummy2,DDdummy3)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.exp()
        assertBounds(2.7182818284590446, result.values[0].asAadd())
        assertBounds(1.0, result.values[1].asAadd())
        assertBounds(7.38905609893065, result.values[2].asAadd())
        assertBounds(20.085536923187664, result.values[3].asAadd())
    }

    @Test
    fun vectorPow2Test() {
        val vec1 = listOf(DDdummy1,DDdummy0,DDdummy2,DDdummy3)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.pow2()
        assertBounds(2.0, result.values[0].asAadd())
        assertBounds(1.0, result.values[1].asAadd())
        assertBounds(4.0, result.values[2].asAadd())
        assertBounds(8.0, result.values[3].asAadd())
    }

    @Test
    fun vectorPow2IntTest() {
        val vec1 = listOf(IDDdummy1,IDDdummy3,IDDdummy4,IDDdummy0)
        val quantity = VectorQuantity(vec1)
        val result = quantity.pow2()
        assertBounds(2, result.values[0].asIdd())
        assertBounds(8, result.values[1].asIdd())
        assertBounds(16, result.values[2].asIdd())
        assertBounds(1, result.values[3].asIdd())
    }
    @Test
    fun vectorPowTest() {
        val vec1 = listOf(DDdummy1,DDdummy0,DDdummy2,DDdummy3)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.pow(DDdummy2)
        assertBounds(1.0, result.values[0].asAadd())
        assertBounds(0.0, result.values[1].asAadd())
        assertBounds(4.0, result.values[2].asAadd())
        assertBounds(9.0, result.values[3].asAadd())
    }

    @Test
    fun vectorPowIntTest() {
        val vec1 = listOf(IDDdummy1,IDDdummy3,IDDdummy4,IDDdummy0)
        val quantity = VectorQuantity(vec1)
        val result = quantity.pow(IDDdummy2)
        assertBounds(1, result.values[0].asIdd())
        assertBounds(9, result.values[1].asIdd())
        assertBounds(16, result.values[2].asIdd())
        assertBounds(0, result.values[3].asIdd())
    }

    @Test
    fun vectorToStringTest() {
        val vec1 = listOf(DDdummy1,DDdummy3,DDdummy4,DDdummy0)
        val quantity = VectorQuantity(vec1, Unit("m"))
        assertEquals("(1, 3, 4, 0) m", quantity.toString())

        val vec2 = listOf(IDDdummy1,IDDdummy3,IDDdummy4,IDDdummy0)
        val quantity2 = VectorQuantity(vec2)
        assertEquals("(1, 3, 4, 0)", quantity2.toString())

        val vec3 = listOf(BDDdummyF,BDDdummyT,BDDdummyT)
        val quantity3 = VectorQuantity(vec3)
        assertEquals("(False, True, True)", quantity3.toString())
    }

    @Test
    fun vectorToStringWithUnitsTest() {
        val vec1 = listOf(DDdummy1,DDdummy3,DDdummy4,DDdummy0)

        val quantity3 = VectorQuantity(vec1, Unit("kg m / s"))
        assertEquals("(1, 3, 4, 0) kg m/s", quantity3.toString())
        val quantity = VectorQuantity(vec1, Unit("N"))
        assertEquals("(1, 3, 4, 0) N", quantity.toString())

        val quantity2 = VectorQuantity(vec1, Unit("km/h"))
        assertEquals("(0.27778, 0.83333, 1.1111, 0) m/s", quantity2.toString())
    }


    @Test
    fun vectorToStringTestWithUnits() {
        val vec1 = listOf(DDdummy1,DDdummy3,DDdummy4,DDdummy0)
        val quantity = VectorQuantity(vec1, Unit("m"))
        assertEquals("(1, 3, 4, 0) m", quantity.toString())

        val vec2 = listOf(IDDdummy1,IDDdummy3,IDDdummy4,IDDdummy0)
        val quantity2 = VectorQuantity(vec2)
        assertEquals("(1, 3, 4, 0)", quantity2.toString())
    }

    @Test
    fun vectorDotProduct() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy5, DDdummy0, DDdummy10)
        val vec2 = listOf(DDdummy5, DDdummy1, DDdummy10, DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.dot(quantity2)
        assertBounds(10.0, result.value.asAadd())

    }

    @Test
    fun vectorCrossProduct() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy5, DDdummy10)
        val vec2 = listOf(DDdummy5, DDdummy1, DDdummy10)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.cross(quantity2)
        assertBounds(40.0, result.values[0].asAadd())
        assertBounds(40.0, result.values[1].asAadd())
        assertBounds(-24.0, result.values[2].asAadd())
    }

    @Test
    fun vectorNormalize() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy3, DDdummy4,DDdummy0)
        val vec2 = listOf(DDdummy10, DDdummy3, DDdummy2)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result1 = quantity1.norm()
        val result2 = quantity2.norm()
        assertBounds(0.6, result1.values[0].asAadd())
        assertBounds(0.8, result1.values[1].asAadd())
        assertBounds(0.0, result1.values[2].asAadd())
        assertBounds(0.9407208683835953, result2.values[0].asAadd())
        assertBounds(0.28221626051507853, result2.values[1].asAadd())
        assertBounds(0.18814417367671904, result2.values[2].asAadd())
    }

    @Test
    fun vectorAngle() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val vec2 = listOf(DDdummy1, DDdummy0, DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertBounds(45.0, result.valueIn("°").asAadd())
    }

    @Test
    fun vectorAngle2() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val vec2 = listOf(DDdummyNeg1, DDdummyNeg1, DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertBounds(180.0, result.valueIn("°").asAadd())
    }
    @Test
    fun vectorAngle3() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val vec2 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertBounds(0.0, result.valueIn("°").asAadd())
    }

    @Test
    fun vectorAngle4() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy5, DDdummy10)
        val vec2 = listOf(DDdummy5, DDdummy2, DDdummyNeg1)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertBounds(85.33526881505367..85.33526881505367, result.valueIn("°").asAadd())
    }

    @Test
    fun scalarGettersErrorOnVector() {
        val multiVector = VectorQuantity(listOf(DDdummy1, DDdummy5))
        assertFailsWith<VectorDimensionError> { multiVector.value }
        assertFailsWith<VectorDimensionError> { multiVector.aadd() }
        assertFailsWith<VectorDimensionError> { multiVector.idd() }
        assertFailsWith<VectorDimensionError> { multiVector.bdd() }
    }

    @Test
    fun undefinedUnitThrowsInVectorQuantity() {
        assertFailsWith<UnknownUnitError> {
            VectorQuantity(DDdummy1, "notDefinedUnit")
        }
        assertFailsWith<UnknownUnitError> {
            VectorQuantity(listOf(DDdummy1, DDdummy2), "notDefinedUnit")
        }
        assertFailsWith<UnknownUnitError> {
            VectorQuantity(DDdummy1, Unit("m"), unitSpec = "notDefinedUnit")
        }
        assertFailsWith<UnknownUnitError> {
            VectorQuantity(listOf(DDdummy1, DDdummy2), Unit("m"), unitSpec = "notDefinedUnit")
        }
        assertFailsWith<UnknownUnitError> {
            val q = VectorQuantity(DDdummy1, "m")
            q.copy(unitSpec = "notDefinedUnit")
        }
        assertFailsWith<UnknownUnitError> {
            val q = VectorQuantity(DDdummy1, "m")
            q.valuesIn("notDefinedUnit")
        }
        assertFailsWith<UnknownUnitError> {
            val q = VectorQuantity(DDdummy1, "m")
            q.valueIn("notDefinedUnit")
        }
    }

    @Test
    fun immutabilityTest() {
        val q1 = VectorQuantity(DDdummy1, "m", userWantedUnitSpec = true)
        val q1InitialValues = q1.values
        val q1InitialUnit = q1.unit
        assertSame(q1, q1.clone(), "clone() should return the same instance since VectorQuantity is immutable")

        val q2 = q1.copy(unitSpec = "km")
        assertEquals("km", q2.unitSpec)
        assertEquals("m", q1.unitSpec)

        val q3 = q1.copy(userWantedUnitSpec = false)
        assertEquals(false, q3.userWantedUnitSpec)
        assertEquals(true, q1.userWantedUnitSpec)

        val q4 = VectorQuantity(DDdummy2, "m")
        val sum = q1 + q4
        assertEquals(q1InitialValues, q1.values)
        assertEquals(q1InitialUnit, q1.unit)
        assertBounds(3.0, sum.values[0].asAadd())
    }

    @Test
    fun unitOnlyAllowedWithAaddTest() {
        val idd = IDDdummy1
        val bdd = BDDdummyT

        // Disallowed: with non-empty Unit or unitString for IDD, BDD
        assertFailsWith<DDError> {
            VectorQuantity(idd, "m")
        }
        assertFailsWith<DDError> {
            VectorQuantity(idd, Unit("m"))
        }
        assertFailsWith<DDError> {
            VectorQuantity(listOf(idd), "m")
        }
        assertFailsWith<DDError> {
            VectorQuantity(listOf(idd), Unit("m"))
        }
        assertFailsWith<DDError> {
            VectorQuantity.fromCanonical(idd, Unit("m"))
        }
        assertFailsWith<DDError> {
            VectorQuantity.fromCanonical(listOf(idd), Unit("m"))
        }
        assertFailsWith<DDError> {
            VectorQuantity(bdd, Unit("s"))
        }
        assertFailsWith<DDError> {
            VectorQuantity(listOf(bdd), "s")
        }

        // Allowed: AADD with Unit or unitString
        val aaddVq1 = VectorQuantity(DDdummy1, "m")
        kotlin.test.assertEquals("m", aaddVq1.unitSpec)
        val aaddVq2 = VectorQuantity(DDdummy1, Unit("m"))
        kotlin.test.assertEquals("m", aaddVq2.unit.unitSet.first().symbol)
        val aaddVq3 = VectorQuantity.fromCanonical(DDdummy1, Unit("m"), "m")
        kotlin.test.assertEquals("m", aaddVq3.unitSpec)

        // Allowed: Unitless constructors or empty Unit for IDD, BDD
        val iddVq1 = VectorQuantity(idd)
        kotlin.test.assertEquals(VectorQuantity.NO_UNIT, iddVq1.unit)
        val iddVq2 = VectorQuantity(listOf(idd))
        kotlin.test.assertEquals(VectorQuantity.NO_UNIT, iddVq2.unit)
        val iddVq3 = VectorQuantity.fromCanonical(idd)
        kotlin.test.assertEquals(VectorQuantity.NO_UNIT, iddVq3.unit)
        val iddVq4 = VectorQuantity(idd, Unit())
        kotlin.test.assertEquals(VectorQuantity.NO_UNIT, iddVq4.unit)
        val bddVq = VectorQuantity(bdd)
        kotlin.test.assertEquals(VectorQuantity.NO_UNIT, bddVq.unit)
    }
}

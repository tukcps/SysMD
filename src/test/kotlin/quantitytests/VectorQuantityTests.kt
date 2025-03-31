@file:Suppress("PrivatePropertyName")

package quantitytests

import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.VectorQuantity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @BeforeAll
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
            BDDdummyF = False
            BDDdummyT = True
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
        assertEquals(6.0,result.values[0].asAadd().min, 0.00001)
        assertEquals(6.0,result.values[1].asAadd().min, 0.00001)
        assertEquals(10.0,result.values[2].asAadd().min, 0.00001)
        assertEquals(10.0,result.values[3].asAadd().min, 0.00001)

        val quantity3 = VectorQuantity(DDdummy0, u1)
        val result2 = quantity1.plus(quantity3)
        assertEquals(1.0,result2.values[0].asAadd().min, 0.00001)
        assertEquals(5.0,result2.values[1].asAadd().min, 0.00001)
        assertEquals(0.0,result2.values[2].asAadd().min, 0.00001)
        assertEquals(10.0,result2.values[3].asAadd().min, 0.00001)
    }

    @Test
    fun vectorPlusIntZero() {
        val vec1 = listOf(IDDdummy1,IDDdummy5,IDDdummy100)
        val quantity1 = VectorQuantity(vec1)
        val quantity2 = Quantity(IDDdummy0)
        val result = quantity1.plus(quantity2)
        assertEquals(1,result.values[0].asIdd().min)
        assertEquals(5,result.values[1].asIdd().min)
        assertEquals(100,result.values[2].asIdd().min)

        val result2 = quantity1.plus(quantity2)
        assertEquals(1,result2.values[0].asIdd().min)
        assertEquals(5,result2.values[1].asIdd().min)
        assertEquals(100,result2.values[2].asIdd().min)
    }

    @Test
    fun vectorMinus() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1,DDdummy5,DDdummy0,DDdummy10)
        val vec2 = listOf(DDdummy5,DDdummy1,DDdummy10,DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.minus(quantity2)
        assertEquals(-4.0,result.values[0].asAadd().min, 0.00001)
        assertEquals(4.0,result.values[1].asAadd().min, 0.00001)
        assertEquals(-10.0,result.values[2].asAadd().min, 0.00001)
        assertEquals(10.0,result.values[3].asAadd().min, 0.00001)
    }


    @Test
    fun vectorTimesScalar() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1,DDdummy5,DDdummy0,DDdummy10)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = Quantity(DDdummy10, u1)
        val result = quantity1.times(quantity2)
        assertEquals(10.0, result.values[0].asAadd().min, 0.00001)
        assertEquals(50.0, result.values[1].asAadd().min, 0.00001)
        assertEquals(0.0, result.values[2].asAadd().min, 0.00001)
        assertEquals(100.0, result.values[3].asAadd().min, 0.00001)

        //other order
        val result1 = quantity2.times(quantity1)
        assertEquals(10.0, result1.values[0].asAadd().min, 0.00001)
        assertEquals(50.0, result1.values[1].asAadd().min, 0.00001)
        assertEquals(0.0, result1.values[2].asAadd().min, 0.00001)
        assertEquals(100.0, result1.values[3].asAadd().min, 0.00001)
    }

    @Test
    fun vectorScalarDiv() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy10,DDdummy100,DDdummy1,DDdummy200)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = Quantity(DDdummy2, u1)
        val result = quantity1.div(quantity2)
        assertEquals(5.0,result.values[0].asAadd().min, 0.00001)
        assertEquals(50.0,result.values[1].asAadd().min, 0.00001)
        assertEquals(0.5,result.values[2].asAadd().min, 0.00001)
        assertEquals(100.0,result.values[3].asAadd().min, 0.00001)
    }

    @Test
    fun vectorNegate() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1,DDdummy5,DDdummy0,DDdummy10)
        val quantity = VectorQuantity(vec1, u1)
        val result = quantity.negate()
        assertEquals(-1.0,result.values[0].asAadd().min, 0.00001)
        assertEquals(-5.0,result.values[1].asAadd().min, 0.00001)
        assertEquals(0.0,result.values[2].asAadd().min, 0.00001)
        assertEquals(-10.0,result.values[3].asAadd().min, 0.00001)
    }

    @Test
    fun vectorNegateInt() {
        val vec1 = listOf(IDDdummy1,IDDdummy0,IDDdummy100,IDDdummy5)
        val quantity = VectorQuantity(vec1)
        val result = quantity.negate()
        assertEquals(-1,result.values[0].asIdd().min)
        assertEquals(0,result.values[1].asIdd().min)
        assertEquals(-100,result.values[2].asIdd().min)
        assertEquals(-5,result.values[3].asIdd().min)
    }

    @Test
    fun vectorAbsTest() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy3,DDdummy0,DDdummy4)
        val quantity = VectorQuantity(vec1, u1)
        val result = quantity.abs()
        assertEquals(5.0,result.getMinAsDouble(),0.000001)
    }

    @Test
    fun vectorAbsTestInt() {
        val vec1 = listOf(IDDdummy3,IDDdummy0,IDDdummy4)
        val quantity = VectorQuantity(vec1)
        val result = quantity.abs()
        assertEquals(5,result.value.asIdd().min)
    }

    @Test
    fun vectorCeilTest() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1_5,DDdummy0,DDdummy2_5,DDdummy0_5)
        val quantity = VectorQuantity(vec1, u1)
        val result = quantity.ceil()
        assertEquals(2.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(0.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(3.0,result.values[2].asAadd().min, 0.0000001)
        assertEquals(1.0,result.values[3].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorFloorTest() {
        val vec1 = listOf(DDdummy1_5,DDdummy2_5,DDdummy0_5)
        val u1 = Unit("m")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.floor()
        assertEquals(1.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(2.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(0.0,result.values[2].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorSqrtTest() {
        val vec1 = listOf(DDdummy100,DDdummy1,DDdummy4,DDdummy36)
        val u1 = Unit("m^2")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.sqrt()
        assertEquals(10.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(1.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(2.0,result.values[2].asAadd().min, 0.0000001)
        assertEquals(6.0,result.values[3].asAadd().min, 0.0000001)
        assertEquals("m",result.unit.toString())
    }

    @Test
    fun vectorSqrtIntTest() {
        val vec1 = listOf(IDDdummy4,IDDdummy1,IDDdummy0)
        val quantity = VectorQuantity(vec1)
        val result = quantity.sqrt()
        assertEquals(2,result.values[0].asIdd().min)
        assertEquals(1,result.values[1].asIdd().min)
        assertEquals(0,result.values[2].asIdd().min)
    }

    @Test
    fun vectorSqrTest() {
        val vec1 = listOf(DDdummy10,DDdummy1,DDdummy4,DDdummy2_5)
        val u1 = Unit("m")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.sqr()
        assertEquals(100.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(1.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(16.0,result.values[2].asAadd().min, 0.0000001)
        assertEquals(6.25,result.values[3].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorSqrIntTest() {
        val vec1 = listOf(IDDdummy4,IDDdummy100,IDDdummy0,IDDdummy5)
        val quantity = VectorQuantity(vec1)
        val result = quantity.sqr()
        assertEquals(16,result.values[0].asIdd().min)
        assertEquals(10000,result.values[1].asIdd().min)
        assertEquals(0,result.values[2].asIdd().min)
        assertEquals(25,result.values[3].asIdd().min)
    }

    @Test
    fun vectorLogTest() {
        val vec1 = listOf(DDdummy10,DDdummy1,DDdummy4,DDdummy2_5)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.ln()
        assertEquals(2.3025850929940455,result.values[0].asAadd().min, 0.0000001)
        assertEquals(0.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(1.3862943611198904,result.values[2].asAadd().min, 0.0000001)
        assertEquals(0.916290731874155,result.values[3].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorLogBaseTest() {
        val vec1 = listOf(DDdummy10,DDdummy1,DDdummy1000,DDdummy100)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.log(DDdummy10)
        assertEquals(1.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(0.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(3.0,result.values[2].asAadd().min, 0.0000001)
        assertEquals(2.0,result.values[3].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorLogBaseIDDTest() {
        val vec1 = listOf(IDDdummy100,IDDdummy1,IDDdummy10)
        val quantity = VectorQuantity(vec1)
        val result = quantity.log(IDDdummy10)
        assertEquals(2,result.values[0].asIdd().min)
        assertEquals(0,result.values[1].asIdd().min)
        assertEquals(1,result.values[2].asIdd().min)
    }

    @Test
    fun vectorExpTest() {
        val vec1 = listOf(DDdummy1,DDdummy0,DDdummy2,DDdummy3)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.exp()
        assertEquals(2.7182818284590446,result.values[0].asAadd().min, 0.0000001)
        assertEquals(1.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(7.38905609893065,result.values[2].asAadd().min, 0.0000001)
        assertEquals(20.085536923187664,result.values[3].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorPow2Test() {
        val vec1 = listOf(DDdummy1,DDdummy0,DDdummy2,DDdummy3)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.pow2()
        assertEquals(2.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(1.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(4.0,result.values[2].asAadd().min, 0.0000001)
        assertEquals(8.0,result.values[3].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorPow2IntTest() {
        val vec1 = listOf(IDDdummy1,IDDdummy3,IDDdummy4,IDDdummy0)
        val quantity = VectorQuantity(vec1)
        val result = quantity.pow2()
        assertEquals(2,result.values[0].asIdd().min)
        assertEquals(8,result.values[1].asIdd().min)
        assertEquals(16,result.values[2].asIdd().min)
        assertEquals(1,result.values[3].asIdd().min)
    }
    @Test
    fun vectorPowTest() {
        val vec1 = listOf(DDdummy1,DDdummy0,DDdummy2,DDdummy3)
        val u1 = Unit("")
        val quantity = VectorQuantity(vec1,u1)
        val result = quantity.pow(DDdummy2)
        assertEquals(1.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(0.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(4.0,result.values[2].asAadd().min, 0.0000001)
        assertEquals(9.0,result.values[3].asAadd().min, 0.0000001)
    }

    @Test
    fun vectorPowIntTest() {
        val vec1 = listOf(IDDdummy1,IDDdummy3,IDDdummy4,IDDdummy0)
        val quantity = VectorQuantity(vec1)
        val result = quantity.pow(IDDdummy2)
        assertEquals(1,result.values[0].asIdd().min)
        assertEquals(9,result.values[1].asIdd().min)
        assertEquals(16,result.values[2].asIdd().min)
        assertEquals(0,result.values[3].asIdd().min)
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
        assertEquals("(0.27778, 0.83333, 1.11111, 0) m/s", quantity2.toString())
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
        assertEquals(10.0, result.value.asAadd().min, 0.00001)

    }

    @Test
    fun vectorCrossProduct() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy5, DDdummy10)
        val vec2 = listOf(DDdummy5, DDdummy1, DDdummy10)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.cross(quantity2)
        assertEquals(40.0,result.values[0].asAadd().min, 0.0000001)
        assertEquals(40.0,result.values[1].asAadd().min, 0.0000001)
        assertEquals(-24.0,result.values[2].asAadd().min, 0.0000001)
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
        assertEquals(0.6,result1.values[0].asAadd().min, 0.0000001)
        assertEquals(0.8,result1.values[1].asAadd().min, 0.0000001)
        assertEquals(0.0,result1.values[2].asAadd().min, 0.0000001)
        assertEquals(0.9407208683835953,result2.values[0].asAadd().min, 0.00000001)
        assertEquals(0.28221626051507853,result2.values[1].asAadd().min, 0.00000001)
        assertEquals(0.18814417367671904,result2.values[2].asAadd().min, 0.00000001)
    }

    @Test
    fun vectorAngle() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val vec2 = listOf(DDdummy1, DDdummy0, DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertEquals(45.0,result.valueIn("°").asAadd().min, 0.0000001)
    }

    @Test
    fun vectorAngle2() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val vec2 = listOf(DDdummyNeg1, DDdummyNeg1, DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertEquals(180.0,result.valueIn("°").asAadd().min, 0.00001)
    }
    @Test
    fun vectorAngle3() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val vec2 = listOf(DDdummy1, DDdummy1, DDdummy0)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertEquals(0.0,result.valueIn("°").asAadd().min, 0.00001)
    }

    @Test
    fun vectorAngle4() {
        val u1 = Unit("m")
        val vec1 = listOf(DDdummy1, DDdummy5, DDdummy10)
        val vec2 = listOf(DDdummy5, DDdummy2, DDdummyNeg1)
        val quantity1 = VectorQuantity(vec1, u1)
        val quantity2 = VectorQuantity(vec2, u1)
        val result = quantity1.angle(quantity2)
        assertEquals(85.33526881505367,result.valueIn("°").asAadd().min, 0.0000001)
        assertEquals(85.33526881505367,result.valueIn("°").asAadd().max, 0.0000001)
    }
}

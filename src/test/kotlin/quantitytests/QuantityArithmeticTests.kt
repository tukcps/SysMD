package quantitytests

import com.github.tukcps.sysmd.cspsolver.Solver
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.cspsolver.VariableImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.MembershipImplementation
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.IDD
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.math.ln
import kotlin.math.pow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QuantityArithmeticTests {
    private lateinit var ddDummy0: AADD
    private lateinit var ddDummy1: AADD
    private lateinit var ddDummy5: AADD
    private lateinit var ddDummy10: AADD
    private lateinit var ddDummy20: AADD
    private lateinit var ddDummy30: AADD
    private lateinit var ddDummy50: AADD
    private lateinit var ddDummy99: AADD
    private lateinit var ddDummy100: AADD
    private lateinit var ddDummy200: AADD
    private lateinit var ddDummyMinus50: AADD
    private lateinit var aaddDummy100: AADD
    private lateinit var aaddDummy5: AADD
    private lateinit var iddDummy1: IDD
    private lateinit var iddDummy5: IDD

    private val precisionExpMinus6 = 0.000001

    @BeforeTest
    fun setUp() {
        DDBuilder {
            ddDummy0 = real(0.0)
            ddDummy1 = real(1.0)
            ddDummy5 = real(5.0)
            ddDummy10 = real(10.0)
            ddDummy20 = real(20.0)
            ddDummy30 = real(30.0)
            ddDummy50 = real(50.0)
            ddDummy99 = real(99.0)
            ddDummy100 = real(100.0)
            ddDummy200 = real(200.0)
            ddDummyMinus50 = real(-50.0)
            aaddDummy100 = real(-100.0..100.0)
            aaddDummy5 = real(-5.0..5.0)
            iddDummy1 = integer(-1L .. 1)
            iddDummy5 = integer(-5L .. 5)
        }
    }

    @Test
    fun multiplication1() {
        val u1 = Unit("m / s")
        val u2 = Unit("s")

        val result = Quantity(ddDummy10, u1).times(Quantity(ddDummy20, u2))
        assertEquals(200.0, (result.value as AADD).getRange().min, 0.000001)

        val u3 = Unit("N")
        val u4 = Unit("m")

        val result2 = Quantity(ddDummy10, u3).times(Quantity(ddDummy20, u4))
        assertEquals(200.0, (result2.valueIn("N m") as AADD).getRange().min, 0.000001)

        val u5 = Unit("m")
        val u6 = Unit("1/m")
        val result3 = Quantity(ddDummy1, u5).times(Quantity(ddDummy1, u6))
        assert(result3.unit.unitSet.isEmpty())
    }

    @Test
    fun multiplication2() {
        val u1 = Unit("km")
        val u2 = Unit("m")

        val result = Quantity(ddDummy1, u1).times(Quantity(ddDummy10, u2))
        assertEquals("m^2", result.unit.toString())
        assertEquals(10000.0, (result.value as AADD).getRange().min, 0.00001)
        val resultInKm2 = result.valueIn("km^2")
        assertEquals(0.01, (resultInKm2 as AADD).getRange().min, 0.000001)
    }

    @Test
    fun multiplicationPercentage() {
        val u1 = Unit("m")
        val u2 = Unit("%")

        val q1 = Quantity(ddDummy50, u2)
        var result = Quantity(ddDummy5, u1).times(q1)
        assertEquals(2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q2 = Quantity(ddDummy50, u2)
        result = q2.times(Quantity(ddDummy5, u1))
        assertEquals(2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q3 = Quantity(ddDummy5, u2)
        result = q3.times(Quantity(ddDummy50, u1))
        assertEquals(2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q4 = Quantity(ddDummyMinus50, u2)
        result = Quantity(ddDummy5, u1).times(q4)
        assertEquals(-2.5, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)
    }

    @Test
    fun multiplicationOnlyPercentage() {
        val u1 = Unit("%")
        val u2 = Unit("%")

        val q1 = Quantity(ddDummyMinus50, u1)
        val q2 = Quantity(ddDummyMinus50, u2)
        var result = q1.times(q2)
        val valueInPercentage = result.valueIn("%")
        assertEquals(0.25, (result.value as AADD).getRange().min, 0.000001)
        assertEquals(25.0, (valueInPercentage.asAadd().getRange().min), 0.000001)

        val q3 = Quantity(ddDummy200, u1)
        val q4 = Quantity(ddDummy100, u2)
        result = q3.times(q4)
        assertEquals(2.0, (result.value as AADD).getRange().min, 0.000001)
    }

    @Test
    fun division1() {
        val u1 = Unit("m")
        val u2 = Unit("s")

        val result = Quantity(ddDummy10, u1).div(Quantity(ddDummy20, u2))
        assertEquals("m / s", result.unit.toString())
        assertEquals(0.5, (result.value as AADD).getRange().min, 0.000001)
    }

    @Test
    fun division2() {
        val u1 = Unit("km")
        val u2 = Unit("s")

        val result = Quantity(ddDummy10, u1).div(Quantity(ddDummy20, u2))
        assertEquals("m / s", result.unit.toString())
        assertEquals(500.0, (result.value as AADD).getRange().min, 0.000001)
    }

    @Test
    fun divisionPercentage() {
        val u1 = Unit("m")
        val u2 = Unit("%")

        val q1a = Quantity(ddDummy50, u2)
        var result = Quantity(ddDummy5, u1).div(q1a)
        assertEquals(10.0, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)

        val q1 = Quantity(ddDummy50, u2)
        val q2 = Quantity(ddDummy5, u1)
        result = q1.div(q2)
        assertEquals(0.1, (result.value as AADD).getRange().min, 0.000001)
        assertEquals("meter", result.unit.unitSet.elementAt(0).name)
    }

    @Test
    fun divisionOnlyPercentage() {
        val u1 = Unit("%")
        val u2 = Unit("%")

        val result = Quantity(ddDummy50, u1).div(Quantity(ddDummy50, u2))
        assertEquals(1.0, (result.value as AADD).getRange().min, 0.000001)
    }

    @Test
    fun addition() {
        val u1 = Unit("kN")
        val u2 = Unit("kN")

        var result = Quantity(ddDummy1, u1).plus(Quantity(ddDummy10, u2))
        assertEquals("kg m / s^2", result.unit.toString())
        assertEquals(11000.0, (result.value as AADD).getRange().min, 0.00001)

        val q3 = Quantity(ddDummy1, Unit("%"))
        val q4 = Quantity(ddDummy10, Unit("%"))

        result = q3.plus(q4)
        assertEquals("1", result.unit.toString())
        assertEquals(0.11, (result.value as AADD).getRange().min, 0.00001)
    }

    @Test
    fun subtraction() {
        val u1 = Unit("kN")
        val u2 = Unit("N")

        val q1 = Quantity(ddDummy1, u1)
        val q2 = Quantity(ddDummy100, u2)
        var result = q1.minus(q2)
        assertEquals("kg m / s^2", result.unit.toString())
        assertEquals(900.0, (result.value as AADD).getRange().min, 0.001)

        val q3 = Quantity(ddDummy50, Unit("%"))
        val q4 = Quantity(ddDummy20, Unit("%"))

        result = q3.minus(q4)
        assertEquals("1", result.unit.toString())
        assertEquals(0.3, (result.value as AADD).getRange().min, 0.0001)
    }

    @Test
    fun sqrt() {
        val u = Unit("m^2")
        var result = Quantity(ddDummy100, u).sqrt()
        assertEquals(10.0, (result.value as AADD).getRange().min, precisionExpMinus6)
        assertEquals("m", result.unit.toString())

        val u1 = Unit("s^4 m^2")
        result = Quantity(ddDummy20, u1).sqrt()
        assertEquals(4.472135955, (result.value as AADD).getRange().min, precisionExpMinus6)
        assertEquals("m s^2", result.unit.toString())
    }

    @Test
    fun decibelAddition() {
        val quant1 = Quantity(ddDummy10, "dB")
        val quant2 = Quantity(ddDummy10, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(1.0) + 10.0.pow(1.0)) / ln(10.0),
            (quant1 + quant2).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )

        val quant3 = Quantity(ddDummy99, "dB")
        val quant4 = Quantity(ddDummy100, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(10.0) + 10.0.pow(9.9)) / ln(10.0),
            (quant3 + quant4).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )
    }

    @Test
    fun decibelSubtraction() {
        val quant1 = Quantity(ddDummy10, "dB")
        val quant2 = Quantity(ddDummy10, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(1.0) - 10.0.pow(1.0)) / ln(10.0),
            (quant1 - quant2).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )

        val quant3 = Quantity(ddDummy99, "dB")
        val quant4 = Quantity(ddDummy100, "dB")
        assertEquals(
            10.0 * ln(10.0.pow(10.0) - 10.0.pow(9.9)) / ln(10.0),
            (quant4 - quant3).valueIn("dB").asAadd().getRange().min,
            0.0000001
        )
    }

    @Test
    fun quantity1() {
        var quant1 = Quantity(ddDummy100, "m")
        var quant2 = Quantity(ddDummy50, "%")
        var quantResult = quant1 * quant2
        assertEquals(50.0, quantResult.getMinAsDouble(), 0.000001)
        assertEquals("m", quantResult.unit.toString())

        quant1 = Quantity(ddDummy20, "%")
        quant2 = Quantity(ddDummy10, "%")
        quantResult = quant1 + quant2
        assert(quantResult.getRange().contains(0.3))
        assertEquals("1", quantResult.unit.toString())
    }

    @Test
    fun modify() {
        val quant = Quantity(ddDummy1, "m^2/s^2")
        val quantCopy = quant.clone()
        val quant2 = Quantity(ddDummy10, "km^2/h^2")
        val quant2Copy = quant2.clone()
        assertEquals(quantCopy, quant)
        quant.sqr()
        assertEquals(quantCopy, quant)
        quant.sqrt()
        assertEquals(quantCopy, quant)
        quant.times(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.plus(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.div(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.minus(quant2)
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
        quant.valueIn(quant2.unit.toString())
        assertEquals(quant2Copy, quant2)
        assertEquals(quantCopy, quant)
    }

    @Test
    fun intersect() {
        val model = SessionImplementation()
        val solver = Solver(model)
        val p = VariableImplementation(
            MembershipImplementation(model, memberElement = FeatureImplementation(model)),
            baseType = Variable.BaseType.Real,
            solver = solver,
            path = "p",
        )
        p.vectorQuantity = Quantity(aaddDummy100, "cm")
        val upQuantity = Quantity(aaddDummy5, "m")
        assertEquals(-1.0, p.vectorQuantity.intersect(upQuantity).value.asAadd().getRange().min, 0.000001)
        assertEquals(1.0, p.vectorQuantity.intersect(upQuantity).value.asAadd().getRange().max, 0.000001)
    }

    @Test
    fun intersectInt() {
        val model = SessionImplementation()
        val solver = Solver(model)
        val p = VariableImplementation(
            MembershipImplementation(model, memberElement = FeatureImplementation(model)),
            solver=solver,
            path = "p",
            baseType = Variable.BaseType.Real
        )
        p.vectorQuantity = Quantity(iddDummy1)
        val upQuantity = Quantity(iddDummy5)
        assertEquals(-1, p.vectorQuantity.intersect(upQuantity).value.asIdd().getRange().min)
        assertEquals(1, p.vectorQuantity.intersect(upQuantity).value.asIdd().getRange().max)
    }

    @Test
    fun constraint() {
        val model = SessionImplementation()
        val solver = Solver(model)
        val p = VariableImplementation(
            MembershipImplementation(model, memberElement = FeatureImplementation(model)),
            solver = solver, path = "p", baseType = Variable.BaseType.Real
        )
        p.rangeSpec(Range("-0.5..2"))
        p.vectorQuantity = Quantity(aaddDummy100, "cm")
        assertEquals(
            -0.5,
            p.vectorQuantity.constrain(p.vectorQuantity, p.rangeSpecs, p.unitSpec).value.asAadd().getRange().min,
            0.000001
        )
        assertEquals(
            1.0,
            p.vectorQuantity.constrain(p.vectorQuantity, p.rangeSpecs, p.unitSpec).value.asAadd().getRange().max,
            0.000001
        )
    }

    @Test
    fun constraintInt() {
        val model = SessionImplementation()
        val solver = Solver(model)
        val p = VariableImplementation(
            MembershipImplementation(model, memberElement = FeatureImplementation(model)),
            solver=solver, path = "p", baseType = Variable.BaseType.Real)
        p.vectorQuantity = Quantity(iddDummy1)
        p.intSpec(IntegerRange("0..2"))
        assertEquals(0, p.vectorQuantity.constrain(p.intSpecs).value.asIdd().getRange().min)
        assertEquals(1, p.vectorQuantity.constrain(p.intSpecs).value.asIdd().getRange().max)
    }

    @Test
    fun addZero() {
        val q1 = Quantity(ddDummy10, "m")
        val q2 = Quantity(ddDummy0, "")
        val qResult = q1.plus(q2)
        assertEquals(10.0, qResult.value.asAadd().getRange().min, 0.000001)
        assertEquals(10.0, qResult.value.asAadd().getRange().max, 0.000001)
    }

    @Test
    fun unitsMixed() = testSession("ISQ") {
        loadKerML("""
                feature percentage: Quantities::ScalarQuantityValue(* [%]) = 10.0 [%];
                feature number: ScalarValues::Real = 1.0;
                feature result: ScalarValues::Real = percentage + number;
                feature ratio: Quantities::ScalarQuantityValue(* [dB]) = 10.0 [dB];
                feature result2: ScalarValues::Real[1] = ln(ratio)/ln(10.0);
                feature result3: ScalarValues::Real = power2(ratio);
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0.1, solver.getVariable("percentage")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(1.0, solver.getVariable("number")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(1.1, solver.getVariable("result")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(
            10.0,
            solver.getVariable("ratio")!!.vectorQuantity.valuesIn("dB")[0].asAadd().getRange().min,
            0.0000001
        )
        assertEquals(1.0, solver.getVariable("result2")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(1024.0, solver.getVariable("result3")!!.vectorQuantity.getMinAsDouble(), 0.0000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test
    fun additionNegative() {
        val u1 = Unit("kN")
        val u2 = Unit("kN")

        val q1 = Quantity(ddDummyMinus50, u1)
        val q2 = Quantity(ddDummyMinus50, u2)
        val result = q1.plus(q2)
        assertEquals("kg m / s^2", result.unit.toString())
        assertEquals(-100000.0, (result.value as AADD).getRange().min, 0.00001)
    }

    @Test
    fun subtractionNegative() {
        val u1 = Unit("kN")
        val u2 = Unit("N")

        val q1 = Quantity(ddDummyMinus50, u1)
        val q2 = Quantity(ddDummy100, u2)
        val result = q1.minus(q2)
        assertEquals("kg m / s^2", result.unit.toString())
        assertEquals(-50100.0, (result.value as AADD).getRange().min, 0.001)
    }

    @Test
    fun divisionNegative() {
        val u1 = Unit("m")
        val u2 = Unit("s")

        val q1 = Quantity(ddDummyMinus50, u1)
        val q2 = Quantity(ddDummy10, u2)
        val result = q1.div(q2)
        assertEquals("m / s", result.unit.toString())
        assertEquals(-5.0, (result.value as AADD).getRange().min, 0.000001)
    }
}

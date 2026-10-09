package quantitytests

import com.github.tukcps.sysmd.quantities.Representer
import io.github.tukcps.aadd.DDBuilder
import io.github.tukcps.aadd.values.bounds.DoubleBound
import kotlin.test.Test
import kotlin.test.assertEquals

class RepresenterTest {
    private val r = Representer()
    private val builder = DDBuilder()


    @Test
    fun testZeroAndNearZero() {
        assertEquals("0", r.formatNumber(0.0))
        assertEquals("0", r.formatNumber(-0.0))
        assertEquals("0", r.represent(builder.real(0.0)))
        assertEquals("0", r.represent(builder.real(0.0..0.0)))
    }

    @Test
    fun testSingleValuesFromTests() {
        // Powers of 10 from singleValues3
        assertEquals("100e-9", r.formatNumber(0.0000001))
        assertEquals("1e-6", r.formatNumber(0.000001))
        assertEquals("10e-6", r.formatNumber(0.00001))
        assertEquals("100e-6", r.formatNumber(0.0001))
        assertEquals("0.001", r.formatNumber(0.001))
        assertEquals("0.01", r.formatNumber(0.01))
        assertEquals("0.1", r.formatNumber(0.1))
        assertEquals("1", r.formatNumber(1.0))
        assertEquals("10", r.formatNumber(10.0))
        assertEquals("100", r.formatNumber(100.0))
        assertEquals("1000", r.formatNumber(1000.0))
        assertEquals("10000", r.formatNumber(10000.0))
        assertEquals("100000", r.formatNumber(100000.0))
        assertEquals("1e6", r.formatNumber(1000000.0))
        assertEquals("10e6", r.formatNumber(10000000.0))
        assertEquals("100e6", r.formatNumber(100000000.0))
        assertEquals("1e9", r.formatNumber(1000000000.0))
        assertEquals("10e9", r.formatNumber(10000000000.0))

        // Values with 5 from singleValues4
        assertEquals("500e-9", r.formatNumber(0.0000005))
        assertEquals("5e-6", r.formatNumber(0.000005))
        assertEquals("50e-6", r.formatNumber(0.00005))
        assertEquals("500e-6", r.formatNumber(0.0005))
        assertEquals("0.005", r.formatNumber(0.005))
        assertEquals("0.05", r.formatNumber(0.05))
        assertEquals("0.5", r.formatNumber(0.5))
        assertEquals("5", r.formatNumber(5.0))
        assertEquals("50", r.formatNumber(50.0))
        assertEquals("500", r.formatNumber(500.0))
        assertEquals("5000", r.formatNumber(5000.0))
        assertEquals("50000", r.formatNumber(50000.0))
        assertEquals("500000", r.formatNumber(500000.0))
        assertEquals("5e6", r.formatNumber(5000000.0))
        assertEquals("50e6", r.formatNumber(50000000.0))
        assertEquals("500e6", r.formatNumber(500000000.0))
        assertEquals("5e9", r.formatNumber(5000000000.0))
        assertEquals("50e9", r.formatNumber(50000000000.0))
    }

    @Test
    fun testNegativeValues() {
        assertEquals("-100e-9", r.formatNumber(-0.0000001))
        assertEquals("-0.001", r.formatNumber(-0.001))
        assertEquals("-5", r.formatNumber(-5.0))
        assertEquals("-5e6", r.formatNumber(-5000000.0))
        assertEquals("0", r.formatNumber(-0.0))
        assertEquals("0", r.formatNumber(-1e-250))
    }


    @Test
    fun testRanges() {
        assertEquals("4..9", r.represent(builder.real(4.0..9.0)))
        assertEquals("0..1000", r.represent(builder.real(0.0..1000.0)))
        assertEquals("0..100", r.represent(builder.real(0.0..100.0)))
        assertEquals("0.04..10000", r.represent(builder.real(0.04..10000.0)))
        assertEquals("1..10000", r.represent(builder.real(1.0..10000.0)))
        assertEquals("4e12..9e12", r.represent(builder.real(4e12..9e12)))
        assertEquals("0.002..0.006", r.represent(builder.real(0.002..0.006)))
        assertEquals("2000..6000", r.represent(builder.real(2000.0..6000.0)))
        assertEquals("20000..30000", r.represent(builder.real(20000.0..30000.0)))
        assertEquals("0..130", r.represent(builder.real(0.0..130.0)))
        assertEquals("0..36.111", r.represent(builder.real(0.0..36.111111111)))
        // Symmetric relative zero:
        assertEquals("0..1000", r.represent(builder.real(1e-12..1000.0)))
        assertEquals("-1000..0", r.represent(builder.real(-1000.0..-1e-12)))
    }

    @Test
    fun testInfinities() {
        // FIXME: When adopting -*..*, change expected "*..*" to "-*..*" to better show the negative part
        assertEquals("*..*", r.represent(builder.Reals.All))
        assertEquals("*", r.represent(builder.real(DoubleBound.PositiveInfinity..DoubleBound.PositiveInfinity)))
        assertEquals("*", r.represent(DoubleBound.PositiveInfinity))
        // FIXME: When adopting -*..*, NegativeInfinity may be represented as "-*"
        assertEquals("*", r.represent(DoubleBound.NegativeInfinity))
    }

    @Test
    fun testEmpty() {
        assertEquals("∅", r.represent(builder.Reals.Empty))
    }

    @Test
    fun testNegativeInfinityString() {
        val rep = Representer(negativeinfinityString = "-*")
        assertEquals("-*", rep.represent(DoubleBound.NegativeInfinity))
        assertEquals("*", rep.represent(DoubleBound.PositiveInfinity))
        assertEquals("-*..*", rep.represent(builder.Reals.All))
        assertEquals("-*", rep.negativeinfinityString)
        assertEquals("-*", rep.negativeInfinityString)
    }

    @Test
    fun testCompanionObject() {
        assertEquals("100", Representer.formatNumber(100.0))
        assertEquals("*", Representer.represent(DoubleBound.PositiveInfinity))
        assertEquals("4..9", Representer.represent(builder.real(4.0..9.0)))
    }
}

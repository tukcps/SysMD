package quantitytests

import com.github.tukcps.sysmd.quantities.AdditionError
import com.github.tukcps.sysmd.quantities.VectorQuantity
import com.github.tukcps.sysmd.quantities.TransformationError
import com.github.tukcps.sysmd.quantities.Unit
import io.github.tukcps.aadd.dd.AADD
import io.github.tukcps.aadd.DDBuilder
import util.assertBounds
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Currencies have no fixed exchange rate, so different currencies must behave like any other
 * incompatible units: they cannot be added, subtracted or converted into each other implicitly.
 */
class CurrencyTests {
    private lateinit var one: AADD
    private lateinit var hundred: AADD

    @BeforeTest
    fun setUp() {
        DDBuilder {
            one = real(1.0)
            hundred = real(100.0)
        }
    }

    @Test
    fun sameCurrencyAdds() {
        val sum = VectorQuantity(hundred, Unit("USD")) + VectorQuantity(hundred, Unit("USD"))
        assertEquals(200.0, sum.getMinAsDouble(), 0.00001)
        assertEquals("USD", sum.unit.toString())
    }

    @Test
    fun differentCurrenciesCannotBeAdded() {
        assertFailsWith<AdditionError> { VectorQuantity(hundred, Unit("USD")) + VectorQuantity(hundred, Unit("EUR")) }
        assertFailsWith<AdditionError> { VectorQuantity(hundred, Unit("GBP")) + VectorQuantity(hundred, Unit("USD")) }
    }

    @Test
    fun currencyKeepsItsOwnUnit() {
        assertEquals("EUR", VectorQuantity(hundred, Unit("EUR")).unit.toString())
        assertEquals("USD", VectorQuantity(hundred, Unit("USD")).unit.toString())
        assertEquals("GBP", VectorQuantity(hundred, Unit("GBP")).unit.toString())
    }

    @Test
    fun differentCurrenciesCannotBeConverted() {
        assertFailsWith<TransformationError> { VectorQuantity(hundred, Unit("USD")).valueIn("EUR") }
        assertFailsWith<TransformationError> { VectorQuantity(one, Unit("GBP")).valueIn("USD") }
    }

    @Test
    fun sameCurrencyConvertsToItself() {
        assertBounds(100.0, VectorQuantity(hundred, Unit("USD")).valueIn("USD").asAadd())
    }
}

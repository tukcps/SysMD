package quantitytests

import com.github.tukcps.sysmd.quantities.AdditionError
import com.github.tukcps.sysmd.quantities.Quantity
import com.github.tukcps.sysmd.quantities.TransformationError
import com.github.tukcps.sysmd.quantities.Unit
import io.github.tukcps.aadd.AADD
import io.github.tukcps.aadd.DDBuilder
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
        val sum = Quantity(hundred, Unit("USD")) + Quantity(hundred, Unit("USD"))
        assertEquals(200.0, sum.getMinAsDouble(), 0.00001)
        assertEquals("USD", sum.unit.toString())
    }

    @Test
    fun differentCurrenciesCannotBeAdded() {
        assertFailsWith<AdditionError> { Quantity(hundred, Unit("USD")) + Quantity(hundred, Unit("EUR")) }
        assertFailsWith<AdditionError> { Quantity(hundred, Unit("GBP")) + Quantity(hundred, Unit("USD")) }
    }

    @Test
    fun currencyKeepsItsOwnUnit() {
        assertEquals("EUR", Quantity(hundred, Unit("EUR")).unit.toString())
        assertEquals("USD", Quantity(hundred, Unit("USD")).unit.toString())
        assertEquals("GBP", Quantity(hundred, Unit("GBP")).unit.toString())
    }

    @Test
    fun differentCurrenciesCannotBeConverted() {
        assertFailsWith<TransformationError> { Quantity(hundred, Unit("USD")).valueIn("EUR") }
        assertFailsWith<TransformationError> { Quantity(one, Unit("GBP")).valueIn("USD") }
    }

    @Test
    fun sameCurrencyConvertsToItself() {
        val range = Quantity(hundred, Unit("USD")).valueIn("USD").asAadd().getRange()
        assertEquals(100.0, range.min, 0.00001)
    }
}

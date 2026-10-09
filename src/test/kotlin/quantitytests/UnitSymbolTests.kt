package quantitytests

import com.github.tukcps.sysmd.quantities.NoPrefix
import com.github.tukcps.sysmd.quantities.Unit
import com.github.tukcps.sysmd.quantities.derivedUnits.ElectricPotentialDifferenceValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The symbol of a unit must be the string under which it can be parsed again;
 * otherwise the string representation of a unit names a different unit or none at all.
 */
class UnitSymbolTests {

    @Test
    fun psiIsNotPrintedAsBar() {
        assertEquals("psi", Unit("psi").toString())
        assertEquals("bar", Unit("bar").toString())
    }

    @Test
    fun psiSymbolCanBeParsedAgain() {
        val psi = Unit("psi")
        val parsedAgain = Unit(psi.toString())
        assertEquals(psi.unitSet, parsedAgain.unitSet)
        assertEquals(6894.757293, parsedAgain.unitSet.first().convFac)
    }

    @Test
    fun yardSymbolCanBeParsedAgain() {
        val yard = Unit("yd")
        assertEquals("yd", yard.toString())
        val parsedAgain = Unit(yard.toString())
        assertEquals(yard.unitSet, parsedAgain.unitSet)
        assertEquals(0.9144, parsedAgain.unitSet.first().convFac)
    }

    @Test
    fun inchSymbolCanBeParsedAgain() {
        val inch = Unit("inch")
        assertEquals("inch", inch.toString())
        val parsedAgain = Unit(inch.toString())
        assertEquals(inch.unitSet, parsedAgain.unitSet)
        assertEquals(0.0254, parsedAgain.unitSet.first().convFac)
    }

    @Test
    fun symbolsWithExponentAndDenominator() {
        assertEquals("yd^2", Unit("yd^2").toString())
        assertEquals("inch / s", Unit("inch/s").toString())
    }

    @Test
    fun copyOfPotentialDifferenceKeepsIsDifference() {
        val difference = ElectricPotentialDifferenceValue("volt difference", "V", NoPrefix, 1.0, isDifference = true)
        val copy = difference.copy()
        assertTrue(copy.isDifference)
        assertFalse(copy.isLogarithmic)
    }

    @Test
    fun copyOfVoltIsNeitherDifferenceNorLogarithmic() {
        val copy = ElectricPotentialDifferenceValue.Volt.copy()
        assertFalse(copy.isDifference)
        assertFalse(copy.isLogarithmic)
    }
}

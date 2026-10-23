package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.baseUnits.ThermodynamicTemperature
import com.github.tukcps.sysmd.quantities.derivedUnits.DimensionOne
import io.github.tukcps.aadd.util.Tolerance
import io.github.tukcps.aadd.values.real.ia.RealRange
import io.github.tukcps.aadd.values.real.ia.abs
import io.github.tukcps.aadd.values.real.ia.log
import io.github.tukcps.aadd.values.real.ia.minus
import io.github.tukcps.aadd.values.real.ia.pow
import io.github.tukcps.aadd.values.real.ia.times
import java.io.Reader
import java.io.StreamTokenizer
import java.io.StringReader
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.min

class Unit(
    val unitSet: Set<UnitOfMeasurement> = emptySet(),
    val unitDomain: String = "",
    val calculatedUnitSymbol: String = "",
    val isLogarithmic: Boolean = false,
    val isDifference: Boolean = false,
    val unitStr: String = ""
) : Cloneable {

    constructor() : this(emptySet())

    constructor(unitStr: String, unitDomain: String = "") : this(parseAndValidate(unitStr, unitDomain))

    private constructor(parsed: ParsedUnit) : this(
        unitSet = parsed.unitSet,
        unitDomain = parsed.unitDomain,
        calculatedUnitSymbol = "",
        isLogarithmic = parsed.isLogarithmic,
        isDifference = parsed.isDifference,
        unitStr = parsed.unitStr
    )

    fun copy(
        unitSet: Set<UnitOfMeasurement> = this.unitSet,
        unitDomain: String = this.unitDomain,
        calculatedUnitSymbol: String = this.calculatedUnitSymbol,
        isLogarithmic: Boolean = this.isLogarithmic,
        isDifference: Boolean = this.isDifference,
        unitStr: String = this.unitStr
    ): Unit = Unit(unitSet, unitDomain, calculatedUnitSymbol, isLogarithmic, isDifference, unitStr)

    /**
     * Returns this unit since it is immutable.
     */
    public override fun clone(): Unit = this

    /**
     * Calculate the unit domain of the current unit after converting to SI unit
     * Also the unitSymbol of this domain is saved. It is used by the toString() method
     * If the unit is not a combined unit, the type was already assigned before and is simply returned
     */
    fun calculateUnitDomain(unitSpec: String = ""): Unit {
        if ((unitDomain.isEmpty() || unitSpec.isNotEmpty()) && unitStr != "?") {
            val unitString = if (unitSpec.isNotEmpty()) Unit(unitSpec).unitStr else unitStr
            if (unitSet.isEmpty()) {
                return copy(unitDomain = DimensionOne.One.domain)
            }
            val unitList = ConversionTables.unitsMap.values.filter { it.baseUnits == unitSet }
            // prefer the unit that was actually given ("uts" is a timestamp, although it contains the "s" of a duration)
            unitList.firstOrNull { unitString.replace(" ", "") == it.symbol }?.let {
                return copy(unitDomain = it.domain, isDifference = isDifference || it.isDifference)
            }
            //only return result directly, if unitStr contains the symbol of the compared unit (remove whitespaces first)
            unitList.firstOrNull { unitString.replace(" ", "").contains(it.symbol) }?.let {
                return copy(unitDomain = it.domain, isDifference = isDifference || it.isDifference)
            }
            unitList.firstOrNull()?.let {
                return copy(unitDomain = it.domain, isDifference = isDifference || it.isDifference)
            }
        }
        return this
    }

    /**
     * Calculate the unit symbol of the current unit
     */
    fun calculateUnitSymbol(unitSpec: String = ""): Unit {
        // Simple case only one element in SI unit list, because it is a base unit
        if (calculatedUnitSymbol == "" || unitSpec != "") {
            if (unitSpec != "") { // if unitSpec exists, use this as the unitString (no influence of calculations)
                return copy(calculatedUnitSymbol = unitSpec)
            }
            if (unitSet.isEmpty()) { // Quantity of domain one
                return copy(calculatedUnitSymbol = "")
            }
            //List of possible units for the domains
            val unitList = ConversionTables.unitsMap.values.filter { it.baseUnits == unitSet }

            //if there are units from different (or zero) domains
            //and also different units (or zero), return empty string, because no unique solution possible
            if (unitList.groupBy { it.domain }.size != 1 && unitList.groupBy { it.symbol }.size != 1) {
                return copy(calculatedUnitSymbol = "")
            } else {
                //For more than one unit of the same domain use the unit with the closest conversion factor to one
                val symbol = unitList.minByOrNull { abs(log(it.convFac, 10.0)).min }?.symbol ?: ""
                return copy(calculatedUnitSymbol = symbol)
            }
        }
        return this
    }

    /**
     * Adds a unit of measurement to the unitSet (returns a new Unit with updated exponent)
     */
    fun addUnitOfMeasurement(unit: UnitOfMeasurement): Unit {
        return copy(
            unitSet = addUnitOfMeasurementToSet(unitSet, unit),
            isLogarithmic = isLogarithmic || unit.isLogarithmic,
            isDifference = isDifference
        )
    }

    /**
     * Reduce redundant Units by removing all units with the exponent 0
     */
    fun reduceRedundantUnits(): Unit {
        val filtered = unitSet.filter { it.exponent != 0 }.toSet()
        return if (filtered.size == unitSet.size) this else copy(unitSet = filtered)
    }

    /** Negates all Exponents of a unit **/
    fun negateExponentsOfUnits(): Unit {
        return copy(unitSet = unitSet.map { it.negateExponent() }.toSet())
    }

    operator fun times(other: Unit): Unit {
        if (this.toString() == "?" || other.toString() == "?") return Unit("?")
        var result = this.unitSet
        for (u in other.unitSet) {
            result = addUnitOfMeasurementToSet(result, u)
        }
        return Unit(
            unitSet = result,
            isLogarithmic = this.isLogarithmic || other.isLogarithmic,
            isDifference = false
        )
    }

    operator fun div(other: Unit): Unit {
        if (this.toString() == "?" || other.toString() == "?") return Unit("?")
        var result = this.unitSet
        for (u in other.unitSet) {
            result = addUnitOfMeasurementToSet(result, u.negateExponent())
        }
        return Unit(
            unitSet = result,
            isLogarithmic = this.isLogarithmic || other.isLogarithmic,
            isDifference = false
        )
    }

    fun sqrt(): Unit {
        if (toString() == "?") return Unit("?")
        val newSet = unitSet.map {
            if (it.exponent % 2 != 0) throw SquareRootError(toString())
            it.copy(exponent = it.exponent / 2)
        }.toSet()
        return Unit(
            unitSet = newSet,
            isLogarithmic = isLogarithmic,
            isDifference = false
        )
    }

    fun sqr(): Unit {
        if (toString() == "?") return Unit("?")
        val newSet = unitSet.map {
            it.copy(exponent = it.exponent * 2)
        }.toSet()
        return Unit(
            unitSet = newSet,
            isLogarithmic = isLogarithmic,
            isDifference = false
        )
    }

    fun pow(exponent: Int): Unit {
        if (exponent == 0) return Unit()
        if (exponent == 1 && !isDifference) return this // units are immutable; a difference must lose its flag
        if (toString() == "?") return Unit("?")
        val newSet = unitSet.map {
            it.copy(exponent = it.exponent * exponent)
        }.toSet()
        return Unit(
            unitSet = newSet,
            isLogarithmic = isLogarithmic,
            isDifference = false
        )
    }

    /**
     * Two units are equal if they have the same SI dimension and the same scale, so N and kg m / s^2 are equal,
     * but m and km (or N and kN) are not. Use [hasSameDimension] or [isCompatibleWith] to ignore the scale.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Unit) return false
        if (unitSet == other.unitSet) return true
        if (!hasSameDimension(other)) return false
        // Temperatures differ by an offset, not only by scale (°C vs K)
        if (singleTemperature != null || other.singleTemperature != null) return false

        return Tolerance(relative = 1e-12).areEqual(scaleToSI(unitSet), scaleToSI(other.unitSet))
    }

    /** Compares the SI dimensions, ignoring prefix, scale and representation: m and km have the same dimension. */
    fun hasSameDimension(other: Unit): Boolean =
        this === other || unitSet == other.unitSet || computeSI(unitSet) == computeSI(other.unitSet)

    /** Checks dimensional compatibility, allowing an unknown unit as a wildcard. */
    fun isCompatibleWith(other: Unit): Boolean =
        hasSameDimension(other) || toString() == "?" || other.toString() == "?"

    /**
     * Returns String representation of a unit as a fraction of units
     */
    override fun toString(): String {
        return formatUnitSet(unitSet)
    }

    /** The temperature unit if this is a single temperature with exponent 1 (K, °C, m°F), otherwise null. */
    internal val singleTemperature: UnitOfTemperature?
        get() = unitSet.singleOrNull()?.tryCast<ThermodynamicTemperature>()?.takeIf { it.exponent == 1 }

    /** Whether this unit can be treated as a plain number or not */
    val isNumber: Boolean get() = when(toString()) {
        "1", "dB", "%", "?" -> true
        else -> false
    }

    /**
     * Adds difference if isDifference is true.
     * Durations and timestamps are told apart by their unit (s, h, ... or DateTime, Date, ...), not by the
     * size of the value: a date before 2020 is still a timestamp, and a long duration is still a duration.
     * @return domain of the unit including difference if needed
     */
    fun effectiveDomain(): String {
        return when {
            // the difference of two timestamps is a duration
            unitDomain == "Timestamp" && isDifference -> "Duration"
            isDifference -> "$unitDomain Difference"
            else -> unitDomain
        }
    }

    /**
     * Transforms Unit to the SI System (does not consider value of Quantity)
     */
    fun toSI() = Unit(
        unitSet = computeSI(unitSet),
        unitDomain = unitDomain,
        calculatedUnitSymbol = calculatedUnitSymbol,
        isDifference = isDifference,
        isLogarithmic = false
    )

    fun siUnitSet() = computeSI(unitSet)

    override fun hashCode(): Int {
        return computeSI(unitSet).hashCode()
    }

    companion object {
        private data class ParsedUnit(
            val unitSet: Set<UnitOfMeasurement>,
            val unitDomain: String,
            val isLogarithmic: Boolean,
            val isDifference: Boolean,
            val unitStr: String
        )

        private fun parseAndValidate(str: String, domain: String): ParsedUnit {
            val r: Reader = StringReader(str)
            val strTok = StreamTokenizer(r)
            strTok.resetSyntax()
            strTok.lowerCaseMode(false) // No conversion into lower case

            //Define the range of a word
            strTok.wordChars('a'.code, 'z'.code)
            strTok.wordChars('A'.code, 'Z'.code)
            strTok.wordChars('.'.code, '.'.code)
            strTok.wordChars('_'.code, '_'.code)
            strTok.wordChars('^'.code, '^'.code)
            strTok.wordChars('-'.code, '-'.code) // negative exponents: m^-2
            strTok.wordChars('0'.code, '9'.code)
            strTok.wordChars('%'.code, '%'.code)
            strTok.wordChars('°'.code, '°'.code)
            strTok.wordChars('µ'.code, 'µ'.code) // micro sign U+00B5; greek mu U+03BC is a word char by default
            strTok.wordChars('?'.code, '?'.code)

            //Define Whitespaces for separation of words
            strTok.whitespaceChars(' '.code, ' '.code)
            strTok.whitespaceChars('\t'.code, '\t'.code)

            //ignore comments
            strTok.slashSlashComments(false)
            strTok.slashStarComments(false)

            var unitSet = emptySet<UnitOfMeasurement>()
            var isLogarithmic = false
            var isDifference = false

            /** start parsing **/
            var token = strTok.nextToken()

            while (token == StreamTokenizer.TT_WORD) {
                if (strTok.sval != "1") { // there are some values in the nominator
                    val resultUnit = splitBaseExponent(strTok.sval)
                    unitSet = addUnitOfMeasurementToSet(unitSet, resultUnit)
                    isLogarithmic = isLogarithmic || resultUnit.isLogarithmic //if isLogarithmic is true once it should stay true
                    token = strTok.nextToken()
                } else
                    token = strTok.nextToken()
            }
            when (token) {
                '/'.code -> { // separates nominator and denominator
                    token = strTok.nextToken()
                }
                StreamTokenizer.TT_EOF, StreamTokenizer.TT_EOL -> { // no denominator
                    // Test if unitSet consists of one unit. In this case isDifference is considered
                    if (unitSet.size == 1)
                        if (unitSet.elementAt(0).isDifference)
                            isDifference = true
                    val unitStr = formatUnitSet(unitSet)
                    return validateDomain(unitSet, domain, isLogarithmic, isDifference, unitStr)
                }
                else -> throw UnknownUnitError("Problem in Unit string in unit $str")
            }
            var denominatorCount = 0
            while (token == StreamTokenizer.TT_WORD) {
                val resultUnit = splitBaseExponent(strTok.sval)
                unitSet = addUnitOfMeasurementToSet(unitSet, resultUnit.negateExponent())
                token = strTok.nextToken()
                denominatorCount++
            }
            if (denominatorCount == 0 || (token != StreamTokenizer.TT_EOF && token != StreamTokenizer.TT_EOL)) {
                throw UnknownUnitError("Problem in Unit string in unit $str")
            }
            val unitStr = formatUnitSet(unitSet)
            return validateDomain(unitSet, domain, isLogarithmic, isDifference, unitStr)
        }

        private fun validateDomain(
            unitSet: Set<UnitOfMeasurement>,
            unitDomain: String,
            isLogarithmic: Boolean,
            isDifference: Boolean,
            unitStr: String
        ): ParsedUnit {
            if (unitDomain.isEmpty()) {
                return ParsedUnit(unitSet, "", isLogarithmic, isDifference, unitStr)
            }
            if (unitSet.isEmpty()) { // unit is not defined
                // unit is not given -> use Unit of given domain (remove whitespaces and ignore case)
                val possibleDomain = ConversionTables.unitsMap.values.filter {
                    it.domain.equals(unitDomain.replace("Value", ""), ignoreCase = true)
                }
                return if (possibleDomain.isNotEmpty()) {
                    // select unit with the closest conversion factor to 1
                    val selectedUnit = possibleDomain.minByOrNull { abs(it.convFac - 1.0).min }!!
                    ParsedUnit(setOf(selectedUnit), unitDomain, isLogarithmic, isDifference, selectedUnit.symbol)
                } else {
                    ParsedUnit(unitSet, "", isLogarithmic, isDifference, unitStr)
                }
            } else { // unit is defined
                val siUnitSet = computeSI(unitSet)
                val possibleUnits = ConversionTables.unitsMap.values.filter { it.baseUnits == siUnitSet }
                // test if the given unit domain is possible (remove whitespaces and ignore case)
                if (possibleUnits.none {
                    it.domain.replace(" ", "").equals(unitDomain.replace("Value", "").replace("3dVector", "").replace("Cartesian", ""), ignoreCase = true) ||
                    it.alternativeDomain.replace(" ", "").equals(unitDomain.replace("Value", "").replace("3dVector", "").replace("Cartesian", ""), ignoreCase = true)
                }) {
                    if (!unitDomain.contains("QuantityValue")) { // if domain is not defined, throw no error (no error in this case)
                        if (unitDomain == "ScalarValues::Real" || unitDomain == "Real")
                            throw UnitDomainError("Units with type ScalarValues::Real are not allowed. Use Type from ISQ Package instead with units (e.g. ISQ::DurationValue, ISQ::LengthValue, Quantities::ScalarQuantityValue ...)")
                        else
                            throw UnitDomainError("Domain $unitDomain not possible for unit ${formatUnitSet(unitSet)}")
                    }
                }
                return ParsedUnit(unitSet, unitDomain, isLogarithmic, isDifference, unitStr)
            }
        }

        private fun splitBaseExponent(str: String): UnitOfMeasurement {
            val delimiter = "^"
            val parts = str.split(delimiter, ignoreCase = true)
            if (parts.size > 2) throw UnknownUnitError("Invalid exponent in unit $str")
            val unitOfMeasurement = isolatePrefix(parts[0])
            val exponent = if (parts.size > 1) {
                parts[1].toIntOrNull() ?: throw UnknownUnitError("Invalid exponent in unit $str")
            } else {
                1
            }
            return unitOfMeasurement.copy(exponent = exponent)
        }

        private fun isolatePrefix(str: String): UnitOfMeasurement {
            for (i in (0..min(2, str.length - 1))) {
                val tempPref = str.substring(0, i)
                val tempBase = str.substring(i)
                if (ConversionTables.prefixes.containsKey(tempPref) && ConversionTables.unitsMap.containsKey(tempBase)) {
                    val baseUnit = ConversionTables.unitsMap[tempBase]!!
                    val prefix = ConversionTables.prefixes[tempPref]!!
                    return baseUnit.copy(prefix = prefix)
                }
            }
            throw UnknownUnitError(str)
        }

        fun formatUnitSet(unitSet: Set<UnitOfMeasurement>): String {
            val numerator = unitSet.filter { it.exponent > 0 }.sortedBy { it.name }
            val denominator = unitSet.filter { it.exponent < 0 }.sortedBy { it.name }
            var numeratorStr = "1"
            if (numerator.isNotEmpty()) {
                numeratorStr = numerator.joinToString(" ") {
                    "${it.prefix.symbol}${it.symbol}${if (it.exponent != 1) "^${it.exponent}" else ""}"
                }
            }
            var denominatorStr = ""
            if (denominator.isNotEmpty()) {
                denominatorStr = denominator.joinToString(" ", " / ") {
                    "${it.prefix.symbol}${it.symbol}${if (it.exponent != -1) "^${-it.exponent}" else ""}"
                }
            }
            return numeratorStr + denominatorStr
        }

        fun<K : Kind> addUnitOfMeasurementToSet(
            units: Collection<UnitOfKind<K>>,
            unit: UnitOfKind<K>
        ): Set<UnitOfKind<K>> {
            val list = units.toMutableList()
            val index = list.indexOfFirst { it.name == unit.name && it.prefix == unit.prefix }
            if (index >= 0) {
                val existing = list[index]
                val newExponent = existing.exponent + unit.exponent
                list[index] = existing.copy(exponent = newExponent)
            } else {
                list.add(unit)
            }
            return list.filter { it.exponent != 0 }.toSet()
        }

        /** Factor to multiply a value in these units with to get the value in the SI units of [computeSI]. */
        private fun scaleToSI(unitSet: Set<UnitOfMeasurement>): RealRange =
            unitSet.fold(RealRange(1.0)) { factor, unit ->
                factor * pow(unit.prefix.factor * unit.convFac, unit.exponent.toDouble()) // int into double
            }

        fun computeSI(unitSet: Set<UnitOfMeasurement>): Set<BaseUnit>
        {
            var resultUnits = emptySet<BaseUnit>()

            for (currentUnit in unitSet) {
                for (baseUnit in currentUnit.baseUnits) {
                    val exponent = currentUnit.exponent * baseUnit.exponent
                    resultUnits = addUnitOfMeasurementToSet(resultUnits, baseUnit.copy(exponent = exponent))
                }
            }
            return resultUnits
        }
    }
}

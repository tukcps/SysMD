package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.derivedUnits.DimensionOne
import java.io.Reader
import java.io.StreamTokenizer
import java.io.StringReader
import kotlin.math.abs
import kotlin.math.log
import kotlin.math.min

class Unit : Cloneable {
    var unitSet = mutableSetOf<UnitOfMeasurement>()
    private var unitStr = "" //String before converting to SI unit
    var unitDomain = "" //domain calculated by calculateUnitDomain()
    var calculatedUnitSymbol = ""  // calculated simplified unitString
    var isLogarithmic = false
    var isDifference = false

    constructor()

    constructor(unitStr: String, unitDomain:String="") {
        this.unitDomain = unitDomain
        parse(unitStr)
        if(unitDomain!="")
            testGivenUnitDomain()
    }

    /**
     * Returns a clone of the unit with new references of objects
     */
    public override fun clone(): Unit {
        val clone = Unit()
        for (i in unitSet) {
            clone.unitSet.add(i.clone())
        }
        clone.unitStr = unitStr
        clone.isLogarithmic = isLogarithmic
        clone.unitDomain = unitDomain
        clone.isDifference = isDifference
        return clone
    }

    /**
     * Parses string to unit Sting must contain defined units with spaces between them.
     * A "/" symbol separates nominator and denominator.
     * The units can have a prefix and an exponent (Example: dm^3).
     */
    private fun parse(str: String) {
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
        strTok.wordChars('0'.code, '9'.code)
        strTok.wordChars('%'.code, '%'.code)
        strTok.wordChars('°'.code, '°'.code)
        strTok.wordChars('?'.code, '?'.code)
        strTok.wordChars('€'.code, '€'.code)

        //Define Whitespaces for separation of words
        strTok.whitespaceChars(' '.code, ' '.code)
        strTok.whitespaceChars('\t'.code, '\t'.code)

        //ignore comments
        strTok.slashSlashComments(false)
        strTok.slashStarComments(false)

        /** start parsing **/
        var token = strTok.nextToken()

        while (token == StreamTokenizer.TT_WORD) {
            if (strTok.sval != "1") { // there are some values in the nominator
                val resultUnit = splitBaseExponent(strTok.sval)
                addUnitOfMeasurement(resultUnit)
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
                unitStr = toString() // Set unitStr to string representation of unit, which is not in SI
                return
            }
            else -> throw UnknownUnitError("Problem in Unit string in unit $str")
        }
        while (token == StreamTokenizer.TT_WORD) {
            val resultUnit = splitBaseExponent(strTok.sval)
            addUnitOfMeasurement(resultUnit.negateExponent())
            token = strTok.nextToken()
        }
        unitStr = toString() // Set unitStr to string representation of unit, which is not in SI
    }

    /**
     * splits Base and Exponent
     * @param str: String of unit with prefix, Unit and exponent
     * @return UnitOfMeasurement
     */
    private fun splitBaseExponent(str: String): UnitOfMeasurement {
        // separate by "^" into parts
        val delimiter = "^"
        val parts = str.split(delimiter, ignoreCase = true)

        //Divides into Prefix, unit value and exponent
        val unitOfMeasurement = isolatePrefix(parts[0])
        if (parts.size > 1) {  // Contains exponent
            unitOfMeasurement.exponent = parts[1].toInt()
        } else {  // Contains no exponent
            unitOfMeasurement.exponent = 1
        }
        return unitOfMeasurement
    }

    /**
     * isolates Prefix from Base Unit
     * @param str: String of unit with prefix and Unit, but without the exponent
     * @return UnitOfMeasurement without exponent
     */
    private fun isolatePrefix(str: String): UnitOfMeasurement {
        // loop over all possible prefix lengths (0,1 and 2)
        for (i in (0..min(2, str.length-1))){ //i: prefix length
            val tempPref = str.substring(0, i)
            val tempBase = str.substring(i)
            if (ConversionTables.prefixes.containsKey(tempPref) && ConversionTables.unitsMap.containsKey(tempBase)) {
                val unitOFMeasurement = ConversionTables.unitsMap[tempBase]!!.clone()
                unitOFMeasurement.prefix = ConversionTables.prefixes[tempPref]!!
                return unitOFMeasurement
            }
        }
        throw IsolationError(str)
    }

    /**
     * Calculate the unit domain of the current unit after converting to SI unit
     * Also the unitSymbol of this domain is saved. It is used by the toString() method
     * If the unit is not a combined unit, the type was already assigned before and is simply returned
     */
    fun calculateUnitDomain(unitSpec: String) {
        if ((unitDomain.isEmpty() || unitSpec.isNotEmpty()) && unitStr != "?") {
            val unitString = if (unitSpec.isNotEmpty()) Unit(unitSpec).unitStr else unitStr
            if (unitSet.isEmpty()) {
                unitDomain = DimensionOne.One.domain
                return
            }
            val unitList = ConversionTables.unitsMap.values.filter { it.getBaseUnits() == unitSet }
            //only return result directly, if unitStr contains the symbol of the compared unit (remove whitespaces first)
            unitList.firstOrNull { unitString.replace(" ", "").contains(it.symbol) }?.let {
                unitDomain = it.domain
                isDifference = isDifference || it.isDifference
                return
            }
            unitList.firstOrNull()?.let {
                unitDomain = it.domain
                isDifference = isDifference || it.isDifference
            }
        }
    }

    /**
     * Calculate the unit symbol of the current unit
     */
    fun calculateUnitSymbol(unitSpec: String) {
        // Simple case only one element in SI unit list, because it is a base unit
        if (calculatedUnitSymbol == "" || unitSpec != "") {
            if (unitSpec != "") { // if unitSpec exists, use this as the unitString (no influence of calculations)
                calculatedUnitSymbol = unitSpec
                return
            }
            if (unitSet.isEmpty()) { // Quantity of domain one
                calculatedUnitSymbol = ""
                return
            }
            //List of possible units for the domains
            val unitList = ConversionTables.unitsMap.values.filter { it.getBaseUnits() == unitSet }

            //if there are units from different (or zero) domains
            //and also different units (or zero), return empty string, because no unique solution possible
            if (unitList.groupBy { it.domain }.size!=1 && unitList.groupBy { it.symbol }.size!=1) {
                calculatedUnitSymbol = ""
                return
            } else {
                //For more than one unit of the same domain use the unit with the closest conversion factor to one
                calculatedUnitSymbol = unitList.minByOrNull { abs(log(it.convFac, 10.0)) }?.symbol ?: ""
            }
        }
    }

    /**
     * Test if the given unit domain is possible and add the base unit to the unitSet
     */
    private fun testGivenUnitDomain() {

        if (unitSet.isEmpty() && unitDomain!="") { //unit is not defined
            // unit is not given -> use Unit of given domain (remove whitespaces and ignore case)
            val possibleDomain = ConversionTables.unitsMap.values.filter { it.domain.equals(unitDomain.replace("Value",""),ignoreCase = true) }
            if(possibleDomain.isNotEmpty()) {
                // select unit with the closest conversion factor to 1
                val selectedUnit = possibleDomain.minByOrNull { abs(it.convFac - 1 )}!!
                unitSet = mutableSetOf(selectedUnit)
                unitStr = selectedUnit.symbol
            } else
                unitDomain = ""
        } else { //unit is defined
            val possibleUnits = ConversionTables.unitsMap.values.filter { it.getBaseUnits() == clone().toSI().unitSet }
            //test, if the given unit domain is possible (remove whitespaces and ignore case)
            if (possibleUnits.none { it.domain.replace(" ", "").equals(unitDomain.replace("Value","").replace("3dVector","").replace("Cartesian",""), ignoreCase = true) ||
                it.alternativeDomain.replace(" ", "").equals(unitDomain.replace("Value","").replace("3dVector","").replace("Cartesian",""), ignoreCase = true) })
                if (!unitDomain.contains("QuantityValue")) { //if domain is not defined, throw no error (no error in this case)
                    if (unitDomain == "ScalarValues::Real" || unitDomain == "Real")
                        throw UnitDomainError("Units with type ScalarValues::Real are not allowed. Use Type from ISQ Package instead with units (e.g. ISQ::DurationValue, ISQ::LengthValue, Quantities::ScalarQuantityValue ...)")
                    else
                        throw UnitDomainError("Domain $unitDomain not possible for unit $this")
                }
        }
    }

    /**
     * Adds a unit of measurement to the unitSet (Changes exponent if unit already in Set)
     */
    fun addUnitOfMeasurement(unit: UnitOfMeasurement) {
        unitSet.find { it.name == unit.name && it.prefix == unit.prefix }?.let {
            it.exponent += unit.exponent
        } ?: unitSet.add(unit)
    }

    /**
     * Reduce redundant Units by removing all units with the exponent 0
     */
    fun reduceRedundantUnits() {
        unitSet.removeAll { it.exponent == 0 }
    }

    /** Negates all Exponents of a unit **/
    fun negateExponentsOfUnits(): MutableSet<UnitOfMeasurement> {
        unitSet.forEach { it.exponent *= -1 }
        return unitSet
    }

    /**
     * compare this with other unit and return false
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Unit) return false
        if (unitSet == other.unitSet) return true
        // Special case for unknown units
        if (toString() == "?" || other.toString() == "?") return true
        return false
    }


    /**
     * Returns String representation of a unit as a fraction of units
     */
   override fun toString(): String {
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

    /**
     * Adds difference if isDifference is true
     * @return domain of the unit including difference if needed
     */
    fun getUnitDomain(value: Double): String {
        //special case for time:
        return when {
            unitDomain == "duration" || unitDomain == "timestamp" -> if (value < 50 * 365 * 24 * 60 * 60) "duration" else "timestamp"
            isDifference -> "$unitDomain Difference"
            else -> unitDomain
        }
    }

    /**
     * Transforms Unit to the SI System (does not consider value of Quantity)
     */
    fun toSI():Unit {
        unitSet.forEach { it.prefix = NoPrefix }
        val resultUnit = clone().apply { unitSet.clear() }
        // Change Unit to SI
        unitSet.forEach { currentUnit ->
            currentUnit.getBaseUnits().forEach {
                //change exponent of derived unit
                resultUnit.addUnitOfMeasurement(it.clone().apply { exponent = currentUnit.exponent * it.exponent })
            }
        }
        resultUnit.reduceRedundantUnits()
        return resultUnit
    }

    override fun hashCode(): Int {
        var result = unitSet.hashCode()
        result = 31 * result + unitStr.hashCode()
        result = 31 * result + unitDomain.hashCode()
        result = 31 * result + isLogarithmic.hashCode()
        result = 31 * result + isDifference.hashCode()
        return result
    }
}
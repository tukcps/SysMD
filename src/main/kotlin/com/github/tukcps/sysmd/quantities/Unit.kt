package com.github.tukcps.sysmd.quantities

import com.github.tukcps.sysmd.quantities.derivedUnits.QuantityOfDimensionOne
import java.io.Reader
import java.io.StreamTokenizer
import java.io.StringReader
import kotlin.math.abs
import kotlin.math.log

class Unit : Cloneable {
    var unitSet = mutableSetOf<UnitOfMeasurement>()
    private var unitStr = "" //String before converting to SI unit
    private var unitDimension = "" //dimension calculated by calculateUnitDimension()
    var calculatedUnitSymbol = ""  // calculated simplified unitString
    var isLogarithmic = false
    var isDifference = false

    constructor()

    constructor(unitStr: String) {
        parse(unitStr)
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
        clone.unitDimension = unitDimension
        clone.isDifference = isDifference
        return clone
    }

    /**
     * Parses sting to unit Sting must contain defined units with spaces between them. A "/" symbol separates nominator and denominator
     * The units can have a prefix and an exponent (Example: dm^3)
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
            else -> throw UnknownUnitError("Problem in Unit string")
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
        for (i in (0..2)){ //i: prefix length
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
     * Calculate the unit dimension of the current unit after converting to SI unit
     * Also the unitSymbol of this dimension is saved. It is used by the toString() method
     * If the unit is not a combined unit, the type was already assigned before and is simply returned
     */
    fun calculateUnitDimension(unitSpec: String) {
        // Simple case only one element in SI unit list, because it is a base unit
        if (unitDimension == "" || unitSpec != "") {
            var unitString = unitStr
            if (unitSpec != "") { // if unitSpec exists, use this as the unitString (no influence of calculations)
                unitString = Unit(unitSpec).unitStr
            }
            if (unitSet.isEmpty()) { // Quantity of dimension one
                unitDimension = QuantityOfDimensionOne.One.dimension
                return
            }
            val unitList = mutableListOf<UnitOfMeasurement>() //List of possible units for the dimension
            //iterate through all units
            for (unitCompared in ConversionTables.unitsMap.values) {
                // iterate through all element in the unitsMap and test if they are the same.
                if (unitCompared.getBaseUnits() == unitSet) {
                    //only return result directly, if unitStr contains the symbol of the compared unit (remove whitespaces first)
                    if (unitString.replace(" ","") .contains(unitCompared.symbol)) {
                        unitDimension = unitCompared.dimension
                        isDifference = isDifference || unitCompared.isDifference
                        return
                    }
                    if (unitList.none { unit -> unit.dimension == unitCompared.dimension && unit.isDifference == unitCompared.isDifference })
                        unitList.add(unitCompared) //only add to list, if there is no unit with the same dimension
                }
            }
            if (unitList.size == 0)
                return
            //Select first element, because there are no more information about the dimension available
            unitDimension = unitList[0].dimension
            isDifference = isDifference || unitList[0].isDifference
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
            if (unitSet.isEmpty()) { // Quantity of dimension one
                calculatedUnitSymbol = ""
                return
            }
            val unitList = mutableListOf<UnitOfMeasurement>() //List of possible units for the dimension
            //iterate through all units
            for (unitCompared in ConversionTables.unitsMap.values) {
                // iterate through all element in the unitsMap and test if they are the same.
                if (unitCompared.getBaseUnits() == unitSet) {
                    unitList.add(unitCompared)
                }
            }
            //if there are units from different (or zero) dimensions
            //and also different units (or zero), return empty string, because no unique solution possible
            if (unitList.groupBy { it.dimension }.size!=1 && unitList.groupBy { it.symbol }.size!=1) {
                calculatedUnitSymbol = ""
                return
            } else {
                //For more than one unit of the same dimension use the unit with the closest conversion factor to one
                unitList.sortBy { abs(log(it.convFac,10.0)) }
                calculatedUnitSymbol = unitList[0].symbol
            }
        }
    }

    /**
     * Adds a unit of measurement to the unitSet (Changes exponent if unit already in Set)
     */
    fun addUnitOfMeasurement(unit: UnitOfMeasurement) {
        if (unitSet.none { it.name == unit.name && it.prefix == unit.prefix })
            unitSet.add(unit)
        else { // Change exponent id Unit already in Set
            unitSet.filter { it.name == unit.name }.elementAt(0).exponent += unit.exponent
        }
    }

    /**
     * Reduce redundant Units by removing all units with the exponent 0
     */
    fun reduceRedundantUnits() {
        unitSet.removeAll { i -> i.exponent == 0 }
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
        if (other !is Unit)
            return false
        return if(toString()=="?" || other.toString()=="?")
            true
        else
            unitSet == other.unitSet
    }


    /**
     * Returns String representation of a unit as a fraction of units
     */
    override fun toString(): String {
        var unitStr = ""
        var i = 0
        //  use the calculatedUnitSymbol if available
        val numerator = unitSet.partition { elem -> elem.exponent > 0 }.first
        val denominator = unitSet.partition { elem -> elem.exponent < 0 }.first
        numerator.sortedBy { elem -> elem.name }
        denominator.sortedBy { elem -> elem.name }
        if (numerator.isNotEmpty()) {
            for (s in numerator) {
                unitStr += s.prefix.symbol
                unitStr += s.symbol
                if (s.exponent != 1) unitStr += "^" + s.exponent
                i++
                if (i != numerator.size) unitStr += " "
            }
        } else {
            unitStr += "1"
        }
        if (denominator.isNotEmpty()) {
            unitStr += " /"
            for (s in denominator) {
                unitStr += " " + s.prefix.symbol
                unitStr += s.symbol
                if (s.exponent != -1) unitStr += "^" + s.exponent * -1
            }
        }
        return unitStr
    }

    /**
     * Adds difference if isDifference is true
     * @return dimension of the unit including difference if needed
     */
    fun getUnitDimension(value: Double): String {
        //special case for time:
        if (unitDimension == "time" || unitDimension == "timestamp") {
            return if (value < 50 * 365 * 24 * 60 * 60) "time" else "timestamp"
        }
        return if (isDifference) "$unitDimension difference" else unitDimension
    }

    /**
     * Transforms Unit to the SI System (does not consider value of Quantity)
     */
    fun toSI():Unit {
        for (element in unitSet) {
            element.prefix = NoPrefix
        }
        val resultUnit = clone()
        resultUnit.unitSet = mutableSetOf() // make resultSet empty
        for (currentUnit in unitSet) {
            // Change Unit to SI
            (currentUnit.getBaseUnits()).forEach {
                val newUnitElement = it.clone()
                //change exponent of derived unit
                newUnitElement.exponent = currentUnit.exponent * it.exponent
                resultUnit.addUnitOfMeasurement(newUnitElement)
            }
        }
        resultUnit.reduceRedundantUnits()
        return resultUnit
    }

    override fun hashCode(): Int {
        var result = unitSet.hashCode()
        result = 31 * result + unitStr.hashCode()
        result = 31 * result + unitDimension.hashCode()
        result = 31 * result + isLogarithmic.hashCode()
        result = 31 * result + isDifference.hashCode()
        return result
    }
}
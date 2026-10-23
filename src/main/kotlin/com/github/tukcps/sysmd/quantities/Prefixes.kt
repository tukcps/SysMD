package com.github.tukcps.sysmd.quantities

import kotlin.math.pow

/**
 * Prefixes of the Units
 */

val NoPrefix = Prefix("NoPrefix", "", 1, 0)
val Yotta = Prefix("yotta", "Y", 10, 24)
val Yobi = Prefix("Yobi", "Yi", 2, 80)
val Zetta = Prefix("zetta", "Z", 10, 21)
val Zebi = Prefix("Zebi", "Zi", 2, 70)
val Exa = Prefix("exa", "E", 10, 18)
val Exbi = Prefix("Exbi", "Ei", 2, 60)
val Peta = Prefix("peta", "P", 10, 15)
val Pebi = Prefix("Pebi", "Pi", 2, 50)
val Tera = Prefix("tera", "T", 10, 12)
val Tebi = Prefix("Tebi", "Ti", 2, 40)
val Giga = Prefix("giga", "G", 10, 9)
val Gibi = Prefix("Gibi", "Gi", 2, 30)
val Mega = Prefix("mega", "M", 10, 6)
val Mebi = Prefix("Mebi", "Mi", 2, 20)
val Kilo = Prefix("kilo", "k", 10, 3)
val Kibi = Prefix("kibi", "Ki", 2, 10)
val Hecto = Prefix("hecto", "h", 10, 2)
val Deka = Prefix("deka", "da", 10, 1)
val Deci = Prefix("deci", "d", 10, -1)
val Centi = Prefix("centi", "c", 10, -2)
val Milli = Prefix("milli", "m", 10, -3)
val Micro = Prefix("micro", "μ", 10, -6)
val Nano = Prefix("nano", "n", 10, -9)
val Pico = Prefix("pico", "p", 10, -12)
val Femto = Prefix("femto", "f", 10, -15)
val Atto = Prefix("atto", "a", 10, -18)
val Zepto = Prefix("zepto", "z", 10, -21)
val Yocto = Prefix("yocto", "y", 10, -24)
package com.github.tukcps.sysmd.quantities

import kotlin.math.pow

/**
 * Prefixes of the Units
 */

object NoPrefix : Prefix("NoPrefix", "", 1.0)
object Yotta : Prefix("yotta", "Y", 10.0.pow(24.0))
object Yobi : Prefix("Yobi", "Yi", 2.0.pow(80.0))
object Zetta : Prefix("zetta", "Z", 10.0.pow(21.0))
object Zebi : Prefix("Zebi", "Zi", 2.0.pow(70.0))
object Exa : Prefix("exa", "E", 10.0.pow(18.0))
object Exbi : Prefix("Exbi", "Ei", 2.0.pow(60.0))
object Peta : Prefix("peta", "P", 10.0.pow(15.0))
object Pebi : Prefix("Pebi", "Pi", 2.0.pow(50.0))
object Tera : Prefix("tera", "T", 10.0.pow(12.0))
object Tebi : Prefix("Tebi", "Ti", 2.0.pow(40.0))
object Giga : Prefix("giga", "G", 10.0.pow(9.0))
object Gibi : Prefix("Gibi", "Gi", 2.0.pow(30.0))
object Mega : Prefix("mega", "M", 10.0.pow(6.0))
object Mebi : Prefix("Mebi", "Mi", 2.0.pow(20.0))
object Kilo : Prefix("kilo", "k", 10.0.pow(3.0))
object Kibi : Prefix("kibi", "Ki", 2.0.pow(10.0))
object Hecto : Prefix("hecto", "h", 10.0.pow(2.0))
object Deka : Prefix("deka", "da", 10.0.pow(1.0))
object Deci : Prefix("deci", "d", 10.0.pow(-1.0))
object Centi : Prefix("centi", "c", 10.0.pow(-2.0))
object Milli : Prefix("milli", "m", 10.0.pow(-3.0))
object Micro : Prefix("micro", "μ", 10.0.pow(-6.0))
object Nano : Prefix("nano", "n", 10.0.pow(-9.0))
object Pico : Prefix("pico", "p", 10.0.pow(-12.0))
object Femto : Prefix("femto", "f", 10.0.pow(-15.0))
object Atto : Prefix("atto", "a", 10.0.pow(-18.0))
object Zepto : Prefix("zepto", "z", 10.0.pow(-21.0))
object Yocto : Prefix("yocto", "y", 10.0.pow(-24.0))
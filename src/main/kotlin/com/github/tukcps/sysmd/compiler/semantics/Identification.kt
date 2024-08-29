package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.compiler.parser.SimpleName


/**
 * An identification of an element that consists of a short name and a long name.
 * @param shortName short name or null
 * @param name long name or null
 */
class Identification(
    var shortName:  SimpleName? = null,    // KerML: Abbreviation of name
    var name:       SimpleName? = null          // KerML: Name
): Cloneable {

    constructor(element: Element): this(element.declaredShortName, element.declaredName)

    override fun toString(): String =
                (if (shortName!= null) "shortName: $shortName" else "")+
                (if (name != null) " name: $name" else "")

    fun toName(): String = shortName?:name?:"(no name)"

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is Identification) return false
        if (other.shortName == this.shortName && this.shortName != null) return true
        return other.name == this.name && this.name != null
    }

    public override fun clone(): Identification {
        return Identification(shortName, name)
    }

    override fun hashCode(): Int {
        var result = shortName?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
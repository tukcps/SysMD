package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName


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

    override fun equals(other: Any?) = other is Identification && (
        (shortName !== null && shortName == other.shortName) ||
        (name !== null && name == other.name) ||
        (shortName === null && name === null && other.name === null && other.shortName === null)
    )


    public override fun clone(): Identification {
        return Identification(shortName, name)
    }

    override fun hashCode() = 1
}
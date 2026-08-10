package com.github.tukcps.sysmd.compiler.parser.util

import com.github.tukcps.sysmd.model.generated.ElementHierarchy
import com.github.tukcps.sysmd.model.generated.ElementType
import java.util.*

// TODO: generate this as a constant during codegen
val transitiveSupertypes : Map<ElementType, Set<ElementType>> = EnumMap<ElementType, EnumSet<ElementType>>(ElementType::class.java).apply {
    for((k,v) in ElementHierarchy.directSuperTypes)
        this[k] = if(v.isEmpty()) EnumSet.noneOf(ElementType::class.java) else EnumSet.copyOf(v)

    do {
        var delta = false

        for((specific, xs) in ElementHierarchy.directSuperTypes) {
            val set = this[specific]!!

            for(general in xs) {
                set.addAll(this[general]!!).also {
                    if(it) delta = true
                }
            }
        }
    } while(delta)
}

/** @return Whether [this] is a direct or transient subtype of [general].
 *          Equivalent types are considered subtypes of another.
 */
infix fun ElementType.isSubtypeOf(general : ElementType): Boolean = this == general || general in transitiveSupertypes[this]!!
infix fun ElementType.isSupertypeOf(specific : ElementType): Boolean = specific isSubtypeOf this

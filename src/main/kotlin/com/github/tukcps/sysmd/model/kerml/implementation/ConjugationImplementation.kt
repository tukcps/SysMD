package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Conjugation
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.SimpleName

class ConjugationImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null
): Conjugation, RelationshipImplementation(
    declaredName,
    declaredShortName
) {
    @Suppress("UNCHECKED_CAST")
    override val conjugated: Resolved<Type>?
        get() = source.firstOrNull() as Resolved<Type>?

    @Suppress("UNCHECKED_CAST")
    override val type: Resolved<Type>?
        get() = target.firstOrNull() as Resolved<Type>?

}
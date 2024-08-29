package com.github.tukcps.sysmd.model.kerml.implementation

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*


class PackageImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElements: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    isLibraryElement: Boolean = false,
    isStandard: Boolean = false,
    elementType: String = "Package"
): Package, NamespaceImplementation(
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    ownedElement = ownedElements,
    owner = owner,
    elementType = elementType
) {
    init {
        this.isStandard = isStandard
        this.isLibraryElement = isLibraryElement
        if (isStandard || isLibraryElement)
            this.elementId = Generators.nameBasedGenerator().generate(qualifiedName)
    }

    override fun clone(): Package {
        return PackageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            owner = Resolved(owner),
            ownedElements = Resolved.copyOfIdentityList(ownedElement),
            isStandard = isStandard,
            isLibraryElement = isLibraryElement
        ).also { klon ->
            klon.model = model
        }
    }

    override fun toString(): String {
        return "Package {" +
                (if(declaredName != null) "name='$declaredName', " else "") +
                (if(declaredShortName != null) "shortName='$declaredShortName', " else "") +
                "owner='${owner.ref?.qualifiedName}', " +
                "#owned=${ownedElement.size}, " +
                "#imports=${imports.size}, +" +
                "id='${elementId}')"
    }

}

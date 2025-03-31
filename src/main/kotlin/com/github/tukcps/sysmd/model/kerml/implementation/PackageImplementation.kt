package com.github.tukcps.sysmd.model.kerml.implementation

import com.fasterxml.uuid.Generators
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*


class PackageImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    isLibraryElement: Boolean = false,
    isStandard: Boolean = false,
    elementType: String = "Package"
): Package, NamespaceImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    elementType = elementType
) {
    init {
        this.isStandard = (owner.ref?.isStandard == true) or isStandard
        this.isLibraryElement = (owner.ref?.isStandard == true) or isLibraryElement
        if (isStandard || isLibraryElement)
            this.elementId = Generators.nameBasedGenerator().generate(qualifiedName)
    }

    override fun clone(): Package {
        return PackageImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
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

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.LibraryPackage
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

class LibraryPackageImplementation(model : Session,elementId : Uuid = Uuid.random()) : LibraryPackage, PackageImplementation(model,elementId = elementId)
{
    override var isStandard: Boolean = false
    override fun libraryNamespace(): Namespace? = null
    
}

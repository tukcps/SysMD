package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Import
import com.github.tukcps.sysmd.model.kerml.MembershipImport
import com.github.tukcps.sysmd.model.kerml.UnresolvedElement

abstract class ImportImplementation(

    elementType: String
): Import, RelationshipImplementation(
    owningRelatedElement = UnresolvedElement(),
    elementType = elementType
) {
    override var visibility: Import.VisibilityKind = Import.VisibilityKind.Private
    override var isRecursive: Boolean = false   // False by default in SysMLv2
    override var isImportAll: Boolean = false
    override fun updateFrom(template: Element) {
        super.updateFrom(template)
        if (template is MembershipImport) {
            visibility = template.visibility
            isImportAll = template.isImportAll
            isRecursive = template.isRecursive
        }
    }
}
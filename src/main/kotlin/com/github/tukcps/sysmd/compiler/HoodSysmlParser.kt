package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionImplementation

typealias KerMLPackage = com.github.tukcps.sysmd.model.kerml.Package
class HoodSysmlParser {
    fun parseString(sysmlText: String): Session {
        val model = SessionImplementation()
        val parser = SysMLv2(model)
        parser.parse(sysmlText)
        model.initialize(5)
        return model
    }

    fun getTopLevelPackage(model: Session, packageName: String): KerMLPackage? =
        model.global.resolve(packageName)?.member()

    fun getAttributeDefinitions(owner: Namespace): List<AttributeDefinition>  =
        owner.visibleMemberships().mapNotNull { it.member() }

    fun getActionUsages(owner: Namespace): List<ActionUsage> =
        owner.visibleMemberships().mapNotNull { it.member() }

    fun getOwner(it: Element) = it.owner
}
package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.getOwnedElementsOfType
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.model.sysml.AttributeDefinition
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionImplementation

typealias KerMLPackage = com.github.tukcps.sysmd.model.kerml.Package
class HoodSysmlParser {
    fun parseString(sysmlText: String): Session {
        val model = SessionImplementation()
        val input = TextualRepresentationImplementation(body = sysmlText, language = "SysML")
        val parser = SysMLv2(model)
        parser.parse(input)
        model.initialize(5)
        // model.propagate()
        return model
    }

    fun getTopLevelPackage(model: Session, packageName: String): KerMLPackage? =
        model.global.resolve<KerMLPackage>(packageName)

    fun getAttributeDefinitions(owner: Element): List<AttributeDefinition>  =
        owner.getOwnedElementsOfType<AttributeDefinition>()

    fun getActionUsages(owner: Element): List<ActionUsage> =
        owner.getOwnedElementsOfType<ActionUsage>()

    fun getOwner(it: Element) = it.owner.ref
}
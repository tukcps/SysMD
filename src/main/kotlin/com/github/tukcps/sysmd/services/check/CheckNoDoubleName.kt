package com.github.tukcps.sysmd.services.check

import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.sysml.InterfaceUsage
import com.github.tukcps.sysmd.services.session.Session


/**
 * Quick check whether duplicate names (that are allowed) exist in namespaces.
 */
fun Session.reportDoubleNamesInNamespace() {
    val namespaces = get().filterIsInstance<Namespace>()

    namespaces.forEach { namespace ->
        if(namespace is InterfaceUsage)
            // TODO: check of these are parsed right, they own their members rather than referencing?!
            return@forEach

        namespace.membership.flatMap { m ->
            // fixme: per standard memberName should be overwritten directly
            listOfNotNull( m.name, m.shortName ).ifEmpty {
                listOfNotNull(m.memberName, m.memberShortName)
            }.map {
                it to m
            }
        }.groupBy(
            { it.first }, { it.second }
        ).forEach { (name, members) ->
            if(members.size > 1)
            {
                status.warn(
                    message = "Duplicate element ${namespace.path()}::$name is present ${members.size} times",
                    element = members.first().toElementData()
                )
            }
        }
    }
}
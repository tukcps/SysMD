package com.github.tukcps.sysmd.compiler.parser.util

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.OwningMembership
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


fun Session.toIndentedString(
    element: Element,
    indent: String = "",
    isLast: Boolean = true
): String {
    val sb = StringBuilder()

    sb.append(indent)
    sb.append(if (isLast) "└─ " else "├─ ")
    sb.append(element.toString())
    sb.append('\n')

    val nextIndent = indent + if (isLast) "   " else "│  "

    if (element is OwningMembership) {
        repeat(element.target.size) { i ->
            sb.append(
                this.toIndentedString(
                    element.target[i],
                    nextIndent,
                    i == element.target.size - 1
                )
            )
        }
    } else
        repeat(element.ownedRelationship.size) { i ->
            sb.append(
                this.toIndentedString(
                    element.ownedRelationship[i],
                    nextIndent,
                    i == element.ownedRelationship.size - 1
                )
            )
        }

    return sb.toString()
}

fun List<ElementData>.toIndentedString(): String = DataModelWithEdges(this).toString()
fun List<ElementData>.toIndentedString(from : Uuid): String = buildString {
    DataModelWithEdges(this@toIndentedString).print(from, this@buildString)
}
fun List<ElementData>.toIndentedString(from : ElementData): String = toIndentedString(from.elementId)

package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Comment
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*


open class CommentImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    ownedElements: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    body: String = "",
    override var locale: String? = null,
    elementType: String = "Comment"
): Comment, AnnotatingElementImplementation(
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    owner = owner,
    ownedElement = ownedElements,
    body = body,
    elementType = elementType
) {
    override fun clone(): Comment {
        return CommentImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            ownedElements = Resolved.copyOfIdentityList(ownedElement),
            owner = Resolved(owner),
            body = body,
            locale = locale
        ).also {
            it.model = model
        }
    }
    override fun toString(): String =
        "Comment {id='${elementId}', name='$declaredName', body=$body}"
}

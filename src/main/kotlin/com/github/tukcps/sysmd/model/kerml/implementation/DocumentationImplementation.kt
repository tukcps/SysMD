package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Documentation
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.parser.SimpleName
import java.util.*


/**
 * Documentation is an Annotation whose annotatingElement is a Comment that provides
 * documentation of the annotatedElement. Documentation is always an ownedRelationship
 * of its annotatedElement.
 * @param body the documentation as a string
 */
class DocumentationImplementation(
    elementId: UUID = UUID.randomUUID(),
    declaredName: String? = null,
    declaredShortName: SimpleName? = null,
    ownedElement: MutableList<Resolved<Element>> = mutableListOf(),
    owner: Resolved<Element> = Resolved(),
    body: String = "",
    elementType: String = "Documentation"
): Documentation, CommentImplementation(
    elementId=elementId,
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    ownedElements=ownedElement,
    owner=owner,
    body = body,
    elementType = elementType
) {

    override fun clone(): Documentation {
        return DocumentationImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
            ownedElement=Resolved.copyOfIdentityList(ownedElement),
            owner=Resolved(owner),
            body=body).also {
                model=it.model
        }
    }
}
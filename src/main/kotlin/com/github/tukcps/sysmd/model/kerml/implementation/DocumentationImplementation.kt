package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Documentation
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid


/**
 * Documentation is an Annotation whose annotatingElement is a Comment that provides
 * documentation of the annotatedElement. 
 * @param body the documentation as a string
 */
class DocumentationImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: String? = null,
    declaredShortName: SimpleName? = null,
    body: String = "",
): Documentation, CommentImplementation(
    model,
    elementId = elementId,
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    body = body,
) {

    override fun clone(): Documentation = DocumentationImplementation(
        model,
        declaredName = declaredName,
        declaredShortName = declaredShortName,
        body = body
    )
}
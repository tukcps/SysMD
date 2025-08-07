package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Documentation
import com.github.tukcps.sysmd.model.util.SimpleName


/**
 * Documentation is an Annotation whose annotatingElement is a Comment that provides
 * documentation of the annotatedElement. 
 * @param body the documentation as a string
 */
class DocumentationImplementation(
    declaredName: String? = null,
    declaredShortName: SimpleName? = null,
    body: String = "",
    elementType: String = "Documentation"
): Documentation, CommentImplementation(
    declaredName=declaredName,
    declaredShortName = declaredShortName,
    body = body,
    elementType = elementType
) {

    override fun clone(): Documentation {
        return DocumentationImplementation(
            declaredName=declaredName,
            declaredShortName=declaredShortName,
            body=body).also {
                model=it.model
        }
    }
}
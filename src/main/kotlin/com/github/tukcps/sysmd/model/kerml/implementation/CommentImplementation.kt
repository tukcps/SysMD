package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Comment
import com.github.tukcps.sysmd.model.util.SimpleName
import java.util.*


open class CommentImplementation(
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    body: String = "",
    override var locale: String? = null,
    elementType: String = "Comment"
): Comment, AnnotatingElementImplementation(
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    body = body,
    elementType = elementType
) {
    override fun clone(): Comment {
        return CommentImplementation(
            declaredName = declaredName,
            declaredShortName = declaredShortName,
            body = body,
            locale = locale
        ).also {
            it.model = model
        }
    }
    override fun toString(): String =
        "Comment { name='$declaredName', body=$body}"
}

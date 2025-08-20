package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Comment
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName

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
    override fun clone(): Comment = CommentImplementation()
        .also { klon -> klon.updateFrom(this) }

    override fun updateFrom(template: Element) {
        if (template is Comment)
            locale = template.locale
        super.updateFrom(template)
    }
}

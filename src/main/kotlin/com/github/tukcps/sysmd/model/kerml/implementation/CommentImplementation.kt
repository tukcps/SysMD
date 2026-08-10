package com.github.tukcps.sysmd.model.kerml.implementation

import com.github.tukcps.sysmd.model.kerml.Comment
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

open class CommentImplementation(
    model : Session,
    elementId : Uuid = Uuid.random(),
    declaredName: SimpleName? = null,
    declaredShortName: SimpleName? = null,
    body: String = "",
    override var locale: String? = null,
): Comment, AnnotatingElementImplementation(
    model,
    elementId = elementId,
    declaredName = declaredName,
    declaredShortName = declaredShortName,
    body = body,
) {
    override fun clone(): Comment = CommentImplementation(model).also { klon ->
        klon.updateFrom(this)
    }

    override fun updateFrom(template: Element) {
        if (template is Comment)
            locale = template.locale
        super.updateFrom(template)
    }
}

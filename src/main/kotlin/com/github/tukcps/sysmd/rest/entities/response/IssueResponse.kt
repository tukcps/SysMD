package com.github.tukcps.sysmd.rest.entities.response

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.Session


/**
 * An issue response is used to represent the data of an Issue in a REST response.
 */
data class IssueResponse(
    var kind: Issue.Kind? = null,
    val message: String? = null,
    val line: Int?=null,
    val token: String? = null,
    val elementId: String?=null,
    val inputHash: String?=null,
){
    constructor(issue: Issue, session: Session?=null) : this(
        kind = issue.kind,
        message = issue.message,
        line = issue.token?.lineNo,
        token = issue.token?.string,
        elementId = session?.global?.resolve<Element>(issue.elementPath?:"")?.elementId.toString(),
        inputHash = hashBase64UrlSafe(issue.input.toString())
    )
}
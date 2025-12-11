package com.github.tukcps.sysmd.rest.entities.response

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.session.Session


/**
 * An issue response is used to represent the data of an Issue in a REST response.
 * @param kind the kind of the issue
 * @param message a shore textual description
 * @param line the line number in the related input (given by its hash)
 * @param token only for syntax errors, the token that could not be parsed
 * @param inputHash hash of the input of the compiler, allows identification of the input
 * @param indices indices in the input string, allows finding related text range in input
 */
data class IssueResponse(
    var kind: Issue.Kind? = null,
    val message: String? = null,
    val line: Int?=null,
    val token: String? = null,
    val elementId: String?=null,
    val inputHash: String?=null,
    val indices: IntRange?=null,
){
    constructor(issue: Issue, session: Session?=null) : this(
        kind = issue.kind,
        message = issue.message,
        line = issue.line(),
        token = issue.token?.string,
        elementId = session?.global?.resolve(issue.elementPath?:"")?.memberElement?.elementId.toString(),
        inputHash = hashBase64UrlSafe(issue.input.toString()),
        indices = issue.indices
    )
}
package util

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.session.Session
import kotlin.test.assertTrue


fun KerML.assertNoIssues(
    filter: (issue: Issue) -> Boolean = { true }
) {
    val issues = status.issues.filter(filter)

    assertTrue(
        issues.isEmpty(),
        buildString {
            appendLine("Expected 0 issues, but session has ${issues.size} issues:")
            issues.forEachIndexed { i, issue ->
                appendLine("[$i], line ${issue.line()}: $issue")
            }
        }
    )
}

fun Session.assertNoIssues(
    filter: (issue: Issue) -> Boolean = { true }
) {
    val issues = status.issues.filter(filter)

    assertTrue(
        issues.isEmpty(),
        buildString {
            appendLine("Expected 0 issues, but session has ${issues.size} issues:")
            issues.forEachIndexed { i, issue ->
                appendLine("[$i], line ${issue.line()}: $issue")
            }
        }
    )
}

fun Session.assertIssue(messageSubstring: String, message: String? = null) {
    val found = status.issues.any { it.message.contains(messageSubstring) }
    assertTrue(found, message ?: buildString {
        append("Expected issue with substring '$messageSubstring', but not found. Instead got")

        if(status.issues.isEmpty())
            append(" no issues")
        else
            appendLine(":")

        status.issues.forEachIndexed { i, issue ->
            appendLine("[$i], line ${issue.line()}: $issue")
        }
    })
}
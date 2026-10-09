package util

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.session.Session
import org.junit.jupiter.api.Assertions.assertTrue
import org.opentest4j.AssertionFailedError

fun assertEmpty(issues : Collection<Issue>)
{
    if(issues.isNotEmpty())
    {
        throw AssertionFailedError(buildString {
            append("Expected 0 issues, but session has ").append(issues.size).appendLine(" issues:")
            issues.forEachIndexed { i, issue ->
                append("[").append(i).append("], line ").append(issue.line()).append(": ").appendLine(issue)
                issue.cause?.stackTraceToString()?.lineSequence()?.forEach { line ->
                    append('\t').appendLine(line)
                }
            }
        })
    }
}

inline fun KerML.assertNoIssues(
    filter: (issue: Issue) -> Boolean = { true }
) {
    assertEmpty(status.issues.filter(filter))
}

inline fun Session.assertNoIssues(
    filter: (issue: Issue) -> Boolean = { true }
) {
    assertEmpty(status.issues.filter(filter))
}

fun Session.assertIssue(messageSubstring: String, message: String? = null) {
    val found = status.issues.any { it.message.contains(messageSubstring) }
    assertTrue(found) {
        message ?: buildString {
            append("Expected issue with substring '$messageSubstring', but not found. Instead got")

            if(status.issues.isEmpty())
                append(" no issues")
            else
                appendLine(":")

            status.issues.forEachIndexed { i, issue ->
                appendLine("[$i], line ${issue.line()}: $issue")
            }
        }
    }
}
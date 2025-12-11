package util

import com.github.tukcps.sysmd.services.session.Session
import kotlin.test.assertTrue


fun Session.assertNoIssues() {
    var message = ""
    status.issues.forEachIndexed { i, m ->  message += " [$i], line ${status.issues.elementAt(i).line()}: $m\n" }
    assertTrue(status.issues.isEmpty(), "Expected 0 issues, but session has ${status.issues.size} issues: \n$message")
}

fun Session.assertIssue(messageSubstring: String, message: String? = null) {
    val found = status.issues.any { it.message.contains(messageSubstring) }
    assertTrue(found, message?:"Expected issue with substring '$messageSubstring', but not found.")
}
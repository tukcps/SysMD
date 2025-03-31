package com.github.tukcps.sysmd.compiler.parser.util

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.services.session.reportInfo

/**
 * A production rule that reports that a statement is not supported.
 * It consumes tokens until a semicolon has been consumed.
 */
fun KerML.Unsupported() {
    model.reportInfo(semantics.namespace, "Not yet implemented: $token")
    noOrMore(stop = Token.Kind.SEMICOLON) {
        consume()
    }
    consume()
}
@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.util

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token

/**
 * A production rule that reports that a statement is not supported.
 * It consumes tokens until a semicolon has been consumed.
 */
fun KerML.Unsupported(message: String? = null) {
    model.status.info(message?:"Not yet implemented: $token", this, semantics.namespace)
    noOrMore(stop = Token.Kind.SEMICOLON) {
        consume()
    }
    consume()
}
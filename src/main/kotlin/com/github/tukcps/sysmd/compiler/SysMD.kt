package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.compiler.parser.sysmd.Triple
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.EOF
import com.github.tukcps.sysmd.services.session.Session


class SysMD(
    model: Session,                                 // model in which the results will be returned.
                                                    // the textual representation, in which parsing is done.
    indices: IntRange? = null,                      // allows us to select a subset to be parsed, i.e., an expression.
    generateAnnotations: Boolean = false
) : KerML(model, indices, generateAnnotations) {
    override fun parse() {

        noOrMore(stop = EOF) {
            Triple()
        }

    }
}

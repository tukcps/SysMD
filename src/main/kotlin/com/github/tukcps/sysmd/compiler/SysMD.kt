package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.compiler.parser.sysmd.Triple
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.EOF
import com.github.tukcps.sysmd.services.session.Session


/**
 * A SysMD Parser.
 * @param model model in which the results will be returned.
 */
class SysMD(
    model: Session
) : KerML(model) {
    override fun parse() {
        noOrMore(stop = EOF) {
            Triple()
        }
    }
}

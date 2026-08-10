package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.compiler.parser.sysmd.Triple
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.EOF
import com.github.tukcps.sysmd.services.session.Session

/**
 * A SysMD Parser.
 * For interactive modifications in existing model.
 * @param model model in which the results will be returned.
 */
class SysMD(
    model: Session
) : KerML(model, model.status, model.settings, keywords = Token.sysMDKeywords) {

    override fun parse() {
        noOrMore(stop = EOF) {
            Triple()
        }
    }
}

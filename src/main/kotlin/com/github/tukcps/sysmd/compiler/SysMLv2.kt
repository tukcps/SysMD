@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler

import com.github.tukcps.sysmd.compiler.parser.sysmlv2.PackageBodyElement
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.EOF
import com.github.tukcps.sysmd.compiler.semantics.sysmlv2.SysMLv2Semantics
import com.github.tukcps.sysmd.services.session.Session

class SysMLv2(
    model: Session                                 // model in which the results will be returned.
) : KerML(model) {

    // A class that implements the semantic actions of SysML v2 productions
    var sysMLSemantics = SysMLv2Semantics(semantics)

    override fun parse() {

        try {
            noOrMore(stop = EOF) {
                PackageBodyElement()
            }
        } catch(exception: Exception) {
            handleError(exception)
        }
    }
}
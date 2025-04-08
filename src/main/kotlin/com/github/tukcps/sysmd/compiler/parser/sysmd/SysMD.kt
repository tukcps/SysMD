@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmd

import com.github.tukcps.sysmd.compiler.SysMD
import com.github.tukcps.sysmd.compiler.parser.kerml.Association
import com.github.tukcps.sysmd.compiler.parser.kerml.Class
import com.github.tukcps.sysmd.compiler.parser.kerml.ElementList
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.ASSOC
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.CLASS
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DATATYPE
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEF
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DEFINES
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.DOT
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.HAS_A
import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.kerml.Resolved


/**
 * SysMD productions lean towards triples that give additional facts on existing elements.
 * The existing element is the subject and is identified by its qualified name.
 *
 *     Triple :-
 *            QualifiedName hasA                FeatureList
 *          | QualifiedName imports             QualifiedNameList
 *          | QualifiedName defines             DefinitionList
 *
 * For implementation, we consider the special relations IS_A, HAS_A, IMPORTS, DEFINES separately.
 */
fun SysMD.Triple() {

    QualifiedName().also {
        semantics.pushOwner(Resolved(it))
    }

    alternatives {
        HAS_A then { ElementList() }
        DEFINES then { DefinitionList() }
        others {
            semantics.initOwners("Global")
            throw SyntaxError(this@Triple,
                "Expecting a SysMD triple (isA, hasA, uses, imports, defines, user-defined, but read $consumedToken"
            )
        }
    }
    semantics.popOwner()
}


/**
 *      DefinitionList :- (Definition)*
 *      Definition :- Class | Association
 */
private fun SysMD.DefinitionList() {
    noOrMore(end = { consumedToken.kind == DOT }) {
        alternatives {
            DEF starts { Class() }
            CLASS starts { Class() }
            DATATYPE starts { Class() }
            ASSOC starts { Association() }
            others { Class() }
        }
    }
}
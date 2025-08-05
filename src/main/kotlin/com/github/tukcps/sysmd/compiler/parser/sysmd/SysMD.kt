@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmd

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMD
import com.github.tukcps.sysmd.compiler.parser.kerml.Association
import com.github.tukcps.sysmd.compiler.parser.kerml.Class
import com.github.tukcps.sysmd.compiler.parser.kerml.NamespaceBodyElement
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.exceptions.SyntaxError


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
    val owners = semantics.ownerName()

    QualifiedName().also {
        semantics.addOwningNamespaces(it.removePrefix("Global"))
    }

    alternatives {
        HAS_A then { ElementList() }
        DEFINES then { DefinitionList() }
        others {
            semantics.initOwningNamespaces("Global")
            throw SyntaxError(this@Triple,
                "Expecting a SysMD triple with isA, hasA - but read $consumedToken"
            )
        }
    }

    semantics.initOwningNamespaces(owners)
}


/**
 *      ElementList: Element+
 *       Deprecated (SysMD legacy): Also, an Element ending with a dot shall stop the list
 */
fun KerML.ElementList() {
    oneOrMore(stop = { consumedToken.kind == DOT || token.kind == RCURBRACE || token.kind == EOF }) {
        NamespaceBodyElement()
    }
}


/**
 *      DefinitionList: Definition*
 *      Definition: Class | Association
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
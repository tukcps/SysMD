@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.exceptions.throwSyntaxError

/**
 * 8.2.2.6 Definition and Usage Textual Notation
 * 8.2.2.6.1 Definitions
 *
 * Definition = DefinitionDeclaration DefinitionBody
 */
internal fun SysMLv2.Definition() {
    DefinitionDeclaration()
    DefinitionBody()
}


/**
 *      DefinitionBody = ';' | '{' DefinitionBodyItem* '}'
 */
internal fun SysMLv2.DefinitionBody() {
    alternatives {
        LCURBRACE then {
            noOrMore(end = { token.kind == RCURBRACE }) {
                DefinitionBodyItem()
            }
            RCURBRACE.consume()
        }
        SEMICOLON then { }
    }
}

/**
 *      DefinitionBodyItem =
 *            DefinitionMember
 *          | VariantUsageMember
 *          | NonOccurrenceUsageMember
 *          | ( SourceSuccessionMember )? OccurrenceUsageMember
 *          | AliasMember
 *          | Import
 */
internal fun SysMLv2.DefinitionBodyItem() {
    PUBLIC.optional                         { semantics.prefixes.add(PUBLIC)  }
    PRIVATE.optional                        { semantics.prefixes.add(PRIVATE) }
    PROTECTED.optional                      { semantics.prefixes.add(PROTECTED) }
    FeaturePrefix()
    when {
        definitionElementStarts()           -> { DefinitionElement() }
        nonOccurrenceUsageStarts()          -> { NonOccurrenceUsageElement() }
        THEN.starts()                       -> { SourceSuccessionMember(); OccurrenceUsageElement()}
        occurrenceUsageStart.starts()       -> { OccurrenceUsageElement() }
        ALIAS.starts()                      -> { AliasMember()}
        IMPORT.starts()                     -> { Import() }
        else                                -> { throwSyntaxError("Error while processing definition body item") }
    }
    semantics.prefixes.clear()
}

fun SysMLv2.DefinitionDeclaration() {
    Identification().also { semantics.create(it) }
    optional(start = SPECIALIZES or DPGT) {
        SubclassificationPart()
    }
}

/**
 *      SubclassificationPart = SPECIALIZES OwnedSubclassification
 *          (',' OwnedSubclassification)*
 *      OwnedSubclassification = QualifiedName
 */
fun SysMLv2.SubclassificationPart() {
    SPECIALIZES()
    QualifiedName().also { semantics.addSubclassification(it) }
    noOrMore(COMMA) {
        COMMA.consume()
        QualifiedName() .also { semantics.addSubclassification(it) }
    }
}
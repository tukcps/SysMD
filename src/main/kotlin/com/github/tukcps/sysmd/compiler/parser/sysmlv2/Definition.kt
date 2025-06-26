@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.AliasMember
import com.github.tukcps.sysmd.compiler.parser.kerml.FeaturePrefix
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.Import
import com.github.tukcps.sysmd.compiler.parser.kerml.SPECIALIZES
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.TypeActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.Type

/**
 * 8.2.2.6 Definition and Usage Textual Notation
 * 8.2.2.6.1 Definitions
 *
 * Definition = DefinitionDeclaration DefinitionBody
 */
internal fun SysMLv2.Definition(typeActions: TypeActions<Type>) {
    DefinitionDeclaration(typeActions)
    DefinitionBody(Resolved(typeActions.created!!))
}


/**
 *      DefinitionBody = ';' | '{' DefinitionBodyItem* '}'
 */
internal fun SysMLv2.DefinitionBody(owner: Resolved<Element>) {
    alternatives {
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(end = { token.kind == RCURBRACE }) {
                DefinitionBodyItem()
            }
            semantics.popOwner()
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

fun SysMLv2.DefinitionDeclaration(klass: TypeActions<Type>) {
    Identification().also { klass.create(it) }
    optional(start = SPECIALIZES or DPGT) {
        SubclassificationPart(klass)
    }
}

/**
 *      SubclassificationPart = SPECIALIZES OwnedSubclassification
 *          (',' OwnedSubclassification)*
 *      OwnedSubclassification = QualifiedName
 */
fun KerML.SubclassificationPart(klass: TypeActions<Type>) {
    SPECIALIZES()
    QualifiedNameList().also { klass.addSpecialization(it)}
}
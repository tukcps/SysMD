@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Body
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*


/**
 * In this file we collect parser production implementations for
 * - part usage and definition
 * - port usage and definition
 */



fun KerML.PortDefinition() {
    val portDefinition = sysMLSemantics.PortDefinitionSemantics()
    PORT.consume()
    DEF.consume()
    Identification().also { portDefinition.identification = it }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also { portDefinition.specialization = it }
    }
    portDefinition.create()
    DefinitionBody(Resolved(portDefinition.identification.name?:portDefinition.identification.shortName!!))
}


fun KerML.PortUsage() {
    val portUsage = sysMLSemantics.PortUsageSemantics()
    PORT.consume()
    Identification().also { portUsage.identification = it }
    optional(DP) {
        DP.consume()
        QualifiedName().also { portUsage.className = it }
    }
    portUsage.create()
    DefinitionBody(Resolved(portUsage.identification?.name?:portUsage.identification?.shortName!!))
}


fun KerML.PartDefinition() {
    val partDefinition = sysMLSemantics.PartDefinitionSemantics()
    PART.consume()
    DEF.consume()
    Identification().also       { partDefinition.identification = it }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also    { partDefinition.specialization = it }
    }
    partDefinition.create()
    DefinitionBody(Resolved(partDefinition.created?.escapedName()!!))
}



fun KerML.PartUsage() {
    val partUsage = sysMLSemantics.PartUsageSemantics()
    PART.consume()
    Identification().also { partUsage.identification = it }

    alternatives {
        DP starts {
            DP.consume()
            Multiplicity().also { partUsage.multiplicity = it }
            QualifiedName().also { partUsage.className = it }
        }
        REFERENCES starts {
            REFERENCES.consume()
            QualifiedName().also { partUsage.references = it }
        }
        others {  }
    }

    // For SysMLV2, multiplicity is after the type:
    optional(LCBRACE) {
        Multiplicity().also { partUsage.multiplicity = it }
    }

    optional(EQ, consume = true) {
        when(token.kind) {
            NAME_LIT ->  partUsage.subsetting = QualifiedNameList()
            else -> throw SyntaxError(this, "expected when or list of subsetted features")
        }
    }
    partUsage.create()
    DefinitionBody(Resolved(partUsage.created!!))
}
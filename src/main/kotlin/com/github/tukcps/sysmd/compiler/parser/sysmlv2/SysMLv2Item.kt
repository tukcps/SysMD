@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.exceptions.SyntaxError
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.parser.kerml.Body
import com.github.tukcps.sysmd.compiler.parser.kerml.Identification
import com.github.tukcps.sysmd.compiler.parser.kerml.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*



fun KerML.ItemDefinition() {
    val itemDefinition = sysMLSemantics.ItemDefinitionSemantics()
    ITEM.consume()
    DEF.consume()
    Identification().also { itemDefinition.identification = it }
    optional(SPECIALIZES) {
        SPECIALIZES.consume()
        QualifiedName().also { itemDefinition.specialization = it }
    }
    itemDefinition.create()
    Body(Resolved(itemDefinition.identification.name?:itemDefinition.identification.shortName!!))
}



fun KerML.ItemUsage() {
    val itemUsage = sysMLSemantics.ItemUsageSemantics()
    ITEM.consume()
    Identification().also { itemUsage.identification = it }
    alternatives {
        DP starts {
            DP.consume()
            Multiplicity().also { itemUsage.multiplicity = it } // deprecated
            QualifiedName().also { itemUsage.className = it }
        }
        REFERENCES starts {
            REFERENCES.consume()
            QualifiedName().also { itemUsage.references = it }
        }
        others {  }
    }

    // For SysMLV2, multiplicity is after the type:
    optional(LCBRACE) {
        Multiplicity().also { itemUsage.multiplicity = it }
    }

    optional(EQ, consume =true) {
        when(token.kind) {
            NAME_LIT ->  itemUsage.subsetting = QualifiedNameList()
            else -> throw SyntaxError(this, "expected when or list of subsetted features")
        }
    }
    itemUsage.create()
    DefinitionBody(Resolved(itemUsage.created!!))
}
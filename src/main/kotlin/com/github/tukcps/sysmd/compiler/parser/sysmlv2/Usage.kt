@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation


/**
 * 8.2.2.6.2 Usages
 *
 *      FeatureDirection = 'in' | 'out' | 'inout'
 */
fun SysMLv2.FeatureDirectionOptional() {
    when(token.kind) {
        IN  -> IN.consume().also { semantics.prefixes.add(IN)  }
        OUT -> OUT.consume().also { semantics.prefixes.add(OUT)  }
        INOUT -> INOUT.consume().also { semantics.prefixes.add(INOUT)  }
        else -> return // Used as optional
    }
}

/**
 *      RefPrefix = FeatureDirection? ('abstract'|'variation')? 'readonly'? 'derived'? 'end'?
 */
fun SysMLv2.RefPrefix() {
    FeatureDirectionOptional()
    when (token.kind) {
        ABSTRACT  -> ABSTRACT.consume()
        VARIATION -> VARIATION.consume()
        else -> { }
    }
    READONLY.optional()
    DERIVED.optional()
    END.optional()
}

/**
 *      BasicUsagePrefix = RefPrefix 'ref'?
 */
fun SysMLv2.BasicUsagePrefix() {
    RefPrefix()
    REF.optional()
}

/**
 *      UsageExtensionKeyword = PrefixMetadataMember
 */
fun SysMLv2.UsageExtensionKeyword() {
    HASHTAG.consume()
    Unsupported("Usage extension keyword are not supported yet")
}

/**
 *      UsagePrefix = BasicUsagePrefix UsageExtensionKeyword*
 */
fun SysMLv2.UsagePrefix() {
    BasicUsagePrefix()
    noOrMore(start = HASHTAG) { UsageExtensionKeyword() }
}

/**
 *      Usage = UsageDeclaration UsageCompletion
 */
internal fun SysMLv2.Usage() {
    UsageDeclaration()
    UsageCompletion()
}
fun SysMLv2.usageStarts() = usageDeclarationStarts()

/**
 *      UsageDeclaration = Identification FeatureSpecializationPart?
 */
internal fun SysMLv2.UsageDeclaration() {
    Identification().also { semantics.create(it) }
    optional(start = featureSpecializationPartStart) {
        FeatureSpecializationPart()
    }
    // SysMD proprietary extension
    TypeConstraint().also { if ( it.isNotEmpty() ) semantics.addTypeConstraint(it) }
    UnitConstraint().also { semantics.addUnitConstraint(it) }
}


/**
 *      UsageCompletion = ValuePart? UsageBody
 */
internal fun SysMLv2.UsageCompletion() {
    optional(valuePartStart) {
        ValuePart()
    }
    UsageBody()
}

/**
 *      UsageBody = DefinitionBody
 */
fun SysMLv2.UsageBody() {
    DefinitionBody()
}

/**
 * 8.2.2.6.3 Reference Usages
 *
 *      DefaultReferenceUsage = RefPrefix Usage
 *      ReferenceUsage = RefPrefix 'ref' Usage
 *
 * Note: Both productions are implemented as one by making ref optional.
 */
fun SysMLv2.ReferenceUsage() = FeatureActions<Feature>(semantics, ::FeatureImplementation).parse {
    // RefPrefix()      Prefixed are done in calling production
    // REF.optional()
    Usage()
}

/**
 *      VariantReference = OwnedReferenceSubsetting FeatureSpecialization* UsageBody
 */
fun SysMLv2.VariableUsage() {
    TODO()
}
@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.FeatureActions
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Resolved
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
    TODO()
}

/**
 *      UsagePrefix = BasicUsagePrefix UsageExtensionKeyword*
 */
fun SysMLv2.UsagePrefix() {
    BasicUsagePrefix()
    // optional { UsageExtensionKeyword() }
}

/**
 *      Usage = UsageDeclaration UsageCompletion
 */
internal fun SysMLv2.Usage(feature: FeatureActions<Feature>) {
    UsageDeclaration(feature)
    UsageCompletion(feature)
}
fun SysMLv2.usageStarts() = usageDeclarationStarts()

/**
 *      UsageDeclaration = Identification FeatureSpecializationPart?
 */
internal fun SysMLv2.UsageDeclaration(feature: FeatureActions<Feature>) {
    Identification().also { feature.create(it) }
    optional(start = featureSpecializationPartStart) {
        FeatureSpecializationPart(feature)
    }
    // SysMD proprietary extension
    TypeConstraint().also {   feature.addTypeConstraint(it) }
    UnitConstraint().also { feature.addUnitConstraint(it) }
}


/**
 *      UsageCompletion = ValuePart? UsageBody
 */
internal fun SysMLv2.UsageCompletion(feature: FeatureActions<Feature>) {
    optional(valuePartStart) {
        ValuePart(feature)
    }
    UsageBody(Resolved(feature.created!!))
}

/**
 *      UsageBody = DefinitionBody
 */
fun SysMLv2.UsageBody(owner: Resolved<Element>) {
    DefinitionBody(owner)
}

/**
 * 8.2.2.6.3 Reference Usages
 *
 *      DefaultReferenceUsage = RefPrefix Usage
 *      ReferenceUsage = RefPrefix 'ref' Usage
 *
 * Note: Both productions are implemented as one by making ref optional.
 */
fun SysMLv2.ReferenceUsage() {
    val referenceUsage = FeatureActions<Feature>(semantics, ::FeatureImplementation, mutableListOf("Base::Anything"))
    // RefPrefix()      Prefixed are done in calling production
    // REF.optional()
    Usage(referenceUsage)
    referenceUsage.finish()
}

/**
 *      VariantReference = OwnedReferenceSubsetting FeatureSpecialization* UsageBody
 */
fun SysMLv2.VariableUsage() {
    TODO()
}

@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.NamespaceActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.Resolved
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation

/**
 *      Package = PrefixMetadataMember* PackageDeclaration PackageBody
 *      LibraryPackage = ( isStandard ?= 'standard' ) 'library' PrefixMetadataMember* PackageDeclaration PackageBody
 *      PackageDeclaration = 'package' Identification
 */
fun SysMLv2.Package() {
    val pkg = NamespaceActions(semantics, ::PackageImplementation)
    STANDARD.optional { semantics.prefixes.add(STANDARD) }
    LIBRARY.optional  { semantics.prefixes.add(LIBRARY) }
    PACKAGE.consume()
    Identification().also { pkg.create(it) }
    PackageBody(Resolved(pkg.created!!))
}

/**
 *      PackageBody = ';' | '{' PackageBodyElement* '}'
 */
fun SysMLv2.PackageBody(owner: Resolved<Namespace>) {
    alternatives {
        SEMICOLON then { }
        LCURBRACE then {
            semantics.pushOwner(owner)
            noOrMore(stop = RCURBRACE) {
                PackageBodyElement()
            }
            RCURBRACE.consume()
            semantics.popOwner()
        }
    }
}

/**
 *      PackageBodyElement = PackageMember | ElementFilterMember | AliasMember | Import
 *      PackageMember = MemberPrefix (DefinitionElement | UsageElement)
 */
fun SysMLv2.PackageBodyElement( ) {
    MemberPrefix()
    when {
        definitionElementStarts() -> DefinitionElement()
        ALIAS.starts()            -> AliasMember()
        IMPORT.starts()           -> Import()
        else -> {
            FeaturePrefix() // Todo: replace by Occurrence prefix and put it in right place.
            UsageElement()
        }
    }
}

/**
 *      MemberPrefix = (VisibilityIndicator)?
 *      ElementFilterMember = MemberPrefix 'filter' OwnedExpression ';'
 */


/**
 * 8.2.2.5.2 Package Elements
 *
 *      DefinitionElement = Package | LibraryPackage | AnnotatingElement | Dependency
 *          | AttributeDefinition | EnumerationDefinition | OccurrenceDefinition
 *          | IndividualDefinition | ItemDefinition | PartDefinition | ConnectionDefinition
 *          | FlowConnectionDefinition | InterfaceDefinition | PortDefinition
 *          | ActionDefinition | CalculationDefinition | StateDefinition
 *          | ConstraintDefinition | RequirementDefinition
 *          | ConcernDefinition | CaseDefinition | AnalysisCaseDefinition
 *          | VerificationCaseDefinition | UseCaseDefinition
 *          | ViewDefinition | ViewpointDefinition | RenderingDefinition
 *          | MetadataDefinition | ExtendedDefinition
 */
fun SysMLv2.DefinitionElement() {
    alternatives {
        PACKAGE             starts { Package() }
        LIBRARY             starts { Package() }
        annotatingElementStart starts  { AnnotatingElement() }
        DEPENDENCY          starts { Dependency() }
        ATTRIBUTE then DEF  starts { AttributeDefinition() }
        // ENUM not supported
        OCCURRENCE then DEF starts { OccurrenceDefinition() }
        INDIVIDUAL then DEF starts { Unsupported() }
        ITEM then DEF       starts { ItemDefinition() }
        PART then DEF       starts { PartDefinition() }
        CONNECTION then DEF starts { ConnectionDefinition() }
        ALLOCATION then DEF starts { AllocationDefinition() }
        // FLOW isn't supported
        INTERFACE then DEF  starts { InterfaceDefinition() }
        PORT then DEF       starts { PortDefinition() }
        ACTION then DEF     starts { ActionDefinition() }
        CALC then DEF       starts { CalculationDefinition() }
        STATE then DEF      starts { StateDefinition() }
        CONSTRAINT then DEF starts { ConstraintDefinition() }
        REQUIREMENT then DEF starts { RequirementDefinition() }
        METADATA then DEF starts   { Unsupported() }
        others { throwSyntaxError("Invalid definition") }
    }
}
fun SysMLv2.definitionElementStarts() = (nextToken.kind == DEF ) or
    (token.kind in setOf(PACKAGE, LIBRARY, DEPENDENCY)) or ( token.kind in annotatingElementStart)

/**
 * 8.2.2.6.4 Body Elements
 *
 *      NonOccurrenceUsageElement =
 *          DefaultReferenceUsage | ReferenceUsage | AttributeUsage | EnumerationUsage
 *          | BindingConnectorAsUsage | SuccessionAsUsage | ExtendedUsage
 */
fun SysMLv2.NonOccurrenceUsageElement() {
    UsagePrefix()
    when {
        // DefaultReferenceUsage implemented by ReferenceUsage
        referenceUsageStarts() -> ReferenceUsage()
        ATTRIBUTE.starts()     -> AttributeUsage()
        enumerationUsageStarts()-> EnumerationUsage()
        else                   -> throwSyntaxError("Invalid non-occurrence usage")
    }
}
fun SysMLv2.nonOccurrenceUsageStarts() = (token.kind in (mutableSetOf(ATTRIBUTE) + usagePrefixStart) )
        || referenceUsageStarts()

/**
 *      BehaviorUsageElement = ActionUsage | CalculationUsage | StateUsage
 *          | ConstraintUsage | RequirementUsage | ConcernUsage | CaseUsage
 *          | AnalysisCaseUsage | VerificationCaseUsage | UseCaseUsage
 *          | ViewpointUsage | PerformActionUsage | ExhibitStateUsage | IncludeUseCaseUsage
 *          | AssertConstraintUsage | SatisfyRequirementUsage
 */
fun SysMLv2.BehaviorUsageElement() {
    when(token.kind) {
        ACTION      -> { ActionUsage() }
        CALC        -> { CalculationUsage() }
        STATE       -> { StateUsage() }
        CONSTRAINT  -> { ConstraintUsage() }
        // Verification use case ... include use case not supported yes
        REQUIREMENT -> { RequirementUsage() }
        PERFORM     -> { PerformActionUsage() }
        ASSERT      -> { AssertConstraintUsage() }
        SATISFY     -> { Unsupported()}
        else        -> { throwSyntaxError("Unknown behavior usage element")}
    }
}
val behaviorUsageElementStart = setOf(ACTION, CALC, STATE, CONSTRAINT, REQUIREMENT, PERFORM, ASSERT, SATISFY)

/**
 *      StructureUsageElement = OccurrenceUsage | IndividualUsage | PortionUsage
 *          | EventOccurrenceUsage | ItemUsage | PartUsage | ViewUsage | RenderingUsage
 *          | PortUsage | ConnectionUsage | InterfaceUsage | AllocationUsage
 *          | Message | FlowConnectionUsage | SuccessionFlowConnectionUsage
 */
fun SysMLv2.StructureUsageElement() {
    FeaturePrefix()
    alternatives {
        OCCURRENCE starts { OccurrenceUsage() }
        INDIVIDUAL starts { IndividualUsage() }
        ITEM    starts { ItemUsage()}
        PART    starts { PartUsage() }
        PORT    starts { PortUsage() }
        connectionUsageStart starts { ConnectionUsage() }
        INTERFACE starts { InterfaceUsage() }
        allocationUsageStart starts { AllocationUsage() }
        FLOW starts { FlowConnectionUsage() }
        // ..
    }
}
val structureUsageElementStart = setOf(OCCURRENCE, ITEM, PART, PORT, INTERFACE, FLOW)+allocationUsageStart+connectionUsageStart

/**
 *      OccurrenceUsageElement = StructureUsageElement | BehaviorUsageElement
 */
fun SysMLv2.OccurrenceUsageElement() {
    alternatives {
        structureUsageElementStart starts { StructureUsageElement() }
        behaviorUsageElementStart  starts { BehaviorUsageElement() }
    }
}
val occurrenceUsageStart = structureUsageElementStart + behaviorUsageElementStart

/**
 *      UsageElement = NonOccurrenceUsageElement | OccurrenceUsageElement
 */
fun SysMLv2.UsageElement() {
    UsagePrefix()
    when {
        nonOccurrenceUsageStarts()      -> { NonOccurrenceUsageElement() }
        occurrenceUsageStart.starts()   -> { OccurrenceUsageElement() }
        else -> { throwSyntaxError("Unknown usage element") }
    }
}
fun SysMLv2.usageElementStarts() = nonOccurrenceUsageStarts() ||
        occurrenceUsageStart.starts() || usagePrefixStart.starts()

/**
 *      VariantUsageElement = VariantReference | ReferenceUsage | AttributeUsage
 *          | BindingConnectorAsUsage | SuccessionAsUsage | OccurrenceUsage | IndividualUsage
 *          | PortionUsage | EventOccurrenceUsage | ItemUsage | PartUsage | ViewUsage
 *          | RenderingUsage | PortUsage | ConnectionUsage | InterfaceUsage
 *          | AllocationUsage | Message | FlowConnectionUsage
 *          | SuccessionFlowConnectionUsage | BehaviorUsageElement
 */
fun SysMLv2.VariantUsageElement() {
    Unsupported()
}

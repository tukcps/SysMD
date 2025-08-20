@file:Suppress("FunctionName")

package com.github.tukcps.sysmd.compiler.parser.sysmlv2

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.parser.kerml.*
import com.github.tukcps.sysmd.compiler.parser.util.Unsupported
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.*
import com.github.tukcps.sysmd.compiler.semantics.kerml.NamespaceActions
import com.github.tukcps.sysmd.exceptions.throwSyntaxError
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation

/**
 *      Package = PrefixMetadataMember* PackageDeclaration PackageBody
 *      LibraryPackage = ('standard'?) 'library' PrefixMetadataMember* PackageDeclaration PackageBody
 *      PackageDeclaration = 'package' Identification
 */
fun SysMLv2.Package() = NamespaceActions(semantics, ::PackageImplementation).parse {
    STANDARD.optional { semantics.prefixes.add(STANDARD) }
    LIBRARY.optional  { semantics.prefixes.add(LIBRARY) }
    PACKAGE.consume()
    Identification().also { semantics.create(it) }
    PackageBody()
}

/**
 *      PackageBody = ';' | '{' PackageBodyElement* '}'
 */
fun SysMLv2.PackageBody() {
    when(token.kind) {
        SEMICOLON -> { SEMICOLON.consume() }
        LCURBRACE -> {
            LCURBRACE.consume()
            noOrMore(stop = RCURBRACE) {
                PackageBodyElement()
            }
            RCURBRACE.consume()
        }
        else -> handleSyntaxError("expecting package body (semicolon for none, or body in curly braces)")
    }
}

/**
 *      PackageBodyElement = PackageMember | ElementFilterMember | AliasMember | Import
 *      PackageMember = MemberPrefix (DefinitionElement | UsageElement)
 */
fun SysMLv2.PackageBodyElement() {
    MemberPrefix()
    when {
        definitionElementStarts() -> DefinitionElement()
        ALIAS.starts()            -> AliasMember()
        IMPORT.starts()           -> Import()
        usageElementStarts()      -> UsageElement()
        else                      -> handleSyntaxError("At ${token}: Problem parsing package body element.")
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
    when (token.kind) {
        PACKAGE                     -> Package()
        LIBRARY                     -> Package()
        in annotatingElementStart   -> AnnotatingElement()
        DEPENDENCY, HASHTAG         -> Dependency()
        ATTRIBUTE if DEF.isNext()   -> AttributeDefinition()
        ENUM                        -> Unsupported("Enumerations are not yet supported")
        OCCURRENCE if DEF.isNext()  -> OccurrenceDefinition()
        INDIVIDUAL if match(INDIVIDUAL, OCCURRENCE, DEF)  -> OccurrenceDefinition()
        INDIVIDUAL if DEF.isNext()  -> IndividualDefinition()
        ITEM if DEF.isNext()        -> ItemDefinition()
        PART if DEF.isNext()        -> PartDefinition()
        CONNECTION if DEF.isNext()  -> ConnectionDefinition()
        ALLOCATION if DEF.isNext()  -> AllocationDefinition()
        FLOW                        -> Unsupported("Flows are not yet supported")
        INTERFACE if DEF.isNext()   -> InterfaceDefinition()
        PORT if DEF.isNext()        -> PortDefinition()
        ACTION if DEF.isNext()      -> ActionDefinition()
        CALC if DEF.isNext()        -> CalculationDefinition()
        STATE if DEF.isNext()       -> StateDefinition()
        CONSTRAINT if DEF.isNext()  -> ConstraintDefinition()
        REQUIREMENT if DEF.isNext() -> RequirementDefinition()
        VIEW if DEF.isNext()        -> Unsupported("Views are not yet supported")
        VIEWPOINT if DEF.isNext()   -> Unsupported("Viewpoints are not yet supported")
        METADATA if DEF.isNext()    -> MetadataDefinition()
        else                        -> handleSyntaxError("Invalid definition")
    }
}
fun SysMLv2.definitionElementStarts() = (nextToken.kind == DEF ) or match(INDIVIDUAL, OCCURRENCE, DEF) or
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
        else                   -> handleSyntaxError("Invalid non-occurrence usage")
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
        ACTION      -> ActionUsage()
        CALC        -> CalculationUsage()
        STATE       -> StateUsage()
        CONSTRAINT  -> ConstraintUsage()
        REQUIREMENT -> RequirementUsage()
        CONCERN     -> Unsupported("Concern usage not yet supported")
        CASE        -> Unsupported("Case usage not yet supported")
        ANALYSIS    -> Unsupported("Analysis case usage not yet supported")
        VERIFICATION-> Unsupported("Verification case usage not yet supported")
        VIEWPOINT   -> Unsupported("Viewpoint not yet supported")
        PERFORM     -> PerformActionUsage()
        ASSERT if match(ASSERT, SATISFY) -> SatisfyRequirementUsage()
        ASSERT if match(ASSERT, NOT,SATISFY) -> SatisfyRequirementUsage()
        SATISFY, NOT -> SatisfyRequirementUsage()
        ASSERT      -> AssertConstraintUsage()
        else        -> handleSyntaxError("Unknown behavior usage element")
    }
}
val behaviorUsageElementStart = setOf(ACTION, CALC, STATE, CONSTRAINT, CONCERN, CASE, ANALYSIS, VERIFICATION, VIEWPOINT, REQUIREMENT, PERFORM, ASSERT, SATISFY, ASSUME, NOT)

/**
 *      StructureUsageElement = OccurrenceUsage | IndividualUsage | PortionUsage
 *          | EventOccurrenceUsage | ItemUsage | PartUsage | ViewUsage | RenderingUsage
 *          | PortUsage | ConnectionUsage | InterfaceUsage | AllocationUsage
 *          | Message | FlowConnectionUsage | SuccessionFlowConnectionUsage
 */
fun SysMLv2.StructureUsageElement() {
    FeaturePrefix()
    when(token.kind) {
        OCCURRENCE  -> OccurrenceUsage()
        INDIVIDUAL if SNAPSHOT.isNext() ->  PortionUsage()
        INDIVIDUAL if TIMESLICE.isNext() -> PortionUsage()
        INDIVIDUAL  -> IndividualUsage()
        SNAPSHOT    -> PortionUsage()
        EVENT       -> EventOccurrenceUsage()
        ITEM        -> ItemUsage()
        PART        -> PartUsage()
        VIEW        -> Unsupported("View usage not yet supported")
        RENDERING   -> Unsupported("Rendering usage not yet supported")
        PORT        -> PortUsage()
        in connectionUsageStart -> ConnectionUsage()
        INTERFACE   -> InterfaceUsage()
        in allocationUsageStart -> AllocationUsage()
        FLOW        -> FlowConnectionUsage()
        SUCCESSION  -> Unsupported("Succession flow not supported")
        MESSAGE     -> Unsupported("Messages not supported")
        else        -> handleSyntaxError("Unknown structure usage element")
    }
}
val structureUsageElementStart = setOf(
    OCCURRENCE, INDIVIDUAL, TIMESLICE, SNAPSHOT, EVENT, ITEM, PART, PORT, INTERFACE, FLOW, SUCCESSION, MESSAGE)+allocationUsageStart+connectionUsageStart

/**
 *      OccurrenceUsageElement = StructureUsageElement | BehaviorUsageElement
 */
fun SysMLv2.OccurrenceUsageElement() {
    when(token.kind) {
        in structureUsageElementStart -> { StructureUsageElement() }
        in behaviorUsageElementStart  -> { BehaviorUsageElement() }
        else -> handleSyntaxError("Unknown occurrence usage element")
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

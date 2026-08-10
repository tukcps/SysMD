@file:Suppress("UNCHECKED_CAST")
package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.END
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.util.TypeConstraint

/**
 * Semantic action for the declaration of a Type.
 * @param context object with the overall semantic action's context of the parser
 * @param isImplicit The qualified name of the Type's superclass
 */
open class TypeAction(
    context: ActionsContext,
    type: ElementType,
    owningMembershipType: ElementType? = ElementType.OwningMembership,
    var isImplicit: String? = "Base::Anything",
): NamespaceAction(context, type, owningMembershipType) {

    /** Whether a type was added. */
    var superTypeDefined = false

    /** Whether a multiplicity was added; else, default multiplicity must be added. */
    var multiplicityAdded = false

    override fun beforeProduction() {
        super.beforeProduction()
        if (Token.Kind.ABSTRACT in context.prefixes)
            element.isAbstract = true
    }

    override fun afterProduction() {
        // Dont add implicit specialization for Base::Anything. TODO: Identif Anything by ID, not name.
        if (context.element.declaredName != "Anything"
            && !superTypeDefined && isImplicit != null
            && element.isAbstract != true
            && context.compiler.settings.addImplied
        ) {
            context.addSpecialization(isImplicit!!)
            superTypeDefined = true
        }
        super.afterProduction()
    }
}

/**
 * Semantic action for the declaration of a Class.
 * @param context object with the semantic actions of the parser.
 * @param type string that defines the type of the element.
 * @param isImplicit class that is general for non-abstract classes; also default for missing general class
 */
open class ClassAction(
    context: ActionsContext,
    type: ElementType = ElementType.Class,
    isImplicit: String = "Occurrences::Occurrence",
): TypeAction(context = context, type = type, isImplicit = isImplicit) {
    override fun afterProduction() {
        // TODO: FIXME, must use UUID5 of Anything.
        if (context.element.declaredName != "Anything" && !superTypeDefined && isImplicit != null && element.isAbstract != true) {
            context.addSubclassification(isImplicit!!)
            superTypeDefined = true
        }
        super.afterProduction()
    }
}

/**
 * Semantic actions of a Feature
 * @param context object with the semantic context of the parse run
 * @param isImplicit supertypes to be added as default
 */
open class FeatureAction(
    context: ActionsContext,
    type: ElementType = ElementType.Feature,
    isImplicit: String = "Base::Anything",
    owningMembershipType : ElementType? = ElementType.FeatureMembership,
): TypeAction(
    context = context,
    type = type,
    owningMembershipType = owningMembershipType,
    isImplicit = isImplicit
) {
    override fun beforeProduction() {
        element.direction = if (context.directionFromPrefixes() != null) context.directionFromPrefixes() else null
        element.isEnd = END in context.prefixes
        element.isComposite = Token.Kind.COMPOSITE in context.prefixes
        element.isPortion = Token.Kind.PORTION in context.prefixes
        element.isUnique = Token.Kind.UNIQUE in context.prefixes
        element.isOrdered = Token.Kind.ORDERED in context.prefixes
        element.isConstant = Token.Kind.CONST in context.prefixes
        element.isAbstract = Token.Kind.ABSTRACT in context.prefixes
        element.isDerived = Token.Kind.DERIVED in context.prefixes
        super.beforeProduction()
    }
}

/**
 * Semantic action that builds a Multiplicity element.
 */
class MultiplicityAction(
    context: ActionsContext
) : FeatureAction(
    context = context,
    type = ElementType.Multiplicity,
    isImplicit = "ScalarValues::Natural"
){
    /** Defaults: type: 0 .. * for types/features, 1..1 for usages, definitions */
    var typeConstraint =
        if (context.compiler is SysMLv2) TypeConstraint("1..1")
        else TypeConstraint("0..*")

    override fun afterProduction() {
        element.declaredName = "multiplicity"
        element.declaredShortName = "cardinality"
        if (context.compiler.settings.addConstraints)
            context.addTypeConstraint(typeConstraint)
        super.afterProduction()
    }
}

/**
 * Semantic action for the declaration of a Type.
 * @param context object with the overall semantic action's context of the parser
 * @param isImplicit The qualified name of the Type's superclass
 */
@Deprecated("Use TypeAction instead with ElementType as parameter")
open class TypeActions<T: Type>(
    context: ActionsContext,
    creator: () -> T,
    var isImplicit: String? = "Base::Anything",
): NamespaceActions<T>(context, creator)
/**
 * Semantic actions of a Feature
 * @param context object with the semantic context of the parse run
 * @param isImplicit supertypes to be added as default
 * @param valuePart the AST as parsed
 */
@Deprecated("Will cause crash")
open class FeatureActions<T: Feature>(
    context: ActionsContext,
    creator: () -> T,
    isImplicit: String = "Base::Anything",
    var valuePart: AstRoot? = null,
): TypeActions<T>(context, creator, isImplicit) {

    override fun create(identification: Identification?) {}

    override fun finish() {}
}
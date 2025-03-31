package com.github.tukcps.sysmd.compiler.semantics.kerml

import io.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.END
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName

/**
 * Semantic action for the declaration of a Type.
 * @param context object with the semantic actions of the parser
 * @param specializes The qualified name of the Type's superclass
 */
open class TypeActions<T: Type>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    var specializes: MutableList<String> = mutableListOf("Base::Anything"),
    var isSufficient: Boolean = false,
): SemanticAction<T>(context, creator) {

    override fun create(identification: Identification) {
        super.create(identification)
        created?.isSufficient = isSufficient
        created?.isAbstract = Token.Kind.ABSTRACT in context.prefixes
    }

    fun addMultiplicity(multiplicity: IntegerRange) {
        val multiplicity = MultiplicityImplementation(
            multiplicity = "${multiplicity.min} .. ${multiplicity.max}"
        )
        context.model.addUnownedElement(multiplicity, startOfOwnerPath = created!!)
    }

    fun addSpecialization(types: MutableList<String>) {
        types.forEach {
            val specialization = SpecializationImplementation(
                specific = Resolved(ref = created!!),
                general = Resolved(it)
            )
            context.model.addUnownedElement(specialization, startOfOwnerPath = created!!)
        }
    }

    fun addConjugation(types: MutableList<String>) {
        types.forEach {

        }
    }

    open fun finish() {
        if (created != null) {
            val specializations = context.model.getUnownedElements().filter { it.element is Specialization && it.startOfPath == this.created }
            if (specializations.isEmpty()) {
                addSpecialization(specializes)
            }
        }
    }
}

open class ClassifierActions<T: Classifier>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<String> = mutableListOf("Occurrences::Occurrence")
): TypeActions<T>(context, creator, specializes) {

    fun addSubclassification(types: MutableList<String>) {
        types.forEach {
            val specialization = SubclassifierImplementation(
                subclassification = Resolved(ref = created!!),
                superclassification = Resolved(it)
            )
            context.model.addUnownedElement(specialization, startOfOwnerPath = created!!)
        }
    }
}

/**
 * Semantic action for the declaration of a Class.
 * @param context object with the semantic actions of the parser
 * @param creator function that creates a Class element
 * @param specializes class that is general for non-abstract classes; also default for missing general class
 */
open class ClassActions<T: Class>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<String> = mutableListOf("Occurrences::Occurrence")
): ClassifierActions<Class>(context, creator, specializes)


/**
 * Semantic action for the declaration of a DataType.
 * @param context object with the semantic actions of the parser
 * @param creator Function to build a DataType
 * @param specializes Default specialization if none is given by user
 */
open class DataTypeActions<T: DataType>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<String> = mutableListOf("Base::DataValue"),
): ClassifierActions<DataType>( context, creator , specializes )


/**
 * Semantic action for the declaration of a Structure.
 * @param context object with the semantic actions of the parser
 */
open class StructureActions<T: Structure>(
    context: SemanticActions,
    creator: (SimpleName?, SimpleName?) -> T,
    specializes: MutableList<String> = mutableListOf("Occurrences::Occurrence")
): ClassifierActions<T>(context, creator, specializes = specializes)


/**
 * Semantic actions of a Feature
 * @param context object with the semantic context of the parse run
 * @param defaultType supertypes to be added as default
 * @param valuePart the AST as parsed
 */
open class FeatureActions<T: Feature>(
    context: ActionsContext,
    creator: (SimpleName?, SimpleName?) -> T,
    var defaultType: MutableList<String>,
    isSufficient: Boolean = false,
    var valuePart: AstRoot? = null,
): TypeActions<T>(context, creator, defaultType, isSufficient) {
    override fun create(identification: Identification) {
        super.create(identification)
        created?.direction = context.directionFromPrefixes()
        created?.isEnd = END in context.prefixes
        created?.isComposite = Token.Kind.COMPOSITE in context.prefixes
        created?.isPortion = Token.Kind.PORTION in context.prefixes
        created?.isUnique = Token.Kind.UNIQUE in context.prefixes
        created?.isOrdered = Token.Kind.ORDERED in context.prefixes
        created?.isReadOnly = Token.Kind.READONLY in context.prefixes
        created?.isAbstract = Token.Kind.ABSTRACT in context.prefixes
        created?.isDerived = Token.Kind.DERIVED in context.prefixes
        context.namespace = created!!
    }

    /**
     * Adds FeatureTyping elements to a created Feature.
     * The types are still names and will become references during initialization.
     * @param type a list of qualified names that shall be added.
     */
    fun addTyping(type: MutableList<QualifiedName>) {
        if (type.isEmpty() )
            type.addAll(defaultType)
        type.forEach {
            val typing = FeatureTypingImplementation(
                typedFeature = Resolved(ref = created!!),
                type = Resolved(str = it)
            )
            context.model.addUnownedElement(typing, startOfOwnerPath = created!!)
        }
    }

    /**
     * Adds the subsetting.
     */
    fun addSubsetting(features: MutableList<QualifiedName>) {
        features.forEach {
            val subsetting = SubsettingImplementation(
                subsettedFeature = Resolved(str = it),
                subsettingFeature = Resolved(ref = created!!)
            )
            context.model.addUnownedElement(subsetting, startOfOwnerPath = created!!)
        }
    }

    /**
     * Adds a reference subsetting to the feature
     * @param references, a single qualified name
     */
    fun addReferences(references: QualifiedName?) {
        if (references != null) {
            context.addReferenceSubsetting(owner = created!!, null, referencedFeature = references)
        }
    }

    fun addRedefinitions(redefines: String?) {
        if (redefines != null) {
            if (created == null) {
                create(Identification(name = redefines))
                created?.isRedefined = true
                context.addRedefinition(owner = created!!, null, redefinedFeature = redefines)
            } else  {
                if (created!!.declaredName == null && created!!.shortName == null)
                    created!!.declaredName = redefines
                created!!.isRedefined = true
                context.addRedefinition(owner = created!!, null, redefinedFeature = redefines)
             }
        }
    }

    fun addUnitConstraint(unitConstraint: String?) {
        created?.unitConstraint = unitConstraint
    }

    fun addTypeConstraint(typeConstraint: MutableList<String>) {
        created?.typeConstraint = typeConstraint
    }

    override fun finish() {
        if (created != null) {
            val typing = context.model.getUnownedElements().filter { it.element is FeatureTyping && it.startOfPath == this.created }
            if (typing.isEmpty()) {
                addTyping(specializes)
            }
            val mult = context.model.getUnownedElements().filter { it.element is Multiplicity && it.startOfPath == this.created }
            if (mult.isEmpty()) {
                // addMultiplicity(IntegerRange(1, 1))
            }
        }
        context.prefixes.clear()
    }
}

package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.kerml.ClassifierDeclarationInfo
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.END
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions

/**
 * Semantic action for the declaration of a Class.
 * @param context object with the semantic actions of the parser
 * @param owner The identification of the Class' owner
 * @param classifierDeclaration The declaration information of the Class
 */
class ClassActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var classifierDeclaration: ClassifierDeclarationInfo = ClassifierDeclarationInfo(),
    var created: Class? = null
) {
    fun create() {
        if (classifierDeclaration.superclassingPart.isEmpty())
            classifierDeclaration.superclassingPart.add("Base::Anything")

        // Call constructor depending on the type
        created = ClassImplementation(
            declaredName = classifierDeclaration.identification.name,
            declaredShortName = classifierDeclaration.identification.shortName,
            owner = context.owners.peek())
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)

        classifierDeclaration.superclassingPart.forEach { general ->
            val specialization = SpecializationImplementation(
                owner = Resolved(ref = created!!),
                specific = Resolved(ref = created!!),
                general = Resolved(general)
            )
            context.model.addUnownedElement(specialization, startOfOwnerPath = created!!)
        }

        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}

/**
 * Semantic action for the declaration of a Type.
 * @param context object with the semantic actions of the parser
 * @param owner The identification of the Type's owner
 * @param identification The name of the Type
 * @param general The qualified name of the Type's superclass
 */
class TypeActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification = Identification(),
    var general: MutableList<QualifiedName> = mutableListOf(),
    var created: Type? = null
) {
    fun create() {
        created = TypeImplementation(declaredName = identification.name, declaredShortName = identification.shortName, owner = context.owners.peek())
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)
        general.forEach {
            val specialization = SpecializationImplementation(
                owner = Resolved(ref = created!!),
                specific = Resolved(ref = created!!),
                general = Resolved(it)
            )
            context.model.addUnownedElement(specialization, startOfOwnerPath = created!!)
        }
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}

/**
 * Semantic action for the declaration of a DataType.
 * @param context object with the semantic actions of the parser
 * @param owner The identification of the owner
 * @param classifierDeclaration name, shortname, specializations
 */
class DataTypeActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var classifierDeclaration: ClassifierDeclarationInfo = ClassifierDeclarationInfo(),
    var created: DataType? = null
) {
    fun create() {
        // Call constructor depending on the type
        created = DataTypeImplementation(
            declaredName = classifierDeclaration.identification.name,
            declaredShortName = classifierDeclaration.identification.shortName,
            owner = context.owners.peek())
        context.model.addUnownedElement(created!!, owner)
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)

        if (classifierDeclaration.superclassingPart.isEmpty())
            classifierDeclaration.superclassingPart.add("Base::Anything")

        classifierDeclaration.superclassingPart.forEach { general ->
            val specialization = SpecializationImplementation(
                owner = Resolved(ref = created!!),
                specific = Resolved(ref = created!!),
                general = Resolved(general)
            )
            context.model.addUnownedElement(specialization, startOfOwnerPath = created!!)
        }
        if (context.generateAnnotations)
            context.addAnnotation(context.textualRepresentation, created!!)
    }
}


/**
 * Semantic actions of a Feature
 * @param context object with the semantic actions of the parser
 * @param type supertypes
 * @param valuePart the AST as parsed
 * @param unitConstraint PROFILE - the extension allows us specifying a required Unit as part of a subtype
 * @param typeConstraint PROFILE - the extension allows us specifying the type to a subtype, i.e., a range
 */
open class FeatureActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var isAbstract: Boolean = Token.Kind.ABSTRACT in context.prefixes,
    var isSufficient: Boolean = false,
    var isPortion: Boolean = Token.Kind.PORTION in context.prefixes,
    var isDerived: Boolean = Token.Kind.DERIVED in context.prefixes,
    var isUnique: Boolean = Token.Kind.UNIQUE in context.prefixes,
    var isOrdered: Boolean = Token.Kind.ORDERED in context.prefixes,
    var isComposite: Boolean = Token.Kind.COMPOSITE in context.prefixes,
    var identification: Identification? = null,
    var multiplicity: IntegerRange = IntegerRange(1,1),
    var type: MutableList<QualifiedName> = mutableListOf("Base::Anything"),
    var references: QualifiedName? = null,
    var redefines: QualifiedName? = null,
    var subsetting: MutableList<QualifiedName> = mutableListOf(),
    var valuePart: AstRoot? = null,
    var unitConstraint: String? = null,
    var typeConstraint: MutableList<String> = mutableListOf(),
    var created: Feature? = null
) {
    open fun create() {
        created = FeatureImplementation(
            declaredName = identification!!.name,
            declaredShortName = identification!!.shortName,
            owner = context.owners.peek(),
            direction = context.directionFromPrefixes(),
            isEnd = END in context.prefixes,
            isComposite = isComposite,
            isPortion = isPortion,
            isSufficient = isSufficient,
            isUnique = isUnique,
            isOrdered = isOrdered,
            isRedefined = redefines != null,
            typeConstraint = typeConstraint,
            unitConstraint = unitConstraint
        ).also {
            it.isAbstract = isAbstract
            it.isDerived = isDerived
            if (redefines != null && it.declaredName == null && it.declaredShortName == null) {
                it.declaredName = redefines!!
                identification?.name = redefines!!
            }
        }
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.namespace = created!!
        val multiplicity = MultiplicityImplementation(
            owner = Resolved(created!!),
            multiplicity = "${multiplicity.min} .. ${multiplicity.max}"
        )
        type.forEach {
            val typing = FeatureTypingImplementation(
                owner = Resolved(ref = created!!),
                typedFeature = Resolved(ref = created!!),
                type = Resolved(str = it)
            )
            context.model.addUnownedElement(typing, (owner + "::${identification!!.toName()}"))
        }
        if (references != null) {
            context.addReferenceSubsetting(owner = created!!, null, referencedFeature = references!!)
        }
        if (redefines != null) {
            context.addRedefinition(owner = created!!, null, redefinedFeature = redefines!!)
        }
        context.model.addUnownedElement(created!!, owner)
        context.model.addUnownedElement(multiplicity, (owner + "::${identification!!.toName()}"))
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }

    fun createReference() {
        context.addReferenceSubsetting(owner = created!!, null, referencedFeature = references!! )
    }
}

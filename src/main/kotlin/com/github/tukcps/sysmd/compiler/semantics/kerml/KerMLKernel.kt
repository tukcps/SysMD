package com.github.tukcps.sysmd.compiler.semantics.kerml

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.LIBRARY
import com.github.tukcps.sysmd.compiler.scanner.Token.Kind.STANDARD
import com.github.tukcps.sysmd.compiler.semantics.Identification
import com.github.tukcps.sysmd.compiler.semantics.SemanticActions
import com.github.tukcps.sysmd.model.kerml.Function


/**
 * Adds a package.
 * If the semantic actions object was build with a generatedElementsAnnotation not null,
 * the created package will be added to it as well.
 */
class PackageActions(
    var context: SemanticActions,
    var isLibrary: Boolean = false,
    var isStandard: Boolean = false,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var created: Package? = null
) {
    fun create(): Package {
        created = PackageImplementation(
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            owner = context.owners.peek(),
            isStandard = isStandard,
            isLibraryElement = isLibrary)
        context.model.addUnownedElement(created!!, owner)
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
        return created!!
    }
}



/**
 * Semantic action for the definition of an association type.
 * @param owner The qualified name of the owner of the Classifiable
 * @param identification The name of the Classifiable
 * @param superclass The qualified name of the superclass of the Classifiable
 * @param sourceEnd A type that specifies class of the sources.
 * @param sourceMult multiplicity of sources
 * @param targetEnd A type that specifies class of the targets.
 * @param targetMult ... its multiplicity
 * an Association will be created; otherwise, a Classifier
 */
class AssociationActions(
    var context: SemanticActions,
    var identification: Identification? = null,
    var superclass: QualifiedName = "Links::BinaryLink",
    var sourceEnd:  Resolved<Feature>? = null,
    var sourceMult: IntegerRange? = null,
    var targetEnd:  Resolved<Feature>? = null,
    var targetMult: IntegerRange? = null,
    var owner: Resolved<Element> = Resolved(context.ownerName()),
    var created: Association? = null
) {
    fun create() {
        // The respective KerML element is either an Association (=typed relationship), or a
        // Connector (=relationship and feature)
        created = AssociationImplementation(
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            owner = context.owners.peek()
        )
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)

        // add the created element to its (unresolved) owner
        context.model.addUnownedElement(created!!, path = context.ownerName())
        context.addSpecialization(created!!, superclass)
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}



/**
 * Adds a relationship/association types by Link to the KerML instances.
 * @param owner Qualified name of the owner
 * @param identification name, short name
 * @param source list of all sources
 * @param target list of all targets
 */
class ConnectorActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification? = null,
    var source: List<QualifiedName> = mutableListOf(),
    var association: QualifiedName = "Links::Link",
    var target: List<QualifiedName> = mutableListOf(),
    var created: Connector? = null
) {
    fun create() = with(context){
        created = ConnectorImplementation(
            owner = owners.peek(),
            declaredName = identification?.name,
            declaredShortName = identification?.shortName,
            from = source.toIdentityList(),
            to = target.toIdentityList(),
        )
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)

        val featureTyping = FeatureTypingImplementation(
            owner = Resolved(str = null, ref = created, id = created!!.elementId),
            typedFeature = Resolved(ref = created!!),
            type = Resolved(str = association)
        )
        model.addUnownedElement(created!!, owner)
        model.addUnownedElement(featureTyping, startOfOwnerPath =  created!!)
        if (source.isNotEmpty()) {
            addReferenceSubsetting(
                owner = created!!,
                pathFromOwnerToReferencingFeature = "source",
                referencedFeature = source.first())
        }
        if (target.isNotEmpty()) {
            addReferenceSubsetting(
                owner = created!!,
                pathFromOwnerToReferencingFeature = "target",
                referencedFeature = target.first())
        }
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}




/**
 * Semantic action for the definition of a Function.
 * @param owner The identification of the Calculation's owner
 * @param identification The name of the Classifiable
 * @param superclass The qualified name of the Calculation's superclass
 */
class FunctionActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification = Identification(),
    var superclass: QualifiedName = "Base::Anything",
    var created: Function? = null
) {
    fun create() {
        // Call constructor depending on type
        created = FunctionImplementation(declaredName = identification.name, declaredShortName = identification.shortName, owner = context.owners.peek())
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)
        val specialization = SpecializationImplementation(specific = Resolved(ref = created!!), general = Resolved(superclass))
        context.model.addUnownedElement(specialization, startOfOwnerPath = created!!)
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}




/**
 * Semantic action for the definition of a Calculation.
 * @param owner The identification of the Calculation's owner
 * @param identification The name of the Classifiable
 * @param superclass The qualified name of the Calculation's superclass
 */
class CalculationActions(
    var context: SemanticActions,
    var owner: QualifiedName = context.ownerName(),
    var identification: Identification = Identification(),
    var superclass: QualifiedName = "Base::Anything",
    var created: Calculation? = null
) {
    fun create() {
        // Call constructor depending on type
        created = CalculationDefinitionImplementation(declaredName = identification.name, declaredShortName = identification.shortName, owner = context.owners.peek())
        created?.textualRepresentation = mutableListOf(context.textualRepresentation)
        context.model.addUnownedElement(created!!, owner)
        val specialization = SpecializationImplementation(specific = Resolved(ref = created!!), general = Resolved(superclass))
        context.model.addUnownedElement(specialization, startOfOwnerPath = created!!)
        if (context.generateAnnotations) context.addAnnotation(context.textualRepresentation, created!!)
    }
}

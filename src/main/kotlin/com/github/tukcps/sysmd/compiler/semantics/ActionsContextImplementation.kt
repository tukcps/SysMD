package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.sysmd.compiler.parser.QualifiedName
import com.github.tukcps.sysmd.compiler.parser.firstName
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.session.Session
import java.util.*


/**
 * This class provides methods that add KerML-Element instances to
 * the KerML model in a session.
 * @param model the KerML model in use in a Session
 * between the textual representation element and the generated elements are added.
 * @param textualRepresentation the textual representation that is parsed.
 *
 * The semantic actions add KerML elements to an owner in the KerML model.
 * As the owner is not completely known, we maintain a stack of names of owner:```owners```.
 * Each action has the parameters:
 * - owner (optional, if known), Identity that can also contain reference and/or id.
 * - element to be added if not created by the action.
 * - the actions shall return the element
 *
 * Generally, a semantic action shall interact with the parser as follows:
 * 1) create the element via *constructor* (hence, not yet owned by KerML model)
 * 2) modify, extend the element such that it satisfies invariants of instances (e.g. features must have specialization, etc.)
 * 3) finally, if (1,2) were successful, call the semantic action with all data needed.
 * 4) add optional parts to the element
 */
open class ActionsContextImplementation(
    final override val model: Session,
    final override val textualRepresentation: TextualRepresentation,
    override val owners: Stack<Resolved<Element>> = Stack<Resolved<Element>>(), // A stack of Identifications of all nested elements
    override val generateAnnotations: Boolean = false,
    override var expression: Feature? = null,
    override var visibilityKind: Token.Kind = Token.Kind.PUBLIC
): ActionsContext {

    /** set with the prefixes of a definition or declaration */
    override val prefixes = mutableSetOf<Token.Kind>()

    init {
        initOwners()
        if (textualRepresentation.language.firstName() !in setOf("SysMD", "SysML", "KerML")) {
            model.report("SysMD parser called with textual representation that is not tagged with language SysMD, SysML, KerML")
        }
    }

    /** Initializes owner stack */
    final override fun initOwners() {
        owners.empty()
        val ownersPrefixes = textualRepresentation.getOwnerPrefix().split("::")
        owners.push(Resolved(ref = model.global))
        ownersPrefixes.forEach {
            pushOwner(Resolved(it))
        }
    }

    /**
     * Pushes an owner
     * @param owner of new
     */
    override fun pushOwner(owner: Resolved<Element>) {
        if (owner.ref != model.global && owner.str != "Global")
            owners.push(owner)
    }

    /**
     * Removes the top name from the owners stack
     */
    override fun popOwner(): Resolved<Element> =
        if (owners.peek().ref != model.global)
            owners.pop()
        else Resolved(id = model.global.elementId, ref = model.global, str = model.global.escapedName())


    /**
     * Adds the namespace prefix of a parse run to the name given as parameter.
     * @return ownerPrefix + name, considering "::" and formatting.
     */
    override fun ownerName(): QualifiedName {
        var s = ""
        owners.forEach {
            s += if (it.ref != null) it.ref?.escapedName() + "::"
            else "${it.str}::"
        }
        s = s.removeSuffix("::")
        return s
    }


    /**
     * Marker only for the generation of AST in Expressions.
     * ONLY FOR USE IN EXPRESSION !!!
     */
    override var namespace: Namespace = model.global


    /**
     * Adds the namespace prefix of a parse run to the name given as parameter.
     * @param name
     * @return namespacePrefix + name, considering "::" and formatting.
     */
    override fun toEffectiveName(name: QualifiedName): QualifiedName {
        return when {
            textualRepresentation.getOwnerPrefix() == "Global" && name.isNotEmpty() -> name
            name.isNotEmpty() -> "${textualRepresentation.getOwnerPrefix()}::$name"
            else -> textualRepresentation.getOwnerPrefix()
        }
    }

    override fun addSpecialization(owner: Type, type: QualifiedName): Specialization {
        val specialization = SpecializationImplementation(
            owner = Resolved(owner),
            specific = Resolved(owner),
            general = Resolved(type)
        )
        model.addUnownedElement(element = specialization, startOfOwnerPath = owner)
        return specialization
    }

    override fun addFeatureTyping(owner: Feature, type: QualifiedName): FeatureTyping {
        val featureTyping = FeatureTypingImplementation(
            owner = Resolved(owner),
            typedFeature = Resolved(owner),
            type = Resolved(type)
        )
        model.addUnownedElement(element = featureTyping, startOfOwnerPath = owner)
        return featureTyping
    }

    override fun addMultiplicity(owner: Feature, integerRange: IntegerRange): Multiplicity {
        val multiplicity = MultiplicityImplementation(
            name = "range",
            owner = Resolved(owner),
        ).also {
            it.typeConstraint = mutableListOf(integerRange.toString())
        }
        addFeatureTyping(multiplicity, "ScalarValues::Integer")
        model.addUnownedElement(multiplicity, startOfOwnerPath = owner)
        return multiplicity
    }

    /**
     * Adds an implied Annotation between an annotating element and an allement.
     */
    override fun addAnnotation(owner: AnnotatingElement, annotatedElement: Element): Annotation {
        val annotation = AnnotationImplementation(
            owner = Resolved(owner),
            annotatingElement = Resolved(ref=owner),
            annotatedElement = Resolved(ref=annotatedElement)
        )
        model.addUnownedElement(annotation, startOfOwnerPath = owner)
        return annotation
    }

    /**
     * Adds a ReferenceSubsetting relationship.
     * @param owner the referencing feature's owner
     * @param pathFromOwnerToReferencingFeature the name of the referencing feature
     * @param referencedFeature qualified name of the referenced feature
     */
    override fun addReferenceSubsetting(
        owner: Element,
        pathFromOwnerToReferencingFeature: QualifiedName?,
        referencedFeature: QualifiedName
    ) {
        val reference = ReferenceSubsettingImplementation(
            owner = Resolved(),
            referencingFeature = Resolved(),
            referencedFeature = Resolved(str = referencedFeature)
        )
        model.addUnownedElement(element = reference, path = pathFromOwnerToReferencingFeature, startOfOwnerPath = owner)
    }


    /**
     * Adds a Redefinition relationship.
     * @param owner the redefining feature and owner of the Redefinition
     * @param pathFromOwnerToRedefinedFeature
     * @param redefinedFeature
     */
    override fun addRedefinition(
        owner: Feature,
        pathFromOwnerToRedefinedFeature: QualifiedName?,
        redefinedFeature: QualifiedName
    ) {
        val redefinition = RedefinitionImplementation(
            owner = Resolved(ref = owner),
            redefiningFeature = Resolved(ref = owner),
            redefinedFeature = Resolved(str = redefinedFeature)
        )
        model.addUnownedElement(element = redefinition, path = pathFromOwnerToRedefinedFeature, startOfOwnerPath = owner)
    }


    /**
     * Service function that adds an implicit feature.
     *  @param owner the owner of the feature
     *  @param type the class of which the feature instances have to be
     *  @param rangeOfMultiplicity the range
     *  @return The created feature.
     */
    override fun addFeature(
        owner: Element,
        feature: Feature,
        type: QualifiedName,
        rangeOfMultiplicity: IntegerRange
    ) {
        addFeatureTyping(feature, type)
        addMultiplicity(feature, rangeOfMultiplicity)
        model.addUnownedElement(feature, startOfOwnerPath = owner)
        if (generateAnnotations) addAnnotation(textualRepresentation, feature)
    }


    /**
     * @method mkFuncCall
     * @detail This method builds the expression tree for a function call.
     * It sets the parameters of a function into the local symbol table.
     * @param function name of the function to be called
     * @param param of the function as an ArrayList of AstNode
     * @return The Expression tree
     * @throws SemanticError
     */
    override fun handleFunctionCall(function: QualifiedName, param: ArrayList<AstNode>, semantics: SemanticActions): AstNode {
        when (function) {
            "ITE" -> return AstIte(model, param)
            "oneOf" -> return buildOneOfAst(model, expression = semantics.expression!!, param, semantics)
            "allOf" -> return AstAllOf(model, param)
            "anyOf" -> return AstAnyOf(model, param)
            "sum_i" -> return AstSumI(namespace, model, param)
            "sum" -> return AstSum(namespace, model, param)
            "sumOverParts" -> return AstSumHasA(model, namespace, param, transitive = true)
            "sumOverSubclasses" -> return AstSumIsA(model, namespace, param, transitive = true)
            "productOverParts" -> return AstProductHasA(model, namespace, param, transitive = true)
            "productOverSubclasses" -> return AstProductIsA(model, namespace, param, transitive = true)
            "sumOverPartsNotTransitive" -> return AstSumHasA(model, namespace, param, transitive = false)
            "sumOverSubclassesNotTransitive" -> return AstSumIsA(model, namespace, param, transitive = false)
            "productOverPartsNotTransitive" -> return AstProductHasA(model, namespace, param, transitive = false)
            "productOverSubclassesNotTransitive" -> return AstProductIsA(model, namespace, param, transitive = false)
            "characterizedResult" -> return AstCharacterizedResult(model, namespace, param)
            "ln" -> return AstLn(model, param)
            "exp" -> return AstExp(model, param)
            "sqr" -> return AstSqr(model, param)
            "sqrt" -> return AstSqrt(model, param)
            "ceil" -> return AstCeil(model, param)
            "floor" -> return AstFloor(model, param)
            "power2" -> return AstPower2(model, param)
            "pow2" -> return AstPower2(model, param)
            "powerb" -> return AstPower(model, param)
            "power" -> return AstPower(model, param)
            "powb" -> return AstPower(model, param)
            "pow" -> return AstPower(model, param)
            "sin" -> return AstSin(model,param)
            "cos" -> return AstCos(model,param)
            "toReal" -> return AstToReal(model, param)
            "DateTime" -> return AstDateTime(model, param)
            "Date" -> return AstDate(model, param)
            "Month" -> return AstMonth(model, param)
            "Year" -> return AstYear(model, param)
            "max" -> return AstMax(model, param)
            "min" -> return AstMin(model, param)
            "abs" -> return AstAbs(model, param)
            "intersect" -> return AstIntersect(model, param)
            "bySubclasses" -> return AstBySubclasses(model, namespace, param)
            "byParts" -> return AstByParts(model, namespace, param)
            "byImplements" -> return AstByImplements(model, namespace, param)
            "linear" -> return AstLinear(model, param)
            "stepInterpolation" -> return AstStepInterpolation(model,param)
            "Real" -> { semantics.model.report(semantics.expression, "Deprecated: Real; use ToReal"); return AstReal(model, param)}
            "ToReal" -> return AstReal(model, param)
            "Integer" -> { semantics.model.report(semantics.expression, "Deprecated: Integer; use ToInteger"); return AstInteger(model, param)}
            "ToInteger" -> return AstInteger(model, param)
            "norm" -> return AstNormalizeVector(model,param)
            "angle" -> return AstVectorAngle(model,param)
            "cityBlockDistance" -> return AstCityBlockDistance(model,param)
            "quantityOfVectorAtPosition" -> return AstQuantityOfVectorAtPosition(model,param)
            else -> return AstUserDefinedFunction(model,namespace, param, function)
        }
    }
}

package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.SimpleName
import com.github.tukcps.sysmd.services.session.Session
import io.github.tukcps.aadd.values.IntegerRange
import java.util.*


/**
 * This class provides methods that add KerML-Element instances to
 * the KerML model in a session.
 * @param model the KerML model in use in a Session
 * between the textual representation element and the generated elements is added.
 *
 * The semantic actions add KerML elements to an owner in the KerML model.
 * As the owner is not completely known, we maintain a stack of names of the owner:```owners```.
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
    final override val compiler: KerML,
    final override val owners: Stack<Resolved<Element>> = Stack<Resolved<Element>>(), // A stack all nexted element's Identifications
    final override var expression: Feature? = null,
    final override var visibilityKind: Token.Kind? = null
): ActionsContext {

    init {
        owners.push(Resolved(model.global))
    }

    /** set with the prefixes of a definition or declaration */
    override val prefixes = mutableSetOf<Token.Kind>()

    /**
     * Initializes the owner stack
     * @param ownerPrefix a string with owners separated by '::'
     */
    final override fun initOwners(ownerPrefix: String) {
        owners.clear()
        val ownersPrefixes = ownerPrefix.split("::")
        owners.push(Resolved(ref = model.global))
        ownersPrefixes.forEach {
            if (it.isNotEmpty())
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
     * Pushes an owner on the stack that maintains hierarchical ownership hierarchy
     * @param owner the owner of new elements until pop
     */
    override fun pushOwner(owner: Element) {
        owners.push(Resolved(ref = owner))
    }

    /**
     * Removes the top name from the owners' stack
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
            if (it.ref != model.global) {
                s += if (it.ref != null) it.ref?.escapedName() + "::"
                else "${it.str}::"
            }
        }
        s = s.removeSuffix("::")
        return s
    }

    /**
     * Generates the owner's name, with owned element's name added.
     * If the simple name is null, only the owner's name is returned.
     * @param owned name of an owned element or null
     * @return generated qualified name of an owned element or owner
     */
    override fun qualifiedName(owned: SimpleName?): QualifiedName {
        var s = ""
        owners.forEach {
            if (it.ref?.name != "Global")
                s += if (it.ref != null) it.ref?.escapedName() + "::"
                else "${it.str}::"
        }
        return if (owned == null)
            s.removeSuffix("::")
        else
            s+owned
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
            ownerName() == "Global" && name.isNotEmpty() -> name
            name.isNotEmpty() -> "${ownerName()}::$name"
            else -> ownerName()
        }
    }

    override fun addSpecialization(owner: Type, type: QualifiedName): Specialization {
        val specialization = SpecializationImplementation(
            specific = Resolved(owner),
            general = Resolved(type)
        ).also { it.owner = Resolved(owner) }
        model.addUnownedElement(element = specialization, startOfOwnerPath = owner)
        return specialization
    }

    override fun addFeatureTyping(owner: Feature, type: QualifiedName): FeatureTyping {
        val featureTyping = FeatureTypingImplementation(
            typedFeature = Resolved(owner),
            type = Resolved(type)
        )
        model.addUnownedElement(element = featureTyping, startOfOwnerPath = owner)
        return featureTyping
    }

    override fun addMultiplicity(owner: Feature, integerRange: IntegerRange): Multiplicity {
        val multiplicity = MultiplicityImplementation(
            name = "range",
        ).also {
            it.typeConstraint = mutableListOf(integerRange.toString())
        }
        addFeatureTyping(multiplicity, "ScalarValues::Integer")
        model.addUnownedElement(multiplicity, startOfOwnerPath = owner)
        return multiplicity
    }

    /**
     * Adds an implied Annotation between an annotating element and an element.
     */
    override fun addAnnotation(owner: AnnotatingElement, annotatedElement: Element): Annotation {
        val annotation = AnnotationImplementation(
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
            redefiningFeature = Resolved(ref = owner),
            redefinedFeature = Resolved(str = redefinedFeature)
        )
        model.addUnownedElement(element = redefinition, path = pathFromOwnerToRedefinedFeature, startOfOwnerPath = owner)
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
            "bySpecializations" -> return AstBySpecializations(model, namespace, param)
            "byParts" -> return AstByParts(model, namespace, param)
            "byImplements" -> return AstByImplements(model, namespace, param)
            "linear" -> return AstLinear(model, param)
            "stepInterpolation" -> return AstStepInterpolation(model,param)
            "ToReal" -> return AstReal(model, param)
            "ToInteger" -> return AstInteger(model, param)
            "norm" -> return AstNormalizeVector(model,param)
            "angle" -> return AstVectorAngle(model,param)
            "cityBlockDistance" -> return AstCityBlockDistance(model,param)
            "quantityOfVectorAtPosition" -> return AstQuantityOfVectorAtPosition(model,param)
            else -> return AstUserDefinedFunction(model,namespace, param, function)
        }
    }
}

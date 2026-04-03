package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.compiler.semantics.kerml.NamespaceActions
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.model.expression.AstNode
import com.github.tukcps.sysmd.model.expression.functions.*
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.model.util.QualifiedName
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
 * As the owner is not completely known, we maintain a stack of the owner's:```owners```.
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
open class ActionsContext(
    val model: Session,
    val compiler: KerML,
) {

    /**
     * A stack with allow nested namespaces.
     */
    private val owners: Stack<NamespaceActions<Namespace>> = Stack<NamespaceActions<Namespace>>()

    /**
     * The visibility of the top of the owners; only needed between prefixes are parsed and semantic action
     * is started.
     */
    var visibility: Import.VisibilityKind? = null

    /**
     * A set with the prefixes of the currently parsed definition or declaration.
     * Valid from start of prefixes being parsed and the first semantic actions are executed.
     */
    val prefixes = mutableSetOf<Token.Kind>()

    /**
     * Initializes the owner stack.
     * @param ownerPrefix a string with owners separated by '::'
     */
    fun initOwningNamespaces(ownerPrefix: String = "") {
        owners.clear()
        addOwningNamespaces(ownerPrefix)
    }

    /**
     * Adds multiple owning namespaces; needed for SysMD only.
     * @param ownerPrefix a string of owners separated by '::'
     */
    fun addOwningNamespaces(ownerPrefix: String = "") {
        val ownersPrefixes = ownerPrefix.split("::").filter { it.isNotBlank() }
        ownersPrefixes.forEach {
            if (it.isNotEmpty()) {
                val found = element<Namespace>().resolveLocal(it)?.memberElement
                val action = NamespaceActions<Namespace>(this, ::PackageImplementation)
                action.parse {
                    if (found != null)
                        currentActions()?.created = found as Namespace
                    else
                        create(Identification(name = it))
                }
                pushOwningNamespace(action)
            }
        }
    }


    /**
     * Pushes an owner to the stack of owning namespaces.
     * @param owner of new elements that will be created.
     */
    fun pushOwningNamespace(owner: NamespaceActions<Namespace>) {
        if (owner != model.global)
            owners.push(owner)
    }

    /**
     * Removes the top name from the owners' stack
     */
    fun popOwningNamespace() {
        owners.pop()
    }

    fun currentActions(): NamespaceActions<Namespace>?  = if (owners.isEmpty()) null else owners.peek()


    /**
     * @return the currently processed namespace
     */
    @Suppress("UNCHECKED_CAST")
    fun <T: Namespace> element(): T = (currentActions()?.created as T?) ?: (model.global as T)

    fun owner(): Namespace = if (owners.size-2 in owners.indices) owners[owners.size-2].created else model.global

    fun create(identification: Identification?) =
            currentActions()?.create(identification)

    /**
     * Adds the namespace prefix of a parse run to the name given as parameter.
     * @return ownerPrefix + name, considering "::" and formatting.
     */
    fun ownerName(): QualifiedName {
        var s = ""
        owners.forEach {
            if (it != model.global) {
                s += if (it != null) it.created.escapedName() + "::"
                else "${it}::"
            }
        }
        s = s.removeSuffix("::")
        return s
    }


    /**
     * Marker only for the generation of AST in Expressions.
     * ONLY FOR USE IN EXPRESSION !!!
     */
    var namespace: Namespace = model.global

    /**
     * Adds an owned unioning relationship.
     * @param type the name of the type, as in the source code
     */
    fun addUnioning(type: QualifiedName): Unioning {
        val owner = element<Type>()
        val unioning = UnioningImplementation(unionedType = owner, unioningType = UnresolvedType(type))
        model.addOwnedRelationship(unioning, owner)
        return unioning
    }


    /**
     * Adds an owned differencing relationship.
     * @param type the name of the type, as in the source code
     */
    fun addDifferencing(type: QualifiedName): Differencing {
        val owner = element<Type>()
        val differencing = DifferencingImplementation(typeDifferenced = owner, differencingType = UnresolvedType(type))
        model.addOwnedRelationship(differencing, owner)
        return differencing
    }

    /**
     * Adds an owned differencing relationship.
     * @param type the name of the type, as in the source code
     */
    fun addIntersecting(type: QualifiedName): Intersecting {
        val owner = element<Type>()
        val intersecting = IntersectingImplementation(typeIntercected = owner, intersectingType = UnresolvedType(type))
        model.addOwnedRelationship(intersecting, owner)
        return intersecting
    }

    /**
     * Adds an owned feature typing relationship.
     * @param type the name of the type, as in the source code
     */
    fun addDisjoining(type: QualifiedName): Disjoining {
        val owner = element<Type>()
        val disjoining = DisjoiningImplementation(typeDisjoined = owner, disjoiningType = UnresolvedType(type))
        model.addOwnedRelationship(disjoining, owner)
        return disjoining
    }


    /**
     * Adds owned multiplicity.
     * @param integerRange integerRange the range, by default 0 .. *
     */
    fun addMultiplicity(integerRange: IntegerRange): Multiplicity {
        val multiplicity = MultiplicityImplementation(multiplicity = "${integerRange.min} .. ${integerRange.max}")
        val owner = element<Type>()
        model.addOwnedMember(multiplicity, owner)

        // val featureTyping = FeatureTypingImplementation(typedFeature = multiplicity, type = UnresolvedType("ScalarValues::Integer"))
        // model.addOwnedRelationship(featureTyping, multiplicity)
        return multiplicity
    }

    /**
     * Adds a ReferenceSubsetting relationship.
     * @param owner the referencing feature's owner
     * @param referencedFeature qualified name of the referenced feature
     */
    fun addReferenceSubsetting(referencedFeature: QualifiedName) {
        val owner = element<Feature>()
        val reference = ReferenceSubsettingImplementation(
            referencingFeature = owner,
            referencedFeature = UnresolvedFeature(referencedFeature)
        )
        model.addOwnedRelationship(reference, owner)
    }


    /**
     * Adds a ReferenceSubsetting relationship.
     * @param referencedFeature the referencing feature that can also be unresolved
     */
    fun addReferenceSubsetting(referencedFeature: Feature) {
        val owner = element<Feature>()
        val reference = ReferenceSubsettingImplementation(
            referencingFeature = owner, referencedFeature = referencedFeature
        )
        model.addOwnedRelationship(reference, owner)
    }

    /**
     * Adds an owned Subclassification relationship
     * @param type the type for which owned subclassification is created
     */
    fun addSubclassification(type: String) {
        val owner = element<Type>()
        val subclassification = SubclassificationImplementation(
            subclassification = owner,
            superclassification = UnresolvedType(type)
        )
        model.addOwnedRelationship(subclassification, owner)
    }

    /**
     * Adds a Redefinition relationship.
     * @param owner the redefining feature and owner of the Redefinition
     * @param redefinedFeature
     */
    fun addRedefinition(redefinedFeature: QualifiedName) {
        val owner = element<Feature>()
        val redefinition = RedefinitionImplementation(
            redefiningFeature = owner,
            redefinedFeature = UnresolvedFeature(redefinedFeature)
        )
        model.addOwnedRelationship(redefinition, owner)
    }

    /**
     * Adds one or more owned Specializations to a Type
     * @param type a type name for which an owned Specialization is created
     */
    fun addSpecialization(type: QualifiedName) {
        val owner = element<Type>()
        val specialization = SpecializationImplementation(
            specific = owner,
            general = UnresolvedType(type)
        )
        model.addOwnedRelationship(specialization, owner)
    }

    fun addConjugation(conjugated: String) {
        val owner = element<Type>()
        val conjugation = ConjugationImplementation(
            type = owner,
            conjugated = UnresolvedType(conjugated)
        )
        model.addOwnedRelationship(conjugation, owner)
    }

    /**
     * Adds FeatureTyping elements to a created Feature.
     * The types are still names and will become references during initialization.
     * @param type a list of qualified names that shall be added.
     */
    fun addTyping(type: QualifiedName) {
        val owner = element<Feature>()
        val typing = FeatureTypingImplementation(
            typedFeature = owner,
            type = UnresolvedType(type)
        )
        model.addOwnedRelationship(typing, owner)
    }

    /**
     * Adds an owned subsetting.
     */
    fun addSubsetting(subsettedFeature: QualifiedName) {
        val owner = element<Feature>()
        val subsetting = SubsettingImplementation(
            subsettingFeature = owner,
            subsettedFeature = UnresolvedFeature(subsettedFeature)
        )
        model.addOwnedRelationship(subsetting, owner)
    }

    /**
     * Adds a reference subsetting to the feature
     * @param references, a single qualified name
     */
    fun addReferences(references: QualifiedName?) {
        if (references != null) {
            addReferenceSubsetting(referencedFeature = references)
        }
    }


    /**
     * Adds a sequence of redefinition
     */
    fun addRedefinitions(redefines: MutableList<String>) {
        if (element<Feature>().escapedName() == null)
            element<Feature>().declaredName = redefines.firstOrNull()
        redefines.forEach {
            addRedefinition(it)
        }
    }


    fun addUnitConstraint(unitConstraint: String?) {
        if (unitConstraint != null) {
            val constraint = FeatureImplementation()
            constraint.declaredName = "unit"
            constraint.expression = unitConstraint
            model.addOwnedMember(constraint, element<Feature>())
        }
    }

    fun addTypeConstraint(typeConstraint: MutableList<String>) {
        element<Feature>().typeConstraint = typeConstraint
        if (typeConstraint.isNotEmpty()) {
            val constraint = FeatureImplementation()
            constraint.declaredName = "range"
            constraint.expression = typeConstraint.firstOrNull()
            model.addOwnedMember(constraint, element<Feature>())
        }
    }

    fun addTarget(target: Element) {
        element<Connector>().target.add(target)
    }

    fun setSource(source: Element) {
        element<Connector>().source = mutableListOf(source)
    }

    fun setTarget(target: Element) {
        element<Connector>().target = mutableListOf(target)
    }

    fun setSourceEnd(source: Feature) { setSource(source) }
    fun addTargetEnd(target: Feature) { addTarget(target) }
    fun setTargetEnd(target: Feature) { setTarget(target) }


    fun directionFromPrefixes(): Feature.FeatureDirectionKind = when {
        Token.Kind.IN in prefixes -> Feature.FeatureDirectionKind.IN
        Token.Kind.OUT in prefixes -> Feature.FeatureDirectionKind.OUT
        Token.Kind.INOUT in prefixes -> Feature.FeatureDirectionKind.INOUT
        else -> Feature.FeatureDirectionKind.INOUT
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
    fun handleFunctionCall(function: QualifiedName, param: ArrayList<AstNode>, semantics: ActionsContext): AstNode {
        when (function) {
            "owns" -> return AstHasA(model, param, semantics)
            "ITE" -> return AstIte(model, param)
            "oneOf" -> return buildOneOfAst(model, param, semantics)
            "allOf" -> return AstAllOf(model, param)
            "anyOf" -> return AstAnyOf(model, param)
            "sum_i" -> return AstSumI(namespace, model, param)
            "sum" -> return AstSum(namespace, model, param)
            "sumOverParts" -> return AstSumOverParts(model, namespace, param, transitive = true)
            "sumOverSubclasses" -> return AstSumOverSubclasses(model, namespace, param, transitive = true)
            "productOverParts" -> return AstProductOverParts(model, namespace, param, transitive = true)
            "productOverSubclasses" -> return AstProductOverSubclasses(model, namespace, param, transitive = true)
            "sumOverPartsNotTransitive" -> return AstSumOverParts(model, namespace, param, transitive = false)
            "sumOverSubclassesNotTransitive" -> return AstSumOverSubclasses(model, namespace, param, transitive = false)
            "productOverPartsNotTransitive" -> return AstProductOverParts(model, namespace, param, transitive = false)
            "productOverSubclassesNotTransitive" -> return AstProductOverSubclasses(model, namespace, param, transitive = false)
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
            "linearInterpolation" -> return AstLinearInterpolation(model, param)
            "stepInterpolation" -> return AstStepInterpolation(model,param)
            "ToReal" -> return AstReal(model, param)
            "ToInteger" -> return AstInteger(model, param)
            "norm" -> return AstNormalizeVector(model,param)
            "size" -> return AstVectorSize(model, param)
            "angle" -> return AstVectorAngle(model,param)
            "cityBlockDistance" -> return AstCityBlockDistance(model,param)
            "quantityOfVectorAtPosition" -> return AstQuantityOfVectorAtPosition(model,param)
            else -> return AstUserDefinedFunction(model,namespace, param, function)
        }
    }
}

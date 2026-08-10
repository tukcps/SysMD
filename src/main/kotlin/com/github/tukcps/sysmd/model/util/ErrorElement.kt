package com.github.tukcps.sysmd.model.util

import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.session.Session
import kotlin.uuid.Uuid

/**
 * Fake element that replaces a wrongly typed element from compiler.
 * @param errorElement a wrongly typed element.
 */
class ErrorElement(override val model : Session, val errorElement: Element?) : Feature, OwningMembership {
    override val type: List<Type>
        get() = mutableListOf()
    override val typing: List<FeatureTyping>
        get() = mutableListOf()
    override val ownedTypeFeaturing: List<FeatureTyping>
        get() = mutableListOf()
    override var direction: Feature.FeatureDirectionKind?
        get() = if (errorElement is Feature) errorElement.direction else null
        set(value) {}

    /**
     * Getter and setter for the specified multiplicity; via
     * the owned Multiplicity element.
     */
    override val multiplicityRange: MultiplicityRange
        get() = if (errorElement is Feature) errorElement.multiplicityRange else MultiplicityRange(1,1 )

    /** Variable that is true if the feature constrains the source/target of a relationship.*/
    override var isEnd: Boolean
        get() = if (errorElement is Feature) errorElement.isEnd else false
        set(value) {}
    override var isComposite: Boolean
        get() = if (errorElement is Feature) errorElement.isComposite else false
        set(value) {}
    override var isPortion: Boolean
        get() = if (errorElement is Feature) errorElement.isPortion else false
        set(value) {}
    override var isUnique: Boolean
        get() = if (errorElement is Feature) errorElement.isUnique else false
        set(value) {}
    override var isOrdered: Boolean
        get() = if (errorElement is Feature) errorElement.isOrdered else false
        set(value) {}
    override var isDerived: Boolean
        get() = if (errorElement is Feature) errorElement.isDerived else false
        set(value) {}
    override var isReadOnly: Boolean
        get() = if (errorElement is Feature) errorElement.isReadOnly else false
        set(value) {}
    override var isConstant: Boolean
        get() = false
        set(value) {}
    override var isVariable: Boolean
        get() = false 
        set(value) {}

    /** True if the feature value was assigned with ':=' or 'default =', meaning it can be overridden in subtypes. */
    override var isDefaultValue: Boolean
        get() = if (errorElement is Feature) errorElement.isDefaultValue else false
        set(value) {}

    /** True if the feature value was assigned with ':=' or '=', meaning it's an initial value assignment. */
    override var isInitialValue: Boolean
        get() = if (errorElement is Feature) errorElement.isInitialValue else false
        set(value) {}

    /**
     * The name of the memberElement, relative to the membershipOwningNamespace
     */
    override val memberName: String?
        get() = if (errorElement is Membership) errorElement.memberName else null

    /**
     * The short name of the memberElement, relative to the membershipOwningNamespace
     */
    override val memberShortName: String?
        get() = if (errorElement is Membership) errorElement.memberShortName else null

    override var visibility: Import.VisibilityKind
        get() = if (errorElement is Membership) errorElement.visibility else Import.VisibilityKind.Private
        set(value) {}

    override fun clone() = ErrorElement(model, this)

    /**
     * Standard-extensions; string-level only.
     * Together, they are serialized as textual representation body
     */
    override val unitConstraint: String
        get() = TODO("Not yet implemented")
    override val typeConstraint: MutableList<String>
        get() = TODO("Not yet implemented")
    override var expression: String?
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * Features can be references that are represented by an implied owned
     * ReferenceSubsetting.
     */
    override val referencedFeature: Feature
        get() = TODO("Not yet implemented")

    /**
     * A reference to the variable in the constraint solver
     */
    override val variable: com.github.tukcps.sysmd.cspsolver.Variable
        get() = TODO("Not yet implemented")

    override var isAbstract: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override var isSufficient: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    /**
     * If this Type is conjugated, then return just the originalType of the Conjugation.
     * Otherwise, return the general Types from all ownedSpecializations of this type, and:
     * @param excludeImplied if excludeImplied = false, no, or all nonimplied ownedSpecializations, if excludeImplied = true
     */
    override fun supertypes(excludeImplied: Boolean): List<Type> = emptyList()

    /**
     * Return the public, protected and inherited Memberships of this Type.
     * @param excludedNamespaces excludes the given set of excludedNamespaces.
     * @param excludedTypes excludes Types in the given set of excludedTypes.
     * @param excludeImplied if true, then also exclude any supertypes from implied Specializations.
     */
    override fun nonPrivateMemberships(
        excludedNamespaces: Set<Namespace>,
        excludedTypes: Set<Type>,
        excludeImplied: Boolean
    ): List<Membership> {
        TODO("Not yet implemented")
    }

    /**
     * Returns all the non-private Memberships of all the supertypes of this Type,
     * excluding any supertypes that are this Type or are in the given set of excludedTypes.
     * @param excludeImplied If excludeImplied = true, then also transitively exclude any supertypes from implied Specializations.
     *     body: let excludingSelf : Set(Type) = excludedType->including(self) in
     *     supertypes(excludeImplied)->reject(t | excludingSelf->includes(t)).
     *     nonPrivateMemberships(excludedNamespaces, excludingSelf, excludeImplied)
     */
    override fun inheritableMemberships(
        excludedNamespaces: Set<Namespace>,
        excludedTypes: Set<Type>,
        excludeImplied: Boolean
    ): List<Membership> = emptyList()

    /**
     * Return the Memberships inheritable from supertypes of this Type with redefined Features removed.
     * When computing inheritable Memberships, exclude Imports of excludedNamespaces,
     * Specializations of excludedTypes.
     * @param excludedNamespaces to exclude imports
     * @param excludedTypes to exclude types to prevent cyclic recursion
     * @param excludeImplied excludes, if true, all implied Specializations.
     */
    override fun inheritedMemberships(
        excludedNamespaces: Set<Namespace>,
        excludedTypes: Set<Type>,
        excludeImplied: Boolean
    ): List<Membership> = mutableListOf<Membership>()

    /** All subtypes of this type after initialization */
    override val subtypes: MutableSet<Type>
        get() = mutableSetOf()

    override fun multiplicityRange(): MultiplicityRange {
        return MultiplicityRange(1,1)
    }

    /**
     * The indices in the input string during a parse run.
     * Start is the first token of the production, last one the start of a body.
     */
    override var indices: IntRange?
        get() = errorElement?.indices
        set(value) {}
    override var input: CharSequence?
        get() = errorElement?.input
        set(value) {}

    /**
     * Recursive search for the visible Memberships starting in this namespace.
     * A filter SAM permits to search for a particular membership.
     * @param excluded set of already searched namespaces, to prevent circular search
     * @param isRecursive
     * @param includeAll
     * @param filter lambda that allows filtering; by default true
     * @return Memberships of this namespace, filtered with filter.
     */
    override fun visibleMemberships(
        excluded: Set<Namespace>,
        isRecursive: Boolean,
        includeAll: Boolean,
        filter: Membership.() -> Boolean
    ): List<Membership> = emptyList()

    /**
     * Returns the imported memberships of this namespace.
     * A filter SAM permits to search for particular memberships, e.g., with a certain name.
     * @param excluded set of already searched namespaces, to prevent circular search
     * @param filter - lambda that allows filtering of the result; by default true
     * @return All memberships that are imported.
     */
    override fun importedMemberships(
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean
    ): List<Membership> {
        TODO("Not yet implemented")
    }

    /**
     * Returns the membership of owned memberships of owned and imported memberships of a given kind.
     * @param visibility Kind of visibility, all owned and imported if null.
     * @param excluded For recursion, allows excluding visited namespaces from recursion.
     */
    override fun membershipsOfVisibility(
        visibility: Import.VisibilityKind?,
        excluded: Set<Namespace>,
        filter: Membership.() -> Boolean
    ): List<Membership> {
        TODO("Not yet implemented")
    }

    /**
     * Resolves a qualified name to the respective membership, starting at the root namespace.
     * @param qualifiedName the qualified name
     * @return the membership of an element to which the parameter resolves
     */
    override fun resolveGlobal(qualifiedName: QualifiedName): Membership? {
        TODO("Not yet implemented")
    }

    /**
     * Resolves a simple name to a membership, searching only in the local scope.
     * @param name a name that is resolved locally
     * @return the membership of an element to which the parameter resolves
     */
    override fun resolveLocal(name: SimpleName): Membership? = null

    /**
     * A unique id of the Element that remains unchained over the whole lifecycle of the element.
     * We use UUIDv4 as suggested in SysMLv2 Std, except for library elements for which we use UUIDv5.
     */
    override var elementId: Uuid
        get() = errorElement?.elementId ?: Uuid.NIL
        set(value) {}
    /** Tool-specific ids; not used */
    override var aliasIds: Collection<String>
        get() = emptyList()
        set(value) {}

    /**
     * fields for identification by name, shortName.
     */
    override var declaredName: SimpleName?
        get() = errorElement?.declaredName
        set(value) {}
    override var declaredShortName: SimpleName?
        get() = errorElement?.declaredShortName
        set(value) {}

    /**
     * Derived properties via getters of the field: name, shortName, effectiveName
     * They consider the effective name and re-definition of the declared name
     */
    override val name: SimpleName
        get() = errorElement?.name?: ""
    override val shortName: SimpleName
        get() = errorElement?.shortName?: ""

    override fun effectiveName(): String?  {
        return errorElement?.effectiveName()
    }

    override fun effectiveShortName(): String? {
        return errorElement?.effectiveShortName()
    }

    /** The name, or short name, if necessary in ticks and with escape sequences */
    override fun escapedName(): String? = errorElement?.escapedName()

    /** Path from root namespace to this element */
    override val qualifiedName: QualifiedName
        get() = errorElement?.qualifiedName?:"ErrorElement"

    /**
     * Path including unnamed elements to this element.
     */
    override fun path(): String {
        return errorElement?.path()?:"(Missing reference)"
    }

    /**
     * Needed for the path
     */
    override fun positionOf(element: Element): Int? {
        return errorElement?.positionOf(element)
    }

    /**
     * Reified Relationships from which owner, owningNamespace, etc. are derived.
     * Contains reified relationships that relate the element with its owned elements.
     */
    override var ownedRelationship: MutableList<Relationship>
        get() = mutableListOf()
        set(value) {}
    /** Reified Relationship from which owner and the below properties are derived. */
    override var owningRelationship: OwningMembership?
        get() = TODO("Not yet implemented")
        set(value) {}
    override val owningNamespace: Namespace
        get() = TODO("Not yet implemented")
    override val standardNamespace: Namespace
        get() = TODO("Not yet implemented")
    /** Whether the element is from SysML or KerML libraries, these have UUID type 5, not 4 */
    override var isLibraryElement: Boolean
        get() = false
        set(value) {}
    override var isStandard: Boolean
        get() = false
        set(value) {}
    /** Whether (all) implied relationships are included or not */
    override var isImpliedIncluded: Boolean
        get() = false
        set(value) {}

    /**
     * Is true for elements that are added, but are not subject to persistence and/or exchange of
     * entities.
     */
    override var isTransient: Boolean
        get() = false
        set(value) {}

    /**
     * The textual representation that created this element
     */
    override var textualRepresentation: MutableList<TextualRepresentation>
        get() = mutableListOf()
        set(value) {}

    /**
     * Related documentation, i.e., the notebook cells before the elements textual representation.
     */
    override var documentation: MutableList<Documentation>
        get() = mutableListOf()
        set(value) {}

    /** The sources the relationship */
    override var source: MutableList<Element>
        get() = mutableListOf()
        set(value) {}

    /** The targets of the relationship */
    override var target: MutableList<Element>
        get() = mutableListOf()
        set(value) {}

    /** The owning element of this relationship */
    override var owningRelatedElement: Element
        get() = errorElement?.owningNamespace ?: model.global
        set(value) {}

    /** The owned elements */
    override var ownedRelatedElement: MutableList<Element>
        get() = mutableListOf()
        set(value) {}

    override var isImplied: Boolean
        get() = false
        set(value) {}

    override fun updateFrom(template: Element) {}

    /**
     * is true if the element has been updated in evaluate/update cycles of constraint propagation
     * mechanisms etc.; prior to its use, it shall be set to false.
     */
    override var updated: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    /** Is true if the element has been changed w.r.t. the last commit and needs to be included in a new commit */
    override var hasBeenChanged: Boolean
        get() = false
        set(value) {}
}
package com.github.tukcps.sysmd.compiler.parser.util

import com.github.tukcps.sysmd.compiler.parser.util.DataModelWithEdges.EdgeKind.*
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.datamodel.IdentifiedByName
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import java.util.*
import kotlin.reflect.KProperty1
import kotlin.uuid.Uuid

/** Data model extended with the edges of the ownership graph.
 */
class DataModelWithEdges(val elements : List<ElementData>)
{
    sealed class VirtualElement
    {
        abstract val name : String?
        open fun isType(type : ElementType) : Boolean = false
        fun isNotType(type : ElementType) : Boolean = ! isType(type)
    }
    /** Can only ever be referenced by null IDs */
    data object NoElement : VirtualElement()
    {
        override val name = null
        override fun isType(type: ElementType): Boolean = type isSupertypeOf ElementType.Namespace // for RootNamespace
    }

    /** Element with missing Uuid */
    data class MissingElement(val id : Uuid) : VirtualElement()
    {
        override val name = null
    }
    /** Element referenced by name */
    data class NamedElement(override val name: String) : VirtualElement()
    {
        override fun isType(type: ElementType): Boolean = type isSupertypeOf ElementType.Namespace // for implicit owners
    }

    /** An element that was actually present in the model, including the case were multiple elements shared a UUID. */
    sealed class FoundElement : VirtualElement()
    {
        abstract val elements : List<ElementData>
        abstract val id : Uuid
    }
    data class ActualElement(val element : ElementData) : FoundElement()
    {
        override val elements : List<ElementData> = listOf(element)
        override val id = element.elementId
        override val name = element.declaredName ?: element.declaredShortName

        override fun isType(type: ElementType): Boolean = elements.any { it.type isSubtypeOf type }
    }
    data class AmbiguousElement(override val elements : List<ElementData>) : FoundElement()
    {
        init { require(elements.size > 1) }

        override val id: Uuid = elements.first().elementId
        val unpacked get() = elements.map { ActualElement(it) }
        override val name = null
    }

    enum class EdgeKind(
        /** If true, this edge implies that [Edge.from] owns [Edge.to] */
        val isOwnership : Boolean = false,
        /** If true, [Edge.from] must be a Relationship */
        val fromIsRelationship : Boolean = false,
        /** If true, [Edge.to] must be a Relationship */
        val toIsRelationship : Boolean = false
    ) {
        /** An edge implied by ownedRelationship <-> owningRelatedElement */
        OWNED_RELATIONSHIP(isOwnership = true, toIsRelationship = true),
        /** An edge implied by ownedRelatedElement <-> owningRelationship */
        OWNED_RELATED_ELEMENT(isOwnership = true, fromIsRelationship = true),
        /** A non-owning edge implied by relatedElement */
        TARGET(fromIsRelationship = true),

        /** An inconsistent edge. */
        INCONSISTENT(isOwnership = true)
    }

    /** An edge in the ownership graph.
     * Note that an edge exists e.g. between an element and one of its owned relationships,
     * but not between an element and its ownedElement, as these are transitively owned via a relationship.
     * */
    data class Edge(
        val from : VirtualElement, val to : VirtualElement,
        var kind : EdgeKind,
        /** Describes any problems with this edge */
        val error : MutableList<String>,
        var circular : Boolean = false,
        /** If true, [to] is owned by another edge that was earlier in model order.
         * Always false if [kind] is not an ownership relation.
         */
        var superfluous : Boolean = false,
    )

    /** Traces a path through the model. Includes both relationships and non-relationship elements
     * @param head The element this path leads to
     * @param owningRelationship Path to [head]'s owningRelationship, if any
     * @param owningRelatedElement Path to [head]'s owningRelatedElement, if any
     */
    sealed class Path(
        val head : VirtualElement,
        val owningRelationship : Path?,
        val owningRelatedElement : Path?
    ) {
        val isRootNamespace = head === NoElement
        abstract val isTopLevel : Boolean
        open val isStandardLibrary : Boolean = head is FoundElement && head.elements.any {
            it.isStandard == true && it.type isSubtypeOf ElementType.LibraryPackage
        }

        abstract val qualifiedName : String?
        abstract val path : String

        /** must be separated from [next] because sealed classes may not be inner  */
        protected fun DataModelWithEdges.nextImpl() : List<PathHead>
        = (edgesFrom[head] ?: emptyList()).mapIndexedNotNull { ix, edge ->
            if(!edge.kind.isOwnership || edge.circular || edge.superfluous)
                return@mapIndexedNotNull null

            PathHead(prefix = this@Path, edge = edge, index = ix)
        }

        /** All paths that have this path as [PathHead.prefix] (i.e. traverses breadth-first)
         * Cached to prevent re-computing [qualifiedName] and [path] (but lazy)
         */
        abstract val next : List<PathHead>

        val id : Uuid? = when(head) {
            is FoundElement -> head.id
            is MissingElement -> head.id
            is NamedElement -> null
            NoElement -> null
        }

        val owner = owningRelationship?.owningRelatedElement

        val membershipOwningNamespace = when {
            owningRelatedElement === null -> null
            head.isNotType(ElementType.Membership) -> null
            owningRelatedElement.isRootNamespace -> owningRelatedElement
            owningRelatedElement.head.isType(ElementType.Namespace) -> owningRelatedElement
            else -> null
        }

        val owningNamespace = owningRelationship?.membershipOwningNamespace


        override fun toString(): String = "Path($qualifiedName at $path)"
    }

    /** The root of a path.
     * [head] should be either [implicitOwner], or an unresolved reference i.e. [MissingElement] or [NamedElement]
     */
    inner class PathRoot(head : VirtualElement) : Path(head, null, null) {
        override val qualifiedName : String? = head.name
        override val path: String = qualifiedName ?: ""
        override val next: List<PathHead> by lazy { nextImpl() }
        override val isTopLevel : Boolean = false
    }

    /** A path within the model. Does not distinguish between elements and relationships.
     * Omits [NoElement] if it is the path root
     * @param prefix The prefix of this path, leading to immediate owner of [head].
     *              Not to be confused with the semantics of [ElementData.owner], for which relationships are transparent.
     * @param index 0-based index of [head] within [prefix]
     */
    inner class PathHead(
        val prefix : Path,
        val edge : Edge,
        val index : Int
    ) : Path(
        edge.to,
        owningRelationship = when {
            edge.kind == OWNED_RELATED_ELEMENT -> prefix
            else -> null
        },
        owningRelatedElement = when {
            edge.kind == OWNED_RELATIONSHIP -> prefix
            else -> null
        }
    ) {
        init {
            assert(prefix.head === edge.from)
        }

        override val isStandardLibrary: Boolean = prefix.isStandardLibrary || super.isStandardLibrary
        override val isTopLevel: Boolean = prefix.isRootNamespace || owningNamespace?.isRootNamespace == true

        override val qualifiedName: String? = when {
            head.name === null -> null
            // standard handled duplicate names here, we want to propagate them further
            isTopLevel -> head.name // fixme: shouldn't this start with "$::"?
            owningNamespace?.qualifiedName === null -> null
            else -> "${owningNamespace.qualifiedName}::${head.name}"
        }

        /** The path() implementation for base elements */
        private fun elementPath() : String = when {
            qualifiedName !== null -> qualifiedName
            // owningRelationship <> null
            edge.kind == OWNED_RELATED_ELEMENT -> "${prefix.path}/${index + 1}"
            else -> ""
        }

        /** The path() specialization for owningMembership subtypes */
        private fun relationshipPath() : String = when {
            // owningRelationship = null and owningRelatedElement <> null
            edge.kind == OWNED_RELATIONSHIP -> "${prefix.path}/${index + 1}"
            else -> elementPath()
        }

        /** The path() specialization for owningMembership subtypes */
        private fun owningMembershipPath() : String
            = next.singleOrNull()?.qualifiedName?.plus("/owningMembership") ?: relationshipPath()

        // lazy because otherwise memberships would eagerly span the entire subtree
        override val path: String by lazy { when {
            head.isType(ElementType.OwningMembership) -> owningMembershipPath()
            head.isType(ElementType.Relationship) -> relationshipPath()
            else -> elementPath()
        } }


        override val next: List<PathHead> by lazy { nextImpl() }
    }

    /** Treat elements with duplicated IDs as one unit */
    val deduplicated = elements.groupBy { it.elementId }.mapValues { (_,v) ->
        v.singleOrNull()?.let { ActualElement(it) } ?: AmbiguousElement(v)
    }
    private val missing = HashMap<Uuid, MissingElement>()
    private val named = HashMap<String, NamedElement>()
    /** True if [get] has ever returned [NoElement].
     * Used hackily during init, to tell if [NoElement] is referenced anywhere in the model.
     */
    private var hasRootNamespace = false

    val edges : List<Edge>
    private val edgesTo : Map<VirtualElement, List<Edge>>
    private val edgesFrom : Map<VirtualElement, List<Edge>>
    val virtualElements : Set<VirtualElement>

    val roots : List<VirtualElement>

    fun edgesOwning(e : VirtualElement)  = edgesTo[e]?.filter { it.kind.isOwnership && !it.circular } ?: emptyList()
    fun edgesOwnedBy(e : VirtualElement) = edgesFrom[e]?.filter { it.kind.isOwnership && !it.circular } ?: emptyList()

    fun get(e : ElementData) = deduplicated[e.elementId]!!

    fun get(id : Uuid) : VirtualElement
        = deduplicated[id] ?: missing.computeIfAbsent(id, ::MissingElement)

    fun get(id : Identified?) : VirtualElement = when(id) {
        null -> {
            hasRootNamespace = true
            NoElement
        }
        is IdentifiedByName -> named.computeIfAbsent(id.name, ::NamedElement)
        // Do not distinguish `Identified(null)` from `null`
        else if id.id === null -> {
            hasRootNamespace = true
            NoElement
        }
        else -> get(id.id!!)
    }

    operator fun List<Identified?>.contains(x : VirtualElement) = any { get(it) == x }

    /** Lifts an [ElementData] property to [VirtualElement] */
    private fun FoundElement.relatedBy(p : KProperty1<ElementData, Identified?>) : Set<VirtualElement>
            = elements.mapNotNull { p.get(it) }.map(::get).toSet()
    @JvmName("relatedByMany")
    private fun FoundElement.relatedBy(p : KProperty1<ElementData, List<Identified?>?>) : Set<VirtualElement>
            = elements.flatMap { p.get(it) ?: emptyList() }.map(::get).toSet()

    // these are the only fields relevant for reconstructing ownership. Other fields are derived.
    val FoundElement.owningRelatedElement get() = relatedBy(ElementData::owningRelatedElement)
    val FoundElement.owningRelationship get() = relatedBy(ElementData::owningRelationship)
    val FoundElement.ownedRelatedElement get() = relatedBy(ElementData::ownedRelatedElement)
    val FoundElement.ownedRelationship get() = relatedBy(ElementData::ownedRelationship)

    val rootPaths get() = roots.map { PathRoot(it) }

    /** Performs depth-first search over all paths in the model.
     * (!) may revisit paths on ambiguous ownerships
     * Within a level, leaves are visited in model order
     */
    inline fun dfs(visit : (Path) -> Unit) = dfs(from = rootPaths, visit)

    inline fun dfs(from : List<Path>, visit : (Path) -> Unit)
    {
        val todo = from.toMutableList() // a stack is DFS, a queue would be BFS

        while(todo.isNotEmpty())
        {
            val cur = todo.removeFirst()
            visit(cur)

            todo.addAll(0, cur.next)
        }
    }

    fun dfs() = sequence {
        dfs { yield(it) }
    }

    /** Record that holds context for each cycle search generation
     * @param cycles All cycles that were found
     * @param shouldVisit Set of elements that should be considered in the current iteration
     * @param didVisit Subset of [shouldVisit] that receives every element the current iteration actually visited
     * @param start The element that this search started at
     */
    private data class CycleGeneration(
        val start : VirtualElement,
        val shouldVisit : Set<VirtualElement>,
        val cycles : MutableList<List<Edge>> = mutableListOf(),
        val didVisit : MutableSet<VirtualElement> = HashSet<VirtualElement>(shouldVisit.size).also {
            it.add(start)
        },
    )

    private fun CycleGeneration.traceCycle(trace : List<Edge>)
    {
        val head = if(trace.isEmpty()) start else trace.last().from

        for(e in edgesOwning(head))
        {
            if(e.from !in shouldVisit)
                continue

            val wasNew = didVisit.add(e.from)

            if(! wasNew)
            {
                val ix = trace.indexOfFirst {
                    it.to == e.from
                }

                if(ix >= 0)
                {
                    cycles.add(trace.drop(ix) + e)
                    continue
                }
            }

            traceCycle(trace + e)
        }
    }

    /** Finds all cycles in the model and cuts them by setting [Edge.circular]. */
    private fun fixCycles()
    {
        val notVisited = HashSet(virtualElements)

        while(notVisited.isNotEmpty())
        {
            val curGen = CycleGeneration(
                start = notVisited.first(),
                shouldVisit = notVisited,
            )

            assert(curGen.didVisit.isNotEmpty())
            curGen.traceCycle(emptyList())
            notVisited -= curGen.didVisit

            for(cycle in curGen.cycles)
            {
                if(cycle.any { it.circular })
                    // already resolved by cutting another cycle
                    continue

                // FIXME: make this deterministic across isomorphic graphs (aka independent compile runs of same code)
                // heuristic: cut edge that creates the highest in-degree
                val toCut = cycle.maxBy { e -> edgesOwning(e.to).size }

                toCut.circular = true
            }
        }

    }

    init {
        val edges = mutableListOf<Edge>()
        val inconsistencyError = "relationship/element inconsistency"

        // 1. pass: collect owned* entries
        for(owner in deduplicated.values)
        {
            val ownedRelationships = owner.ownedRelationship
            val ownedRelatedElements = owner.ownedRelatedElement

            for(owned in (ownedRelationships union ownedRelatedElements))
            {
                val kind = when(owned) {
                    !in ownedRelationships -> OWNED_RELATED_ELEMENT
                    !in ownedRelatedElements -> OWNED_RELATIONSHIP
                    else -> INCONSISTENT // must be in both
                }

                @Suppress("KotlinConstantConditions") // complains about TARGET, but doesn't allow removing it either...
                edges += Edge(from = owner, to = owned, kind, mutableListOf<String>().apply {
                    when(kind) {
                        INCONSISTENT -> add(inconsistencyError)
                        // check for reciprocating field
                        else if owned !is FoundElement -> {}
                        OWNED_RELATIONSHIP    -> if(owner !in owned.owningRelatedElement) add("is not owningRelatedElement")
                        OWNED_RELATED_ELEMENT -> if(owner !in owned.owningRelationship) add("is not owningRelationship")
                        TARGET -> throw IllegalStateException("unreachable")
                    }
                })
            }

            // bonus: collect non-owned relationship targets
            for(target in owner.relatedBy(ElementData::target) - ownedRelationships - ownedRelatedElements)
                edges += Edge(from = owner, to = target, TARGET, mutableListOf())
        }

        // 2. pass: collect dangling owning* entries
        for(owned in deduplicated.values)
        {
            val owningRelationships = owned.owningRelationship
            val owningRelatedElements = owned.owningRelatedElement
            val ambiguous = (owningRelationships.size + owningRelatedElements.size) > 1

            for(owner in owningRelationships union owningRelatedElements)
            {
                val kind = when(owner) {
                    !in owningRelationships -> OWNED_RELATIONSHIP
                    !in owningRelatedElements -> OWNED_RELATED_ELEMENT
                    else -> INCONSISTENT
                }

                // caches not populated yet
                val presentEdge = edges.filter { it.kind.isOwnership && it.from == owner && it.to == owned }

                if(presentEdge.isEmpty())
                {
                    @Suppress("KotlinConstantConditions") // complains about TARGET, but doesn't allow removing it either...
                    edges += Edge(from = owner, to = owned, kind, mutableListOf<String>().apply {
                        when(kind) {
                            INCONSISTENT -> add(inconsistencyError)
                            else if owner !is FoundElement -> {}
                            OWNED_RELATIONSHIP -> add("not in ownedRelationship")
                            OWNED_RELATED_ELEMENT -> add("not in ownedRelatedElement")
                            TARGET -> throw IllegalStateException("unreachable")
                        }
                    })
                }
                // check that all edges agree on the kind of ownership
                else if(presentEdge.any { it.kind != kind })
                {
                    presentEdge.forEach {
                        it.kind = INCONSISTENT
                        it.error.add(inconsistencyError)
                    }
                }
            }

            if(ambiguous)
            {
                edges.filter { it.to == owned && it.kind.isOwnership }.forEachIndexed { ix, edge ->
                    edge.error.add("ambiguous ownership")
                    edge.superfluous = ix > 0
                }
            }
        }

        // initialize caches
        this.edges = edges
        val edgesTo = IdentityHashMap<VirtualElement, MutableList<Edge>>()
        val edgesFrom = IdentityHashMap<VirtualElement, MutableList<Edge>>()

        fun<K,V> MutableMap<K,MutableList<V>>.push(k : K, v : V) {
            compute(k) { _,cur ->
                if(cur !== null)
                {
                    cur.add(v)
                    cur
                }
                else
                    mutableListOf(v)
            }
        }

        for(edge in edges)
        {
            edgesFrom.push(edge.from, edge)
            edgesTo.push(edge.to, edge)
        }

        this.edgesTo = edgesTo
        this.edgesFrom = edgesFrom

        this.virtualElements = HashSet<VirtualElement>(1 + deduplicated.size + missing.size + named.size).apply {
            addAll(deduplicated.values)
            addAll(missing.values)
            addAll(named.values)

            if(hasRootNamespace)
                add(NoElement)
        }

        // 3.pass: find roots and detect cycles
        fixCycles()
        // afterward, source elements are root elements (when excluding "cut" edges)
        this.roots = virtualElements.filter { e ->
            edgesOwning(e).isEmpty() && (e is FoundElement || edgesOwnedBy(e).isNotEmpty())
        }
    }

    private data class Indent(val left : Indent?, val last : Boolean, val error : Boolean, val ownership : Boolean)
    {
        fun print(to : Appendable, rightmost : Boolean = true)
        {
            left?.print(to, rightmost = false)
            to.append(when {
                rightmost -> if(last) "└─" else "├─"
                else      -> if(last) "  " else "│ "
            })
            to.append(when {
                !ownership -> '('
                !rightmost -> ' '
                error      -> '!'
                else       -> ' '
            })
        }

        val lineEnd = if(ownership) "" else ")"
    }

    /** prints only an element description, minus indent or newline, to [to] */
    private fun printSingle(to : Appendable, head : VirtualElement, error : List<String> = emptyList())
    {
        when(head)
        {
            is AmbiguousElement -> throw IllegalStateException("Handled earlier")
            is ActualElement -> {
                with(head.element)
                {
                    to.append(elementId.toString())
                    to.append(" [", type.name, "]")
                    declaredName?.let { to.append(" '", it, "'") }
                    declaredShortName?.let { to.append(" <", it, ">") }
                    literalBooleanValue?.let { to.append(" ", it.toString()) }
                    literalStringValue?.let {
                        val q = if(isNameLiteral == true) "`" else "\""
                        to.append(" ", q, it, q)
                    }
                    literalIntegerValue?.let { to.append(" ", it.toString()) }
                    literalRationalValue?.let {
                        val s = it.toString()
                        to.append(" ", s, if('.' in s) "" else ".0")
                    }
                    functionName?.let { to.append(" ", it, "(…)") }
                }

            }
            is MissingElement -> to.append(head.id.toString(), " !missing")
            is NamedElement -> to.append("\"", head.name, "\"")
            NoElement -> to.append("(no element)")
        }

        error.forEach {
            to.append(" !", it)
        }

    }

    private inline fun<T> Iterable<T>.markLast(f : (last : Boolean, it : T) -> Unit)
    {
        val iter = iterator()

        while(iter.hasNext())
        {
            val cur = iter.next()
            f(!iter.hasNext(), cur)
        }
    }

    /** Prints full lines to [to] including owned elements */
    private fun print(to : Appendable, edge : Edge, indent : Indent?)
    {
        val head = edge.to

        if(head is AmbiguousElement) // unpack before printing
        {
            head.unpacked.markLast { last, it ->
                if(indent !== null)
                    indent.copy(last = last && indent.last).print(to)
                val e = listOf("duplicate") + if(last) edge.error else emptyList()
                printSingle(to, it, e)
                to.appendLine(indent?.lineEnd ?: "")
            }
        }
        else
        {
            indent?.print(to)
            printSingle(to, head, edge.error)
            to.appendLine(indent?.lineEnd ?: "")
        }

        if(edge.kind.isOwnership && !edge.circular && !edge.superfluous)
        {
            edgesFrom[head]?.markLast { last, e ->
                print(to, e, Indent(indent, last, e.error.isNotEmpty(), e.kind.isOwnership))
            }
        }
    }

    fun print(to : Appendable)
    {
        for(r in roots)
        {
            // use an imaginary edge since only owned is printed
            print(to, Edge(NoElement, r, OWNED_RELATED_ELEMENT, mutableListOf()), null)
        }
    }

    fun print(from : Uuid, to : Appendable)
    {
        print(to, Edge(NoElement, get(from), OWNED_RELATED_ELEMENT, mutableListOf()), null)
    }

    override fun toString(): String = buildString { print(this) }
}
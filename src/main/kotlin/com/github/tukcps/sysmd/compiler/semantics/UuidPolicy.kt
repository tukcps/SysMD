package com.github.tukcps.sysmd.compiler.semantics

import com.github.tukcps.sysmd.compiler.parser.util.DataModelWithEdges
import com.github.tukcps.sysmd.compiler.parser.util.DataModelWithEdges.Path
import com.github.tukcps.sysmd.compiler.semantics.UuidPolicies.SYSML_ROOT_NAMESPACE
import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.datamodel.IdentifiedImplementation
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.model.util.QualifiedName
import com.github.tukcps.sysmd.model.util.mapInPlace
import com.github.tukcps.sysmd.rest.entities.api.entities.Identified
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.reflect.KMutableProperty0
import kotlin.uuid.Uuid

fun interface UuidPolicy {
	/** Assigns IDs for [root], and all elements (transitively) owned by it. */
	fun assignUuids(root : Path, model : DataModelWithEdges, changes : HashMap<Uuid, Uuid>)
}

/** Recursively calls [assignUuids] to assign Uuids for elements owned by [root] but NOT [root] itself.
 * Skips elements that are doubly owned (i.e. in invalid models)
 * For use inside [assignUuids] implementations when no special logic is needed.
 */
fun UuidPolicy.recurse(root : Path, model : DataModelWithEdges, changes : HashMap<Uuid, Uuid>)
{
	for(p in root.next)
	{
		if(p.id !in changes)
			assignUuids(p, model, changes)
	}
}

/** Subtype of [UuidPolicy] that only considers singular elements instead of whole (sub-)models. */
fun interface LocalUuidPolicy : UuidPolicy {
	/** Assigns a Uuid for the given element.
	 * Its elementId field will already have been initialized with a random (type 4) Uuid.
	 * Should not mutate elements directly.
	 * @param path The ownership path to this element
	 * @return The new Uuid for [path]'s headElement, or null to keep its random Uuid
	 */
	fun mapUuid(path : Path) : Uuid?

	override fun assignUuids(root : Path, model : DataModelWithEdges, changes : HashMap<Uuid, Uuid>)
	{
		// bottom-up
		recurse(root, model, changes)
		assignUuid(root, changes)
	}
}

fun LocalUuidPolicy.assignUuid(path : Path, changes : HashMap<Uuid, Uuid>)
{
	if(path.id in changes)
		return // skip doubly-owned element

	val new = mapUuid(path) ?: return
	val old = path.id ?: return
	assert(new !in changes) { "Remapped ID collision" }
	changes[old] = new
}

fun UuidPolicy.fixIDs(elements : List<ElementData>)
{
	fixIDs(DataModelWithEdges(elements))
}

fun UuidPolicy.fixIDs(model : DataModelWithEdges)
{
	// stores updated Uuids
	val changes = HashMap<Uuid, Uuid>()

	for(p in model.rootPaths)
		assignUuids(p, model, changes)

	fun fix(refs : MutableList<Identified>?) {
		if(refs === null)
			return

		refs.mapInPlace { ref ->
			ref.id?.let(changes::get)?.let { IdentifiedImplementation(it) } ?: ref
		}
	}

	fun fix(prop : KMutableProperty0<Identified?>) {
		val cur = prop.get()?.id ?: return

		changes[cur]?.let {
			prop.set(IdentifiedImplementation(it))
		}
	}

	if(changes.isNotEmpty()) for(element in model.elements) {
		changes[element.elementId]?.let {
			element.elementId = it
		}

		fix(element.ownedElement)
		fix(element::owningNamespace)
		fix(element::owner)

		fix(element.ownedRelationship)
		fix(element.ownedRelatedElement)
		fix(element::owningRelatedElement)
		fix(element.source)
		fix(element.target)

		fix(element::owningMembership)
		fix(element::owningRelationship)
	}
}

object UuidPolicies {
	/** Assigns random (type 4) Uuids to all elements
	 * (!) NOT standard compliant on library elements
	 */
	object RandomUuids : LocalUuidPolicy {
		override fun mapUuid(path: Path) = null

		override fun assignUuids(root: Path, model: DataModelWithEdges, changes: HashMap<Uuid, Uuid>) {}
	}
	/** Assigns a path-based (type 5) Uuid with the given [namespace] to each element. */
	class PathBasedUuids(val namespace : Uuid) : LocalUuidPolicy
	{
		override fun mapUuid(path: Path): Uuid = uuid5(path.path, namespace)
	}

	/** Ensures top-level standard libraries have the correct Uuids per standard.
	 * Leaves memberships to those libraries unmodified, UNLESS [fallback] is a [LocalUuidPolicy], in which case it is applied.
	 * @param fallback Applied to non-standard-library elements
	 * */
	class StandardLibraryUuids(val fallback : UuidPolicy) : UuidPolicy
	{
		override fun assignUuids(root: Path, model: DataModelWithEdges, changes: HashMap<Uuid, Uuid>) = when {
			root.isRootNamespace
				-> recurse(root, model, changes)
			!root.isTopLevel
				-> fallback.assignUuids(root, model, changes)
			root.isStandardLibrary && root.head.name !== null -> {
				val libID = uuid5("https://www.omg.org/spec/KerML/${root.head.name}", DNS_NAMESPACE)

				changes[root.id!!] = libID
				// continue with path-based Uuids
				PathBasedUuids(libID).recurse(root, model, changes)
			}

			root.head.isType(ElementType.OwningMembership) -> {
				recurse(root, model, changes)

				if (fallback is LocalUuidPolicy)
					fallback.assignUuid(root, changes)

				Unit
			}

			else -> fallback.assignUuids(root, model, changes)
		}
	}

	val DNS_NAMESPACE: Uuid = Uuid.parse("6ba7b810-9dad-11d1-80b4-00c04fd430c8")
	val SYSML_ROOT_NAMESPACE: Uuid = uuid5("https://omg.org", DNS_NAMESPACE)
	// FIXME: Decide on the correct value here
	val SYSMD_NOTEBOOK_ROOT_NAMESPACE: Uuid = uuid5("https://cps.cs.rptu.de/", DNS_NAMESPACE)

	/**
	 * Generates a Version 5 UUID (Name-based using SHA-1 hashing) based on a namespace and a name.
	 *
	 * @param name The string name to hash within the namespace.
	 * @param ns The namespace UUID to use as the anchor, defaults to [SYSML_ROOT_NAMESPACE].
	 * @return A [Uuid] representing the generated Version 5 UUID.
	 */
	fun uuid5(name: String, ns: Uuid = SYSML_ROOT_NAMESPACE): Uuid {
		// 1. Extract namespace bits and write them into a 16-byte array
		val nsBytes = ns.toLongs { msb, lsb ->
			ByteBuffer.allocate(16)
				.putLong(msb)
				.putLong(lsb)
				.array()
		}

		// 2. Compute the SHA-1 hash over the namespace bytes concatenated with the name bytes
		val hash = MessageDigest.getInstance("SHA-1").digest(nsBytes + name.toByteArray(Charsets.UTF_8))

		// 3. Set the UUID version to 5 (Name-based SHA-1)
		hash[6] = (hash[6].toInt() and 0x0f or 0x50).toByte()
		// 4. Set the UUID variant to IETF (RFC 4122 / RFC 9562)
		hash[8] = (hash[8].toInt() and 0x3f or 0x80).toByte()

		// 5. Convert the first 16 bytes of the hash into two Long values and create the Uuid
		return ByteBuffer.wrap(hash).let {
			Uuid.fromLongs(it.long, it.long)
		}
	}

	/** Computes the ID of a _standard library_ element by its path */
	fun libraryUuid5(path : QualifiedName) : Uuid {
		if(path.isEmpty())
			return uuid5("Global") // empty path -> global namespace

		val topLevelPkg = path.split("::", "/").first()
		val libID = uuid5("https://www.omg.org/spec/KerML/$topLevelPkg", DNS_NAMESPACE)

		return if(path == topLevelPkg) libID else uuid5(path, libID)
	}

	/** The UUIDv5 assigned to an element in a regular notebook context */
	fun notebookUuid5(path : QualifiedName) : Uuid = uuid5(path, SYSMD_NOTEBOOK_ROOT_NAMESPACE)


	/** Assigns a path-based (type 5) Uuid to library elements, and random (type 4) Uuids to other elements
	 * SysML-API standard compliant.
	 */
	val LegacySysMD = StandardLibraryUuids(fallback = RandomUuids)

	/** Assigns a path-based (type 5) Uuid to every element.
	 * Picks the SysML root namespace for library/standard elements,
	 * and the SysMD Notebook root namespace for all other elements.
	 *
	 * (!) In the latter case, not all IDs are assigned by their paths, but will be stable on re-compiling.
	 */
	val NewSysMD = StandardLibraryUuids(
		fallback = PathBasedUuids(SYSMD_NOTEBOOK_ROOT_NAMESPACE)
	)
}

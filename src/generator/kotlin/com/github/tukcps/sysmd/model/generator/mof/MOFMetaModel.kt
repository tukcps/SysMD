package com.github.tukcps.sysmd.model.generator.mof

/**
 * A MOF metamodel read from an OMG XMI file.
 * @param packages Top-level packages contained in the metamodel.
 */
data class MOFMetaModel(
    /** Top-level packages contained in the metamodel. */
    val packages: MutableList<MOFPackage> = mutableListOf(),
) {

    /**
     * Returns all metaclasses contained in this metamodel.
     *
     * @return List of all metaclasses.
     */
    fun allClasses(): List<MOFClass> {
        val result = mutableListOf<MOFClass>()

        packages.forEach {
            collectClasses(it, result)
        }

        return result
    }

    /**
     * Collects all metaclasses contained in the specified package.
     *
     * @param pkg Package to traverse.
     * @param result Result list receiving all metaclasses.
     */
    private fun collectClasses(
        pkg: MOFPackage,
        result: MutableList<MOFClass>
    ) {
        result += pkg.classes

        pkg.packages.forEach {
            collectClasses(it, result)
        }
    }

    /**
     * Returns the metaclass with the specified XMI identifier.
     *
     * @param id XMI identifier of the metaclass.
     * @return Matching metaclass or null if no such metaclass exists.
     */
    fun findClassById(id: String): MOFClass? =
        allClasses().find { it.id == id }

    /**
     * Returns the root package with the specified name.
     *
     * @param name Package name.
     * @return Package or null.
     */
    fun findPackage(name: String): MOFPackage? =
        packages.firstOrNull { it.name == name }

    /**
     * Returns the root package with the specified name.
     *
     * @param name Package name.
     * @return Package.
     */
    fun packageByName(name: String): MOFPackage =
        packages.first { it.name == name }
}


/**
 * A package in a MOF metamodel.
 *
 * @param id XMI identifier of the package.
 * @param name Name of the package.
 * @param owningPackage Owning package, or {@code null} for a root package.
 * @param packages Nested packages contained in this package.
 * @param classes Metaclasses contained in this package.
 */
data class MOFPackage(

    /** XMI identifier of the package. */
    val id: String,

    /** Name of the package. */
    val name: String,

    /** Owning package, or {@code null} for a root package. */
    var owningPackage: MOFPackage? = null,

    /** Nested packages contained in this package. */
    val packages: MutableList<MOFPackage> = mutableListOf(),

    /** Metaclasses contained in this package. */
    val classes: MutableList<MOFClass> = mutableListOf(),
) {

    /**
     * Returns the root package.
     *
     * @return Root package.
     */
    fun root(): MOFPackage =
        owningPackage?.root() ?: this

    /**
     * Returns this package and all nested packages recursively.
     *
     * @return All packages.
     */
    fun allPackages(): List<MOFPackage> =
        listOf(this) + packages.flatMap { it.allPackages() }

    /**
     * Returns all metaclasses contained in this package and its nested packages.
     *
     * @return All metaclasses.
     */
    fun allClasses(): List<MOFClass> =
        classes + packages.flatMap { it.allClasses() }
}


/**
 * A MOF metaclass.
 *
 * @param id XMI identifier.
 * @param name Class name.
 * @param isAbstract Whether the metaclass is abstract.
 * @param superClassIds Direct superclasses.
 * @param owningPackage Package containing this metaclass.
 * @param attributes Owned attributes.
 * @param operations Owned operations.
 */
data class MOFClass(

    /** XMI identifier. */
    val id: String,

    /** Name of the metaclass. */
    val name: String,

    /** Whether the metaclass is abstract. */
    val isAbstract: Boolean,

    /** Direct superclasses. */
    val superClassIds: MutableList<String> = mutableListOf(),

    /** Package containing this metaclass. */
    var owningPackage: MOFPackage? = null,

    /** Attributes directly owned by this metaclass. */
    val attributes: MutableList<MOFAttribute> = mutableListOf(),

    /** Operations directly owned by this metaclass. */
    val operations: MutableList<MOFOperation> = mutableListOf(),
)

/**
 * An attribute of a MOF metaclass.
 *
 * @param id XMI identifier.
 * @param name Attribute name.
 * @param typeId Referenced type identifier.
 * @param lower Lower multiplicity bound.
 * @param upper Upper multiplicity bound, or {@code null} for unlimited.
 * @param isDerived Whether the attribute is derived.
 * @param isReadOnly Whether the attribute is read-only.
 * @param isOrdered Whether the attribute is ordered.
 * @param redefinedAttributeIds Redefined attribute identifiers.
 * @param subsettedAttributeIds Subsetted attribute identifiers.
 */
data class MOFAttribute(

    /** XMI identifier. */
    val id: String,

    /** Attribute name. */
    val name: String,

    /** Referenced type identifier. */
    val typeId: String? = null,

    /** Lower multiplicity bound. */
    val lower: Int = 0,

    /** Upper multiplicity bound, or {@code null} for unlimited. */
    val upper: Int? = 1,

    /** Whether the attribute is derived. */
    val isDerived: Boolean = false,

    /** Whether the attribute is read-only. */
    val isReadOnly: Boolean = false,

    /** Whether the attribute is ordered. */
    val isOrdered: Boolean = false,

    /** Redefined attribute identifiers. */
    val redefinedAttributeIds: MutableList<String> = mutableListOf(),

    /** Subsetted attribute identifiers. */
    val subsettedAttributeIds: MutableList<String> = mutableListOf(),
)

/**
 * An operation of a MOF metaclass.
 *
 * @param id XMI identifier.
 * @param name Operation name.
 * @param parameters Owned parameters.
 */
data class MOFOperation(

    /** XMI identifier. */
    val id: String,

    /** Operation name. */
    val name: String,

    /** Parameters owned by this operation. */
    val parameters: MutableList<MOFParameter> = mutableListOf(),
)

/**
 * A parameter of a MOF operation.
 *
 * @param id XMI identifier.
 * @param name Parameter name.
 * @param typeId Referenced type identifier.
 * @param direction Parameter direction.
 * @param lower Lower multiplicity bound.
 * @param upper Upper multiplicity bound, or {@code null} for unlimited.
 * @param isOrdered Whether the parameter is ordered.
 */
data class MOFParameter(

    /** XMI identifier. */
    val id: String,

    /** Parameter name. */
    val name: String,

    /** Referenced type identifier. */
    val typeId: String? = null,

    /** Parameter direction. */
    val direction: MOFParameterDirection = MOFParameterDirection.IN,

    /** Lower multiplicity bound. */
    val lower: Int = 0,

    /** Upper multiplicity bound, or {@code null} for unlimited. */
    val upper: Int? = 1,

    /** Whether the parameter is ordered. */
    val isOrdered: Boolean = false,
)

/**
 * Direction of a MOF operation parameter.
 */
enum class MOFParameterDirection {
    IN,
    OUT,
    INOUT,
    RETURN
}
/**
 * A property of a MOF metaclass.
 *
 * @param id XMI identifier of the property.
 * @param name Property name.
 * @param typeId XMI identifier of the property type.
 * @param lower Lower multiplicity bound.
 * @param upper Upper multiplicity bound, or {@code null} for unlimited.
 * @param isDerived Whether the property is derived.
 * @param isReadOnly Whether the property is read-only.
 * @param isOrdered Whether the property is ordered.
 * @param redefinedPropertyIds Redefined properties.
 * @param subsettedPropertyIds Subsetted properties.
 */
data class MOFProperty(

    /** XMI identifier of the property. */
    val id: String,

    /** Property name. */
    val name: String,

    /** XMI identifier of the property type. */
    val typeId: String?,

    /** Lower multiplicity bound. */
    val lower: Int = 0,

    /** Upper multiplicity bound, or {@code null} for unlimited. */
    val upper: Int? = 1,

    /** Whether the property is derived. */
    val isDerived: Boolean = false,

    /** Whether the property is read-only. */
    val isReadOnly: Boolean = false,

    /** Whether the property is ordered. */
    val isOrdered: Boolean = false,

    /** Redefined properties. */
    val redefinedPropertyIds: MutableList<String> = mutableListOf(),

    /** Subsetted properties. */
    val subsettedPropertyIds: MutableList<String> = mutableListOf(),
)
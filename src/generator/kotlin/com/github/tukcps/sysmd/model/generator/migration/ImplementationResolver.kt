package com.github.tukcps.sysmd.model.generator.migration

import com.github.tukcps.sysmd.model.generator.kotlin.KotlinTypeMapper
import com.github.tukcps.sysmd.model.generator.mof.MOFAttribute
import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFHierarchyResolver
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFOperation
import com.github.tukcps.sysmd.model.generator.mof.MOFParameterDirection

/**
 * Resolves existing Kotlin implementations and required MOF members.
 */
class ImplementationResolver(
    private val model: MOFMetaModel,
    scanner: ExistingSourceScanner
) {
    private val hierarchy = MOFHierarchyResolver(model)
    private val implementations = scanner.implementations()

    /**
     * Returns the implementation of a metaclass.
     *
     * @param clazz Metaclass.
     * @return Existing implementation.
     */
    fun implementation(
        clazz: MOFClass
    ): ExistingSourceScanner.Implementation? =
        implementations["${clazz.name}Implementation"]

    /**
     * Returns whether a metaclass has an existing implementation.
     *
     * @param clazz Metaclass.
     * @return Whether an implementation exists.
     */
    fun hasImplementation(clazz: MOFClass): Boolean =
        implementation(clazz) != null

    /**
     * Returns the nearest superclass with an existing implementation.
     *
     * @param clazz Metaclass.
     * @return Nearest implemented superclass.
     */
    fun implementationSuperClass(clazz: MOFClass): MOFClass? =
        hierarchy.nearestFirstSuperClasses(clazz)
            .firstOrNull(::hasImplementation)

    /**
     * Returns all classes whose members are required by an implementation.
     *
     * Classes already covered by implementation superclasses are excluded.
     *
     * @param clazz Metaclass.
     * @return Required classes.
     */
    fun requiredClasses(clazz: MOFClass): List<MOFClass> {
        val implementedSuperTypes = implementation(clazz)
            ?.superTypes
            .orEmpty()
            .mapNotNull(::implementedClass)

        val covered = implementedSuperTypes
            .flatMap(hierarchy::hierarchy)
            .map { it.id }
            .toSet()

        return hierarchy.hierarchy(clazz)
            .filterNot { it.id in covered }
    }

    /**
     * Returns all attributes required by an implementation.
     *
     * @param clazz Metaclass.
     * @return Required attributes.
     */
    fun requiredAttributes(clazz: MOFClass): List<MOFAttribute> =
        requiredClasses(clazz)
            .flatMap { it.attributes }
            .distinctBy { it.id }

    /**
     * Returns all operations required by an implementation.
     *
     * @param clazz Metaclass.
     * @return Required operations.
     */
    fun requiredOperations(clazz: MOFClass): List<MOFOperation> =
        requiredClasses(clazz)
            .flatMap { it.operations }
            .distinctBy { it.id }

    /**
     * Returns attributes missing in an existing implementation.
     *
     * @param clazz Metaclass.
     * @return Missing attributes.
     */
    fun missingAttributes(clazz: MOFClass): List<MOFAttribute> {
        val existing = implementation(clazz)
            ?.properties
            .orEmpty()

        return requiredAttributes(clazz)
            .filterNot { it.name in existing }
    }

    /**
     * Returns operations missing in an existing implementation.
     *
     * @param clazz Metaclass.
     * @return Missing operations.
     */
    fun missingOperations(clazz: MOFClass): List<MOFOperation> {
        val existing = implementation(clazz)
            ?.operations
            .orEmpty()

        return requiredOperations(clazz)
            .filterNot { operation ->
                operationSignature(operation) in existing
            }
    }

    /**
     * Returns the metaclass implemented by an implementation type.
     *
     * @param implementationName Implementation class name.
     * @return Implemented metaclass.
     */
    private fun implementedClass(
        implementationName: String
    ): MOFClass? {
        val name = implementationName
            .removeSuffix("Implementation")

        return model.allClasses()
            .firstOrNull { it.name == name }
    }

    /**
     * Returns the Kotlin signature of a MOF operation.
     *
     * @param operation MOF operation.
     * @return Kotlin operation signature.
     */
    private fun operationSignature(
        operation: MOFOperation
    ): ExistingSourceScanner.OperationSignature =
        ExistingSourceScanner.OperationSignature(
            name = operation.name,
            parameterTypes = operation.parameters
                .filter {
                    it.direction == MOFParameterDirection.IN
                }
                .map {
                    KotlinTypeMapper.type(model, it)
                }
        )
}
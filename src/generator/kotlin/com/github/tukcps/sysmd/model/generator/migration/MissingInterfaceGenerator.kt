package com.github.tukcps.sysmd.model.generator.migration

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinFileWriter
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinModelResolver
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinTypeMapper
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinWriter
import com.github.tukcps.sysmd.model.generator.mof.*

/**
 * Generates model interfaces from the MOF metamodel.
 *
 * Existing interfaces are never overwritten. If an interface already exists,
 * the generated source is written with the suffix {@code .kt.generated} for
 * manual integration.
 */
class MissingInterfaceGenerator {

    /** Kotlin source file writer. */
    private val fileWriter = KotlinFileWriter()

    /**
     * Generates model interfaces.
     *
     * @param model MOF metamodel.
     */
    fun generate(model: MOFMetaModel) {
        val existing = ExistingSourceScanner(
            GeneratorConfiguration.MODEL_DIRECTORIES
        ).interfaces()

        model.allClasses()
            .sortedBy { it.name }
            .forEach { clazz ->
                val directory = KotlinModelResolver.directory(clazz)
                val exists = clazz.name in existing
                val fileName = if (exists)
                    "${clazz.name}.kt.generated"
                else
                    "${clazz.name}.kt"

                fileWriter.writeGenerated(
                    directory,
                    fileName,
                    createSource(model, clazz)
                )

                if (exists) {
                    println(
                        "Generated interface proposal ${clazz.name} -> $fileName"
                    )
                } else {
                    println(
                        "Generated interface ${clazz.name} -> $fileName"
                    )
                }
            }
    }

    /**
     * Creates the source code of an interface.
     *
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     * @return Kotlin source code.
     */
    private fun createSource(
        model: MOFMetaModel,
        clazz: MOFClass
    ): String {
        val packageName = KotlinModelResolver.packageName(clazz)

        val superTypes = clazz.superClassIds
            .mapNotNull(model::findClassById)
            .sortedBy { it.name }

        val referencedTypes = buildList {
            addAll(superTypes)

            clazz.attributes
                .mapNotNullTo(this) {
                    it.typeId?.let(model::findClassById)
                }

            clazz.operations
                .flatMap { it.parameters }
                .mapNotNullTo(this) {
                    it.typeId?.let(model::findClassById)
                }
        }

        val writer = KotlinWriter()

        writer.generatedFile("MissingInterfaceGenerator")
        writer.line()
        writer.packageDeclaration(packageName)

        writer.imports(
            *referencedTypes
                .mapNotNull { type ->
                    val typePackage = KotlinModelResolver.packageName(type)

                    if (typePackage == packageName)
                        null
                    else
                        "$typePackage.${type.name}"
                }
                .distinct()
                .sorted()
                .toTypedArray()
        )

        val declaration = buildString {
            append("interface ${clazz.name}")

            if (superTypes.isNotEmpty()) {
                append(" : ")
                append(superTypes.joinToString(", ") { it.name })
            }
        }

        writer.begin(declaration)

        writeAttributes(writer, model, clazz)
        writeOperations(writer, model, clazz)

        if (clazz.attributes.isNotEmpty() ||
            clazz.operations.isNotEmpty()
        ) {
            writer.line()
        }

        writer.end()

        return writer.toString()
    }

    /**
     * Writes the attributes of a metaclass.
     *
     * @param writer Kotlin source writer.
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     */
    private fun writeAttributes(
        writer: KotlinWriter,
        model: MOFMetaModel,
        clazz: MOFClass
    ) {
        if (clazz.attributes.isEmpty())
            return

        writer.line()

        clazz.attributes
            .sortedBy { it.name }
            .forEach { attribute ->
                val override = if (
                    overridesAttribute(model, clazz, attribute)
                ) {
                    "override "
                } else {
                    ""
                }

                val keyword =
                    KotlinTypeMapper.declarationKeyword(attribute)

                val type =
                    KotlinTypeMapper.type(model, attribute)

                writer.line(
                    "$override$keyword ${attribute.name}: $type"
                )
            }
    }

    /**
     * Returns whether an attribute overrides an inherited Kotlin property.
     *
     * MOF redefinition alone does not imply a Kotlin override. Kotlin requires
     * an inherited property with the same name.
     *
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     * @param attribute Attribute.
     * @return Whether the attribute overrides an inherited property.
     */
    private fun overridesAttribute(
        model: MOFMetaModel,
        clazz: MOFClass,
        attribute: MOFAttribute
    ): Boolean =
        MOFHierarchyResolver(model)
            .allSuperClasses(clazz)
            .any { superClass ->
                superClass.attributes.any {
                    it.name == attribute.name
                }
            }

    /**
     * Writes the operations of a metaclass.
     *
     * @param writer Kotlin source writer.
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     */
    private fun writeOperations(
        writer: KotlinWriter,
        model: MOFMetaModel,
        clazz: MOFClass
    ) {
        if (clazz.operations.isEmpty())
            return

        writer.line()

        clazz.operations
            .sortedBy { it.name }
            .forEach { operation ->
                val override = if (overridesOperation(model, clazz, operation)) {
                    "override "
                } else { "" }

                writer.line(
                    override + operationDeclaration(model, operation)
                )
            }
    }

    /**
     * Returns whether an operation overrides an inherited Kotlin operation.
     *
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     * @param operation Operation.
     * @return Whether the operation overrides an inherited operation.
     */
    private fun overridesOperation(
        model: MOFMetaModel,
        clazz: MOFClass,
        operation: MOFOperation
    ): Boolean {
        val signature = operationSignature(model, operation)

        return MOFHierarchyResolver(model)
            .allSuperClasses(clazz)
            .flatMap { it.operations }
            .any {
                operationSignature(model, it) == signature
            }
    }

    /**
     * Returns the Kotlin signature of a MOF operation.
     *
     * @param model MOF metamodel.
     * @param operation MOF operation.
     * @return Kotlin operation signature.
     */
    private fun operationSignature(
        model: MOFMetaModel,
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

    /**
     * Returns the Kotlin declaration of an operation.
     *
     * @param model MOF metamodel.
     * @param operation MOF operation.
     * @return Kotlin operation declaration.
     */
    private fun operationDeclaration(
        model: MOFMetaModel,
        operation: MOFOperation
    ): String {
        val unsupported = operation.parameters.filter {
            it.direction == MOFParameterDirection.OUT ||
                    it.direction == MOFParameterDirection.INOUT
        }

        require(unsupported.isEmpty()) {
            "Unsupported parameter direction in operation " +
                    "${operation.name}: " +
                    unsupported.joinToString {
                        "${it.name}:${it.direction}"
                    }
        }

        val parameters = operation.parameters
            .filter {
                it.direction == MOFParameterDirection.IN
            }
            .joinToString(", ") {
                "${it.name}: ${KotlinTypeMapper.type(model, it)}"
            }

        val returnParameters = operation.parameters
            .filter {
                it.direction == MOFParameterDirection.RETURN
            }

        require(returnParameters.size <= 1) {
            "Multiple return parameters in operation ${operation.name}"
        }

        val returnType = returnParameters
            .singleOrNull()
            ?.let {
                KotlinTypeMapper.type(model, it)
            }
            ?: "Unit"

        return buildString {
            append("fun ${operation.name}($parameters)")

            if (returnType != "Unit")
                append(": $returnType")
        }
    }
}
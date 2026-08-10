package com.github.tukcps.sysmd.model.generator.migration

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinFileWriter
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinModelResolver
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinTypeMapper
import com.github.tukcps.sysmd.model.generator.kotlin.KotlinWriter
import com.github.tukcps.sysmd.model.generator.mof.*


/**
 * Generates model implementation proposals from the MOF metamodel.
 *
 * Existing implementations are never overwritten. Generated proposals are
 * written with the suffix {@code .kt.generated} for manual comparison and
 * integration.
 */
class MissingImplementationGenerator {

    /** Kotlin source file writer. */
    private val fileWriter = KotlinFileWriter()

    /**
     * Generates implementation proposals.
     *
     * @param model MOF metamodel.
     */
    fun generate(model: MOFMetaModel) {
        val resolver = ImplementationResolver(
            model,
            ExistingSourceScanner(
                GeneratorConfiguration.MODEL_DIRECTORIES
            )
        )

        model.allClasses()
            .sortedBy { it.name }
            .forEach { clazz ->
                val implementation = resolver.implementation(clazz)

                if (implementation == null)
                    generateMissingProposal(model, resolver, clazz)
                else
                    generateExistingProposal(
                        model,
                        resolver,
                        clazz,
                        implementation
                    )
            }
    }

    /**
     * Generates a proposal for an existing implementation.
     *
     * @param model MOF metamodel.
     * @param resolver Implementation resolver.
     * @param clazz Metaclass.
     * @param implementation Existing implementation.
     */
    private fun generateExistingProposal(
        model: MOFMetaModel,
        resolver: ImplementationResolver,
        clazz: MOFClass,
        implementation: ExistingSourceScanner.Implementation
    ) {
        val attributes = resolver.requiredAttributes(clazz)
        val operations = resolver.requiredOperations(clazz)
        val superImplementation =
            resolver.implementationSuperClass(clazz)
        val fileName =
            "${clazz.name}Implementation.kt.generated"

        fileWriter.writeGenerated(
            implementation.file.parent,
            fileName,
            createSource(
                model = model,
                clazz = clazz,
                packageName = implementation.packageName,
                superImplementation = superImplementation,
                attributes = attributes,
                operations = operations
            )
        )

        val missingAttributes =
            resolver.missingAttributes(clazz)
        val missingOperations =
            resolver.missingOperations(clazz)

        println(
            "Generated implementation proposal ${clazz.name} -> " +
                    "$fileName (${missingAttributes.size} missing properties, " +
                    "${missingOperations.size} missing operations)"
        )
    }

    /**
     * Generates a proposal for a missing implementation.
     *
     * @param model MOF metamodel.
     * @param resolver Implementation resolver.
     * @param clazz Metaclass.
     */
    private fun generateMissingProposal(
        model: MOFMetaModel,
        resolver: ImplementationResolver,
        clazz: MOFClass
    ) {
        val directory = KotlinModelResolver
            .directory(clazz)
            .resolve("implementation")

        val packageName =
            "${KotlinModelResolver.packageName(clazz)}.implementation"

        val superImplementation =
            resolver.implementationSuperClass(clazz)

        val fileName =
            "${clazz.name}Implementation.kt.generated"

        fileWriter.writeGenerated(
            directory,
            fileName,
            createSource(
                model = model,
                clazz = clazz,
                packageName = packageName,
                superImplementation = superImplementation,
                attributes = clazz.attributes,
                operations = clazz.operations
            )
        )

        println(
            "Generated missing implementation ${clazz.name} -> $fileName"
        )
    }

    /**
     * Creates an implementation proposal.
     *
     * @param model MOF metamodel.
     * @param clazz Metaclass.
     * @param packageName Kotlin package.
     * @param superImplementation Suggested implementation superclass.
     * @param attributes Attributes to implement.
     * @param operations Operations to implement.
     * @return Kotlin source code.
     */
    private fun createSource(
        model: MOFMetaModel,
        clazz: MOFClass,
        packageName: String,
        superImplementation: MOFClass?,
        attributes: List<MOFAttribute>,
        operations: List<MOFOperation>
    ): String {
        val referencedTypes = buildList {
            add(clazz)

            attributes
                .mapNotNullTo(this) {
                    it.typeId?.let(model::findClassById)
                }

            operations
                .flatMap { it.parameters }
                .mapNotNullTo(this) {
                    it.typeId?.let(model::findClassById)
                }
        }

        val writer = KotlinWriter()

        writer.generatedFile("MissingImplementationGenerator")
        writer.line()
        writer.packageDeclaration(packageName)

        writer.imports(
            *referencedTypes
                .mapNotNull { type ->
                    val typePackage =
                        KotlinModelResolver.packageName(type)

                    if (typePackage == packageName)
                        null
                    else
                        "$typePackage.${type.name}"
                }
                .distinct()
                .sorted()
                .toTypedArray()
        )

        if (superImplementation != null) {
            writer.line(
                "/** Suggested implementation superclass: " +
                        "${superImplementation.name}Implementation. */"
            )
        }

        writer.begin(
            "class ${clazz.name}Implementation(model : Session) : ${clazz.name}"
        )

        writeAttributes(
            writer,
            model,
            attributes
        )

        writeOperations(
            writer,
            model,
            operations
        )

        if (attributes.isNotEmpty() || operations.isNotEmpty())
            writer.line()

        writer.end()

        return writer.toString()
    }

    /**
     * Writes implementation attributes.
     *
     * @param writer Kotlin source writer.
     * @param model MOF metamodel.
     * @param attributes Attributes.
     */
    private fun writeAttributes(
        writer: KotlinWriter,
        model: MOFMetaModel,
        attributes: List<MOFAttribute>
    ) {
        if (attributes.isEmpty())
            return

        writer.line()

        attributes
            .sortedBy { it.name }
            .forEach { attribute ->
                val keyword =
                    KotlinTypeMapper.declarationKeyword(attribute)

                val type =
                    KotlinTypeMapper.type(model, attribute)

                writer.line(
                    "override $keyword ${attribute.name}: $type = TODO()"
                )
            }
    }

    /**
     * Writes implementation operations.
     *
     * @param writer Kotlin source writer.
     * @param model MOF metamodel.
     * @param operations Operations.
     */
    private fun writeOperations(
        writer: KotlinWriter,
        model: MOFMetaModel,
        operations: List<MOFOperation>
    ) {
        if (operations.isEmpty())
            return

        writer.line()

        operations
            .sortedBy { it.name }
            .forEach { operation ->
                writer.line(
                    operationDeclaration(model, operation)
                )
            }
    }

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
            append(
                "override fun ${operation.name}($parameters)"
            )

            if (returnType != "Unit")
                append(": $returnType")

            append(" = TODO()")
        }
    }
}
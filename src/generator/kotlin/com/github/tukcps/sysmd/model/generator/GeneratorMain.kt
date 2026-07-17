package com.github.tukcps.sysmd.model.generator

import com.github.tukcps.sysmd.model.generator.elementdata.ElementDataGenerator
import com.github.tukcps.sysmd.model.generator.elementdata.ElementDataIFGenerator
import com.github.tukcps.sysmd.model.generator.kotlin.*
import com.github.tukcps.sysmd.model.generator.migration.MissingImplementationGenerator
import com.github.tukcps.sysmd.model.generator.migration.MissingInterfaceGenerator
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel
import com.github.tukcps.sysmd.model.generator.mof.MOFXmiLoader
import com.github.tukcps.sysmd.model.generator.report.DataElementReport
import com.github.tukcps.sysmd.model.generator.report.MetamodelReport
import java.nio.file.Path

/**
 * Entry point for generating Kotlin sources from the official OMG metamodel.
 */
object GeneratorMain {

    /**
     * Runs the SysMD metamodel generator.
     *
     * The OMG KerML and SysML metamodel sources are updated and loaded before
     * executing the selected generator targets. If no target is specified, all
     * reports and generators are executed.
     *
     * Supported targets:
     * - `report` generates the metamodel implementation report.
     * - `dataReport` reports data properties defined by the metamodel.
     * - `type` generates the metamodel element type enumeration.
     * - `typeResolver` generates runtime metamodel type resolution.
     * - `hierarchy` generates the metamodel element hierarchy.
     * - `factory` generates the element factory.
     * - `elementData` generates the common element data interface.
     * - `migration` generates interface and implementation migration proposals.
     *
     * @param args Generator targets to execute.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        println("Updating OMG metamodel sources...")
        OMGDownloader.updateAll()

        println("Loading metamodel...")
        val model = loadMetaModel()

        val targets = args.toSet()

        if (targets.isEmpty() || "report" in targets)
            println(MetamodelReport(model).create())

        if (targets.isEmpty() || "dataReport" in targets)
            println(DataElementReport(model).create())

        if (targets.isEmpty() || "type" in targets)
            generateElementType(model)

        if (targets.isEmpty() || "typeResolver" in targets)
            generateElementTypeResolver(model)

        if (targets.isEmpty() || "hierarchy" in targets)
            generateElementHierarchy(model)

        if (targets.isEmpty() || "factory" in targets)
            generateElementFactory(model)

        if (targets.isEmpty() || "elementData" in targets)
            generateElementData(model)

        if (targets.isEmpty() || "migration" in targets)
            generateMigrationProposals(model)

        println("Generated ${model.allClasses().size} metaclasses.")
        println("Generation completed.")
    }

    /**
     * Loads the official OMG metamodel.
     *
     * @return Loaded MOF metamodel.
     */
    private fun loadMetaModel(): MOFMetaModel =
        MOFMetaModel().also { model ->
            MOFXmiLoader.loadMetaModel(
                GeneratorConfiguration.KERML_XMI,
                model
            )
            MOFXmiLoader.loadMetaModel(
                GeneratorConfiguration.SYSML_XMI,
                model
            )
        }

    /**
     * Generates the element type source.
     *
     * @param model MOF metamodel.
     */
    private fun generateElementType(model: MOFMetaModel) {
        generate(model, "ElementType.kt", ElementTypeGenerator())
    }

    /**
     * Generates the element hierarchy source.
     *
     * @param model MOF metamodel.
     */
    private fun generateElementHierarchy(model: MOFMetaModel) {
        generate(model, "ElementHierarchy.kt", ElementHierarchyGenerator())
    }

    /**
     * Generates the element factory source.
     *
     * @param model MOF metamodel.
     */
    private fun generateElementFactory(model: MOFMetaModel) {
        generate(model, "ElementFactory.kt", ElementFactoryGenerator())
    }

    private fun generate(
        model: MOFMetaModel,
        fileName: String,
        generator: KotlinGenerator
    ) {
        val output = Path.of("build/generated/source/sysmd/main/kotlin/com/github/tukcps/sysmd/model/generated")
        KotlinFileWriter().writeGenerated(output, fileName, generator.generate(model))
        println("Generated $fileName")
    }


    /**
     * Generates migration proposals for existing model sources.
     *
     * @param model MOF metamodel.
     */
    private fun generateMigrationProposals(model: MOFMetaModel) {
        MissingInterfaceGenerator().generate(model)
        MissingImplementationGenerator().generate(model)
    }

    /**
     * Generates the common element data interface.
     *
     * @param model MOF metamodel.
     */
    private fun generateElementData(
        model: MOFMetaModel,
    ) {
        generate(model, "ElementDataIF.kt", ElementDataIFGenerator(),)
        generate(model, "ElementData.kt", ElementDataGenerator(),)
    }

    /**
     * Generates runtime element type resolution.
     *
     * @param model MOF metamodel.
     */
    private fun generateElementTypeResolver(model: MOFMetaModel) {
        generate(model, "ElementTypeResolver.kt", ElementTypeResolverGenerator())
    }
}
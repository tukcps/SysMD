package com.github.tukcps.sysmd.model.generator.report

import com.github.tukcps.sysmd.model.generator.GeneratorConfiguration
import com.github.tukcps.sysmd.model.generator.migration.ExistingSourceScanner
import com.github.tukcps.sysmd.model.generator.mof.MOFClass
import com.github.tukcps.sysmd.model.generator.mof.MOFMetaModel

/**
 * Creates a report about the current implementation status of the metamodel.
 *
 * @param model MOF metamodel.
 */
class MetamodelReport(
    private val model: MOFMetaModel
) {
    private val scanner = ExistingSourceScanner(
        GeneratorConfiguration.MODEL_DIRECTORIES
    )
    private val interfaces = scanner.interfaces()
    private val implementations = scanner.implementations()

    /**
     * Creates the report.
     *
     * @return Report text.
     */
    fun create(): String =
        buildString {
            appendLine("============================================================")
            appendLine("SysMD Metamodel Report")
            appendLine("============================================================")
            appendLine()

            appendLine("OMG Metamodel")
            appendLine("------------------------------------------------------------")
            appendLine("KerML : ${GeneratorConfiguration.KERML_SOURCE}")
            appendLine("SysML : ${GeneratorConfiguration.SYSML_SOURCE}")
            appendLine()

            reportPackage(
                this,
                "KerML",
                model.packageByName("KerML").allClasses()
            )
            reportPackage(
                this,
                "SysML",
                model.packageByName("SysML").allClasses()
            )

            reportOverall(this)
        }

    /**
     * Appends the report for one metamodel.
     *
     * @param sb Target string builder.
     * @param name Package name.
     * @param classes Standard metaclasses.
     */
    private fun reportPackage(
        sb: StringBuilder,
        name: String,
        classes: List<MOFClass>
    ) {
        val interfaceCount = classes.count(::hasInterface)
        val implementationCount = classes.count(::hasImplementation)

        sb.appendLine("------------------------------------------------------------")
        sb.appendLine(name)
        sb.appendLine("------------------------------------------------------------")
        sb.appendLine("Standard classes     : ${classes.size}")
        sb.appendLine(
            "Interfaces           : $interfaceCount / ${classes.size} " +
                    "(${percent(interfaceCount, classes.size)}%)"
        )
        sb.appendLine(
            "Implementations      : $implementationCount / ${classes.size} " +
                    "(${percent(implementationCount, classes.size)}%)"
        )

        sb.appendLine()
        sb.appendLine("Missing interfaces")
        appendClasses(
            sb,
            classes.filterNot(::hasInterface)
        )

        sb.appendLine()
        sb.appendLine("Missing implementations")
        appendClasses(
            sb,
            classes.filterNot(::hasImplementation)
        )

        sb.appendLine()
    }

    /**
     * Appends the overall report.
     *
     * @param sb Target string builder.
     */
    private fun reportOverall(sb: StringBuilder) {
        val classes = model.allClasses()
        val interfaceCount = classes.count(::hasInterface)
        val implementationCount = classes.count(::hasImplementation)

        sb.appendLine("------------------------------------------------------------")
        sb.appendLine("Overall")
        sb.appendLine("------------------------------------------------------------")
        sb.appendLine("Standard classes     : ${classes.size}")
        sb.appendLine(
            "Interfaces           : $interfaceCount / ${classes.size} " +
                    "(${percent(interfaceCount, classes.size)}%)"
        )
        sb.appendLine(
            "Implementations      : $implementationCount / ${classes.size} " +
                    "(${percent(implementationCount, classes.size)}%)"
        )
    }

    /**
     * Returns whether an interface exists.
     *
     * @param clazz Metaclass.
     * @return Whether an interface exists.
     */
    private fun hasInterface(clazz: MOFClass): Boolean =
        clazz.name in interfaces

    /**
     * Returns whether an implementation exists.
     *
     * @param clazz Metaclass.
     * @return Whether an implementation exists.
     */
    private fun hasImplementation(clazz: MOFClass): Boolean =
        "${clazz.name}Implementation" in implementations

    /**
     * Appends metaclass names.
     *
     * @param sb Target string builder.
     * @param classes Metaclasses.
     */
    private fun appendClasses(
        sb: StringBuilder,
        classes: List<MOFClass>
    ) {
        if (classes.isEmpty()) {
            sb.appendLine("    -")
            return
        }

        classes
            .sortedBy { it.name }
            .forEach {
                sb.appendLine("    ${it.name}")
            }
    }

    /**
     * Returns a percentage.
     *
     * @param value Current value.
     * @param total Maximum value.
     * @return Percentage.
     */
    private fun percent(
        value: Int,
        total: Int
    ): Int =
        if (total == 0) 0
        else value * 100 / total
}
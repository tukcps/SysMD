package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.LibraryRepository.loadLibraryFromResources
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import java.util.concurrent.ConcurrentHashMap


/**
 * The library repository maintains standard libraries that are read from the file.
 * They are pre-compiled.
 */
object LibraryRepository {
    private val libraries : ConcurrentHashMap<String, List<ElementDAO>> = ConcurrentHashMap()

    fun reset() {
        libraries.clear()
    }

    /**
     * Gets a standard library package's elements as DAO.
     * The library is cached.
     * @param key name of a standard library package
     */
    fun get(key: String): List<ElementDAO>? { return libraries[key] }


    /**
     * Loads a standard library directly from the resources.
     * @param packageNames Name of the standard package. Must be in resources/library.
     */
    fun loadLibraryFromResources(key: String, packageNames: List<String>): List<ElementDAO> {
        logger.info("Loading libraries ($key) - $packageNames")
        try {
            val session = SessionImplementation(libraries = mutableListOf())

            packageNames.forEach {
                val inputStream = javaClass.getResourceAsStream("/libraries/$it.kerml")
                val inputString = inputStream?.bufferedReader().use { input -> input?.readText() }
                if (inputStream == null) {
                    logger.error("Could not load library '/libraries/$it.kerml' from resources")
                    return emptyList()
                }
                else
                    KerML(session).parse(inputString!!)
                if (session.status.issues.isNotEmpty()) {
                    logger.error("Issue while compiling library '$it': ${session.status.issues.joinToString(", ")}")
                }
            }
            session.initialize(4) // resolve and inherit, but no setup of constraint system

            session.checkOwnership()
            session.checkLibraryElementIds()

            if (session.status.issues.isNotEmpty()) {
                logger.error("Issue while compiling arrangement '$packageNames': ${session.status.issues.joinToString(", ")}")
            }

            val elementDAO = session.export().map { it.payloadElementSnapshot!! }
            libraries[key] = elementDAO
            return elementDAO
        } catch (e: Exception) {
            logger.error("Error while loading standard library '$packageNames'", e)
            return emptyList()
        }
    }
}

/**
 * Some arrangements of libraries to be loaded for testing.
 * Choosing a suitable arrangements with less loaded libraries allows us to debug in a more effective way.
 * For the tool and releases, use SysMLLibraries or KermLLibraries that load all needed libraries.
 */
val Arrangements = hashMapOf(
    "Base"          to listOf("Base"),
    "ScalarValues"  to listOf("Base", "ScalarValues"),
    "Objects"       to listOf("Base", "ScalarValues", "Objects"),
    "Links"         to listOf("Base", "ScalarValues", "Links"),
    "Occurrences"   to listOf("Base", "ScalarValues", "Links", "Occurrences"),
    "Objects"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects"),
    "Ranges"        to listOf("Base", "ScalarValues", "SI", "Ranges", "ISQ"),
    "SI"            to listOf("Base", "ScalarValues", "SI", "Ranges", "ISQ"),
    "Ports"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ports"),
    "Items"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items"),
    "Parts"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items", "Parts"),
    "Calculations"  to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items", "Calculations"),
    "Connections"   to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections"),
    "Attributes"    to listOf("Base", "ScalarValues", "Links", "Occurrences", "Attributes"),
    "Allocations"   to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections", "Allocations"),
    "Math"          to listOf("Base", "ScalarValues", "SI", "Ranges", "Math"),
    "Constraints"   to listOf("Base", "ScalarValues", "SI", "Ranges", "Constraints"),
    "Requirements"  to listOf("Base", "ScalarValues", "Constraints", "Requirements"),
    "Actions"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Actions"),
    "Context"       to listOf("Base", "ScalarValues", "Context"),
    "KerML"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ranges", "KerML"),
    "KerMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "Performances", "SI", "Ranges", "ISQ"),
    "SysMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "Performances", "Items", "SI", "Ranges",
        "Ports", "Parts", "Calculations", "Constraints", "Requirements", "Interfaces", "Actions", "States", "Connections", "Signals", "ISQ"),
    "ISO26262"      to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "ISO26262"),
    "Signals"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Signals"),
    "SysMD"         to listOf("Base", "ScalarValues", "SysMD")
)

/**
 * Loads a standard library into the session.
 * This is done directly from the resources, or from the repository, if available.
 * @param library the name of the standard package.
 */
fun Session.loadLibrary(library: String) {

    val daoOfLibrary: List<ElementDAO> = if (library !in Arrangements.keys)
        LibraryRepository.get(library)
            ?: loadLibraryFromResources(library, listOf(library))
    else
        LibraryRepository.get(library)
            ?: loadLibraryFromResources(library, Arrangements[library]!!)

    this.import(daoOfLibrary)
}

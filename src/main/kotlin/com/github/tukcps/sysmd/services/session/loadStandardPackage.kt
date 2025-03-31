package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import com.github.tukcps.sysmd.services.session.LibraryRepository.loadLibraryFromResources
import io.github.tukcps.sysmlv2.api.entities.ElementDAO
import java.util.concurrent.ConcurrentHashMap


/**
 * The library repository maintains standard libraries that are read from the file.
 * They are pre-compiled.
 */
object LibraryRepository {
    private val libraries : ConcurrentHashMap<String, List<io.github.tukcps.sysmlv2.api.entities.ElementDAO>> = ConcurrentHashMap()

    fun reset() {
        libraries.clear()
    }

    /**
     * Gets a standard library package's elements as DAO.
     * The library is cached.
     * @param key name of a standard library package
     */
    fun get(key: String): List<io.github.tukcps.sysmlv2.api.entities.ElementDAO>? {
        return libraries.get(key)
    }


    /**
     * Loads a standard library directly from the resources.
     * @param packageNames Name of the standard package. Must be in resources/library.
     */
    fun loadLibraryFromResources(key: String, packageNames: List<String>): List<io.github.tukcps.sysmlv2.api.entities.ElementDAO> {
        logger.info("Loading libraries ($key) - $packageNames")
        try {
            val session = SessionImplementation(libraries = mutableListOf())

            packageNames.forEach {
                val inputStream = javaClass.getResourceAsStream("/libraries/$it.kerml")
                val inputString = inputStream?.bufferedReader().use { it?.readText() }
                if (inputStream == null)
                    logger.error("Could not load library '/libraries/$it.kerml' from resources")
                else
                    KerML(session).parse(inputString!!)
                if (session.status.exceptions.isNotEmpty())
                    logger.error("Error while compiling library '$it': ${session.status.exceptions.joinToString(", ")}")
            }
            session.initialize(1) // resolve and inherit, but no setup of constraint system

            val elementDAO = mutableListOf<io.github.tukcps.sysmlv2.api.entities.ElementDAO>()
            session.repo.elements.values.forEach {
                if (it != session.global && it != session.anything)
                    elementDAO.add(it.toDAO())
            }
            libraries.put(key, elementDAO)
            return elementDAO
        } catch (e: Exception) {
            logger.error("Error while loading standard library '$packageNames'", e)
            return emptyList()
        }
    }
}

val Arrangements = hashMapOf(
    "Base"          to listOf("Base"),
    "ScalarValues"  to listOf("Base", "ScalarValues"),
    "Objects"       to listOf("Base", "ScalarValues", "Objects"),
    "Links"         to listOf("Base", "ScalarValues", "Links"),
    "Occurrences"   to listOf("Base", "ScalarValues", "Links", "Occurrences"),
    "Ranges"        to listOf("Base", "ScalarValues", "Ranges"),
    "SI"            to listOf("Base", "ScalarValues", "SI"),
    "Ports"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ports"),
    "Items"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items"),
    "Parts"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items", "Parts"),
    "Calculations"  to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items", "Calculations"),
    "Connections"   to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections"),
    "Attributes"    to listOf("Base", "ScalarValues", "Attributes"),
    "Math"          to listOf("Base", "ScalarValues", "Math"),
    "Requirements"  to listOf("Base", "ScalarValues", "Constraints", "Requirements"),
    "Actions"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Actions"),
    "Context"       to listOf("Base", "ScalarValues", "Context"),
    "KerML"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ranges", "KerML"),
    "KerMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "Context", "Items", "SI", "Ranges"),
    "SysMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "Context", "Items", "SI", "Ranges",
        "Ports", "Parts", "Calculations", "Constraints", "Requirements", "Interfaces", "Actions", "States", "Connections", "Signals"),
    "ISO26262"      to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "ISO26262"),
    "Signals"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Signals"),
    "SysMD"         to listOf("Base", "ScalarValues", "SysMD")
)

/**
 * Loads a standard library into the session.
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

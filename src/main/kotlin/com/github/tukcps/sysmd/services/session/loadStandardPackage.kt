package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.datamodel.ElementData
import java.util.concurrent.ConcurrentHashMap

/**
 * The library repository maintains standard libraries that are read from the file.
 * They are pre-compiled.
 */
object LibraryRepository {

    /**
     * Single, pre-compiled libraries.
     */
    private val arrangement : ConcurrentHashMap<String, List<ElementData>> = ConcurrentHashMap()

    /**
     * Pre-compiled arrangement of libraries for testing.
     */
    private val library: ConcurrentHashMap<String, List<ElementData>> = ConcurrentHashMap()

    /**
     * Loads all libraries into map of pre-compiled data elements.
     * @param status Status via which issues are reported.
     */
    fun cacheAllLibraries(status: SessionStatus = SessionStatus()) {
        logger.info("Loading all libraries from resources/libraries/index.txt into cache")

        val index = javaClass.getResourceAsStream("/libraries/index.txt")
            ?.bufferedReader().use { input -> input?.readText() }
            ?.lines()
            ?.filter(String::isNotBlank)?:emptyList()

        for (fileName in index) {
            // logger.info("Loading library $fileName")

            val text = javaClass.getResourceAsStream("/libraries/$fileName")
                ?.bufferedReader().use { input -> input?.readText() }
                ?: ""

            val thisStatus = SessionStatus()
            thisStatus.issues.forEach { issue ->
                logger.info("Issue $issue in library $fileName")
            }
            val elements = KerML(status = thisStatus).parse(text)

            status.issues.addAll(thisStatus.issues)

            library[fileName.removeSuffix(".kerml")] = elements
        }
    }

    /**
     * Clears all cached libraries.
     * They are refreshed lazy when getting them.
     */
    fun reset() {
        library.clear()
        arrangement.clear()
    }

    /**
     * Gets a standard library package's elements as DAO.
     * The library is cached.
     * @param key name of a standard library package
     */
    fun getArrangementFromCache(key: String): List<ElementDataIF>? { return arrangement[key] }

    /**
     * Gets the elements of a library. If not in cache, it will be put into the cache.
     * @param name Name of the library
     * @param status Status object for reporting issues
     * @return List with elements in the library.
     */
    fun getLibrary(name: String, status: SessionStatus): List<ElementData> {

        if (library.containsKey(name)) return library[name]?: emptyList()

        var result: List<ElementData>? = null
        val inputStream = javaClass.getResourceAsStream("/libraries/$name.kerml")
        val inputString = inputStream?.bufferedReader().use { input -> input?.readText() }
        if (inputStream == null) {
            logger.error("Could not load library '/libraries/$name.kerml' from resources")
            return emptyList()
        } else {
            result = KerML(status = status)
                .settings {
                    addDefaultMultiplicity = false
                    addConstraints = false
                }
                .parse(inputString!!)
        }
        if (status.issues.isNotEmpty()) {
            logger.error("Issue while compiling library '$name': ${status.issues.joinToString(", ")}")
        } else {
            library[name] = result
            logger.info("Loaded library $name into cache")
        }
        return result
    }

    /**
     * Loads a library arrangement from the resources, and saves it into this repository.
     * @param libraryNames Name of the standard package. Must be in resources/library.
     * @param status For reporting issues.
     */
    fun getArrangement(key: String, libraryNames: List<String>, status: SessionStatus = SessionStatus()): List<ElementData> {
        logger.info("Loading arrangement ($key) - $libraryNames")

        if (arrangement.containsKey(key)) return arrangement[key]!!

        try {
            val export = mutableListOf<ElementData>()
            libraryNames.forEach {
                export.addAll( getLibrary(it, status) )
            }

            // Cache compiled arrangement
            arrangement[key] = export
            return export
        } catch (e: Exception) {
            logger.error("Error while loading standard library  arrangement'$libraryNames'", e)
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
    "Performances"  to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Performances"),
    "Ranges"        to listOf("Base", "ScalarValues", "Ranges", "ISQ", "Quantities"),
    "ISQ"           to listOf("Base", "ScalarValues", "Ranges", "ISQ", "Quantities"),
    "Ports"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ports"),
    "Items"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items"),
    "Parts"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items", "Parts"),
    "Calculations"  to listOf("Base", "ScalarValues", "Ranges", "Links", "Occurrences", "Objects", "Items", "Actions", "Attributes", "Calculations"),
    "Connections"   to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections"),
    "Interfaces"    to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections", "Interfaces"),
    "Attributes"    to listOf("Base", "ScalarValues", "Ranges", "Links", "Occurrences", "Attributes"),
    "Allocations"   to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections", "Allocations"),
    "Math"          to listOf("Base", "ScalarValues", "ISQ", "Ranges", "Math", "Quantities"),
    "Constraints"   to listOf("Base", "ScalarValues", "ISQ", "Ranges", "Constraints", "Quantities"),
    "Requirements"  to listOf("Base", "ScalarValues", "Constraints", "Requirements"),
    "Actions"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Actions"),
    "States"        to listOf("Base", "ScalarValues", "Links", "Occurrences", "Actions", "States"),
    "Context"       to listOf("Base", "ScalarValues", "Context"),
    "KerML"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ranges", "KerML"),
    "KerMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "ISQ", "Objects", "Links", "Occurrences", "Performances", "Quantities"),
    "SysMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "ISQ", "Objects", "Links", "Occurrences", "Performances", "Items",
        "Ports", "Parts", "Actions", "Calculations", "Attributes", "Constraints", "Requirements", "Interfaces", "States", "Connections", "Signals", "Quantities", "VerificationCases"),
    "ISO26262"      to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "ISO26262", "Quantities"),
    "Signals"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Signals"),
    "SysMD"         to listOf("Base", "ScalarValues", "SysMD"),
    "DataFunctions" to listOf("Base", "ScalarValues", "DataFunctions"), // FIXME: +BaseFunctions
)
package com.github.tukcps.sysmd.services.session

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.logger
import com.github.tukcps.sysmd.model.expression.implementation.BuiltinFunctions
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.implementation.FunctionImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.check.checkLibraryElementIds
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.session.LibraryRepository.loadLibraryFromResources
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
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
            session.initialize(Runlevel.MODEL) // resolve and inherit, but no setup of constraint system

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
    "Performances"  to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Performances"),
    "Ranges"        to listOf("Base", "ScalarValues", "Ranges", "ISQ", "Quantities"),
    "ISQ"           to listOf("Base", "ScalarValues", "Ranges", "ISQ", "Quantities"),
    "Ports"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ports"),
    "Items"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items"),
    "Parts"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items", "Parts"),
    "Calculations"  to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Items", "Actions", "Calculations"),
    "Connections"   to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections"),
    "Interfaces"    to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections", "Interfaces"),
    "Attributes"    to listOf("Base", "ScalarValues", "Links", "Occurrences", "Attributes"),
    "Allocations"   to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Connections", "Allocations"),
    "Math"          to listOf("Base", "ScalarValues", "ISQ", "Ranges", "Math", "Quantities"),
    "Constraints"   to listOf("Base", "ScalarValues", "ISQ", "Ranges", "Constraints", "Quantities"),
    "Requirements"  to listOf("Base", "ScalarValues", "Constraints", "Requirements"),
    "Actions"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Actions"),
    "States"        to listOf("Base", "ScalarValues", "Links", "Occurrences", "Actions", "States"),
    "Context"       to listOf("Base", "ScalarValues", "Context"),
    "KerML"         to listOf("Base", "ScalarValues", "Links", "Occurrences", "Objects", "Ranges", "KerML"),
    "KerMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "Performances", "Ranges", "ISQ", "Quantities"),
    "SysMLLibraries" to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "Performances", "Items", "Ranges",
        "Ports", "Parts",  "Actions", "Calculations", "Constraints", "Requirements", "Interfaces", "States", "Connections", "Signals", "ISQ", "Quantities", "VerificationCases"),
    "ISO26262"      to listOf("Base", "ScalarValues", "Ranges", "Objects", "Links", "Occurrences", "ISO26262", "Quantities"),
    "Signals"       to listOf("Base", "ScalarValues", "Links", "Occurrences", "Signals"),
    "SysMD"         to listOf("Base", "ScalarValues", "SysMD"),
    "DataFunctions" to listOf("Base", "ScalarValues", "DataFunctions"), // FIXME: +BaseFunctions
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

	if(library == "DataFunctions")
	{
		fun initArithmetic(pkg : Package?)
		{
			if(pkg === null)
				return

			for(func in pkg.ownedElement.filterIsInstance<FunctionImplementation>())
			{
				func.builtin = when(func.name) {
					"+" -> BuiltinFunctions.PLUS
					"-" -> BuiltinFunctions.MINUS
					"*" -> BuiltinFunctions.TIMES
					"/" -> BuiltinFunctions.DIV
					"**", "^" -> BuiltinFunctions.EXP
					"<" -> BuiltinFunctions.LT
					">" -> BuiltinFunctions.GT
					"<=" -> BuiltinFunctions.LE
					">=" -> BuiltinFunctions.GE
					"==" -> BuiltinFunctions.EE
					"if" -> BuiltinFunctions.ITE
					else -> continue
				}.f
			}
		}

		// TODO: unary operators
		// FIXME: there are more undefined functions in these packages
		initArithmetic(global.getOwned<Package>("IntegerFunctions"))
		initArithmetic(global.getOwned<Package>("RealFunctions"))
		initArithmetic(global.getOwned<Package>("RationalFunctions"))
		initArithmetic(global.getOwned<Package>("NaturalFunctions"))

		global.getOwned<Package>("BooleanFunctions")?.let { bf ->
			for(func in bf.ownedElement.filterIsInstance<FunctionImplementation>())
			{
				func.builtin = when(func.name) {
					"not" -> BuiltinFunctions.NOT
					"&" -> BuiltinFunctions.AND
					"|" -> BuiltinFunctions.OR
					"==" -> BuiltinFunctions.EE
					else -> continue
				}.f
			}
		}

		global.resolve("")
	}
}

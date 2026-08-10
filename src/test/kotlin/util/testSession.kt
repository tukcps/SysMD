package util

import com.github.tukcps.sysmd.model.expression.implementation.BuiltinFunctions
import com.github.tukcps.sysmd.model.generated.ElementDataIF
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.model.kerml.implementation.FunctionImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.getOwned
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.repositories.local.ProjectData.Companion.fromInterchangeFiles
import com.github.tukcps.sysmd.services.session.Arrangements
import com.github.tukcps.sysmd.services.session.LibraryRepository
import com.github.tukcps.sysmd.services.session.LibraryRepository.getArrangement
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.services.session.implementation.ProjectSessionImplementation
import com.github.tukcps.sysmd.services.session.implementation.SessionImplementation
import kotlinx.io.files.Path
import util.mockup.MockupSysMDProjectService
import java.nio.file.Paths


/**
 * Starts a mockup project session for testing purposes.
 * - It creates a mock repository for projects
 * - The session is terminated after the test has run.
 * The parameters permit setting up test cases:
 * @param arrangement arrangements of in-itself consistent preloaded libraries that are loaded into the test session prior to the test.
 * @param runlevel how far to initialize the session.
 * @param testDirectory the path where a test project is located in the test resources
 * @param test a lambda with the test.
 */
inline fun testProjectSession(
    vararg arrangement: String,
    runlevel: Runlevel = Runlevel.NAMES_RESOLVED,
    testDirectory: String = "/testSession",
    test: ProjectSessionImplementation.() -> Unit,
)  {
    // For test, only use mockup project service
    SessionManager.projectService = MockupSysMDProjectService()

    val dir = (SessionManager.javaClass.getResource(testDirectory)?.toURI() )
        ?: SessionManager.javaClass.classLoader.getResource(testDirectory)?.toURI()
                ?: throw Exception("Could not load project from $testDirectory")

    val project = fromInterchangeFiles(Path(Paths.get(dir).toString()) )
        ?: throw Exception("Could not create project from .project.json and .meta.json in $testDirectory")

    val session = SessionManager.createSession(project = project, runlevel = runlevel,)

    try {
        with(session) {
            (SessionManager.projectService as MockupSysMDProjectService).setProjects(listOf(project))
            loadLibraryArrangement("Base")
            arrangement.forEach { arrangement -> loadLibraryArrangement(arrangement) }
            initialize(settings.runlevel)
            (this as ProjectSessionImplementation).test()
        }
    } finally {
        SessionManager.kill(session.id)
    }
}

/**
 * Starts a mockup project session for testing purposes.
 * - It creates a mock repository for projects
 * - The session is terminated after the test has run.
 * The parameters permit setting up test cases:
 * @param arrangement arrangements of in-itself consistent preloaded libraries that are
 * loaded into the test session prior to the test. If no parameter is given, "Base" ist the default.
 * To load no libraries at all, one can pass an empty string.
 * @param runlevel how far to initialize the session.
 * @param test a lambda with the test.
 */
inline fun testSession(
    vararg arrangement: String,
    runlevel: Runlevel = Runlevel.NAMES_RESOLVED,
    test: SessionImplementation.() -> Unit,
)  {
    with ( SessionImplementation() ) {
        if (arrangement.isEmpty())
            loadLibrary("Base")
        settings.runlevel = runlevel
        arrangement.forEach {
            arrangement -> loadLibraryArrangement(arrangement)
        }
        initialize(settings.runlevel)
        this.test()
    }
}


/**
 * Loads an arrangement of standard libraries into the session.
 * This is done directly from the resources, or from the repository, if available.
 * @param arrangement the name of the standard package.
 */
fun Session.loadLibraryArrangement(arrangement: String) {

    val daoOfLibrary: List<ElementDataIF> = if (arrangement !in Arrangements.keys)
        LibraryRepository.getArrangementFromCache(arrangement)
            ?: getArrangement(arrangement, listOf(arrangement))
    else
        LibraryRepository.getArrangementFromCache(arrangement)
            ?: getArrangement(arrangement, Arrangements[arrangement]!!)

    this.import(daoOfLibrary)

    if(arrangement == "DataFunctions") {
        fun initArithmetic(pkg : Package?) {
            if(pkg === null)
                return

            for(func in pkg.ownedElement.filterIsInstance<FunctionImplementation>()) {
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
            for(func in bf.ownedElement.filterIsInstance<FunctionImplementation>()) {
                func.builtin = when(func.name) {
                    "not" -> BuiltinFunctions.NOT
                    "&" -> BuiltinFunctions.AND
                    "|" -> BuiltinFunctions.OR
                    "==" -> BuiltinFunctions.EE
                    else -> continue
                }.f
            }
        }
    }
}

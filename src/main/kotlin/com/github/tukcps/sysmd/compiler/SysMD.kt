package com.github.tukcps.sysmd.compiler

import com.github.tukcps.aadd.AADD
import com.github.tukcps.aadd.BDD
import com.github.tukcps.aadd.IDD
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.exceptions.SemanticError
import com.github.tukcps.sysmd.exceptions.SysMDError
import com.github.tukcps.sysmd.model.expression.AstRoot
import com.github.tukcps.sysmd.model.kerml.AnnotatingElement
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.TextualRepresentation
import com.github.tukcps.sysmd.model.kerml.implementation.AnnotatingElementImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.compiler.parser.kerml.Expression
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.report
import com.github.tukcps.sysmd.services.repositories.local.ProjectData
import com.github.tukcps.sysmd.services.repositories.local.ProjectUsageData
import com.github.tukcps.sysmd.services.resolveNames
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmlv2.entities.ElementDAO
import java.io.File


/**
 * Loads a SysMD file from an input string.
 * NOTE: Rather useful for test purposes and only used there.
 * @param input A SysMD language sting; pure SysMD without interwoven MD.
 * @param catchExceptions by default, exceptions are caught, and errors are reported via status; this can
 * @param createTextualRepresentation the name of textual representation that will be created in the KerML model
 * be turned off by setting catchExceptions to false.
 */
fun Session.loadSysMD(
    input: String,
    catchExceptions: Boolean = settings.catchExceptions,
    createTextualRepresentation: String? = null,
    generateAnnotations: Boolean = false
){
    settings.catchExceptions = catchExceptions
    var rep = TextualRepresentationImplementation(declaredName = createTextualRepresentation, language = "SysMD", body=input)
    if (createTextualRepresentation != null) rep = create(rep, global)
    KerML(
        model = this,
        textualRepresentation = rep,
        generateAnnotations = (createTextualRepresentation != null) && generateAnnotations
    ).parseSysMD()

    try {
        if (settings.initialize) initialize()
    }  catch (exception: SysMDError) {
        report(exception)
        if (!settings.catchExceptions)
            throw exception
    }
}


/**
 * Loads a complete project usage from the repository into the session.
 * The project is first searched in the repository; if not there, in
 * the local files.
 * The method imports *all* elements into the session.
 * @param projectName Name of the project that will be imported.
 * @param initialize whether to also call initialize.
 * @param setProject whether to set the project metadata based on YAML cell
 */
fun Session.loadProject(
    projectName: String,
    initialize: Boolean = true,
    setProject: Boolean = true
) {
    val loaded = SessionManager.projectService.getProjectByName(projectName) ?: return

    val elementData: Collection<ElementDAO> = if (loaded !is ProjectData) {
        SessionManager.elementNavigationService.getElements(projectName)
    } else {
        loaded.data.filter { it.payloadElementSnapshot != null }.mapNotNull { it.payloadElementSnapshot }
    }
    import(elementData)
    if (setProject) {
        project = ProjectData(loaded)
    }
    // identify Qualified Names, etc.
    if (settings.initialize && initialize)
        initialize()
}

/**
 * Loads the default projects that contain the KerML library definitions
 * used in SysMD and SysML. Used for starting/resetting a session.
 *
 * @author Bertil Muth (HOOD GmbH), bertilmuth@hood-group.com
 */
fun Session.loadDefaultProjects(){
    loadProject("Occurrences", initialize = false)
    loadProject("Items", initialize = false)
    loadProject("Parts", initialize = false)
    loadProject("Ports", initialize = false)
    loadProject("Attributes", initialize = false)
    loadProject("Connections", initialize = false)
    loadProject("Allocations", initialize = false)
    loadProject("Interfaces", initialize = false)
    loadProject("Requirements", initialize = false)
    loadProject("States", initialize = false)
    initialize()
}

/**
 * Loads a file from the repository, but limits the import to the
 * documents persisted as annotating elements for the notebook.
 * For this purpose, it uses the element navigation service implemented
 * independent of whether the project is in a file on a web project.
 */
fun Session.loadProjectSourceOnly(projectName: String) {
    val newElements = SessionManager.elementNavigationService.getElements(projectName)

    val documents = newElements.filter {
        it.type == "AnnotatingElement" && it.owner?.id == null
    }

    val textualRepresentations = newElements.filter {
        it.type == "TextualRepresentation"
    }

    textualRepresentations.forEach {
        it.ownedElements.clear()
        if(it.language=="YaML" && it.body != null) {
            importMD(it.body!!, null)
        }
    }

    import(documents + textualRepresentations)
    project = SessionManager.projectService.getProjectByName(projectName) as ProjectData
}


/**
 * This method calls the parser with a given property, from which the dependency string is
 * parsed and the ast is created.
 */
fun Variable.parseDependency() {

    try {
        if (feature.model != null) {
            val parserSysMD = KerML(feature.model!!,
                TextualRepresentationImplementation(language = "SysMD", body = dependency),
                feature.indices
            )

            // The parsing itself, can throw exceptions that are caught optionally below.
            if (feature.model?.repo?.scalarType == null)
                feature.model?.report(feature, "Could not resolve ScalarValues::ScalarValue -- add usage of ScalarValues")
            feature.type.forEach {
                if (it.ref == null)
                    feature.model!!.report(feature, "Could not resolve Type '${feature.generalization.firstOrNull()?.str}' of feature ${feature.qualifiedName}")
            }
            if (!feature.specializes(feature.model?.repo?.scalarType))
                feature.model?.report(feature, "Expected subtype of ScalarValues::ScalarValue")

            if (dependency.isNotBlank()) {
                // set the scope to the element to which the property belongs.
                feature.owner.ref?:throw SemanticError("No owner of ${feature.qualifiedName}; initialize identifications before using services.")
                parserSysMD.semantics.namespace = feature.owningNamespace!!
                parserSysMD.semantics.expression = feature

                ast = AstRoot(feature.model!!, feature, parserSysMD.Expression())
                // Initialize internal AST nodes, starting from leaves
                ast?.runDepthFirst { initialize() }
                when {
                    feature.specializes(feature.model!!.repo.realType) -> {
                        if (ast!!.upQuantity.values[0] !is AADD)
                            throw SemanticError("Expecting dependency of type Real")
                    }

                    feature.specializes(feature.model!!.repo.booleanType) -> {
                        if (ast!!.upQuantity.values[0] !is BDD)
                            throw SemanticError("Expecting dependency of type Boolean")
                    }

                    feature.specializes(feature.model!!.repo.integerType) -> {
                        if (ast!!.upQuantity.values[0] !is IDD)
                            throw SemanticError("Expecting dependency of type Integer")
                    }
                }
            }
        }
     } catch (exception: Exception) {
        ast = null
        feature.model?.report(feature, "Error in expression '$dependency' of ${feature.qualifiedName}; problem: ${exception.message}", exception)
    }
}


/**
 * Imports a SysMD file, including its usages.
 * The file may be in .md format, and will first be pre-processed by an MD parser, and then
 * the code cells will be processed by the SysMD parser.
 * @param fileName The filename of the file in SysMD-resources or projects folder
 * @param initialize Whether to do also initialization and first propagation or only name resolution
 */
fun Session.loadSysMDFromFile(fileName: String, initialize: Boolean = true) {

    /**
     * Recursively walk through the owned elements of type TextualRepresentation and language SysMD.
     * Compile them, while considering usage of other libraries/projects, load them, etc.
     */
    fun compileRecursively(element: Element) {
        element.ownedElement.forEach {
            val ownedElement = get(it.id!!) as Element
            if (ownedElement is TextualRepresentation
                && (ownedElement.language.startsWith("SysMD")
                        ||ownedElement.language.startsWith("SysML")
                        ||ownedElement.language.startsWith("KerML"))) {
                ownedElement.compile()
            }
            compileRecursively(ownedElement) // if there are textual representations embedded in text. rep?
        }
    }

    // Search project file in resources or in SysMD-Home; just for default-libraries.
    var inputStreamSysMD = javaClass.getResourceAsStream("/projects/$fileName")

    if (inputStreamSysMD == null) {
        if (File(File(com.github.tukcps.sysmd.settings.dataFolder).canonicalPath, fileName.removeSuffix(".md")).isDirectory()) {
            val dir = File(File(com.github.tukcps.sysmd.settings.dataFolder).canonicalPath, fileName.removeSuffix(".md"))
            inputStreamSysMD = File(dir, fileName).inputStream()
        } else
            inputStreamSysMD = File(File(com.github.tukcps.sysmd.settings.projectFolder!!).canonicalPath, fileName).inputStream()
    }
    // first read the MD into the memory; creates only annotations with textual models and description annotations

    val inputString = inputStreamSysMD.bufferedReader().use { it.readText() }
    val fileAnnotation = createOrReplace(AnnotatingElementImplementation(declaredName=fileName, body = inputString), global)
    importMD(inputString, fileAnnotation)
    project.getUsages().forEach {
        if (it is ProjectUsageData)
            loadProject(it.resource.toString(), initialize = false, setProject = false)
    }
    compileRecursively(fileAnnotation)
    if (initialize && settings.initialize)
        initialize()
    else
        resolveNames()
}

/**
 * Loads a library file directly from resources into the session.
 * The method will only call the parser that creates directly the respective
 * elements in the session, and will resolve names.
 * No annotations or Textual Representations are created.
 * @param fileName the name of the file in the resources; the function will only search in
 * the resources folder "projects".
 * @param addFile if true, an annotation with the file contents is added to the project
 */
fun Session.loadLibrary(fileName: String, addFile: Boolean=true) {
    val inputStream = javaClass.getResourceAsStream("/projects/$fileName")
    val inputString = inputStream?.bufferedReader().use { it?.readText() }
    var fileAnnotation: AnnotatingElement? = null

    if (inputString == null)
        throw SysMDError("Problem reading file '$fileName' from resources")

    if (addFile) {
        fileAnnotation = createOrReplace<AnnotatingElement>(AnnotatingElementImplementation(declaredName = fileName, body = inputString), global)
    }

    val cells = splitMarkdown(markdownString = inputString)

    // Get all usages
    cells.forEach {
        if (it is TextualRepresentation && it.language == "YaML")
            importMD(it.body, null)

        if (addFile && fileAnnotation!=null)
            create(it, fileAnnotation)
    }

    // Compile all SysMD/KerML/SysML cells
    cells.forEach {
        if (it is TextualRepresentation && it.language in setOf("SysMD", "KerML", "SysML"))
            KerML(this, it).parseSysMD()
    }
    resolveNames()
}
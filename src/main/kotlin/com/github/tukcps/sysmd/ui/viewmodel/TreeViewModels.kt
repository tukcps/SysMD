package com.github.tukcps.sysmd.ui.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.tukcps.sysmd.cspsolver.Variable
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.PackageImplementation
import com.github.tukcps.sysmd.model.sysml.AcceptActionUsage
import com.github.tukcps.sysmd.model.sysml.SuccessionAsUsage
import com.github.tukcps.sysmd.model.sysml.TransitionUsage
import com.github.tukcps.sysmd.rest.AgilaRepository.getBranches
import com.github.tukcps.sysmd.rest.AgilaRepository.getCommits
import com.github.tukcps.sysmd.rest.entities.BranchImplementation
import com.github.tukcps.sysmd.rest.entities.ProjectImplementation
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.composables.TreeViewNodeModel
import com.github.tukcps.sysmlv2.entities.Commit
import com.github.tukcps.sysmlv2.entities.Project
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.MicrochipSolid
import java.io.File

val ModelsFolder: File get() = File(settings.dataFolder)

/**
 * NOTE: TreeView is a generic composable that visualizes and edits trees.
 * The composable is in the package "composables".
 * It only needs a tree as a parameter that implements the TreeViewNode interface.
 * The class AgilaInheritanceTree sets up such a tree for a given Agila model.
 */
class AgilaInheritanceTree(
    var elem: Type,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {
    override val hasChildren: Boolean
        get() = elem.subclasses().isNotEmpty()
    override val name: String
        get() = elem.escapedName()?:""
    override fun children(): List<TreeViewNodeModel> {
        if (elem.declaredName == "Any")
            elem = elem.model!!.any
        val elems =  elem.subclasses()
        val result = mutableListOf<TreeViewNodeModel>()
        elems.forEach {
            try {
                result.add(AgilaInheritanceTree(it))
            } catch (ignore: Exception) {}
        }
        return result
    }
    override fun icon(): ImageVector {
        // Icons from: https://icons8.com/line-awesome
        return LineAwesomeIcons.MicrochipSolid
    }
    fun getElem(): Element {return elem}
}

/**
 * NOTE: TreeView is a generic composable that visualizes and edits trees.
 * The composable is in the package composables.
 * It only needs a tree as a parameter that implements the TreeViewNode interface.
 * The class AgilaCompositionTree sets up such a tree for a given Agila model.
 */
class AgilaCompositionTree(
    var elem: Element,  // Element of entities package
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {
    override val hasChildren: Boolean
        get() {
            if (ignoreChildren) return false
            // also display Relationships as owned elements ... not entirely correct.
            // val rels = if (elem is Relationship) {
            //    (elem as Relationship).targets.isNotEmpty()
            // } else false
            return elem.ownedElement.isNotEmpty() // or rels
        }

    override val name: String
        get() = elem.generateName()

    override fun children(): List<TreeViewNodeModel> {
        if (elem.declaredName=="Global")
            elem = elem.model!!.global
        val result = mutableListOf<TreeViewNodeModel>()
        elem.ownedElement.forEach {
            try {
                val elem = it.ref
                if (elem != null)
                    result.add(AgilaCompositionTree(elem))
            }
            catch (ignore: Exception) { }
        }
        /*
        if (elem is Relationship) {
            (elem as Relationship).source.forEach { if (it.ref != null) result.add(AgilaCompositionTree(it.ref!!, true)) }
            (elem as Relationship).target.forEach { if (it.ref != null) result.add(AgilaCompositionTree(it.ref!!, true)) }
        } */
        return result
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return when (elem) {
            is Variable -> Icons.Default.Functions
            is PackageImplementation -> Icons.Default.Folder
            is TextualRepresentation -> Icons.Default.TextFields
            else -> LineAwesomeIcons.MicrochipSolid
        }
    }
}

/**
 * Function that generates the name for display in a UI.
 */
fun Element.generateName(): String {
    val nameStr = (if (declaredShortName!=null) "<$declaredShortName>" else "") +
                  (if (declaredName!=null) declaredName else "")
    try {
        return when(this) {
            is Anything        -> "Base::Anything"
            is Import          -> "Import '$target'"
            is Multiplicity    -> "$elementType ${variable?.vectorQuantity}"
            is Association     -> "$elementType $nameStr :> ${allSupertypes().first().qualifiedName} $source -> $target"
            is Annotation      -> "$elementType $nameStr: $target"
            is Specialization  -> "$elementType $target"
            is AcceptActionUsage -> "$elementType '${payloadParameter.ref?.type?.get(0)?.ref?.escapedName()}'"
            is TransitionUsage   -> "$elementType '${source.ref?.escapedName()}' -> '${target.ref?.escapedName()}'"
            is SuccessionAsUsage -> "$elementType '${source[0].ref?.escapedName()}' -> '${target.firstOrNull()?.ref?.escapedName()}'"
            is Relationship    -> "$elementType $nameStr $source -> $target"
            is TextualRepresentation -> "TextualRepresentation $language"
            is Feature         -> { "$elementType $nameStr ${if (this.isEnd) "(end)" else ""}" +
                if (variable != null) " = ${variable!!.vectorQuantity}" else ""
            }
            else -> if (owningNamespace == null) "Root" else "$elementType $nameStr ${if (this.isStandard) "(standard)" else ""}"
        }
    } catch (issue: Exception) {
        return "( problem with '${escapedName()}': $issue)"
    }
}


/**
 * NOTE: TreeView is a generic composable that visualizes and edits trees.
 * The composable is in the package composables.
 * It only needs a tree as a parameter that implements the TreeViewNode interface.
 * The class AgilaFileTree sets up such a tree for a directory.
 * @param file: The root of the directory tree that is displayed.
 */
class AgilaFileTree(
    var file: File,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {
    override val hasChildren: Boolean
        get() = file.isDirectory && (file.listFiles()?.size ?: 0) > 0

    override val name: String
        get() = file.name

    override fun children(): List<TreeViewNodeModel> {
        val files = file.listFiles { dir , name -> !name.startsWith(".") &&
                ( File(dir, name).isDirectory|| name.endsWith(".md") || name.endsWith(".sysmd") || name.endsWith(".sysml"))}.orEmpty().toList()
        val treeNodes = mutableListOf<TreeViewNodeModel>()
        files.forEach {
            treeNodes.add(AgilaFileTree(it))
        }
        return treeNodes
    }
    override fun icon() = Icons.Default.FilePresent
}

class AgilaProjectsTree(
    var projects: MutableList<ProjectImplementation>,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    override val hasChildren: Boolean
        get() {
            return projects.isNotEmpty()
        }

    override val name: String
         get() {
             return if (projects.isNotEmpty()){
                 " Repository projects"
             } else {
                 " No connection to backend"
             }
            }

    override fun children(): MutableList<TreeViewNodeModel> {
        val projectsTree = mutableListOf<TreeViewNodeModel>()
        projects.forEach {
            projectsTree.add(AgilaProjectTree(it))
        }
        return projectsTree
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return Icons.Default.AddTask
    }
}



class AgilaProjectTree(
    private var project:ProjectImplementation,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    override val hasChildren: Boolean
        get() = project.branches.isNotEmpty()

    override val name: String
        get() = project.name

    val description: String
        get() = project.description

    override fun children(): MutableList<TreeViewNodeModel> {
        val projectTree = mutableListOf<TreeViewNodeModel>()

        projectTree.add(AgilaOpenProjectNode(project))
        projectTree.add(AgilaBranchesTree(project))
        projectTree.add(AgilaCommitsTree(project))

        return projectTree
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return Icons.Default.AddTask
    }
}

class AgilaOpenProjectNode(
    var project: ProjectImplementation,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    override val hasChildren: Boolean
        get() = false


    override val name: String
        get() = "Open Project"

    val description: String
        get() = "Opens Project on Reference Branch with reference Commit."

    override fun children(): List<TreeViewNodeModel> {
        return emptyList()
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return Icons.Default.FilePresent
    }
}

class AgilaBranchesTree(
    var project: ProjectImplementation,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    override val hasChildren: Boolean
        get() = project.branches.isNotEmpty()


    override val name: String
        get() = "Branches"

    val description: String
        get() = "Opens Branches of the Project"

    override fun children(): List<TreeViewNodeModel> {
        val branchTree = mutableListOf<TreeViewNodeModel>()

        val projectID = project.id
        val branches = getBranches(projectID)

        for (branch in branches ) {
            branchTree.add(AgilaBranchTree(project, branch))
        }
        return branchTree
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return Icons.Default.FilePresent
    }
}


class AgilaCommitsTree(
    var project: ProjectImplementation,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    override val hasChildren: Boolean
        get() = project.commits.isNotEmpty()


    override val name: String
        get() = "Commits"

    val description: String
        get() = "Lists all Branches of Project"

    override fun children(): List<TreeViewNodeModel> {
        val projectTree = mutableListOf<TreeViewNodeModel>()

        val projectID = project.id
        val commits = getCommits(projectID)

        for (commit in commits ) {
            projectTree.add(AgilaCommitTree(project, commit))
        }
        return projectTree
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return Icons.Default.FilePresent
    }
}

class AgilaBranchTree(
    var project: Project,
    var branch: BranchImplementation,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    override val hasChildren: Boolean
        get() = false


    override val name: String
        get() = branch.name

    val description: String
        get() = branch.description

    override fun children(): List<TreeViewNodeModel> {
        return emptyList()
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return Icons.Default.FilePresent
    }
}

class AgilaCommitTree(
    var project: Project,
    var commit: Commit,
    override val ignoreChildren: Boolean = false
) : TreeViewNodeModel {

    override val hasChildren: Boolean
        get() = false


    override val name: String
        get() = commit.description

    val description: String
        get() = commit.description

    override fun children(): List<TreeViewNodeModel> {
        return emptyList()
    }

    // Method that, for a given element, determines an icon.
    override fun icon(): ImageVector {
        return Icons.Default.FilePresent
    }
}

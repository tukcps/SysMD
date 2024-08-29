package com.github.tukcps.sysmd.services

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.Session
import com.github.tukcps.sysmd.services.session.SessionManager
import com.github.tukcps.sysmd.settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The global index of components.
 * Note: The component name is stored together with the name of the package it belongs to as one single string.
 * The format looks like this: *component_name::package_name*
 * When using SysMD the name of the package must be in front of the component name.
 */
var globalComponentsIndex : MutableSet<String> = mutableSetOf("Anything::Base") //As "Anything" is a build in class, its declaration cannot be found in the files and therefore is initially added here

/**The global index of Packages*/
var globalPackagesIndex : MutableSet<String> = mutableSetOf("SI")

val indexerSession  = SessionManager.startSession()



/**
 * The Indexer is used to create Indexes for Packages and Components defined in the projects.
 * The Indexes can then be used to provide suggestions to the user when writing or highlight the text.
 */
class Indexer {

    /**
     * Initializes the Index lists by scanning all Markdown files of the SysMD data Folder
     */
    suspend fun initializeIndexes(progressBarValue: MutableState<Float>) {
        println("INDEXER: Start indexing files...")

        val tempComponents = mutableStateOf( mutableSetOf<String>() )
        val tempPackages = mutableStateOf( mutableSetOf<String>() )

        //Build a list of all SysMD files in the directory
        val sysMDFiles = mutableListOf<File>()
        File(settings.dataFolder).walk(FileWalkDirection.TOP_DOWN).forEach { file ->
            if(file.extension == "md") sysMDFiles.add(file)
        }

        //Set progressBar to zero
        progressBarValue.value = 0.0F

        sysMDFiles.forEachIndexed{ index, file ->
            tempComponents.value.clear()
            tempPackages.value.clear()

            progressBarValue.value = ((1.0/sysMDFiles.size) * (index+1)).toFloat()

            withContext(Dispatchers.Default){
                indexerSession.textToElements(getSysMDTexts(file.readText(Charsets.UTF_8)))

                indexElements(indexerSession.global, tempPackages, tempComponents)

                globalComponentsIndex.addAll(tempComponents.value)
                globalPackagesIndex.addAll(tempPackages.value)
            }

            //Update the progress bar
            progressBarValue.value = ((1.0/sysMDFiles.size) * (index+1)).toFloat()
        }

        globalPackagesIndex
        globalComponentsIndex

        //Set the progress bar to full (is needed because the forEach block can fail and then the bar will be stuck)
        progressBarValue.value = 1f

        println("INDEXER: Global indexes successfully initialized")
    }

    private fun indexElements(
        ele: Element,
        tempPackages: MutableState<MutableSet<String>>,
        tempComponents: MutableState<MutableSet<String>>
    ){

        when(ele.javaClass.simpleName.toString()){
            "PackageImplementation" -> tempPackages.value.add(ele.declaredName.toString())
            "ClassImplementation","PartUsageImplementation","PartDefinitionImplementation" -> tempComponents.value.add(ele.declaredName.toString())
        }

        //Recursively call this function on element children
        ele.ownedElement.forEach { child ->
            indexElements(child.ref!!, tempPackages, tempComponents)
        }
    }

    private fun getSysMDTexts(text: String) : String {
        val parts = text.split("```")
        val strBld = StringBuilder("")

        parts.forEach { part ->
            if(part.startsWith("SysMD")) strBld.append("\n"+part.substringAfter("SysMD"))
        }

        return strBld.toString()
    }

    /**Builds indexes for a given SysMD model.*/
    fun TextFieldValue.buildLocalIndexes(
        localComponentsIndexReference: MutableState<MutableSet<String>>,
        localPackagesIndexReference: MutableState<MutableSet<String>>
    ) {
        indexerSession.textToElements(this.text)
        indexElements(
            indexerSession.global,
            tempComponents = localComponentsIndexReference,
            tempPackages = localPackagesIndexReference
        )
    }

    /**Updates the local indexes of a TextFieldValue and also applies changes to the global indexes
     * (for instance if a component was deleted in this TFV, it will also be removed from the global indexes)
     */
    fun TextFieldValue.updateLocalIndexes(
        localComponentsIndexReference: MutableState<MutableSet<String>>,
        localPackagesIndexReference: MutableState<MutableSet<String>>
    ) {
        //Store old indexes to compare them to the new indexes.
        //Necessary to be able to detect if an entry was removed.
        val oldComponents = mutableSetOf<String>()
        val oldPackages = mutableSetOf<String>()

        oldComponents.addAll(localComponentsIndexReference.value)
        oldPackages.addAll(localPackagesIndexReference.value)

        localComponentsIndexReference.value.removeAll(localComponentsIndexReference.value)
        localPackagesIndexReference.value.removeAll(localPackagesIndexReference.value)

        //Update the local indexes
        this.buildLocalIndexes(localComponentsIndexReference = localComponentsIndexReference, localPackagesIndexReference = localPackagesIndexReference)

        //Update the global indexes
        globalComponentsIndex.applyLocalChangesToGlobal(oldIndex = oldComponents, newIndex = localComponentsIndexReference.value)
        globalPackagesIndex.applyLocalChangesToGlobal(oldIndex = oldPackages, newIndex = localPackagesIndexReference.value)

    }

    /** Takes a new SysMD text, analyzes it and returns the root Element
     * @param text A String that represents the SysMD code
     * @return The root Element of the built KerML tree
     */
    private fun Session.textToElements(text : String) : Element{

        this.reset()
        this.loadSysMD(text)
        this.initialize(1)
        return this.global
    }

    /**Updates the global indexes by comparing and old Index to a new Index.
     * New entries will be added to the global index while deleted entries will be removed.
     */
    private fun MutableSet<String>.applyLocalChangesToGlobal(oldIndex : MutableSet<String>, newIndex : MutableSet<String>){
        //What exists in the OLD but NOT in the NEW index has to be REMOVED
        this.removeAll(oldIndex.subtract(newIndex))

        //What exists in the NEW but NOT in the OLD index has to be ADDED
        this.addAll(newIndex.subtract(oldIndex))

    }

}
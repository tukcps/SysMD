@file:Suppress("PropertyName")

package com.github.tukcps.sysmd.ui.viewmodel

import org.commonmark.node.Heading
import org.commonmark.node.LinkReferenceDefinition
import org.commonmark.node.Node
import org.commonmark.node.Text
import java.util.*

/**
 * A class for generating the table of contents and references
 */
class InternalRefReference (
    val editorTabModel: TabViewModel?,
    onUpdateAction: ()->Unit
) {
    fun generateHeadingNumbering() {
        if (Headings.isEmpty())
            return

        @Suppress("UNCHECKED_CAST")
        val headingsCopy = Headings.clone() as LinkedList<Pair<Int,String>>
        headingsCopy.sortBy { it.first }
        val highestHeadingNumber = headingsCopy.last()
        val headingsNumberingArray = IntArray(highestHeadingNumber.first){0}

        for (heading in Headings) {
            var intent = ""

            headingsNumberingArray[heading.first-1]++

            if(heading.first<headingsNumberingArray.size){
                for(i in heading.first until headingsNumberingArray.size)
                    headingsNumberingArray[i]=0
            }

            for (i in 0 until heading.first){
                intent += headingsNumberingArray[i].toString()
                intent += if(i<(heading.first-1))
                    "."
                else
                    " "
            }

            HeadingsWithNumbering.add(Pair(intent,heading.second))
        }
    }

    fun updateTOC() {
        OnUpdateAction()
    }

    private fun getStringOfHeading(heading:Heading): Pair<Int, String> {
        val text = heading.firstChild as Text
        return Pair(heading.level,text.literal)
    }

    fun reset() {
        Headings.clear()
        HeadingsWithNumbering.clear()
        refReferenceOfElements.clear()
    }

    fun generateRefReferenceOfElements(viewModel: TextualRepresentationViewModel?, document: Node?) {
        if (document == null)
            return

        var element = document.firstChild
        do {
            if (element is Heading) {
                    Headings.add(getStringOfHeading(element))
                    if (viewModel != null)
                        addIfNotAlreadyThere(
                            viewModel,
                            generateInternalLinkForHeading((element.firstChild as Text).literal)
                        )
                }

            if (element is LinkReferenceDefinition)
                if(viewModel != null)
                    addIfNotAlreadyThere(viewModel, element.label)

            if (element != null)
                element = element.next

        } while (element != null)
    }

    private fun addIfNotAlreadyThere(viewModel:TextualRepresentationViewModel, value:String){
        if (refReferenceOfElements.containsKey(value)) {
            return
        } else {
            refReferenceOfElements[value]=viewModel
        }
    }

    private fun generateInternalLinkForHeading(headingTitle:String):String {
        if(headingTitle.isEmpty())
            return ""

        return "#"+headingTitle.lowercase().replace(' ','-')
    }

    fun getIndexOfNewActiveElement(identifier: String):Int {
        val scrollToElement = refReferenceOfElements[identifier]
        var index = 0
        for(i in 0 until editorTabModel?.cells?.size !!)
            if(editorTabModel.cells[i]==scrollToElement)
                index = i

        editorTabModel.selectedIndex.value =index
        return index
    }

    private val refReferenceOfElements = hashMapOf<String,TextualRepresentationViewModel>()
    internal var OnUpdateAction:()->Unit = onUpdateAction
    internal val Headings = LinkedList<Pair<Int, String>>()
    val HeadingsWithNumbering = LinkedList<Pair<String, String>>()
}

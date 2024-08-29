package com.github.tukcps.sysmd.ui.rendering

import com.github.tukcps.sysmd.ui.viewmodel.InternalRefReference
import org.commonmark.node.*
import java.util.*

fun isShortTOCElement(element:Node?): Boolean {
    return if((element is Paragraph)&&(element.firstChild is Text)){
        val text = element.firstChild as Text
        isElementTOCElement(element) and text.literal.lowercase().contains("(...)")
    }
    else
        false
}

fun isElementTOCElement(element:Node?): Boolean {
    return if((element is Paragraph)&&(element.firstChild is Text)){
        val text = element.firstChild as Text
        (text.literal.lowercase().contains("[toc]")) or
                (isBigTOCCommand(element)) or
                (text.literal.lowercase().contains("{:toc}"))
    }
    else
        false
}

fun isBigTOCCommand(element:Node?): Boolean {
    val text = element?.firstChild as Text
    if(text.literal == "[["){
        val emphisedText = element.firstChild?.next as Emphasis
        val emphisizedText = emphisedText.firstChild as Text
        val nextElementText = emphisedText.next as Text
        return (emphisizedText.literal.lowercase() == "toc") and (nextElementText.literal=="]]")
    }
    return false
}


class TableOfContentsRenderer(headingsModel: InternalRefReference) {

    fun generateTOCAsParagraphElement(): Paragraph {
        val returnValue = Paragraph()

        if(headings.isEmpty()){
            return returnValue
        }

        for (heading in headings) {
            val textValue = heading.first + heading.second
            if(!textValue.contains("title")) {
                val referenceValue = "#" + heading.second.replace(' ', '-').lowercase()
                val addedLink = Link(referenceValue, textValue)
                addedLink.appendChild(Text(textValue))
                returnValue.appendChild(addedLink)
                returnValue.appendChild(HardLineBreak())
            }
        }
        return returnValue
    }

    fun generateSmallTOC(): Paragraph {
        val returnValue = Paragraph()

        if(headings.isEmpty()){
            return returnValue
        }

        for (heading in headings) {
            val textValue = heading.first + heading.second
            val referenceValue="#"+heading.second.replace(' ','-').lowercase()
            val addedLink = Link(referenceValue,textValue)
            addedLink.appendChild(Text(textValue))
            returnValue.appendChild(addedLink)
            returnValue.appendChild(Text("(...)"))
            returnValue.appendChild(HardLineBreak())
            return returnValue
        }
        return returnValue
    }

    private var headings: LinkedList<Pair<String, String>>

    init {
        headings = headingsModel.HeadingsWithNumbering
    }
}
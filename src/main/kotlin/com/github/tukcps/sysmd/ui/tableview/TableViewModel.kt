package com.github.tukcps.sysmd.ui.tableview

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.ui.tableview.TableType.*
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel
import com.github.tukcps.sysmd.ui.viewmodel.TextualRepresentationViewModel.Companion.Language


class TableViewModel(model: TextualRepresentationViewModel) {
    
    private var model by mutableStateOf(model)
    
    private val _lang: MutableState<Language> = model.language
    val lang: MutableState<out Language> get() = _lang
    
    private val _tableTreeRoot: MutableState<TableTreeNode>
    val tableTreeRoot: TableTreeNode get() = _tableTreeRoot.value
    
    private var _wasVisible by mutableStateOf(false)
    val wasVisible: Boolean get() = _wasVisible
    
    val setVisible: Unit get() {
        _wasVisible = true
    }
    
    val hasChanged: Boolean get() = !tableTreeRoot.hasNotChanged()
    
    init {
        _tableTreeRoot = mutableStateOf(internalBuild())
    }
    
    //build tables from body
    fun build() {
        _tableTreeRoot.value = internalBuild()
    }
    
    private fun internalBuild(): TableTreeNode {
        //instantly return an empty package table if body is empty
        if (model.body.value.text.isNullOrBlank()) return TableTreeNode(PACKAGE, null)
        
        var lineCounter = 0
        val tr = TextReader(model.body.value.text)
        var currentTable: TableTreeNode = TableTreeNode(ERROR, null)
        //look for the first table
        while(currentTable.type == ERROR && tr.next) {
            lineCounter++
            tr.kw?.let {
                currentTable = TableTreeNode(
                    type = TableType.tableTypeOf(tr.kw),
                    parent = null,
                    dependencyType = tr.rel,
                    flags = tr.flags,
                    originalLine = tr.line,
                    values = TableType.tableTypeOf(tr.kw).buildValues(tr)?.toList() ?: listOf(),
                )
            }
        }
        //every call of "next" updates the values of tr with the next recognised line of text/code
        while(tr.next) try { //try/catch to prevent trying to convert random text from crashing the application
            lineCounter++
            // if match doesn't contain relevant data: add as hidden element
            if (tr.isEmpty) {
                currentTable.add(type = ANON, originalLine = tr.line)
            } else {
                //set table type to default choice for keyword def if def is true OR default choice for keywords OR hidden if no known keyword is found
                val baseType = TableType.tableTypeOf(
                    if (tr.def && tr.kw.notNull()) "${tr.kw} def"
                    else tr.kw
                )
                //check if default choice is right in case of ambiguous mappings
                val type = when {
                    baseType == CONN_USE && tr.dirCon     -> CONN_USD
                    baseType == INTR_USE && tr.dirCon     -> INTR_USD
                    baseType == ATTR_RNG && tr.vr == null -> ATTR_EXP
                    baseType == ANON     && tr.dirCon     -> AN_COND
                    baseType == ANON     && tr.uc         -> AN_CON
                    baseType == ANON     && tr.be != null -> ANON_EXP
                    else                                  -> baseType
                }
                
                //create table according to keyword and values in tr
                if (tr.line?.trim() != "}") currentTable.add(
                    type = type,
                    dependencyType = tr.rel,
                    flags = tr.flags,
                    originalLine = tr.line,
                    readingMode = true,
                    values = (type.buildValues(tr) ?: arrayOf<String>())
                )
            }
            
            //when line contains more { than }: go deeper, else go to parent or stay in this node
            when {
                tr.braces.first > tr.braces.second && currentTable.children.isNotEmpty() -> currentTable = currentTable.children.last()
                tr.braces.first > tr.braces.second                                       -> println("Table builder: Unexpected '{' in line number $lineCounter, line: \"${tr.line}\"")
                tr.braces.first < tr.braces.second && currentTable.parent.notNull()      -> currentTable = currentTable.parent!!
                tr.braces.first < tr.braces.second && tr.hasNext                         -> println("Table builder: Unexpected '}' in line number $lineCounter, line: \"${tr.line}\"")
            }
            
            
        } catch(e: Exception) {
            println(buildString {
                appendLine("Table builder: Skipped line \"${tr.line}\", reason:")
                append("\t")
                e.localizedMessage.trim().let {
                    append("$it at: ")
                    e.stackTraceToString().lines().forEach {
                        append("\t")
                        appendLine(it)
                    }
                }
            } )
        }
        return currentTable.root
    }
    
    
    //set body to text representation of tables
    fun toText() {
        model.body.value = TextFieldValue(toText(tableTreeRoot))
        _tableTreeRoot.value = internalBuild()
    }
    
    //convert tables to compilable text by recursively traversing the tree and all of its children
    private fun toText(table: TableTreeNode, tabsAtStart: Int = 0): String {
        
        //how many \t indentation for this line
        val indent = tabsAtStart
        
        return buildString {
            with(table) {
                //if this line was read from textual data and is unchanged:
                if (origLineUnmodified && !originalLine.isNullOrBlank()) {
                    append(indent.toTabs)
                    append(originalLine.trim())
                } else {
                    //add indent to line
                    append(indent.toTabs)
                    //convert current table to string depending on type
                    append(type.buildLine(this)?.trim())
                }
                
                //add children recursively if there are any
                if (children.isNotEmpty()) {
                    if (!origLineUnmodified) append(" {")
                    children.forEach { child ->
                        appendLine()
                        append(toText(child, indent + 1))
                    }
                    appendLine()
                    append(indent.toTabs)
                    append("}")
                } else {
                    if (!origLineUnmodified) {
                        if (type != ANON_EXP) append(";")
                        appendLine()
                    }
                }
            }
        }
    }
}



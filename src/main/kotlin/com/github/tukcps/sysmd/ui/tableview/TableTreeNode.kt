package com.github.tukcps.sysmd.ui.tableview

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.github.tukcps.sysmd.ui.tableview.TableTreeNode.Dependencies.DEFINED_BY
import com.github.tukcps.sysmd.ui.tableview.TableType.*
import com.github.tukcps.sysmd.ui.tableview.TableType.Companion.ROWS_DEFAULT
import com.github.tukcps.sysmd.ui.tableview.TableType.Companion.varColSetup
import kotlin.math.max

//tables as nodes of a recursively linked structure
class TableTreeNode(
    val type: TableType,
    parent: TableTreeNode?,
    dependencyType: String? = null,
    flags: String? = null,
    val originalLine: String? = null,
    values: Collection<String> = listOf(),
    children: Collection<TableTreeNode> = listOf(),
    //original line of text this table was generated from (if applicable)
) {
    
    //return the root of the tree structure (usually of type: package)
    val root: TableTreeNode get() = parent?.root ?: this
    
    //is this tables length dependent on the size of it's _values
    val isVarLength: Boolean = type.varColSetup != null
    
    private var _parent: TableTreeNode? by mutableStateOf(parent)
    val parent: TableTreeNode? get() = _parent
    
    //if an originalLine was provided this is set to true, but changes to false on modification of this table
    private var _origLineUnmodified: Boolean by mutableStateOf(originalLine.notNull())
    val origLineUnmodified: Boolean get() = _origLineUnmodified
    
    //in, out, etc.
    private var _flags: String? by mutableStateOf(flags)
    val flags: String? get() = _flags
    
    //:, :>, :>>, etc. if applicable
    private var _dependencyType: String? by mutableStateOf(dependencyType)
    val dependencyType: String? get() = _dependencyType
    
    //string values of this table (name, super, attribute values,...)
    private val _values: SnapshotStateList<String> = mutableStateListOf(*values.toTypedArray())
    val values by ReadOnlyStateList(_values)
    
    //children of this table i.e. things that would be on an indented line below it between braces in sysml
    private val _children: SnapshotStateList<TableTreeNode> = children.toMutableStateList()
    val children by ReadOnlyStateList(_children)
    
    //shorthand for readability
    val hasFlags get() = !flags.isNullOrBlank()
    
    //could this be a var length table with all extra values in a single row e.g. connect (a,b,c)?
    val isVLngSnglRow: Boolean =
        //if this is null it's a normal table
        type.varColSetup?.let { vcsList ->
            //if this is null it's a VLSR table
            (vcsList.getOrNull(0).isNull()).also {
                if (it && type.kw.e.isEmpty()) _values.removeAll(is2Slash)
            }
        } ?: false
    
    init {
        //add missing empty fields if needed
        _values.addAll(buildList {
            val missing =
                type.columnSetup
                    ?.lastIndex
                    ?.minus(_values.size)
                    ?.coerceAtLeast(0)
            
            addAll(List<String>(size = missing ?: 0) { "" })
            if (type.kw.e.isNotBlank()) when {
                isVLngSnglRow && _values.none(is2Slash)                                        -> addAll(listOf("//", "", "//"))
                isVLngSnglRow && _values.count(is2Slash) == 1 && _values.lastOrNull().is2Slash -> addAll(listOf("", "//"))
                isVLngSnglRow && _values.count(is2Slash) == 1                                  -> addAll(listOf("//"))
                isVarLength && _values.none(is2Slash)                                          -> addAll(listOf("//", "", "//", "to", "//", "", "//"))
                isVarLength && _values.count(is2Slash) == 1 && _values.lastOrNull().is2Slash   -> addAll(listOf("", "//", "to", "//", "", "//"))
                isVarLength && _values.count(is2Slash) == 1                                    -> addAll(listOf("//", "to", "//", "", "//"))
                isVarLength && _values.count(is2Slash) == 2 && _values.lastOrNull().is2Slash   -> addAll(listOf("to", "//", "", "//"))
                isVarLength && _values.count(is2Slash) == 2                                    -> addAll(listOf("//", "", "//"))
                isVarLength && _values.count(is2Slash) == 3 && _values.lastOrNull().is2Slash   -> addAll(listOf("", "//"))
                isVarLength && _values.count(is2Slash) == 3                                    -> addAll(listOf("//"))
            }
        })
        //every modification after init should set this to false
        _origLineUnmodified = originalLine.notNull()
    }
    
    private val VLVL by lazy { VarLengthValueLink() }
    
    //connection relation if there is one
    val conRel: String? get() = VLVL[0].second
    
    val VLVlengths: Pair<Int, Int> get() = VLVL.VLVlengths
    
    //how many rows does this table need? (usually 2, except for tables with variable _values length like connection)
    val rows: Int
        get() = when {
            isVLngSnglRow -> 1
            isVarLength   -> ROWS_DEFAULT - 1 + VLVL.VLVmaxLength
            else          -> ROWS_DEFAULT
        }
    
    //sorted by type
    val childrenInOrder
        get() = ReadOnlyStateList<ReadOnlyStateList<TableTreeNode>>(
            children
                .sortedBy { it.type.ordinal }
                //group similar children but not connections/similar
                .fold(mutableListOf<List<TableTreeNode>>()) { acc, child ->
                    val simPrevSib = acc.lastOrNull()?.lastOrNull()?.let { prevChild ->
                        child.type !in setOf(AN_COND, AN_CON, INTR_USD, INTR_USE, CONNECTR, CONN_USD, CONN_USE) &&
                        prevChild.type == child.type &&
                        prevChild.isChildless &&
                        child.isChildless &&
                        prevChild.dependencyType.isNullOrEmpty() &&
                        child.dependencyType.isNullOrEmpty()
                    } ?: false
                    
                    if (simPrevSib)
                        acc[acc.lastIndex] = acc.last() + child
                    else
                        acc += listOf(child)
                    acc
                }
                .map { ReadOnlyStateList(it.toMutableStateList()) }
                .toMutableStateList()
        )
    
    //does this node have no children/are all children hidden nodes?
    val isChildless: Boolean get() = children.isEmpty() || children.all { it.type in setOf(ANON, ANON_EXP) }
    
    private val isaIndex = type.columnSetup?.indexOfFirst { it == "is a" }
    
    //recursively check if this node and every node below is unchanged
    fun hasNotChanged(): Boolean =
        if (origLineUnmodified)
            children.all(TableTreeNode::hasNotChanged)
        else false
    
    //change a value to newVal at given _values index
    @Suppress("ReplaceNotNullAssertionWithElvisReturn")
    fun changeVal(newVal: String, index: Int? = null) {
        _origLineUnmodified = false
        _values[index ?: 0] = newVal
        if (index.notNull() && index + 1 == isaIndex)
            if (newVal.isEmpty()) _dependencyType = ""
            else if (dependencyType.isNullOrEmpty()) _dependencyType = DEFINED_BY.component3()
    }
    
    //add a subordinate table
    fun add(
        type: TableType,
        dependencyType: String? = null,
        flags: String? = "",
        originalLine: String? = null,
        readingMode: Boolean = false,
        vararg values: String,
    ) {
        _origLineUnmodified = readingMode
        _children.add(
            TableTreeNode(
                type = type,
                parent = this,
                dependencyType = dependencyType,
                flags = flags,
                originalLine = originalLine,
                values = values.toList(),
            )
        )
    }
    
    //change dependency type
    fun changeDep(newDep: Dependencies) {
        _origLineUnmodified = false
        _dependencyType = newDep.symbol
    }
    
    //change flags
    fun changeFlags(newFlags: String?) {
        _origLineUnmodified = false
        _flags = newFlags
    }
    
    //delete this table
    @Suppress("ReplacePrintlnWithLogging")
    fun deleteTable() {
        _origLineUnmodified = false
        _parent?._origLineUnmodified = false
        parent.let { it?._children?.remove(this) }
        ?: println("TableViewModel.TableTree.deleteTable(): Table could not be deleted because ${if (parent == null) "parent was null" else "parents children is null"}")
    }
    
    //unwrap children (=> delete this table and add children to parent)
    fun unwrap() {
        _origLineUnmodified = false
        _parent?._origLineUnmodified = false
        parent?._children?.addAll(_children.onEach { it._parent = this.parent })
        parent.let { it?._children?.remove(this) }
        ?: println("TableViewModel.TableTree.deleteTable(): Table could not be deleted because ${if (parent == null) "parent was null" else "parents children is null"}")
    }
    
    fun getVLV(row: Int, col: Int): String? {
        when {
            isVLngSnglRow -> return VLVL[col + 1].first
            isVarLength   -> {
                return when(VLVL.convertCol(col)) {
                    0    -> VLVL[row].first
                    1    -> VLVL[row].third
                    else -> "ERROR"
                }
            }
            else          -> println("Tried to get VL-row of non VL type ($type) Table")
        }
        return "ERROR"
    }
    
    fun changeVLV(row: Int, col: Int, value: String) {
        when {
            isVLngSnglRow -> VLVL[col + 1] = Triple(value, null, null)
            isVarLength   -> {
                VLVL[row] = when(VLVL.convertCol(col)) {
                    0    -> Triple(value, null, null)
                    1    -> Triple(null, null, value)
                    else -> return
                }
            }
            else          -> println("Tried to set VL-row of non VL type ($type) Table")
        }
    }
    
    fun addVLVfield(row: Int, col: Int) {
        when {
            isVLngSnglRow -> VLVL.addFieldAfter(row + 1, col)
            isVarLength   -> VLVL.addFieldAfter(row, col)
            else          -> println("Tried to add VL-field to non VL type ($type) Table")
        }
    }
    
    fun delVLVfield(row: Int, col: Int) {
        when {
            isVLngSnglRow -> VLVL.delField(row + 1, col)
            isVarLength   -> VLVL.delField(row, col)
            else          -> println("Tried to delete VL-field of non VL type ($type) Table")
        }
    }
    
    override fun toString(): String {
        return buildString {
            val chi = _children.joinToString(", ") { it.type.toString() }
            val valu = _values.joinToString(", ")
            arrayOf(
                "",
                "TABLE:",
                "    origLineUnmodified: $origLineUnmodified, originalLine: $originalLine",
                "    type: $type, parentType: ${parent?.type}",
                "    flags: $flags, dependencyType: $dependencyType",
                "    values:",
                "        $valu",
                "    VLVL:",
                "        ${VLVL.toString()}",
                "    childrenTypes:",
                "        $chi",
            ).forEach(::appendLine)
        }
    }
    
    //provides easy access to values like connections stored in _values like this: [name, super , //, con1, con2, //, to, // con3, con4, //]
    @Suppress("PropertyName")
    private inner class VarLengthValueLink {
        private val fstDelimiter: Int?
        private var sndDelimiter: Int? by mutableStateOf(null)
        private var trdDelimiter: Int? by mutableStateOf(null)
        private var fthDelimiter: Int? by mutableStateOf(null)
        
        val VLVmaxLength
            get() = if (type.kw.e.isEmpty() && isVLngSnglRow) {
                _values.size
            } else {
                max(
                    noneNullLet(fstDelimiter, sndDelimiter) { (x, y) -> y - x - 1 } ?: 0,
                    noneNullLet(trdDelimiter, fthDelimiter) { (x, y) -> y - x - 1 } ?: 0
                )
            }
        
        val VLVlengths
            get() = when {
                type.kw.isNullOrBlank() && isVLngSnglRow -> values.size to 0
                isVLngSnglRow                            -> noneNullLet(fstDelimiter, sndDelimiter) { (x, y) -> y - x - 1 to 0 } ?: (0 to 0)
                else                                     -> {
                    Pair(
                        noneNullLet(fstDelimiter, sndDelimiter) { (x, y) -> y - x - 1 } ?: 0,
                        noneNullLet(trdDelimiter, fthDelimiter) { (x, y) -> y - x - 1 } ?: 0
                    )
                }
            }
        
        init {
            val indices = values.foldIndexed(listOf<Int>()) { index, delimiterIndices, s ->
                if (s.is2Slash) delimiterIndices + index else delimiterIndices
            }
            fstDelimiter = indices.getOrNull(0)
            sndDelimiter = indices.getOrNull(1)
            trdDelimiter = indices.getOrNull(2)
            fthDelimiter = indices.getOrNull(3)
        }
        
        //relocate indices - should be triggered by changes to _values length
        private fun relocateIndices() {
            val indices = values.foldIndexed(listOf<Int>()) { index, delimiterIndices, s ->
                if (s.is2Slash) delimiterIndices + index else delimiterIndices
            }
            sndDelimiter = indices.getOrNull(1)
            trdDelimiter = indices.getOrNull(2)
            fthDelimiter = indices.getOrNull(3)
        }
        
        //returns a triple of the strings in _values corresponding to the given row index
        operator fun get(row: Int?): Triple<String?, String?, String?> = with(values) {
            Triple(
                noneNullLet(fstDelimiter, row) { (fd, r) ->
                    (fd + r).takeIf {
                        it in (fd + 1)..<(sndDelimiter ?: 0)
                    }?.let { getOrNull(it) }
                },
                noneNullLet(sndDelimiter) { (sd) -> getOrNull(sd + 1) },
                noneNullLet(trdDelimiter, row) { (td, r) ->
                    (td + r).takeIf {
                        it in (td + 1)..<(fthDelimiter ?: 0)
                    }?.let { getOrNull(it) }
                },
            )
        }
        
        operator fun set(row: Int, values: Triple<String?, String?, String?>): Unit = values.let { (v1, v2, v3) ->
            _origLineUnmodified = false
            noneNullLet(fstDelimiter, v1) { (fd, v) -> _values[fd as Int + row] = v as String }
            noneNullLet(sndDelimiter, v2) { (sd, v) -> _values[sd as Int + 1] = v as String }
            noneNullLet(trdDelimiter, v3) { (td, v) -> _values[td as Int + row] = v as String }
        }
        
        fun convertCol(c: Int): Int {
            return when(type.varColSetup?.size) {
                0, 1, null -> 0
                2          -> c
                else       -> {
                    val leftVC = type.varColSetup!!.indexOfFirst { it == true }
                    val rightVC = type.varColSetup!!.indexOfLast { it == true }
                    when(c) {
                        leftVC  -> 0
                        rightVC -> 1
                        else    -> -1
                    }
                }
            }
        }
        
        fun addFieldAfter(row: Int, col: Int) {
            _origLineUnmodified = false
            when(convertCol(col)) {
                0    -> _values.add((fstDelimiter ?: return) + row + 1, "")
                1    -> _values.add((trdDelimiter ?: return) + row + 1, "")
                else -> println("VLVL received out of bounds index")
            }
            VLVL.relocateIndices()
        }
        
        fun delField(row: Int, col: Int) {
            _origLineUnmodified = false
            when(convertCol(col)) {
                0    -> _values.removeAt((fstDelimiter ?: return) + row)
                1    -> _values.removeAt((trdDelimiter ?: return) + row)
                else -> println("VLVL received out of bounds index")
            }
            VLVL.relocateIndices()
        }
        
        override fun toString(): String {
            return "$fstDelimiter, $sndDelimiter, $trdDelimiter, $fthDelimiter"
        }
    }
    
    enum class Dependencies(val rlName: String, val symbol: String) {
        DEFINED_BY("defined by", ":"),
        SPECIALIZES("specializes", ":>"),
        SUBSETS("subsets", ":>"),
        REFERENCES("references", "::>"),
        REDEFINES("redefines", ":>>");
        
        operator fun component1(): Dependencies = this
        operator fun component2(): String = rlName
        operator fun component3(): String = symbol
        
        companion object {
            fun mapSymbol(symbol: String): Dependencies? = symbolMap[symbol]
            private val symbolMap by lazy {
                mapOf(
                    ":" to DEFINED_BY,
                    ":>" to SPECIALIZES,
                    "::>" to REFERENCES,
                    ":>>" to REDEFINES,
                )
            }
        }
    }
    
}



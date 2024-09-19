package com.github.tukcps.sysmd.ui.tableview

internal typealias TableFun = (TextReader) -> Array<String>?
internal typealias LineFun = (TableTreeNode) -> String?

//buildTab functions - names are the order in which the TextReader properties are called
internal object ValBuildFuns {
    // @formatter:off
    val be: TableFun                            = { with(it) { buildList { add(be.e)         ;                              ;                              ;                     ;                   }.toTypedArray() } }
    val con0: TableFun                          = { with(it) { buildList { addConTuple(con0) ;                              ;                              ;                     ;                   }.toTypedArray() } }
    val con1_con2: TableFun                     = { with(it) { buildList { addConTuple(con1) ; add(to.e)                    ; addConTuple(con2)            ;                     ;                   }.toTypedArray() } }
    val name: TableFun                          = { with(it) { buildList { add(name.e)       ;                              ;                              ;                     ;                   }.toTypedArray() } }
    val name_be: TableFun                       = { with(it) { buildList { add(name.e)       ; add(be.e)                    ;                              ;                     ;                   }.toTypedArray() } }
    val name_super: TableFun                    = { with(it) { buildList { add(name.e)       ; add(super_.e)                ;                              ;                     ;                   }.toTypedArray() } }
    val name_super_con0: TableFun               = { with(it) { buildList { add(name.e)       ; add(super_.e)                ; addConTuple(con0)            ;                     ;                   }.toTypedArray() } }
    val name_super_con1_to_con2: TableFun       = { with(it) { buildList { add(name.e)       ; add(super_.e)                ; addConTuple(con1)            ; add(to.e)           ; addConTuple(con2) }.toTypedArray() } }
    val name_mult_super: TableFun               = { with(it) { buildList { add(name.e)       ; add(mult.e)                  ; add(super_.e)                ;                     ;                   }.toTypedArray() } }
    val name_mult_super_const_v: TableFun       = { with(it) { buildList { add(name.e)       ; add(mult.e)                  ; add("${super_.e}${const.e}") ; add(v ?: vr.e)      ;                   }.toTypedArray() } }
    val name_super_const_v_unit: TableFun       = { with(it) { buildList { add(name.e)       ; add("${super_.e}${const.e}") ; add(v.e.trim())              ; add(unit.e.trimBr)  ; add(at.e)         }.toTypedArray() } }
    val name_super_const_min_max_unit: TableFun = { with(it) { buildList { add(name.e)       ; add("${super_.e}${const.e}") ; add(min_max.first)           ; add(min_max.second) ; add(unit.e.trimBr)}.toTypedArray() } }
    // @formatter:on
}

//buildLine functions
internal object LineBuildFuns {
    // @formatter:off
    //  '+ string' appends it and a space to the line if it's not null
    //  '* string/stringList' does the same but also for lists
    //  '+' can only be used at the start of a line, '*' needs to be used between strings/stringLists
    //  '- string' adds the following string to the list of suffixes to be removed after building the string
    val anonExp: LineFun           = { it.buildLine { + expression                                                               } }
    val anonConnection: LineFun    = { it.buildLine { + flags * -"connect" * con0                                                } }
    val dirAnonConnection: LineFun = { it.buildLine { + flags * -"connect" * con1 * -rel * con2                                  } }
    val connection: LineFun        = { it.buildLine { + flags * keyword * name * isA1 * super1 * -"connect" * con0               } }
    val connector: LineFun         = { it.buildLine { + flags * keyword * name * isA1 * super1 * -"from" * con1 * -rel * con2    } }
    val dirConnection: LineFun     = { it.buildLine { + flags * keyword * name * isA1 * super1 * -"connect" * con1 * -rel * con2 } }
    val default: LineFun           = { it.buildLine { + flags * keyword * name * isA1 * super1                                   } }
    val defaultMult: LineFun       = { it.buildLine { + flags * keyword * name * isA2 * mult * super2 * value3                   } }
    val requireAss: LineFun        = { it.buildLine { + flags * keyword * name * boolex                                          } }
    val attrAssign: LineFun        = { it.buildLine { + flags * keyword * name * isA1 * valType * unit3B * -"=" * value          } }
    val attrRange: LineFun         = { it.buildLine { + flags * keyword * name * isA1 * valType * unit4B * -"=" * minMax * unit4 } }
    // @formatter:on
}

//modified StringBuilder
private class LineBuilder(private val table: TableTreeNode) {
    private val internalSB = StringBuilder()
    private val splitCon by lazy { table.values.splitCon() }
    private val rmSuffixes = mutableListOf<String>()
    private val multSplitRules = Regex("(?<=[\\d*])\\h*(?:\\.\\.|-(?!-))\\h*(?=[\\d*]|-\\h*\\d)")
    
    //values saved as separate properties
    val keyword get() = table.type.kw
    val isA1 get() =
        when {
            table.dependencyType.isNullOrBlank() && (!super1.isNullOrBlank()) -> ":"
            super1.isNullOrBlank()                                            -> null
            else                                                              -> table.dependencyType
        }
    val isA2 get() =
        when {
            table.dependencyType.isNullOrBlank()
            && (!super2.isNullOrBlank() /*|| !mult.isNullOrBlank()*/) -> ":"
            super2.isNullOrBlank() /*&& mult.isNullOrBlank()*/        -> null
            else                                                  -> table.dependencyType
        }
    val flags get() = table.flags
    
    //values saved in "values"-property
    val expression get() = table.values.getOrNull(0)
    val name get() = table.values.getOrNull(0)
        .takeUnless(String?::isNullOrBlank) ?: keyword?.first().toString()
    val mult get() = table.values.getOrNull(1)
        .takeUnless(String?::isNullOrBlank)
        ?.split(multSplitRules)?.let {
        when {
            it.size == 1 -> {
                "[${it.getOrNull(0)}]"
            }
            it.size > 1  -> {
                it.let { (v1, v2) ->
                    "[$v1 .. $v2]"
                }
            }
            else         -> {
                null
            }
        }
    }
    val super1 get() = table.values.getOrNull(1) //super without mult
    val super2 get() = table.values.getOrNull(2) //super with mult
        .takeUnless { it.isNullOrBlank() } /*?: if(!mult.isNullOrBlank()) {
            when(keyword) {//TODO: für nicolas: scheint nicht mehr nötig? 'part p [14];' hat bei mir kompiliert
                PART_USE.kw -> "Parts::Part"
                ITEM.kw     -> "Items::Item"
                FEAT.kw     -> "Base::Anything"
                else        -> "Base::Anything"
            }
        } else null*/
    val con0 get() = table.values.let {
        if(it.none(is2Slash)) it
        else it.dropWhile(!is2Slash).drop(1).takeWhile(!is2Slash) }
    inline val con1 get() = splitCon.getOrNull(0)
    inline val rel get() = splitCon.getOrNull(1)?.firstOrNull()
    inline val con2 get() = splitCon.getOrNull(2)
    val boolex get() = table.values.getOrNull(1).let { if(!it.isNullOrBlank()) "{ $it }" else null }
    val valType get() = table.values.getOrNull(1)
    val value get() = table.values.getOrNull(2)
    val unit3B get() = table.values.getOrNull(3).let { if(!it.isNullOrBlank()) "[$it]" else null }
    val minMax get() = when {
        table.values.getOrNull(2).isNullOrBlank() &&
        table.values.getOrNull(3).isNullOrBlank() -> ""
        table.values.getOrNull(2).isNullOrBlank() -> "[${table.values.getOrNull(3)} .. ${table.values.getOrNull(3)}]"
        table.values.getOrNull(3).isNullOrBlank() -> "[${table.values.getOrNull(2)} .. ${table.values.getOrNull(2)}]"
        else                                      -> "[${table.values.getOrNull(2)} .. ${table.values.getOrNull(3)}]"
    }
    val value3 get() = table.values.getOrNull(4)?.let { if(it.isNotBlank()) "= $it" else null}
    val unit4 get() = table.values.getOrNull(4)
    val unit4B get() = table.values.getOrNull(4)?.let { if(it.isNotBlank()) "[$it]" else null }
    
    //append if not null
    val String?.add: Unit get() {
        if(!this.isNullOrBlank()) internalSB.append(this)
    }
    val List<String?>?.add: Unit get() {
        if(!this.isNullOrEmpty()) internalSB.appendCon(this.filterNotNull())
    }
    //append this and a space if not null
    val String?.add_: Unit get() {
        if(!this.isNullOrBlank()) internalSB.append(this, ' ')
    }
    val List<String?>?.add_: Unit get() {
        if(!this.isNullOrEmpty()) internalSB.appendCon(this.filterNotNull(), " ")
    }
    
    operator fun String?.unaryMinus(): String? = this@unaryMinus?.let { rmSuffixes.add(it); it}
    operator fun String?.unaryPlus() = this@unaryPlus.add_
    operator fun List<String?>?.unaryPlus() = this@unaryPlus.add_
    operator fun Unit.times(s: String?) = s.add_
    operator fun Unit.times(l: List<String?>?) = l.add_
    
    override fun toString(): String = internalSB.toString().trimSuffixes(rmSuffixes)
}
private inline fun TableTreeNode.buildLine(builderAction: LineBuilder.() -> Unit): String {
    return LineBuilder(this).apply(builderAction).toString()
}

private fun String.trimSuffixes(suffixes: List<String>): String {
    var result = this
    suffixes.forEach {
        result = result.trim().removeSuffix(it.trim())
    }
    return result
}

//shorthand for readability
internal val is2Slash: (String?) -> Boolean = { "//" == it }
internal val String?.is2Slash: Boolean get() = is2Slash(this)
//shorthand for readability; adds "//", converts "(a, b, c)" to listOf("a", "b", "c"), then adds each as a value to its List and adds "//" at last
private fun MutableList<String>.addConTuple(ct: String?) {
    add("//")
    for(con in ct.e
        .trim('(', ')', ' ')
        .split(",")
        .map(String::trim)
    ) {
        add(con)
    }
    add("//")
}
private val String.trimBr get() = this.trim().trim('[',']')
//shorthand for comfort; converts listOf("a", "b", "c") to "(a, b, c)" and adds it to its StringBuilder
private fun StringBuilder.appendCon(connections: List<String>, appendAtEnd: String = "") {
    connections.filter(String::isNotEmpty).let {
        if (it.size <= 1) {
            append(it.getOrNull(0).e, appendAtEnd)
        } else {
            append('(')
            for(con in connections.dropLast(1)) {
                append(con, ", ")
            }
            append(it.last(), ')', appendAtEnd)
        }
    }
}

//split values into: connections1, to-relation, connections2 (while dropping stuff before that)
private fun List<String?>.splitCon() = this
    .dropWhile(!is2Slash)
    .drop(1)
    .fold(
        0 to listOf<String>().arr(3)
    ) { (listInd, listArr), s ->
        if (is2Slash(s)) (listInd + 1) to listArr
        else listInd to listArr.apply { this[listInd] = this[listInd] + (s.e) }
    }.second

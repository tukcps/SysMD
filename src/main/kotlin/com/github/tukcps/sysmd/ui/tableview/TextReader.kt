package com.github.tukcps.sysmd.ui.tableview

import androidx.compose.runtime.*
import org.intellij.lang.annotations.Language

class TextReader(input: String) {
    //the original line
    val line: String? get() = mr?.value
    //was relevant matchable text found?
    val isEmpty: Boolean get() = mr?.groupValues?.drop(1)?.all(String::isNullOrEmpty) ?: true
    //is there a "def" at the beginning of the line?
    val def: Boolean get() = g("defc", "defd", "defa", "defr", "defk","defs") != null
    //is there a keyword in the line (part, port, ...)? which one? checks against known keywords for table-types on call
    val kw: String? get() = g("kwc", "kwd", "kwa", "kwr", "kwk","kws")
    //the name associated with said keyword (e.g. part1)
    val name: String? get() = g("namec", "named", "namea", "namer","names")
    //multiplicity (e.g. [1..2])?
    val mult: String? get() = g("multd","mult2d","mult3d","multa",)?.trim('[',']')
    //some kind of inheritance, etc (:>, :, :>>, ...)?
    val rel: String? get() = g("relc", "reld", "rela", "relr","rels")
    //super associated with inheritance/similar
    val super_: String? get() = g("superc", "supera", "superd", "superr", "supers")
    //assignment: range
    val vr: String? get() = g("vra")
    val min_max get() = vr?.trim('[', ']')?.split("..").let { (it?.first().e.trim()) to (it?.last().e.trim()) }
    //assignment: number/simple calculation/bool
    val v: String? get() = g("va")
    //assignment type of ve
    val at: String? get() = g("ata")
    //unit of val exp
    val unit: String? get() = g("unit3a", "unita", "unit2a",)
    //value constraints
    val const: String? get() = g("consta")
    //end of line: {,},;
    val eol: String? get() = g("eolc", "eold", "eol2a", "eola", "eolr", "eolb", "eolo", "eolk", "eols")
    //connected elements
    val con0: String? get() = g("con0c", "con0s")
    val con1: String? get() = g("con1c")
    val con2: String? get() = g("con2c")
    //is this an unnamed connection?
    val uc: Boolean get() =
        kw == null && (
            dirCon ||
            ((con0 ?: con1) != null)
        )
    
    //type of "to" relation in connections
    val to: String? get() = g("toc")
    //directed conn?
    val dirCon: Boolean get() = to != null
    //in or out flags
    val flags: String? get() = g("flags")
    //boolean expression for checks
    val be: String? get() = g("ber","beb")?.trim()
    //amount of opening and closing braces in this line
    val braces: Pair<Int, Int>
        get() = if (line?.trim()?.startsWith("//") != false) {
            0 to 0
        } else {
            (mr?.groups?.get(0)?.value?.count { it == '{' } ?: 0) to (mr?.groups?.get(0)?.value?.count { it == '}' } ?: 0)
        }
    
    //move to next match in text, returns true if successful and false if there is no remaining match
    val next: Boolean get() {
        if(hasNext) {
            mr = matchIterator.next()
            return true
        }
        mr = null
        return false
    }
    val hasNext get() = matchIterator.hasNext()
    
    //internal stuff
    //replace sysMD end of line "." with sysML ";" and contract multiline code for easier line-by-line matching
    private val text: String = buildString {
        val prep = input
            .replace(Regex(                                 //try to contract "xyz {\n abc \n}" type constructs to "xyz { abc }"
                "(?>((?<=\\v)\\h*\\w+\\V*\\{\\h*)\\v" +
                    "\\h*(\\S+[^;{}\\v]*)\\v" +
                "\\h*(\\}\\h*)(?=\\v))"
            )) {
                try{
                    "${it.groups.get(1)?.value} ${it.groups.get(2)?.value} ${it.groups.get(3)?.value}"
                } catch(ex: Exception){
                    println(ex.stackTraceToString())
                    ""
                }
            }
            .lines()                     //split into lines
            .filter(String::isNotBlank)  //remove empty lines
            .flatMap {it     //remove unnecessary whitespaces at the end of each line, split it up if part of it is a comment, replace '.' eol with ';' eol
                .trimEnd()
                .split("//")
                .mapIndexed {i, s->
                    if (i > 0) "//$s"
                    else if (s.lastOrNull() == '.') "${s.dropLast(1)};"
                    else s
                }
            }
        for(line in prep) {
            if (line.trimStart().startsWith("//")){ //if this line is a comment append it with linebreak at the end and continue with next
                appendLine(line)
                continue
            }
            if (isNotEmpty() && last().isWhitespace()) //if previous line ended with linebreak
                append(line)
            else if (isNotEmpty() && line.trimStart().first() in "{};"){ //if line starts with eol char and previous line did not have a proper eol char add one, and put this one into the next line
                appendLine(";")
                append(line)
            } else //getting to here should mean that this line is part of a line of code that was split between multiple lines: replace whitespaces at it's start with a single space to make it fit snugly to the prev one
                append(" ${line.trimStart()}")
            if (line.lastOrNull() != null && line.last() in "{};") //if the last char of the line added in one of the cases above is an eol char: linebreak - else this is another potential multiline declaration to be checked next iteration
                appendLine()
        }
    }
    private var matches by mutableStateOf(Rex.pattern.findAll(this.text))
    private var matchIterator by mutableStateOf(matches.iterator())
    var mr: MatchResult? by mutableStateOf(null)
    
    private fun g(vararg groupName: String): String? {
        groupName.forEach { gN ->
            mr?.groups?.get(gN)?.let { return it.value }
        }
        return null
    }
        
    @Suppress("RegExpRedundantEscape", "RemoveSingleExpressionStringTemplate")
    private object Rex {
        //chars/words that can be part of an elements name/reference
        @Language("RegExp")
        const val NAME_CHARS: String = "(?>[a-zA-Z_\\d]|::\\*|::)"
        //expanded definition for constructs like a.b(c)
        @Language("RegExp")
        const val NAME_EX: String = "(?>[a-zA-Z_:](?>$NAME_CHARS|(?>\\(\\h*(?>$NAME_CHARS+\\h*,\\h*)*$NAME_CHARS+\\h*\\)|[.()])?)*)"
        //name tuples
        @Language("RegExp")
        const val NAME_TUP: String = "(?>\\(?\\h*(?>$NAME_EX\\h*,\\h*)+$NAME_EX\\h*\\)?)"
        //chars that can be part of a keyword
        @Language("RegExp")
        const val KW_CHARS: String = "[a-z]"
        //pre keyword flags: in, out, end ...
        @Language("RegExp")
        const val FLAGS: String = "(?>(?>in\\s|out\\s|inout\\s|end\\s|derived\\s)*)"
        //end of line chars
        @Language("RegExp")
        const val EOL_CHARS: String = "(?>[{};]|[{};\\h]+[{};])"
        //numerical value chars/words
        @Language("RegExp")
        const val NUM_CHARS: String = "(?>\\d|(?<!\\d)[.,'+-](?=\\d)|(?<=\\d)[.,'^](?=\\d)|(?<=\\d)[i%'](?!\\d))"
        //range
        @Language("RegExp")
        const val RANGE: String =
            "(?>" +
                //[7 .. 10]
                "\\[\\h*(?>$NUM_CHARS+|\\*)(?:\\h*\\.\\.\\h*(?>$NUM_CHARS+|\\*)\\h*)?\\]|" +
                //(7 .. 10)
                "\\(\\h*(?>$NUM_CHARS+|\\*)(?:\\h*\\.\\.\\h*(?>$NUM_CHARS+|\\*)\\h*)?\\)" +
            ")"
        //calculations
        @Language("RegExp")
        const val CALC: String =
            "(?>" +
                "[\\(\\h]*(?>$NUM_CHARS+|$NAME_EX)\\h*[-+*\\/:]" +
                "(?>[\\(\\)\\h]*(?>$NUM_CHARS+|$NAME_EX)[\\(\\)\\h]*[-+*\\/:])*" +
                "\\h*(?>$NUM_CHARS+|$NAME_EX)[\\)\\h]*" +
            ")"
        //relations
        @Language("RegExp")
        const val RELAS: String = "(?>[:>]+|defined\\h*by|defined|by|specializes|subsets|redefines|references|is[aA])"
        //comparable expressions
        @Language("RegExp")
        const val COMP: String =
            "(?>" +
                "true|false|" +
                "$NAME_EX\\h*(?>\\[[^;}\\]\\h]+\\])?|" +
                "$NUM_CHARS+\\h*(?>\\[[^;}\\]\\h]+\\])?|" +
                "$RANGE\\h*(?>\\[[^;}\\]\\h]+\\])?|" +
                "$CALC\\h*(?>\\[[^;}\\]\\h]+\\]) ?" +
            ")"
        //boolean expressions
        @Language("RegExp")
        const val BOOL_EXP: String =
            "(?>" +
                "(?:" +
                    "(?:not|[\\h()])*$COMP[\\h()]*" +
                    "(?>==|=!|!=|<=|>=|=<|=>|[<>]|(?<=[()\\h])or(?=[()\\h])|(?<=[()\\h])and(?=[()\\h])|(?<=[()\\h])xor(?=[()\\h])|(?<=[()\\h])not(?=[()\\h]))" +
                ")+" +
                "(?:not|[\\h()])*$COMP[()\\h]*" +
            ")"
        
        
        //patterns for matchable lines with identifying match group suffix as name:
        //full line comment
        @Language("RegExp")
        const val FLC: String =
            "(?>\\/\\/.*$)"
        
        //single/zero point connection edge case
        @Language("RegExp")
        const val s: String =
            "(?>(?>" +
                "(?<kws>connection|interface|connector)\\h+(?<defs>def\\h+)?(?><'\\V+'>\\h*)?(?<names>(?<=\\D)$NAME_EX(?=\\D))\\h*" +
                "(?>(?<rels>$RELAS)\\h*(?<supers>$NAME_EX))?\\h*)?" +
            "connect\\h*(?<con0s>(?<=\\h)\\w+)?(?<eols>$EOL_CHARS+)$)"
        
        //more connection- and interface-specific line syntax
        @Language("RegExp")
        const val c: String =
            //keyword, flags, name
            "(?>(?>(?<kwc>connection|interface|connector)\\h+(?<defc>def\\h+)?(?><'\\V+'>\\h*)?(?<namec>(?<=\\D)$NAME_EX(?=\\D))\\h*" +
                    //potential relations/dependencies
                    "(?>(?<relc>$RELAS)\\h*(?<superc>$NAME_EX))?" +
                ")?" +
                "(?>(?>\\h*=?\\h*(?>connect|from)\\h*)|(?>\\h*=\\h*(?>connect|from)?\\h*))" +
                //elements to be connected e.g.: a to b OR (a, b, c) to (d, e) OR combination of both OR (a, b, c)
                "(?>" +
                    "(?>(?<con1c>(?<=\\h)$NAME_TUP|(?<=\\h)$NAME_EX)\\h*(?<toc>(?<=\\D)to|$NAME_EX)\\h*(?<con2c>(?<=\\h)$NAME_TUP|(?<=\\h)$NAME_EX)?)|" +
                    "(?<con0c>$NAME_TUP)" +
                ")\\h*" +
                //eol
            "(?<eolc>$EOL_CHARS+)$)"
        
        //default line e.g. "def port port3 {"
        @Language("RegExp")
        const val d: String =
            //flags, keywords, name
            "(?>\\h*(?<kwd>$KW_CHARS+)\\h+(?<defd>def\\h+)?(?><'\\V+'>\\h*)?(?<named>(?<=\\D)$NAME_EX(?=\\D))\\h*(?<multd>$RANGE)?" +
                //potential relations/dependencies
                "\\h*(?>(?<reld>$RELAS+)\\h*(?<mult2d>$RANGE)?\\h*(?<superd>$NAME_EX))?\\h*(?<mult3d>$RANGE)?" +
            //eol
            "\\h*(?<eold>$EOL_CHARS+)$)"
        
        
        //assignment of value expression e.g. "attribute cutoff : Integer [unit] = [5..10];"
        @Language("RegExp")
        const val a: String =
            "(?>" +
                "(?>" +
                    //flags keywords, name
                    "(?<kwa>$KW_CHARS+)\\h+(?<defa>def\\h+)?(?><'\\V+'>\\h*)?(?<namea>(?<=\\D)$NAME_EX(?=\\D))\\h*(?<multa>$RANGE)?" +
                     //potential relations/dependencies
                    "\\h*(?>(?<rela>$RELAS)\\h*(?<supera>$NAME_CHARS+)\\h*(?<consta>\\([0-9.*\\h\\-^]+\\))?\\h*)?\\h*" +
                ")" +
                "(?>" +
                    "(?>(?<unit3a>\\[?[^;}\\[\\]\\h]+\\]?)?\\h*(?<eol2a>$EOL_CHARS+)$)|" +
                    //unit, assignment type
                    "(?>" +
                        "\\h*(?<unita>\\[[^;}\\]\\h]+\\])?\\h*(?<ata>default|default\\h*=|default\\h*:=|=|:=)\\h*" +
                        "(?>" +
                            //value expression stuff
                            "(?<vra>$RANGE)|" +
                            "(?<va>" +
                                 "(?=.*[-+*\\/:].*)$CALC|" +
                                 "(?=.*(?>==|=!|!=|<=|>=|=<|=>|[<>]|(?<=[()\\h])or(?=[()\\h])|(?<=[()\\h])and(?=[()\\h])|(?<=[()\\h])xor(?=[()\\h])|(?<=[()\\h])not(?=[()\\h])).*)$BOOL_EXP|" +
                                 "$NUM_CHARS+|" +
                                 "(?>(?>\\\"(?=.*\\\")|'(?=.*')).*[\\\"'])|" +
                                 "$NAME_EX|" +
                                 "$CALC|" +
                                 "$BOOL_EXP" +
                            ")" +
                        ")" +
                        //unit
                        "\\h*(?<unit2a>\\[?[^;}\\[\\]\\h]+\\]?)?\\h*?" +
                        //eol
                        "\\h*(?<eola>$EOL_CHARS+)$" +
                    ")" +
                ")" +
            ")"
        
        //requirement/constraint/bool expr line syntax
        @Language("RegExp")
        const val r: String =
            //keyword, flags, name
            "(?>(?<kwr>$KW_CHARS+)\\h+(?<defr>def\\h+)?(?<namer>(?<=\\D)$NAME_EX(?=\\D))" +
                //potential relations/dependencies
                "\\h*(?>(?<relr>$RELAS)\\h*(?<superr>$NAME_EX))?" +
                //bool expression
                "\\h*\\{?\\h*(?<ber>$BOOL_EXP|(?:$NAME_EX\\h?)+)\\h*" +
            //eol
            "(?<eolr>$EOL_CHARS+)$)"
        
        //lone bool expr
        @Language("RegExp")
        const val b: String =
            "(?>(?<beb>$BOOL_EXP)\\h*" +
            //eol
            "(?<eolb>$EOL_CHARS+)$)"
        
        //keyword only
        @Language("RegExp")
        const val k: String =
            "(?>(?<kwk>$KW_CHARS+)(?>\\h+(?<defk>def))?\\h*" +
            //eol
            "(?<eolk>$EOL_CHARS+)$)"
        
        //line containing only eol/closure char e.g.: ";,{,}"
        @Language("RegExp")
        const val o: String =
            "(?>(?<eolo>$EOL_CHARS+)$)"
        
        //unrecognised line
        @Language("RegExp")
        const val UR =
            "(?>.*$)"
        
        
        //full regex string for line matching
        @Language("RegExp")
        const val LMS: String = "^(?>\\h)*(?<flags>$FLAGS)(?>$FLC|$s|$c|$k|$d|$a|$r|$b|$o|$UR)"
        
        //compiled pattern with multiline option to enable matching ^...$ to lines
        val pattern = Regex(LMS, RegexOption.MULTILINE)
    }
}
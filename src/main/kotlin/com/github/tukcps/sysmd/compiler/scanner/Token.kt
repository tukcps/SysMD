package com.github.tukcps.sysmd.compiler.scanner


/**
 * We represent tokens by this enum. The enum also has properties sVal and nVal in
 * which the string or numeric value of literals is saved.
 **/
class Token(
    val kind: Kind,
    val string: String,
    val number: Double = 0.0,
    val lineNo: Int = 0,
    val indices: IntRange = 0 .. 0
) {

    override fun toString(): String = when (kind) {
        Kind.NAME_LIT -> string
        Kind.INTEGER_LIT -> number.toString()
        Kind.STRING_LIT -> "\"$string\""
        else -> kind.toString()
    }

    enum class Kind {
        // Just for passing information; not really tokens
        ERROR, WHITESPACE, COMMENT,

        // Literals
        INTEGER_LIT, FLOAT_LIT, STRING_LIT, NAME_LIT,

        // SysML & SysMD Keywords for Types and Identification
        IMPORTS, DEFINES, ONE, WHEN, HAS_A,

        // SysML & KerML Keywords
        ABOUT, ABSTRACT, ACCEPT, ACTION, ALIAS, ALL, ALLOCATION, ALLOCATE, ASSOC, ASSERT, ASSUME, ATTRIBUTE,
        BEHAVIOR, BINDING, BY,
        CALCULATION, CLASS, CLASSIFIER, CONNECT, COMPOSITE, CONJUGATION, CONNECTION, CONNECTOR, CONSTRAINT,
        DATATYPE, DEF, DOC, DEPENDENCY, DERIVED,
        ELSE, END, ENTRY, EXPR,
        FIRST, FEATURE, FOR, FROM, FUNCTION,
        IF, IMPORT, IN, INOUT, INTERACTION, INTERFACE, INV, ITEM,
        LANGUAGE, LIBRARY, MESSAGE, METACLASS, NAMESPACE, NOTE,
        OCCURRENCE, OF, ORDERED, OUT,
        PACKAGE, PART, PORT, PORTION, PREDICATE, PRIVATE, PROTECTED, PUBLIC,
        READONLY, REGULAR_COMMENT, RELATIONSHIP, REQUIRE, REP, RETURN, REDEFINES, REFERENCES, REQUIREMENT,
        SATISFY, SPECIALIZATION, SPECIALIZES, STANDARD, STATE, STEP, STRUCT, STRUCTURE, SUBJECT, SUCCESSION,
        THEN, TO, TRANSITION, TYPE, TYPED,
        UNIQUE,
        // SysMLv2 Keywords Digital Twin
        MEASURABLE, CONTROLLABLE, VARIABLE,

        // Operators
        LCURBRACE { override fun toString() = "{" },
        RCURBRACE { override fun toString() = "}" },
        GT { override fun toString() = ">" },
        LT { override fun toString() = "<" },
        GE { override fun toString() = ">=" },
        LE { override fun toString() = "<=" },
        EE { override fun toString() = "==" },
        NEQ { override fun toString() = "!=" },
        EOF { override fun toString() = "EOF" },
        DOTDOT { override fun toString() = ".." },
        TRUE { override fun toString() = "true" },
        FALSE { override fun toString() = "false" },
        EQ { override fun toString() = "=" },
        PLUS { override fun toString() = "+" },
        MINUS { override fun toString() = "-" },
        ARROW { override fun toString() = "->" },
        TIMES { override fun toString() = "*" },
        STARSTAR { override fun toString() = "**" },
        CROSS { override fun toString() = "cross" }, //For cross product
        DOTProduct { override fun toString() = "dot" }, //For cross product
        DIV { override fun toString() = "/" },
        DPDP { override fun toString() = "::" },
        EXP { override fun toString() = "^" },
        AND { override fun toString() = "and" },
        OR { override fun toString() = "or" },
        NOT { override fun toString() = "not" },
        LBRACE { override fun toString() = "(" },
        RBRACE { override fun toString() = ")" },
        LCBRACE { override fun toString() = "[" },
        RCBRACE { override fun toString() = "]" },
        DOT { override fun toString() = "." },
        COMMA { override fun toString() = "," },
        DP { override fun toString() = ":" },
        SEMICOLON { override fun toString() = ";" },
        HASHTAG { override fun toString(): String = "#" },
        PERCENT { override fun toString(): String = "%" },
        EURO { override fun toString(): String = "€" },
        QUESTION { override fun toString(): String = "?" }
    }

    /** Constants for abbreviating some keywords */
    companion object Definitions {

        val WHITESPACE = setOf(' ', '\t', '\n', '\r', 13.toChar())

        val charTokens: HashMap<Char, Kind> = hashMapOf(
            '(' to Kind.LBRACE,
            ')' to Kind.RBRACE,
            '[' to Kind.LCBRACE,
            ']' to Kind.RCBRACE,
            '{' to Kind.LCURBRACE,
            '}' to Kind.RCURBRACE,
            '=' to Kind.EQ,
            '+' to Kind.PLUS,
            '-' to Kind.MINUS,
            '*' to Kind.TIMES,
            '^' to Kind.EXP,
            '&' to Kind.AND,
            '|' to Kind.OR,
            '/' to Kind.DIV,
            '<' to Kind.LT,
            '>' to Kind.GT,
            'x' to Kind.CROSS,
            '.' to Kind.DOT,
            ',' to Kind.COMMA,
            ':' to Kind.DP,
            ';' to Kind.SEMICOLON,
            '#' to Kind.HASHTAG,
            '%' to Kind.PERCENT,
            '€' to Kind.EURO,
            '?' to Kind.QUESTION,
            '~' to Kind.NOT,
            '!' to Kind.NOT
        )

        val keywords = hashMapOf(
            "about" to Kind.ABOUT,
            "abstract" to Kind.ABSTRACT,
            "accept" to Kind.ACCEPT,
            "action" to Kind.ACTION,
            "alias" to Kind.ALIAS,
            "all" to Kind.ALL,
            "allocation" to Kind.ALLOCATION,
            "allocate" to Kind.ALLOCATE,
            "and" to Kind.AND,
            "assert" to Kind.ASSERT,
            "assoc" to Kind.ASSOC,
            "assume" to Kind.ASSUME,
            "attribute" to Kind.ATTRIBUTE,
            "behavior" to Kind.BEHAVIOR,
            "binding" to Kind.BINDING,
            "by" to Kind.BY,
            "class" to Kind.CLASS,
            "classifier" to Kind.CLASSIFIER,
            "comment" to Kind.COMMENT,
            "composite" to Kind.COMPOSITE,
            "conjugation" to Kind.CONJUGATION,
            "connect" to Kind.CONNECT,
            "connection" to Kind.CONNECTION,
            "connector" to Kind.CONNECTOR,
            "constraint" to Kind.CONSTRAINT,
            "cross" to Kind.CROSS,
            "datatype" to Kind.DATATYPE,
            "def" to Kind.DEF,
            "defines" to Kind.DEFINES,
            "dependency" to Kind.DEPENDENCY,
            "derived" to Kind.DERIVED,
            "doc" to Kind.DOC,
            "dot" to Kind.DOTProduct,
            "else" to Kind.ELSE,
            "end" to Kind.END,
            "entry" to Kind.ENTRY,
            "expr" to Kind.EXPR,
            "false" to Kind.FALSE,
            "feature" to Kind.FEATURE,
            "first" to Kind.FIRST,
            "for" to Kind.FOR,
            "function" to Kind.FUNCTION,
            "hasA" to Kind.HAS_A,
            "in" to Kind.IN,
            "inout" to Kind.INOUT,
            "interaction" to Kind.INTERACTION,
            "inv" to Kind.INV,
            "isA" to Kind.SPECIALIZES,
            "item" to Kind.ITEM,
            "import" to Kind.IMPORT,
            "imports" to Kind.IMPORTS,
            "interface" to Kind.INTERFACE,
            "language" to Kind.LANGUAGE,
            "library" to Kind.LIBRARY,
            "metaclass" to Kind.METACLASS,
            "namespace" to Kind.NAMESPACE,
            "not" to Kind.NOT,
            "occurrence" to Kind.OCCURRENCE,
            "or" to Kind.OR,
            "out" to Kind.OUT,
            "ordered" to Kind.ORDERED,
            "port" to Kind.PORT,
            "portion" to Kind.PORTION,
            "predicate" to Kind.PREDICATE,
            "private" to Kind.PRIVATE,
            "protected" to Kind.PROTECTED,
            "public" to Kind.PUBLIC,
            "readonly" to Kind.READONLY,
            "redefines" to Kind.REDEFINES,
            "relationship" to Kind.RELATIONSHIP,
            "references" to Kind.REFERENCES,
            "rep" to Kind.REP,
            "requirement" to Kind.REQUIREMENT,
            "require" to Kind.REQUIRE,
            "satisfy" to Kind.SATISFY,
            "specialization" to Kind.SPECIALIZATION,
            "standard" to Kind.STANDARD,
            "state" to Kind.STATE,
            "step" to Kind.STEP,
            "struct" to Kind.STRUCT,
            "structure" to Kind.STRUCTURE,
            "subject" to Kind.SUBJECT,
            "succession" to Kind.SUCCESSION,
            "transition" to Kind.TRANSITION,
            "true" to Kind.TRUE,
            "type" to Kind.TYPE,
            "typed" to Kind.TYPED,
            "one" to Kind.ONE,
            "if" to Kind.IF,
            "from" to Kind.FROM,
            "to" to Kind.TO,
            "then" to Kind.THEN,
            "specializes" to Kind.SPECIALIZES,
            "unique" to Kind.UNIQUE,
            "package" to Kind.PACKAGE,
            "when" to Kind.WHEN,
            "part" to Kind.PART,
            "measurable" to Kind.MEASURABLE,
            "controllable" to Kind.CONTROLLABLE,
            "variable" to Kind.VARIABLE,
            "message" to Kind.MESSAGE,
            "of" to Kind.OF,
            "return" to Kind.RETURN,
            "calc" to Kind.CALCULATION
        )
    }
}

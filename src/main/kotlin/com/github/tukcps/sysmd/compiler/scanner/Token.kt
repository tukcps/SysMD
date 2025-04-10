package com.github.tukcps.sysmd.compiler.scanner


/**
 * We represent tokens by this enum. The enum also has properties sVal and nVal in
 * which the string or numeric value of literals is saved.
 * @param kind the kind of token, as defined below
 * @param string the text of the token
 * @param number the value of the token, if it is a number, as a Real
 * @param lineNo the line number in which the token started
 * @param indices the indices of the token in the input stream, as first .. last character range
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

    /**
     * An enumeration of all token's kind
     */
    enum class Kind {
        // Just for passing information; not really tokens
        ERROR, WHITESPACE, COMMENT,

        // Literals
        INTEGER_LIT, FLOAT_LIT, STRING_LIT, NAME_LIT,

        // SysML & SysMD Keywords for Types and Identification
        DEFINES, ONE, HAS_A,

        // SysML & KerML Keywords
        ABOUT, ABSTRACT, ACCEPT, ACTION, ACTOR, ALIAS, ALL, ALLOCATION, ALLOCATE, ASSOC, ASSERT, ASSUME, ATTRIBUTE,
        BEHAVIOR, BINDING, BOOL, BY,
        CALC, CHAINS, CLASS, CLASSIFIER, CONJUGATE, CONJUGATES, CONNECT, COMPOSITE, CONJUGATION, CONNECTION, CONNECTOR, CONSTRAINT,
        DATATYPE, DECISION, DEF, DEFAULT, DO, DOC, DEPENDENCY, DERIVED, DIFFERENCES, DISJOINT, DISJOINING,
        ELSE, END, ENTRY, ENUM, EXIT, EXPR,
        FIRST, FEATURE, FEATURED, FLOW, FOR, FORK, FRAME, FROM, FUNCTION,
        HASTYPE,
        IF, IMPORT, IN, INDIVIDUAL, INOUT, INTERACTION, INTERFACE, INTERSECTS, INV, INVERSE, ISTYPE, ITEM,
        JOIN,
        LANGUAGE, LIBRARY,
        METADATA, MERGE, MESSAGE, METACLASS, NAMESPACE,
        NONUNIQUE, NOTE,
        OCCURRENCE, OF, ORDERED, OUT,
        PACKAGE, PART, PERFORM, PORT, PORTION, PREDICATE, PRIVATE, PROTECTED, PUBLIC,
        READONLY, REF, REGULAR_COMMENT, RELATIONSHIP, REQUIRE, REP, RETURN, REDEFINES, REFERENCES, REQUIREMENT,
        SATISFY, SEND, SNAPSHOT, SPECIALIZATION, SPECIALIZES, STAKEHOLDER, STANDARD, STATE, STEP, STRUCT, STRUCTURE, SUBJECT, SUCCESSION, SUBSETS, SUBTYPE,
        THEN, TIMESLICE, TO, TRANSITION, TYPE, TYPED,
        UNIQUE, UNIONS,
        VARIATION, VIEW,

        // Operators
        LCURBRACE { override fun toString() = "{" },
        RCURBRACE { override fun toString() = "}" },
        GT { override fun toString() = ">" },
        LT { override fun toString() = "<" },
        GE { override fun toString() = ">=" },
        LE { override fun toString() = "<=" },
        EE { override fun toString() = "==" },
        DPEQ { override fun toString() = ":=" },
        DPGT { override fun toString() = ":>" },
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
        TYPED_BY { override fun toString() = ":" },
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
            '@' to Kind.METADATA,
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
            ':' to Kind.TYPED_BY,
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
            "actor" to Kind.ACTOR,
            "alias" to Kind.ALIAS,
            "all" to Kind.ALL,
            "allocation" to Kind.ALLOCATION,    // SysML
            "allocate" to Kind.ALLOCATE,
            "and" to Kind.AND,
            "assert" to Kind.ASSERT,
            "assoc" to Kind.ASSOC,
            "assume" to Kind.ASSUME,
            "attribute" to Kind.ATTRIBUTE,
            "behavior" to Kind.BEHAVIOR,
            "binding" to Kind.BINDING,
            "bool" to Kind.BOOL,
            "by" to Kind.BY,
            "calc" to Kind.CALC,                // SysML
            "chains" to Kind.CHAINS,
            "class" to Kind.CLASS,
            "classifier" to Kind.CLASSIFIER,
            "comment" to Kind.COMMENT,
            "composite" to Kind.COMPOSITE,
            "conjugation" to Kind.CONJUGATION,
            "conjugate" to Kind.CONJUGATE,
            "conjugates" to Kind.CONJUGATES,
            "connect" to Kind.CONNECT,
            "connection" to Kind.CONNECTION,
            "connector" to Kind.CONNECTOR,
            "constraint" to Kind.CONSTRAINT,    // SysML
            "cross" to Kind.CROSS,
            "datatype" to Kind.DATATYPE,        // KerML
            "decision" to Kind.DECISION,        // SysML
            "def" to Kind.DEF,                  // SysML
            "default" to Kind.DEFAULT,
            "dependency" to Kind.DEPENDENCY,
            "derived" to Kind.DERIVED,
            "differences" to Kind.DIFFERENCES,
            "disjoining" to Kind.DISJOINING,
            "disjoint" to Kind.DISJOINT,
            "do" to Kind.DO,
            "doc" to Kind.DOC,
            "dot" to Kind.DOTProduct,
            "else" to Kind.ELSE,
            "end" to Kind.END,
            "entry" to Kind.ENTRY,
            "enum" to Kind.ENUM,
            "exit" to Kind.EXIT,
            "expr" to Kind.EXPR,
            "false" to Kind.FALSE,
            "feature" to Kind.FEATURE,
            "featured" to Kind.FEATURED,
            "first" to Kind.FIRST,
            "flow" to Kind.FLOW,
            "for" to Kind.FOR,
            "fork" to Kind.FORK,
            "frame" to Kind.FRAME,
            "from" to Kind.FROM,
            "function" to Kind.FUNCTION,
            "hasA" to Kind.HAS_A,
            "hastype" to Kind.HASTYPE,
            "in" to Kind.IN,
            "individual" to Kind.INDIVIDUAL,
            "inout" to Kind.INOUT,
            "inverse" to Kind.INVERSE,
            "istype" to Kind.ISTYPE,
            "interaction" to Kind.INTERACTION,
            "inv" to Kind.INV,
            "isA" to Kind.SPECIALIZES,          // SysMD
            "item" to Kind.ITEM,
            "import" to Kind.IMPORT,
            "interface" to Kind.INTERFACE,
            "language" to Kind.LANGUAGE,
            "library" to Kind.LIBRARY,
            "metaclass" to Kind.METACLASS,
            "metadata" to Kind.METADATA,
            "namespace" to Kind.NAMESPACE,
            "nonunique" to Kind.NONUNIQUE,
            "not" to Kind.NOT,
            "occurrence" to Kind.OCCURRENCE,
            "of" to Kind.OF,                    // SysML
            "or" to Kind.OR,
            "out" to Kind.OUT,
            "ordered" to Kind.ORDERED,
            "perform" to Kind.PERFORM,          // SysML
            "port" to Kind.PORT,                // SysML
            "portion" to Kind.PORTION,
            "predicate" to Kind.PREDICATE,
            "private" to Kind.PRIVATE,
            "protected" to Kind.PROTECTED,
            "public" to Kind.PUBLIC,
            "readonly" to Kind.READONLY,
            "redefines" to Kind.REDEFINES,
            "ref" to Kind.REF,                  // SysML
            "references" to Kind.REFERENCES,
            "relationship" to Kind.RELATIONSHIP,
            "rep" to Kind.REP,
            "requirement" to Kind.REQUIREMENT,
            "require" to Kind.REQUIRE,
            "satisfy" to Kind.SATISFY,
            "send" to Kind.SEND,
            "snapshot" to Kind.SNAPSHOT,
            "specialization" to Kind.SPECIALIZATION,
            "stakeholder" to Kind.STAKEHOLDER,
            "standard" to Kind.STANDARD,
            "state" to Kind.STATE,
            "step" to Kind.STEP,
            "struct" to Kind.STRUCT,
            "structure" to Kind.STRUCTURE,
            "subject" to Kind.SUBJECT,
            "subsets" to Kind.SUBSETS,
            "succession" to Kind.SUCCESSION,
            "subtype" to Kind.SUBTYPE,
            "timeslice" to Kind.TIMESLICE,
            "transition" to Kind.TRANSITION,
            "true" to Kind.TRUE,
            "to" to Kind.TO,
            "type" to Kind.TYPE,
            "typed" to Kind.TYPED,
            "one" to Kind.ONE,
            "if" to Kind.IF,
            "intersects" to Kind.INTERSECTS,
            "then" to Kind.THEN,
            "specializes" to Kind.SPECIALIZES,
            "unique" to Kind.UNIQUE,
            "unions" to Kind.UNIONS,
            "package" to Kind.PACKAGE,
            "part" to Kind.PART,
            "variation" to Kind.VARIATION,
            "view" to Kind.VIEW,
            "message" to Kind.MESSAGE,
            "return" to Kind.RETURN,
        )
    }
}

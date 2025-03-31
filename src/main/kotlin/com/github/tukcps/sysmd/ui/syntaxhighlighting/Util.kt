package com.github.tukcps.sysmd.ui.syntaxhighlighting

import com.github.tukcps.sysmd.compiler.scanner.Token

/**Stores information about a brace*/
class Brace(
    /**The value of the brace which was assigned by the brace counter
     * Is used to identify the corresponding partner brace.*/
    val value : Int,
    /**The index to the left of the brace in the text-field*/
    val firstIndex : Int,
    /**The index to the right of the brace in the text-field*/
    val secondIndex : Int
)

/**Stores information about the Brace at which the cursor is placed*/
class CursorBrace(
    /**At which position this brace is in the brace list*/
    val positionInList : Int,
    /**The value of this brace which was assigned by the brace counter.*/
    val value : Int,
    /**The type of Brace. Uses the Token Kind of the Scanner*/
    val braceType : Token.Kind
)


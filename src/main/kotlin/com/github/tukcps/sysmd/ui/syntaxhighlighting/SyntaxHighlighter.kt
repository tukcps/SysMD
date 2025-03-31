package com.github.tukcps.sysmd.ui.syntaxhighlighting

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.compiler.scanner.Scanner
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.ui.Inconsistency
import kotlin.math.min

/**
 * The Syntax Highlighter is used to apply Syntax Highlighting to the text of a TextFieldValue.
 */
object SyntaxHighlighter {

    /**Background color for a CurlyBrace which is highlighted*/
    private val braceMarkedBackground = Color(255, 255, 0, 150)

    /**
     * This function actually performs the Syntax highlighting onto a blank text.
     */
    private fun annotateSyntaxHighlighting(text: String, colorScheme: ColorScheme): AnnotatedString {
        val tokens = mutableListOf<AnnotatedString>()
        val scanner = Scanner(skip = emptySet()).also { it.input = text }

        do {
            tokens.add(
                AnnotatedString(
                    when (scanner.token.kind) {
                        Token.Kind.REGULAR_COMMENT -> "/*${scanner.token.string}*/"
                        Token.Kind.STRING_LIT -> "\"" + scanner.token.string + "\""
                        Token.Kind.NAME_LIT -> text.substring(scanner.token.indices)
                        Token.Kind.ERROR -> return AnnotatedString(text) //Simple fix to cope with errors in text
                        else -> scanner.token.string
                    },
                    when (scanner.token.kind) {
                        in Token.keywords.values -> SpanStyle(colorScheme.primary)
                        Token.Kind.ERROR -> SpanStyle(colorScheme.error)
                        Token.Kind.COMMENT, Token.Kind.REGULAR_COMMENT, Token.Kind.NOTE -> SpanStyle(Color.Gray)
                        Token.Kind.STRING_LIT -> SpanStyle(color = colorScheme.secondary, fontStyle = FontStyle.Italic)
                        Token.Kind.FLOAT_LIT -> SpanStyle(color = colorScheme.secondary, fontStyle = FontStyle.Italic)
                        Token.Kind.INTEGER_LIT -> SpanStyle(color = colorScheme.secondary, fontStyle = FontStyle.Italic)
                        else -> SpanStyle(colorScheme.onBackground)
                    }
                )
            )
            scanner.nextToken()
        } while(scanner.token.kind != Token.Kind.EOF)
        return tokens.fold(AnnotatedString(""), operation = { e1, e2 -> e1 + e2 })
    }

    /**
     * Takes a text field and performs Syntax Highlighting on its whole text.
     * Usually used to initialize a text that has no Syntax Highlighting yet.
     */
    fun applyFullSyntaxHighlighting(tfv: TextFieldValue, colorScheme: ColorScheme) : TextFieldValue {
        return TextFieldValue(
            annotatedString = annotateSyntaxHighlighting(tfv.text, colorScheme),
            selection = tfv.selection
        )
    }

    /**
     * Checks if the text has changed and updates the Syntax Highlighting to the corresponding areas.
     * The Syntax Highlighting of parts that have not changes is contained.
     * Only new text will go through the Highlighting process.
     */
   fun updateSyntaxHighlighting(
        newTFV: TextFieldValue,
        oldTFV: TextFieldValue,
        /**Color scheme for the Syntax Highlighting*/
        colorScheme: ColorScheme,
        /**Extension function for TextFieldValues that replaces a tab with the according number of spaces*/
        replaceTabFunction: (TextFieldValue) -> TextFieldValue
    ): TextFieldValue
   {


        /** If TextField only consists of one char, skip the whole Syntax Highlighting and return the new Text-field as is*/
        if (newTFV.text.length < 2) {
            return newTFV
        }

        /**The AnnotatedString that is returned at the end of this function
         *(if the program doesn't run into a code section which has its individual return block)**/
        val finalAnnotatedString: AnnotatedString

        /**Shows the difference in length between the new, and the old TextField
         * Positive means the new text is longer. Negative means that the length has reduced.*/
        val offset = newTFV.text.length - oldTFV.text.length

        when {
            /** Nothing changed (Indicates that user just clicks around)**/
            (offset == 0 && newTFV.selection != oldTFV.selection) -> {
                //Return TextFieldValue with:
                //  - Old AnnotatedText
                //  - New Selection
                return TextFieldValue(
                    annotatedString = oldTFV.annotatedString,
                    selection = newTFV.selection
                )
            }

            /** Action within one line, cases are:
             *      - The user is writing/removing text within this line and changes one char at a time.
             *      - The user pastes or removes a whole string but stays withing this line.
             *  Note: In both cases, the number of lines does not change.
             *  **/
           (offset > -1 && offset < 1) -> {
                /**Shadows the newFTV value but here the \t (tabs) are replaced with the according number of spaces*/
                val newTFVShadowed = if (newTFV.selection.start > 0) {
                    if (newTFV.text[newTFV.selection.start - 1] == '\t')
                        replaceTabFunction(newTFV) else newTFV
                } else newTFV

                /**Tells by how many Spaces a \t (tab) ras been replaced*/
                val tabDifference = newTFVShadowed.text.length - newTFV.text.length

                when {

                    /** Special Case 1: Cursors is directly at the end of the text*/
                    (newTFVShadowed.selection.start == newTFVShadowed.text.length) -> {
                        var splitIndex = newTFV.selection.start

                        if (newTFVShadowed.text[splitIndex - 1] == '\n') { //If a line at the end of the text-field is added/removed
                            return if (offset == 1) {//Line added
                                TextFieldValue(
                                    annotatedString = oldTFV.annotatedString + AnnotatedString("\n"),
                                    selection = newTFVShadowed.selection
                                )
                            } else {//Line removed
                                TextFieldValue(
                                    annotatedString = oldTFV.annotatedString.subSequence(0, newTFVShadowed.text.length),
                                    selection = newTFVShadowed.selection
                                )
                            }

                        } else { //If a user is simply writing at the end of the text

                            //Iterate to the left to find the point to split the last line and the upper block of text
                            while (newTFVShadowed.text[splitIndex - 1] != '\n') {
                                splitIndex--
                                if (splitIndex == 0) break //We reached start of text-field, so stop walking to the left
                            }
                        }


                        val block1 = oldTFV.annotatedString.subSequence(0, splitIndex) //Unchanged block above line that is edited
                        val block2 = annotateSyntaxHighlighting(newTFVShadowed.text.substring(splitIndex, newTFVShadowed.text.length), colorScheme) //Line that is edited, gets new Syntax Highlighting

                        //Merge the upper block with the old Syntax Highlighting and the last line (with new Syntax Highlighting) together
                        finalAnnotatedString = block1 + block2
                    }

                    /** Standard Case: Cursors is somewhere inside the text*/
                    else -> {

                        var firstIndex = newTFVShadowed.selection.start
                        var lastIndex = newTFVShadowed.selection.start

                        //We only have to go left and search for the start of the line if we are not directly at the start of the text-field (which is index 0)
                        if(newTFV.selection.start > 0) {
                            if (newTFVShadowed.text[newTFV.selection.start - 1] == '\n') {
                                //The user has pressed ENTER and added a newline directly before the cursor
                                firstIndex = newTFVShadowed.selection.start - 1

                                if (firstIndex > 0) { //Make sure we do not walk left if we are already at the start of the text-field
                                    while (newTFVShadowed.text.getOrNull(firstIndex - 1) != '\n') {
                                        firstIndex--
                                        if (firstIndex == 0) break //We reached start of text-field, so stop walking to the left
                                    }
                                }
                            } else {
                                //The cursor is somewhere inside the line, go left to find where the line begins
                                while (newTFVShadowed.text[firstIndex - 1] != '\n') {
                                    firstIndex--
                                    if (firstIndex == 0) break //We reached start of text-field, so stop walking to the left
                                }
                            }
                        }

                        //Go right to find where this line ends
                        while (newTFVShadowed.text[lastIndex] != '\n') {
                            lastIndex++
                            if (lastIndex == newTFVShadowed.text.length) break //Break if reached end of text
                        }
                        if (lastIndex != newTFVShadowed.text.length) lastIndex++

                        val block1 = oldTFV.annotatedString.subSequence(0,firstIndex) //Unchanged block above line that is edited
                        val block2 = annotateSyntaxHighlighting(newTFVShadowed.text.substring(firstIndex,
                            min(lastIndex, newTFVShadowed.text.length)
                        ),colorScheme) //Line that is edited, gets new Syntax Highlighting
                        val block3 = oldTFV.annotatedString.subSequence(lastIndex - offset - tabDifference, oldTFV.text.length) //Unchanged block below line that is edited

                        //Merge the three text blocks together
                        finalAnnotatedString = block1 + block2 + block3
                    }
                }

                return TextFieldValue(
                    annotatedString = finalAnnotatedString,
                    selection = newTFVShadowed.selection
                )
            }

            /** Pasted/Removed a larger area of text (Indicates Paste/Extract action covering multiple lines)**/
            else -> {
                return TextFieldValue(
                    annotateSyntaxHighlighting(newTFV.text, colorScheme),
                    newTFV.selection
                )
            }

        }
    }

    /**Takes a SysMD text and checks if Elements like imported Packages or used Classes are actually found in the corresponding global index.
     * If an Element is not found, it will be added to the list of inconsistencies.
     * @param tfv The textFieldValue which should be checked for inconsistencies.
     * @param sysMDColorScheme The ColorScheme which is used to color found inconsistencies accordingly.
     * @return A list of Elements from the SysMD Text that were not found in any of the global indexes.
     */
    fun checkForInconsistencies(tfv: MutableState<TextFieldValue>, sysMDColorScheme: ColorScheme) : MutableList<Inconsistency> {

        val sc = Scanner().also { it.input = tfv.value.text }

        /**The Kind attribute of the previous Token (Whitespaces are excluded!)*/
        var prevTokenKind = Token.Kind.ERROR

        /**The list holding the ranges of all Inconsistencies*/
        val inconsistencyRanges = mutableListOf<IntRange>()

        /**The list holding the ranges of all Consistencies*/
        val consistencyRanges = mutableListOf<IntRange>()

        /**The list holding all Inconsistency Objects*/
        val inconsistencies = mutableListOf<Inconsistency>()

        /**Brace list in which occurrences of braces are stored.
         * First value is the corresponding braceCounter value.
         * Second and Third value are the index of the brace.
         */
        val braceList = mutableListOf<Brace>()
        var braceCounter = 0
        var cursorBrace = CursorBrace(-1, -1, Token.Kind.ERROR)

        //Get the current SpanStyles of the TextField as a MutableList
        val styles = tfv.value.annotatedString.spanStyles.toMutableList()

        while(sc.token.kind != Token.Kind.EOF){

            when(sc.token.kind){

                Token.Kind.NAME_LIT, Token.Kind.INTEGER_LIT, Token.Kind.FLOAT_LIT -> {

                    when(prevTokenKind){

                        Token.Kind.IMPORT -> {
                            /**Add the Indices of the Token into the list depending on if it has an inconsistency or not*/
                            if (globalPackagesIndex.contains(sc.token.string)) {
                                // -> Token is consistent
                                consistencyRanges.add(sc.token.indices)
                            } else {
                                // -> Token is NOT consistent

                                //Write down Position of inconsistency
                                inconsistencyRanges.add(sc.token.indices)

                                //Add inconsistency to the list
                                inconsistencies.add(
                                    Inconsistency(
                                        name = sc.token.string,
                                        line = sc.token.lineNo,
                                    )
                                )

                                //Go to next Token
                                prevTokenKind = sc.token.kind
                                sc.nextToken()
                            }
                        }

                        Token.Kind.SPECIALIZES -> {
                            var fullyQualifiedName = sc.token.string

                            /*If the component name is extended by a leading information about the package name, then swap their order
                                so that they can be used with the globalComponentsIndex
                                    Before swap: PackageName::ComponentName (not compatible with Index)
                                    After swap: ComponentName::PackageName  (now compatible with Index)
                             */
                            if(sc.nextToken.kind == Token.Kind.DPDP){
                                sc.nextToken() //Jump to "::"
                                sc.nextToken() //Jump to Component Name
                                fullyQualifiedName = sc.token.string + "::" + fullyQualifiedName
                            }

                            if (globalComponentsIndex.contains(fullyQualifiedName)) {
                                // -> Token is consistent
                                consistencyRanges.add(sc.token.indices)
                            } else {
                                // -> Token is NOT consistent

                                //Write down the Position of inconsistency
                                inconsistencyRanges.add(sc.token.indices)

                                //Add inconsistency to the list
                                inconsistencies.add(
                                    Inconsistency(
                                        name = sc.token.string,
                                        line = sc.token.lineNo,
                                    )
                                )

                                //Go to next Token
                                prevTokenKind = sc.token.kind
                                sc.nextToken()
                            }

                        }

                        else -> {}
                    }
                }


                Token.Kind.LCURBRACE -> {
                    //Add the brace to the brace list
                    braceList.add(
                        Brace(braceCounter, sc.token.indices.first, sc.token.indices.first + 1)
                    )

                    //Check if the cursor is at this brace, if yes apply relevant information to the cursorBrace vairable
                    if(IntRange(sc.token.indices.first,sc.token.indices.first+1).contains(tfv.value.selection.start)) {
                        cursorBrace = CursorBrace(braceList.size - 1, braceCounter, Token.Kind.LCURBRACE)

                        styles.add(
                            AnnotatedString.Range(
                                item = SpanStyle(
                                    color = sysMDColorScheme.onBackground,
                                    background = braceMarkedBackground
                                ),
                                start = sc.token.indices.first,
                                end = sc.token.indices.first + 1
                            )
                        )
                    }else{
                        styles.add(
                            AnnotatedString.Range(
                                item = SpanStyle(color = sysMDColorScheme.onBackground, background = Color(0, 0, 0, 0)),
                                start = sc.token.indices.first,
                                end = sc.token.indices.first + 1
                            )
                        )
                    }

                    braceCounter++ //Now increment Counter
                }


                Token.Kind.RCURBRACE -> {
                    braceCounter-- //Decrement the counter

                    //Add the brace to the brace list
                    braceList.add(
                        Brace(braceCounter, sc.token.indices.first, sc.token.indices.first + 1)
                    )

                    //Check if the cursor is at this brace, if yes apply relevant information to the cursorBrace vairable
                    if(IntRange(sc.token.indices.first,sc.token.indices.first+1).contains(tfv.value.selection.start)){
                        cursorBrace = CursorBrace(braceList.size - 1, braceCounter, Token.Kind.RCURBRACE)

                        styles.add(
                            AnnotatedString.Range(
                                item = SpanStyle(
                                    color = sysMDColorScheme.onBackground,
                                    background = braceMarkedBackground
                                ),
                                start = sc.token.indices.first,
                                end = sc.token.indices.first + 1
                            )
                        )
                    }else{
                        styles.add(
                            AnnotatedString.Range(
                                item = SpanStyle(color = sysMDColorScheme.onBackground, background = Color(0, 0, 0, 0)),
                                start = sc.token.indices.first,
                                end = sc.token.indices.first + 1
                            )
                        )
                    }
                }


                else -> {}
            }

            if(sc.token.kind != Token.Kind.WHITESPACE){
                prevTokenKind = sc.token.kind
            }
            sc.nextToken()
        }


        //Update the SpanStyles list with a new SpanStyle for every Inconsistency found
        inconsistencyRanges.forEach {
            styles.add(
                AnnotatedString.Range(
                    item = SpanStyle(Color.Red),
                    start = it.first,
                    end = it.last + 1
                )
            )
        }

        //Update the SpanStyles list with a new SpanStyle for every Consistency found
        consistencyRanges.forEach {
            styles.add(
                AnnotatedString.Range(
                    item = SpanStyle(sysMDColorScheme.onBackground),
                    start = it.first,
                    end = it.last + 1
                )
            )
        }

        //Now mark the second brace
        if(cursorBrace.braceType == Token.Kind.LCURBRACE){
            markBraces(braceList, cursorBrace, styles, sysMDColorScheme)//Mark the braces by passing relevant information
        }else if(cursorBrace.braceType == Token.Kind.RCURBRACE){
            //Mark the braces by passing relevant information. As the cursor is at a closing brace the brace list must be reversed.
            markBraces(
                braceList.reversed().toMutableList(),
                CursorBrace(braceList.size - 1 - cursorBrace.positionInList, cursorBrace.value, cursorBrace.braceType),
                styles,
                sysMDColorScheme)
        }

        //Update the TextField with the new SpanStyles
        tfv.value = TextFieldValue(
            annotatedString = AnnotatedString(tfv.value.text, styles),
            selection = tfv.value.selection
        )

        //Return the Inconsistencies that were found
        return inconsistencies
    }

    private fun markBraces(
        braceList: MutableList<Brace>,
        cursorBrace: CursorBrace,
        styles: MutableList<AnnotatedString.Range<SpanStyle>>,
        sysMDColorScheme: ColorScheme
    ) : Boolean {

        braceList.forEachIndexed { index, brace ->

            //This check ensures that braces of the list that are in front of the relevant brace are skipped
            if(index > cursorBrace.positionInList){

                //Check if this is the partner brace.
                if (brace.value == cursorBrace.value){

                    //This brace has the same value as the opening brace. Create a highlighted SpanStyle for it.
                    styles.add(
                        AnnotatedString.Range(
                            item = SpanStyle(color = sysMDColorScheme.onBackground, background = braceMarkedBackground),
                            start = brace.firstIndex,
                            end = brace.secondIndex
                        )
                    )

                    //Now abort this whole function
                    return true
                }

            }
        }

        return false
    }


}//END SYNTAX HIGHLIGHTER OBJECT
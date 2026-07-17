package com.github.tukcps.sysmd.ui.composables

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.compiler.scanner.Scanner
import com.github.tukcps.sysmd.compiler.scanner.Token
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.Fonts
import com.github.tukcps.sysmd.ui.syntaxhighlighting.globalComponentsIndex
import com.github.tukcps.sysmd.ui.syntaxhighlighting.globalPackagesIndex


/**
 * Data Class to conveniently handle all information to analyze, show and insert suggestions while writing code.
 */
data class SuggestionsData(val dummyValue : Boolean){
    /** The actual suggestions that mirror the corresponding list from the indexer*/
    var actualSuggestions = mutableListOf<String>()

    /** A temporary list that only holds the items that match the currently written token*/
    var filteredSuggestions = mutableListOf<String>()

    /** Flag that indicates if the suggestions menu should be displayed or not*/
    var show = false

    /** Line of the suggestions*/
    var lineNo = 0

    /** Substring before the cursor*/
    var prevSubstring = ""

    /** Index of the cursor in the TextField*/
    var cursorIndex = 0

    /** The suggestion that is currently selected/focused*/
    var currMenuIndex = 0

    /** The current token that is written*/
    var currToken = ""

    /** Holds the Kind of Token that initiated the suggestion menu, is used to know which suggestions category to show*/
    var tokenCategory = Token.Kind.ERROR

    /** How many items are shown in the suggestions menu*/
    var itemsShown = 0

    /** A flag that is used to disable the Exit from the suggestions menu via a Spacebar press*/
    var blockExitViaSpace = false

    /**Lazy List state of the list displaying the suggestions*/
    var listState = LazyListState()

    /**Shows if a temporary Space-Char was added to enable proper down/-up navigation in SuggestionsMenu.
     * The Space-Char has to be removed and this variable has to be set to "false" after the SuggestionsMenu is hidden*/
    var temporarySpace = false

    /**Closes the menu and resets variables so that it will be displayed next time in its initial state*/
    fun close() {
        show = false
        currMenuIndex = 0
        listState = LazyListState(firstVisibleItemIndex = 0)
    }
}

/**
 * A DropDownMenu showing suggestions for a currently written Token.
 * The DropDownMenu automatically updates the suggestions while writing the Token.
 * When a Token is selected, it is inserted at the current position of the cursor.
 * Depending on the type of Token, additional Elements such as braces are added according to the SysMD Syntax.
 * Note: A Suggestions Menu must always be used with the corresponding handleKeyInput function!
 *
 *  @param sg The suggestions data class which stores all necessary information to view, update and insert the suggestions properly.
 *  @param tfv The TextFieldValue from the Editor in which the Suggestions should be shown.
 *  @param lineHeight Required to match the height of the elements in the Suggestions Menu to the lines of the Editor field.
 *  @param enableElementListScrolling To enable proper scrolling in the Suggestions Menu, other scrollable Elements must be deactivated.
 *  Typically, the scrolling function can be dis-/enabled via the "userScrollEnabled" parameter of the corresponding column/row.
 *  To automatically dis-/enable the scrolling function of another Element, assign a Boolean variable to the "userScrollEnabled" parameter and pass it here.
 *
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun suggestionsDropDown(
    sg: MutableState<SuggestionsData>,
    tfv: MutableState<TextFieldValue>,
    lineHeight: TextUnit,
    enableElementListScrolling: MutableState<Boolean>,
    editorHeigth: MutableState<Dp>,
) {

    //Calculate the height of the editor lines in Dp
    val lineHeightInDp = with(LocalDensity.current) { lineHeight.toDp() }

    //Calculate the length of a String in Pixels to know at which X Position the suggestion menu has to be displayed
    val widthInPixels = rememberTextMeasurer().measure(
        text = sg.value.prevSubstring,
        density = LocalDensity.current,
        style = TextStyle(
            fontSize = AppTheme.fontSize,
            fontFamily = Fonts.jetbrainsMono,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        )
    ).size.width

    //Update Suggestions by filtering them using the current Token that is written
    sg.value.show = updateSuggestions(tfv, sg)

    //Iterate filteredSuggestions to find length of longest suggestion string
    //And check if a menu entry is hovered by cursor
    var longestStringLength = 0
    sg.value.filteredSuggestions.forEachIndexed { _, fs ->
        if (fs.length > longestStringLength) longestStringLength = fs.length
    }
    longestStringLength += 3

    //TODO Think about how to properly set the amount of shown items, also make the value accessible for the handleKeyInput() method
    //Compute amount of items to show in the suggestions menu
    val itemsToShow = if(sg.value.filteredSuggestions.size < 8) sg.value.filteredSuggestions.size else 8
    sg.value.itemsShown = itemsToShow


    /*
     * Scroll down or up depending on the CurrMenuIndex
     */
    if(!sg.value.listState.isScrollInProgress){ //Make sure list is currently not scrolled over
        if(sg.value.currMenuIndex == sg.value.listState.firstVisibleItemIndex + itemsToShow-2){ //If the current Menu index is the 2. visible item from the BOTTOM
            sg.value.listState = LazyListState(sg.value.listState.firstVisibleItemIndex + 1) //Scroll the list down by one step
        }
        if(sg.value.currMenuIndex != 1 && sg.value.currMenuIndex == sg.value.listState.firstVisibleItemIndex+1) { //If the current Menu index is the 2. visible item from the TOP
            sg.value.listState = LazyListState( sg.value.listState.firstVisibleItemIndex - 1) //Scroll the list up by one step
        }
    }

    //Create interactionSource for the LazyList to detect if the cursor hovers over it
    val lazyListInteractionSource = remember { MutableInteractionSource() }
    val hovered by lazyListInteractionSource.collectIsHoveredAsState()


    if (sg.value.show) {

        Box(modifier = Modifier
            .height(lineHeightInDp * itemsToShow) //A fixed height is absolutely required as nested scrolling otherwise does not work!
            .offset(
                x = with(LocalDensity.current) { widthInPixels.toDp() },
                //TODO make this even nicer so that we do not run into trouble when the editor itself is to small to display the suggestions menu
                y = if(((lineHeightInDp * (sg.value.lineNo + 1)) + lineHeightInDp*8) > editorHeigth.value) lineHeightInDp * (sg.value.lineNo - 8) else lineHeightInDp * (sg.value.lineNo + 1)
            )
        ) {

            LazyColumn(state = sg.value.listState,
                modifier = Modifier
                    .hoverable(lazyListInteractionSource)
                    .background(Color.LightGray)
            ) {

                // Enable/Disable the scrolling of the ElementList (Which holds the Markdown and SysMD Editor fields) depending on if the suggestion list is hovered or not
                enableElementListScrolling.value = !hovered

                itemsIndexed(sg.value.filteredSuggestions) { index, item ->
                    Text(
                        text = if(item.contains("::")){
                                    "${item.substringBefore("::")} (${item.substringAfter("::")})".padEnd(longestStringLength)
                                }else{
                                    item.padEnd(longestStringLength)
                                },
                        fontSize = AppTheme.fontSize,
                        fontFamily = Fonts.jetbrainsMono,
                        color = if (index == sg.value.currMenuIndex) Color.Black else Color.Gray,
                        modifier = Modifier
                            .height(lineHeightInDp)
                            .onClick {
                                sg.value.show = false
                                tfv.value =
                                    insertSuggestion(tfv.value, sg.value.cursorIndex, item, sg)
                                sg.value.currMenuIndex = 0
                                sg.value.listState = LazyListState(0)
                            }
                            .background(
                                if (index == sg.value.currMenuIndex)
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 0.60f)
                                else
                                    MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp).copy(alpha = 1.00f)
                            )
                    )
                }
            }//LazyColumn End
            VerticalScrollbar(adapter = rememberScrollbarAdapter(sg.value.listState), modifier = Modifier.align(
                Alignment.CenterEnd))
        }//BoxScope End
    }
}




/**
 * Returns the substring from the start of a line to a given cursor position.
 */
private fun String.getSubstringBeforeCursor(cursor : Int) : String{
    val lastNewLine = this.substring(0,cursor).lastIndexOf("\n")
    return this.substring(if(lastNewLine >= 0) lastNewLine else 0,cursor)
}
/**
 * Checks if Suggestions Menu should be enabled.
 * The Function analyzes the Token right before the cursor (to the left).
 * If the Token Kind is of a category for which suggestions can be shown (like for IMPORT it can show available Packages) it returns true.
 * In addition, information required to draw the Suggestions Menu at the right locations and insert the suggestions correctly
 */
fun checkSuggestionsInit(
    /**The TextFieldValue in which the user is currently writing.*/
    tfv: MutableState<TextFieldValue>,
    /**The SuggestionsData Object of the Editor which is currently active*/
    sg : MutableState<SuggestionsData>
) : Boolean{

    val sc = Scanner(keywords = Token.sysMLv2Keywords+Token.kerMLKeywords).also { it.input = tfv.value.text } //Feed total textField text to Scanner

    //Scan through the text until reaching a token whose last index is the current cursor position
    do{

        if(tfv.value.selection.start == sc.token.indices.last+2){

            //Now check if the token that is just before our cursor is a SPECIALIZATION ("isA")
            if(sc.token.kind == Token.Kind.SPECIALIZES || sc.token.kind == Token.Kind.IMPORT){

                sg.value.tokenCategory = sc.token.kind

                when(sc.token.kind){
                    Token.Kind.SPECIALIZES -> {sg.value.actualSuggestions = globalComponentsIndex.sorted().toMutableList(); sg.value.filteredSuggestions = globalComponentsIndex.sorted().toMutableList()}
                    Token.Kind.IMPORT -> {sg.value.actualSuggestions = globalPackagesIndex.sorted().toMutableList(); sg.value.filteredSuggestions = globalPackagesIndex.sorted().toMutableList()}
                    else -> {}
                }

                //Write down line of "isA" token and the new cursor position
                sg.value.lineNo = sc.token.lineNo-1
                sg.value.cursorIndex = sc.token.indices.last + 2

                //Calc total sum of chars in the lines before the current relevant "isA" Token is located
                //prevLinesCharSum = separatedLines.calcCharsInPreviousLines(sg.value.lineNo)

                //Get substring before the current CursorPos and add a space at the current cursor Position
                sg.value.prevSubstring = tfv.value.text.getSubstringBeforeCursor(tfv.value.selection.start)


                //Just a temporary String Builder needed for the next few lines of code
                val strBldr = StringBuilder(tfv.value.text)

                if(tfv.value.selection.start != tfv.value.text.length && tfv.value.text.isNotEmpty()) {
                    //If the cursors is directly before a newline "\n" then insert a temporary Space-Char behind the cursors
                    //to ensure that the up-/down navigation works properly.
                    //This temporary Space-Char must be removed (and the temporarySpace flag set to "false") when the Suggestion Menu is hidden.
                    if (tfv.value.text[tfv.value.selection.start] == '\n') {
                        strBldr.insert(tfv.value.selection.start, " ")
                        sg.value.temporarySpace = true
                    }
                }

                //Force Compose to recompose the EditorField by appending a Space-Char at the very End
                //and directly remove it (This procedure is invisible to the user)
                tfv.value = TextFieldValue(strBldr.append(" ").toString(), tfv.value.selection)
                tfv.value = TextFieldValue(strBldr.removeRange(tfv.value.text.length-1,tfv.value.text.length).toString(), tfv.value.selection)

                //Makes sure that the next SpaceBar press does not directly disable the Suggestions Box again (this would happen in the handleKeyInput method)
                sg.value.blockExitViaSpace = true

                return true //We found the token at cursor position, and it WAS a "isA" Token, we noted all imported information and can now stop the token inspection

            }else {
                return false //We found the token at cursor position, but it WAS NOT an "isA" Token, so abort process
            }

        }else{
            sc.nextToken()
        }

    }while (sc.token.kind != Token.Kind.EOF)


    return false
}



/**
 * Extension function for String
 *      Extracts a Token from a String by walking left until a Space is met.
 *      The upper and the lower indices are then used to extract the Token via the substring() method.
 */
private fun String.walkLeftTillSpace(upperIndex : Int) : String{
    var lowerIndex : Int
    var index = upperIndex

    do{
        lowerIndex = index
        index--
    }while(this[index] != ' ' && (index > 0))

    return this.substring(lowerIndex,upperIndex)
}


/**
 * Updates the suggestions list by removing irrelevant suggestions that do not match the currently written Token anymore.
 */
private fun updateSuggestions(
    /**The TextFieldValue in which the user is currently writing.*/
    tfv: MutableState<TextFieldValue>,
    /**The SuggestionsData Object of the Editor which is currently active*/
    sg : MutableState<SuggestionsData>
): Boolean {

    val oldFiltered = sg.value.filteredSuggestions //Saves the old filtered suggestions, so they can be compared to the new ones

    //Get current Token and use it to filter the suggestions
    val token = tfv.value.text.walkLeftTillSpace(tfv.value.selection.start)
    sg.value.currToken = token
    sg.value.filteredSuggestions = sg.value.actualSuggestions.filter { it.startsWith(token, ignoreCase = true) }.toMutableList()

    //If after filtering the suggestions there are still entries then show them
    if(sg.value.filteredSuggestions.isNotEmpty()){

        //If the filtered suggestions have actually changed, reset the menu index to 0
        if(oldFiltered != sg.value.filteredSuggestions){
            sg.value.currMenuIndex = 0
        }

        return true
    }

    //There are no more suggestions that match the current token, stop displaying suggestions
    sg.value.filteredSuggestions = sg.value.actualSuggestions

    //If there was a temporary space added behind the cursors, remove it.
    if(sg.value.temporarySpace){
        tfv.value = TextFieldValue(
            text = StringBuilder(tfv.value.text).delete(tfv.value.selection.start,tfv.value.selection.start+1).toString(),
            selection = tfv.value.selection
        )
    }

    return false
}

/**
 * Returns the empty area before actual text begins as a String of spaces.
 */
private fun String.getPreviousSpaces() : String{
    var spaceString = ""
    var idx = 0

    while(this[idx] == ' '){
        spaceString += this[idx]
        idx++
    }

    return spaceString
}

/**
 * Inserts a Suggestion into a new TextFieldValue at a given Position/Index and returns it.
 * The cursor is then automatically placed after the inserted suggestion.
 */
fun insertSuggestion(tfv: TextFieldValue, index: Int, suggestion: String, sg : MutableState<SuggestionsData>) : TextFieldValue {

    var suggestionString = suggestion

    //If suggestion is a fully qualified name and contains a "::" then rearrange the package and component name
    if(suggestionString.contains("::")){
        suggestionString = suggestionString.substringAfter("::") + "::" + suggestionString.substringBefore("::")
    }

    val sb = StringBuilder(tfv.text)
    val insertString = suggestionString
    //val insertString = suggestionString.substring(sg.value.currToken.length,suggestionString.length) //Remove already written part from the suggestionString
    val currLine = tfv.text.lines()[sg.value.lineNo]
    val space = currLine.getPreviousSpaces()  //Holds a String full of blank space to which is added in front of the string that is inserted to maintain the indents
    var skipDistance = 0 //Is used to move the cursor forward

    //If a temporary Space-Char was added behind the cursors, remove it and reset the temporarySpace flag to "false"
    if(sg.value.temporarySpace) {
        sb.delete(tfv.selection.start,tfv.selection.start+1)
        sg.value.temporarySpace = false
    }

    when(sg.value.tokenCategory){
        Token.Kind.SPECIALIZES -> {
            skipDistance = if(sb.length <= index){
                sb.append("$insertString {\n$space\t\n$space}") //Insert suggestions and also Component Scope
                4 + space.length //Skip into next line
            }else{
                if(currLine.contains("{")){
                    sb.insert(index, "$insertString ") //Insert suggestions and a space
                    1 //Just skip one index forward
                }else{
                    sb.insert(index, "$insertString {\n$space\t\n$space}") //Insert suggestions and also Component Scope
                    4 + space.length //Skip into next line
                }
            }
        }

        Token.Kind.IMPORT -> {
            skipDistance = 1
            sb.insert(index, "$insertString;") //Insert package name and add semicolon
        }

        else -> {/*Nothing*/}
    }

    /**Remove the remains of the written token after the suggestion has been inserted
     * User is currently writing
     * --> "Amp"
     * User selects suggestion "Amplifier from the menu which is inserted behind the already written Token
     * --> "AmpAmplifier"
     * The user written part "Amp" which is left over is removed and the selected suggestions remains
     * --> "Amplifier"
     */
    sb.delete(tfv.selection.start - sg.value.currToken.length,tfv.selection.start)

    return TextFieldValue(sb.toString(), TextRange(index+insertString.length + skipDistance - sg.value.currToken.length))
}
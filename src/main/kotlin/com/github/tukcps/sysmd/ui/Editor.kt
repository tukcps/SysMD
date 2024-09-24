@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "FunctionName")

package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.tukcps.sysmd.imports.ResultAnnotation
import com.github.tukcps.sysmd.indexer
import com.github.tukcps.sysmd.services.SyntaxHighlighter
import com.github.tukcps.sysmd.settings
import com.github.tukcps.sysmd.ui.composables.*
import com.github.tukcps.sysmd.ui.styles.AppTheme
import com.github.tukcps.sysmd.ui.styles.Fonts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skiko.currentNanoTime

class Inconsistency(
    val name : String,
    val line : Int,
)

/**
 * Holds data and methods to realize a delay between key presses.
 * Can be used to avoid registering a single key press as multiple or
 * to ensure that a key cannot be pressed to quickly in a sequence.
 */
private class KeyDelay{

    private var lastKeyTime = 0L //Time when last key was pressed (in nanoseconds)
    private val delay = 500E6 //Delay between key presses is set to 125 ms

    /**Checks if the key that is pressed is currently set on pause.
     * @return True if key input is currently locked, False if input is allowed.
     */
    fun isLocked(): Boolean{

        if((currentNanoTime() - lastKeyTime) > delay) {
            lastKeyTime = currentNanoTime()
            return false
        }
        return true

    }

}

/**
 * Editor field with decorations. Parameters are:
 * - Lines: The lines of e.g. code as a TextFieldValue
 * - lineAnnotations: A map of line number to an annotating text, e.g. an error message.
 * - active: Boolean, a flag that can de-activate the editor.
 *
 * The Editor has 2 columns;
 * - left: line numbers (70.dp)
 * - right: textarea (rest of width)
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Editor(
    rowWidth : Dp,
    lines: MutableState<TextFieldValue>,
    lineAnnotations: SnapshotStateMap<Int, String>,
    resultsAnnotations: MutableList<ResultAnnotation>,
    readOnly: Boolean = false,
    useHighlighting: Boolean = true,
    elementEdited: MutableState<Boolean> = mutableStateOf(false),
    enableElementListScrolling : MutableState<Boolean>
) = Box(Modifier.fillMaxSize()) {

    val coroutineScope = rememberCoroutineScope()

    val resultsColumnWidth = 200.dp //Width of the Column that displays the imported Simulation Results
    val fixedLineHeight = AppTheme.fixedLineHeight  // * settings.lineHeightMultiplier
    val horizontalState = rememberScrollState(0)
    
    
    /**Tells if this is the initial run of the EditorField*/
    val firstRun = remember { mutableStateOf(true) }
    
    /**Before the new state of the TextFieldValue is applied, the state is saved here, so it can be rolled back*/
    val oldTextFieldValue = remember { mutableStateOf(lines.value) }

    
    /**The data class which holds all necessary attributes to handle showing the suggestions menu*/
    val suggestions = remember { mutableStateOf(SuggestionsData(dummyValue = true)) }

    /**Holds a local index of Components defined in this TextField*/
    val localComponentsIndex = remember { mutableStateOf( mutableSetOf<String>() ) }
    /**Holds a local index of Packages defined in this TextField*/
    val localPackagesIndex = remember { mutableStateOf( mutableSetOf<String>() ) }

    /**Holds the point of time (in nanoseconds) when the last change has happened to the TextField*/
    val lastChange = remember { mutableStateOf(-1L) }

    /** The value (in milliseconds) defining how long the TextField should be untouched before the re-analization process starts*/
    val delayValue = 450

    /**The inconsistencies which were found when analyzing the text of this Editor*/
    val inconsistencies = remember { mutableListOf<Inconsistency>()  }

    /**The SysMD color scheme. Used to perform the Syntax Highlighting.*/
    val sysMDColorScheme = MaterialTheme.colorScheme

    /**Indicates if SyntaxHighlighting or Re-Indexing is ongoing. Used to show/hide the circular progress indicator. */
    val isUpdating = remember { mutableStateOf(false) }

    /**Holds KeyDelay object that enables delays between key presses of the same key.*/
    val keyDelay = remember { mutableStateOf(KeyDelay()) }

    val density = LocalDensity.current
    val editorHeight = remember { mutableStateOf(0.dp) }



    if(firstRun.value){

        //When the Editor is displayed the first time, the whole text gets a Syntax Highlighting
        if(useHighlighting) lines.value = SyntaxHighlighter.applyFullSyntaxHighlighting(lines.value, sysMDColorScheme)
        oldTextFieldValue.value = lines.value

        with(indexer){
            lines.value.buildLocalIndexes(localComponentsIndex, localPackagesIndex)
        }

        firstRun.value = false
    }

    // This row needs padding at the end to make space for the scrollbar if needed
    Row {
        // Background of line numbers & line menus over whole screen.
        Box(
            modifier = Modifier
                .background(color = if (!readOnly)
                    MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                .width(70.dp).fillMaxHeight()
        ) {
            //line numbers, left column
            Column {
                for (i in 1..lines.value.text.lines().size) {
                    Box {
                        //explanations / errors for each line
                        if (lineAnnotations[i - 1] != null)
                            SelectableIcon(
                                Icons.Default.Notifications,
                                AppTheme.colors.iconRed,
                                lineAnnotations[i - 1]!!
                            ) {}
                        Text(
                            text = String.format("%5d ", i),
                            fontSize = AppTheme.fonts.medium,
                            fontFamily = Fonts.jetbrainsMono,
                            lineHeight = fixedLineHeight, // synchronize height with height in editable field below
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.50f),
                            modifier = Modifier.padding(0.dp),
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }


        Box{
            // The editable field itself, right of line numbers.
            BasicTextField(
                readOnly = readOnly,
                //replace tabs with 4 spaces if there are tabs
                value =
                    if (useHighlighting) {
                        lines.value
                        if (lines.value.text.contains("\t")) lines.value = lines.value.replaceTab()
                        //Update the Syntax Highlighting
                        lines.value
                        oldTextFieldValue.value = SyntaxHighlighter.updateSyntaxHighlighting(newTFV = lines.value, oldTFV = oldTextFieldValue.value, colorScheme = sysMDColorScheme, replaceTabFunction = TextFieldValue::replaceTab)
                        //oldTextFieldValue.value = SyntaxHighlighter.applyFullSyntaxHighlighting(tfv = lines.value, colorScheme = sysMDColorScheme)
                        //Output to be viewed by BasicTextField (oldTextFieldValue can be safely used here as to this point of time, it is up-to-date)
                        oldTextFieldValue.value
                    } else {
                        if (lines.value.text.contains("\t")) lines.value = lines.value.replaceTab()
                        //Store new textFieldValue so when update occurs we can compare new TextField to old one
                        oldTextFieldValue.value = lines.value
                        //Output to be viewed by BasicTextField (oldTextFieldValue can be safely used here as to this point of time, it is up-to-date)
                        oldTextFieldValue.value
                    },
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = AppTheme.fontSize,
                    lineHeight = fixedLineHeight,
                    fontFamily = Fonts.jetbrainsMono,
                ),
                modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
                    .width(if (resultsAnnotations.isNotEmpty()) rowWidth - resultsColumnWidth else rowWidth)
                    .fillMaxHeight()
                    .onPreviewKeyEvent {
                        keyInputHandler(keyEvent = it,lines,suggestions,oldTextFieldValue, keyDelay)
                    }
                    .onPointerEvent(PointerEventType.Press){
                        suggestions.value.close()
                    }
                    //.withoutWidthConstraints()
                    .horizontalScroll(horizontalState, enabled = true)
                    .onGloballyPositioned { coordinates ->
                    editorHeight.value = with(density) { coordinates.size.height.toDp() }
                },

                // only perform syntax highlighting if you need it e.g. if you are in a code block
                //visualTransformation = if (useHighlighting) syntaxHighlightingTransformation else VisualTransformation.None,
                onValueChange = {

                    if(!readOnly){

                        lastChange.value = currentNanoTime()

                        lines.value = checkAndAddIndents(it, oldTextFieldValue)
                        elementEdited.value = true


                        //Launch a coroutine which re-analyzes the EditorField if it hasn't been changes for a specific period of time
                        coroutineScope.launch {
                            //Delay coroutine for a specific time
                            delay(delayValue.toLong())

                            //Check if no updates have happened in the field for a specific period of time
                            if(currentNanoTime() - lastChange.value > delayValue*1e+6){

                                //Pass new Text Content to indexer to update Indices
                                with(indexer) { isUpdating.value = true
                                    it.updateLocalIndexes(localComponentsIndexReference = localComponentsIndex, localPackagesIndexReference = localPackagesIndex)
                                    //Clear the current inconsistencies
                                    inconsistencies.clear()
                                    //Update the inconsistencies
                                    inconsistencies.addAll(SyntaxHighlighter.checkForInconsistencies(oldTextFieldValue, sysMDColorScheme))
                                    isUpdating.value = false
                                }
                            }

                        }
                    }
                },
            )

            /**Block for Suggestions Menu*/
            if(suggestions.value.show) {
                suggestionsDropDown(suggestions,lines,fixedLineHeight,enableElementListScrolling, editorHeight)
            }else{
                enableElementListScrolling.value = true //Make sure the ElementList can be scrolled when Suggestion Box is not visible!
            }

            /**Block for the Warning Section in the right upper corner*/
            if(isUpdating.value) {
                Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)){
                    CircularProgressIndicator(modifier = Modifier.size(AppTheme.fontSize.value.dp))
                }
            }else{
                if(false){
                //TODO Uncomment line below if red error symbol on right upper corner should be shown
                //if(inconsistencies.isNotEmpty()){
                    Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)){
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = inconsistencies.size.toString(), fontSize = AppTheme.fontSize)
                            SelectableIcon(
                                icon = Icons.Outlined.Error,
                                color = AppTheme.colors.iconRed,
                                tooltip = if(inconsistencies.size == 1) "There is 1 inconsistency!" else "There are ${inconsistencies.size} inconsistencies!",
                                onSelection = {})

                        }
                    }
                }
            }
        }

    }

    HorizontalScrollbar(
        adapter = rememberScrollbarAdapter(horizontalState), modifier = Modifier.align(Alignment.BottomStart)
    )
}

/**
 * This function analyzes the key input and manages the suggestions features like
 * navigating in the DropDown Menu.
 * The function is always used in combination with a suggestionsDropDown Composable
 * which should be shown in a TextField.
 *
 * @param keyEvent The KeyEvent from the .onKeyEvent() function of the TextField.
 * @param tfv The TextFieldValue in which the Suggestions Menu should be shown and inserted.
 * @param sg The Suggestions Data Class object which holds all necessary Attributes and States of the
 * @param oldTfv The old TextFieldValue before a change was made.
 * @return returns a Boolean indicating if the pressed key should be forwarded or intercepted
 */
private fun keyInputHandler(
    keyEvent: KeyEvent,
    tfv: MutableState<TextFieldValue>,
    sg: MutableState<SuggestionsData>,
    oldTfv: MutableState<TextFieldValue>,
    keyDelay: MutableState<KeyDelay>
) : Boolean {

    sg.value.cursorIndex = tfv.value.selection.start

    when(keyEvent.key.keyCode){

        Key.DirectionDown.keyCode -> {
            if(sg.value.show){

                if(keyDelay.value.isLocked()) return true

                return true.also {
                    if (sg.value.currMenuIndex > sg.value.listState.firstVisibleItemIndex + sg.value.itemsShown - 1) {   //Check if current Menu Index is visually BELOW the field of visible items
                        sg.value.listState = LazyListState(firstVisibleItemIndex = sg.value.currMenuIndex)
                    } else if (sg.value.currMenuIndex < sg.value.listState.firstVisibleItemIndex) {                       //Check if current Menu Index is visually ABOVE the field of visible items
                        sg.value.listState = LazyListState(firstVisibleItemIndex = sg.value.currMenuIndex)
                    } else {
                        //Increases the current Menu Index and scrolls the list down if necessary
                        sg.value.currMenuIndex++
                        if (sg.value.currMenuIndex > sg.value.filteredSuggestions.size - 1) sg.value.currMenuIndex =
                            sg.value.filteredSuggestions.size - 1
                    }
                    //Do quick manipulation of TextFieldValue to trigger recomposition (is invisible to user)
                    //First, add a space to force recomposition
                    tfv.value = TextFieldValue(StringBuilder(tfv.value.text).append(" ").toString(), tfv.value.selection)
                    //Now apply the old text-field again, ensures that Syntax Highlighting is visible again
                    tfv.value = oldTfv.value
                }
            }
        }

        Key.DirectionUp.keyCode -> {
            if(sg.value.show){

                if(keyDelay.value.isLocked()) return true

                return true.also {
                    if(sg.value.currMenuIndex > sg.value.listState.firstVisibleItemIndex + sg.value.itemsShown - 1) {  //Check if current Menu Index is visually BELOW the field of visible items
                        sg.value.listState = LazyListState(firstVisibleItemIndex = sg.value.currMenuIndex)
                    }else if(sg.value.currMenuIndex < sg.value.listState.firstVisibleItemIndex){                       //Check if current Menu Index is visually ABOVE the field of visible items
                        sg.value.listState = LazyListState(firstVisibleItemIndex = sg.value.currMenuIndex)
                    }else{
                        //Decreases the current Menu Index and scrolls the list up if necessary
                        sg.value.currMenuIndex--
                        if(sg.value.currMenuIndex < 0) sg.value.currMenuIndex = 0
                    }
                    //Do quick manipulation of TextFieldValue to trigger recomposition (is invisible to user)
                    //First, add a space to force recomposition
                    tfv.value = TextFieldValue(StringBuilder(tfv.value.text).append(" ").toString(), tfv.value.selection)
                    //Now apply the old text-field again, ensures that Syntax Highlighting is visible again
                    tfv.value = oldTfv.value
                }
            }

        }

        Key.DirectionRight.keyCode, Key.DirectionLeft.keyCode -> {
            //If suggestions are shown, Left and Right arrow should be intercepted
            if(sg.value.show) return true
        }

        43486543872, Key.NumPadEnter.keyCode, Key.Enter.keyCode -> {
            //If suggestions are shown then Enter press means that something
            //from the Suggestions menu has been chosen and should be inserted into the text
            if(sg.value.show){
                return true.also {
                    tfv.value = insertSuggestion(
                        tfv = tfv.value,
                        index = sg.value.cursorIndex,
                        suggestion = sg.value.filteredSuggestions[sg.value.currMenuIndex],
                        sg = sg
                    )

                    //Disable the suggestions and reset the selected Menu Index
                    sg.value.show = false
                    sg.value.currMenuIndex = 0
                    sg.value.listState = LazyListState(0)
                }
            }
        }

        Key.Tab.keyCode -> {

            if(keyDelay.value.isLocked()) return true

            return true.also {
                //If a Shift-Tab combination is pressed, the user wants to remove indents from this line
                if(keyEvent.isShiftPressed){
                    //Remove indents at beginning of lines
                    tfv.value = handleIndents(tfv,removeIndents = true)
                }else{
                    //Add indents at beginning of lines
                    tfv.value = handleIndents(tfv, removeIndents = false)
                }
            }
        }

        Key.Escape.keyCode -> {
            sg.value.currMenuIndex = 0
            sg.value.show = false
        }

        Key.Spacebar.keyCode -> {
            //If suggestions are not already shown
            if(!sg.value.show) {
                //Check if suggestions should be activated
                sg.value.show = checkSuggestionsInit(tfv,sg)
            }else{
                //User escaped suggestions menu via Spacebar press -> disable suggestions menu
                sg.value.close()
            }

        }

        else -> {
            // println("Some else key pressed")
        }
    }

    return false
}

/**This function takes a TextFieldValue and adds indents if a newline was inserted.
 * @param tfv The most recent TextFieldValue
 * @param oldTfv The previous TextFieldValue of the Editor. Used to check if the size has increased.
 * @return A TextFieldValue that contains the indents if they were added.**/
private fun checkAndAddIndents(tfv: TextFieldValue, oldTfv: MutableState<TextFieldValue>) : TextFieldValue {

    //Check if the text size has increased by one (we do not add indents if a whole section was pasted) and if a newline was added in front of the cursor
    if(tfv.text.length-1 == oldTfv.value.text.length){

        //Get text before (aka. above) the cursor
        var textAbove = tfv.text.substring(0, tfv.selection.start)
        val textAfter = tfv.text.substring(tfv.selection.end, tfv.text.length)

        when(tfv.text[tfv.selection.start - 1]){


            //When User enters a new line, calculate required indents and detect if a prefix has to be added to the line (e.g.: a comment)
            '\n' -> {

                /**Tells if the cursors has to be shifted to the left or right independently from any brace indentation*/
                var cursorsShift = 0

                /** Returns the prefix of the current line (e.g.: like "//" for a comment)*/
                val linePrefix : (Int) -> String= {indents ->


                    if(tfv.selection.start < 3) {
                        //If the cursor near the beginning of the text, we can skip checking - we return an empty string
                        ""
                    } else if(textAbove.substring(textAbove.length-3,textAbove.length-1) == "/*"){
                        //If there is a long comment start in front of the cursor, add the long comment behind it
                        "\n" + " ".repeat(indents) + "*/"
                    }else if(textAbove[textAbove.length-2] == '{'){ //Adds a closing scope brace if needed
                            "\n" + " ".repeat(indents-4) + "}"
                    }else if(textAfter.substringBefore('\n').contains(Regex("\\S"))){ //In case of this line being a single comment line, check if after cursors follows text. If yes, a "//" single line comment will be added automatically
                        val currLine = textAbove.removeRange(textAbove.length-1,textAbove.length).substringAfterLast('\n')

                        if(currLine.contains("//"))  {
                            cursorsShift += 2
                            "//"
                        }else ""
                    }else{
                        //If no other cases apply, return empty string
                        ""
                    }
                 }

                var braceIndents = 0
                var whiteSpaceIndents = 0

                /**Last Opening Brace Indent*/
                var lastOBI = 0
                /**Last Closing Brace Indent*/
                var lastCBI = 0

                var lastBrace = ' '
                var countWhitespace = true

                textAbove.forEach { char ->
                    when(char){
                        ' '     -> if(countWhitespace) whiteSpaceIndents += 1
                        '\t'    -> if(countWhitespace) whiteSpaceIndents += 4
                        '{'     -> {braceIndents += 4; lastOBI = whiteSpaceIndents; lastBrace = char; countWhitespace = false}
                        '}'     -> {braceIndents -= 4; lastCBI = whiteSpaceIndents; lastBrace = char; countWhitespace = false}
                        '\n'    -> {whiteSpaceIndents = 0; countWhitespace = true}
                        else -> countWhitespace = false
                    }
                }

                if(lastBrace == '{'){
                    braceIndents = lastOBI + 4
                }else if (lastBrace == '}'){
                    braceIndents = lastCBI
                }

                //Make sure indents do not go negative
                if(braceIndents < 0) braceIndents = 0


                //Return a TextFieldValue with the inserted indents and the cursor position adjusted
                return TextFieldValue(
                    text = StringBuilder(tfv.text).insert(tfv.selection.start," ".repeat(braceIndents) + linePrefix(braceIndents)  ).toString(),
                    selection = TextRange( tfv.selection.start + braceIndents + cursorsShift)
                )
            }

            //When user ends a curly brace scope, we want to shift the closing brace to the left to match the opening brace of this scope
            '}' -> {

                if(textAbove.length > 2){
                    textAbove = textAbove.substring(0,textAbove.length-1)
                    var removableIndents = 0

                    //Check how many indentations to the left are needed or if they are needed at all
                    for(i in 1.. textAbove.length){

                        //Count Whitespaces to the left of the closing brace
                        if(i <= 4) {
                            if (textAbove[textAbove.length - i] == ' ') {
                                removableIndents++
                            } else {
                                if (textAbove[textAbove.length - i] == '{') {
                                    removableIndents = 0
                                }
                                break
                            }
                        }else{
                            //If we exceed the 4 positions to the left of the closing brace,
                            // we are not interested into whitespaces anymore but have to check if withing the same line there is a opening brace "{"
                            if(textAbove[textAbove.length - i] == '\n'){
                                //If we hit a newLine without having hit an opening brace before, the indentation is legit - we can break here
                                break
                            }
                            if(textAbove[textAbove.length - i] == '{'){

                                //If we hit an opening brace before we have reached a newLine, then the indentation is not needed - set it to 0 and break here
                                removableIndents = 0
                                break
                            }
                        }
                    }


                    return TextFieldValue(
                        text = StringBuilder(tfv.text).removeRange(textAbove.length - removableIndents, textAbove.length).toString(),
                        selection = TextRange( tfv.selection.start - removableIndents)
                    )
                }
            }


        }
    }

    //No indents were added, just return the input TextFieldValue as is
    return tfv
}

/**Returns the enlarged version of the current selection.
 * The enlarged selection ensures, that the first and last line are completely included.
 * In other words, the selection start is moved backwards to the beginning of the first selected line and
 * the selection end is moved forwards to the end of the last selected line.
 * @return The TextRange that comprises the enlarged selection.
 **/
fun TextFieldValue.getEnlargedSelection() : TextRange{

    //Swaps the start and end cursors if necessary
    var start = if(selection.start < selection.end) selection.start else selection.end
    var end = if(selection.end > selection.start) selection.end-1 else selection.start

    if(start >= text.length) start--
    if(start < 0) start = 0


    //Travel left to find the end of the beginning of the first line that is included in the cursors selection
    while (start > 0  &&  text[start] != '\n'){
        if(text[start-1] != '\n') start-- else break
    }

    //Travel right to find the end of the end of the last line that is included in the cursors selection
    while (end < text.length-1 && text[end] != '\n' && end < text.length){
        if(text[end+1] != '\n') end++ else {
            //Note: Have to increment end because otherwise the char before the endLine is not included
            end++
            break
        }
    }

    return TextRange(start,end)
}

/**Removes or adds indentation at the beginning of selected lines.
 * @param tfv The TextFieldValue that is currently edited.
 * @param removeIndents Boolean flag that tells if indents should be removed or added.
 */
private fun handleIndents(tfv : MutableState<TextFieldValue>, removeIndents : Boolean) : TextFieldValue{

    //Easy access to data of TextFieldValue via lambdas
    val selectionStart : () -> Int = {if(tfv.value.selection.start < tfv.value.selection.end) tfv.value.selection.start else tfv.value.selection.end}
    val selectionEnd : () -> Int = {if(tfv.value.selection.start < tfv.value.selection.end) tfv.value.selection.end else tfv.value.selection.start}
    val text : () -> String = {tfv.value.text}

    /**Start position for the area that is edited.
     * If multiple lines are selected, this will be at the beginning of the first line.*/
    var start = selectionStart()

    /**End position for the area that is edited.
     * If multiple lines are selected, this will be at the end of the last line.*/
    var end = selectionEnd()

    //Simple case if cursor start and end position is equal, simply add a tab at the current position
    if(!removeIndents && start == end){
        return TextFieldValue(
            text = StringBuilder(text()).insert(start,"\t").toString(),
            selection = TextRange(start+1)
        )
    }

    //Get the enlarged selection so that the first and last line are completely included in the selection
    tfv.value.getEnlargedSelection().also {
        start = it.start
        end = it.end
    }

    val lines = text().substring(start,end).lines()

    /**Tells how many chars have been removed/added in total.
     * Negative numbers represent removal of chars and positive number the addition of chars.
     * Used to adjust the end position of the cursor.**/
    var totalCharsEdited = 0

    /**Tells how many chars have been removed/added in the first line.
     * Negative numbers represent removal of chars and positive number the addition of chars.
     * Needed to adjust the starting position of the cursor.**/
    var firstLineEdits = 0

    val strBld = StringBuilder()


    lines.forEachIndexed{ lineIdx,it->
        if(it.isNotEmpty()){
            if(removeIndents){
                //Remove indents from beginning of line
                var rmvIdx = 0
                while (it[rmvIdx] == ' ' && rmvIdx < 4 && selectionStart()-rmvIdx > 0){
                    if(rmvIdx+1 >= it.length) break else rmvIdx++
                }

                //Append this edited line to the result string and for every line except the last one add the newLine Char again
                strBld.append(it.removeRange(0,rmvIdx) + if(lineIdx < lines.size-1) "\n" else "")
                totalCharsEdited -= rmvIdx
                if(lineIdx == 0) firstLineEdits = -rmvIdx
            }else{
                //Add indents to the beginning of every line
                strBld.append(" ".repeat(settings.tabSize) + it + if(lineIdx < lines.size-1) "\n" else "")
                totalCharsEdited += 4
                if(lineIdx == 0) firstLineEdits = 4
            }

        }else{
            if(lineIdx < lines.size-1) strBld.append("\n")
        }
    }

    return TextFieldValue(
        text = StringBuilder(text()).replace(start,end,strBld.toString()).toString(),
        selection = TextRange(maxOf(selectionStart() + firstLineEdits, start),selectionEnd() + totalCharsEdited)
    )

}


private fun TextFieldValue.replaceTab(): TextFieldValue {
    var replaced = ""
    var count = 0
    this.text.lines().forEachIndexed { i, line ->
        val parts = line.split('\t')
        parts.forEachIndexed { index, part ->
            if (index == parts.size - 1) {
                replaced += part
            } else {
                count = settings.tabSize - (part.length % settings.tabSize)
                replaced += part + " ".repeat(count)
            }
        }
        if (i != text.lines().size - 1) replaced += "\n"
    }
    return copy(text = replaced,
        selection = with(this.selection) {
            if (collapsed) TextRange(
                start + count - 1, end + count - 1
            ) else TextRange(start, end + count - 1)
        }
    )
}


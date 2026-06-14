package ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.services.repositories.local.Language
import com.github.tukcps.sysmd.ui.viewmodel.CellListViewModel
import com.github.tukcps.sysmd.ui.viewmodel.CellViewModel
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel
import util.assertNoIssues
import util.testProjectSession
import kotlin.test.Test
import kotlin.test.assertTrue

class CellViewModelTests {

    @Test
    fun testInheritedFeaturesDisplayedWhenNoLocalFeatures() = testProjectSession("ScalarValues", "Base", "Objects", "Occurrences", "Links") {
        // 1. Load the superclass defining a feature
        val superclassCode = """
            package test {
                private import ScalarValues::*;
                class Vehicle {
                    feature weight : Real = 1500.0;
                }
            }
        """.trimIndent()
        
        KerML(this).parse(superclassCode, "Global")
        
        // 2. Load the subclass defining no local features, but specializing/extending the superclass
        val subclassCode = """
            package test {
                private import ScalarValues::*;
                class Car specializes test::Vehicle;
            }
        """.trimIndent()

        val sysMDViewModel = SysMDViewModel().also { it.sessionIdState.value = id}
        val sessionIdState = sysMDViewModel.sessionIdState
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        val tabViewModel = CellListViewModel(sessionIdState = sessionIdState, tabsViewModel, nameState = mutableStateOf("name"))
        val cell = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.KerML),
            bodyState = mutableStateOf(TextFieldValue(subclassCode))
        )
        
        // Compile the subclass cell (with propagation enabled, which will trigger solve & initialization)
        cell.compile(propagate = true)
        
        assertNoIssues()
        
        // Verify that the inherited feature "weight" is displayed in the GUI cell items!
        val displayStrings = cell.displayItems.map { it.text }
        
        // Let's print them for debugging if it fails
        println("Display items:")
        displayStrings.forEach { println(it) }
        
        // Check that "Car" class is mentioned
        assertTrue(displayStrings.any { it.contains("Car") && it.contains("created or updated") })
        // Check that inherited feature "weight = 1500" is shown under "Car"!
        assertTrue(displayStrings.any { it.contains("Feature: weight") && it.contains("1500") })
    }
}

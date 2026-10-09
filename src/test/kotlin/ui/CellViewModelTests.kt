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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Some tests for cell view model in SysMD Notebook.
 * Different cells are compiled step by step. 
 */
class CellViewModelTests {

    @Ignore // TODO: adapt to SysMD interactive
    @Test
    fun testInheritedAndLocalPropertiesDisplayed() = testProjectSession("ScalarValues") {
        // 1. Load the superclass defining a feature in cell 1
        val superclassCode = """
            package test {
                private import ScalarValues::*;
                classifier Vehicle {
                    feature weight : Real = 1500.0;
                }
            }
        """
        import(KerML(this).parse(superclassCode))
        
        // 2. Load the subclass defining no local features, but specializing/extending the superclass
        val subclassCode = """
            package test {
                private import ScalarValues::*;
                classifier Car specializes test::Vehicle;
            }
        """
        import(KerML(this).parse(subclassCode, "Global"))

        val sysMDViewModel = SysMDViewModel().also { it.sessionIdState.value = id}
        val sessionIdState = sysMDViewModel.sessionIdState
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        val tabViewModel = CellListViewModel(sessionIdState = sessionIdState, tabsViewModel, nameState = mutableStateOf("name"))
        val cell = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.KerML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(subclassCode))
        )
        
        // Compile the subclass cell (with propagation enabled, which will trigger solve & initialization)
        cell.compile(propagate = true)
        
        assertNoIssues()
        
        // Verify that the inherited feature "weight" is displayed in the GUI cell items!
        val displayStrings = cell.displayItems.map { it.text }
        
        // Let's print them for debugging if it fails
        // println("Display items:")
        displayStrings.forEach { println(it) }
        
        // Check that "Car" class is mentioned
        assertTrue(displayStrings.any { it.contains("Car") && it.contains("created or updated") })
        // Check that local feature "wheels" is shown
        assertTrue(displayStrings.any { it.contains("wheels") && it.contains("4") })
        // Check that inherited feature "weight" IS shown under Car
        assertTrue(displayStrings.any { it.contains("weight") && it.contains("1500") })
        assertTrue(displayStrings.any { it.contains("Feature: weight") && it.contains("1500") })
    }

    @Test
    fun testPropertiesOrderedAlphabeticallyInBlock() = testProjectSession("ScalarValues") {
        val cellCode = """
            package test {
                private import ScalarValues::*;
                classifier Device {
                    feature zebra : Real = 1.0;
                    feature apple : Real = 2.0;
                    feature mango : Real = 3.0;
                }
            }
        """
        val sysMDViewModel = SysMDViewModel().also { it.sessionIdState.value = id }
        val sessionIdState = sysMDViewModel.sessionIdState
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        val tabViewModel = CellListViewModel(sessionIdState = sessionIdState, tabsViewModel, nameState = mutableStateOf("name"))
        val cell = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.KerML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(cellCode))
        )

        cell.compile(propagate = true)
        assertNoIssues()

        val featureLines = cell.displayItems.map { it.text }.filter { it.contains("Feature:") }
        val featureNames = featureLines.map { line -> line.substringAfter("Feature:").substringBefore("=").trim() }

        kotlin.test.assertEquals(listOf("apple", "mango", "zebra"), featureNames)
    }

    @Test
    fun testConstraintDisplayedInCurrentClassAndSubclass() = testProjectSession("ScalarValues", "ISQ", "Parts", "Constraints", "Quantities", "Ranges") {
        val sysMDViewModel = SysMDViewModel().also { it.sessionIdState.value = id }
        val sessionIdState = sysMDViewModel.sessionIdState
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        val tabViewModel = CellListViewModel(sessionIdState = sessionIdState, tabsViewModel, nameState = mutableStateOf("name"))

        val kermlCode = """
            package kermlTest {
                private import ScalarValues::*;
                classifier Device {
                    feature x : Real = 10.0;
                    inv { x > 0.0 }
                    inv cKerml { x < 100.0 }
                }
            }
        """
        val cell1 = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.KerML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(kermlCode))
        )
        cell1.compile(propagate = true)
        assertNoIssues()
        val kermlDisplay = cell1.displayItems.map { it.text }
        assertTrue(kermlDisplay.any { it.contains("x") && it.contains("10") })
        assertFalse(kermlDisplay.any { it.contains("cKerml") || it.contains("x > 0.0") })

        val sysmlCode = """
            package sysmlTest {
                private import ScalarValues::*;
                part def System {
                    attribute val : Real = 5.0;
                    assert constraint { val > 0.0 }
                    assert constraint cSysml { val < 100.0 }
                    assert { val >= 1.0 }
                    constraint cSysml2 { val != 0.0 }
                }
            }
        """
        val cell2 = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.SYS_ML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(sysmlCode))
        )
        cell2.compile(propagate = true)
        assertNoIssues()
        val sysmlDisplay = cell2.displayItems.map { it.text }
        assertTrue(sysmlDisplay.any { it.contains("val") && it.contains("5") })
        assertFalse(sysmlDisplay.any { it.contains("cSysml") || it.contains("cSysml2") || it.contains("val > 0.0") || it.contains("val >= 1.0") })

        val userScenarioCell1 = """
            package OpenBoardnet {
                package CU {
                    private import ISQ::*;
                    private import Quantities::*;
                    private import Ranges::*;
                    part def ControlUnit {
                        attribute fclk: FrequencyValue {:>> range default = 0.1 .. 100000.0 [MHz];}
                        attribute opsPerCyle: DimensionOneValue {:>> range default = 1.0 .. 1000.0;}
                        attribute FLOPS_Hardware: FrequencyValue = fclk * opsPerCyle {:>> unit = "GFLOPS";}
                    }
                    part def Yolov5n {
                        attribute FLOPsTotal: DimensionOneValue = 4.51913;
                        attribute MemoryTotal: StorageCapacityValue = 7.54565 [MB];
                    }
                    part def ADASController :> ControlUnit {
                        part runningModel : Yolov5n;
                        attribute FLOPs_Total: DimensionOneValue = runningModel::FLOPsTotal {:>> unit = "GFLOPs";}
                        attribute Memory_Total: StorageCapacityValue = runningModel::MemoryTotal {:>> unit = "MB";}
                        attribute T : DurationValue = FLOPs_Total / FLOPS_Hardware {:>> unit = "ms";}
                        attribute R : DurationValue = 33.0[ms] {:>> unit = "ms";} 
                        assert constraint TimeRequirement { R >= T }
                        attribute Memory_Hardware : StorageCapacityValue {:>> range default = 1.0 .. 100000.0 [MB];}
                        assert constraint MemoryRequirement { Memory_Hardware >= Memory_Total }
                    }
                }
            }
        """
        val cellAdas = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.SYS_ML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(userScenarioCell1))
        )
        cellAdas.compile(propagate = true)
        assertNoIssues()
        val adasDisplay = cellAdas.displayItems.map { it.text }
        // Verify constraints are NOT displayed in result view!
        assertFalse(adasDisplay.any { it.contains("TimeRequirement") || it.contains("MemoryRequirement") }, "Constraints should not be displayed in result view")

        val userScenarioCell2 = """
            package OpenBoardnet {
                package CU {
                    part def ARMCortex :> ADASController {
                        :>> fclk {:>> range = 240.0 [MHz];}
                        :>> opsPerCyle = 2.0;
                        :>> Memory_Hardware = 2.0 [MB];
                    }
                }
            }
        """.trimIndent()
        val cellArmCortex = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.SYS_ML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(userScenarioCell2))
        )
        cellArmCortex.compile(propagate = true)

        // Refresh cellAdas display
        cellAdas.collectVariablesToDisplay()
        cellArmCortex.collectVariablesToDisplay()

        val adasDisplayAfter = cellAdas.displayItems.map { it.text }
        val armDisplay = cellArmCortex.displayItems.map { it.text }

        // Inconsistency MUST NOT be displayed in superclass cell ADASController
        assertFalse(adasDisplayAfter.any { it.startsWith("INFO: Constraint") || it.startsWith("ERROR: Constraint") }, "Inconsistency must not be visible in superclass ADASController: $adasDisplayAfter")
        assertTrue(cellAdas.annotations.isEmpty(), "No error bells on superclass cell ADASController: ${cellAdas.annotations}")

        // Constraints should NOT be listed as normal feature results in displayItems
        assertFalse(armDisplay.any { it.startsWith("    Feature: TimeRequirement") || it.startsWith("    Feature: MemoryRequirement") }, "Constraints should not be listed as normal feature values: $armDisplay")

        // Inconsistency INFO MUST be displayed in subclass cell ARMCortex
        assertTrue(armDisplay.any { it.startsWith("INFO: Constraint 'MemoryRequirement'") && it.contains("ARMCortex") }, "Inconsistency INFO should be displayed under subclass cell ARMCortex: $armDisplay")
        assertTrue(status.issues.any { it.message.contains("MemoryRequirement") && it.message.contains("ARMCortex") }, "Inconsistency issue for MemoryRequirement in ARMCortex: ${status.issues}")
    }

    @Test
    fun testNoSlashTwoOneAttributesOrUnitRangePrinted() = testProjectSession("ISQ", "Parts") {
        val cellCode = """
            package test {
                private import ISQ::*;
                part vehicle {
                    attribute speed : SpeedValue [m/s] = 100.0 [m/s];
                }
            }
        """
        val sysMDViewModel = SysMDViewModel().also { it.sessionIdState.value = id }
        val sessionIdState = sysMDViewModel.sessionIdState
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        val tabViewModel = CellListViewModel(sessionIdState = sessionIdState, tabsViewModel, nameState = mutableStateOf("name"))
        val cell = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.SYS_ML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(cellCode))
        )
        
        cell.compile(propagate = true)
        assertNoIssues()
        
        val displayStrings = cell.displayItems.map { it.text }
        
        // Check that speed is shown
        assertTrue(displayStrings.any { it.contains("speed") })
        // Check that no attribute ending in /2/1 or containing /2/1 is shown
        assertTrue(displayStrings.none { it.contains("/2/1") })
        // Check that unit and range calculation attributes are never printed
        assertTrue(displayStrings.none { it.trim().startsWith("unit") || it.contains("::unit") || it.contains("Feature: unit") })
        assertTrue(displayStrings.none { it.trim().startsWith("range") || it.contains("::range") || it.contains("Feature: range") })
    }

    @Test
    fun testOpenNetworkTabs() {
        val sysMDViewModel = SysMDViewModel()
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        
        tabsViewModel.showTab("NeuralNetwork.md")
        // Try opening with different casing to verify case-insensitive matching
        tabsViewModel.showTab("network.md")
        
        // println("SelectedIndex after opening network.md: ${tabsViewModel.selectedIndex.value}")
        // println("Tabs: ${tabsViewModel.editorTabs.map { it.nameState.value }}")
        
        assert(tabsViewModel.selectedIndex.value == 1)

        // Verify name-based tab renaming works
        tabsViewModel.updateTabTitle("network.md", "Network-Renamed.md")
        assert(tabsViewModel.editorTabs[1].nameState.value == "Network-Renamed.md")
    }

    @Test
    fun testCalculationDefinitionAttributesNotDisplayed() = testProjectSession("ScalarValues", "Calculations") {
        val sysMDViewModel = SysMDViewModel().also { it.sessionIdState.value = id }
        val sessionIdState = sysMDViewModel.sessionIdState
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        val tabViewModel = CellListViewModel(sessionIdState = sessionIdState, tabsViewModel, nameState = mutableStateOf("name"))
        val cell = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.SYS_ML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue("""
                package calcTest {
                    private import ScalarValues::*;
                    calc def Twice {
                        in x: Real;
                        attribute factor: Real = 2.0;
                        return result: Real = x * factor;
                    }
                    attribute a: Real = Twice(2.0);
                }
            """))
        )
        cell.compile(propagate = true)

        val display = cell.displayItems.map { it.text }
        assertTrue(display.any { it.contains("a") && it.contains("4") })
        assertFalse(display.any { it.contains("factor") || it.contains("result") })
    }

    @Test
    fun testCalcDefWithRangeParametersAndOtherCell() = testProjectSession("ScalarValues", "ISQ", "Quantities", "Ranges", "Calculations") {
        val sysMDViewModel = SysMDViewModel().also { it.sessionIdState.value = id }
        val sessionIdState = sysMDViewModel.sessionIdState
        val tabsViewModel = sysMDViewModel.editorTabsViewModel
        val tabViewModel = CellListViewModel(sessionIdState = sessionIdState, tabsViewModel, nameState = mutableStateOf("name"))
        fun cell(code: String) = CellViewModel(
            cellListViewModel = tabViewModel,
            sessionIdState = sessionIdState,
            refreshTrees = {},
            language = mutableStateOf(Language.SYS_ML),
            namespace = mutableStateOf("Global"),
            bodyState = mutableStateOf(TextFieldValue(code))
        )
        val other = cell("""
            package Other {
                private import ISQ::*;
                attribute otherValue: DimensionOneValue = 3.0;
            }
        """)
        other.compile(propagate = true)
        val calc = cell("""
            package OpenBoardnet { package Safety {
                private import ISQ::*;
                private import ScalarValues::*;
                private import Ranges::*;
                private import Quantities::*;
                calc def calculateASILDecomposition {
                    in attribute part1: DimensionOneValue {:>> range = 0..4;}
                    in attribute part2: DimensionOneValue {:>> range = 0..4;}
                    in attribute isRedundant: Boolean;
                    attribute redundantLevel: DimensionOneValue = min(part1 + part2, 4.0 );
                    attribute notredundantLevel: DimensionOneValue = min(part1, part2);
                    return result: DimensionOneValue = if isRedundant ? redundantLevel else notredundantLevel {:>> range = 0..4;}
                }
            } }
        """)
        calc.compile(propagate = true)
        val display = calc.displayItems.map { it.text }
        assertFalse(display.any { it.contains("part1") || it.contains("redundantLevel") || it.contains("result") || it.contains("otherValue") })
    }
}

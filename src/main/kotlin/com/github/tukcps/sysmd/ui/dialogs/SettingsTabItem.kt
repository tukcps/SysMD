package com.github.tukcps.sysmd.ui.dialogs

import androidx.compose.runtime.Composable
import com.github.tukcps.sysmd.ui.viewmodel.resetBoard
import com.github.tukcps.sysmd.ui.viewmodel.resetLogin
import com.github.tukcps.sysmd.ui.viewmodel.resetRendering
import com.github.tukcps.sysmd.ui.viewmodel.resetSolver

sealed class SettingsTabItem(var title: String, var screen: @Composable () -> Unit, var reset: () -> Unit) {
    data object Login : SettingsTabItem("Projects", { LoginScreen() }, { resetLogin() })
    data object Solver : SettingsTabItem("Solver", { SolverScreen() }, { resetSolver() })
    data object Rendering : SettingsTabItem("Rendering", { RenderingScreen() }, { resetRendering() })
    data object Board : SettingsTabItem("Board", { BoardScreen() }, { resetBoard() })
}

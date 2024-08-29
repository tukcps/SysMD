package com.github.tukcps.sysmd.ui.styles

import androidx.compose.runtime.Composable
import com.github.tukcps.sysmd.ui.*
import com.github.tukcps.sysmd.ui.viewmodel.*

sealed class SettingsTabItem(var title: String, var screen: @Composable () -> Unit, var reset: () -> Unit) {
    data object Login : SettingsTabItem("Projects", { LoginScreen() }, { resetLogin() })
    data object Solver : SettingsTabItem("Solver", { SolverScreen() }, { resetSolver() })
    data object Rendering : SettingsTabItem("Rendering", { RenderingScreen() }, { resetRendering() })
    data object Agenda : SettingsTabItem("Agenda", { AgendaScreen() }, { resetAgenda() })
}

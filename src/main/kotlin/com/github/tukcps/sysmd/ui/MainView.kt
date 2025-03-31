package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.github.tukcps.sysmd.ui.viewmodel.SysMDViewModel

/**
 * The main window of SysMD
 */
@Composable
fun MainView(sysMdViewModel: SysMDViewModel) {
    DisableSelection {
        Surface {
            SysMDView(sysMdViewModel)
        }
    }
}
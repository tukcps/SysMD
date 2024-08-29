package com.github.tukcps.sysmd.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.tukcps.sysmd.ui.styles.AppTheme

@Composable
fun EditorEmptyView() = Box(Modifier.fillMaxSize()) {
    Column(Modifier.align(Alignment.Center)) {


        Spacer(Modifier.height(50.dp))

        Icon(
            Icons.Default.Code,
            contentDescription = null,
            tint = LocalContentColor.current.copy(alpha = 0.60f),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(36.dp)
        )

        Spacer(Modifier.height(50.dp))

        Text(
            "New to SysMD? Beginner?",
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 24.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(6.dp)
        )
        Text(
            "Double-click on \"SysMD-Kickstart.md\" in the navigation panel left for a quick introduction.",
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(6.dp)
        )

        Spacer(Modifier.height(50.dp))

        Text(
            """This is SysMD Notebook version ${AppTheme.version}
                
               Release Notes for ${AppTheme.version}:
                   !!!!!!!!!!!!!!!!!!!!!!
                   !!! Since v2.12.17 !!!
                   SysMD Notebook opens complete projects, not single .md files. To transfer files to a project,
                   one or more files must be put in a directory with the same name as the main project .md file.
                   Example: SysMD-Kickstart with a file SysMD-Kickstart.md and a folder Files with all files
                   belonging to the project. 
                      
                   Since v2.12.0 
                   Major update that adds many SysML v 2 constructs and focuses SysMD language on  
                   interactive work and integration of document & model:  
                   
                   1) NEW: A YaML header contains all keys as defined in standard. 
                      Changed: YaML header must contain all usages.
                      Changed: ''Document uses projectName' is no longer supported.
                   
                   2) Import statement not by imports triple; import Namespace::*; or **; shall be used.
                   3) All element declarations and definitions even in triples must be valid SysML v2 statements. 
                      Changed: a isA b;   -> class a isA b; 
                   4) NEW: support for end features, associations, connectors with added constraints and propagations. 
                   5) NEW: KerML Libraries (Base, KerML, ScalarValues) following standard loaded 
                      Changed: Any(thing) is now Base::Anything
                     
            """.trimMargin(),
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Start).padding(6.dp)
        )

    }
}
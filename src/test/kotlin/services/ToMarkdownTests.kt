package services

import com.github.tukcps.sysmd.services.repositories.local.ElementData
import com.github.tukcps.sysmd.services.repositories.local.toMarkdownString
import java.util.*
import kotlin.test.Test
import kotlin.test.assertTrue

class ToMarkdownTests {
    @Test
    fun testCellsToMarkdown() {
        val cells = listOf(ElementData(
            elementId = UUID.randomUUID(),
            type = "TextualRepresentation",
            language = "YAML",
            body = """
---
title:        SysMD Kickstart
subtitle:     SysMD Notebook User Interface 
author:       RPTU Kaiserslautern-Landau, Chair of Cyber-Physical Systems
logo:         Files/icon.png
---
        """.trimIndent()),
            ElementData(
                elementId = UUID.randomUUID(),
                type = "TextualRepresentation",
                language = "Markdown",
                body = """
                    # Testheading
                    
                    text
                """.trimIndent()
            ),
            ElementData(
                elementId = UUID.randomUUID(),
                type = "TextualRepresentation",
                language = "SysML",
                body = """
                    package testSysML;
                """.trimIndent()
            ),
            ElementData(
                elementId = UUID.randomUUID(),
                type = "TextualRepresentation",
                language = "KerML",
                body = """
                    package testKerML;
                """.trimIndent()
            ),
        )
        val str = toMarkdownString(cells)
        assertTrue(str.startsWith("---"))
    }
}
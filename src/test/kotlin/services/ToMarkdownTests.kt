package services

import com.github.tukcps.sysmd.model.datamodel.ElementData
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.services.repositories.local.toMarkdownString
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class ToMarkdownTests {
    @Test
    fun testCellsToMarkdown() {
        val cells = listOf(ElementData(
            elementId = Uuid.random(),
            type = ElementType.TextualRepresentation,
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
                elementId = Uuid.random(),
                type = ElementType.TextualRepresentation,
                language = "Markdown",
                body = """
                    # Testheading
                    
                    text
                """.trimIndent()
            ),
            ElementData(
                elementId = Uuid.random(),
                type = ElementType.TextualRepresentation,
                language = "SysML",
                body = """
                    package testSysML;
                """.trimIndent()
            ),
            ElementData(
                elementId = Uuid.random(),
                type = ElementType.TextualRepresentation,
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
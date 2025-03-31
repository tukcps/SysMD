package models.kerml

import com.github.tukcps.sysmd.model.kerml.NamespaceImport
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NamespaceImportTests {
    @Test
    fun testNamespaceImport() = testSession("ScalarValues") {
        loadKerML("private import Base;")
        val import = global.getOwnedElementOfType<NamespaceImport>()
        assertNotNull(import)
        assertEquals(1, import.source.size)
        assertEquals(1, import.target.size)
        assertNotNull(import.source.first().id)
        assertNotNull(import.target.first().id)
    }
}
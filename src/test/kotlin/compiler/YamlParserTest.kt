package compiler

import com.github.tukcps.sysmd.compiler.getYaml
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

class YamlParserTest {

    @Test
    fun test1() {
        val list = getYaml("""
            ---
            file: test.md 
            notInList : 
            notInList : not : really 
            ---
        """.trimIndent().lineSequence())
        assertEquals(1, list?.size)
    }

    @Test
    fun testMissingDelimiter() {
        val list = getYaml("""
            ---
            file: test.md 
            notInList : 
            notInList : not : really 
         """.trimIndent().lineSequence())
        assertNull(list)
    }

    @Test
    fun testNoDelimiter() {
        val list = getYaml("""
            file: test.md 
            notInList : 
            notInList : not : really 
         """.trimIndent().lineSequence())
        assertNull(list)
    }

    @Test
    fun testTwoKeysOk() {
        val path = this.javaClass.getResource("/yamlParserTest/testTwoKeysOK.md")?.toURI()!!
        val file = File(path)
        val list = getYaml(file)
        assertEquals(2, list?.size)
    }

    @Test
    fun testMissingDelimiterInFile() {
        val path = this.javaClass.getResource("/yamlParserTest/testMissingDelimiter.md")?.toURI()!!
        val file = File(path)
        val list = getYaml(file)
        assertNull(list)
    }
}
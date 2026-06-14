package compiler

import com.github.tukcps.sysmd.compiler.getYaml
import kotlinx.io.files.Path
import org.junit.jupiter.api.Test
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
        """.trimIndent().lines())
        assertEquals(1, list?.size)
    }

    @Test
    fun testMissingDelimiter() {
        val list = getYaml("""
            ---
            file: test.md 
            notInList : 
            notInList : not : really 
         """.trimIndent().lines())
        assertNull(list)
    }

    @Test
    fun testNoDelimiter() {
        val list = getYaml("""
            file: test.md 
            notInList : 
            notInList : not : really 
         """.trimIndent().lines())
        assertNull(list)
    }

    @Test
    fun testTwoKeysOk() {
        val path = this.javaClass.getResource("/yamlParserTest/testTwoKeysOK.md")?.toURI()!!.path
        val list = getYaml(Path(path))
        assertEquals(2, list?.size)
    }

    @Test
    fun testMissingDelimiterInFile() {
        val path = this.javaClass.getResource("/yamlParserTest/testMissingDelimiter.md")?.toURI()!!.path
        val list = getYaml(path)
        assertNull(list)
    }
}
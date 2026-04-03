package kermltests

import com.github.tukcps.sysmd.model.kerml.Association
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class LibrariesTests {

    val base: String by lazy {
        Thread.currentThread().contextClassLoader.getResourceAsStream("libraries/Base.kerml")!!
            .bufferedReader().use { it.readText() }
    }

    @Test
    fun baseTest() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> ScalarValue; datatype ScalarValue :> Base::Anything; }")
        loadKerML(base)
        assertNoIssues()
    }

    @Test
    fun scalarValuesTest() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> ScalarValue; datatype ScalarValue :> Base::Anything; }")
        loadKerML(base)
        assertNoIssues()

        val scalarValues = javaClass.getResourceAsStream("/libraries/ScalarValues.kerml")?.bufferedReader().use { it?.readText() }
        loadKerML(scalarValues!!)
        assertNoIssues()
    }

    /**
     * Checks that the package Links is parsed correctly.
     */
    @Test
    fun linksTest() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> ScalarValue; datatype ScalarValue :> Base::Anything; }")
        loadKerML(base)
        assertNoIssues()

        val scalarValues = javaClass.getResourceAsStream("/libraries/ScalarValues.kerml")!!.bufferedReader().use { it.readText() }
        loadKerML(scalarValues)
        assertNoIssues()

        val links = javaClass.getResourceAsStream("/libraries/Links.kerml")!!.bufferedReader().use { it.readText() }
        loadKerML(links)
        val link = global.resolve("Links::Link")?.memberElement as Association?
        val binaryLink = global.resolve("Links::BinaryLink")?.memberElement as Association?
        assertNotNull(link)
        assertNotNull(binaryLink)
        assertNoIssues()
    }

    /**
     * Checks that the package Links is parsed, serialized, and deserialized correctly
     */
    @Test
    fun linksTestWithRepository() = testSession("Links") {
        val link = global.resolve("Links::Link")?.memberElement as Association?
        val binaryLink = global.resolve("Links::BinaryLink")?.memberElement as Association?
        assertNotNull(link)
        assertNotNull(binaryLink)
        assertNoIssues()
    }

    @Test
    fun rangesTest() = testSession("ScalarValues") {
        loadKerML(base)
        assertNoIssues()

        val scalarValues = javaClass.getResourceAsStream("/libraries/ScalarValues.kerml")!!.bufferedReader().use { it.readText() }
        loadKerML(scalarValues)
        assertNoIssues()

        val ranges = javaClass.getResourceAsStream("/libraries/Ranges.kerml")!!.bufferedReader().use { it.readText() }
        settings.initialize=false
        loadKerML(ranges)
        assertNoIssues()
    }
}
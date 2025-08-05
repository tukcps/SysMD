package kermltests

import com.github.tukcps.sysmd.model.kerml.Association
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LibrariesTests {

    val base: String by lazy {
        Thread.currentThread().contextClassLoader.getResourceAsStream("libraries/Base.kerml")!!
            .bufferedReader().use { it.readText() }
    }

    @Test
    fun baseTest() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> Base::Anything; }")
        loadKerML(base)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun scalarValuesTest() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> Base::Anything; }")
        loadKerML(base)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val scalarValues = javaClass.getResourceAsStream("/libraries/ScalarValues.kerml")?.bufferedReader().use { it?.readText() }
        loadKerML(scalarValues!!)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Checks that the package Links is parsed correctly.
     */
    @Test
    fun linksTest() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> Base::Anything; }")
        loadKerML(base)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val scalarValues = javaClass.getResourceAsStream("/libraries/ScalarValues.kerml")!!.bufferedReader().use { it.readText() }
        loadKerML(scalarValues)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val links = javaClass.getResourceAsStream("/libraries/Links.kerml")!!.bufferedReader().use { it.readText() }
        loadKerML(links)
        val link = global.resolve<Association>("Links::Link")
        val binaryLink = global.resolve<Association>("Links::BinaryLink")
        assertNotNull(link)
        assertNotNull(binaryLink)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Checks that the package Links is parsed, serialized, and deserialized correctly
     */
    @Test
    fun linksTestWithRepository() = testSession("Links") {
        val link = global.resolve<Association>("Links::Link")
        val binaryLink = global.resolve<Association>("Links::BinaryLink")
        assertNotNull(link)
        assertNotNull(binaryLink)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun rangesTest() = testSession {
        loadKerML("package ScalarValues { datatype Natural :> Base::Anything; }")
        loadKerML(base)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val scalarValues = javaClass.getResourceAsStream("/libraries/ScalarValues.kerml")!!.bufferedReader().use { it.readText() }
        loadKerML(scalarValues)
        assertTrue(status.issues.isEmpty(), status.issues.toString())

        val ranges = javaClass.getResourceAsStream("/libraries/Ranges.kerml")!!.bufferedReader().use { it.readText() }
        settings.initialize=false
        loadKerML(ranges)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}
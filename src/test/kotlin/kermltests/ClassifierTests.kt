package kermltests

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ClassifierTests {
    @Test
    fun declarationTest() = testSession("Occurrences") {
        loadKerML("""
            abstract classifier a; 
            classifier b :> a; 
            classifier c :> a, b; 
        """)
        checkOwnership()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val a = global.resolve<Classifier>("a")
        assertTrue(a?.isAbstract == true)
        val c = global.resolve<Classifier>("c")
        assertNotNull(c)
        assertTrue(c.specializes(global.resolve<Type>("b")))
        assertTrue(c.specializes(global.resolve<Type>("a")))
    }
}
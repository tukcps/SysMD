package test.sysmdtests

import com.github.tukcps.sysmd.model.kerml.Classifier
import com.github.tukcps.sysmd.model.kerml.getOwnedElement
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InstancesTest {

    /**
     * instanceOf creates clone via instantiate.
     * All properties and relations are copied.
     */
    @Test
    fun instantiateTest1() = testSession {
        loadSysMD("""                
            // Physical de-composition and technical architecture.  
            class X;  
            X hasA 
                part x: [1 .. 2] Base::Anything = Base::Anything, Base::Anything.// Subtype of Gbo::Component
        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.getOwnedElement("X")
        assertNotNull(x)
        assertTrue(x is Classifier)
    }
}

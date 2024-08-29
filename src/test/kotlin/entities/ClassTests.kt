package test.entities

import com.github.tukcps.sysmd.model.kerml.implementation.ClassImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ClassTests {

    @Test
    fun getSubclassesTest() = testSession {
        val a = create(ClassImplementation(declaredName = "a"), global)
        create(SpecializationImplementation(a, "Any"), a)
        val b = create(ClassImplementation(declaredName = "b"), global)
        create(SpecializationImplementation(b, "a"), b)
        val c = create(ClassImplementation(declaredName = "c"), global)
        create(SpecializationImplementation(c, "b"), c)
        initialize()
        val asub = a.subclasses()
        Assertions.assertEquals("b", asub.first().escapedName())
    }
}
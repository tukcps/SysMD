package entities

import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.initialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TypeTests {

    @Test fun specializesTest() = testSession {
        val a = create(TypeImplementation(declaredName="a"), global)
        create(SpecializationImplementation(a, any), a)
        val b = create(TypeImplementation(declaredName="b"), global)
        create(SpecializationImplementation(b, a), b)
        val c = create(TypeImplementation(declaredName="c"), global)
        create(SpecializationImplementation(c, b), c)
        initialize()
        assertEquals(true, c.specializes(b))
        assertEquals(false, a.specializes(b))
    }

    @Test fun isSubtypeTest() = testSession {
        val a = create(TypeImplementation(declaredName="a"), global)
        create(SpecializationImplementation(a, any), a)
        val b = create(TypeImplementation(declaredName="b"), global)
        create(SpecializationImplementation(b, a), b)
        val c = create(TypeImplementation(declaredName="c"), global)
        create(SpecializationImplementation(c, b), c)
        initialize()
        assertEquals(false, b.specializes(c))
        assertEquals(true, b.specializes(a))
    }

    @Test fun getSubclassesTest() = testSession {
        val a = create(TypeImplementation(declaredName="a"), global)
        create(SpecializationImplementation(a, any), a)
        val b = create(TypeImplementation(declaredName="b"), global)
        create(SpecializationImplementation(b, a), b)
        val c = create(TypeImplementation(declaredName="c"), global)
        create(SpecializationImplementation(c, b), c)
        initialize()
        val aSubtype = a.subtypes()
        assertEquals("b", aSubtype.first().declaredName)
    }
}
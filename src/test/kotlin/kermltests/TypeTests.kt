package kermltests

import com.github.tukcps.sysmd.model.kerml.implementation.TypeImplementation
import com.github.tukcps.sysmd.model.kerml.Specialization
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.getOwnedElementOfType
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypeTests {

    @Test
    fun testType() = testSession {
        loadKerML("""
           type a :> Base::Anything;  
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Type>("a")
        assertNotNull(a)
        assertTrue(a.getOwnedElementOfType<Specialization>()?.general?.ref == anything)
    }

    @Test
    fun testTwoSupertypes() = testSession("ScalarValues") {
        loadKerML("""
            type A :> Base::Anything { feature a; }
            type B :> Base::Anything { feature b; }
            type AB :> A, B; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val ab = global.resolve<TypeImplementation>("AB")
        assertNotNull(ab)
        assertEquals(2, ab.allSupertypes().size)
    }

    @Test
    fun testVisibility() = testSession("ScalarValues") {
        loadKerML("""
            package p {
                private type t :> Base::Anything; 
            }
            
            type A :> p::t; 
        """)
        val pt = global.resolve<Type>("p::t")
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
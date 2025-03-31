package kermltests

import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class SpecializationTests {

    @Test fun specializationTest1() = testSession {
        loadKerML("""
            type t1 :> Base::Anything; 
            type t2 :> Base::Anything;
            specialization a subtype t1 specializes t2; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test fun specializationTest2() = testSession {
        loadKerML("""
            type t1 :> Base::Anything; 
            type t2 :> Base::Anything;
            subtype t1 specializes t2; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
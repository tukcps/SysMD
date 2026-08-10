package compiler.kerml.root

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class SpecializationTests {

    @Test
    fun specializationTest1() = testSession("ScalarValues") {
        loadKerML("""
            type t1 :> Base::Anything; 
            type t2 :> Base::Anything;
            specialization a subtype t1 specializes t2; 
        """, Runlevel.MODEL)
        assertNoIssues()
        val a = global.resolve("a")
        // assertNotNull(a)
    }

    @Test
    fun specializationTest2() = testSession("ScalarValues") {
        loadKerML("""
            type t1 :> Base::Anything; 
            type t2 :> Base::Anything;
            subtype t3 specializes t2; 
        """, Runlevel.MODEL)
        assertNoIssues()
        val a = global.resolve("t3")?.member<Element>()
        // assertNotNull(a)
    }
}
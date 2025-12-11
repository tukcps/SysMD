package kermltests

import com.github.tukcps.sysmd.model.kerml.Type
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class SubsettingTests {
    @Test
    fun subsettingTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a; 
            feature b; 
            feature Type; 
  			derived feature f : Type[0..1] subsets a, b;
        """)
        assertNoIssues()
        val f = global.resolve("f")?.memberElement
        solver.propagate()
        assertNoIssues()
    }

    @Test
    fun subsettingTestInherited() = testSession("ScalarValues") {
        loadKerML("""
            feature a; 
            feature b; 
            feature Type; 
            type t :> Base::Anything {
  			    derived feature f : Type[0..1] subsets a, b;
            }
            type t2 :> t; 
        """)
        assertNoIssues()
        val t2 = global.resolve("t2")?.member<Type>()
        solver.propagate()
        assertNoIssues()
    }

}
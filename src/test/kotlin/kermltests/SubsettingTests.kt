package kermltests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertTrue

class SubsettingTests {
    @Test
    fun subsettingTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a; 
            feature b; 
            feature Type; 
  			derived feature f : Type[0..1] subsets a, b;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val f = global.resolve<Feature>("f")
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
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
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val t2 = global.resolve<Type>("t2")
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

}
package models.expression

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import com.github.tukcps.sysmd.services.repositories.local.toElement
import com.github.tukcps.sysmd.services.resolve.resolve
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VariableTests {

    @Test
    fun testSerialization1() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature f: Ranges::RealInRange [m] = 2.0 m {:>> range= "2..3" ;}
            // serialized in body-field: 
            // 2..3 $$ m $$ 1.0 m;
        """)
        val f = global.resolve<Feature>("f")
        val fdao = f!!.toDAO()
        assertEquals("2..3 ## m ## 2.0 m", fdao.body?.trim() )
        val f2 = fdao.toElement().also { it.model = this  }
        assertTrue(f2 is Feature)
        assertEquals("2.0 m", f2.expression?.trim())
        assertEquals("m", f2.unitConstraint?.trim())
        assertEquals(f.typeConstraint.first().trim(), f2.typeConstraint.first().trim())
        assertEquals(f.unitConstraint?.trim(), f2.unitConstraint?.trim())
    }


    @Test
    fun testSerialization2() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                feature f: Ranges::RealInRange = 2.0 {:>> range= "2..3";}
                // serialized in body-field: 
                // 2..3 $$ $$ 1.0 m;
            """)
        val f = global.resolve<Feature>("f")
        val fdao = f!!.toDAO()
        assertEquals("2..3 ##  ## 2.0", fdao.body?.trim() )
        val f2 = fdao.toElement().also { it.model = this  }
        assertTrue(f2 is Feature)
        assertEquals("2.0", f2.expression?.trim())
        assertEquals("", f2.unitConstraint?.trim())
        assertEquals(f.typeConstraint.first().trim(), f2.typeConstraint.first().trim())
        assertEquals(f.unitConstraint?:"".trim(), f2.unitConstraint?.trim())
    }
}
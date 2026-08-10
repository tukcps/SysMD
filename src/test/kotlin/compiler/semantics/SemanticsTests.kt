package compiler.semantics

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.semantics.ActionsContext
import com.github.tukcps.sysmd.compiler.semantics.kerml.ElementAction
import com.github.tukcps.sysmd.model.generated.ElementType
import com.github.tukcps.sysmd.services.Runlevel
import util.assertIssue
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test

class SemanticsTests {
    @Test
    fun testPathAddOwnedRelationship() {
        val compiler = KerML()
        val actions = ActionsContext(compiler)

        val first = ElementAction(actions, ElementType.Namespace)
        first.element.declaredName = "first"
        val second = ElementAction(actions, ElementType.Namespace)
        second.element.declaredName = "second"

//         val path = actions.getPath()
//        assertEquals("first::second", "") // TODO
    }


    /**
     * Issue: Both get same UUID, which is wrong.
     * TODO: Index-based UUID needed for double names, otherwise import overwrites 1st element.
     * And there is no duplicate anymore ...
     */
    @Test
    fun noDoubleName() = testSession {
        settings.reportDoubleNames = true
        loadKerML("""
            namespace    c2;
            doc c2 /* c2 duplicate */ 
        """, Runlevel.MODEL)
        assertIssue("Duplicate")
    }
}
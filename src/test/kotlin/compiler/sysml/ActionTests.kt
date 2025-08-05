package compiler.sysml

import com.github.tukcps.sysmd.model.sysml.ActionDefinition
import com.github.tukcps.sysmd.model.sysml.ActionUsage
import com.github.tukcps.sysmd.services.resolve.resolve
import util.assertNoIssues
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull

class ActionTests {
    @Test
    fun basicUsageTest() = testSession("Actions") {
        loadSysMLv2("""
            action a; 
        """)
        assertNoIssues()
        val a = global.resolve<ActionUsage>("a")
        assertNotNull(a)
    }

    @Test
    fun basicDefinitionTest() = testSession("Actions") {
        loadSysMLv2("""
            action def a;  
        """)
        assertNoIssues()
        val a = global.resolve<ActionDefinition>("a")
        assertNotNull(a)
    }
}
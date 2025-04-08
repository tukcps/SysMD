package parsertests

import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionImplementation
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Generic tests of the parser(s), without semantic actions.
 */
class ParserTests {

    private fun getParser(): SysMLv2 {
        val model = SessionImplementation()
        return SysMLv2(model = model)
    }


    @Test
    fun commentTest() {
        val parser = getParser()
        parser.input = """
            /* Test 1 */
            package p; 
            /*123*/
        """.trimIndent()
        parser.parse()
    }


    @Test
    fun syntaxTest() = getParser().run {
        input = """
            package p {
                //*123*/
                part def Vehicle {
                    part eng : Component;
                    attribute weight: Real = 5.0 + 3.0 + 5.0; 
                    part wheels : Component; 
                }
                part car : Vehicle;  
            }
            part def car :> p::Vehicle; 
        """
        semantics.initOwners("Global")
        parse()
        assertTrue(model.status.issues.isEmpty(), model.status.issues.toString())
    }

    @Test
    fun syntaxTestMultiplicity() = testSession("Parts") {
        loadSysMLv2("""
             package tree {          
                part def trunk;  
                part branch [1 .. 8];
             }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val tree = global.resolve<Namespace>("tree")!!
        assertNotNull(tree)
        val branch = tree.resolve<Element>("branch")
        assertNotNull(branch)
    }
}
package sysmdtests

import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.model.kerml.implementation.TextualRepresentationImplementation
import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionImplementation
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SysMLTests {

    private fun getParser(): KerML {
        val model = SessionImplementation()
        val textualRepresentation = model.create(TextualRepresentationImplementation(language = "SysMD", body = ""), model.global)
        return KerML(model = model, textualRepresentation = textualRepresentation)
    }


    @Test
    fun packageTest() = testSession {
        +"""
            package p; 
        """
        assertNotNull(global.resolve("p"))
    }

    @Test
    fun partTest() = testSession {
        +"""
            part def p; 
        """
        assertNotNull(global.resolve("Global::p"))
    }

    @Test
    fun commentTest() {
        val parser = getParser()
        parser.input = """
            /* Test 1 */
            package p; 
            /*123*/
        """.trimIndent()
        parser.parseSysMD()
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
        parseSysMD()
        assertTrue(model.status.exceptions.isEmpty(), model.status.exceptions.toString())
    }

    @Test
    fun syntaxTestMultiplicity() = testSession("Parts") {
        loadSysMD("""
             // SysML V2                -- SysMD triple                       -- KerML Class generated
             package tree {             // Global hasA Package tree.          -- Package
                part def trunk;         // tree defines trunk isA Anything.   -- Class w/ Specialization
                part branch [1 .. 8];   // tree hasA branch: [1..8] Anything. -- Feature w/ Multiplicity & FeatureTyping. 
             }
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val tree = global.resolve<Namespace>("tree")!!
        assertNotNull(tree)
        val branch = tree.resolve<Element>("branch")
        assertNotNull(branch)
    }
}
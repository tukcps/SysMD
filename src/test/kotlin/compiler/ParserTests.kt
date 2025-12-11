package compiler

import com.github.tukcps.sysmd.compiler.KerML
import com.github.tukcps.sysmd.compiler.SysMLv2
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.Package
import com.github.tukcps.sysmd.services.session.SessionImplementation
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertTimeoutPreemptively
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * Generic tests of the parser(s), without semantic actions.
 */
class ParserTests {

    private fun getParser(): SysMLv2 {
        val model = SessionImplementation(libraries = mutableListOf())
        return SysMLv2(model = model)
    }

    /** Short name is given in <> */
    @Test
    fun shortNameTest12() = testSession(initialize = false) {
        loadKerML("""class < abc >; """)
        assertNoIssues()
    }

    /** Short name is given in <> */
    @Test
    fun shortNameTest21() = testSession(initialize = false) {
        loadKerML("class < shortName > longName;")
        val abc = global.ownedElement.firstOrNull { it is Class }
        assertEquals("shortName", abc!!.shortName)
        assertEquals("longName", abc.name)
        assertNoIssues()
    }

    @Test
    fun commentTest() = getParser().run {
        input = """
            /* Test 1 */
            package p; 
            /*123*/
        """
        parse()
        model.assertNoIssues()
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
        semantics.initOwningNamespaces()
        parse()
        model.assertNoIssues()
    }

    @Test
    fun syntaxTestMultiplicity(): Unit = getParser().run {
        input = """
             package tree {          
                part def trunk;  
                part branch [1 .. 8];
             }
        """
        semantics.initOwningNamespaces()
        parse()
        model.assertNoIssues()
    }

    @Test
    fun testExprKeyword() : Unit = assertTimeoutPreemptively(5.seconds.toJavaDuration()) {
        KerML(model = SessionImplementation(libraries = mutableListOf())).run {
            input = """
                package p {
                    expr x : ScalarValues::Boolean = a and b;
                    // examples from standard:
                    expr : Dynamics {
                        in initialState;
                        in time;
                        return result : VehicleState =
                        vehicleComputation(initialState, time);
                    }
                    expr vehicleComputation subsets computation {
                        return : VehicleState;
                    }
                    expr computation : ComputeDynamics {
                        in state;
                        in dt;
                        return result;
                    }
                }
            """
            semantics.initOwningNamespaces()
            assertTimeoutPreemptively(5.seconds.toJavaDuration()) {
                parse()
            }
            model.assertNoIssues()
        }
    }

	@Test
	fun testAbstract() : Unit = KerML(SessionImplementation()).run {
		input = """
			private import ScalarValues::Real;
			package p {
				abstract function foo { in x : Real; return : Real }
				abstract feature bar {
					feature x : Real;
					feature y : Real;
				}
			}
		"""
		semantics.initOwningNamespaces()
		parse()
		model.assertNoIssues()

		val p = assertNotNull(model.global.resolve("p")?.member<Package>())

		assertNotNull(p.resolve("bar")?.member<Feature>()).apply {
			assertTrue(isAbstract)
			val x = assertNotNull(resolve("x")?.member<Feature>())
			val y = assertNotNull(resolve("y")?.member<Feature>())

			assertFalse(x.isAbstract)
			assertFalse(y.isAbstract)
		}

		assertNotNull(p.resolve("foo")?.member<Function>()).apply {
			assertTrue(isAbstract)
			val args = ownedElement.filterIsInstance<Feature>()
			// println(args)

			assertEquals(2, args.size)
			args.forEach { assertFalse(it.isAbstract) }
		}
	}
}
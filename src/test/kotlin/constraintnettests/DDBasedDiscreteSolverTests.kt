@file:Suppress("UNUSED_VARIABLE", "unused")

package constraintnettests

import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.BDD
import io.github.tukcps.aadd.values.Range
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession

class DDBasedDiscreteSolverTests {

    @Test
    fun booleanBySubclasses() = testSession("ScalarValues") {
        loadKerML("""
            type General :> Base::Anything {
                feature p: ScalarValues::Boolean = bySpecializations(p);
                feature q: ScalarValues::Boolean = bySpecializations(q);
            }
            type Variant1 :> General {
                inv p; 
                inv q; 
            }
            type Variant2 :> General {
                inv p; 
                inv q false; 
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        solver.propagate()
        val v1p = global.resolveVar("Variant1::p")
        val v2p = global.resolveVar("Variant2::p")
        assertEquals(0, status.issues.size, "Errors: ${status.issues}")
        val p = global.resolveVar("General::p")!!
        val q = global.resolveVar("General::q")!!
        assertEquals(builder.True, p.vectorQuantity.value)
        //assertNotEquals(builder.Bool, q.vectorQuantity.value) //FIXME: Do we want internal with ITE(x, t, f) or unknown leaf?
        assertEquals(builder.Bool, q.vectorQuantity.value)
        assertNotEquals(builder.True, q.vectorQuantity.value)
        assertNotEquals(builder.False, q.vectorQuantity.value)
        assertEquals(0, q.vectorQuantity.value.height())
    }

    @Test
    fun booleanBySubclassesWithBoolSpec() = testSession("ScalarValues") {
        loadKerML(input = """
           package Reason {
               type General  :> Base::Anything {
                   inv p { bySpecializations(p) }
                   feature q: ScalarValues::Boolean = bySpecializations(q);
               }
               type Variant1 :> General {
                   inv p { true }
                   feature q: ScalarValues::Boolean = true;
               }
               type Variant2 :> General {
                   inv p { true }
                   feature q: ScalarValues::Boolean = false;
               }
           }
        """)
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}")
        initialize()
        solver.propagate()
        //FIXME: Exception even before assertions...
        assertTrue(status.issues.isEmpty(), "Error messages: ${status.issues}"
        ) //checking for no errors should be enough here: 'bySubclasses' will throw during initialization if something is wrong
    }

    @Test
    fun hasATest2() = testSession("ScalarValues") {
        loadKerML("""
            inv a; 
            inv b  { not owns(Global, a) }
        """)
        solver.propagate()
        assertNoIssues()
        val b = global.resolveVar("b")!!
        assertEquals("Contradiction", b.vectorQuantity.value.toString()
        ) //FIXME: want contradiction? or exception during evaluation?
    }

    /**
     * Check that misuse of a type as a variable is detected and reported.
     */
    @Test
    fun isATest2() = testSession("ScalarValues") {
        loadKerML("""
            type c2 :> Base::Anything;
            feature c2 : ScalarValues::Boolean;
        """)
        assertEquals(1, status.issues.size, status.issues.toString())
    }

    @Test
    fun requirementTestGt() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real =[1.0..3.0].
            inv r { x > 1.0+1.0 }
        """.trimIndent())
        assertEquals(0, status.issues.size, status.issues.toString())
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val x = global.resolveVar("x")!!
        // println(x)
        assertTrue(x.vectorQuantity.getMinAsDouble() <= 2.0)
    }

    /**
     * A requirement x >= 2.0 is tested.
     * The Minimum must be constrained to something less or equal 2.0.
     */
    @Test
    fun requirementTestGe() = testSession("ScalarValues") {
        loadKerML(catchExceptions = false, input = """
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x >= 1.0+1.0}
        """.trimIndent()
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        initialize()
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val x = global.resolveVar("x")!!
        // println(x.quantity.getMinAsDouble())
        assertTrue(x.vectorQuantity.getMinAsDouble() <= 2.0)
    }

    @Test
    fun requirementTestLt() = testSession("ScalarValues") {
        loadKerML(catchExceptions = false, input = """
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x < 1.0+1.0 }
        """.trimIndent()
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val x = global.resolveVar("x")!!
        // println(x.quantity.getMaxAsDouble())
        assertTrue(x.vectorQuantity.getMaxAsDouble() >= 2.0)
    }


    @Test
    fun requirementTestLe() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x <= 1.0+1.0 }
        """.trimIndent()
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val x = global.resolveVar("x")!!
        // println(x.quantity.getMaxAsDouble())
        assertTrue(x.vectorQuantity.getMaxAsDouble() >= 2.0)
    }

    @Test
    fun contradictionTest() = testSession("Ranges") {
        loadKerML(
            input = """
            feature x: Ranges::RealInRange {:>> range = "1.0..3.0";}
            feature y: ScalarValues::Real = x+0.1;
            inv r { x == y}
        """)
        assertEquals(0, status.issues.size, status.issues.toString())
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val x = global.resolveVar("x")!!
        val y = global.resolveVar("y")!!
        val r = global.resolveVar("r")!!
        assertTrue(x.vectorQuantity.aadd().isEmpty())
//         assertTrue(y.vectorQuantity.aadd().isEmpty())
        assertEquals("Infeasible", r.valueStr)
    }

    @Test
    fun contradictionTest2() = testSession("Ranges") {
        loadKerML(input = """
            feature x: Ranges::RealInRange {:>> range = "1.0..3.0";}
            feature y: ScalarValues::Real = x+0.1;
            inv r { x >= y }
        """)
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        val x = global.resolveVar("x")!!
        val y = global.resolveVar("y")!!
        val r = global.resolveVar("r")!!
        assertTrue(x.vectorQuantity.aadd().isEmpty())
        // assertTrue(y.vectorQuantity.aadd().isEmpty())
        assertEquals("Infeasible", r.valueStr)
        // Contradiction? Depends on order which solver reports issue?
    }

    @Test
    fun basicPropagation() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Boolean;
            inv b false; 
            inv c;
            feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
            inv z; 
        """)
        solver.propagate()
        assertNoIssues()
    }

    @Test
    fun basicPropagation2original() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv b false; 
            inv c;
            inv d { (a and c) or (not(b) and not(a)) }
        """)
        solver.propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        assertNotNull(a)
        assertNotNull(b)
        //println(y!!.quantity.bdd().toIteString())
        //println(y!!.quantity.bdd().evaluate().toIteString())
        // println(builder.conds.indexes.toString())
        assertNoIssues()

        //(this as AgilaServiceImpl).dSolver.propagateByPath(this.getProperties())

        //assertEquals(builder.TRUE, y!!.quantity.bdd())
        //assertEquals(builder.TRUE, a!!.quantity.bdd()) //FIXME: Not working at the moment
    }

    @Test
    fun discContInterfaceTest1() = testSession("ScalarValues") {
        loadKerML(
            catchExceptions = false, input = """
            feature x: ScalarValues::Real = [1.0 .. 3.0];
            feature y: ScalarValues::Real = [1.0 .. 4.0];
            feature a: ScalarValues::Boolean = x >= y {:>> spec = "true";}
        """.trimIndent()
        )
        initialize()
        //TODO: The actual test^^
    }

    @Test
    fun contDiscInterfaceTest1()  // 'z' and 'a' are in conflict!
            = testSession("ScalarValues") {
        loadKerML(
            catchExceptions = false, input = """
            feature x: ScalarValues::Real = [1.0 .. 3.0];
            feature y: ScalarValues::Real = [1.0 .. 4.0];
            feature a: ScalarValues::Boolean = x >= y {:>> spec = "true";}
            feature z: ScalarValues::Real = ITE(a, 3.0, 2.0) {:>> range = "1.0 .. 2.0";}
        """.trimIndent()
        )
        initialize()
        //TODO: The actual test^^
    }

    @Test
    fun discContCyclicDependencyTest()  //not yet finished!
            = testSession("ScalarValues") {
        loadKerML(
            catchExceptions = false, input = """
        feature x: ScalarValues::Real = [1.0..3.0];
        feature y: ScalarValues::Real = [1.0..4.0];
        feature a: ScalarValues::Boolean = x >= y {:>> spec = "true";}
        feature z: ScalarValues::Real = ITE(a, 3.0, 2.0) {:>> range = "2.0..3.0";}
        feature f: ScalarValues::Boolean = z >= y;
        feature g: ScalarValues::Real = ITE(f, 1.5, 1.0);
        """.trimIndent()
        ).run {
            initialize()
            solver.propagate()
            //println(getProperties().toString())
            //this.letVar(UId("f"), builder.scalar(1.0)) //FIXME: Probably breaks...
            //TODO: The actual test^^
        }
    }

    @Test
    fun complexBDDdownTest() = testSession("ScalarValues") {
        loadKerML("""
        feature a: ScalarValues::Boolean;
        feature b: ScalarValues::Boolean {:>> spec = "false";}
        feature c: ScalarValues::Boolean {:>> spec = "true";}
        feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a)) {:>> spec = "true";}
        """.trimIndent())
        // 1) y auf true setzen
        // 2) impact auf a, b, c checken
        // 3) y auf False setzen
        // 4) impact auf a, b, c checken
        // (was passiert wenn es keine Variablenbelegung gibt, die passt?
        // val solver = CspSolver(this)
        //val disc = solver.discSolver
        // val props = global.getOwnedElementsOfType<Expression>().toMutableSet()
        //disc.solve(props)

        // println("break")
    }

    @Test
    fun tautologyTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv b false; 
            inv c; 
            inv y { (a and c) or (not(b) and not(a)) }
            inv z false { a and c }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        solver.propagate()
        val y = global.resolveVar("z")
        assertNotNull(y)
        assertTrue(y!!.vectorQuantity.value == builder.False)
        //disc.solve(props)
        // println("break")
    }

    @Test
    fun exampleTest() = testSession("ScalarValues") {
        loadKerML("""
                inv a { true }
                feature b: ScalarValues::Boolean;
                inv c { a and b }
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        //val disc = solver.discSolver
        solver.propagate()
        val a = global.resolveVar("a")
        assertEquals(builder.True, a!!.vectorQuantity.bdd())
        val b = global.resolveVar("b")
        assertEquals(builder.True, b!!.vectorQuantity.bdd())
        val c = global.resolveVar("c")
        assertEquals(builder.True, c!!.vectorQuantity.bdd())
    }

    @Test
    fun exampleTest2() = testSession("ScalarValues") {
        loadKerML("""
            inv a { true }
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean = a and b;
        """)
        solver.propagate()
        val a = global.resolveVar("a")
        assertEquals(builder.True, a!!.vectorQuantity.bdd())
        val b = global.resolveVar("b")
        assertTrue(b!!.vectorQuantity.bdd() === b.vectorQuantity.value.builder.Bool)
        val c = global.resolveVar("c")
        assertTrue(c!!.vectorQuantity.bdd() === c.vectorQuantity.value.builder.Bool)
    }

    //WIP: 10 Tests!
    @Test
    fun logicOperatorsAndTest() = testSession("ScalarValues") {
        loadKerML("""
            inv a1; 
            feature b1: ScalarValues::Boolean;
            inv c1 { a1 and b1 }
            feature a2: ScalarValues::Boolean;
            feature b2: ScalarValues::Boolean;
            feature c2: ScalarValues::Boolean = a2 and b2;
            feature a3: ScalarValues::Boolean;
            feature b3: ScalarValues::Boolean;
            inv c3 { a3 and b3 }
        """)
        solver.propagate()
        val a1 = global.resolveVar("a1")
        assertEquals(builder.True, a1!!.vectorQuantity.bdd())
        val a2 = global.resolveVar("a2")
        //assertEquals(builder.BOOL, a2!!.quantity.bdd())
        assertTrue(a2!!.valueStr.startsWith("Unknown"))

        val b1 = global.resolveVar("b1")
        // println(b1.toString())
        assertEquals(builder.True, b1!!.vectorQuantity.bdd())
        val b2 = global.resolveVar("b2")
        //assertEquals(builder.BOOL, b2!!.quantity.bdd())
        assertTrue(b2!!.valueStr.startsWith("Unknown"))

        val c1 = global.resolveVar("c1")
        val c2 = global.resolveVar("c2")
        assertNotNull(c1)
        assertNotNull(c2)

        val c3 = global.resolveVar("c3")
        val b3 = global.resolveVar("c3")
        val a3 = global.resolveVar("c3")

        assertEquals(builder.True, c3!!.vectorQuantity.bdd(), "c3")
        assertEquals(builder.True, b3!!.vectorQuantity.bdd(), "b3")
        assertEquals(builder.True, a3!!.vectorQuantity.bdd(), "a3")
    }

    @Test
    fun logicOperatorsInitializationTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            inv c { a and b }
        """)
        solver.propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")
        assertEquals(builder.True, a!!.vectorQuantity.value, "a should be true")
        assertEquals(builder.True, b!!.vectorQuantity.value, "b should be true")
        assertEquals(builder.True, c!!.vectorQuantity.value, "c (= a and b) should be true")
    }

    @Test
    fun logicOperatorsOrTest() = testSession("ScalarValues") {
        loadKerML("""
            inv a2 false; 
            feature b2: ScalarValues::Boolean;
            inv c2 {a2 or b2 }
            feature a3: ScalarValues::Boolean;
            feature b3: ScalarValues::Boolean;
            inv c3 { a3 or b3 }
            feature a4: ScalarValues::Boolean;
            feature b4: ScalarValues::Boolean;
            feature c4: ScalarValues::Boolean = a4 or b4;
        """)
        solver.propagate()
        val a2 = global.resolveVar("a2")
        val a3 = global.resolveVar("a3")
        val a4 = global.resolveVar("a4")

        val b2 = global.resolveVar("b2")
        assertEquals(builder.True, b2!!.vectorQuantity.bdd())
        val b3 = global.resolveVar("b3")
        val b4 = global.resolveVar("b4")

        val c2 = global.resolveVar("c2")
        assertEquals(builder.True, c2!!.vectorQuantity.bdd())
        val c3 = global.resolveVar("c3")
        //assertTrue(c3!!.valueStr.startsWith("Unknown")) //Can make no assumption
        val c4 = global.resolveVar("c4")
        assertTrue(c4!!.valueStr.startsWith("Unknown"))
        //TODO!
    }

    @Test
    fun logicOperatorsNotTest() = testSession("ScalarValues") {
        loadKerML("""
            inv a1 false; 
            inv b1 { not(a1) }
            inv a2; 
            inv b2 false { not(a2) }
            feature a5: ScalarValues::Boolean;
            inv b5 { not(a5) }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        solver.propagate()
        val a1 = global.resolveVar("a1")
        val a2 = global.resolveVar("a2")
        val a5 = global.resolveVar("a5")
        assertEquals(builder.False, a5!!.vectorQuantity.bdd())

        val b1 = global.resolveVar("b1")
        assertEquals(builder.True, b1!!.vectorQuantity.bdd())
        val b2 = global.resolveVar("b2")
        assertEquals(builder.False, b2!!.vectorQuantity.bdd())
        val b5 = global.resolveVar("b5")
        assertEquals(builder.True, b5!!.vectorQuantity.bdd())
    }

    @Test
    fun logicOperatorsITETest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv b false; 
            inv c; 
            inv d { ITE(a,b,c) }
        """)
        solver.propagate()
        //TODO: hier weiter
        val a = global.resolveVar("a")
        assertEquals(builder.False, a!!.vectorQuantity.value.asBdd())
    }

    @Test
    fun inequationTest() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = oneOf(1.0 .. 3.0);
            feature y: ScalarValues::Real = oneOf(1.0 .. 4.0);
            feature b: ScalarValues::Boolean;
            feature a: ScalarValues::Boolean = (x >= y) and not(b) {:>> spec = "true";}
        """)
        solver.propagate()
        val x = global.resolveVar("x")
        val a = global.resolveVar("a")

        // val solver = solver.discreteSolver

        // println(solver.getInfeasibilityMap().toString())
        //solver.handleInequations(a!!)

        //val uMap = (this as AgilaServiceImpl).dSolver.getUnitMap()
        //println(a.quantity.value.asBdd().toIteString())
        //println(builder.conds.x.toString())
        //println((this as AgilaServiceImpl).dSolver.getInfeasibilityMap().toString())
        //assertEquals(builder.True, a!!.quantity.bdd())

        //val guards = dSolver.getGuardingProperties(a!!.uid!!)
        //println(a.ofClass)
        //println(x!!.ofClass)
        //println(guards.toString())

        // println("brk")
    }

    @Test
    fun hasATest() = testSession("ScalarValues") {
        loadKerML("""
            package Example3 {
                feature a: ScalarValues::Boolean = true;
                feature b: ScalarValues::Boolean;
                inv c { a and b }
                type d :> Base::Anything;
                type e :> Base::Anything;
                feature test: Base::Anything[0..2];
            }
            """.trimIndent()
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        solver.propagate()
        val ex3d = global.resolve("Example3::d")?.memberElement as Namespace
        val tst = ex3d.resolve("test")
        assertNotNull(tst)
        //FIXME: Removed erroneous functions that caused a stack overflow. Reformulate test case!
    }

    @Test
    fun semanticTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv b; 
            inv c false; 
            feature d: ScalarValues::Boolean = true or false;
            inv e { true or false }
            inv f false { true or false }
            feature g: ScalarValues::Boolean = true or false;
            """)
        // There is no exor function ... yet. Either we add one ...
        initialize()
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val a = global.resolveVar("a")
        assertTrue(a!!.valueStr.startsWith("Unknown"))
        val b = global.resolveVar("b")
        assertTrue(b!!.valueStr.startsWith("True"))
        val c = global.resolveVar("c")
        val d = global.resolveVar("d")
        val e = global.resolveVar("e")
        val f = global.resolveVar("f")
        val g = global.resolveVar("g")
    }

    @Test
    fun basicUnitRecognitionTest() = testSession("ScalarValues") {
        loadKerML("""
            inv a false { false }
            feature b: ScalarValues::Boolean;
            inv c { a or b }
        """)
        solver.propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")
        assertEquals(true, b!!.vectorQuantity.value is BDD.Leaf, "b not leaf")
        assertEquals(true, c!!.vectorQuantity.value is BDD.Leaf, "c not leaf")
        assertEquals(builder.True, b.vectorQuantity.bdd(), "b")
        assertEquals(builder.True, c.vectorQuantity.bdd(), "c")
    }

    @Test
    fun complexUnitRecognitionTest() {
        //TODO
    }

    @Test
    fun conflictDetectionTest() = testSession("ScalarValues") {
        loadKerML("""
            inv a; 
            inv b false;
            inv c { b } 
        """)
        solver.propagate()
        assertEquals("Contradiction", (global.resolveVar("c")!!.valueStr))
        //println(status.errors.toString())
    }

    @Test
    fun basicDontCareRecognitionTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv b false; 
            inv c; 
            inv d { (a and c) or (not(b) and not(a)) }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        solver.propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")
        val d = global.resolveVar("d")
        assertEquals(false, a!!.bdd() is BDD.Leaf)
        assertEquals(true, d!!.bdd() is BDD.Leaf)
        //assertEquals() TODO: Test for don't-care.size == 1
    }

    @Test
    fun updateDependentPropertiesTest() {
        //TODO!
    }

    @Test
    fun reactToUserChangesTest() = testSession("ScalarValues") {
        loadKerML("""
            inv arbitraryConstraint1; 
            inv arbitraryConstraint2 false; 
            feature arbitraryConstraint3: ScalarValues::Boolean;
            inv constr { 
                arbitraryConstraint1 and arbitraryConstraint2 or arbitraryConstraint3 
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        // println("reactToUserChangesTest before initialization")
        initialize()
        // println("reactToUserChangesTest before 1. propagation")
        solver.propagate()
        val constraint = global.resolveVar("constr")
        val arbitraryConstraint3 = global.resolveVar("arbitraryConstraint3")
        //1. One related constraint should be introduced: aC1 and aC2 evaluate to false => aC3 has to be true
        //FIXME: comment back in! assertEquals(1, (this as AgilaServiceImpl).dSolver.lastIntroducedProperty)
        //assertEquals(builder.True, arbitraryConstraint3!!.quantity.value)
        //println(constraint.toString())
        //println(arbitraryConstraint3!!.quantity.toString())
        //2. Now change quantity/value of the arbitrary constraints and see how it impacts introduced related properties, properties and conditions
        arbitraryConstraint3!!.feature.expression = "false"
        arbitraryConstraint3.compileExpression()
        // println("reactToUserChangesTest before 2. propagation")
        solver.propagate()
        //This scenario should result in not satisfiable Constraint.
        assertEquals("Contradiction", constraint!!.vectorQuantity.bdd().toString())
        //TODO hier weiter!
        //TODO: Now set satisfiable arbitrary constraints again
        arbitraryConstraint3.feature.expression = ""
        arbitraryConstraint3.compileExpression()
        arbitraryConstraint3.vectorQuantity.values = mutableListOf(builder.Bool)
        // println("reactToUserChangesTest before 3. propagation")
        //FIXME: arbitraryConstraint3 still "false" => set conditions not reverted correctly!
        solver.propagate()
        // println(constraint.toString())
        // println(constraint.quantity.toString())
        // println(arbitraryConstraint3.quantity.toString())
    }

    @Test
    fun complexDontCareRecognitionTest() {
        //TODO
    }

    @Test
    fun falsePositiveRecognitionTest() {
        //TODO
    }

    @Test
    fun iteTest() {
        //TODO
    }

    @Test
    fun enumTest() {
        testSession("ScalarValues") {
            loadKerML("""
                feature c1: ScalarValues::Boolean;
                feature c2: ScalarValues::Boolean;
                feature c3: ScalarValues::Boolean;
                feature c4: ScalarValues::Boolean; 
                feature enumFake: ScalarValues::Real = if c1 ? 1.0 else if c2 ? 2.0 else if c3? 3.0 else if c4? 4.0 else 7.0; 
            """.trimIndent())
            solver.propagate()
            val enum = global.resolveVar("enumFake")!!

            //println("=== CONDS ===")
            //builder.conds.indexes.forEach { println(it) }
            assertTrue( enum.vectorQuantity.value.asAadd().getRange() in Range(0.999 .. 7.001) )
        }
    }

    @Test
    fun enumTestViaFunction() {
        testSession("ScalarValues") {
            loadKerML("""
                feature enumFake: ScalarValues::Real = oneOf(1.0, 2.0, 3.0, 4.0, 7.0); 
            """.trimIndent())
            solver.propagate()
            assertTrue(status.issues.isEmpty(), status.issues.toString())
            val enum = global.resolveVar("enumFake")!!

            //println("=== CONDS ===")
            //builder.conds.indexes.forEach { println(it) }
            assertTrue(enum.vectorQuantity.value.asAadd().getRange() in Range(0.99..7.01))
        }
    }

    @Test
    fun enumConstraintTest() {
        testSession("ScalarValues") {
            loadKerML("""
                feature selection: ScalarValues::Real = oneOf(1.0, 2.0, 3.0, 4.0, 7.0);
                inv selectorConstraint { selection >= 6.0 }
            """.trimIndent())
            solver.propagate()
            //builder.conds.x.forEach { println(it) }
            val enum = global.resolveVar("selection")
            val constr = global.resolveVar("selectorConstraint")

            //assertEquals(builder.False, builder.conds.x[4])
            assertTrue(global.resolveVar("selection")!!.vectorQuantity.value.asAadd() in Range(6.99 .. 7.01) )
        }
    }

    @Test
    fun enumMoreConstraintsTest() {
        testSession("ScalarValues") {
            loadKerML("""
                feature selection: ScalarValues::Real = oneOf(1.0, 2.0, 3.0, 4.0, 7.0);
                inv enumConstraint1 { selection >= 3.0 }
                inv enumConstraint2 { selection <= 3.0 }
            """.trimIndent())
            solver.propagate()

            val enum = global.resolveVar("selection")!!

            //assertEquals(builder.False, builder.conds.x[7])
            //assertEquals(builder.False, builder.conds.x[8])
            var falseCounter = 0
            var unknownCounter = 0
            for (x in builder.conds.x) {
                if (x.value is BDD && x.value == builder.False) falseCounter++
                if (x.value is BDD && x.value == builder.Bool) unknownCounter++
            }
            assertTrue(enum.vectorQuantity.value.asAadd().getRange() in Range(2.99 .. 3.01))
            //assertEquals(2, falseCounter)
            //assertEquals(2, unknownCounter)
        }
    }

    @Test
    fun enumArithmeticTest() {
        testSession("ScalarValues") {
            loadKerML("""
                feature selection1: ScalarValues::Real = oneOf(1.0, 2.0, 3.0);
                feature selection2: ScalarValues::Real = oneOf(1.0, 2.0, 3.0);
                inv enumConstraint { (selection1 + selection2) >= 6.0 }
            """.trimIndent())
            solver.propagate()
            assertTrue(status.issues.isEmpty(), status.issues.toString())

            val enum1 = global.resolveVar("selection1")!!
            val enum2 = global.resolveVar("selection2")!!
            //assertEquals(builder.False, builder.conds.x[3])
            //assertEquals(builder.False, builder.conds.x[4])
            //assertEquals(builder.False, builder.conds.x[5])
            //assertEquals(builder.False, builder.conds.x[6])
            // for (x in builder.conds.x) {
                //if (x.value is BDD) assertEquals(builder.False, x.value)
            // }
            assertEquals("6", enum1.vectorQuantity.plus(enum2.vectorQuantity).toString())
        }
    }

    @Test
    fun enumArithmeticTest2() {
        testSession("ScalarValues") {
            loadKerML("""
                feature enum1: ScalarValues::Real = oneOf(1.0, 3.0, 5.0);
                feature enum2: ScalarValues::Real = oneOf(1.0, 4.0, 6.0);
                feature enumResult: ScalarValues::Real = enum1 + enum2;
                //feature enumConstraint: ScalarValues::Boolean = enumResult >= 5.0 {:>> spec = "true";}
                feature enumConstraint: ScalarValues::Boolean = (enum1 + enum2) >= 5.0 {:>> spec = "true";}
            """.trimIndent())
            solver.propagate()
            val enum1 = global.resolveVar("enum1")!!
            val enum2 = global.resolveVar("enum2")!!
            val result = global.resolveVar("enumResult")!!
            val constraint = global.resolveVar("enumConstraint")!!
        }
    }

    @Test
            /** Ensures that any term depending on a contradiction is a contradiction itself */
    fun testContradictionPropagation() = testSession("ScalarValues") {
        loadKerML("""
            feature contradiction : ScalarValues::Boolean(true) = false;
            feature prop1 : ScalarValues::Boolean = contradiction or true;
            feature prop1T : ScalarValues::Boolean(true) = contradiction or true;
            feature prop1F : ScalarValues::Boolean(false) = contradiction or true;
            feature prop2 : ScalarValues::Boolean = prop1 or true;
            feature prop2T : ScalarValues::Boolean(true) = prop1T or true;
            feature prop2F : ScalarValues::Boolean(false) = prop1F and false;
        """.trimIndent())
        assertEquals(0, status.issues.size, status.issues.toString())
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())

        val c = global.resolveVar("contradiction")!!
        val p1 = global.resolveVar("prop1")!!
        val p1f = global.resolveVar("prop1T")!!
        val p1t = global.resolveVar("prop1F")!!
        val p2 = global.resolveVar("prop2")!!
        val p2f = global.resolveVar("prop2T")!!
        val p2t = global.resolveVar("prop2F")!!

        assertEquals("Contradiction", c.vectorQuantity.value.toString())
        assertEquals("Contradiction", p1.vectorQuantity.value.toString())
        assertEquals("Contradiction", p1f.vectorQuantity.value.toString())
        assertEquals("Contradiction", p1t.vectorQuantity.value.toString())
        assertEquals("Contradiction", p2.vectorQuantity.value.toString())
        assertEquals("Contradiction", p2f.vectorQuantity.value.toString())
        assertEquals("Contradiction", p2t.vectorQuantity.value.toString())
    }

    @Test
            /** Ensures that a contradiction doesn't interfere with the remaining program */
    fun testIndirectContradiction() = testSession("ScalarValues") {
        loadKerML("""
            feature x : ScalarValues::Boolean.
            feature y : ScalarValues::Boolean(true) = (not x) and true;
            feature z : ScalarValues::Boolean = y and x;

            feature a : ScalarValues::Boolean(true) = x or contradiction;
            feature contradiction : ScalarValues::Boolean(true) = false;

        """.trimIndent())
        assertEquals(0, status.issues.size, status.issues.toString())
        solver.propagate()
        assertEquals(0, status.issues.size, status.issues.toString())

        val c = global.resolveVar("contradiction")!!
        val x = global.resolveVar("x")!!
        val y = global.resolveVar("y")!!
        val z = global.resolveVar("z")!!
        val a = global.resolveVar("a")!!

        assertEquals("Contradiction", c.vectorQuantity.value.toString())
        assertEquals("Contradiction", a.vectorQuantity.value.toString())
        assertEquals(builder.False, x.vectorQuantity.value)
        assertEquals(builder.True, y.vectorQuantity.value)
        assertEquals(builder.False, z.vectorQuantity.value)
    }

    private fun chain(v : String, n : Int) : List<String>
            = (1 .. n).map { "feature $v$it : ScalarValues::Boolean" + if (it > 1) " = not $v${it-1};" else "." }

    @Test
    fun chainFeasible() = testSession("ScalarValues") {
        val n = 10
        loadKerML(chain("x", n).plus("feature y : ScalarValues::Boolean(true) = x1;").joinToString("\n"))
        solver.propagate()
        assertEquals(0, status.issues.size)

        for(i in 1 .. n)
        {
            val v = global.resolveVar("x$i")!!.vectorQuantity.value

            if(i % 2 == 1)
                assertEquals(v, builder.True)
            else
                assertEquals(v, builder.False)
        }
    }

    private fun cycle(v : String, n : Int) : List<String>
    = (1 .. n).map { "feature $v$it : ScalarValues::Boolean = not $v${if (it > 1) it-1 else n};" }

    @Test
    fun oddCycleInfeasible() = testSession("ScalarValues") {
        val n = 13
        loadKerML(cycle("x", n).joinToString("\n"))
        solver.propagate()
        assertEquals(0, status.issues.size)

        for(i in 1 .. n) {
            val v = global.resolveVar("x$i")!!.vectorQuantity.value

            assertEquals("Infeasible", v.toString())
        }
    }

    @Test
    fun evenCycleFeasible() = testSession("ScalarValues") {
        val n = 4
        loadKerML(cycle("x", n).joinToString("\n"))
        solver.propagate()
        assertEquals(0, status.issues.size)

        for(i in 1 .. n) {
            val v = global.resolveVar("x$i")!!.vectorQuantity.value
            assertEquals("Unknown", v.toString())
        }

        loadKerML("feature y : ScalarValues::Boolean(true) = x1;")
        solver.propagate()

        for(i in 1 .. n) {
            val v = global.resolveVar("x$i")!!.vectorQuantity.value
            assertEquals(if(i % 2 == 1) v.builder.True else v.builder.False, v)
        }
    }
}

@file:Suppress("UNUSED_VARIABLE", "unused")

package constraintnettests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.services.Runlevel
import io.github.tukcps.aadd.dd.BDD
import util.*
import util.mockup.loadKerML
import kotlin.test.*

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
                inv false q; 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val v1p = solver.getVariable("Variant1::p")
        val v2p = solver.getVariable("Variant2::p")
        assertNoIssues()
        val p = solver.variable("General::p")
        val q = solver.variable("General::q")
        assertEquals(builder.Bool.True, p.vectorQuantity.value)
        //assertNotEquals(builder.Bool, q.vectorQuantity.value) //FIXME: Do we want internal with ITE(x, t, f) or unknown leaf?
        assertEquals(builder.Bool.All, q.vectorQuantity.value)
        assertNotEquals(builder.Bool.True, q.vectorQuantity.value)
        assertNotEquals(builder.Bool.False, q.vectorQuantity.value)
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
        """, Runlevel.ALL)
        assertNoIssues()
        //checking for no errors should be enough here: 'bySubclasses' will throw during initialization if something is wrong
    }

    @Test
    fun hasATest2() = testSession("ScalarValues") {
        loadKerML("""
            inv a; 
            inv b  { not owns(Global, a) }
        """, Runlevel.ALL)
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }
        val b = solver.variable("b")
        assertEquals("Contradiction", b.vectorQuantity.value.toString()
        ) //FIXME: want contradiction? or exception during evaluation?
    }

    @Test
    fun requirementTestGt() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real =[1.0..3.0].
            inv r { x > 1.0+1.0 }
        """, Runlevel.ALL)
        assertNoIssues()
        val x = solver.variable("x")
        // println(x)
        assertBounds(2.0..3.0, x)
    }

    /**
     * A requirement x >= 2.0 is tested.
     * The Minimum must be constrained to something less or equal 2.0.
     */
    @Test
    fun requirementTestGe() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x >= 1.0+1.0}
        """, Runlevel.ALL)
        assertNoIssues()
        val x = solver.variable("x")
        assertBounds(2.0..3.0, x)
    }

    @Test
    fun requirementTestLt() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x < 1.0+1.0 }
        """, Runlevel.ALL
        )
        assertNoIssues()
        val x = solver.variable("x")
        // println(x.quantity.getMaxAsDouble())
        assertBounds(1.0..2.0, x)
    }


    @Test
    fun requirementTestLe() = testSession("ScalarValues") {
        loadKerML(
            input = """
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x <= 1.0+1.0 }
        """, Runlevel.ALL)
        assertNoIssues()
        val x = solver.variable("x")
        // println(x.quantity.getMaxAsDouble())
        assertBounds(1.0..2.0, x)
    }

    @Test
    fun contradictionTest() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = 1.0..3.0;}
            feature y: ScalarValues::Real = x+0.1;
            inv r { x == y}
        """, Runlevel.ALL)
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }
        val x = solver.variable("x")
        val y = solver.variable("y")
        val r = solver.variable("r")
        assertEquals("Contradiction", r.valueStr)
    }

    @Test
    fun contradictionTest2() = testSession("Ranges") {
        loadKerML("""
            feature x: Ranges::RealInRange {:>> range = 1.0..3.0;}
            feature y: ScalarValues::Real = x+0.1;
            inv r { x >= y }
        """, Runlevel.ALL)
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }
        val x = solver.variable("x")
        val y = solver.variable("y")
        val r = solver.variable("r")
        // assertTrue(y.vectorQuantity.aadd().isEmpty())
        assertEquals("Contradiction", r.valueStr)
        // Contradiction? Depends on order which solver reports issue?
    }

    @Test
    fun basicPropagation() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Boolean;
            inv false b; 
            inv c;
            feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
            inv z; 
        """, Runlevel.ALL)
        assertNoIssues()
    }

    @Test
    fun basicPropagation2original() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv false b; 
            inv c;
            inv d { (a and c) or (not(b) and not(a)) }
        """, Runlevel.ALL)
        val a = solver.variable("a")
        val b = solver.variable("b")
        //println(y!!.quantity.bdd().toIteString())
        //println(y!!.quantity.bdd().evaluate().toIteString())
        // println(builder.conds.indexes.toString())
        assertNoIssues()

        //(this as AgilaServiceImpl).dSolver.propagateByPath(this.getProperties())

        //assertEquals(builder.Bool.True, y!!.quantity.bdd())
        //assertEquals(builder.Bool.True, a!!.quantity.bdd()) //FIXME: Not working at the moment
    }

    @Test
    fun discContInterfaceTest1() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = [1.0 .. 3.0];
            feature y: ScalarValues::Real = [1.0 .. 4.0];
            feature a: ScalarValues::Boolean = x >= y {:>> range = "true";}
        """, Runlevel.ALL)
        //TODO: The actual test^^
    }

    @Test
    fun contDiscInterfaceTest1()  // 'z' and 'a' are in conflict!
            = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = [1.0 .. 3.0];
            feature y: ScalarValues::Real = [1.0 .. 4.0];
            feature a: ScalarValues::Boolean = (x >= y) {:>> range = "true";}
            feature z: ScalarValues::Real = ITE(a, 3.0, 2.0) {:>> range = "1.0 .. 2.0";}
        """, Runlevel.ALL)
        //TODO: The actual test^^
    }

    @Test
    fun discContCyclicDependencyTest()  //not yet finished!
            = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = [1.0..3.0];
            feature y: ScalarValues::Real = [1.0..4.0];
            feature a: ScalarValues::Boolean = x >= y {:>> range = "true";}
            feature z: ScalarValues::Real = ITE(a, 3.0, 2.0) {:>> range = "2.0..3.0";}
            feature f: ScalarValues::Boolean = z >= y;
            feature g: ScalarValues::Real = ITE(f, 1.5, 1.0);
        """)
        solver.propagate()
        //println(getProperties().toString())
        //this.letVar(UId("f"), builder.scalar(1.0)) //FIXME: Probably breaks...
        //TODO: The actual test^^
    }

    @Test
    fun complexBDDdownTest() = testSession("ScalarValues") {
        loadKerML("""
        feature a: ScalarValues::Boolean;
        feature b: ScalarValues::Boolean {:>> range = "false";}
        feature c: ScalarValues::Boolean {:>> range = "true";}
        feature y: ScalarValues::Boolean = (a and c) or (not(b) and not(a)) {:>> range = "true";}
        """)
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
            inv false b; 
            inv c; 
            inv y { (a and c) or (not(b) and not(a)) }
            inv false z { a and c }
        """)
        assertNoIssues()
        solver.propagate()
        val y = solver.variable("z")
        assertEquals(builder.Bool.False, y.vectorQuantity.value)
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
        assertNoIssues()
        solver.propagate()
        val a = solver.getVariable("a")
        assertEquals(builder.Bool.True, a!!.vectorQuantity.bdd())
        val b = solver.getVariable("b")
        assertEquals(builder.Bool.True, b!!.vectorQuantity.bdd())
        val c = solver.getVariable("c")
        assertEquals(builder.Bool.True, c!!.vectorQuantity.bdd())
    }

    @Test
    fun exampleTest2() = testSession("ScalarValues") {
        loadKerML("""
            inv a { true }
            feature b: ScalarValues::Boolean;
            feature c: ScalarValues::Boolean = a and b;
        """)
        assertNoIssues()
        solver.propagate()
        val a = solver.getVariable("a")
        assertEquals(builder.Bool.True, a!!.vectorQuantity.bdd())
        val b = solver.variable("b")
        assertSame(b.vectorQuantity.value.builder.Bool.All, b.vectorQuantity.bdd())
        val c = solver.variable("c")
        assertSame(c.vectorQuantity.value.builder.Bool.All, c.vectorQuantity.bdd())
    }

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
        """, Runlevel.ALL)
        assertNoIssues()
        val a1 = solver.getVariable("a1")
        assertEquals(builder.Bool.True, a1!!.vectorQuantity.bdd())
        val a2 = solver.getVariable("a2")
        //assertEquals(builder.BOOL, a2!!.quantity.bdd())
        assertTrue(a2!!.valueStr.startsWith("Unknown"))

        val b1 = solver.getVariable("b1")
        // println(b1.toString())
        assertEquals(builder.Bool.True, b1!!.vectorQuantity.bdd())
        val b2 = solver.getVariable("b2")
        //assertEquals(builder.BOOL, b2!!.quantity.bdd())
        assertTrue(b2!!.valueStr.startsWith("Unknown"))

        val c1 = solver.variable("c1")
        val c2 = solver.variable("c2")

        val c3 = solver.getVariable("c3")
        val b3 = solver.getVariable("c3")
        val a3 = solver.getVariable("c3")

        assertEquals(builder.Bool.True, c3!!.vectorQuantity.bdd(), "c3")
        assertEquals(builder.Bool.True, b3!!.vectorQuantity.bdd(), "b3")
        assertEquals(builder.Bool.True, a3!!.vectorQuantity.bdd(), "a3")
    }

    @Test
    fun logicOperatorsInitializationTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            feature b: ScalarValues::Boolean;
            inv c { a and b }
        """)
        solver.propagate()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        val c = solver.getVariable("c")
        assertEquals(builder.Bool.True, c!!.vectorQuantity.value, "c (= a and b) should be true")
        assertEquals(builder.Bool.True, a!!.vectorQuantity.value, "a should be true")
        assertEquals(builder.Bool.True, b!!.vectorQuantity.value, "b should be true")
    }

    @Test
    fun logicOperatorsOrTest() = testSession("ScalarValues") {
        loadKerML("""
            inv false a2; 
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
        val a2 = solver.getVariable("a2")
        val a3 = solver.getVariable("a3")
        val a4 = solver.getVariable("a4")

        val b2 = solver.getVariable("b2")
        assertEquals(builder.Bool.True, b2!!.vectorQuantity.bdd())
        val b3 = solver.getVariable("b3")
        val b4 = solver.getVariable("b4")

        val c2 = solver.getVariable("c2")
        assertEquals(builder.Bool.True, c2!!.vectorQuantity.bdd())
        val c3 = solver.getVariable("c3")
        //assertTrue(c3!!.valueStr.startsWith("Unknown")) //Can make no assumption
        val c4 = solver.getVariable("c4")
        assertTrue(c4!!.valueStr.startsWith("Unknown"))
        //TODO!
    }

    @Test
    fun logicOperatorsNotTest() = testSession("ScalarValues") {
        loadKerML("""
            inv false a1; 
            inv b1 { not(a1) }
            inv a2; 
            inv false b2 { not(a2) }
            feature a5: ScalarValues::Boolean;
            inv b5 { not(a5) }
        """)
        assertNoIssues()
        solver.propagate()
        val a1 = solver.getVariable("a1")
        val a2 = solver.getVariable("a2")
        val a5 = solver.getVariable("a5")
        assertEquals(builder.Bool.False, a5!!.vectorQuantity.bdd())

        val b1 = solver.getVariable("b1")
        assertEquals(builder.Bool.True, b1!!.vectorQuantity.bdd())
        val b2 = solver.getVariable("b2")
        assertEquals(builder.Bool.False, b2!!.vectorQuantity.bdd())
        val b5 = solver.getVariable("b5")
        assertEquals(builder.Bool.True, b5!!.vectorQuantity.bdd())
    }

    @Test
    fun logicOperatorsITETest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv false b; 
            inv c; 
            inv d { ITE(a,b,c) }
        """)
        solver.propagate()
        //TODO: hier weiter
        val a = solver.getVariable("a")
        assertEquals(builder.Bool.False, a!!.vectorQuantity.value.asBdd())
    }

    @Test
    fun inequationTest() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = oneOf(1.0 .. 3.0);
            feature y: ScalarValues::Real = oneOf(1.0 .. 4.0);
            feature b: ScalarValues::Boolean;
            feature a: ScalarValues::Boolean = (x >= y) and not(b) {:>> range = true;}
        """)
        solver.propagate()
        val x = solver.getVariable("x")
        val a = solver.getVariable("a")

        // val solver = solver.discreteSolver

        // println(solver.getInfeasibilityMap().toString())
        //solver.handleInequations(a!!)

        //val uMap = (this as AgilaServiceImpl).dSolver.getUnitMap()
        //println(a.quantity.value.asBdd().toIteString())
        //println(builder.conds.x.toString())
        //println((this as AgilaServiceImpl).dSolver.getInfeasibilityMap().toString())
        //assertEquals(builder.Bool.True, a!!.quantity.bdd())

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
        assertNoIssues()
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
            inv false c; 
            feature d: ScalarValues::Boolean = true or false;
            inv e { true or false }
            inv false f { true or false }
            feature g: ScalarValues::Boolean = true or false;
        """, Runlevel.ALL)
        // There is no exor function ... yet. Either we add one ...
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }
        val a = solver.getVariable("a")
        assertTrue(a!!.valueStr.startsWith("Unknown"))
        val b = solver.getVariable("b")
        assertTrue(b!!.valueStr.startsWith("True"))
        val c = solver.getVariable("c")
        val d = solver.getVariable("d")
        val e = solver.getVariable("e")
        val f = solver.getVariable("f")
        val g = solver.getVariable("g")
    }

    @Test
    fun basicUnitRecognitionTest() = testSession("ScalarValues") {
        loadKerML("""
            inv false a { false }
            feature b: ScalarValues::Boolean;
            inv c { a or b }
        """)
        solver.propagate()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        val c = solver.getVariable("c")
        assertIs<BDD.Leaf>(b!!.vectorQuantity.value, "b")
        assertIs<BDD.Leaf>(c!!.vectorQuantity.value, "c")
        assertEquals(builder.Bool.True, b.vectorQuantity.bdd(), "b")
        assertEquals(builder.Bool.True, c.vectorQuantity.bdd(), "c")
    }

    @Test
    fun complexUnitRecognitionTest() {
        //TODO
    }

    @Test
    fun conflictDetectionTest() = testSession("ScalarValues") {
        loadKerML("""
            inv a; 
            inv false b;
            inv c { b } 
        """)
        solver.propagate()
        assertEquals("Contradiction", (solver.variable("c").valueStr))
        //println(status.errors.toString())
    }

    @Test
    fun basicDontCareRecognitionTest() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Boolean;
            inv false b; 
            inv c; 
            inv d { (a and c) or (not(b) and not(a)) }
        """)
        assertNoIssues()
        solver.propagate()
        val a = solver.getVariable("a")
        val b = solver.getVariable("b")
        val c = solver.getVariable("c")
        val d = solver.getVariable("d")
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
            inv false arbitraryConstraint2; 
            feature arbitraryConstraint3: ScalarValues::Boolean;
            inv constr { 
                arbitraryConstraint1 and arbitraryConstraint2 or arbitraryConstraint3 
            }
        """, Runlevel.ALL)
        assertNoIssues()
        val constraint = solver.getVariable("constr")
        val arbitraryConstraint3 = solver.getVariable("arbitraryConstraint3")
        //1. One related constraint should be introduced: aC1 and aC2 evaluate to false => aC3 has to be true
        //FIXME: comment back in! assertEquals(1, (this as AgilaServiceImpl).dSolver.lastIntroducedProperty)
        //assertEquals(builder.Bool.True, arbitraryConstraint3!!.quantity.value)
        //println(constraint.toString())
        //println(arbitraryConstraint3!!.quantity.toString())
        //2. Now change quantity/value of the arbitrary constraints and see how it impacts introduced related properties, properties and conditions
        arbitraryConstraint3!!.expression = "false"
        arbitraryConstraint3.compileExpression()
        // println("reactToUserChangesTest before 2. propagation")
        settings.runlevel = Runlevel.VARIANCE_CHECKED
        solver.propagate()
        //This scenario should result in not satisfiable Constraint.
        assertEquals("Contradiction", constraint!!.vectorQuantity.bdd().toString())
        //TODO hier weiter!
        //TODO: Now set satisfiable arbitrary constraints again
        arbitraryConstraint3.expression = ""
        arbitraryConstraint3.compileExpression()
        arbitraryConstraint3.vectorQuantity = arbitraryConstraint3.vectorQuantity.copy(values = listOf(builder.Bool.All))
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
            assertNoIssues()
            val enum = solver.variable("enumFake")

            //println("=== CONDS ===")
            //builder.conds.indexes.forEach { println(it) }
            assertBounds(1.0..7.0, enum)
        }
    }

    @Test
    fun enumTestViaFunction() {
        testSession("ScalarValues") {
            loadKerML("""
                feature enumFake: ScalarValues::Real = oneOf(1.0, 2.0, 3.0, 4.0, 7.0); 
            """.trimIndent())
            solver.propagate()
            assertNoIssues()
            val enum = solver.variable("enumFake")

            //println("=== CONDS ===")
            //builder.conds.indexes.forEach { println(it) }
            assertBounds(1.0..7.0, enum)
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
            assertNoIssues()
            //builder.conds.x.forEach { println(it) }
            val enum = solver.getVariable("selection")
            val constr = solver.getVariable("selectorConstraint")

            //assertEquals(builder.Bool.False, builder.conds.x[4])
            assertBounds(7.0, solver.variable("selection"))
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
            assertNoIssues()

            val enum = solver.variable("selection")

            //assertEquals(builder.Bool.False, builder.conds.x[7])
            //assertEquals(builder.Bool.False, builder.conds.x[8])
            var falseCounter = 0
            var unknownCounter = 0
            for (x in builder.conditions.x) {
                if (x.value is BDD && x.value == builder.Bool.False) falseCounter++
                if (x.value is BDD && x.value == builder.Bool) unknownCounter++
            }
            assertBounds(3.0, enum)
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
            assertNoIssues()

            val enum1 = solver.variable("selection1")
            val enum2 = solver.variable("selection2")
            //assertEquals(builder.Bool.False, builder.conds.x[3])
            //assertEquals(builder.Bool.False, builder.conds.x[4])
            //assertEquals(builder.Bool.False, builder.conds.x[5])
            //assertEquals(builder.Bool.False, builder.conds.x[6])
            // for (x in builder.conds.x) {
                //if (x.value is BDD) assertEquals(builder.Bool.False, x.value)
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
                //feature enumConstraint: ScalarValues::Boolean = enumResult >= 5.0 {:>> range = "true";}
                feature enumConstraint: ScalarValues::Boolean = (enum1 + enum2) >= 5.0 {:>> range = "true";}
            """.trimIndent())
            solver.propagate()
            val enum1 = solver.variable("enum1")
            val enum2 = solver.variable("enum2")
            val result = solver.variable("enumResult")
            val constraint = solver.variable("enumConstraint")
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
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }
        solver.propagate()
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }

        val c = solver.variable("contradiction")
        val p1 = solver.variable("prop1")
        val p1f = solver.variable("prop1T")
        val p1t = solver.variable("prop1F")
        val p2 = solver.variable("prop2")
        val p2f = solver.variable("prop2T")
        val p2t = solver.variable("prop2F")

        assertEquals("Contradiction", c.vectorQuantity.value.toString())
        assertEquals("Infeasible", p1.vectorQuantity.value.toString())
        assertEquals("Contradiction", p1f.vectorQuantity.value.toString())
        assertEquals("Contradiction", p1t.vectorQuantity.value.toString())
        assertEquals("Infeasible", p2.vectorQuantity.value.toString())
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
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }
        solver.propagate()
        assertNoIssues { it.kind != Issue.Kind.WARN_INCONSISTENCY }

        val c = solver.variable("contradiction")
        val x = solver.variable("x")
        val y = solver.variable("y")
        val z = solver.variable("z")
        val a = solver.variable("a")

        assertEquals("Contradiction", c.vectorQuantity.value.toString())
        assertEquals("Contradiction", a.vectorQuantity.value.toString())
        assertEquals(builder.Bool.False, x.vectorQuantity.value)
        assertEquals(builder.Bool.True, y.vectorQuantity.value)
        assertEquals(builder.Bool.False, z.vectorQuantity.value)
    }

    private fun chain(v : String, n : Int) : List<String>
            = (1 .. n).map { "feature $v$it : ScalarValues::Boolean" + if (it > 1) " = not $v${it-1};" else "." }

    @Test
    fun chainFeasible() = testSession("ScalarValues") {
        val n = 10
        loadKerML(chain("x", n).plus("feature y : ScalarValues::Boolean(true) = x1;").joinToString("\n"))
        solver.propagate()
        assertNoIssues()

        for(i in 1 .. n)
        {
            val v = solver.variable("x$i").vectorQuantity.value

            if(i % 2 == 1)
                assertEquals(v, builder.Bool.True)
            else
                assertEquals(v, builder.Bool.False)
        }
    }

    private fun cycle(v : String, n : Int) : List<String>
    = (1 .. n).map { "feature $v$it : ScalarValues::Boolean = not $v${if (it > 1) it-1 else n};" }

    @Test
    fun oddCycleInfeasible() = testSession("ScalarValues") {
        val n = 13
        loadKerML(cycle("x", n).joinToString("\n"))
        solver.propagate()
        assertNoIssues()

        for(i in 1 .. n) {
            val v = solver.variable("x$i").vectorQuantity.value

            assertEquals("Infeasible", v.toString())
        }
    }

    @Test
    fun evenCycleFeasible() = testSession("ScalarValues") {
        val n = 4
        loadKerML(cycle("x", n).joinToString("\n"), Runlevel.ALL)
        assertNoIssues()

        for(i in 1 .. n) {
            val v = solver.variable("x$i").vectorQuantity.value
            assertEquals("Unknown", v.toString())
        }

        loadKerML("feature y : ScalarValues::Boolean(true) = x1;", Runlevel.ALL)

        for(i in 1 .. n) {
            val v = solver.variable("x$i").vectorQuantity.value
            assertEquals(if(i % 2 == 1) v.builder.Bool.True else v.builder.Bool.False, v)
        }
    }
}

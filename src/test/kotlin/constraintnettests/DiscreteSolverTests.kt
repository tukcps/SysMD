@file:Suppress("UNUSED_VARIABLE")

package constraintnettests

import com.github.tukcps.aadd.BDD
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.compiler.parseDependency
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Namespace
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class DiscreteSolverTests {

    @Test
    //TODO: Move to better package...
    fun booleanBySubclasses() = testSession {
        loadSysMD(catchExceptions = false, input = """
               package Reason {
                   class General;
                   class Variant1 :> General;
                   class Variant2 :> General; 
               }

               Reason::General hasA feature p: ScalarValues::Boolean = bySubclasses(p).
               Reason::General hasA feature q: ScalarValues::Boolean = bySubclasses(q).

               Reason::Variant1 hasA feature p: ScalarValues::Boolean = true.
               Reason::Variant1 hasA feature q: ScalarValues::Boolean = true.

               Reason::Variant2 hasA feature p: ScalarValues::Boolean = true.
               Reason::Variant2 hasA feature q: ScalarValues::Boolean = false.
            """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        initialize()
        propagate()
        val p = global.resolveVar("Reason::General::p")!!
        val q = global.resolveVar("Reason::General::q")!!
        assertEquals(builder.True, p.vectorQuantity.value)
        assertNotEquals(builder.Bool, q.vectorQuantity.value) //FIXME: Do we want internal with ITE(x, t, f) or unknown leaf?
        assertNotEquals(builder.True, q.vectorQuantity.value)
        assertNotEquals(builder.False, q.vectorQuantity.value)
        assertEquals(1, q.vectorQuantity.value.height())
    }

    @Test
    fun booleanBySubclassesWithBoolSpec() = testSession {
        loadSysMD(input = """
               package Reason {
                   class General;
                   class Variant1 isA General;
                   class Variant2 isA General; 
               }

               Reason::General hasA attribute p: ScalarValues::Boolean(true) = bySubclasses(p).
               Reason::General hasA attribute q: ScalarValues::Boolean = bySubclasses(q).

               Reason::Variant1 hasA attribute p: ScalarValues::Boolean(true) = true.
               Reason::Variant1 hasA attribute q: ScalarValues::Boolean = true.

               Reason::Variant2 hasA attribute p: ScalarValues::Boolean(true) = true.
               Reason::Variant2 hasA attribute q: ScalarValues::Boolean = false.
            """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        initialize()
        propagate()
        //FIXME: Exception even before assertions...
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}"
        ) //checking for no errors should be sufficient here: bySubclasses will throw during initialization if something is wrong
    }

    @Test
    fun hasATest2() = testSession {
        loadSysMD(input = """
                feature a: ScalarValues::Boolean(true);
                feature b: ScalarValues::Boolean(false) = hasA(Global, a);
                """)
        propagate()
        val b = global.resolve<Feature>("b")!!.variable!!
        assertEquals("Contradiction", b.vectorQuantity.value.toString()
        ) //FIXME: want contradiction? or exception during evaluation?
    }

    /**
     * Check that misuse of a type as a variable is detected and reported.
     */
    @Test
    fun isATest2() = testSession {
        loadSysMD("""
            class c2; 
            feature c2 : ScalarValues::Boolean; 
            """.trimIndent()
        )
        assertEquals(1, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun requirementTestGt() = testSession {
        loadSysMD("""
            feature x: ScalarValues::Real =[1.0..3.0].
            inv r { x > 1.0+1.0 }
        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolve<Feature>("x")!!.variable!!
        // println(x)
        assertTrue(x.vectorQuantity.getMinAsDouble() <= 2.0)
    }

    /**
     * A requirement x >= 2.0 is tested.
     * The Minimum must be constrained to something less or equal 2.0.
     */
    @Test
    fun requirementTestGe() = testSession {
        loadSysMD(catchExceptions = false, input = """
            attribute x: ScalarValues::Real = [1.0..3.0];
            inv r { x >= 1.0+1.0}
        """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        initialize()
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolveVar("x")!!
        // println(x.quantity.getMinAsDouble())
        assertTrue(x.vectorQuantity.getMinAsDouble() <= 2.0)
    }

    @Test
    fun requirementTestLt() = testSession {
        loadSysMD(
            catchExceptions = false, input = """
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x < 1.0+1.0 }
        """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolve<Feature>("x")!!.variable!!
        // println(x.quantity.getMaxAsDouble())
        assertTrue(x.vectorQuantity.getMaxAsDouble() >= 2.0)
    }


    @Test
    fun requirementTestLe() = testSession {
        loadSysMD(
            input = """
            feature x: ScalarValues::Real = [1.0..3.0];
            inv r { x <= 1.0+1.0 }
        """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolve<Feature>("x")!!.variable!!
        // println(x.quantity.getMaxAsDouble())
        assertTrue(x.vectorQuantity.getMaxAsDouble() >= 2.0)
    }

    @Test
    fun contradictionTest() = testSession {
        loadSysMD(
            input = """
            attribute x: ScalarValues::Real(1.0..3.0);
            attribute y: ScalarValues::Real = x+0.1;
            inv r { x == y} 
        """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolveVar("x")!!
        val y = global.resolveVar("y")!!
        val r = global.resolveVar("r")!!
        assertTrue(x.vectorQuantity.aadd().isEmpty())
        assertTrue(y.vectorQuantity.aadd().isEmpty())
        assertEquals("[Infeasible]", r.valueStr)
    }

    @Test
    fun contradictionTest2() = testSession {
        loadSysMD(input = """
            attribute x: ScalarValues::Real(1.0..3.0);
            attribute y: ScalarValues::Real = x+0.1;
            attribute r: ScalarValues::Boolean(true) = x >= y;  
            """)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val x = global.resolveVar("x")!!
        val y = global.resolveVar("y")!!
        val r = global.resolveVar("r")!!
        assertTrue(x.vectorQuantity.aadd().isEmpty())
        assertTrue(y.vectorQuantity.aadd().isEmpty())
        assertEquals("[Infeasible]", r.valueStr)
        // Contradiction? Depends on order which solver reports issue?
    }

    @Test
    fun basicPropagation() = testSession {
        loadSysMD(input = """
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean(false);
            attribute c: ScalarValues::Boolean(true);
            attribute y: ScalarValues::Boolean = (a and c) or (not(b) and not(a));
            attribute z: ScalarValues::Boolean(true);
        """
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun basicpropagation2original() = testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean(false).
            attribute c: ScalarValues::Boolean(true).
            attribute d: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a))."""
        )

        // Error is here -----^ would never have seen that typo. thanks.
        // ... an in initialize that fails to recognize this as error & runs in infinite loop ...
        this.settings.catchExceptions = true
        propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        assertNotNull(a)
        assertNotNull(b)
        //println(y!!.quantity.bdd().toIteString())
        //println(y!!.quantity.bdd().evaluate().toIteString())
        // println(builder.conds.indexes.toString())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())

        //(this as AgilaServiceImpl).dSolver.propagateByPath(this.getProperties())

        //assertEquals(builder.TRUE, y!!.quantity.bdd())
        //assertEquals(builder.TRUE, a!!.quantity.bdd()) //FIXME: Not working at the moment
    }

    @Test
    fun discContInterfaceTest1() = testSession {
        loadSysMD(
            catchExceptions = false, input = """
            attribute x: ScalarValues::Real = [1.0 .. 3.0].
            attribute y: ScalarValues::Real = [1.0 .. 4.0].
            attribute a: ScalarValues::Boolean(true) = x >= y.
        """.trimIndent()
        )
        initialize()
        //TODO: The actual test^^
    }

    @Test
    fun contDiscInterfaceTest1()  //z and a are in conflict!
            = testSession {
        loadSysMD(
            catchExceptions = false, input = """
            attribute x: ScalarValues::Real = [1.0 .. 3.0];
            attribute y: ScalarValues::Real = [1.0 .. 4.0];
            attribute a: ScalarValues::Boolean(true) = x >= y;
            attribute z: ScalarValues::Real(1.0 .. 2.0) = ITE(a, 3.0, 2.0);
        """.trimIndent()
        )
        initialize()
        //TODO: The actual test^^
    }

    @Test
    fun discContCyclicDependencyTest()  //not yet finished!
            = testSession {
        loadSysMD(
            catchExceptions = false, input = """
        attribute x: ScalarValues::Real = [1.0..3.0];
        attribute y: ScalarValues::Real = [1.0..4.0];
        attribute a: ScalarValues::Boolean(true) = x >= y;
        attribute z: ScalarValues::Real(2.0..3.0) = ITE(a, 3.0, 2.0);
        attribute f: ScalarValues::Boolean = z >= y;
        attribute g: ScalarValues::Real = ITE(f, 1.5, 1.0);
        """.trimIndent()
        ).run {
            initialize()
            propagate()
            //println(getProperties().toString())
            //this.letVar(UId("f"), builder.scalar(1.0)) //FIXME: Probably breaks...
            //TODO: The actual test^^
        }
    }

    @Test
    fun complexBDDdownTest() = testSession {
        loadSysMD("""
        attribute a: ScalarValues::Boolean;
        attribute b: ScalarValues::Boolean(false);
        attribute c: ScalarValues::Boolean(true);
        attribute y: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a));
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
    fun tautologyTest() = testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean(false).
            attribute c: ScalarValues::Boolean(true).
            attribute y: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a)).
            attribute z: ScalarValues::Boolean(false) = a and c.
        """.trimIndent()
        )
        //val disc = solver.discSolver
        initialize()
        propagate()
        val y = global.resolveVar("z")
        assertNotNull(y)
        assertTrue(y!!.vectorQuantity.value == builder.False)
        //disc.solve(props)
        // println("break")
    }

    @Test
    fun exampleTest() = testSession {
        loadSysMD("""
                attribute a: ScalarValues::Boolean = true;
                attribute b: ScalarValues::Boolean;
                attribute c: ScalarValues::Boolean(true) = a and b;
            """.trimIndent())
        //val disc = solver.discSolver
        propagate()
        val a = global.resolveVar("a")
        assertEquals(builder.True, a!!.vectorQuantity.bdd())
        val b = global.resolveVar("b")
        assertEquals(builder.True, b!!.vectorQuantity.bdd())
        val c = global.resolveVar("c")
        assertEquals(builder.True, c!!.vectorQuantity.bdd())
        //println(getProperties().toString())
        // println("break")
    }

    @Test
    fun exampleTest2() = testSession {
        loadSysMD("""
                attribute a: ScalarValues::Boolean = true;
                attribute b: ScalarValues::Boolean;
                attribute c: ScalarValues::Boolean = a and b;
            """.trimIndent())
        //val disc = solver.discSolver
        propagate()
        val a = global.resolveVar("a")
        assertEquals(builder.True, a!!.vectorQuantity.bdd())
        val b = global.resolveVar("b")
        assertTrue(b!!.vectorQuantity.bdd() is BDD.Internal)
        val c = global.resolveVar("c")
        assertTrue(c!!.vectorQuantity.bdd() is BDD.Internal)
        //disc.solve(props)
        // println("break")
    }


    //WIP: 10 Tests!
    @Test
    fun logicOperatorsAndTest() = testSession {
        loadSysMD(
            """
            attribute a1: ScalarValues::Boolean(true).
            attribute b1: ScalarValues::Boolean.
            attribute c1: ScalarValues::Boolean(true) = a1 and b1.
            attribute a2: ScalarValues::Boolean.
            attribute b2: ScalarValues::Boolean.
            attribute c2: ScalarValues::Boolean = a2 and b2.
            attribute a3: ScalarValues::Boolean.
            attribute b3: ScalarValues::Boolean.
            attribute c3: ScalarValues::Boolean(true) = a3 and b3.
        """
        )
        propagate()
        val a1 = global.resolveVar("a1")
        assertEquals(builder.True, a1!!.vectorQuantity.bdd())
        val a2 = global.resolveVar("a2")
        //assertEquals(builder.BOOL, a2!!.quantity.bdd())
        assertTrue(a2!!.valueStr.startsWith("[Unknown"))

        val b1 = global.resolveVar("b1")
        // println(b1.toString())
        assertEquals(builder.True, b1!!.vectorQuantity.bdd())
        val b2 = global.resolveVar("b2")
        //assertEquals(builder.BOOL, b2!!.quantity.bdd())
        assertTrue(b2!!.valueStr.startsWith("[Unknown"))

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
    fun logicOperatorsInitializationTest() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean.
            attribute c: ScalarValues::Boolean(true) = a and b.
        """)
        propagate()
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")
        assertEquals(builder.True, a!!.vectorQuantity.value, "a should be true")
        assertEquals(builder.True, b!!.vectorQuantity.value, "b should be true")
        assertEquals(builder.True, c!!.vectorQuantity.value, "c (= a and b) should be true")
    }

    @Test
    fun logicOperatorsOrTest() = testSession {
        loadSysMD(
            """
            attribute a2: ScalarValues::Boolean(false).
            attribute b2: ScalarValues::Boolean.
            attribute c2: ScalarValues::Boolean(true) = a2 or b2.
            attribute a3: ScalarValues::Boolean.
            attribute b3: ScalarValues::Boolean.
            attribute c3: ScalarValues::Boolean(true) = a3 or b3.
            attribute a4: ScalarValues::Boolean.
            attribute b4: ScalarValues::Boolean.
            attribute c4: ScalarValues::Boolean = a4 or b4.
        """
        )
        propagate()
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
        assertTrue(c4!!.valueStr.startsWith("[Unknown"))
        //TODO!
    }

    @Test
    fun logicOperatorsNotTest() = testSession {
        loadSysMD(
            """
            attribute a1: ScalarValues::Boolean(false).
            attribute b1: ScalarValues::Boolean(true) = not(a1).
            attribute a2: ScalarValues::Boolean(true).
            attribute b2: ScalarValues::Boolean(false) = not(a2).
            attribute a5: ScalarValues::Boolean.
            attribute b5: ScalarValues::Boolean(true) = not(a5).
        """
        )
        propagate()
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
    fun logicOperatorsITETest() = testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean(false).
            attribute c: ScalarValues::Boolean(true).
            attribute d: ScalarValues::Boolean(true) = ITE(a,b,c).
            """
        )
        propagate()
        //TODO: hier weiter
        val a = global.resolveVar("a")
        assertEquals(builder.False, a!!.vectorQuantity.value.asBdd())
    }

    @Test
    fun inequationTest() = testSession {
        loadSysMD(
            catchExceptions = false, input = """
            attribute x: ScalarValues::Real = [1.0 .. 3.0].
            attribute y: ScalarValues::Real = [1.0 .. 4.0].
            attribute b: ScalarValues::Boolean.
            attribute a: ScalarValues::Boolean(true) = (x >= y) and not(b).
        """.trimIndent()
        )
        propagate()
        val x = global.resolveVar("x")
        val a = global.resolveVar("a")

        val solver = dSolver

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
    fun hasATest() = testSession {
        loadSysMD("""
            package Example3 {
                attribute a: ScalarValues::Boolean = true;
                attribute b: ScalarValues::Boolean; 
                attribute c: ScalarValues::Boolean(true) = a and b; 
                class d;
                class e; 
                part test: [0..2] Base::Anything; 
            }
            """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        val ex3d = global.resolve<Namespace>("Example3::d")!!
        val tst = ex3d.resolve<Element>("test")
        assertNotNull(tst)
        //FIXME: Removed erroneous functions that caused a stack overflow. Reformulate test case!
    }

    @Test
    fun semanticTest() = testSession(catchExceptions = false) {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean(true);
            attribute c: ScalarValues::Boolean(false);
            attribute d: ScalarValues::Boolean = true or false;
            attribute e: ScalarValues::Boolean(true) = true or false;
            attribute f: ScalarValues::Boolean(false) = true or false;
            attribute g: ScalarValues::Boolean = true or false;
            """
        )
        // There is no exor function ... yet. Either we add one ...
        initialize()
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val a = global.resolveVar("a")
        assertTrue(a!!.valueStr.startsWith("[Unknown"))
        val b = global.resolveVar("b")
        assertTrue(b!!.valueStr.startsWith("[True"))
        val c = global.resolveVar("c")
        val d = global.resolveVar("d")
        val e = global.resolveVar("e")
        val f = global.resolveVar("f")
        val g = global.resolveVar("g")
    }

    @Test
    fun basicUnitRecognitionTest() = testSession {
        loadSysMD("""
        attribute a: ScalarValues::Boolean(false) = false;
        attribute b: ScalarValues::Boolean;
        attribute c: ScalarValues::Boolean(true) = a or b;
        """)
        initialize()
        propagate()
        var a = global.resolveVar("a")
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
    fun conflictDetectionTest() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean(true);
            attribute b: ScalarValues::Boolean(false);
            attribute c: ScalarValues::Boolean(true) = b;""")
        propagate()
        assertEquals("[Contradiction]", (global.resolveVar("c")!!.valueStr))
        //println(status.errors.toString())
    }

    @Test
    fun basicDontCareRecognitionTest() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean(false);
            attribute c: ScalarValues::Boolean(true);
            attribute d: ScalarValues::Boolean(true) = (a and c) or (not(b) and not(a));""")
        settings.catchExceptions = false
        propagate()
        val a = global.resolveVar("a")
        val d = global.resolveVar("d")
        assertEquals(false, a!!.bdd() is BDD.Leaf)
        assertEquals(true, d!!.bdd() is BDD.Leaf)
        //assertEquals() TODO: Test for dontcare.size == 1
    }

    @Test
    fun updateDependentPropertiesTest() {
        //TODO!
    }

    @Test @Disabled//Not needed anymore. Delete later...
    fun introducePropertyTest() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean(false) = false;
            attribute b: ScalarValues::Boolean;
            attribute c: ScalarValues::Boolean(true) = a or b;
        """.trimIndent())
        propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")
        val aIntroduced = solver.getRelatedValueFeatures(a!!.elementId)
        val bIntroduced = solver.getRelatedValueFeatures(b!!.elementId)
        val cIntroduced = solver.getRelatedValueFeatures(c!!.elementId)

        assertEquals(0, aIntroduced.size)
        assertEquals(0, bIntroduced.size)
        assertEquals(1, cIntroduced.size)
    }

    @Test
    fun introduceNoPropertyTest() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean;
            attribute c: ScalarValues::Boolean(true) = a or b;
        """.trimIndent()
        )
        propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")
        val aIntroduced = solver.getRelatedValueFeatures(a!!.elementId)
        val bIntroduced = solver.getRelatedValueFeatures(b!!.elementId)
        val cIntroduced = solver.getRelatedValueFeatures(c!!.elementId)
        assertEquals(0, aIntroduced.size)
        assertEquals(0, bIntroduced.size)
        assertEquals(0, cIntroduced.size)
    }

    @Test
    fun introduceNoPropertyTest2() = testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean; 
            attribute b: ScalarValues::Boolean; 
            assert c { a or b } 
        """.trimIndent())
        propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")
        val aIntroduced = solver.getRelatedValueFeatures(a!!.elementId)
        val bIntroduced = solver.getRelatedValueFeatures(b!!.elementId)
        val cIntroduced = solver.getRelatedValueFeatures(c!!.elementId)
        assertEquals(0, aIntroduced.size)
        assertEquals(0, bIntroduced.size)
        assertEquals(0, cIntroduced.size)
    }

    @Test
    fun reactToUserChangesTest() = testSession {
        loadSysMD("""
            feature arbitraryConstraint1: ScalarValues::Boolean(true);
            feature arbitraryConstraint2: ScalarValues::Boolean(false);
            feature arbitraryConstraint3: ScalarValues::Boolean;
            feature constr: ScalarValues::Boolean(true) = arbitraryConstraint1 and arbitraryConstraint2 or arbitraryConstraint3;"""
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        // println("reactToUserChangesTest before initialization")
        initialize()
        // println("reactToUserChangesTest before 1. propagation")
        propagate()
        val constraint = global.resolveVar("constr")
        val arbitraryConstraint3 = global.resolveVar("arbitraryConstraint3")
        //1. One related constraint should be introduced: aC1 and aC2 evaluate to false => aC3 has to be true
        //FIXME: comment back in! assertEquals(1, (this as AgilaServiceImpl).dSolver.lastIntroducedProperty)
        //assertEquals(builder.True, arbitraryConstraint3!!.quantity.value)
        //println(constraint.toString())
        //println(arbitraryConstraint3!!.quantity.toString())
        //2. Now change quantity/value of the arbitrary constraints and see how it impacts introduced related properties, properties and conditions
        arbitraryConstraint3!!.feature.expression = "false"
        arbitraryConstraint3.parseDependency()
        // println("reactToUserChangesTest before 2. propagation")
        propagate()
        //This scenario should result in not satisfiable Constraint.
        assertEquals("Contradiction", constraint!!.vectorQuantity.bdd().toString())
        //TODO hier weiter!
        //TODO: Now set satisfiable arbitrary constraints again
        arbitraryConstraint3.feature.expression = ""
        arbitraryConstraint3.parseDependency()
        arbitraryConstraint3.vectorQuantity.values = mutableListOf(builder.Bool)
        // println("reactToUserChangesTest before 3. propagation")
        //FIXME: arbitraryConstraint3 still "false" => set conditions not reverted correctly!
        propagate()
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
        testSession(catchExceptions = false) {
            loadSysMD("""
                attribute c1: ScalarValues::Boolean; 
                attribute c2: ScalarValues::Boolean; 
                attribute c3: ScalarValues::Boolean;
                attribute c4: ScalarValues::Boolean; 
                attribute enum: ScalarValues::Real = if c1 ? 1.0 else if c2 ? 2.0 else if c3? 3.0 else if c4? 4.0 else 7.0; 
            """.trimIndent())
            propagate()
            val enum = global.resolveVar("enum")!!

            //println("=== CONDS ===")
            //builder.conds.indexes.forEach { println(it) }
            assertEquals("1..7", enum.vectorQuantity.value.toString() )
        }
    }

    @Test
    fun enumTestViaFunction() {
        testSession(catchExceptions = false) {
            loadSysMD("""
                attribute enum: ScalarValues::Real = oneOf(1.0, 2.0, 3.0, 4.0, 7.0); 
            """.trimIndent())
            propagate()
            assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
            val enum = global.resolveVar("enum")!!

            //println("=== CONDS ===")
            //builder.conds.indexes.forEach { println(it) }
            assertEquals("1..7", enum.vectorQuantity.value.toString() )
        }
    }

    @Test
    fun enumConstraintTest() {
        testSession(catchExceptions = false) {
            loadSysMD("""
                attribute selection: ScalarValues::Real = oneOf(1.0, 2.0, 3.0, 4.0, 7.0);
                assert selectorConstraint { selection >= 6.0 }
            """.trimIndent())
            propagate()
            //builder.conds.x.forEach { println(it) }
            val enum = global.resolveVar("selection")
            val constr = global.resolveVar("selectorConstraint")

            //assertEquals(builder.False, builder.conds.x[4])
            assertEquals("7..7", global.resolveVar("selection")!!.vectorQuantity.value.toString())
        }
    }

    @Test
    fun enumMoreConstraintsTest() {
        testSession(catchExceptions = false) {
            loadSysMD("""
                attribute selection: ScalarValues::Real = oneOf(1.0, 2.0, 3.0, 4.0, 7.0); 
                assert enumConstraint1 { selection >= 3.0 }
                assert enumConstraint2 { selection <= 3.0 }
            """.trimIndent())
            propagate()

            val enum = global.resolveVar("selection")!!

            //assertEquals(builder.False, builder.conds.x[7])
            //assertEquals(builder.False, builder.conds.x[8])
            var falseCounter = 0
            var unknownCounter = 0
            for (x in builder.conds.x) {
                if (x.value is BDD && x.value == builder.False) falseCounter++
                if (x.value is BDD && x.value == builder.Bool) unknownCounter++
            }
            assertEquals("3..3", enum.vectorQuantity.value.toString())
            //assertEquals(2, falseCounter)
            //assertEquals(2, unknownCounter)
        }
    }

    @Test
    fun enumArithmeticTest() {
        testSession {
            loadSysMD("""
                feature selection1: ScalarValues::Real = oneOf(1.0, 2.0, 3.0); 
                feature selection2: ScalarValues::Real = oneOf(1.0, 2.0, 3.0); 
                inv enumConstraint { (selection1 + selection2) >= 6.0 }
            """.trimIndent())
            propagate()
            assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())

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
        testSession(catchExceptions = false) {
            loadSysMD("""
                attribute enum1: ScalarValues::Real = oneOf(1.0, 3.0, 5.0); 
                attribute enum2: ScalarValues::Real = oneOf(1.0, 4.0, 6.0); 
                attribute enumResult: ScalarValues::Real = enum1 + enum2; 
                //attribute enumConstraint: ScalarValues::Boolean(true) = enumResult >= 5.0; 
                attribute enumConstraint: ScalarValues::Boolean(true) = (enum1 + enum2) >= 5.0; 
            """.trimIndent())
            propagate()
            val enum1 = global.resolveVar("enum1")!!
            val enum2 = global.resolveVar("enum2")!!
            val result = global.resolveVar("enumResult")!!
            val constraint = global.resolveVar("enumConstraint")!!
        }
    }

    @Test @Disabled
    fun enum4Queens() {
        TODO()
    }
}

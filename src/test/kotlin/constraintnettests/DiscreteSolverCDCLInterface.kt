package constraintnettests

import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.SessionManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Suppress("UNUSED_VARIABLE")
class DiscreteSolverCDCLInterface {

    /**
     * Testing the interface to/from CDCL. Currently, DiscreteSolver and CDCL are conflicting. Cause is known. Current (ugly) hot-fix: Do not propagate before starting the CDCL!
     */

    @Test
    fun simpleCNFTest() = SessionManager.testSession {
        loadSysMD(
            """
            attribute a: Boolean.
            attribute b: Boolean.
            attribute c: Boolean(true) = a or b.
            attribute d: Boolean(true) = a and b.
        """.trimIndent()
        )
        //propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")

        solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
        val normalizedModel = solver.normalizedBooleanModel
        val cnf = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(normalizedModel)

        val assignment = solver.translateCNFsBack(cnf)

        assignment.forEach { it.values.forEach { assertEquals(builder.True, it) } }
    }

    @Test
    fun simpleCNFTest2() = SessionManager.testSession {
        loadSysMD(
            """
            attribute a: Boolean.
            attribute b: Boolean.
            attribute c: Boolean.
            attribute d: Boolean(true) = a and c.
            attribute e: Boolean(true) = (a and b) or d.
        """.trimIndent()
        )
        //propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")

        solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
        val normalizedModel = solver.normalizedBooleanModel
        val cnf = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(normalizedModel)
        val assignment = solver.translateCNFsBack(cnf)

        assignment.forEach {
            it.forEach {
                if(it.key == 1) assertTrue(it.value == builder.True)
                if(it.key == 3) assertTrue(it.value == builder.True)
            }
        }
    }

    @Test
    fun unsatTest() = SessionManager.testSession {
        loadSysMD(
            """
            attribute a: Boolean.
            attribute b: Boolean.
            attribute c: Boolean(true) = not b.
            attribute d: Boolean(true) = a and b.
        """.trimIndent()
        )
        //propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")

        solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
        val normalizedModel = solver.normalizedBooleanModel
        val cnf = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(normalizedModel)
        assertTrue(cnf.isEmpty())
    }

    @Test
    fun falseSpecTest() = SessionManager.testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean.
            attribute c: ScalarValues::Boolean(false) = a or b.
        """.trimIndent())
        //propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")

        solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
        val normalizedModel = solver.normalizedBooleanModel
        val cnf = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(normalizedModel)

        assertTrue(cnf.isNotEmpty())

        val assignment = solver.translateCNFsBack(cnf)
        assignment.forEach { it.forEach { assertEquals(builder.False ,it.value) } }
    }

    @Test
    fun falseSpecTest2() = SessionManager.testSession {
        loadSysMD("""
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean.
            attribute c: ScalarValues::Boolean(false) = a or b.
            attribute d: ScalarValues::Boolean(false) = b.
        """.trimIndent())
        //propagate()
        val solver = dSolver
        val a = global.resolveVar("a")
        val b = global.resolveVar("b")
        val c = global.resolveVar("c")

        solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
        val normalizedModel = solver.normalizedBooleanModel
        val cnf = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(normalizedModel)

        assertTrue(cnf.isNotEmpty())

        val assignment = solver.translateCNFsBack(cnf)
        assignment.forEach { it.forEach { assertEquals(builder.False ,it.value) } }
    }

    @Test
    fun falseSpecTest3() = SessionManager.testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean.
            attribute c: ScalarValues::Boolean(false) = a or b.
            attribute d: ScalarValues::Boolean(true) = not b.
        """.trimIndent()
        )
        //propagate()
        val solver = dSolver

        solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
        val normalizedModel = solver.normalizedBooleanModel
        val cnf = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(normalizedModel)

        assertTrue(cnf.isNotEmpty())

        val assignment = solver.translateCNFsBack(cnf)
        assignment.forEach { it.forEach { assertEquals(builder.False ,it.value) } }
    }

    @Test
    fun falseSpecUnsatTest() = SessionManager.testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean.
            attribute c: ScalarValues::Boolean(false) = a or b.
            attribute d: ScalarValues::Boolean(true) = b.
        """.trimIndent()
        )
        //propagate()
        val solver = dSolver

        solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
        val normalizedModel = solver.normalizedBooleanModel
        val cnf = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(normalizedModel)
        assertTrue(cnf.isEmpty())
    }

    @Test @Disabled
    fun multipleSolutionsTest() = SessionManager.testSession {
        loadSysMD(
            """
            attribute a: ScalarValues::Boolean.
            attribute b: ScalarValues::Boolean.
            attribute c: ScalarValues::Boolean.
            attribute d: ScalarValues::Boolean.
            attribute e: ScalarValues::Boolean.
            attribute x: ScalarValues::Boolean.
            attribute f: ScalarValues::Boolean(true) = a or b or c or d or e.
            attribute g: ScalarValues::Boolean(true) = a or b or c or d or e or not(x).
        """.trimIndent()
        )
        //propagate()
        val model = this
        val solver = dSolver
        val persistentModel = solver.normalizeBooleanValueFeatures()

        solver.computeAllPartialSolutions(true)
        var assignmentsizesum = 0
        val assignmentsizeaverage: Int
        val boundary = 10000
        for (i in 1 .. boundary) {
            solver.resetNormalizedBooleanValueFeatureSolver(true, false)
            //solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
            //val normalizedModel = solver.normalizedBooleanModel
            val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(persistentModel.clone())
            //val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(solver.normalizeBooleanValueFeatures(model))
            assignmentsizesum += assignments.size

        }
        assignmentsizeaverage = assignmentsizesum/boundary
        //assertEquals(10, assignments.size)
        println("Average: $assignmentsizeaverage")
    }

    @Test @Disabled
    fun multipleSolutionsDeterminismTest() = SessionManager.testSession {
        var assignmentsizesum = 0
        val assignmentsizeaverage: Int
        val boundary = 10000
        for (i in 1 .. boundary) {
            loadSysMD(
                """
                attribute a: ScalarValues::Boolean;
                attribute b: ScalarValues::Boolean;
                attribute c: ScalarValues::Boolean;
                attribute d: ScalarValues::Boolean;
                attribute e: ScalarValues::Boolean;
                attribute x: ScalarValues::Boolean;
                attribute f: ScalarValues::Boolean(true) = a or b or c or d or e;
                attribute g: ScalarValues::Boolean(true) = a or b or c or d or e or not(x);
                """.trimIndent()
            )
        //propagate()
        //val model = this
            val solver = dSolver
            val persistentModel = solver.normalizeBooleanValueFeatures()

            solver.computeAllPartialSolutions(true)
            solver.resetNormalizedBooleanValueFeatureSolver(true, false)
            //solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
            //val normalizedModel = solver.normalizedBooleanModel
            val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(persistentModel.clone())
            //val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(solver.normalizeBooleanValueFeatures(model))
            assignmentsizesum += assignments.size

        }
        assignmentsizeaverage = assignmentsizesum/boundary
        //assertEquals(10, assignments.size)
        println("Average: $assignmentsizeaverage")
    }

    @Test @Disabled
    fun consistentAssignmentSizeTest() = SessionManager.testSession {
        var assignmentsizesum = 0
        val assignmentsizeaverage: Int
        val boundary = 10000
        for (i in 1..boundary) {
            loadSysMD(
                """
                attribute a: ScalarValues::Boolean;
                attribute b: ScalarValues::Boolean;
                attribute c: ScalarValues::Boolean;
                attribute d: ScalarValues::Boolean; 
                attribute e: ScalarValues::Boolean; 
                attribute x: ScalarValues::Boolean; 
                attribute f: ScalarValues::Boolean(true) = a or b or c or d or e; 
                attribute g: ScalarValues::Boolean(true) = a or b or c or d or e or not(x); 
                """.trimIndent()
            )
            //propagate()
            //val model = this
            val solver = dSolver
            val persistentModel = solver.normalizeBooleanValueFeatures()

            solver.computeAllPartialSolutions(true)
            solver.resetNormalizedBooleanValueFeatureSolver(true, false)
            //solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
            //val normalizedModel = solver.normalizedBooleanModel
            val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(persistentModel.clone())
            //val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(solver.normalizeBooleanValueFeatures(model))
            assignmentsizesum += assignments.size

        }
        assignmentsizeaverage = assignmentsizesum / boundary

        loadSysMD(
            """
            attribute a: ScalarValues::Boolean;
            attribute b: ScalarValues::Boolean;
            attribute c: ScalarValues::Boolean;
            attribute d: ScalarValues::Boolean;
            attribute e: ScalarValues::Boolean;
            attribute x: ScalarValues::Boolean;
            attribute f: ScalarValues::Boolean(true) = a or b or c or d or e;
            attribute g: ScalarValues::Boolean(true) = a or b or c or d or e or not(x);
        """.trimIndent()
        )
        //propagate()
        val model = this
        val solver = dSolver
        val persistentModel = solver.normalizeBooleanValueFeatures()

        solver.computeAllPartialSolutions(true)
        var assignmentsizesum2 = 0
        val assignmentsizeaverage2: Int
        val boundary2 = 10000
        for (i in 1 .. boundary2) {
            solver.resetNormalizedBooleanValueFeatureSolver(true, false)
            //solver.normalizedBooleanModel = solver.normalizeBooleanValueFeatures()
            //val normalizedModel = solver.normalizedBooleanModel
            val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(persistentModel.clone())
            //val assignments = solver.normalizedBooleanValueFeatureSolver.solveNormalizedCN(solver.normalizeBooleanValueFeatures(model))
            assignmentsizesum2 += assignments.size

        }
        assignmentsizeaverage2 = assignmentsizesum2/boundary2

        assertEquals(assignmentsizeaverage, assignmentsizeaverage2)
    }

}

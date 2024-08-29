package constraintnettests

import com.github.tukcps.sysmd.cspsolver.normalizer.CDCL
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

@Disabled
class CDCLTest {

    private val solver : CDCL = CDCL()
    private val parser : DIMACSParser = DIMACSParser()

    @Test
    fun simpleConjunction() {
        //solver.activateDebugMode()
        solver.findAllAssignments()
        val cnf = listOf(setOf(1), setOf(2))
        val assignments = solver.cdcl(cnf)
        //printDeterminedAssignments(assignments)
        assertTrue(assignments.size == 1)
    }

    @Test
    fun simpleDisjunction() {
        //solver.activateDebugMode()
        solver.findAllAssignments()
        val cnf = listOf(setOf(8, 10))
        //println(cnf)
        val assignments = solver.cdcl(cnf)
        //printDeterminedAssignments(assignments)
        assertTrue(assignments.size >= 2)
    }

    /**
     * Finding all assignments even for one formula can take quite a long time.
     * Statistics on my notebook (filename, amount of found satisfying (partial) assignments, time without debug output):
     * - (easy-sat-050, 432, 7 sec)
     * - (easy-sat-051, 900, 32 sec)
     * - (easy-sat-052, 3527, 7 min 25 sec)
     * - (easy-sat-053, 108, 1 sec)
     * - (easy-sat-054, 1824, 2 min 5 sec)
     * - (easy-sat-055, 2448, 3 min 38 sec)
     * - (easy-sat-056, 1476, 1 min 39 sec)
     * - (easy-sat-121, 31, < 1 sec)
     * - (easy-sat-122, 2, < 1 sec)
     * - (easy-sat-123, 6, < 1 sec)
     * - (easy-sat-057, 504, 9 sec)
     * - (easy-sat-058, 4662, 14 min 40 sec)
     * - (easy-sat-059, 360, 5 sec)
     * - (easy-sat-061, 10932, 1 h 35 min)
     * - (easy-sat-062, 786, 24 sec)
     * - (easy-sat-063, 3636, 9 min 30 sec)
     * - (easy-sat-107, 729, 5 sec)
     * - (easy-sat-108, 916, 7 sec)
     * - (easy-sat-119, 386, 3 sec)
     * - (easy-sat-124, 594, 15 sec)
     * - (easy-sat-125, 2976, 6 min 26 sec)
     * - (easy-sat-126, 3264, 6 min 48 sec)
     * - (easy-sat-127, 2424, 4 min 7 sec)
     * - (easy-sat-128, 672, 18 sec)
     * - (easy-sat-129, 2112, 2 min 49 sec)
     * - (easy-sat-130, 774, 22 sec)
     * - (easy-sat-131, 1482, 1 min 26 sec)
     * - (easy-sat-132, 2268, 3 min 27)
     * - (easy-sat-133, 720, 18 sec)
     */
    @Test
    fun satTestOneFormulaAllAssignments() {
        //solver.deactivateDebugMode()
        solver.findAllAssignments()
        val cnf = parser.parseDIMACSFile(File("src/test/resources/cnfsolvertests/sat/easy-sat-133.cnf"))
        val assignments = solver.cdcl(cnf)
        //printDeterminedAssignments(assignments)
        assertTrue(assignments.isNotEmpty())
    }

    @Test
    fun satTestFormulasSolvedUnderTwoSec() {
        //solver.deactivateDebugMode()
        solver.findAllAssignments()
        val indexesOfFiles = mutableListOf<String>()
        for(x in 1..9) {
            indexesOfFiles.add("00$x")
        }
        for(x in 10..49) {
            indexesOfFiles.add("0$x")
        }
        indexesOfFiles.add("060")
        for(x in 64..99) {
            indexesOfFiles.add("0$x")
        }
        for(x in 100..106) {
            indexesOfFiles.add("$x")
        }
        for(x in 109..118) {
            indexesOfFiles.add("$x")
        }
        for(x in 120..123) {
            indexesOfFiles.add("$x")
        }
        for(index in indexesOfFiles) {
            val cnf = parser.parseDIMACSFile(File("src/test/resources/cnfsolvertests/sat/easy-sat-$index.cnf"))
            val assignments = solver.cdcl(cnf)
            assert(assignments.isNotEmpty())
        }
    }

    @Test
    fun unsatTestOneFormula() {
        //solver.activateDebugMode()
        val cnf = parser.parseDIMACSFile(File("src/test/resources/cnfsolvertests/unsat/easy-unsat-001.cnf"))
        val assignments = solver.cdcl(cnf)
        assertTrue(assignments.isEmpty())
    }

    /**
     * Use a set of files that contain satisfiable CNFs in the DIMACS format to test the CDCL algorithm.
     * It searches for only one satisfying assignment (i.e., if the formula is satisfiable).
     */
    @Test
    fun satTestOnlyOneAssignment() {
        //solver.deactivateDebugMode()
        solver.findOnlyOneAssignment()
        var cnf : List<Set<Int>>
        var assignments : Set<Set<Int>>
        val userDir = System.getProperty("user.dir")
        File("$userDir/src/test/resources/cnfsolvertests/sat/").walk().forEach {
            if(it.isFile) {
                //println(it)
                cnf = parser.parseDIMACSFile(it)
                assignments = solver.cdcl(cnf)
                assert(assignments.size == 1)
                //println("For $it CDCL determined ${assignments.size} assignments")
            }
        }
    }

    /**
     * Use a set of files that contain satisfiable CNFs in the DIMACS format to test the CDCL algorithm.
     * It searches for all satisfying assignments.
     */
    @Disabled("Reason: Finding all solutions of the 133 test files takes too long")
    @Test
    fun satTestAllAssignments() {
        //solver.deactivateDebugMode()
        solver.findAllAssignments()
        var cnf : List<Set<Int>>
        var assignments : Set<Set<Int>>
        val userDir = System.getProperty("user.dir")
        File("$userDir/src/test/resources/cnfsolvertests/sat/").walk().forEach {
            if(it.isFile) {
                //println(it)
                cnf = parser.parseDIMACSFile(it)
                assignments = solver.cdcl(cnf)
                assert(assignments.isNotEmpty())
                //println("For $it CDCL determined ${assignments.size} assignments")
            }
        }
    }

    /**
     * Use a set of files that contain unsatisfiable CNFs in the DIMACS format to test the CDCL algorithm
     */
    @Test
    fun unsatTest() {
        //solver.deactivateDebugMode()
        var cnf : List<Set<Int>>
        var assignments : Set<Set<Int>>
        val userDir = System.getProperty("user.dir")
        File("$userDir/src/test/resources/cnfsolvertests/unsat/").walk().forEach {
            if(it.isFile) {
                //println(it)
                cnf = parser.parseDIMACSFile(it)
                assignments = solver.cdcl(cnf)
                //println("For $it CDCL determined ${assignments.size} assignments")
                assert(assignments.isEmpty())
            }
        }
    }

    private fun printDeterminedAssignments(assignments: Set<Set<Int>>) {
        println("Determined assignments: ${assignments.size}")
        for((i, asg) in assignments.withIndex()) {
            println("Assignment $i: $asg")
        }
    }

    /**
     * Parser to transform the CNF contained in a file in DIMACS format into a CNF represented as a list of set of integers.
     * The sets represent the clause and integers represent the literals.
     */
    private class DIMACSParser {

        /**
         * Transform the CNF contained in [file] into a CNF represented as a list of set of integers.
         * The sets represent the clause and integers represent the literals.
         *
         * @param file file in DIMACS format containing a CNF
         * @return CNF represented as a list of set of integers. The sets represent the clause and integers represent the literals.
         */
        fun parseDIMACSFile(file: File) : List<Set<Int>> {

            val lines = file.bufferedReader().readLines().toMutableList()
            var firstLineFound = false
            var firstLine = ""
            var amountOfClauses = 0

            fun String.removeUnnecessaryWhitespaces() = replace(Regex("\\s+"), " ")

            while(!firstLineFound) {
                firstLine = lines.removeFirst()
                when(firstLine.first()) {
                    // comment line
                    'c' -> {
                        continue
                    }
                    // problem line
                    'p' -> {
                        firstLineFound = true
                        val problemDescription = firstLine.trim().removeUnnecessaryWhitespaces().split(" ")
                        if(problemDescription.size != 4) {
                            throw RuntimeException("The problem line of the file does not contain four parameters (p, cnf, <amount of variables>, <amount of clauses>)")
                        }
                        amountOfClauses = problemDescription[3].toInt()
                    }
                    else -> {
                        throw RuntimeException("One of the lines in front of the formula neither start with 'c' nor with 'p', and therefore does not fit the DIMACS format!")
                    }
                }
            }

            if(firstLine.isEmpty()) {
                throw RuntimeException("Problem line of the file not found. Maybe it does not fit the DIMACS format.")
            }

            // check the assumption that all lines contain exactly one clause, which is delimited at the end with an '0'
            for(line in lines) {
                if(line.isEmpty()) {
                    continue
                }
                var amountOf0 = 0
                val numsInLine = line.trim().removeUnnecessaryWhitespaces().split(" ")
                for(num in numsInLine) {
                    if(num.toInt() == 0) {
                        amountOf0++
                    }
                }
                if(amountOf0 != 1 || line.last() != '0') {
                    throw RuntimeException("The assumption that all lines contain exactly one clause, which is delimited at the end with an '0', is not correct.")
                }
            }
            if(lines.size != amountOfClauses) {
                throw RuntimeException("The assumption that all lines contain exactly one clause, which is delimited at the end with an '0', is not correct.")
            }

            val cnf = emptyList<Set<Int>>().toMutableList()
            for(line in lines) {
                if(line.isEmpty()) {
                    continue
                }
                val literalsAsStrings = line.trim().removeUnnecessaryWhitespaces().split(" ")
                val literals = emptySet<Int>().toMutableSet()
                for(literalString in literalsAsStrings) {
                    val literal = literalString.toInt()
                    // 0 separates the clauses
                    if(literal == 0) {
                        break
                    } else {
                        literals.add(literal)
                    }
                }
                cnf.add(literals)
            }

            return cnf
        }
    }

}
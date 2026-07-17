package constraintnettests

import com.github.tukcps.sysmd.cspsolver.normalizer.CDCL
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@Ignore
class CDCLTests {

    private val solver : CDCL = CDCL()
    private val parser : DIMACSParser = DIMACSParser()

    @Test
    fun simpleConjunction() {
        solver.findAllAssignments()
        val cnf = listOf(setOf(1), setOf(2))
        val assignments = solver.cdcl(cnf)
        assertTrue(assignments.size == 1)
    }

    @Test
    fun simpleDisjunction() {
        solver.findAllAssignments()
        val cnf = listOf(setOf(8, 10))
        val assignments = solver.cdcl(cnf)
        assertTrue(assignments.size >= 2)
    }

    @Test
    fun satTestOneFormulaAllAssignments() {
        solver.findAllAssignments()
        val cnf = parser.parseDIMACSFile(File("src/test/resources/cnfsolvertests/sat/easy-sat-133.cnf"))
        val assignments = solver.cdcl(cnf)
        assertTrue(assignments.isNotEmpty())
    }

    @Test
    fun satTestFormulasSolvedUnderTwoSec() {
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
        val cnf = parser.parseDIMACSFile(File("src/test/resources/cnfsolvertests/unsat/easy-unsat-001.cnf"))
        val assignments = solver.cdcl(cnf)
        assertTrue(assignments.isEmpty())
    }

    @Test
    fun satTestOnlyOneAssignment() {
        solver.findOnlyOneAssignment()
        var cnf : List<Set<Int>>
        var assignments : Set<Set<Int>>
        val userDir = System.getProperty("user.dir")
        File("$userDir/src/test/resources/cnfsolvertests/sat/").walk().forEach {
            if(it.isFile) {
                cnf = parser.parseDIMACSFile(it)
                assignments = solver.cdcl(cnf)
                assert(assignments.size == 1)
            }
        }
    }

    @Ignore
    @Test
    fun satTestAllAssignments() {
        solver.findAllAssignments()
        var cnf : List<Set<Int>>
        var assignments : Set<Set<Int>>
        val userDir = System.getProperty("user.dir")
        File("$userDir/src/test/resources/cnfsolvertests/sat/").walk().forEach {
            if(it.isFile) {
                cnf = parser.parseDIMACSFile(it)
                assignments = solver.cdcl(cnf)
                assert(assignments.isNotEmpty())
            }
        }
    }

    @Test
    fun unsatTest() {
        var cnf : List<Set<Int>>
        var assignments : Set<Set<Int>>
        val userDir = System.getProperty("user.dir")
        File("$userDir/src/test/resources/cnfsolvertests/unsat/").walk().forEach {
            if(it.isFile) {
                cnf = parser.parseDIMACSFile(it)
                assignments = solver.cdcl(cnf)
                assert(assignments.isEmpty())
            }
        }
    }

    private class DIMACSParser {

        fun parseDIMACSFile(file: File) : List<Set<Int>> {
            val lines = file.bufferedReader().readLines().toMutableList()
            var firstLineFound = false
            var firstLine = ""
            var amountOfClauses = 0

            fun String.removeUnnecessaryWhitespaces() = replace(Regex("\\s+"), " ")

            while(!firstLineFound) {
                firstLine = lines.removeFirst()
                when(firstLine.first()) {
                    'c' -> continue
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

            for(line in lines) {
                if(line.isEmpty()) continue
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
                if(line.isEmpty()) continue
                val literalsAsStrings = line.trim().removeUnnecessaryWhitespaces().split(" ")
                val literals = emptySet<Int>().toMutableSet()
                for(literalString in literalsAsStrings) {
                    val literal = literalString.toInt()
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

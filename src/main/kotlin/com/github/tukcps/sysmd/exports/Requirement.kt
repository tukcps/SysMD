package com.github.tukcps.sysmd.exports

import java.io.File

class Requirement(val requirementName: String, private val dut: String, val fullQualifiedName : String) {

    var generateTB = false
    var existsTB  = false
    val constraints : MutableList<Constraint> = mutableListOf()
    val invariants : MutableList<Invariant> = mutableListOf()

    fun writeTestBench(tbDestination: String) {

        File("$tbDestination/${requirementName}_TB.cpp").printWriter().use{ out ->

            out.println("#include <systemc.h>\n" +
                    "#include <systemc-ams.h>\n" +
                    "#include \"../modules/$dut.h\"\n" +
                    "#include \"ResultWriter.cpp\"\n\n" +
                    "int sc_main(int argc, char* argv[]){"
            )

            out.println("\n\t// --- INVARIANTS ---")
            out.println("\t//The following conditions must always hold through the whole simulation:")
            this.invariants.forEachIndexed { idx, invariant ->
                out.println("\t//Invariant ${idx+1}: ${invariant.invariantName} -> ${
                    StringBuilder(invariant.invariantString).replace("\n".toRegex(),"\n\t//")}")
            }

            out.println("\n\t// --- REQUIREMENTS ---")
            out.println("\t//DUT: $dut")
            this.constraints.forEach { constraint ->
                out.println("\t//Requirement ${constraint.constraintName} requires that ${constraint.statement}")
            }

            out.println("\n\n\t// --- STIMULI ---\n" +
                    "\t//Here you define Signals and Inputs that are used as stimuli for the DUT\n")

            out.println("\n\n\t// --- DUT ---\n" +
                    "\t//This is the device that is under test\n")

            out.print("\t$dut dut(\"dut\");")

            out.println("\n\n\t// --- MONITORING and POST-PROCESSING ---\n" +
                    "\t//Monitor relevant signals and calculate results\n")

            out.println("\n\n\t// --- RESULT WRITING ---\n" +
                    "\t//Feed your results to the ResultWriter so that they can be imported in the SysMD Notebook.\n" +
                    "\n\tResultWriter rw;")

            this.constraints.forEach { constraint ->
                out.println("\trw.addResult(\"${constraint.constraintName}\"," +
                        " PASTE_RESULT_HERE," +
                        " \"${constraint.unit}\"," +
                        " Operator::${constraint.operator}," +
                        " ${constraint.referenceValue}," +
                        " \"${constraint.referenceUnit}\"," +
                        " \"${constraint.attributeQUalifiedName}\");")
            }
            out.println("\trw.writeResultFile();\n\n")

            out.println("return 0;\n" +
                    "}")

        }
    }


}

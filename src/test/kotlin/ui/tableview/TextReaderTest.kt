package ui.tableview

import com.github.tukcps.sysmd.ui.tableview.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextReaderTest {
    
    @Test
    fun testTextReader() {
        var matchLine = 0
        
        val tr = TextReader(
            """
        package Amp_Pipecleaner {
            import ScalarValues::*;
            import SI::*;
            
            // Library instances --> SystemC classes
            part def Amplifier isA Base::Anything {
                attribute gain: Real [dB] = [0.0 .. 100.0] dB;
            }
            
            // Concrete model --> SystemC instances of library classes
            part myAmplifier {
                part lna: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;
                    }
                    out port output{
                       attribute value: ScalarValues::Real;
                    }
                    attribute gain: Real [dB] = [15.0 .. 20.0] dB;
                }
                
                part stage2: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;
                    }
                    out port output{
                       attribute value: ScalarValues::Real;
                    }
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB;
                }
                
                part driver: Amplifier {
                    in port input{
                       attribute value: ScalarValues::Real;
                    }
                    out port output{
                       attribute value: ScalarValues::Real;
                    }
                    attribute gain: Real [dB] = [5.0 .. 20.0] dB;
                }
                
                attribute total_gain: Real(20 .. 30) [dB] = productOverParts(gain);
            }
            
            attribute ambientTemperature : Real [°C] = [-15.0 .. 40.0] °C;
            
            assoc Signal;
            interface lna_to_stage2 : Signal connect Amp_Pipecleaner::myAmplifier::lna::output to Amp_Pipecleaner::myAmplifier::stage2::input;
            interface stage2_to_driver : Signal connect Amp_Pipecleaner::myAmplifier::stage2::output to Amp_Pipecleaner::myAmplifier::driver::input;
            
            requirement PipeCleaner_Requirements {
                subject f references myAmplifier;
                
                assert ambientTempRange { (ambientTemperature >= -15.0 [°C]) }
                    // The code for creating Constraint in exporter has a bug:
                    // it assumes assertions of kind Real (OP) Real, but this is then:
                    //   Real OP Real AND Real OP real which has a root with two child of type boolean
                    //   that have leaves of type Real !!!
                    //   --> Tree would have to be traversed completely
                    // &  (ambientTemperature <= 40.0 [°C]) }
                
                require outputGain {
                    f::total_gain >= 2.0
                }
            }
            
        }
            """.trimIndent()
        )
        sequenceOf(
            // package Amp_Pipecleaner {
            OutputMatcher(kw = "package", name = "Amp_Pipecleaner", eol = "{", braces = 1 to 0),
            // import ScalarValues::*;
            OutputMatcher(kw = "import", name = "ScalarValues::*", eol = ";"),
            // import SI::*;
            OutputMatcher(kw = "import", name = "SI::*", eol = ";"),
            // // Library instances --> SystemC classes
            OutputMatcher(isEmpty = true),
            // part def Amplifier isA Base::Anything {
            OutputMatcher(kw = "part", def = true, name = "Amplifier", rel = "isA", super_ = "Base::Anything", eol = "{", braces = 1 to 0),
            //     attribute gain: Real [dB] = [0.0 .. 100.0] dB;
            OutputMatcher(
                
                kw = "attribute",
                name = "gain",
                rel = ":",
                super_ = "Real",
                unit = "[dB]",
                at = "=",
                vr = "[0.0 .. 100.0]",
                min_max = "0.0" to "100.0",
                eol = ";"
            ),
            // }
            OutputMatcher(eol = "}", braces = 0 to 1),
            // // Concrete model --> SystemC instances of library classes
            OutputMatcher(isEmpty = true),
            // part myAmplifier {
            OutputMatcher(kw = "part", name = "myAmplifier", eol = "{", braces = 1 to 0),
            //     part lna: Amplifier {
            OutputMatcher(kw = "part", name = "lna", rel = ":", super_ = "Amplifier", eol = "{", braces = 1 to 0),
            //         in port input{
            OutputMatcher(flags = "in", kw = "port", name = "input", eol = "{", braces = 1 to 0),
            //         attribute value: ScalarValues::Real;
            OutputMatcher(kw = "attribute", name = "value", rel = ":", super_ = "ScalarValues::Real", eol = ";"),
            //     }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //         out port output{
            OutputMatcher(flags = "out", kw = "port", name = "output", eol = "{", braces = 1 to 0),
            //             attribute value: ScalarValues::Real;
            OutputMatcher(kw = "attribute", name = "value", rel = ":", super_ = "ScalarValues::Real", eol = ";"),
            //         }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //         attribute gain: Real [dB] = [15.0 .. 20.0] dB;
            OutputMatcher(
                kw = "attribute",
                name = "gain",
                rel = ":",
                super_ = "Real",
                unit = "[dB]",
                at = "=",
                vr = "[15.0 .. 20.0]",
                min_max = "15.0" to "20.0",
                eol = ";"
            ),
            //     }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //     part stage2: Amplifier {
            OutputMatcher(kw = "part", name = "stage2", rel = ":", super_ = "Amplifier", eol = "{", braces = 1 to 0),
            //         in port input{
            OutputMatcher(flags = "in", kw = "port", name = "input", eol = "{", braces = 1 to 0),
            //         attribute value: ScalarValues::Real;
            OutputMatcher(kw = "attribute", name = "value", rel = ":", super_ = "ScalarValues::Real", eol = ";"),
            //     }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //         out port output{
            OutputMatcher(flags = "out", kw = "port", name = "output", eol = "{", braces = 1 to 0),
            //             attribute value: ScalarValues::Real;
            OutputMatcher(kw = "attribute", name = "value", rel = ":", super_ = "ScalarValues::Real", eol = ";"),
            //         }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //         attribute gain: Real [dB] = [5.0 .. 20.0] dB;
            OutputMatcher(
                kw = "attribute",
                name = "gain",
                rel = ":",
                super_ = "Real",
                unit = "[dB]",
                at = "=",
                vr = "[5.0 .. 20.0]",
                min_max = "5.0" to "20.0",
                eol = ";"
            ),
            //     }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //     part driver: Amplifier {
            OutputMatcher(kw = "part", name = "driver", rel = ":", super_ = "Amplifier", eol = "{", braces = 1 to 0),
            //         in port input{
            OutputMatcher(flags = "in", kw = "port", name = "input", eol = "{", braces = 1 to 0),
            //         attribute value: ScalarValues::Real;
            OutputMatcher(kw = "attribute", name = "value", rel = ":", super_ = "ScalarValues::Real", eol = ";"),
            //     }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //         out port output{
            OutputMatcher(flags = "out", kw = "port", name = "output", eol = "{", braces = 1 to 0),
            //             attribute value: ScalarValues::Real;
            OutputMatcher(kw = "attribute", name = "value", rel = ":", super_ = "ScalarValues::Real", eol = ";"),
            //         }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //         attribute gain: Real [dB] = [5.0 .. 20.0] dB;
            OutputMatcher(
                kw = "attribute",
                name = "gain",
                rel = ":",
                super_ = "Real",
                unit = "[dB]",
                at = "=",
                vr = "[5.0 .. 20.0]",
                min_max = "5.0" to "20.0",
                eol = ";"
            ),
            //     }
            OutputMatcher(eol = "}", braces = 0 to 1),
            //     attribute total_gain: Real(20 .. 30) [dB] = productOverParts(gain);
            OutputMatcher(
                kw = "attribute",
                name = "total_gain",
                rel = ":",
                super_ = "Real",
                const = "(20 .. 30)",
                unit = "[dB]",
                at = "=",
                v = "productOverParts(gain)",
                eol = ";"
            ),
            // }
            OutputMatcher(eol = "}", braces = 0 to 1),
            // attribute ambientTemperature : Real [°C] = [-15.0 .. 40.0] °C;
            OutputMatcher(
                kw = "attribute",
                name = "ambientTemperature",
                rel = ":",
                super_ = "Real",
                unit = "[°C]",
                at = "=",
                vr = "[-15.0 .. 40.0]",
                eol = ";"
            ),
            // assoc Signal;
            OutputMatcher(kw = "assoc", name = "Signal", eol = ";"),
            // interface lna_to_stage2 : Signal connect Amp_Pipecleaner::myAmplifier::lna::output to Amp_Pipecleaner::myAmplifier::stage2::input;
            OutputMatcher(
                kw = "interface",
                name = "lna_to_stage2",
                rel = ":",
                super_ = "Signal",
                dirCon = true,
                con1 = "Amp_Pipecleaner::myAmplifier::lna::output",
                to = "to",
                con2 = "Amp_Pipecleaner::myAmplifier::stage2::input",
                eol = ";"
            ),
            // interface stage2_to_driver : Signal connect Amp_Pipecleaner::myAmplifier::stage2::output to Amp_Pipecleaner::myAmplifier::driver::input;
            OutputMatcher(
                kw = "interface",
                name = "stage2_to_driver",
                rel = ":",
                super_ = "Signal",
                dirCon = true,
                con1 = "Amp_Pipecleaner::myAmplifier::stage2::output",
                to = "to",
                con2 = "Amp_Pipecleaner::myAmplifier::driver::input",
                eol = ";"
            ),
            // requirement PipeCleaner_Requirements {
            OutputMatcher(kw = "requirement", name = "PipeCleaner_Requirements", eol = "{", braces = 1 to 0),
            //     subject f references myAmplifier;
            OutputMatcher(kw = "subject", name = "f", rel = "references", super_ = "myAmplifier", eol = ";"),
            //     assert ambientTempRange { (ambientTemperature >= -15.0 [°C]) }
            OutputMatcher(kw = "assert", name = "ambientTempRange", be = "(ambientTemperature >= -15.0 [°C])", eol = "}", braces = 1 to 1),
            //     // The code for creating Constraint in exporter has a bug:
            OutputMatcher(isEmpty = true),
            //     // it assumes assertions of kind Real (OP) Real, but this is then:
            OutputMatcher(isEmpty = true),
            //     //   Real OP Real AND Real OP real which has a root with two child of type boolean
            OutputMatcher(isEmpty = true),
            //     //   that have leaves of type Real !!!
            OutputMatcher(isEmpty = true),
            //     //   --> Tree would have to be traversed completely
            OutputMatcher(isEmpty = true),
            //     // &  (ambientTemperature <= 40.0 [°C]) }
            OutputMatcher(isEmpty = true),
            //     require outputGain { f::total_gain >= 2.0 }
            OutputMatcher(kw = "require", name = "outputGain", be = "f::total_gain >= 2.0", eol = "}", braces = 1 to 1),
            //}
            OutputMatcher(eol = "}", braces = 0 to 1),
            //}
            OutputMatcher(eol = "}", braces = 0 to 1)
        ).forEach {
            matchLine++
            tr.next
            it.doIt(matchLine, tr)
        }
    }
    
     private class OutputMatcher(
        val line: String? = null,
        val isEmpty: Boolean = false,
        val def: Boolean = false,
        val kw: String? = null,
        val name: String? = null,
        val mult: String? = null,
        val rel: String? = null,
        val super_: String? = null,
        val vr: String? = null,
        val min_max: Pair<String, String>? = null,
        val v: String? = null,
        val at: String? = null,
        val unit: String? = null,
        val const: String? = null,
        val eol: String? = null,
        val con0: String? = null,
        val con1: String? = null,
        val con2: String? = null,
        val uc: Boolean = false,
        val to: String? = null,
        val dirCon: Boolean? = null,
        val flags: String? = null,
        val be: String? = null,
        val braces: Pair<Int, Int> = 0 to 0,
    ) {
        fun doIt(matchLine: Int, tr: TextReader) {
            if (line.notNull()) assertEquals(line, tr.line?.trim(), "Match $matchLine for line: '${tr.line}' failed due to line:")
            if (isEmpty.notNull()) assertEquals(isEmpty, tr.isEmpty, "Match $matchLine for line: '${tr.line}' failed due to isEmpty:")
            if (def.notNull()) assertEquals(def, tr.def, "Match $matchLine for line: '${tr.line}' failed due to def:")
            if (kw.notNull()) assertEquals(kw, tr.kw?.trim(), "Match $matchLine for line: '${tr.line}' failed due to kw:")
            if (name.notNull()) assertEquals(name, tr.name?.trim(), "Match $matchLine for line: '${tr.line}' failed due to name:")
            if (mult.notNull()) assertEquals(mult, tr.mult?.trim(), "Match $matchLine for line: '${tr.line}' failed due to mult:")
            if (rel.notNull()) assertEquals(rel, tr.rel?.trim(), "Match $matchLine for line: '${tr.line}' failed due to rel:")
            if (super_.notNull()) assertEquals(super_, tr.super_?.trim(), "Match $matchLine for line: '${tr.line}' failed due to super_:")
            if (vr.notNull()) assertEquals(vr, tr.vr?.trim(), "Match $matchLine for line: '${tr.line}' failed due to vr:")
            if (min_max.notNull()) assertEquals(min_max, tr.min_max, "Match $matchLine for line: '${tr.line}' failed due to min_max:")
            if (v.notNull()) assertEquals(v, tr.v?.trim(), "Match $matchLine for line: '${tr.line}' failed due to v:")
            if (at.notNull()) assertEquals(at, tr.at?.trim(), "Match $matchLine for line: '${tr.line}' failed due to at:")
            if (unit.notNull()) assertEquals(unit, tr.unit?.trim(), "Match $matchLine for line: '${tr.line}' failed due to unit:")
            if (const.notNull()) assertEquals(const, tr.const?.trim(), "Match $matchLine for line: '${tr.line}' failed due to const:")
            if (eol.notNull()) assertEquals(eol, tr.eol?.trim(), "Match $matchLine for line: '${tr.line}' failed due to eol:")
            if (con0.notNull()) assertEquals(con0, tr.con0?.trim(), "Match $matchLine for line: '${tr.line}' failed due to con0:")
            if (con1.notNull()) assertEquals(con1, tr.con1?.trim(), "Match $matchLine for line: '${tr.line}' failed due to con1:")
            if (con2.notNull()) assertEquals(con2, tr.con2?.trim(), "Match $matchLine for line: '${tr.line}' failed due to con2:")
            if (uc.notNull()) assertEquals(uc, tr.uc, "Match $matchLine for line: '${tr.line}' failed due to uc:")
            if (to.notNull()) assertEquals(to, tr.to?.trim(), "Match $matchLine for line: '${tr.line}' failed due to to:")
            if (dirCon.notNull()) assertEquals(dirCon, tr.dirCon, "Match $matchLine for line: '${tr.line}' failed due to dirCon:")
            if (flags.notNull()) assertEquals(flags, tr.flags?.trim(), "Match $matchLine for line: '${tr.line}' failed due to flags:")
            if (be.notNull()) assertEquals(be, tr.be?.trim(), "Match $matchLine for line: '${tr.line}' failed due to be:")
            if (braces.notNull()) assertEquals(braces, tr.braces, "Match $matchLine for line: '${tr.line}' failed due to braces:")
        }
    }
}
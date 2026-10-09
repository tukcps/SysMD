package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import util.*
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import kotlin.test.Test

class InvariantTests {

    @Test
    fun restrictInteger1() = testSession("Ranges") {
        loadSysMLv2("""    
            attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
            assert constraint r { weight >= 30 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(30L .. 50L, solver.variable("weight"))
    }

    @Test
    fun restrictInteger1b() = testSession("Ranges") {
        loadSysMLv2("""    
            attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
            assert constraint r { 30 >= weight }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 30L, solver.variable("weight"))
    }

    @Test
    fun restrictInteger2() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight > 30 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(31L .. 50L, solver.variable("weight"))
    }

    @Test
    fun restrictInteger2b() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { 30 > weight }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 29L, solver.variable("weight"))
    }

    @Test
    fun restrictInteger3() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight <= 30 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 30L, solver.variable("weight"))
    }

    @Test
    fun restrictInteger3a() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { 30 <= weight }
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(30L .. 50L, solver.variable("weight"))
    }

    @Test
    fun restrictInteger4() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight < 30 }
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(0L .. 29L, solver.variable("weight"))
    }

    @Test
    fun restrictInteger4a() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { 30 < weight }
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(31L .. 50L, solver.variable("weight"))
    }

    @Test
    fun restrictIntegerEmpty() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight <= -10 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEmpty(solver.variable("weight"))
    }

    @Test
    fun restrictIntegerEmpty1() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight < -10 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEmpty(solver.variable("weight"))
    }

    @Test
    fun restrictIntegerEmpty2() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight >= 60 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEmpty(solver.variable("weight"))
    }

    @Test
    fun restrictIntegerEmpty3() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight > 60 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEmpty(solver.variable("weight"))
    }

    @Test
    fun assertTestIntDiv() = testSession("Ranges") {
        loadSysMLv2("""
            attribute f: Ranges::IntegerInRange = oneOf(1 .. 4); 
            assert constraint ass { 12 / f < 6 } 
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(2L..4L, solver.variable("f"))
    }

    @Test
    fun assertTestIntMultiplication() = testSession("Ranges") {
        loadSysMLv2("""
            attribute f: Ranges::IntegerInRange = oneOf(1 .. 4); 
            assert constraint ass { 12 * f > 24 } 
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(2L..4L, solver.variable("f"))
    }

    @Test
    fun restrictReal1() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..50;}
            inv r { weight <= 30.0 }
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0..30.0, solver.variable("weight"))
    }

    @Test
    fun restrictReal2() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..50;} 
            inv r { weight >= 30.0 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(30.0 .. 50.0, solver.variable("weight"))
    }

    @Test  // Problem with evalDown of Requirement vs. Expression
    fun restrictReal2a() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..100;} 
            feature weight2: Ranges::RealInRange = weight/2.0;
            inv r { weight2 >= 30.0 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(60.0 .. 100.0, solver.variable("weight"))
    }

    @Test // Problem with evalDown of ScalarValues::Requirement vs. Expression
    fun restrictReal3() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = 0..50;}
                inv r { weight > 30.0 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(30.0 .. 50.0, solver.variable("weight"))
    }

    @Test
    fun restrictReal4() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..50;}
            inv r { weight < 30.0 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(0.0 .. 30.0, solver.variable("weight"))
    }

    @Test
    fun assertTestReal() = testSession("Ranges") {
        loadKerML("""   
            feature a: Ranges::RealInRange {:>> range = 1..5;}
            feature b: Ranges::RealInRange {:>> range = 4..6;}
            inv c { a == b }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(4.0 .. 5.0, solver.variable("a"))
        assertBounds(4.0 .. 5.0, solver.variable("b"))
    }

    @Test
    fun evalUpITEEquality() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer = 4;
            feature b: ScalarValues::Integer = 5;
            feature b1: ScalarValues::Boolean = a == 5;
            feature b2: ScalarValues::Boolean = a == 4;
            feature c: ScalarValues::Integer = if b == 6 ? 7 else 6;
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(6L .. 6L, solver.variable("c"))
    }

    @Test
    fun evalUpRealITEEquality() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Real = 4.0;
            feature b: ScalarValues::Real = 5.0;
            feature b1: ScalarValues::Boolean = a == 5.0;
            feature b2: ScalarValues::Boolean = a == 4.0;
            feature c: ScalarValues::Real = if b == 6.0 ? 7.0 else 6.0; 
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        assertBounds(6.0 .. 6.0, solver.variable("c"))
    }

    @Test
    fun evalDownEquality() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer; 
            feature b: ScalarValues::Integer = 5; 
            inv { a == b } 
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(5L, solver.variable("b"))
        assertBounds(5L, solver.variable("a"))
    }

    @Test
    fun restrictRealMultiplication() = testSession("Ranges") {
        loadSysMLv2("""    
            private import ScalarValues::*;
            attribute result: Real = 1.0..4.0 * 1.0..2.0;
            assert constraint range {result<=2.0}
        """, Runlevel.SOLVED)
        solver.propagate()
        assertNoIssues()
        val resultVar = solver.variable("result")
        assertBounds(1.0 .. 2.0, resultVar)
    }

    @Test
    fun restrictIntegerNegative() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = -50..0; }
                assert constraint r { weight <= -30 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-50L .. -30L, solver.variable("weight"))
    }

    @Test
    fun restrictRealNegative() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = -50.0..0.0; } 
                inv r { weight >= -30.0 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertBounds(-30.0 .. 0.0, solver.variable("weight"))
    }
}

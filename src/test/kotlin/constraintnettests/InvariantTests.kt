package constraintnettests

import com.github.tukcps.sysmd.services.Runlevel
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals

class InvariantTests {

    @Test
    fun restrictInteger1() = testSession("Ranges") {
        loadSysMLv2("""    
            attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
            assert constraint r { weight >= 30 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(30L, solver.getVariable("weight")!!.min())
        assertEquals(50L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictInteger1b() = testSession("Ranges") {
        loadSysMLv2("""    
            attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
            assert constraint r { 30 >= weight }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0L, solver.getVariable("weight")!!.min())
        assertEquals(30L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictInteger2() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight > 30 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(31L, solver.getVariable("weight")!!.min())
        assertEquals(50L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictInteger2b() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { 30 > weight }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0L, solver.getVariable("weight")!!.min())
        assertEquals(29L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictInteger3() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight <= 30 }
        """, Runlevel.ALL)
        solver.propagate()
        assertNoIssues()
        assertEquals(0L, solver.getVariable("weight")!!.min())
        assertEquals(30L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictInteger3a() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { 30 <= weight }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEquals(30L, solver.getVariable("weight")!!.min())
        assertEquals(50L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictInteger4() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight < 30 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEquals(0L, solver.getVariable("weight")!!.min())
        assertEquals(29L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictInteger4a() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { 30 < weight }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEquals(31L, solver.getVariable("weight")!!.min())
        assertEquals(50L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictIntegerEmpty() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight <= -10 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assert(solver.getVariable("weight")!!.vectorQuantity.value.asIdd().isEmpty())
    }

    @Test
    fun restrictIntegerEmpty1() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight < -10 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assert(solver.getVariable("weight")!!.vectorQuantity.value.asIdd().isEmpty())
    }

    @Test
    fun restrictIntegerEmpty2() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight >= 60 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assert(solver.getVariable("weight")!!.vectorQuantity.value.asIdd().isEmpty())
    }

    @Test
    fun restrictIntegerEmpty3() = testSession("Ranges") {
        loadSysMLv2("""    
                attribute weight: Ranges::IntegerInRange {:>> range = 0..50;}
                assert constraint r { weight > 60 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assert(solver.getVariable("weight")!!.vectorQuantity.value.asIdd().isEmpty())
    }

    @Test
    fun assertTestIntDiv() = testSession("Ranges") {
        loadSysMLv2("""
            attribute f: Ranges::IntegerInRange = oneOf(1 .. 4); 
            assert constraint ass { 12 / f < 6 } 
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEquals(2, solver.getVariable("f")!!.idd().getRange().min)
        assertEquals(4, solver.getVariable("f")!!.idd().getRange().max)
    }

    @Test
    fun assertTestIntMultiplication() = testSession("Ranges") {
        loadSysMLv2("""
            attribute f: Ranges::IntegerInRange = oneOf(1 .. 4); 
            assert constraint ass { 12 * f > 24 } 
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEquals(2, solver.getVariable("f")!!.idd().getRange().min)
        assertEquals(4, solver.getVariable("f")!!.idd().getRange().max)
    }

    @Test
    fun restrictReal1() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..50;}
            inv r { weight <= 30.0 }
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEquals(0.0, solver.getVariable("weight")!!.min(), 0.00001)
        assertEquals(30.0, solver.getVariable("weight")!!.max(), 0.00001)
    }

    @Test
    fun restrictReal2() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..50;} 
            inv r { weight >= 30.0 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(30.0, solver.getVariable("weight")!!.min(), 0.00001)
        assertEquals(50.0, solver.getVariable("weight")!!.max(), 0.00001)
    }

    @Test  // Problem with evalDown of Requirement vs. Expression
    fun restrictReal2a() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..100;} 
            feature weight2: Ranges::RealInRange = weight/2.0;
            inv r { weight2 >= 30.0 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(60.0, solver.getVariable("weight")!!.min(), 0.00001)
        assertEquals(100.0, solver.getVariable("weight")!!.max(), 0.00001)
    }

    @Test // Problem with evalDown of ScalarValues::Requirement vs. Expression
    fun restrictReal3() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = 0..50;}
                inv r { weight > 30.0 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(30.0, solver.getVariable("weight")!!.min(), 0.00001)
        assertEquals(50.0, solver.getVariable("weight")!!.max(), 0.00001)
    }

    @Test
    fun restrictReal4() = testSession("Ranges") {
        loadKerML("""   
            feature weight: Ranges::RealInRange {:>> range = 0..50;}
            inv r { weight < 30.0 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(0.0, solver.getVariable("weight")!!.min(), 0.00001)
        assertEquals(30.0, solver.getVariable("weight")!!.max(), 0.00001)
    }

    @Test
    fun assertTestReal() = testSession("Ranges") {
        loadKerML("""   
            feature a: Ranges::RealInRange {:>> range = 1..5;}
            feature b: Ranges::RealInRange {:>> range = 4..6;}
            inv c { a == b }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(4.0, solver.getVariable("a")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("a")!!.max(), 0.00001)
        assertEquals(4.0, solver.getVariable("b")!!.min(), 0.00001)
        assertEquals(5.0, solver.getVariable("b")!!.max(), 0.00001)
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
        assertNoIssues()
        assertEquals(6L, solver.getVariable("c")!!.min())
        assertEquals(6L, solver.getVariable("c")!!.max())
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
        assertNoIssues()
        assertEquals(6.0, solver.getVariable("c")!!.aadd().getRange().min)
        assertEquals(6.0, solver.getVariable("c")!!.aadd().getRange().max)
    }

    @Test
    fun evalDownEquality() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer; 
            feature b: ScalarValues::Integer = 5; 
            inv { a == b } 
        """, Runlevel.SOLVED)
        assertNoIssues()
        assertEquals(5L, solver.getVariable("a")!!.min())
        assertEquals(5L, solver.getVariable("a")!!.max())
    }

    @Test
    fun restrictRealMultiplication() = testSession("Ranges") {
        loadSysMLv2("""    
            private import ScalarValues::*;
            attribute result: Real = 1.0..4.0 * 1.0..2.0;
            assert constraint range {result<=2.0}
        """, Runlevel.SOLVED)
        assertNoIssues()
        val resultVar = solver.getVariable("result")!!
        assertEquals(2.0, resultVar.max(), 0.00001)
        assertEquals(1.0, resultVar.min(), 0.00001)
    }

    @Test
    fun restrictIntegerNegative() = testSession("Ranges") {
        loadSysMLv2(
            """    
                attribute weight: Ranges::IntegerInRange {:>> range = -50..0; }
                assert constraint r { weight <= -30 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(-50L, solver.getVariable("weight")!!.min())
        assertEquals(-30L, solver.getVariable("weight")!!.max())
    }

    @Test
    fun restrictRealNegative() = testSession("Ranges") {
        loadKerML("""   
                feature weight: Ranges::RealInRange {:>> range = -50.0..0.0; } 
                inv r { weight >= -30.0 }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(-30.0, solver.getVariable("weight")!!.min(), 0.00001)
        assertEquals(0.0, solver.getVariable("weight")!!.max(), 0.00001)
    }
}

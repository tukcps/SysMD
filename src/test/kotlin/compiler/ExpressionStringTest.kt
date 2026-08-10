package compiler

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.Runlevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession

/**
 * Checks that the dependency string is correctly cut out of the SysMD string.
 */
class ExpressionStringTest {
    /**
     * The dot after an expression is not part of it.
     */
    @Test
    fun dependencyStringTest1() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues::*; 
            feature x2: Real = 1.0 + 2.0  ;
            feature x3: Real = 1.0 + 2.0;
            feature z: Real = 1.0 + 2.0;
        """, Runlevel.MODEL)
        assertNoIssues()
        val x2 = global.resolve("x2")?.member<Feature>()
        assertEquals("1.0 + 2.0", x2?.expression)
        val x3 = global.resolve("x3")?.member<Feature>()
        assertEquals("1.0 + 2.0", x3?.expression)
        val z = global.resolve("z")?.member<Feature>()
        assertEquals("1.0 + 2.0", z?.expression)
    }

    /**
     * The semicolon after an expression is not part of it.
     */
    @Test
    fun dependencyStringTest2() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Boolean = true;
            feature x2: ScalarValues::Boolean = true  ;
            feature x3: ScalarValues::Boolean = true
              ;
            feature z: ScalarValues::Boolean = true""", Runlevel.VARIABLES)
        val x = global.resolve("x")?.member<Feature>()
        assertEquals("true", x?.expression)
        val x2 = global.resolve("x2")?.member<Feature>()
        assertEquals("true", x2?.expression)
        val x3 = global.resolve("x3")?.member<Feature>()
        assertEquals("true", x3?.expression)
        val z = global.resolve("z")?.member<Feature>()
        assertEquals("true", z?.expression)
    }


    /**
     * The dot after an expression is not part of it.
     */
    @Test
    fun dependencyStringTest3() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Integer = 1 + 2 ;
            feature x2: ScalarValues::Integer = 1 + 2  ;
            feature x3: ScalarValues::Integer = 1 + 2
              ;
            feature z: ScalarValues::Integer = 1 + 2;""", Runlevel.VARIABLES)
        val x = global.resolve("x")?.member<Feature>() !!
        assertEquals("1 + 2", x.expression)
        val x2 = global.resolve("x2")?.member<Feature>() !!
        assertEquals("1 + 2", x2.expression)
        val x3 = global.resolve("x3")?.member<Feature>() !!
        assertEquals("1 + 2", x3.expression)
        val z = global.resolve("z")?.member<Feature>() !!
        assertEquals("1 + 2", z.expression)
    }

    /**
     * The comma after an expression is not part of it.
     */
    @Test
    fun dependencyStringTest4() = testSession("ScalarValues") {
        loadKerML("""
                feature x: ScalarValues::Integer = 1 + 2;
                feature x2: ScalarValues::Integer = 1 + 2  ;
                feature x3: ScalarValues::Integer = 1 + 2
                ;
                feature z: ScalarValues::Integer = 1 + 2""", Runlevel.VARIABLES)
        val x = global.resolve("x")?.member<Feature>()!!
        assertEquals("1 + 2", x.expression)
        val x2 = global.resolve("x2")?.member<Feature>()!!
        assertEquals("1 + 2", x2.expression)
        val x3 = global.resolve("x3")?.member<Feature>()!!
        assertEquals("1 + 2", x3.expression)
        val z = global.resolve("z")?.member<Feature>()!!
        assertEquals("1 + 2", z.expression)
    }

    /**
     * Special case: Dot after Integer Literal that ends statement.
     */
    @Test fun dotAfterIntegerDot() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Integer = 1.")
        val x = global.resolve("x")?.member<Feature>()!!
        assertEquals("1", x.expression)
    }
}
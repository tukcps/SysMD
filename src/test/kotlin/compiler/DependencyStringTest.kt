package compiler

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import util.testSession
import kotlin.test.assertTrue

/**
 * Checks that the dependency string is correctly cut out of the SysMD string.
 */
class DependencyStringTest {
    /**
     * The dot after an expression is not part of it.
     */
    @Test
    fun dependencyStringTest1() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues::*; 
            feature x: Real = 1.0 + 2.0.
            feature x2: Real = 1.0 + 2.0  ;
            feature x3: Real = 1.0 + 2.0;
            feature z: Real = 1.0 + 2.0;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val x = global.resolveVar("x") !!
        assertEquals("1.0 + 2.0", x.feature.expression)
        val x2 = global.resolveVar("x2") !!
        assertEquals("1.0 + 2.0", x2.feature.expression)
        val x3 = global.resolveVar("x3") !!
        assertEquals("1.0 + 2.0", x3.feature.expression)
        val z = global.resolveVar("z") !!
        assertEquals("1.0 + 2.0", z.feature.expression)
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
            feature z: Boolean = true""")
        val x = global.resolveVar("x") !!
        assertEquals("true", x.feature.expression)
        val x2 = global.resolveVar("x2") !!
        assertEquals("true", x2.feature.expression)
        val x3 = global.resolveVar("x3") !!
        assertEquals("true", x3.feature.expression)
        val z = global.resolveVar("z") !!
        assertEquals("true", z.feature.expression)
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
            feature z: ScalarValues::Integer = 1 + 2;""")
        val x = global.resolveVar("x") !!
        assertEquals("1 + 2", x.feature.expression)
        val x2 = global.resolveVar("x2") !!
        assertEquals("1 + 2", x2.feature.expression)
        val x3 = global.resolveVar("x3") !!
        assertEquals("1 + 2", x3.feature.expression)
        val z = global.resolveVar("z") !!
        assertEquals("1 + 2", z.feature.expression)
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
                feature z: ScalarValues::Integer = 1 + 2""".trimIndent())
        val x = global.resolve<Feature>("x") !!
        assertEquals("1 + 2", x.expression)
        val x2 = global.resolve<Feature>("x2") !!
        assertEquals("1 + 2", x2.expression)
        val x3 = global.resolve<Feature>("x3") !!
        assertEquals("1 + 2", x3.expression)
        val z = global.resolve<Feature>("z") !!
        assertEquals("1 + 2", z.expression)
    }

    /**
     * Special case: Dot after Integer Literal that ends statement.
     */
    @Test fun dotAfterIntegerDot() = testSession("ScalarValues") {
        loadKerML("feature x: ScalarValues::Integer = 1.")
        val x = global.resolve<Feature>("x") !!
        assertEquals("1", x.expression)
    }
}
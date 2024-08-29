package constraintnettests

import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.SysMDInfo
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AggregationFunctionTest {

    /**
     * Test: productOverParts(property) computes the PRODUCT of all
     */
    @Test
    fun astProductHasATest() = testSession {
        loadSysMD("""
            package l{
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(0.1..0.5).
            l::c3 hasA
                feature a: l::c1[1..2] ;    // 1..2 * 1..2 \n"
                feature b: l::c2[2..3];    // shall be 0 as no property p is not defined.
                feature p3: ScalarValues::Real = productOverParts(p);
                feature p4: ScalarValues::Real = productOverPartsNotTransitive(p).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.01, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.01, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATestEvalDown() = testSession {
        loadSysMD(
            """
            package l {
                class c1 {
                    attribute p: ScalarValues::Real(0.001..1.0).
                }
                class c2; 
                class c3 {
                    feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                    feature b: l::c2 [2..3];    // shall be 0 as no property p is defined.
                    attribute p3: ScalarValues::Real(0.1..25) = productOverParts(p); 
                }
            }"""
        )
        propagate()
        // val c3 = global.resolveName<Class>("l::c3")
        // val a = global.resolveName<Feature>("l::c3::a")
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.1, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATestInt() = testSession {
        loadSysMD(
            """
            package l {
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA
                attribute p: ScalarValues::Integer(1..5).
            l::c3 hasA
                part a: [1..2] l::c1;    // 1..2 * 1..2 \n"
                part b: [2..3] l::c2;    // shall be 0 as no property p is not defined.
                attribute p3: ScalarValues::Integer = productOverParts(p);
                attribute p4: ScalarValues::Integer = productOverPartsNotTransitive(p)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(25, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().getRange().max)
        assertEquals(1, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(25, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test
    fun astProductHasATestEvalDownInt() = testSession {
        loadSysMD("""
            package l {
                class c1 {
                    attribute p: ScalarValues::Integer(0..100).
                }
                class c2; 
                class c3 {
                    feature a: c1 [2..2];    // 1..2 * 1..2 \n"
                    feature b: c2 [2..3];    // shall be 0 as no property p is not defined.
                    attribute p3: ScalarValues::Integer(1..25) = productOverParts(p);
                }
            }"""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(5, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test
    fun astProductHasATest2() = testSession(catchExceptions = false) {
        loadSysMD("""
            package l {
                class c1 isA Base::Anything; 
                class c2 isA Base::Anything; 
                class c3 isA Base::Anything; 
            }
            l::c1 hasA
                attribute p: ScalarValues::Real(0.8).
            l::c2 hasA
                attribute p: ScalarValues::Real(0.5).
             l::c3 hasA
                part p1: [2..2] l::c1;
                part p2: [1..1] l::c2;
                attribute p3: ScalarValues::Real = productOverParts(p);
                attribute p4: ScalarValues::Real = productOverPartsNotTransitive(p).""")
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.32, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)

    }

    @Test
    fun astProductHasATest2EvalDown() = testSession(catchExceptions = false) {
        loadSysMD("""
                package l { class c1;  class c2;  class c3; }
                l::c1 hasA
                    attribute p: ScalarValues::Real(0.8).
                l::c2 hasA
                    attribute p: ScalarValues::Real.
                l::c3 hasA
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2 [1..1];
                    attribute p3: ScalarValues::Real(0.32..0.32) = productOverParts(p).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATest2WithExpression() = testSession {
        loadSysMD("""
                package l { class c1;  class c2;  class c3; }
                l::c1 hasA
                    attribute p: ScalarValues::Real(0.8).
                l::c2 hasA
                    attribute p: ScalarValues::Real(0.5).
                l::c3 hasA
                    feature p1: l::c1 [1..1];
                    feature p2: l::c2 [1..1];
                    attribute p3: ScalarValues::Real = productOverParts(1.0-p);
                    attribute p4: ScalarValues::Real = productOverPartsNotTransitive(1.0-p).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.10, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)

    }

    @Test
    fun astProductHasATest2WithExpressionEvalDown() = testSession {
        loadSysMD("""
                package l { class c1;  class c2;  class c3; } 
                l::c1 hasA
                    attribute p: ScalarValues::Real(0.8).
                l::c2 hasA
                    attribute p: ScalarValues::Real.
                l::c3 hasA
                    feature p1: l::c1;
                    feature p2: l::c2;
                    attribute p3: ScalarValues::Real(0.10..0.10) = productOverParts(1.0-p).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)

    }

    @Test
    fun astProductHasATest3() = testSession {
        loadSysMD(input = """
            package l { 
                class c1 {
                    attribute p: ScalarValues::Real(1..2).
                }
                class c2 {
                    feature c: l::c1;             
                };  
                class c3 {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    attribute p3: ScalarValues::Real = productOverParts(p);
                    attribute p4: ScalarValues::Real = productOverPartsNotTransitive(p);               
                }
            } """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(32.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // Issue: #240
    fun astProductHasATest3EvalDown() = testSession {
        loadSysMD(input = """
            package l { 
                class c1;  
                class c2;  
                class c3; 
            }
            l::c1 hasA
                attribute p: ScalarValues::Real(0..1000).
            l::c2 hasA
                feature c: l::c1.
            l::c3 hasA
                feature b: l::c2 [2 .. 3];
                attribute p3: ScalarValues::Real(1..8) = productOverParts(p)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.828427, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATest4() = testSession {
        loadSysMD(input = """
            package l { class c1;  class c2;  class c3;  class c4; }
            l::c1 hasA
                attribute p: ScalarValues::Real(1.0 .. 2).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2]; 
                feature b: l::c2 [2..3]; 
                feature c: l::c4 [1..2]; 
                attribute p3: ScalarValues::Real = productOverParts(p/2.0); 
                attribute p4: ScalarValues::Real = productOverPartsNotTransitive(p/2.0).
            l::c4 hasA
                attribute p: ScalarValues::Real(2..3).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.03125, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test @Disabled
    // the current issue is that [0..1000]/2 =[-e-324,500] which cannot be the input for the stable pow(AffineForm) Method
    fun astProductHasATest4EvalDown() = testSession {
        loadSysMD(input = """
            package l { 
                class c1;  
                class c2;  
                class c3;  
                class c4; 
            }
            l::c1 hasA
                p: ScalarValues::Real(1..1).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature c: l::c4 [1..2];
                expr p3: ScalarValues::Real(2.25..2.25) = productOverParts(p/2.0).
            l::c4 hasA
                p: ScalarValues::Real(0..1000).
            """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(8.4852813742, global.resolveVar("l::c4::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(144.0, global.resolveVar("l::c4::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // same test as astProductHasATest4, but with another model
    fun astProductHasATest5() = testSession {
        loadSysMD(input = """
            package l;
            l defines class c1;  class c2;  class c3;  class c4.
            l::c1 hasA
                attribute p: ScalarValues::Real(1..2);
                attribute q: ScalarValues::Real(0.5..0.5).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature c: l::c4 [1..2];
                attribute p3: ScalarValues::Real = productOverParts(p*q);
                attribute p4: ScalarValues::Real = productOverPartsNotTransitive(p*q).
            l::c4 hasA
                attribute p: ScalarValues::Real(2..3);
                attribute q: ScalarValues::Real(0.5..0.5).
            """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.03125, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(2.25, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }


    @Test // same test as astProductHasATest4, but with another model
    fun astProductHasATest5EvalDown() = testSession {
        loadSysMD(input = """
            package l { class c1;  class c2;  class c3;  class c4; }
            l::c1 hasA
                attribute p: ScalarValues::Real(1..2);
                attribute q: ScalarValues::Real(0.5..0.5).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature c: l::c4 [1..2];
                attribute p3: ScalarValues::Real(2.25..2.25) = productOverParts(p*q).
            l::c4 hasA
                attribute p: ScalarValues::Real(2..3);
                attribute q: ScalarValues::Real(0.01..100.0).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::c::q")!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(36.00, global.resolveVar("l::c3::c::q")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest() = testSession {
        loadSysMD("""
            package l {
                    class c1; 
                    class c2; 
                    class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(0.1..0.5).
            l::c3 hasA
                feature a: l::c1 [1..2];    // 1..2 * 1..2
                feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                feature p3: ScalarValues::Real = sumOverParts(p);
                feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.1, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.1, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATestEvalDown() = testSession {
        loadSysMD("""
            package l {
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(0.0..10.0).
            l::c3 hasA
                feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                feature p3: ScalarValues::Real(0.1..0.25) = sumOverParts(p)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.05, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATestInt() = testSession {
        loadSysMD("""
            package l {
                    class c1; 
                    class c2; 
                    class c3; 
            }
            l::c1 hasA
                    feature p: ScalarValues::Integer(1..5).
            l::c3 hasA
                    feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                    feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                    feature p3: ScalarValues::Integer = sumOverParts(p);
                    feature p4: ScalarValues::Integer = sumOverPartsNotTransitive(p)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().min)
        assertEquals(10, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().max)
        assertEquals(1, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().min)
        assertEquals(10, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun astSumHasATestEvalDownInt() = testSession {
        loadSysMD("""
                 package l {
                     class c1 { attribute p: ScalarValues::Integer(0..1000); }
                     class c2; 
                     class c3 {
                        feature a: l::c1 [2..2];    // 1..2 * 1..2 \n"
                        feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                        feature p3: ScalarValues::Integer(1..10) = sumOverParts(p); 
                    }
                 }"""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun astSumHasATest2() = testSession {
        loadSysMD(
            """
                package l { 
                    class c1;  
                    class c2;  
                    class c3; 
                }
                l::c1 hasA
                    feature p: ScalarValues::Real(0.8).
                l::c2 hasA
                    feature p: ScalarValues::Real(0.5).
                l::c3 hasA
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2 ;
                    feature p3: ScalarValues::Real = sumOverParts(p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p)."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(2.1, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest2EvalDown() = testSession {
        loadSysMD("""
            package l {
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(0.8).
            l::c2 hasA
                feature p: ScalarValues::Real(0..10).
             l::c3 hasA
                feature p1: l::c1 [2..2];
                feature p2: l::c2;
                feature p3: ScalarValues::Real(2.1..2.1) = sumOverParts(p)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest2WithExpression() = testSession {
        loadSysMD(
            """
            package l {
                    class c1; 
                    class c2; 
                    class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(0.8).
            l::c2 hasA
                feature p: ScalarValues::Real(0.5).
            l::c3 hasA
                feature p1: l::c1;
                feature p2: l::c2;
                feature p3: ScalarValues::Real = sumOverParts(1.0-p);
                feature p4: ScalarValues::Real = sumOverPartsNotTransitive(1.0-p)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.7, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest2WithExpressionEvalDown() = testSession {
        loadSysMD("""
            package l {
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(0.8).
            l::c2 hasA
                feature p: ScalarValues::Real(0..5).
            l::c3 hasA
                feature p1:  l::c1;
                feature p2:  l::c2;
                feature p3: ScalarValues::Real(0.7..0.7) = sumOverParts(1.0-p)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest3() = testSession {
        loadSysMD(input = """
            package l {
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(1..2).
            l::c2 hasA
                feature c: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature p3: ScalarValues::Real = sumOverParts(p);
                feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(10.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest3EvalDown() = testSession {
        loadSysMD(input = """
            package l {
                class c1; 
                class c2; 
                class c3; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(0..20).
            l::c2 hasA
                feature c: l::c1.
            l::c3 hasA
                feature b: l::c2 [2 .. 3];
                feature p3: ScalarValues::Real(12..12) = sumOverParts(p).
            """
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(4.0, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(6.0, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest4() = testSession {
        loadSysMD(input = """
            package l {
                class c1; 
                class c2; 
                class c3; 
                class c4; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(1..2).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature c: l::c4 [1..2];
                feature p3: ScalarValues::Real = sumOverParts(p/2.0);
                feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p/2.0).
            l::c4 hasA
                feature p: ScalarValues::Real(2..3).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(2.5, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(8.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(5.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest4EvalDown() = testSession {
        loadSysMD("""
            package l {
                class c1;  
                class c2;  
                class c3; 
                class c4; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(1..2).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature c: l::c4 [1..2];
                feature p3: ScalarValues::Real(8..8) = sumOverParts(p/2.0).
            l::c4 hasA
                feature p: ScalarValues::Real(0..100).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(13.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest5() = testSession {
        loadSysMD(input = """
            package l {
                class c1; 
                class c2; 
                class c3; 
                class c4; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(1..2);
                feature q: ScalarValues::Real(0.5..0.5).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature c: l::c4 [1..2];
                feature p3: ScalarValues::Real = sumOverParts(p*q);
                feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p*q).
            l::c4 hasA
                feature p: ScalarValues::Real(2..3);
                feature q: ScalarValues::Real(0.5..0.5).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(2.5, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(8.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(5.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest5EvalDown() = testSession {
        loadSysMD(input = """
            package l { 
                class c1;  class c2;  class c3; class c4; 
            }
            l::c1 hasA
                feature p: ScalarValues::Real(1..2);
                feature q: ScalarValues::Real(0.5..0.5).
            l::c2 hasA
                feature d: l::c1.
            l::c3 hasA
                feature a: l::c1 [1..2];
                feature b: l::c2 [2..3];
                feature c: l::c4 [1..2];
                feature p3: ScalarValues::Real(8..8) = sumOverParts(p*q);
                feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p*q).
            l::c4 hasA
                feature p: ScalarValues::Real(0..100);
                feature q: ScalarValues::Real(0.5..0.5).
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(13.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpression() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 :> c1;
                    class c3 :> c1;
                    class c4 :> c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Real(0.3..0.4).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature securityOfSupply: ScalarValues::Real = productOverSubclasses(securityOfSupply);
                    feature securityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(0.06,
            global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.08,
            global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.06,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.08,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpressionEvalDown() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 :> c1;
                    class c3 :> c1;
                    class c4 :> c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Real(0.01..1.0).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature needsOtherNameNotAsSubclass: ScalarValues::Real(0.06..0.06) = productOverSubclasses(securityOfSupply).""")
        propagate()
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpressionInt() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Integer(2..2).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Integer(3..4).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Integer(4..4).
                 l::c1 hasA
                    feature securityOfSupply: ScalarValues::Integer = productOverSubclasses(securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(6, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.value.asIdd().min)
        assertEquals(8, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.value.asIdd().max)
    }

    @Test @Disabled
    /**
    Not possible because of INT*Real no defined, needed for an inherited property result.
    Alternatively, there must be a possibility in the Compiler, which forwards the
    type of the property (int or real) to the Aggregation function
    **/
    fun astProductIsATestWithoutExpressionEvalDownInt() = testSession {
        loadSysMD(""" 
             package l;
             l defines
                class c1; 
                class c2 isA c1;
                class c3 isA c1.
             l::c2 hasA
                feature securityOfSupply: ScalarValues::Integer(2..2).
             l::c3 hasA
                feature securityOfSupply: ScalarValues::Integer(1..100).
             l::c1 hasA
                feature result: ScalarValues::Integer(6..6) = productOverSubclasses(securityOfSupply).""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3, global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.value.asIdd().max)
        assertEquals(3, global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.value.asIdd().min)
    }

    @Test // c4 shadowed by c3, so no further transitive search
    fun astProductIsATest() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2);
                    feature a: ScalarValues::Real(0.2..0.2);
                    feature b: ScalarValues::Real(0.2..0.2).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Real(0.3..0.4);
                    feature a: ScalarValues::Real(0.2..0.2);
                    feature b: ScalarValues::Real(0.2..0.2).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4);
                    feature a: ScalarValues::Real(0.2..0.2);
                    feature b: ScalarValues::Real(0.2..0.2).
                 l::c1 hasA
                    feature securityOfSupply1: ScalarValues::Real = productOverSubclasses(1.0-securityOfSupply);
                    feature securityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(1.0-securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.48, global.resolveVar("l::c1::securityOfSupply1")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.56, global.resolveVar("l::c1::securityOfSupply1")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.48, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.56, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search
    fun astProductIsATestEvalDown() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.0..0.99).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Real(0.3..0.4).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature otherName: ScalarValues::Real(0.56..0.56) = productOverSubclasses(1.0-securityOfSupply)."""
        )
        propagate()
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.06666,
            global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.2,
            global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductIsATest2() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3; 
                 }

                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.3..0.3).
                 l::c5 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real = 1.0 - productOverSubclasses(1.0 - securityOfSupply);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = 1.0 - productOverSubclassesNotTransitive(1.0 - securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.664,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.664,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.2,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.2,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductIsATest2EvalDown() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3; 
                 }
                 // Above definitions with fully qualified name were contradictions --> is c2 l::c1 or Any??
                 // Fixed them as above.
                 // Unfortunately, no error was thrown.
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.0..0.99).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.3..0.3).
                 l::c5 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real(0.664..0.664) = 1.0 - productOverSubclasses(1.0 - securityOfSupply)."""
        )
        propagate()
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.2,
            global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.2,
            global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductIsATest3() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3; 
                 }
                 l::c2 hasA
                    feature a: ScalarValues::Real(0.8..0.8);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c4 hasA
                    feature a: ScalarValues::Real(0.7..0.7);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c5 hasA
                    feature a: ScalarValues::Real(0.6..0.6);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real = productOverSubclasses(a*b);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(a*b)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.042,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.042,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.4,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.4,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductIsATest3EvalDown() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3; 
                 }
                 l::c2 hasA
                    feature a: ScalarValues::Real(0.01..1.0);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c4 hasA
                    feature a: ScalarValues::Real(0.7..0.7);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c5 hasA
                    feature a: ScalarValues::Real(0.6..0.6);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real(0.042..0.042) = productOverSubclasses(a*b)."""
        )
        propagate()
        assertTrue(status.exceptions.any { it !is SysMDInfo }, "Reports: ${status.exceptions}")
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.8, global.resolveVar("l::c2::a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.8, global.resolveVar("l::c2::a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATestWithoutExpression() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1;
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Real(0.3..0.4).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature securityOfSupply: ScalarValues::Real = sumOverSubclasses(securityOfSupply);
                    feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5,
            global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.6,
            global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.5,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.6,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATestWithoutExpressionEvalDown() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3; 
                 }
                 l::c2 hasA
                    attribute securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c3 hasA
                    attribute securityOfSupply: ScalarValues::Real(0.1..0.5).
                 l::c4 hasA
                    attribute securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    attribute needsOtherName: ScalarValues::Real(0.5..0.5) = sumOverSubclasses(securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.any { it !is SysMDInfo }, "Reports: ${status.exceptions}")
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    /** TODO ---> fix aggregation functions to consider variable field */
    @Test @Disabled
    fun astSumIsATest() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1 {
                        feature securityOfSupply:  ScalarValues::Real = sumOverSubclasses(1.0-securityOfSupply); 
                        feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(1.0-securityOfSupply); 
                    }
                    class c2 isA c1 {
                        feature securityOfSupply: ScalarValues::Real(0.2..0.2); 
                    }
                    class c3 isA c1 {
                        feature securityOfSupply: ScalarValues::Real(0.3..0.4); 
                    }
                    class c4 isA c3 {
                        feature securityOfSupply: ScalarValues::Real(0.4..0.4); 
                    }
                 }"""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.4,
            global.resolve<Feature>("l::c1::securityOfSupply")!!.variable!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.5,
            global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.4,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.5,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }


    @Test
    fun astSumIsATestWithOldAttribute() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3; 
                 }
                 l::c2 hasA
                    attribute securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c3 hasA
                    attribute securityOfSupply: ScalarValues::Real(0.3..0.4).
                 l::c4 hasA
                    attribute securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                   attribute securityOfSupply: ScalarValues::Real = sumOverSubclasses(1.0-securityOfSupply);
                   attribute securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(1.0-securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.4,
            global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.5,
            global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.4,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.5,
            global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATestInt() = testSession {
        loadSysMD(""" 
                 package l.
                 l defines
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3.
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Integer(2..2).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Integer(3..4).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Integer(4..4).
                 l::c1 hasA
                    feature securityOfSupply: ScalarValues::Integer = sumOverSubclasses(securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(5, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.idd().min)
        assertEquals(6, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.idd().max)
    }

    @Test
    fun astSumIsATestEvalDown() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c3 hasA
                    feature securityOfSupply: ScalarValues::Real(0.0..1.0).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature needsOtherName: ScalarValues::Real(1.5..1.5) = sumOverSubclasses(1.0-securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.any{ it !is SysMDInfo }, "Reports: ${status.exceptions}")
        // there are, however, exceptions:
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest2() = testSession {
        loadSysMD(""" 
                 package l;
                 l defines
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3.

                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.3..0.3).
                 l::c5 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real = 3.0 - sumOverSubclasses(1.0-securityOfSupply);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = 3.0 - sumOverSubclassesNotTransitive(1.0-securityOfSupply)."""
        )
        // print(resolveName<Expression>("l::c2::securityOfSupply"))
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.9,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.9,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(2.2,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.2,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest2EvalDown() = testSession {
        loadSysMD(""" 
                 package l { 
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3; 
                 }
                 l::c2 hasA
                    feature securityOfSupply: ScalarValues::Real(0.2..0.2).
                 l::c4 hasA
                    feature securityOfSupply: ScalarValues::Real(0.0..1.0).
                 l::c5 hasA
                    feature securityOfSupply: ScalarValues::Real(0.4..0.4).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real(0.9..0.9) = 3.0 -  sumOverSubclasses(1.0-securityOfSupply)."""
        )
        propagate()
        assertTrue(status.exceptions.any { it !is SysMDInfo}, "Exceptions: ${status.exceptions}")
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.3,
            global.resolveVar("l::c4::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c4::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest3() = testSession {
        loadSysMD(""" 
                 package l;
                 l defines
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3.
                 l::c2 hasA
                    feature a: ScalarValues::Real(0.8..0.8);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c4 hasA
                    feature a: ScalarValues::Real(0.7..0.7);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c5 hasA
                    feature a: ScalarValues::Real(0.6..0.6);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real = sumOverSubclasses(a*b);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(a*b)."""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.05,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.05,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.4,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.4,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest3EvalDown() = testSession {
        loadSysMD(""" 
                 package l;
                 l defines
                    class c1; 
                    class c2 isA c1;
                    class c3 isA c1;
                    class c4 isA c3;
                    class c5 isA c3.
                 l::c2 hasA
                    feature a: ScalarValues::Real(0.8..0.8);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c4 hasA
                    feature a: ScalarValues::Real(0.01..1.0);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c5 hasA
                    feature a: ScalarValues::Real(0.6..0.6);
                    feature b: ScalarValues::Real(0.5..0.5).
                 l::c1 hasA
                    feature resultingSecurityOfSupply: ScalarValues::Real(1.05..1.05) = sumOverSubclasses(a*b)."""
        )
        propagate()
        assertTrue(status.exceptions.any { it !is SysMDInfo }, "Exceptions: ${status.exceptions}")
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.7, global.resolveVar("l::c4::a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c4::a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    /**
     * FAILS after changes in:
     * - Quantity.kt --> constrain returns an empty set if so ... and not the specified value.
     * There is eventually a problem with astSumIsA as when the function is called its
     * parameters are from other classes and eventually not yet computed (should be initialized, however).
     */
    @Test
    fun astSumIsATestWithUnits() = testSession {
        loadSysMD(""" 
                 package l {
                    class c1 :> Base::Anything;
                    class c2 :> c1;
                    class c3 :> c1;
                    class c4 :> c1;
                 }
                 l::c2 hasA
                    attribute weight: ScalarValues::Real(0.2..0.2) [m].
                 l::c3 hasA
                    attribute weight: ScalarValues::Real(30.0..30.0) [cm].
                 l::c4 hasA
                    attribute weight: ScalarValues::Real(4.0..4.0) [dm].
                 l::c1 hasA
                    attribute securityOfSupply: ScalarValues::Real [m] = sumOverSubclasses(weight)."""
        )
        propagate()
        assertEquals(0, status.exceptions.size, "Reports: ${status.exceptions}")
        assertEquals(0.9, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.9, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals("m", global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.unit.toString())
    }


    @Test @Disabled // Issue: #240
    fun kpiTest() = testSession(catchExceptions = false) {
        loadSysMD(
            """
                class Metric; // Inheritance from Element leads to overloading of Element::Availability ...
                Metric hasA
                    attribute value: ScalarValues::Real(0.0 .. 1.0),
                    attribute weight: ScalarValues::Real(0.0 .. 1.0).
                class Realizability isA ScalarValues::Quality.
                class RealizabilityMetric isA Metric.
                RealizabilityMetric hasA
                    feature value: ScalarValues::Real(0..1) = sumOverSubclasses(weight*value);
                    feature value2: ScalarValues::Real(0..1) = sumOverSubclassesNotTransitive(weight*value);
                    feature weight: ScalarValues::Real(0..1) = 0.1;
                    feature weightsum: ScalarValues::Real (0..2) = sumOverSubclasses(weight);
                    feature weightsum2: ScalarValues::Real (0..2) = sumOverSubclassesNotTransitive(weight);
                    feature rightWeightSum: ScalarValues::Requirement = (weightsum >= 0.999999) and (weightsum <= 1.000001).

                Realizability hasA
                    feature weight: ScalarValues::Real(0 .. 1);
                    feature RealizabilityMetrics: [1..2] RealizabilityMetric; // ??? We need to define Vectors or so ...
                    feature weightedValue: ScalarValues::Real(0 .. 100) [%] = sumOverParts(weight*value);
                    feature weightedValue2: ScalarValues::Real(0 .. 100) [%] = sumOverPartsNotTransitive(weight*value).

                class Effort :> RealizabilityMetric.
                Effort hasA
                    feature weight: ScalarValues::Real(0.2) = 0.2;
                    feature value: ScalarValues::Real(0.55) = 0.55.

                class Availability isA RealizabilityMetric.
                Availability hasA
                    feature weight: ScalarValues::Real(0..1) = 0.3;
                    feature value: ScalarValues::Real(0..1) = 0.3.

                class Scalability isA RealizabilityMetric.
                Scalability hasA
                    feature weight: ScalarValues::Real(0..1) = 0.2;
                    feature value: ScalarValues::Real(0..1) = 0.7.

                class Implementability :> RealizabilityMetric {
                    feature weight: ScalarValues::Real(0..1) = 0.2;
                    feature value: ScalarValues::Real(0..1) = 0.7;
                }

                class BoundaryConditions isA RealizabilityMetric {
                    attribute weight: ScalarValues::Real(0..1) = 0.1;
                    attribute value: ScalarValues::Real(0..1) = 0.9; 
                }
    """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(1.00, global.resolveVar("RealizabilityMetric::weightsum")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(builder.True, global.resolveVar("RealizabilityMetric::rightWeightSum")!!.vectorQuantity.value)
        assertEquals(0.057, global.resolveVar("Realizability::weightedValue")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(0.114, global.resolveVar("Realizability::weightedValue")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value2")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value2")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(1.00, global.resolveVar("RealizabilityMetric::weightsum2")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(0.057, global.resolveVar("Realizability::weightedValue2")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(0.114, global.resolveVar("Realizability::weightedValue2")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
    }

    @Test
    fun kpiTest2() = testSession {
        loadSysMD("""
                    class RealizabilityMetric {
                        feature values: ScalarValues::Real = 0.71;
                    }
                    
                    feature f: RealizabilityMetric; 

                    class Realizability {
                        feature RealizabilityMetrics: RealizabilityMetric;
                        feature value: ScalarValues::Real(0 .. 100)  = 1.0 - sumOverParts(values);
                        feature value2: ScalarValues::Real(0 .. 100)  = 1.0 - sumOverPartsNotTransitive(values);
                    }
                    """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.29, global.resolveVar("Realizability::value")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(0.29, global.resolveVar("Realizability::value")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
        assertEquals(0.29, global.resolveVar("Realizability::value2")!!.vectorQuantity.getMinAsDouble(), 0.00001)
        assertEquals(0.29, global.resolveVar("Realizability::value2")!!.vectorQuantity.getMaxAsDouble(), 0.00001)
    }
}

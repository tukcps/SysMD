package constraintnettests

import util.testSession
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.SysMDInfo
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.mockup.loadKerML
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregationFunctionTest {

    /**
     * Test: productOverParts(property) computes the PRODUCT of all
     */
    @Test
    fun astProductHasATest() = testSession("ScalarValues") {
        loadKerML("""
            package l{
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0.1..0.5";}             
                }
                type c2:> Base::Anything; 
                type c3:> Base::Anything {
                    feature a: l::c1[1..2] ;    // 1..2 * 1..2 \n"
                    feature b: l::c2[2..3];    // shall be 0 as no property p is not defined.
                    feature p3: ScalarValues::Real = productOverParts(p);
                    feature p4: ScalarValues::Real = productOverPartsNotTransitive(p); 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.01, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.01, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATestEvalDown() = testSession("ScalarValues") {
        loadKerML("""
                package l {
                    type c1 :> Base::Anything {
                        feature p: ScalarValues::Real {:>> range = "0.001..1.0";}
                    }
                    type c2 :> Base::Anything; 
                    type c3 :> Base::Anything {
                        feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                        feature b: l::c2 [2..3];    // shall be 0 as no property p is defined.
                        feature p3: ScalarValues::Real = productOverParts(p) {:>> range = "0.1..25";} 
                    }
                }
        """)
        propagate()
        // val c3 = global.resolveName<Class>("l::c3")
        // val a = global.resolveName<Feature>("l::c3::a")
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.1, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATestInt() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Integer {:>> range = "1..5";}
                }
                type c2:> Base::Anything; 
                type c3:> Base::Anything {
                    feature a: l::c1[1..2];    // 1..2 * 1..2 \n"
                    feature b: l::c2[2..3];    // shall be 0 as no property p is not defined.
                    feature p3: ScalarValues::Integer = productOverParts(p);
                    feature p4: ScalarValues::Integer = productOverPartsNotTransitive(p); 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(25, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().getRange().max)
        assertEquals(1, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(25, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test
    fun astProductHasATestEvalDownInt() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1 :> Base::Anything {
                    feature p: ScalarValues::Integer {:>> range = "0..100";}
                }
                type c2 :> Base::Anything; 
                type c3 :> Base::Anything {
                    feature a: c1 [2..2];    // 1..2 * 1..2 \n"
                    feature b: c2 [2..3];    // shall be 0 as no property p is not defined.
                    feature p3: ScalarValues::Integer = productOverParts(p) {:>> range = "1..25";}
                }
            }"""
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(5, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test
    fun astProductHasATest2() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1 :> Base::Anything {
                    feature p: ScalarValues::Real(0.8); 
                }
                type c2 :> Base::Anything {
                    feature p: ScalarValues::Real(0.5); 
                }
                type c3 :> Base::Anything {
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2 [1..1];
                    feature p3: ScalarValues::Real = productOverParts(p);
                    feature p4: ScalarValues::Real = productOverPartsNotTransitive(p); 
                }
            }
        """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.32, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)

    }

    @Test
    fun astProductHasATest2EvalDown() = testSession("Occurrences") {
        loadKerML("""
                class c1 { feature p: ScalarValues::Real(0.8); }
                class c2 { feature p: ScalarValues::Real; }
                class c3 {
                    feature p1: c1 [2..2];
                    feature p2: c2 [1..1];
                    feature p3: ScalarValues::Real = productOverParts(p) {:>> range = "0.32..0.32";}
                }""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATest2WithExpression() = testSession("ScalarValues") {
        loadKerML("""
            package l { 
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real(0.8);
                }
                type c2:> Base::Anything {
                    feature p: ScalarValues::Real(0.5).
                }
                type c3:> Base::Anything {
                    feature p1: l::c1 [1..1];
                    feature p2: l::c2 [1..1];
                    feature p3: ScalarValues::Real = productOverParts(1.0-p);
                    feature p4: ScalarValues::Real = productOverPartsNotTransitive(1.0-p);
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.10, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)

    }

    @Test
    fun astProductHasATest2WithExpressionEvalDown() = testSession("ScalarValues") {
        loadKerML("""
                package l { 
                    type c1:> Base::Anything {
                        feature p: ScalarValues::Real(0.8).
                    }
                    type c2:> Base::Anything {
                        feature p: ScalarValues::Real.
                    }
                    type c3:> Base::Anything {
                        feature p1: l::c1;
                        feature p2: l::c2;
                        feature p3: ScalarValues::Real = productOverParts(1.0-p) {:>> range = "0.10..0.10";}
                    }
                }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)

    }

    @Test
    fun astProductHasATest3() = testSession("Occurrences") {
        loadKerML(input = """
            class c1 {
                feature p: ScalarValues::Real {:>> range = "1..2";}
            }
            class c2 {
                feature c: c1;             
            };  
            class c3 {
                feature a: c1 [1..2];
                feature b: c2 [2..3];
                feature p3: ScalarValues::Real = productOverParts(p);
                feature p4: ScalarValues::Real = productOverPartsNotTransitive(p);               
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(32.0, global.resolveVar("c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, global.resolveVar("c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // Issue: #240
    fun astProductHasATest3EvalDown() = testSession("ScalarValues") {
        loadKerML("""
            package l { 
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0..1000";}
                }
                type c2:> Base::Anything {
                    feature c: l::c1;                 
                }
                type c3:> Base::Anything {
                    feature b: l::c2 [2 .. 3];
                    feature p3: ScalarValues::Real = productOverParts(p) {:>> range = "1..8";}
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.0, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.828427, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductHasATest4() = testSession("ScalarValues") {
        loadKerML("""
                package l { 
                    type c1 :> Base::Anything {
                        feature p: ScalarValues::Real {:>> range = "1.0 .. 2";}              
                    }
                    type c2 :> Base::Anything {
                        feature d: l::c1;
                    }
                    type c3 :> Base::Anything {
                        feature a: l::c1 [1..2]; 
                        feature b: l::c2 [2..3]; 
                        feature c: l::c4 [1..2]; 
                        feature p3: ScalarValues::Real = productOverParts(p/2.0); 
                        feature p4: ScalarValues::Real = productOverPartsNotTransitive(p/2.0);
                    } 
                    type c4 :> Base::Anything {
                        feature p: ScalarValues::Real {:>> range = "2..3";} 
                    }
                }
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.03125, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test @Ignore
    // the current issue is that [0..1000]/2 =[-e-324,500] which cannot be the input for the stable pow(AffineForm) Method
    fun astProductHasATest4EvalDown() = testSession("ScalarValues") {
        loadKerML("""
            package l { 
                class c1 {
                    p: ScalarValues::Real {:>> range = "1..1";} 
                }
                class c2 {
                    feature d: l::c1;             
                }
                class c3 {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: ScalarValues::Real = productOverParts(p/2.0) {:>> range = "2.25..2.25";}                 
                }
                class c4 {
                    p: ScalarValues::Real  {:>> range = "0..1000";} 
                }
            }
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(8.4852813742, global.resolveVar("l::c4::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(144.0, global.resolveVar("l::c4::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // same test as astProductHasATest4, but with another model
    fun astProductHasATest5() = testSession("ScalarValues") {
        loadKerML(input = """
            package l {
                type c1 :> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "1..2";}
                    feature q: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
                type c2 :> Base::Anything {
                    feature d: l::c1; 
                }
                type c3 :> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: ScalarValues::Real = productOverParts(p*q);
                    feature p4: ScalarValues::Real = productOverPartsNotTransitive(p*q); 
                }
                type c4 :> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "2..3";}
                    feature q: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.03125, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(2.25, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }


    @Test // same test as astProductHasATest4, but with another model
    fun astProductHasATest5EvalDown() = testSession("ScalarValues") {
        loadKerML(input = """
            package l { 
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "1..2";}
                    feature q: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
                type c2:> Base::Anything {
                    feature d: l::c1;
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: ScalarValues::Real = productOverParts(p*q) {:>> range = "2.25..2.25";} 
                }
                type c4:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "2..3";}
                    feature q: ScalarValues::Real {:>> range = "0.01..100.0";}               
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::c::q")!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(36.00, global.resolveVar("l::c3::c::q")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0.1..0.5";} 
                }
                type c2:> Base::Anything; 
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];    // 1..2 * 1..2
                    feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                    feature p3: ScalarValues::Real = sumOverParts(p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p); 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.1, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.1, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATestEvalDown() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1 :> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0.0..10.0";}
                }
                type c2 :> Base::Anything; 
                type c3 :> Base::Anything {
                    feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                    feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                    feature p3: ScalarValues::Real = sumOverParts(p) {:>> range = "0.1..0.25";}
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.05, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::a::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATestInt() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Integer {:>> range = "1..5";}
                }
                type c2:> Base::Anything; 
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                    feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                    feature p3: ScalarValues::Integer = sumOverParts(p);
                    feature p4: ScalarValues::Integer = sumOverPartsNotTransitive(p); 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().min)
        assertEquals(10, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().max)
        assertEquals(1, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().min)
        assertEquals(10, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun astSumHasATestEvalDownInt() = testSession("ScalarValues") {
        loadKerML("""
                 package l {
                     type c1:> Base::Anything { feature p: ScalarValues::Integer {:>> range = "0..1000";} }
                     type c2:> Base::Anything; 
                     type c3 :> Base::Anything {
                        feature a: l::c1 [2..2];    // 1..2 * 1..2 \n"
                        feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                        feature p3: ScalarValues::Integer = sumOverParts(p) {:>> range = "1..10";} 
                    }
                 }
        """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun astSumHasATest2() = testSession("ScalarValues") {
        loadKerML("""
            package l { 
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real(0.8); 
                }
                type c2:> Base::Anything {
                    feature p: ScalarValues::Real(0.5); 
                }
                type c3:> Base::Anything {
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2 ;
                    feature p3: ScalarValues::Real = sumOverParts(p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p); 
                }
            }
        """)
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(2.1, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest2EvalDown() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real(0.8);
                }
                type c2:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0..10";}
                } 
                type c3:> Base::Anything {
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2;
                    feature p3: ScalarValues::Real = sumOverParts(p) {:>> range = "2.1..2.1";}
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest2WithExpression() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real(0.8); 
                }
                type c2:> Base::Anything {
                    feature p: ScalarValues::Real(0.5); 
                }
                type c3:> Base::Anything {
                    feature p1: l::c1;
                    feature p2: l::c2;
                    feature p3: ScalarValues::Real = sumOverParts(1.0-p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(1.0-p); 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.7, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest2WithExpressionEvalDown() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real(0.8); 
                }
                type c2:> Base::Anything {
                    feature p: ScalarValues::Real(0..5); 
                }
                type c3:> Base::Anything {
                    feature p1:  l::c1;
                    feature p2:  l::c2;
                    feature p3: ScalarValues::Real = sumOverParts(1.0-p) {:>> range = "0.7..0.7";} 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest3() = testSession("ScalarValues") {
        loadKerML(input = """
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "1..2";}  
                }
                type c2:> Base::Anything {
                    feature c: l::c1; 
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature p3: ScalarValues::Real = sumOverParts(p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p); 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(10.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(4.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest3EvalDown() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0..20";} 
                }
                type c2:> Base::Anything {
                    feature c: l::c1; 
                }
                type c3:> Base::Anything {
                    feature b: l::c2 [2 .. 3];
                    feature p3: ScalarValues::Real = sumOverParts(p) {:>> range = "12..12";}  
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(4.0, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(6.0, global.resolveVar("l::c2::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest4() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "1..2";} 
                }
                type c2:> Base::Anything {
                    feature d: l::c1; 
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: ScalarValues::Real = sumOverParts(p/2.0);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p/2.0); 
                }
                type c4:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "2..3";} 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(2.5, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(8.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(5.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest4EvalDown() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "1..2";} 
                }
                type c2:> Base::Anything {
                    feature d: l::c1; 
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: ScalarValues::Real = sumOverParts(p/2.0) {:>> range = "8..8";} 
                }
                type c4:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0..100";} 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(13.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest5() = testSession("ScalarValues") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "1..2";} 
                    feature q: ScalarValues::Real {:>> range = "0.5..0.5";}  
                }
                type c2:> Base::Anything {
                    feature d: l::c1; 
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: ScalarValues::Real = sumOverParts(p*q);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p*q);        
                }
                type c4:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "2..3";} 
                    feature q: ScalarValues::Real {:>> range = "0.5..0.5";} 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(2.5, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(8.0, global.resolveVar("l::c3::p3")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(5.0, global.resolveVar("l::c3::p4")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumHasATest5EvalDown() = testSession("ScalarValues") {
        loadKerML(input = """
            package l { 
                type c1:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "1..2";} 
                    feature q: ScalarValues::Real {:>> range = "0.5..0.5";} 
                }
                type c2:> Base::Anything {
                    feature d: l::c1; 
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: ScalarValues::Real = sumOverParts(p*q) {:>> range = "8..8";} 
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p*q); 
                }
                type c4:> Base::Anything {
                    feature p: ScalarValues::Real {:>> range = "0..100";} 
                    feature q: ScalarValues::Real {:>> range = "0.5..0.5";} 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(13.0, global.resolveVar("l::c3::c::p")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpression() = testSession("ScalarValues") {
        loadKerML(""" 
                 package l {
                    type c1 :> Base::Anything {
                        feature securityOfSupply: ScalarValues::Real = productOverSubclasses(securityOfSupply);
                        feature securityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(securityOfSupply); 
                    }
                    type c2 :> c1 {
                        feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";} 
                    }
                    type c3 :> c1 {
                        feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.4";}
                    }
                    type c4 :> c3 {
                        feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";} 
                    }
                 }
        """)
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
    fun astProductIsATestWithoutExpressionEvalDown() = testSession("Occurrences") {
        loadKerML(""" 
                 package l {
                    type c1 :> Base::Anything {
                        feature needsOtherNameNotAsSubclass: ScalarValues::Real = productOverSubclasses(securityOfSupply) {:>> range = "0.06..0.06";} 
                    }
                    type c2 :> c1 {
                        feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";} 
                    }
                    type c3 :> c1 {
                        feature securityOfSupply: ScalarValues::Real {:>> range = "0.01..1.0";}
                    }
                    type c4 :> c3 {
                        feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}
                    }
                 }
        """)
        propagate()
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpressionInt() = testSession("ScalarValues") {
        loadKerML(""" 
                 package l {
                    type c1 :> Base::Anything {
                        feature securityOfSupply: ScalarValues::Integer = productOverSubclasses(securityOfSupply);                 
                    }
                    type c2 :> c1 {
                        feature securityOfSupply: ScalarValues::Integer {:>> range = "2..2";} 
                    }
                    type c3 :> c1 {
                        feature securityOfSupply: ScalarValues::Integer {:>> range = "3..4";} 
                    }
                    type c4 :> c3 {
                        feature securityOfSupply: ScalarValues::Integer {:>> range = "4..4";}                
                    }
                 }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(6, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.value.asIdd().min)
        assertEquals(8, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.value.asIdd().max)
    }

    @Test @Ignore
    /**
    Not possible because of INT*Real no defined, needed for an inherited property result.
    Alternatively, there must be a possibility in the Compiler, which forwards the
    type of the property (int or real) to the Aggregation function
    **/
    fun astProductIsATestWithoutExpressionEvalDownInt() = testSession("Occurrences") {
        loadKerML(""" 
             package l;
             l defines
                class c1; 
                class c2 isA c1;
                class c3 isA c1;
             l::c2 hasA
                feature securityOfSupply: ScalarValues::Integer {:>> range = "2..2";}
             l::c3 hasA
                feature securityOfSupply: ScalarValues::Integer {:>> range = "1..100";}
             l::c1 hasA
                feature result: ScalarValues::Integer = productOverSubclasses(securityOfSupply) {:>> range = "6..6";}""")
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(3, global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.value.asIdd().max)
        assertEquals(3, global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.value.asIdd().min)
    }

    @Test // c4 shadowed by c3, so no further transitive search
    fun astProductIsATest() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply1: ScalarValues::Real = productOverSubclasses(1.0-securityOfSupply);
                    feature securityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(1.0-securityOfSupply);                 
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";}
                    feature a: ScalarValues::Real {:>> range = "0.2..0.2";} 
                    feature b: ScalarValues::Real {:>> range = "0.2..0.2";}                
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.4";}
                    feature a: ScalarValues::Real {:>> range = "0.2..0.2";}
                    feature b: ScalarValues::Real  {:>> range = "0.2..0.2";}               
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}
                    feature a: ScalarValues::Real {:>> range = "0.2..0.2";}
                    feature b: ScalarValues::Real {:>> range = "0.2..0.2";}                
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.48, global.resolveVar("l::c1::securityOfSupply1")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.56, global.resolveVar("l::c1::securityOfSupply1")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.48, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.56, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search
    fun astProductIsATestEvalDown() = testSession("Occurrences") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature otherName: ScalarValues::Real = productOverSubclasses(1.0-securityOfSupply) {:>> range = "0.56..0.56";}                  
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.0..0.99";}                 
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.4";}                 
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}                
                }
            }
        """)
        propagate()
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.06666, global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.2, global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductIsATest2() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = 1.0 - productOverSubclasses(1.0 - securityOfSupply);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = 1.0 - productOverSubclassesNotTransitive(1.0 - securityOfSupply);               
                } 
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";}
                }
                type c3 :> c1; 
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.3";}
                }
                type c5 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}
                }
            }
        """)
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
    fun astProductIsATest2EvalDown() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                class c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = 1.0 - productOverSubclasses(1.0 - securityOfSupply) {:>> range = "0.664..0.664";}                
                }
                class c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.0..0.99";}                
                }
                class c3 :> c1;
                class c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.3";}               
                }
                class c5 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}                
                }
            }
        """)
        propagate()
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.2, global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.2, global.resolveVar("l::c2::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astProductIsATest3() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = productOverSubclasses(a*b);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(a*b);            
                }
                type c2 :> c1 {
                    feature a: ScalarValues::Real {:>> range = "0.8..0.8";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}             
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.7..0.7";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}             
                }
                type c5 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.6..0.6";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}             
                }
            }
        """)
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
    fun astProductIsATest3EvalDown() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = productOverSubclasses(a*b) {:>> range = "0.042..0.042";}
                }
                type c2 :> c1 {
                    feature a: ScalarValues::Real {:>> range = "0.01..1.0";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.7..0.7";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
                type c5 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.6..0.6";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.any { it !is SysMDInfo }, "Reports: ${status.exceptions}")
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.8, global.resolveVar("l::c2::a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.8, global.resolveVar("l::c2::a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATestWithoutExpression() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply: ScalarValues::Real = sumOverSubclasses(securityOfSupply);
                    feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(securityOfSupply); 
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";}
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.4";} 
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";} 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.5, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.6, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.6, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATestWithoutExpressionEvalDown() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature needsOtherName: ScalarValues::Real = sumOverSubclasses(securityOfSupply) {:>> range = "0.5..0.5";}                 
                } 
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";}               
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.1..0.5";}                
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}                
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.any { it !is SysMDInfo }, "Reports: ${status.exceptions}")
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1  :> Base::Anything{
                    feature securityOfSupply:  ScalarValues::Real = sumOverSubclasses(1.0-securityOfSupply); 
                    feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(1.0-securityOfSupply); 
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.4";} 
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";} 
                }
            }
        """)
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
    fun astSumIsATestWithAttribute() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                   feature securityOfSupply: ScalarValues::Real = sumOverSubclasses(1.0-securityOfSupply);
                   feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(1.0-securityOfSupply);                 
                } 
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";}                
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.4";}                 
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}                 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.4, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.4, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c1::securityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATestInt() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply: ScalarValues::Integer = sumOverSubclasses(securityOfSupply);                 
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Integer {:>> range = "2..2";}                
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Integer {:>> range = "3..4";}                 
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Integer {:>> range = "4..4";}                 
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(5, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.idd().min)
        assertEquals(6, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.idd().max)
    }

    @Test
    fun astSumIsATestEvalDown() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature needsOtherName: ScalarValues::Real = sumOverSubclasses(1.0-securityOfSupply) {:>> range = "1.5..1.5";} 
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.0..1.0";} 
                }
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";} 
                }
            }
        """)
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
    fun astSumIsATest2() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = 3.0 - sumOverSubclasses(1.0-securityOfSupply);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = 3.0 - sumOverSubclassesNotTransitive(1.0-securityOfSupply);                    
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.3..0.3";} 
                }
                type c5 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";} 
                }
            }
        """)
        // print(resolveName<Expression>("l::c2::securityOfSupply"))
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.9, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.9, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(2.2, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.2, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest2EvalDown() = testSession("ScalarValues") {
        loadKerML(""" 
            package l { 
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = 3.0 -  sumOverSubclasses(1.0-securityOfSupply) {:>> range = "0.9..0.9";}
                }
                type c2 :> c1 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.2..0.2";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.0..1.0";} 
                }
                type c5 :> c3 {
                    feature securityOfSupply: ScalarValues::Real {:>> range = "0.4..0.4";}
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.any { it !is SysMDInfo}, "Exceptions: ${status.exceptions}")
        // assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(0.3, global.resolveVar("l::c4::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.3, global.resolveVar("l::c4::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest3() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = sumOverSubclasses(a*b);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(a*b);
                } 
                type c2 :> c1 {
                    feature a: ScalarValues::Real {:>> range = "0.8..0.8";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.7..0.7";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}                   
                }
                type c5 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.6..0.6";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
            }
        """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Exceptions: ${status.exceptions}")
        assertEquals(1.05, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.05, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(0.4, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.4, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun astSumIsATest3EvalDown() = testSession("ScalarValues") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = sumOverSubclasses(a*b) {:>> range = "1.05..1.05";} 
                }
                type c2 :> c1 {
                    feature a: ScalarValues::Real {:>> range = "0.8..0.8";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.01..1.0";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";} 
                }
                type c5 :> c3 {
                    feature a: ScalarValues::Real {:>> range = "0.6..0.6";}
                    feature b: ScalarValues::Real {:>> range = "0.5..0.5";} 
                }
            }
        """)
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
    fun astSumIsATestWithUnits() = testSession("SI") {
        loadKerML(""" 
             package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply: SI::Length = sumOverSubclasses(length);                 
                }
                type c2 :> c1 {
                    feature length: SI::Length {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1 {
                    feature length: SI::Length {:>> unit = "cm"; :>> range = "30.0..30.0";}                 
                }
                type c4 :> c1 {
                    feature length: SI::Length {:>> unit = "dm"; :>> range = "4.0..4.0";}                  
                }
             }
        """)
        propagate()
        assertEquals(0, status.exceptions.size, "Reports: ${status.exceptions}")
        assertEquals(0.9, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.9, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals("m", global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.unit.toString())
    }


    @Test @Ignore // Issue: #240, re-write in either KerML or SysMD
    fun kpiTest() = testSession {
        loadKerML(
            """
                class Metric; // Inheritance from Element leads to overloading of Element::Availability ...
                Metric hasA
                    feature value: ScalarValues::Real {:>> range = "0.0 .. 1.0";} 
                    feature weight: ScalarValues::Real {:>> range = "0.0 .. 1.0";}
                class Realizability isA ScalarValues::Quality;
                class RealizabilityMetric isA Metric;
                RealizabilityMetric hasA
                    feature value: ScalarValues::Real = sumOverSubclasses(weight*value) {:>> range = "0..1";} 
                    feature value2: ScalarValues::Real = sumOverSubclassesNotTransitive(weight*value) {:>> range = "0..1";} 
                    feature weight: ScalarValues::Real = 0.1 {:>> range = "0..1";} 
                    feature weightsum: ScalarValues::Real = sumOverSubclasses(weight) {:>> range = "0..2";} 
                    feature weightsum2: ScalarValues::Real = sumOverSubclassesNotTransitive(weight) {:>> range = "0..2";}
                    feature rightWeightSum: ScalarValues::Requirement = (weightsum >= 0.999999) and (weightsum <= 1.000001).

                Realizability hasA
                    feature weight: ScalarValues::Real {:>> range = "0 .. 1";}
                    feature RealizabilityMetrics: [1..2] RealizabilityMetric; // ??? We need to define Vectors or so ...
                    feature weightedValue: ScalarValues::Real = sumOverParts(weight*value) {:>> unit = "%"; :>> range = "0 .. 100";} 
                    feature weightedValue2: ScalarValues::Real = sumOverPartsNotTransitive(weight*value) {:>> unit = "%"; :>> range = "0 .. 100";} 

                class Effort :> RealizabilityMetric.
                Effort hasA
                    feature weight: ScalarValues::Real = 0.2;
                    feature value: ScalarValues::Real = 0.55;

                class Availability isA RealizabilityMetric.
                Availability hasA
                    feature weight: ScalarValues::Real = 0.3 {:>> range = "0 .. 1";}
                    feature value: ScalarValues::Real = 0.3 {:>> range = "0 .. 1";}

                class Scalability isA RealizabilityMetric.
                Scalability hasA
                    feature weight: ScalarValues::Real = 0.2 {:>> range = "0 .. 1";}
                    feature value: ScalarValues::Real = 0.7 {:>> range = "0 .. 1";}

                class Implementability :> RealizabilityMetric {
                    feature weight: ScalarValues::Real = 0.2 {:>> range = "0 .. 1";}
                    feature value: ScalarValues::Real = 0.7 {:>> range = "0 .. 1";}
                }

                class BoundaryConditions isA RealizabilityMetric {
                    feature weight: ScalarValues::Real = 0.1 {:>> range = "0 .. 1";}
                    feature value: ScalarValues::Real = 0.9 {:>> range = "0 .. 1";} 
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
    fun kpiTest2() = testSession("ScalarValues") {
        loadKerML("""
            type RealizabilityMetric :> Base::Anything {
                feature values: ScalarValues::Real = 0.71;
            }
            
            feature f: RealizabilityMetric; 

            type Realizability :> Base::Anything {
                feature RealizabilityMetrics: RealizabilityMetric;
                feature value: ScalarValues::Real = 1.0 - sumOverParts(values) {:>> range = "0 .. 100";}
                feature value2: ScalarValues::Real = 1.0 - sumOverPartsNotTransitive(values) {:>> range = "0 .. 100";}
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

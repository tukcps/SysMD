package constraintnettests

import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.services.Runlevel
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregationFunctionTest {

    /**
     * Test: productOverParts(property) computes the PRODUCT of all
     */
    @Test
    fun astProductHasATest() = testSession("Ranges") {
        loadKerML("""
            package l{
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.1..0.5";}             
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
        solver.propagate()
        assertNoIssues()
        assertEquals(0.01, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(0.01, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p4")!!.max(), 0.0001)
    }

    @Test
    fun astProductHasATestEvalDown() = testSession("Ranges") {
        loadKerML("""
                package l {
                    type c1 :> Base::Anything {
                        feature p: Ranges::RealInRange {:>> range = "0.001..1.0";}
                    }
                    type c2 :> Base::Anything; 
                    type c3 :> Base::Anything {
                        feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                        feature b: l::c2 [2..3];    // shall be 0 as no property p is defined.
                        feature p3: Ranges::RealInRange = productOverParts(p) {:>> range = "0.1..25";} 
                    }
                }
        """)
        solver.propagate()
        // val c3 = global.resolveName<Class>("l::c3")
        // val a = global.resolveName<Feature>("l::c3::a")
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertEquals(0.1, global.resolveVar("l::c3::a::p")!!.min(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::a::p")!!.max(), 0.0001)
    }

    @Test
    fun astProductHasATestInt() = testSession( "Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::IntegerInRange {:>> range = "1..5";}
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
        solver.propagate()
        assertNoIssues()
        assertEquals(1, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(25, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().getRange().max)
        assertEquals(1, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(25, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test
    fun astProductHasATestEvalDownInt() = testSession( "Ranges") {
        loadKerML("""
            package l {
                type c1 :> Base::Anything {
                    feature p: Ranges::IntegerInRange {:>> range = "0..100";}
                }
                type c2 :> Base::Anything; 
                type c3 :> Base::Anything {
                    feature a: c1 [2..2];    // 1..2 * 1..2 \n"
                    feature b: c2 [2..3];    // shall be 0 as no property p is not defined.
                    feature p3: Ranges::IntegerInRange = productOverParts(p) {:>> range = "1..25";}
                }
            }"""
        )
        solver.propagate()
        assertNoIssues()
        assertEquals(1, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(5, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test
    fun astProductHasATest2() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1 :> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.8";} 
                }
                type c2 :> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.5";}
                }
                type c3 :> Base::Anything {
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2 [1..1];
                    feature p3: ScalarValues::Real = productOverParts(p);
                    feature p4: ScalarValues::Real = productOverPartsNotTransitive(p); 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.32, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(0.32, global.resolveVar("l::c3::p4")!!.max(), 0.0001)

    }

    @Test
    fun astProductHasATest2EvalDown() = testSession("Occurrences", "Ranges") {
        loadKerML("""
                class c1 { feature p: Ranges::RealInRange {:>> range = "0.8";} }
                class c2 { feature p: ScalarValues::Real; }
                class c3 {
                    feature p1: c1 [2..2];
                    feature p2: c2 [1..1];
                    feature p3: Ranges::RealInRange = productOverParts(p) {:>> range = "0.32..0.32";}
                }""")
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("c3::p2::p")!!.min(), 0.0001)
        assertEquals(0.5, global.resolveVar("c3::p2::p")!!.max(), 0.0001)
    }

    @Test
    fun astProductHasATest2WithExpression() = testSession("Ranges") {
        loadKerML("""
            package l { 
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.8";}
                }
                type c2:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.5";}
                }
                type c3:> Base::Anything {
                    feature p1: l::c1 [1..1];
                    feature p2: l::c2 [1..1];
                    feature p3: ScalarValues::Real = productOverParts(1.0-p);
                    feature p4: ScalarValues::Real = productOverPartsNotTransitive(1.0-p);
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.10, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(0.10, global.resolveVar("l::c3::p4")!!.max(), 0.0001)

    }

    @Test
    fun astProductHasATest2WithExpressionEvalDown() = testSession("Ranges") {
        loadKerML("""
                package l { 
                    type c1:> Base::Anything {
                        feature p: Ranges::RealInRange {:>> range = "0.8";}
                    }
                    type c2:> Base::Anything {
                        feature p: ScalarValues::Real.
                    }
                    type c3:> Base::Anything {
                        feature p1: l::c1;
                        feature p2: l::c2;
                        feature p3: Ranges::RealInRange = productOverParts(1.0-p) {:>> range = "0.10..0.10";}
                    }
                }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.min(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.max(), 0.0001)

    }

    @Test
    fun astProductHasATest3() = testSession("Occurrences", "Ranges") {
        loadKerML(input = """
            class c1 {
                feature p: Ranges::RealInRange {:>> range = "1..2";}
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
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("c3::p3")!!.min(), 0.0001)
        assertEquals(32.0, global.resolveVar("c3::p3")!!.max(), 0.0001)
        assertEquals(1.0, global.resolveVar("c3::p4")!!.min(), 0.0001)
        assertEquals(4.0, global.resolveVar("c3::p4")!!.max(), 0.0001)
    }

    @Test // Issue: #240
    fun astProductHasATest3EvalDown() = testSession("Ranges") {
        loadKerML("""
            package l { 
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0..1000";}
                }
                type c2:> Base::Anything {
                    feature c: l::c1;                 
                }
                type c3:> Base::Anything {
                    feature b: l::c2 [2 .. 3];
                    feature p3: Ranges::RealInRange = productOverParts(p) {:>> range = "1..8";}
                }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        assertEquals(1.0, global.resolveVar("l::c2::c::p")!!.min(), 0.0001)
        assertEquals(2.828427, global.resolveVar("l::c2::c::p")!!.max(), 0.0001)
    }

    @Test
    fun astProductHasATest4() = testSession("Ranges") {
        loadKerML("""
                package l { 
                    type c1 :> Base::Anything {
                        feature p: Ranges::RealInRange {:>> range = "1.0 .. 2";}              
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
                        feature p: Ranges::RealInRange {:>> range = "2..3";} 
                    }
                }
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.03125, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p4")!!.max(), 0.0001)
    }

    @Test @Ignore
    // the current issue is that [0..1000]/2 =[-e-324,500] which cannot be the input for the stable pow(AffineForm) Method
    fun astProductHasATest4EvalDown() = testSession("Ranges") {
        loadKerML("""
            package l { 
                class c1 {
                    p: Ranges::RealInRange {:>> range = "1..1";} 
                }
                class c2 {
                    feature d: l::c1;             
                }
                class c3 {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: Ranges::RealInRange = productOverParts(p/2.0) {:>> range = "2.25..2.25";}                 
                }
                class c4 {
                    p: Ranges::RealInRange {:>> range = "0..1000";} 
                }
            }
            """)
        solver.propagate()
        assertNoIssues()
        assertEquals(8.4852813742, global.resolveVar("l::c4::p")!!.min(), 0.0001)
        assertEquals(144.0, global.resolveVar("l::c4::p")!!.max(), 0.0001)
    }

    @Test // same test as astProductHasATest4, but with another model
    fun astProductHasATest5() = testSession("Ranges") {
        loadKerML(input = """
            package l {
                type c1 :> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "1..2";}
                    feature q: Ranges::RealInRange {:>> range = "0.5..0.5";}
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
                    feature p: Ranges::RealInRange {:>> range = "2..3";}
                    feature q: Ranges::RealInRange {:>> range = "0.5..0.5";}
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.03125, global.resolveVar("l::c3::p3")!!.min(), 0.000001)
        assertEquals(2.25, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(2.25, global.resolveVar("l::c3::p4")!!.max(), 0.0001)
    }


    @Test // same test as astProductHasATest4, but with another model
    fun astProductHasATest5EvalDown() = testSession("Ranges") {
        loadKerML(input = """
            package l { 
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "1..2";}
                    feature q: Ranges::RealInRange {:>> range = "0.5..0.5";}
                }
                type c2:> Base::Anything {
                    feature d: l::c1;
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: Ranges::RealInRange = productOverParts(p*q) {:>> range = "2.25..2.25";} 
                }
                type c4:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "2..3";}
                    feature q: Ranges::RealInRange {:>> range = "0.01..100.0";}               
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("l::c3::c::q")!!.min(), 0.000001)
        assertEquals(36.00, global.resolveVar("l::c3::c::q")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.1..0.5";} 
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
        solver.propagate()
        assertNoIssues()
        assertEquals(0.1, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(0.1, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(1.0, global.resolveVar("l::c3::p4")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATestEvalDown() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1 :> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.0..10.0";}
                }
                type c2 :> Base::Anything; 
                type c3 :> Base::Anything {
                    feature a: l::c1 [1..2];    // 1..2 * 1..2 \n"
                    feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                    feature p3: Ranges::RealInRange = sumOverParts(p) {:>> range = "0.1..0.25";}
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.05, global.resolveVar("l::c3::a::p")!!.min(), 0.0001)
        assertEquals(0.25, global.resolveVar("l::c3::a::p")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATestInt() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::IntegerInRange {:>> range = "1..5";}
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
        solver.propagate()
        assertNoIssues()
        assertEquals(1, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().min)
        assertEquals(10, global.resolveVar("l::c3::p3")!!.vectorQuantity.value.asIdd().max)
        assertEquals(1, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().min)
        assertEquals(10, global.resolveVar("l::c3::p4")!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun astSumHasATestEvalDownInt() = testSession("Ranges") {
        loadKerML("""
                 package l {
                     type c1:> Base::Anything { feature p: Ranges::IntegerInRange {:>> range = "0 .. 1000";} }
                     type c2:> Base::Anything; 
                     type c3 :> Base::Anything {
                        feature a: l::c1 [2..2];    // 1..2 * 1..2 \n"
                        feature b: l::c2 [2..3];    // shall be 0 as no property p is not defined.
                        feature p3: Ranges::IntegerInRange = sumOverParts(p) {:>> range = "1..10";} 
                    }
                 }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, global.resolveVar("l::c3::a::p")!!.vectorQuantity.value.asIdd().max)
    }

    @Test
    fun astSumHasATest2() = testSession("ScalarValues", "Ranges") {
        loadKerML("""           
            package l { 
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange { :>> range = " 0.8 .. 0.8"; } 
                }
                type c2:> Base::Anything {
                    feature p: Ranges::RealInRange { :>> range = " 0.5 .. 0.5"; } 
                }
                type c3:> Base::Anything {
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2 ;
                    feature p3: ScalarValues::Real = sumOverParts(p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p); 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(2.1, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(2.1, global.resolveVar("l::c3::p4")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest2EvalDown() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.8"; }
                }
                type c2:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0..10"; }
                } 
                type c3:> Base::Anything {
                    feature p1: l::c1 [2..2];
                    feature p2: l::c2;
                    feature p3: Ranges::RealInRange = sumOverParts(p) {:>> range = "2.1..2.1";}
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.min(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest2WithExpression() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.8"; } 
                }
                type c2:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.5"; }
                }
                type c3:> Base::Anything {
                    feature p1: l::c1;
                    feature p2: l::c2;
                    feature p3: ScalarValues::Real = sumOverParts(1.0-p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(1.0-p); 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.7, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c3::p4")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest2WithExpressionEvalDown() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0.8"; } 
                }
                type c2:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0..5"; } 
                }
                type c3:> Base::Anything {
                    feature p1:  l::c1;
                    feature p2:  l::c2;
                    feature p3: Ranges::RealInRange  = sumOverParts(1.0-p) {:>> range = "0.7..0.7";} 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.min(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c3::p2::p")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest3() = testSession("Ranges") {
        loadKerML(input = """
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange  {:>> range = "1..2";}  
                }
                type c2:> Base::Anything {
                    feature c: c1; 
                }
                type c3:> Base::Anything {
                    feature a: c1 [1..2];
                    feature b: c2 [2..3];
                    feature p3: ScalarValues::Real = sumOverParts(p);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p); 
                }
        """, Runlevel.ALL)
        assertNoIssues()
        assertEquals(3.0, global.resolveVar("c3::p3")!!.min(), 0.0001)
        assertEquals(10.0, global.resolveVar("c3::p3")!!.max(), 0.0001)
        assertEquals(1.0, global.resolveVar("c3::p4")!!.min(), 0.0001)
        assertEquals(4.0, global.resolveVar("c3::p4")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest3EvalDown() = testSession("Ranges") {
        loadKerML("""
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange  {:>> range = "0..20";} 
                }
                type c2:> Base::Anything {
                    feature c: c1; 
                }
                type c3:> Base::Anything {
                    feature b: c2 [2 .. 3];
                    feature p3: Ranges::RealInRange  = sumOverParts(p) {:>> range = "12..12";}  
                }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(4.0, global.resolveVar("c2::c::p")!!.min(), 0.0001)
        assertEquals(6.0, global.resolveVar("c2::c::p")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest4() = testSession("Ranges") {
        loadKerML("""
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange  {:>> range = "1..2";} 
                }
                type c2:> Base::Anything {
                    feature d: c1; 
                }
                type c3:> Base::Anything {
                    feature a: c1 [1..2];
                    feature b: c2 [2..3];
                    feature c: c4 [1..2];
                    feature p3: ScalarValues::Real = sumOverParts(p/2.0);
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p/2.0); 
                }
                type c4:> Base::Anything {
                    feature p: Ranges::RealInRange  {:>> range = "2..3";} 
                }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(2.5, global.resolveVar("c3::p3")!!.min(), 0.0001)
        assertEquals(8.0, global.resolveVar("c3::p3")!!.max(), 0.0001)
        assertEquals(1.5, global.resolveVar("c3::p4")!!.min(), 0.0001)
        assertEquals(5.0, global.resolveVar("c3::p4")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest4EvalDown() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange  {:>> range = "1..2";} 
                }
                type c2:> Base::Anything {
                    feature d: c1; 
                }
                type c3:> Base::Anything {
                    feature a: c1 [1..2];
                    feature b: c2 [2..3];
                    feature c: c4 [1..2];
                    feature p3: Ranges::RealInRange  = sumOverParts(p/2.0) {:>> range = "8..8";} 
                }
                type c4:> Base::Anything {
                    feature p: Ranges::RealInRange  {:>> range = "0..100"; } 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3.0, global.resolveVar("l::c3::c::p")!!.min(), 0.0001)
        assertEquals(13.0, global.resolveVar("l::c3::c::p")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest5() = testSession("Ranges") {
        loadKerML("""
            package l {
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange  {:>> range = "1..2";} 
                    feature q: Ranges::RealInRange  {:>> range = "0.5..0.5";}  
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
                    feature p: Ranges::RealInRange {:>> range = "2..3";} 
                    feature q: Ranges::RealInRange  {:>> range = "0.5..0.5";} 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(2.5, global.resolveVar("l::c3::p3")!!.min(), 0.0001)
        assertEquals(8.0, global.resolveVar("l::c3::p3")!!.max(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c3::p4")!!.min(), 0.0001)
        assertEquals(5.0, global.resolveVar("l::c3::p4")!!.max(), 0.0001)
    }

    @Test
    fun astSumHasATest5EvalDown() = testSession("Ranges") {
        loadKerML("""
            package l { 
                type c1:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "1..2";} 
                    feature q: Ranges::RealInRange {:>> range = "0.5..0.5";} 
                }
                type c2:> Base::Anything {
                    feature d: l::c1; 
                }
                type c3:> Base::Anything {
                    feature a: l::c1 [1..2];
                    feature b: l::c2 [2..3];
                    feature c: l::c4 [1..2];
                    feature p3: Ranges::RealInRange = sumOverParts(p*q) {:>> range = "8..8";} 
                    feature p4: ScalarValues::Real = sumOverPartsNotTransitive(p*q); 
                }
                type c4:> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "0..100";} 
                    feature q: Ranges::RealInRange {:>> range = "0.5..0.5";} 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(3.0, global.resolveVar("l::c3::c::p")!!.min(), 0.0001)
        assertEquals(13.0, global.resolveVar("l::c3::c::p")!!.max(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpression() = testSession("Ranges") {
        loadKerML(""" 
                 package l {
                    type c1 :> Base::Anything {
                        feature securityOfSupply: ScalarValues::Real = productOverSubclasses(securityOfSupply);
                        feature securityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(securityOfSupply); 
                    }
                    type c2 :> c1 {
                        feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";} 
                    }
                    type c3 :> c1 {
                        feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.4";}
                    }
                    type c4 :> c3 {
                        feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";} 
                    }
                 }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.06,
            global.resolveVar("l::c1::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.08,
            global.resolveVar("l::c1::securityOfSupply")!!.max(), 0.0001)
        assertEquals(0.06,
            global.resolveVar("l::c1::securityOfSupply2")!!.min(), 0.0001)
        assertEquals(0.08,
            global.resolveVar("l::c1::securityOfSupply2")!!.max(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpressionEvalDown() = testSession("Occurrences", "Ranges") {
        loadKerML(""" 
                 package l {
                    type c1 :> Base::Anything {
                        feature needsOtherNameNotAsSubclass: Ranges::RealInRange  = productOverSubclasses(securityOfSupply) {:>> range = "0.06..0.06";} 
                    }
                    type c2 :> c1 {
                        feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";} 
                    }
                    type c3 :> c1 {
                        feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.01..1.0";}
                    }
                    type c4 :> c3 {
                        feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}
                    }
                 }
        """)
        solver.propagate()
        // assertNoIssues()
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.max(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search (securityOfSupply in c3 and c4)
    fun astProductIsATestWithoutExpressionInt() = testSession("Ranges") {
        loadKerML(""" 
                 package l {
                    type c1 :> Base::Anything {
                        feature securityOfSupply: ScalarValues::Integer = productOverSubclasses(securityOfSupply);                 
                    }
                    type c2 :> c1 {
                        feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "2..2";} 
                    }
                    type c3 :> c1 {
                        feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "3..4";} 
                    }
                    type c4 :> c3 {
                        feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "4..4";}                
                    }
                 }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(6, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.value.asIdd().min)
        assertEquals(8, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.value.asIdd().max)
    }

    @Test @Ignore
    /**
    Not possible because of INT*Real no defined, needed for an inherited property result.
    Alternatively, there must be a possibility in the Compiler, which forwards the
    type of the property (int or real) to the Aggregation function
    **/
    fun astProductIsATestWithoutExpressionEvalDownInt() = testSession("Occurrences", "Ranges") {
        loadKerML(""" 
             package l;
             l defines
                class c1; 
                class c2 isA c1;
                class c3 isA c1;
             l::c2 hasA
                feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "2..2";}
             l::c3 hasA
                feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "1..100";}
             l::c1 hasA
                feature result: Ranges::IntegerInRange  = productOverSubclasses(securityOfSupply) {:>> range = "6..6";}""")
        solver.propagate()
        assertNoIssues()
        assertEquals(3, global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.value.asIdd().max)
        assertEquals(3, global.resolveVar("l::c3::securityOfSupply")!!.vectorQuantity.value.asIdd().min)
    }

    @Test // c4 shadowed by c3, so no further transitive search
    fun astProductIsATest() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply1: ScalarValues::Real = productOverSubclasses(1.0-securityOfSupply);
                    feature securityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(1.0-securityOfSupply);                 
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";}
                    feature a: Ranges::RealInRange  {:>> range = "0.2..0.2";} 
                    feature b: Ranges::RealInRange  {:>> range = "0.2..0.2";}                
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.4";}
                    feature a: Ranges::RealInRange  {:>> range = "0.2..0.2";}
                    feature b: Ranges::RealInRange   {:>> range = "0.2..0.2";}               
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}
                    feature a: Ranges::RealInRange  {:>> range = "0.2..0.2";}
                    feature b: Ranges::RealInRange  {:>> range = "0.2..0.2";}                
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.48, global.resolveVar("l::c1::securityOfSupply1")!!.min(), 0.0001)
        assertEquals(0.56, global.resolveVar("l::c1::securityOfSupply1")!!.max(), 0.0001)
        assertEquals(0.48, global.resolveVar("l::c1::securityOfSupply2")!!.min(), 0.0001)
        assertEquals(0.56, global.resolveVar("l::c1::securityOfSupply2")!!.max(), 0.0001)
    }

    @Test // c4 shadowed by c3, so no further transitive search
    fun astProductIsATestEvalDown() = testSession("Occurrences", "Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature otherName: Ranges::RealInRange  = productOverSubclasses(1.0-securityOfSupply) {:>> range = "0.56..0.56";}                  
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.0..0.99";}                 
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.4";}                 
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}                
                }
            }
        """)
        solver.propagate()
        // assertTrue(status.reports.isEmpty(), "Exceptions: ${status.reports}")
        assertEquals(0.06666, global.resolveVar("l::c2::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.2, global.resolveVar("l::c2::securityOfSupply")!!.max(), 0.0001)
    }

    @Test
    fun astProductIsATest2() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = 1.0 - productOverSubclasses(1.0 - securityOfSupply);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = 1.0 - productOverSubclassesNotTransitive(1.0 - securityOfSupply);               
                } 
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";}
                }
                type c3 :> c1; 
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.3";}
                }
                type c5 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.664,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.min(), 0.0001)
        assertEquals(0.664,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.max(), 0.0001)
        assertEquals(0.2,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.min(), 0.0001)
        assertEquals(0.2,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.max(), 0.0001)
    }

    @Test
    fun astProductIsATest2EvalDown() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                class c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: Ranges::RealInRange  = 1.0 - productOverSubclasses(1.0 - securityOfSupply) {:>> range = "0.664..0.664";}                
                }
                class c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.0..0.99";}                
                }
                class c3 :> c1;
                class c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.3";}               
                }
                class c5 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}                
                }
            }
        """)
        solver.propagate()
        // assertTrue(status.reports.isEmpty(), "Exceptions: ${status.reports}")
        assertEquals(0.2, global.resolveVar("l::c2::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.2, global.resolveVar("l::c2::securityOfSupply")!!.max(), 0.0001)
    }

    @Test
    fun astProductIsATest3() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = productOverSubclasses(a*b);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = productOverSubclassesNotTransitive(a*b);            
                }
                type c2 :> c1 {
                    feature a: Ranges::RealInRange  {:>> range = "0.8..0.8";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}             
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.7..0.7";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}             
                }
                type c5 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.6..0.6";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}             
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.042,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.min(), 0.0001)
        assertEquals(0.042,
            global.resolveVar("l::c1::resultingSecurityOfSupply")!!.max(), 0.0001)
        assertEquals(0.4,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.min(), 0.0001)
        assertEquals(0.4,
            global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.max(), 0.0001)
    }

    @Test
    fun astProductIsATest3EvalDown() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: Ranges::RealInRange  = productOverSubclasses(a*b) {:>> range = "0.042..0.042";}
                }
                type c2 :> c1 {
                    feature a: Ranges::RealInRange  {:>> range = "0.01..1.0";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.7..0.7";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}
                }
                type c5 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.6..0.6";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}
                }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.any { it.kind.ordinal > Issue.Kind.WARN.ordinal }, "Reports: ${status.issues}")
        // assertTrue(status.reports.isEmpty(), "Exceptions: ${status.reports}")
        assertEquals(0.8, global.resolveVar("l::c2::a")!!.min(), 0.0001)
        assertEquals(0.8, global.resolveVar("l::c2::a")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATestWithoutExpression() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply: ScalarValues::Real = sumOverSubclasses(securityOfSupply);
                    feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(securityOfSupply); 
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";}
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.4";} 
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";} 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.5, global.resolveVar("l::c1::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.6, global.resolveVar("l::c1::securityOfSupply")!!.max(), 0.0001)
        assertEquals(0.5, global.resolveVar("l::c1::securityOfSupply2")!!.min(), 0.0001)
        assertEquals(0.6, global.resolveVar("l::c1::securityOfSupply2")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATestWithoutExpressionEvalDown() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature needsOtherName: Ranges::RealInRange  = sumOverSubclasses(securityOfSupply) {:>> range = "0.5..0.5";}                 
                } 
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";}               
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.1..0.5";}                
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}                
                }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.any { it.kind.ordinal >= Issue.Kind.WARN.ordinal }, "Reports: ${status.issues}")
        // assertTrue(status.reports.isEmpty(), "Exceptions: ${status.reports}")
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATest() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1  :> Base::Anything{
                    feature securityOfSupply:  ScalarValues::Real = sumOverSubclasses(1.0-securityOfSupply); 
                    feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(1.0-securityOfSupply); 
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.4";} 
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";} 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.4,
            global.resolveVar("l::c1::securityOfSupply")!!.min(), 0.0001)
        assertEquals(1.5,
            global.resolveVar("l::c1::securityOfSupply")!!.max(), 0.0001)
        assertEquals(1.4,
            global.resolveVar("l::c1::securityOfSupply2")!!.min(), 0.0001)
        assertEquals(1.5,
            global.resolveVar("l::c1::securityOfSupply2")!!.max(), 0.0001)
    }


    @Test
    fun astSumIsATestWithAttribute() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                   feature securityOfSupply: ScalarValues::Real = sumOverSubclasses(1.0-securityOfSupply);
                   feature securityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(1.0-securityOfSupply);                 
                } 
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";}                
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.4";}                 
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}                 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.4, global.resolveVar("l::c1::securityOfSupply")!!.min(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c1::securityOfSupply")!!.max(), 0.0001)
        assertEquals(1.4, global.resolveVar("l::c1::securityOfSupply2")!!.min(), 0.0001)
        assertEquals(1.5, global.resolveVar("l::c1::securityOfSupply2")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATestInt() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply: ScalarValues::Integer = sumOverSubclasses(securityOfSupply);                 
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "2..2";}                
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "3..4";}                 
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::IntegerInRange  {:>> range = "4..4";}                 
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(5, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.idd().min)
        assertEquals(6, global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.idd().max)
    }

    @Test
    fun astSumIsATestEvalDown() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature needsOtherName: Ranges::RealInRange  = sumOverSubclasses(1.0-securityOfSupply) {:>> range = "1.5..1.5";} 
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.0..1.0";} 
                }
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";} 
                }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.any{ it.kind.ordinal >= Issue.Kind.WARN.ordinal }, "Reports: ${status.issues}")
        // there are, however, exceptions:
        // assertTrue(status.reports.isEmpty(), "Exceptions: ${status.reports}")
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.3,
            global.resolveVar("l::c3::securityOfSupply")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATest2() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = 3.0 - sumOverSubclasses(1.0-securityOfSupply);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = 3.0 - sumOverSubclassesNotTransitive(1.0-securityOfSupply);                    
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.3..0.3";} 
                }
                type c5 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";} 
                }
            }
        """)
        // print(resolveName<Expression>("l::c2::securityOfSupply"))
        solver.propagate()
        assertNoIssues()
        assertEquals(0.9, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.min(), 0.0001)
        assertEquals(0.9, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.max(), 0.0001)
        assertEquals(2.2, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.min(), 0.0001)
        assertEquals(2.2, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATest2EvalDown() = testSession("Ranges") {
        loadKerML(""" 
            package l { 
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: Ranges::RealInRange  = 3.0 -  sumOverSubclasses(1.0-securityOfSupply) {:>> range = "0.9..0.9";}
                }
                type c2 :> c1 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.2..0.2";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.0..1.0";} 
                }
                type c5 :> c3 {
                    feature securityOfSupply: Ranges::RealInRange  {:>> range = "0.4..0.4";}
                }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.any { it.kind.ordinal == Issue.Kind.WARN_INCONSISTENCY.ordinal }, "${status.issues}")
        assertEquals(0.3, global.resolveVar("l::c4::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.3, global.resolveVar("l::c4::securityOfSupply")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATest3() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: ScalarValues::Real = sumOverSubclasses(a*b);
                    feature resultingSecurityOfSupply2: ScalarValues::Real = sumOverSubclassesNotTransitive(a*b);
                } 
                type c2 :> c1 {
                    feature a: Ranges::RealInRange  {:>> range = "0.8..0.8";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.7..0.7";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}                   
                }
                type c5 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.6..0.6";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}
                }
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.05, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.min(), 0.0001)
        assertEquals(1.05, global.resolveVar("l::c1::resultingSecurityOfSupply")!!.max(), 0.0001)
        assertEquals(0.4, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.min(), 0.0001)
        assertEquals(0.4, global.resolveVar("l::c1::resultingSecurityOfSupply2")!!.max(), 0.0001)
    }

    @Test
    fun astSumIsATest3EvalDown() = testSession("Ranges") {
        loadKerML(""" 
            package l {
                type c1 :> Base::Anything {
                    feature resultingSecurityOfSupply: Ranges::RealInRange  = sumOverSubclasses(a*b) {:>> range = "1.05..1.05";} 
                }
                type c2 :> c1 {
                    feature a: Ranges::RealInRange  {:>> range = "0.8..0.8";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";}
                }
                type c3 :> c1;
                type c4 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.01..1.0";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";} 
                }
                type c5 :> c3 {
                    feature a: Ranges::RealInRange  {:>> range = "0.6..0.6";}
                    feature b: Ranges::RealInRange  {:>> range = "0.5..0.5";} 
                }
            }
        """)
        solver.propagate()
        assertTrue(status.issues.any { it.kind.ordinal >= Issue.Kind.WARN.ordinal }, "Reports: ${status.issues}")
        assertEquals(0.7, global.resolveVar("l::c4::a")!!.min(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c4::a")!!.max(), 0.0001)
    }

    /**
     * FAILS after changes in:
     * - Quantity.kt --> constrain returns an empty set if so ... and not the specified value.
     * There is eventually a problem with astSumIsA as when the function is called its
     * parameters are from other classes and eventually not yet computed (should be initialized, however).
     */
    @Test
    fun astSumIsATestWithUnits() = testSession("ISQ", "Ranges") {
        loadKerML(""" 
             package l {
                type c1 :> Base::Anything {
                    feature securityOfSupply: ISQ::LengthValue = sumOverSubclasses(length);                 
                }
                type c2 :> c1 {
                    feature length: ISQ::LengthValue  {:>> range = "0.2..0.2";} 
                }
                type c3 :> c1 {
                    feature length: ISQ::LengthValue  {:>> unit = "cm"; :>> range = "30.0..30.0";}                 
                }
                type c4 :> c1 {
                    feature length: ISQ::LengthValue  {:>> unit = "dm"; :>> range = "4.0..4.0";}                  
                }
             }
        """)
        solver.propagate()
        assertEquals(0, status.issues.size, "Reports: ${status.issues}")
        assertEquals(0.9, global.resolveVar("l::c1::securityOfSupply")!!.min(), 0.0001)
        assertEquals(0.9, global.resolveVar("l::c1::securityOfSupply")!!.max(), 0.0001)
        assertEquals("m", global.resolveVar("l::c1::securityOfSupply")!!.vectorQuantity.unit.toString())
    }


    @Test @Ignore // Issue: #240, re-write in either KerML or SysMD
    fun kpiTest() = testSession {
        loadKerML(
            """
                class Metric; // Inheritance from Element leads to overloading of Element::Availability ...
                Metric hasA
                    feature value: Ranges::RealInRange {:>> range = "0.0 .. 1.0";} 
                    feature weight: Ranges::RealInRange {:>> range = "0.0 .. 1.0";}
                class Realizability isA ScalarValues::Quality;
                class RealizabilityMetric isA Metric;
                RealizabilityMetric hasA
                    feature value: Ranges::RealInRange = sumOverSubclasses(weight*value) {:>> range = "0..1";} 
                    feature value2: Ranges::RealInRange = sumOverSubclassesNotTransitive(weight*value) {:>> range = "0..1";} 
                    feature weight: Ranges::RealInRange = 0.1 {:>> range = "0..1";} 
                    feature weightsum: Ranges::RealInRange = sumOverSubclasses(weight) {:>> range = "0..2";} 
                    feature weightsum2: Ranges::RealInRange = sumOverSubclassesNotTransitive(weight) {:>> range = "0..2";}
                    feature rightWeightSum: ScalarValues::Requirement = (weightsum >= 0.999999) and (weightsum <= 1.000001).

                Realizability hasA
                    feature weight: Ranges::RealInRange {:>> range = "0 .. 1";}
                    feature RealizabilityMetrics: [1..2] RealizabilityMetric; // ??? We need to define Vectors or so ...
                    feature weightedValue: Ranges::RealInRange = sumOverParts(weight*value) {:>> unit = "%"; :>> range = "0 .. 100";} 
                    feature weightedValue2: Ranges::RealInRange = sumOverPartsNotTransitive(weight*value) {:>> unit = "%"; :>> range = "0 .. 100";} 

                class Effort :> RealizabilityMetric.
                Effort hasA
                    feature weight: ScalarValues::Real = 0.2;
                    feature value: ScalarValues::Real = 0.55;

                class Availability isA RealizabilityMetric.
                Availability hasA
                    feature weight: Ranges::RealInRange = 0.3 {:>> range = "0 .. 1";}
                    feature value: Ranges::RealInRange = 0.3 {:>> range = "0 .. 1";}

                class Scalability isA RealizabilityMetric.
                Scalability hasA
                    feature weight: Ranges::RealInRange = 0.2 {:>> range = "0 .. 1";}
                    feature value: Ranges::RealInRange = 0.7 {:>> range = "0 .. 1";}

                class Implementability :> RealizabilityMetric {
                    feature weight: Ranges::RealInRange = 0.2 {:>> range = "0 .. 1";}
                    feature value: Ranges::RealInRange = 0.7 {:>> range = "0 .. 1";}
                }

                class BoundaryConditions isA RealizabilityMetric {
                    feature weight: Ranges::RealInRange = 0.1 {:>> range = "0 .. 1";}
                    feature value: Ranges::RealInRange = 0.9 {:>> range = "0 .. 1";} 
                }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value")!!.min(), 0.00001)
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value")!!.max(), 0.00001)
        assertEquals(1.00, global.resolveVar("RealizabilityMetric::weightsum")!!.max(), 0.00001)
        assertEquals(builder.True, global.resolveVar("RealizabilityMetric::rightWeightSum")!!.vectorQuantity.value)
        assertEquals(0.057, global.resolveVar("Realizability::weightedValue")!!.min(), 0.00001)
        assertEquals(0.114, global.resolveVar("Realizability::weightedValue")!!.max(), 0.00001)
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value2")!!.min(), 0.00001)
        assertEquals(0.57, global.resolveVar("RealizabilityMetric::value2")!!.max(), 0.00001)
        assertEquals(1.00, global.resolveVar("RealizabilityMetric::weightsum2")!!.max(), 0.00001)
        assertEquals(0.057, global.resolveVar("Realizability::weightedValue2")!!.min(), 0.00001)
        assertEquals(0.114, global.resolveVar("Realizability::weightedValue2")!!.max(), 0.00001)
    }

    @Test
    fun kpiTest2() = testSession("Ranges") {
        loadKerML("""
            type RealizabilityMetric :> Base::Anything {
                feature values: ScalarValues::Real = 0.71;
            }
            
            feature f: RealizabilityMetric; 

            type Realizability :> Base::Anything {
                feature RealizabilityMetrics: RealizabilityMetric;
                feature value: Ranges::RealInRange  = 1.0 - sumOverParts(values) {:>> range = "0 .. 100";}
                feature value2: Ranges::RealInRange  = 1.0 - sumOverPartsNotTransitive(values) {:>> range = "0 .. 100";}
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(0.29, global.resolveVar("Realizability::value")!!.min(), 0.00001)
        assertEquals(0.29, global.resolveVar("Realizability::value")!!.max(), 0.00001)
        assertEquals(0.29, global.resolveVar("Realizability::value2")!!.min(), 0.00001)
        assertEquals(0.29, global.resolveVar("Realizability::value2")!!.max(), 0.00001)
    }

    @Test
    fun issueRedefinesTestCompleteWireModel() = testSession("ISQ", "Parts") {
        loadSysMLv2("""
            private import ISQ::*;
            private import ScalarValues::*;
            private import Quantities::*;
            private import Ranges::*;
            part def DomainArchitectureRealization  {
                attribute totalLengthValue: LengthValue = sumOverParts(pathLength);
                attribute totalWeight: MassValue = sumOverParts(wireType::specificWeight * pathLength);
                attribute totalCosts:  AmountOfMoneyValue = sumOverParts(wireType::costsPerMeter * pathLength);
                
                part wireFrontCameraDomain : Network::Wire {
                    part source: Sensors::frontCamera;
                    part target: CU::cameraController;
                    part sourceLocation : LocationsAndSpaces::frontCameraLoc;
                    part targetLocation : LocationsAndSpaces::cameraControllerLoc;
                    part wireType : Network::Ethernet;
                }
                part wireLidarDomain : Network::Wire {
                    part source: Sensors::lidar;
                    part target: CU::radarAndLidarController;
                    part sourceLocation : LocationsAndSpaces::lidarLoc;
                    part targetLocation : LocationsAndSpaces::radarAndLidarControllerLoc;
                    part wireType : Network::Ethernet;
                }
                part wireLongRangeRadarDomain : Network::Wire {
                    part source: Sensors::longRangeRadar;
                    part target: CU::radarAndLidarController;
                    part sourceLocation : LocationsAndSpaces::longRangeRadarLoc;
                    part targetLocation : LocationsAndSpaces::radarAndLidarControllerLoc;
                    part wireType : Network::CANFD;
                }
            }
            
            package Hardware {
                part def Hardware_Base;
            }
            
            
            package LocationsAndSpaces {
                
                part def InstallationSpace {
                    attribute positionOfSpace: CartesianPosition3dVector {:>> range = "-1.5..6.0, -1.25..1.25, -0.5..4.0";} 
                    attribute temperatureRange: ThermodynamicTemperatureValue {:>> unit ="°C"; :>> range="-40 .. 150";} 
                    attribute vibrations: SpeedValue {:>> unit ="mm/s"; :>> range="0 .. 100";}
                    attribute humidity: MassDensityValue {:>> unit ="kg/m^3"; :>> range="0..100000";} 
                    attribute EMI: ElectricPotentialDifferenceValue {:>> unit ="mV"; :>> range="0..100";}
                }
            
                part def Location {
                    part space : InstallationSpace;
                    attribute relativePosition : CartesianPosition3dVector {:>> range= "0.0..4.0, -1.25..1.25, -0.5..4.0";}
                    attribute position: CartesianPosition3dVector = space::positionOfSpace + relativePosition;
                }
                
                part def FrontSpace :> InstallationSpace{
                    attribute positionOfSpace: CartesianPosition3dVector = (-0.9, 0.0, 0.2) [m];
                }
                part def CabinSpace :> InstallationSpace{
                    attribute positionOfSpace: CartesianPosition3dVector  = (0.0, 0.0, 0.2) [m];
                }
                
                part frontCameraLoc: Location {
                    :>> relativePosition = (0.5, 0.0, 0.8) m;
                    part space : CabinSpace;
                }
                part lidarLoc: Location {
                    :>> relativePosition = (0.0, 0.3, 0.0) m;
                    part space : FrontSpace;
                }
                part longRangeRadarLoc: Location {
                    :>> relativePosition = (0.0, 0.6, 0.0) m;
                    part space : FrontSpace;
                }
                part cameraControllerLoc: Location {
                    :>> relativePosition = (1.0, 0.5, -0.2) m;
                    part space : CabinSpace;
                }
                part radarAndLidarControllerLoc: Location {
                    :>> relativePosition = (0.0, 0.3, -0.2) m;
                    part space : CabinSpace;
                }
            }
            
            package Sensors {
            
                part def Sensor :> Hardware::Hardware_Base {
                    attribute measuredQuantityType: String;
                    attribute dataLoad: StorageCapacityValue {:>> unit ="kB"; :>> range="0..100";} 
                }
            
                part def Camera :> Sensor;
                part def Radar :> Sensor;
                
                part frontCamera: Camera;
                part lidar: Sensor;
                part longRangeRadar: Radar;
            }
            
            package CU {
            
                part def ControlUnit :> Hardware::Hardware_Base {
                    attribute severity: DimensionOneValue = 3.0 [1];
                    attribute exposure: DimensionOneValue = 4.0 [1];
                    attribute controllability: DimensionOneValue = 3.0 [1];
                    
                    attribute fclk: FrequencyValue {:>> unit= "MHz"; :>> range= "0.1 .. 10000";} 
                    attribute ipc:  IntegerInRange {:>> range default= "1..10000";}
                    attribute opsPerInstruction: IntegerInRange {:>> range = "1..10000";}
                    attribute FLOPS: FrequencyValue =  fclk * ToReal(opsPerInstruction) * ToReal(ipc) ;
                }
            
                part cameraController: ControlUnit;
                part radarAndLidarController: ControlUnit;
            }
            
            package Network {
                
                part def WireType {
                    attribute specificWeight: ScalarQuantityValue {:>> unit ="g/m"; :>> range default ="1..100";}
                    attribute costsPerMeter: ScalarQuantityValue {:>> unit ="EUR/m"; :>> range default ="0.0..1.0";}
                    attribute transmissionRate: BitRateValue {:>> unit ="Mbit/s"; :>> range default = "0.0001 .. 10000.0";}
                    attribute dataPerFrame: StorageCapacityValue {:>> unit ="B"; :>> range  default ="0..10000";}
                    attribute overheadPerFrame: StorageCapacityValue {:>> unit ="B"; :>> range default ="0..1000";}
                    attribute arbitration: String;
                }
            
                part def Ethernet :> WireType {
                    attribute specificWeight: ScalarQuantityValue {:>> unit ="g/m"; :>> range default ="3..40";}
                    attribute costsPerMeter: ScalarQuantityValue {:>> unit ="EUR/m"; :>> range default ="0.02..0.3";} 
                    attribute transmissionRate: BitRateValue {:>> unit ="Mbit/s"; :>> range default ="0.1..10000";}
                    attribute dataPerFrame: StorageCapacityValue {:>> unit ="B"; :>> range default ="46..1500";}
                    attribute overheadPerFrame: StorageCapacityValue {:>> unit ="B"; :>> range default ="30..30";} 
                }
            
                part def CANFD :> WireType {
                    attribute specificWeight: ScalarQuantityValue {:>> unit ="g/m"; :>> range="25..25";}
                    attribute transmissionRate: BitRateValue {:>> unit ="kB/s"; :>> range="80.0";}
                    attribute costsPerMeter: ScalarQuantityValue {:>> unit ="EUR/m"; :>> range="0.7";}
                    attribute dataPerFrame: StorageCapacityValue {:>> unit ="B"; :>> range="1..64";}
                    attribute overheadPerFrame: StorageCapacityValue {:>> unit ="B"; :>> range="61..87";}
                }
            
                part def Wire {
                    part source: Hardware::Hardware_Base;
                    part target: Hardware::Hardware_Base;
                    part sourceLocation : LocationsAndSpaces::Location;
                    part targetLocation : LocationsAndSpaces::Location;
                    attribute pathLength: LengthValue = cityBlockDistance(sourceLocation::position, targetLocation::position) * 1.4;
                    attribute resistance: ResistanceValue {:>> unit = "Ohm";}
                }
            }
        """
        )
        solver.propagate()
        assertNoIssues()
    }
}

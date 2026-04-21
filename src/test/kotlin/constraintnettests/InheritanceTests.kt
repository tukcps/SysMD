package constraintnettests

import com.github.tukcps.sysmd.cspsolver.getVariableInfo
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.services.check.checkOwnership
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.Range
import util.assertNoIssues
import util.mockup.loadKerML
import util.testSession
import kotlin.test.*

class InheritanceTests {

    /**
     * Inheritance creates a copy of a feature, and as well of its information:
     * - Multiplicity
     * - Specialization
     * There also shall be no duplicates, and the inherited features shall be marked as transient.
     */
    @Test fun cloneInheritedFeature() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything { feature x [1..2]; }
            type b :> a; 
        """)
        assertNoIssues()
        val ax: Feature = global.resolve("a::x")!!.member()!!
        val bx: Feature = global.resolve("b::x")!!.member()!!
        assertEquals(1, ax.ownedRelationship.filter { it is Specialization }.size)
        assertEquals(2, ax.ownedElement.size)
        assertEquals(1, ax.ownedElement.filter { it is Multiplicity }.size)
        assertEquals(IntegerRange(1, 2), ax.multiplicityRange)

        assertEquals(1, bx.ownedElement.filter { it is Multiplicity }.size)
        assertEquals(IntegerRange(1, 2), bx.multiplicityRange)
        assertEquals(1, bx.ownedRelationship.filter { it is Specialization }.size)
        assertEquals(2, bx.ownedElement.size)
        // assertTrue(bx.getOwnedElementOfType<Multiplicity>()!!.isImpliedIncluded)
        // assertTrue(bx.getOwnedElementOfType<Specialization>()!!.isImpliedIncluded)
        assertNotNull(get(bx.getOwnedElementOfType<Multiplicity>()!!.elementId!!))
        assertNotNull(get(bx.getOwnedElementOfType<Specialization>()!!.elementId!!))
        assertNotEquals(ax, bx)
        assertNotEquals(ax.elementId, bx.elementId)
        checkOwnership()
        export()
    }

    /**
     * Elements inherit features with type and multiplicity from its general type,
     * and become visible by the same name in the respective element.
     */
    @Test
    fun inheritFeatureTest() = testSession("ScalarValues") {
        loadKerML(""" 
            type t1 :> Base::Anything {
                feature f [2]: ScalarValues::Natural; 
            }
            type t2 :> t1; 
        """
        )
        // element 2 has a property inherited
        // it has the name p and is actually the one from the element
        assertNoIssues()
        val t2: Type = global.resolve("t2")?.member()!!
        assertNotNull(t2)
        val t2features = t2.getOwnedElementsOfType<Feature>()
        assertNotNull(t2features)
        val t2f = global.resolve("t2::f")?.member<Feature>()!!
        assertNotNull(t2f)
        assertEquals(IntegerRange(2, 2), t2f.multiplicityRange)
        assertEquals("ScalarValues::Natural", t2f.type.firstOrNull()?.qualifiedName)
    }


    @Test
    fun inheritFeatureTest2() = testSession("ScalarValues") {
        loadKerML(
            """ 
            type t1 :> Base::Anything {
                feature f [2]: ScalarValues::Integer; 
            }
            type t2 :> t1; 
            type t3 :> t2; 
            type t4 :> t3; 
        """)
        // element 2 has a property inherited
        // it has the name p and is actually the one from element
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        val t2 = global.resolve("t2")?.member<Type>()
        assertNotNull(t2)
        val t2features = t2.getOwnedElementsOfType<Feature>()
        assertNotNull(t2features)
        val t2f = global.resolve("t2::f")!!.member<Feature>()
        assertNotNull(t2f)
        assertEquals(IntegerRange(2, 2), t2f.multiplicityRange)
        assertEquals("ScalarValues::Integer", t2f.type.firstOrNull()?.qualifiedName)

        val t3 = global.resolve("t3")?.member<Type>()
        assertNotNull(t3)
        val t3features = t3.getOwnedElementsOfType<Feature>()
        assertNotNull(t2features)
        val t3f = global.resolve("t3::f")?.member<Feature>()
        assertNotNull(t3f)
        assertNotEquals(t2f, t3f) // they shall have at least different id
        assertEquals(IntegerRange(2, 2), t3f.multiplicityRange)
        assertEquals("ScalarValues::Integer", t3f.type.firstOrNull()?.qualifiedName)
    }

    @Test
    fun featureInheritsFromType() = testSession("ScalarValues") {
        loadKerML("""
            type t :> Base::Anything {
                feature f;
            }
            feature f: t; 
        """)
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        val ff = global.resolve("f::f")
        assertNotNull(ff)
    }


    /**
     * - Inheritance does not overwrite existing features.
     *  are restricted to the tightest interval.
     * - When a specification is refined, a new property is created.
     */
    @Test
    fun getPropertiesTest2() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            type t1 :> Base::Anything { 
                feature f: Ranges::RealInRange {:>> range = "1 .. 2";}
            }
            type t2 :> t1 { 
                feature f: Ranges::RealInRange {:>> range = "1 .. 1.5";}
            }
        """)
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "${status.issues}")
        val t1 = global.resolve("t1")?.member<Type>()
        val t1f = t1?.resolve("f")?.member<Type>()
        val t2 = global.resolve("t2")?.member<Type>()
        val t2f = t2?.resolve("f")
        /*
        assertEquals(Range(1.0 .. 1.5), t2?.rangeSpecs[0]  )
        assertEquals(Range(1.0 .. 2.0), element?.getOwned<Feature>(   "p")?.variable?.rangeSpecs?.get(0) ) */
    }



    /**
     * The range of inherited properties may not be wider than the superclass property.
     * Otherwise, an error is written into the status.
     */
    @Test
    fun varianceTest() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            type elem :> Base::Anything { 
                feature p: Ranges::RealInRange {:>> range = "1 .. 2";}
            }
            class elem2 :> elem {
                feature p: Ranges::RealInRange {:>> range = "1.5 .. 2.5";} 
            } 
        """)
        solver.propagate()
        // TODO: Propagate must also consider constraints of superclasses even iff no dependency is computed.
        assertTrue(status.issues.any { it.kind == Issue.Kind.WARN_INCONSISTENCY })
        assertEquals(1, status.issues.size)
    }


    /**
     * Values are propagated across inherited elements.
     * Inherited properties can be accessed by
     *  - its name
     *  - the name of the superclass.name
     */
    @Test
    fun findInheritedPropertiesTest4() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            package lib {
                type elem :> Base::Anything {
                    feature p: Ranges::RealInRange {:>> range = "1 .. 2";}
                }
                type elem2 :> elem {
                    feature e: lib::elem; 
                    feature p2: Ranges::RealInRange = elem::p {:>> range = "1.5 .. 2.5";}  
                    feature p3: Ranges::RealInRange = p {:>> range = "1 .. 4";}
                }
            }
        """)
        assertNoIssues()
        val elem2 = global.resolve("lib::elem2")?.member<Type>()
        assertTrue(elem2!!.resolveVar("p2")!!.aadd().getRange() in Range(1.499..2.001))
    }

    /**
     * The range of inherited properties can be "not specified".
     * Then, no error is thrown, and the range from the superclass' property is used.
     */
    @Test
    fun getInheritedFeaturesTest5() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """ 
            type e1 :> Base::Anything {
                feature p: Ranges::RealInRange {:>> range = "1 .. 2";} 
            }
            class e2 :> e1;
        """)
        assertNoIssues()
        val e2 = global.resolve("e2")?.member<Type>()
        assertNotNull(e2)
        val e = e2.resolveVar("p")
        assertEquals(1.0, e?.rangeSpecs?.get(0)?.min)
    }


    /**
     * We can get a list with all superclasses.
     */
    @Test
    fun getAllSuperclassesTest() = testSession {
        loadKerML("""
            type c1 :> Base::Anything; 
            type c2 :> c1; 
            type c3 :> c2.
            """)
        assertNoIssues()
        val c3 = global.resolve("c3")?.member<Type>()
        val allSupertypes = c3?.allSupertypes(transitive = true)
        assertEquals(3, allSupertypes!!.size)
    }


    @Test
    fun inheritHasATest() = testSession("ScalarValues") {
        loadKerML(catchExceptions = false, input = """
            type e :> Base::Anything {
                feature p: e2; 
            }
            type e2 :> Base::Anything;
            type e3 specializes e; 
        """)
        assertNoIssues()
        val e3 = global.resolve("e3")?.member<Type>()
        val hasAOfE3 = e3?.resolveLocal("p")
        assertNotNull(hasAOfE3)
        val m3Members = e3.visibleMemberships()
        assertEquals(1, m3Members.size)
    }

    /**
     * Shall also deliver inherited properties, not just the own.
     */
    @Test
    fun findFeaturesTest2() = testSession("Ranges") {
        settings.catchExceptions = false
        loadKerML(
            """
                package find {
                    type e :> Base::Anything { 
                        feature p1: Ranges::RealInRange {:>> range = "0..3";}
                    }
                    type e2 :> e { 
                        feature p2: Ranges::RealInRange {:>> range = "1";}
                    }
                }
            """
        )
        assertNoIssues()
        val e2: Type? = global.resolve("find::e2")?.member()
        val e: Type? = global.resolve("find::e")?.member()
        assertNotNull(e)
        val p1: Feature? = global.resolve("find::e2::p1")?.member()
        val p2: Feature? = global.resolve("find::e2::p2")?.member()
        assertEquals("p1", p1!!.declaredName)
        assertEquals("p2", p2!!.declaredName)
        val pE2 = e2!!.member
        assertEquals(2, pE2.size)
    }

    /**
     * We allow definition in arbitrary order.
     * The parser shall leave the reference open and refer to any.
     * After parsing, the initialization shall replace the references to 'anything' with the correct ones.
     */
    @Test
    fun useClassBeforeDefinition() = testSession {
        loadKerML(
            """
            type a :> b;
            type b :> c;
            type c :> Base::Anything;
        """
        )
        assertNoIssues()
        val a: Type? = global.resolve("a")?.member()
        val b: Type? = global.resolve("b")?.member()
        val c: Type? = global.resolve("c")?.member()
        assertNotNull(a)
        assertNotNull(b)
        assertNotNull(c)
        assertTrue(b in a.allSupertypes())
        assertTrue(c in b.allSupertypes())
    }


    /**
     * We allow definition in arbitrary order.
     * The parser resolves unknown references in initialization phase.
     * Inherited features are represented by memberships.
     */
    @Test
    fun useClassBeforeDefinition2() = testSession("ScalarValues") {
        loadKerML(
            """
            type c :> Base::Anything {
                feature f: Base::Anything; 
            }
            type b :> c;
            type a :> b;
        """)
        assertNoIssues()
        val a: Type = global.resolve("a")?.member()!!
        val b: Type = global.resolve("b")?.member()!!
        val c: Type = global.resolve("c")?.member()!!
        assertTrue(b in a.allSupertypes())
        assertTrue(c in b.allSupertypes())
        assertEquals("f", a.member.find { it is Feature }!!.declaredName)
    }


    /**
     * We allow definition in arbitrary order.
     * This test checks that features are cloned properly if definitions are in reverse order.
     * Note that the test might fail randomly depending on implementation.
     */
    @Test
    fun useClassBeforeDefinition3() = testSession {
        loadKerML(
            """
            class a :> b;
            class b :> c;
            class c :> Base::Anything {
                feature f: Base::Anything; 
            }
        """)
        val a = global.resolve("a")!!.member<Type>()!!
        val b = global.resolve("b")!!.member<Type>()!!
        val c = global.resolve("c")!!.member<Type>()!!
        assertTrue(b in a.allSupertypes())
        assertTrue(c in b.allSupertypes())
        assertEquals("f", a.member.find { it is Feature }!!.declaredName)
    }


    /**
     * Features and multiplicity are inherited.
     * They constitute independent variables for the inherited feature and its multiplicity.
     */
    @Test
    fun inheritedFeatureWithMultiplicity() = testSession("ScalarValues") {
        loadKerML("""
            type x :> Base::Anything {
                feature y: Base::Anything [1..3]; 
            }
            type z :> x;
        """)
        assertNoIssues()
        val zy = global.resolve("z::y")?.member<Feature>()
        val xy = global.resolve("x::y")?.member<Feature>()
        val zyv = global.resolve("z::y")
        val xyv = global.resolve("x::y")
        val zym = global.resolve("z::y::cardinality")
        val xym = global.resolve("x::y::cardinality")
        assertNotNull(zy)
        assertTrue(zyv !== xyv)
        assertTrue(xym !== zym) // Actually, needs work
        // assertTrue(zy.member<Feature>()?.multiplicityRange !== xy?.member<Feature>()?.multiplicityRange)
        // assertTrue(zy.member<Feature>()?.ownedSpecialization !== xy?.member<Feature>()?.ownedSpecialization)
        assertTrue(zy.isImpliedIncluded)
    }


    /**
     * Inherited values are cloned.
     * This permits writing to subclass without changing superclass.
     */
    @Test
    fun cloneInheritedValue() = testSession {
        loadKerML(
            """
            package ScalarValues { datatype ScalarValue; datatype Real :> ScalarValue; datatype Integer :> ScalarValue; }
            type x :> Base::Anything { feature y: ScalarValues::Real {:>> range="2.0";} }
            type z specializes x; 
            """
        )
        val zy: Feature? = global.resolve("z::y")?.member()
        val xy: Feature? = global.resolve("x::y")?.member()
        assertNotNull(zy)
        assertNotNull(xy)
        assertTrue(zy.variable !== xy.variable, "Inherited values are independent variables")
        // assertTrue(zy!!.multiplicity !== xy!!.multiplicity)
        // assertTrue(zy!!.expression !== xy!!.expression)
        assertTrue(zy.ownedSpecialization !== xy.ownedSpecialization)
        assertTrue(zy.isImpliedIncluded, "Implicit elements shall be marked transient.")
    }

    @Test
    fun inheritedFeatures2() = testSession("ScalarValues") {
        loadKerML(
            """
            class A :> Base::Anything {
                feature a { feature x [1..10]; }
            }
            
            class B :> A { 
                feature a { feature x [2..8]; }
            }
            
            class C :> A {
                feature a { feature x [3..4]; }
            }
        """
        )
        assertNoIssues()
        val aax = global.resolve("A::a::x")
        val bax = global.resolve("B::a::x")
        val cax = global.resolve("C::a::x")
        val baxm = global.resolveVar("B::a::x::cardinality")
        val caxm = global.resolveVar("C::a::x::cardinality")
        assertNotEquals(aax, bax)
        assertNotEquals(aax, cax)
        assertNotEquals(bax, cax)
        assertEquals(IntegerRange(2, 8), baxm?.intSpecs!!.first())
        assertEquals(IntegerRange(3, 4), caxm?.intSpecs!!.first())
    }

    // Fixed with 2.0.30.
    @Test fun overrideDerivedPropertyTest() = testSession("ISQ", "Ranges") {
        loadKerML("""           
            type Coin :> Base::Anything {
                feature diameter: ISQ::LengthValue {:>> range = "5..200"; :>> unit = "mm";}
                feature circumference: ISQ::LengthValue = diameter*3.141 {:>> unit = "mm";} // 15.7 .. 628.2 mm 
            }

            feature oneEuroCoin : Coin { 
                feature diameter: ISQ::LengthValue = 23.25 mm { :>> unit = "mm"; } 
                // circumference is inherited. Must be re-evaluated with correct diameter.
                // Expected behavior:  re-evaluate dependency in new scope, but without changing diameter of Coin. 
            }
        """)
        assertNoIssues()
        solver.propagate()
        val oneEuroCircumference = global.resolveVar("oneEuroCoin::circumference")!!
        val oneEuroDiameter = global.resolveVar("oneEuroCoin::diameter")!!
        val coinCircumference = global.resolveVar("Coin::circumference")!!
        val coinDiameter = global.resolveVar("Coin::diameter")!!
        assertEquals(73.03, oneEuroCircumference.vectorQuantity.getMinAsDouble() * 1000.0, 0.01)
        assertEquals(23.25, oneEuroDiameter.vectorQuantity.getMinAsDouble() * 1000.0, 0.01)
        assertEquals(15.7, coinCircumference.vectorQuantity.getMinAsDouble() * 1000.0, 0.01)
        assertEquals(5.0, coinDiameter.vectorQuantity.getMinAsDouble() * 1000.0, 0.001)
    }


    /**
     * Each overridden value and expression has independent variables;
     * subclass inherits expression, but does not overwrite the value of superclass.
     */
    @Test
    fun overrideDerivedPropertyTest2() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """
            type Coin :> Base::Anything {
                feature diameter: Ranges::RealInRange {:>> range = "2..100";}
                feature circumference: ScalarValues::Real = diameter;
            }

            type OneEuroCoin :> Coin {
                feature diameter: Ranges::RealInRange  {:>> range = "23..23";}
                feature circumference: ScalarValues::Real = diameter * 2.0.
                // circumference is inherited. Must be re-evaluated with correct diameter.
                // Expected:  re-evaluate dependency in new scope, but without changing diameter of Coin. 
            }
        """)
        assertNoIssues()
        solver.propagate()
        val oneEuroCircumference = global.resolveVar("OneEuroCoin::circumference")!!
        val oneEuroDiameter = global.resolveVar("OneEuroCoin::diameter")!!
        val coinCircumference = global.resolveVar("Coin::circumference")!!
        val coinDiameter = global.resolveVar("Coin::diameter")!!
        assertEquals(46.0, oneEuroCircumference.vectorQuantity.getMinAsDouble(), 0.00000001)
        assertEquals(23.0, oneEuroDiameter.vectorQuantity.getMinAsDouble(), 0.00000001)
        assertEquals(2.0, coinCircumference.vectorQuantity.getMinAsDouble(), 0.00000001)
        assertEquals(2.0, coinDiameter.vectorQuantity.getMinAsDouble(), 0.00000001)
    }

    /**
     * Subclasses can have constrained variables that do not affect the superclass.
     */
    @Test
    fun inheritanceConstraintTestFromSubclasses() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """
                type a specializes Base::Anything {
                    feature p: ScalarValues::Real; 
                }
                type b specializes a { 
                    feature p: Ranges::RealInRange  {:>> range = "2..2";}
                }
            """.trimIndent()
        )
        // b shall be 2, a remains Real.
        assertEquals(2.0, global.resolveVar("b::p")?.min())
        assertEquals(builder.Reals.getRange(), global.resolveVar("a::p")?.vectorQuantity?.value?.asAadd()?.getRange())
    }


    /**
     * Subclasses can have constrained variables that do not affect the superclass.
     */
    @Test
    fun inheritanceConstraintTestFromSuperclass() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """
            type a :> Base::Anything { feature p: Ranges::RealInRange  {:>> range = "2..2";}}
            type b :> a { feature p: ScalarValues::Real; }
        """
        )
        // b shall be constrained to 2, a remains 2.
        assertEquals(
            Range(2.0..2.0),
            global.resolveVar("a::p")?.vectorQuantity?.value?.asAadd()?.getRange()
        )
        assertEquals(
            Range(2.0..2.0),
            global.resolveVar("a::p")?.vectorQuantity?.value?.asAadd()?.getRange()
        )
    }

    /**
     * Subclasses must have subset of superclass-values.
     */
    @Test
    fun inheritanceConstraintTestContradiction() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """
            type a specializes Base::Anything {
                feature p: Ranges::RealInRange  {:>> range = "1..1";} 
            }
            type b specializes a {
                feature p: Ranges::RealInRange {:>> range = "2..2";}
            }
        """
        )
        solver.propagate()
        assertEquals(1.0, global.resolveVar("a::p")!!.min())
        assertEquals(1.0, global.resolveVar("a::p")!!.max())
        assertEquals(2.0, global.resolveVar("b::p")!!.min())
        assertEquals(2.0, global.resolveVar("b::p")!!.max())
        assertEquals(status.issues.firstOrNull()?.kind, Issue.Kind.WARN_INCONSISTENCY)
    }

    /**
     * Indirect constraints must be considered as 'writing' the addressed object directly
     * in an inheritance tree, not the superclass definitions.
     */
    @Test
    fun inheritanceConstraintTestIndirect() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """
            type a :> Base::Anything { feature p: ScalarValues::Real; }
            type b :> a; 
            type x :> Base::Anything { feature a: Ranges::RealInRange = b::p {:>> range = "2..2";}}
        """)
        assertNoIssues()
        solver.propagate()
        // 'a' is 2 and propagated to b::p (that is then 2)
        // 'b::p' is NOT propagated to the superclass 'a'.
        // Hence:
        // - b::p = 2.0.
        // - a::p = Reals.
        val xa = global.resolveVar("x::a")!!
        val bp = global.resolveVar("b::p")!!
        val ap = global.resolveVar("a::p")!!
        assertEquals(Range(2.0, 2.0), xa.range()) // directly via eval-up
        assertEquals(Range(2.0, 2.0), bp.range()) // must go here
        assertEquals(builder.Reals.getRange(), ap.range())   // and not here
    }


    @Test
    fun realConstraintTest() = testSession("Occurrences", "Ranges") {
        loadKerML(
            """
            package Test { 
                class m { feature v: Ranges::RealInRange {:>> range = "1..5";} }
                class n :> m {feature v: Ranges::RealInRange {:>> range = "6..6";}}
            }
        """
        )
        // The inconsistency / violation of Liskov Principle must be reported.
        assertEquals(Issue.Kind.WARN_INCONSISTENCY, status.issues.firstOrNull()?.kind)
    }

    @Test
    fun realConstraintTest2() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """
            package Test { 
                type m :> Base::Anything { feature v: Ranges::RealInRange {:>> range = "1..5";} }
                type n :> m {feature v: Ranges::RealInRange {:>> range = "1..5";} }
            }
        """
        )
        // The inconsistency / violation of Liskov Principle must be reported.
        solver.propagate()
        assertEquals(0, status.issues.size)
    }


    @Test
    fun realConstraintTest3() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            """
            package Test {
                type super :> Base::Anything { feature v: Ranges::RealInRange {:>> range = "1..2";} }
                type sub specializes super { feature v: Ranges::RealInRange = 1.5 {:>> range = "1.5..1.5";} }
            }
        """
        )
        // The inconsistency / violation of Liskov Principle must be reported.
        solver.propagate()
        assertEquals(0, status.issues.size)
        assertEquals(Range(1.5, 1.5), global.resolveVar("Test::sub::v")?.vectorQuantity?.value?.asAadd()?.getRange())
    }


    @Test
    fun integerConstraintTest() = testSession("ScalarValues", "Ranges") {
        loadKerML(
            ("""
            package Test { 
                type m :> Base::Anything {feature v: Ranges::IntegerInRange {:>> range = "1..5";} }
                type n :> m {feature v: Ranges::IntegerInRange {:>> range = "6..6";} }; 
            }
        """.trimIndent())
        )
        // The inconsistency / violation of Liskov Principle must be reported.
        solver.propagate()
        assertEquals(status.issues.firstOrNull()?.kind, Issue.Kind.WARN_INCONSISTENCY)
        val v = global.resolveVar("Test::n::v")?.range<Long>()
        assertEquals(IntegerRange(6, 6), v)
    }

    @Test
    fun integerConstraintTest2() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            type m :> Base::Anything {
                feature v: Ranges::IntegerInRange {:>> range = "1..5";}
            }
            type n :> m {
                feature v: Ranges::IntegerInRange {:>> range = "2..2";}
            }
        """)
        assertNoIssues()
        assertEquals(IntegerRange(2, 2), global.resolveVar("n::v")?.vectorQuantity?.value?.asIdd()?.getRange())
    }


    /**
     *  features are inherited.
     */
    @Test
    fun hasAFeatureTest2() = testSession("ScalarValues") {
        settings.catchExceptions = false
        loadKerML(
            """
            type a :> Base::Anything; ; 
            type b specializes a {
                feature xx: x; 
            } 
            """.trimIndent()
        )
        initialize()
        val b: Type? = global.resolve("b")?.member()
        assertTrue(b is Type)
        assertNotNull(b.getOwnedElement("xx"))
    }


    /**
     * Features are inherited including Multiplicity.
     */
    @Test
    fun issue_featureInheritanceMultiplicity() = testSession("ScalarValues") {
        loadKerML("""
            type Car specializes Base::Anything {
                feature wheels: Base::Anything[1..4] ; 
            }
            class VW specializes Car.
        """)
        solver.propagate()
        assertNoIssues()
        val vwWheels: Feature? = global.resolve("VW::wheels")?.member()
        assertNotNull(vwWheels)
        assertEquals(IntegerRange(1.0, 4.0), vwWheels.multiplicityRange)
    }


    /**
     * Features are inherited including Multiplicity.
     */
    @Test
    fun issue_featureInheritanceMultiplicity2() = testSession("ScalarValues") {
        loadKerML("""
            type Car  :> Base::Anything { feature wheels: Base::Anything [1..4]; }
            type Audi :> Car { feature wheels:  Base::Anything[2 .. 5]; }
        """)
        solver.propagate()
        assertEquals(status.issues.firstOrNull()?.kind, Issue.Kind.WARN_INCONSISTENCY) // Inconsistency !!!
        val audiWheels: Feature? = global.resolve("Audi::wheels")?.member()
        assertEquals(IntegerRange(2, 5), audiWheels?.multiplicityRange)
    }


    /**
     * Multiplicity with subclasses is correctly transferred to multiplicity property.
     */
    @Test
    fun issue_featureInheritanceMultiplicity3() = testSession("ScalarValues") {
        loadKerML("""
            type Car  :> Base::Anything { feature wheels: Base::Anything [1 .. 4]; }
            type VW   :> Car { feature wheels: Base::Anything [2 .. 3]; }
            type Audi :> Car { feature wheels: Base::Anything [2 .. 4]; }
        """)
        solver.propagate()
        assertNoIssues()
        val audiWheels: Feature? = global.resolve("Audi::wheels")?.member()
        val vwWheels: Feature? = global.resolve("VW::wheels")?.member()
        val carWheels: Feature?  = global.resolve("Car::wheels")?.member()

        assertEquals(IntegerRange(2, 4), audiWheels?.multiplicityRange)
        assertEquals(IntegerRange(2, 3), vwWheels?.multiplicityRange)
        assertEquals(IntegerRange(1, 4), carWheels?.multiplicityRange)
    }

    @Test
    fun subclassWithoutConstraintsInheritsConstraints() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            type general :> Base::Anything {
                feature x: Ranges::RealInRange = 1.5 {:>> range = "1 .. 2";} 
            }
            type special :> general {
                feature x: Ranges::RealInRange; 
            }
        """)
        assertNoIssues()
    }


    @Test
    fun subclassWithoutConstraintsInheritsConstraints2() = testSession("ISQ") {
        loadKerML("""
            type general :> Base::Anything {
                feature y: ScalarValues::Real; 
                feature x:  ISQ::DurationValue = 1.0 [ms] {
                    :>> range = "0 .. *"; 
                    :>> unit  = "ms"; 
                } 
            }
            class special :> general; 
        """)
        assertNoIssues()
    }

    @Test
    fun addVariablesTest() = testSession("ISQ") {
        loadKerML("""
            feature a: Base::Anything; // No var
            type b {
                feature c: Quantities::ScalarQuantityValue [3 .. 4] { :>> unit = "m"; :>> range = "1..2"; } // range, unit, cardinality 
                feature d: ScalarValues::Real; // b::d
            }
            type d :> b; 
        """)
        val d = global.resolve("b")!!
        val vars = getVariableInfo(this)
        assertEquals(13, vars.size)
    }

}
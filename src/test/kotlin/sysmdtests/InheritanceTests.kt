package sysmdtests

import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.Range
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.SysMDInconsistency
import com.github.tukcps.sysmd.model.kerml.Element
import com.github.tukcps.sysmd.model.kerml.Class
import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.model.kerml.getOwned
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.services.inheritance.getAllInheritedFeatures
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.findAllOwnedElements
import com.github.tukcps.sysmd.services.resolve.findOwnedElement
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import kotlin.test.*


class InheritanceTests {
    /**
     * Elements inherit properties from superclass, and
     * - become visible by the same name in the respective element.
     */
    @Test
    fun agilaGetPropertiesTest() = testSession {
        loadSysMD( """ 
            package lib {
                class elem {
                    attribute  p: ScalarValues::Real(1 .. 2); 
                }
                class elem2 isA elem; 
            }
        """.trimIndent())
        // element 2 has a property inherited
        // it has the name p and is actually the one from element
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        val element2 = global.resolve<Element>("lib::elem2") as Type
        val propertiesOf2 = getAllInheritedFeatures(element2)
        assertEquals(1, propertiesOf2.size)
    }

    @Test
    fun featureInheritsFromClass() = testSession(loadKerML = false) {
        loadSysMD("""
            // Bare minimum of KerML Libraries ... 
            package ScalarValues { datatype ScalarValue; datatype Real :> ScalarValue; datatype Integer :> ScalarValue; }
            class c {
                feature cf: ScalarValues::Real(0..2) = 0.3;
            }
            feature f: c; 
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        assertTrue {
            val f = global.resolve<Feature>("f::cf")
            f?.variable?.ast != null
        }
    }


    /**
     * - Inherited properties are restricted to the tightest interval.
     * - When a specification is refined, a new property is created.
     */
    @Test
    fun getPropertiesTest2() = testSession {
        loadSysMD(input = """
            class elem { feature p: ScalarValues::Real(1 .. 2); }
            class elem2 :> elem { feature p: ScalarValues::Real(1 .. 1.5); }
            """)
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Error messages: ${status.exceptions}")
        val element2 = global.resolve<Class>("elem2")  !!
        val element = global.resolve<Class>("elem") !!
        val p2 = element2.resolveVar("p") !!
        assertEquals(Range(1.0 .. 1.5), p2.rangeSpecs[0]  )
        assertEquals(Range(1.0 .. 2.0), element.getOwned<Feature>(   "p")?.variable?.rangeSpecs?.get(0) )
    }



    /**
     * The range of inherited properties may not be wider than the superclass property.
     * Otherwise, an error is written into the status.
     */
    @Test
    fun agilaGetPropertiesTest3() = testSession {
        +"""
            class elem { 
                feature p: ScalarValues::Real(1 .. 2); 
            }
            class elem2 :> elem {
                feature p: ScalarValues::Real(1.5 .. 2.5); 
            } """
        propagate()
        // TODO: Propagate must also consider restrictions of superclasses even iff no dependency is computed.
        assertTrue(status.exceptions.any { it  is SysMDInconsistency })
        assertEquals(1, status.exceptions.size)
    }


    /**
     * Values are propagated across inherited elements.
     * Inherited properties can be accessed by
     * - its name
     * - the name of the superclass.name
     */
    @Test
    fun findInheritedPropertiesTest4() = testSession {
        loadSysMD("""
            package lib {
                class elem {
                    feature p: ScalarValues::Real(1 .. 2); 
                }
                class elem2 specializes elem {
                    feature e: lib::elem; 
                    feature p2: ScalarValues::Real(1.5 .. 2.5) = elem::p;  
                    feature p3: ScalarValues::Real(1 .. 4) = p;
                }
            }""")
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        val elem2 = global.resolve<Type>("lib::elem2")
        assertTrue(elem2!!.resolveVar("p2")!!.aadd().getRange() in Range(1.499 .. 2.001) )
    }

    /**
     * The range of inherited properties can be "not specified".
     * Then, no error is thrown, and the range from the superclass' property is used.
     */
    @Test
    fun agilaGetPropertiesTest5() = testSession {
        loadSysMD (""" 
            class e1 {
                feature p: ScalarValues::Real(1 .. 2); 
            }
            class e2 :> e1; 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val e2 = global.resolve<Type>("e2")
        assertNotNull(e2)
        val e =  e2.resolveVar("p")
        assertEquals(1.0, e?.rangeSpecs?.get(0)?.min)
    }


    /**
     * We can get a list with all superclasses.
     */
    @Test fun getAllSuperclassesTest() = testSession(loadKerML = false) {
        loadSysMD("""
            class c1; 
            class c2 :> c1; 
            class c3 :> c2.
            """)
        assertEquals(0, status.exceptions.size, "Error message: ${status.exceptions}")
        val c3 = global.resolve<Type>("c3")
        val allSupertypes = c3?.allSupertypes(transitive = true)
        assertEquals(3, allSupertypes!!.size)
    }


   @Test fun inheritHasATest() = testSession {
       loadSysMD(catchExceptions = false, input = """
                package InheritHas {
                    class e;
                    class e2;
                    class e3 specializes e; 
                }

                InheritHas::e hasA
                    feature p: Global::InheritHas::e2.
                // now e3 isA e, and e hasA e2, so e3 must have a e2 too.
           """.trimIndent())
       assertEquals(0, status.exceptions.size, status.exceptions.toString())
       val e3 = global.resolve<Type>("InheritHas::e3") !!
       val hasAOfE3 = e3.findOwnedElement("p")
       assertNotNull(hasAOfE3)
       val hasAsOfE3 = e3.findAllOwnedElements()
       assertNotNull(hasAsOfE3)
    }

    /**
     * GET /packages/{packageName}/elements/math/properties
     * Shall also deliver inherited properties, not just the own.
     */
    @Test
    fun findPropertiesTest2() = testSession("ScalarValues", loadKerML = false) {
        settings.catchExceptions = false
        loadSysMD("""
                package find {
                    class e { 
                        attribute p1: ScalarValues::Real(0..3); 
                    }
                    class e2 isA e { 
                        attribute p2: ScalarValues::Real(1); 
                    }
                }
            """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val e2 = global.resolve<Type>("find::e2")!!
        val e = global.resolve<Type>("find::e")
        assertNotNull(e)
        val p1 = global.resolve<Feature>("find::e2::p1")
        val p2 = global.resolve<Feature>("find::e2::p2")
        assertEquals("p1", p1!!.declaredName)
        assertEquals("p2", p2!!.declaredName)
        val pE2 = getAllInheritedFeatures(e2)
        assertEquals(2, pE2.size)
    }

    /**
     * We allow definition in arbitrary order.
     * The parser shall leave the reference open and refer to any.
     * After parsing, the initialization shall replace the references to 'anything' with the correct ones.
     */
    @Test fun useClassBeforeDefinition() = testSession(loadKerML = false) {
        loadSysMD(
            """
            class a :> b;
            class b :> c;
            class c :> Base::Anything;
        """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Type>("a")!!
        val b = global.resolve<Type>("b")!!
        val c = global.resolve<Type>("c")!!
        assertTrue(b in a.allSupertypes())
        assertTrue(c in b.allSupertypes())
    }


    /**
     * We allow definition in arbitrary order.
     * The parser shall leave the reference open and refer to any.
     * After parsing, the initialization shall replace the any-references with the correct ones.
     */
    @Test fun useClassBeforeDefinition2() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            class c :> Base::Anything {
                feature f: Base::Anything; 
            }
            class b :> c;
            class a :> b;
        """.trimIndent()
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val a = global.resolve<Type>("a")!!
        val b = global.resolve<Type>("b")!!
        val c = global.resolve<Type>("c")!!
        assertTrue(b in a.allSupertypes())
        assertTrue(c in b.allSupertypes())
        assertTrue(a.ownedElement.find { it.ref!! is Feature }!!.ref!!.declaredName == "f" )
    }


    /**
     * We allow definition in arbitrary order.
     * This test checks that features are cloned properly if definitions are in reverse order.
     * Note that the test might fail randomly depending on implementation.
     */
    @Test fun useClassBeforeDefinition3() = testSession(loadKerML = false) {
        loadSysMD("""
            class a :> b;
            class b :> c;
            class c :> Base::Anything {
                feature f: Base::Anything; 
            }
        """.trimIndent()
        )
        val a = global.resolve<Type>("a")!!
        val b = global.resolve<Type>("b")!!
        val c = global.resolve<Type>("c")!!
        assertTrue(b in a.allSupertypes())
        assertTrue(c in b.allSupertypes())
        assertTrue(a.ownedElement.find { it.ref!! is Feature }!!.ref!!.declaredName == "f" )
    }


    @Test
    fun cloneInheritedPart() = testSession(loadKerML = false) {
        loadSysMD(input = """
            class x {
                feature y: Base::Anything; 
            }
            class z specializes x;
        """)
        val zy = global.resolve<Feature>("z::y")
        val xy = global.resolve<Feature>("x::y")
        assertTrue(zy !== xy)
        assertTrue(zy!!.multiplicityProperty !== xy!!.multiplicityProperty)
        assertTrue(zy!!.multiplicity !== xy!!.multiplicity)
        assertTrue(zy!!.ownedSpecialization !== xy!!.ownedSpecialization)
        assertTrue(zy!!.isTransient)
    }


    /**
     * Inherited values are cloned.
     * This permits writing to subclass without changing superclass.
     */
    @Test
    fun cloneInheritedValue() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Real :> ScalarValue; datatype Integer :> ScalarValue; }
            class x { attribute y: ScalarValues::Real(2.0); }
            class z specializes x; 
            """)
        val zy = global.resolve<Feature>("z::y")
        val xy = global.resolve<Feature>("x::y")
        assertTrue(zy!!.variable !== xy!!.variable, "Inherited values are independent variables")
        // assertTrue(zy!!.multiplicity !== xy!!.multiplicity)
        // assertTrue(zy!!.expression !== xy!!.expression)
        assertTrue(zy!!.ownedSpecialization !== xy!!.ownedSpecialization)
        assertTrue(zy!!.isTransient, "Implicit elements shall be marked transient.")
    }


    @Test
    fun inheritedFeatures() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            class A;
            A hasA feature a.
            A::a hasA feature x [1..10]. 
            
            class B isA A. 
            B::a hasA feature x [2..8]. 
            
            class C isA A.
            C::a hasA feature x [3..4].
        """.trimIndent())
        initialize()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val aax = global.resolve<Feature>("A::a::x")
        val bax = global.resolve<Feature>("B::a::x")
        val cax = global.resolve<Feature>("C::a::x")
        assertNotEquals(aax, bax)
        assertNotEquals(aax, cax)
        assertNotEquals(bax, cax)
        assertEquals(IntegerRange(2,8), bax?.multiplicity)
        assertEquals(IntegerRange(3,4), cax?.multiplicity)
    }

    @Test
    fun inheritedFeatures2() = testSession(loadKerML = false) {
        loadSysMD("""
            package ScalarValues { datatype ScalarValue; datatype Integer :> ScalarValue; }
            class A {
                feature a { feature x [1..10]; }
            }
            
            class B :> A { 
                feature a { feature x [2..8]; }
            }
            
            class C :> A {
                feature a { feature x [3..4]; }
            }
        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val aax = global.resolve<Feature>("A::a::x")
        val bax = global.resolve<Feature>("B::a::x")
        val cax = global.resolve<Feature>("C::a::x")
        assertNotEquals(aax, bax)
        assertNotEquals(aax, cax)
        assertNotEquals(bax, cax)
        assertEquals(IntegerRange(2,8), bax?.multiplicity)
        assertEquals(IntegerRange(3,4), cax?.multiplicity)
    }

    // Fixed with 2.0.30.
    @Test fun overrideDerivedPropertyTest() = testSession("SI") {
        loadSysMD(
            input = """           
            class Coin {
                feature diameter: SI::Length(5..200) [mm];
                feature circumference: SI::Length [mm] = diameter*3.141; // 15.7 .. 628.2 mm 
            }

            feature oneEuroCoin : Coin { 
                feature diameter: SI::Length [mm] = 23.25 mm; 
                // circumference is inherited. Must be re-evaluated with correct diameter.
                // Expected behavior:  re-evaluate dependency in new scope, but without changing diameter of Coin. 
            }
            """
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val oneEuroCircumference = global.resolveVar("oneEuroCoin::circumference")!!
        val oneEuroDiameter = global.resolveVar("oneEuroCoin::diameter")!!
        val coinCircumference = global.resolveVar("Coin::circumference")!!
        val coinDiameter = global.resolveVar("Coin::diameter")!!
        assertEquals(73.03, oneEuroCircumference.vectorQuantity.getMinAsDouble()*1000.0, 0.01)
        assertEquals(23.25, oneEuroDiameter.vectorQuantity.getMinAsDouble()*1000.0, 0.01)
        assertEquals(15.7, coinCircumference.vectorQuantity.getMinAsDouble()*1000.0, 0.01)
        assertEquals(5.0, coinDiameter.vectorQuantity.getMinAsDouble()*1000.0, 0.001)
    }


    /**
     * Each overridden value and expression has independent variables;
     * subclass inherits expression, but does not overwrite the value of superclass.
     */
    @Test fun overrideDerivedPropertyTest2() = testSession {
        loadSysMD(input = """
            class Coin; 
            class OneEuroCoin isA Coin.
           
            Coin hasA 
                feature diameter: ScalarValues::Real(2..100);
                feature circumference: ScalarValues::Real = diameter.

            OneEuroCoin hasA 
                feature diameter: ScalarValues::Real(23..23);
                feature circumference: ScalarValues::Real = diameter * 2.0.
                // circumference is inherited. Must be re-evaluated with correct diameter.
                // Expected:  re-evaluate dependency in new scope, but without changing diameter of Coin. 
            """
        )
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        propagate()
        val oneEuroCircumference = global.resolveVar("OneEuroCoin::circumference")!!
        val oneEuroDiameter = global.resolveVar("OneEuroCoin::diameter")!!
        val coinCircumference = global.resolveVar("Coin::circumference")!!
        val coinDiameter = global.resolveVar("Coin::diameter")!!
        assertEquals(46.0, oneEuroCircumference.vectorQuantity.getMinAsDouble())
        assertEquals(23.0, oneEuroDiameter.vectorQuantity.getMinAsDouble())
        assertEquals(2.0, coinCircumference.vectorQuantity.getMinAsDouble())
        assertEquals(2.0, coinDiameter.vectorQuantity.getMinAsDouble())
    }

    /**
     * Subclasses can have constrained variables that do not affect the superclass.
     */
    @Test fun inheritanceConstraintTestFromSubclasses() = testSession{
        loadSysMD("""
                class a {
                    feature p: ScalarValues::Real; 
                }
                class b specializes a { 
                    feature p: ScalarValues::Real(2); 
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
    @Test fun inheritanceConstraintTestFromSuperclass() = testSession {
        loadSysMD("""
                class a { feature p: ScalarValues::Real(2); }
                class b :> a { feature p: ScalarValues::Real; }
            """.trimIndent())
        // b shall be constrained to 2, a remains 2.
        assertEquals(Range(2.0..2.0), global.resolve<Feature>("a::p")?.variable?.vectorQuantity?.value?.asAadd()?.getRange())
        assertEquals(Range(2.0..2.0), global.resolve<Feature>("a::p")?.variable?.vectorQuantity?.value?.asAadd()?.getRange())
    }

    /**
     * Subclasses must have subset of superclass-values.
     */
    @Test fun inheritanceConstraintTestContradiction() = testSession {
        loadSysMD("""
                class a {
                    feature p: ScalarValues::Real(1); 
                }
                class b specializes a {
                    feature p: ScalarValues::Real(2);
                }
            """.trimIndent())
        propagate()
        assertEquals(1.0, global.resolve<Feature>("a::p")!!.variable!!.min())
        assertEquals(1.0, global.resolve<Feature>("a::p")!!.variable!!.max())
        assertEquals(2.0, global.resolve<Feature>("b::p")!!.variable!!.min())
        assertEquals(2.0, global.resolve<Feature>("b::p")!!.variable!!.max())
        assertTrue(status.exceptions.first() is SysMDInconsistency)
    }

    /**
     * Indirect constraints must be considered as 'writing' the addressed object directly
     * in an inheritance tree, not the superclass definitions.
     */
    @Test fun inheritanceConstraintTestIndirect() = testSession(catchExceptions = false){
        loadSysMD("""
                class a { feature p: ScalarValues::Real; }
                class b :> a; 
                class x { feature a: ScalarValues::Real(2) = b::p; }
            """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        // 'a' is 2 and propagated to b::p (that is then 2)
        // 'b::p' is NOT propagated to the superclass 'a'.
        // Hence:
        // - b::p = 2.0.
        // - a::p = Reals.
        val xa = global.resolve<Feature>("x::a")!!.variable
        val bp = global.resolve<Feature>("b::p")!!.variable
        val ap = global.resolve<Feature>("a::p")!!.variable
        assertEquals(Range(2.0, 2.0), xa?.vectorQuantity?.value?.asAadd()?.getRange()) // directly via eval-up
        assertEquals(Range(2.0, 2.0), bp?.vectorQuantity?.value?.asAadd()?.getRange()) // must go here
        assertEquals(builder.Reals.getRange(), ap?.vectorQuantity?.value?.asAadd()?.getRange())   // and not here
    }


    @Test fun realConstraintTest() = testSession {
        loadSysMD(("""
            package Test { 
                class m { feature v: ScalarValues::Real(1..5); }
                class n :> m {feature v: ScalarValues::Real(6..6); }
            }
        """.trimIndent()))
        // The inconsistency / violation of Liskov Principle must be reported.
        assertEquals(1, status.exceptions.filterIsInstance<SysMDInconsistency>().size)
    }

    @Test fun realConstraintTest2() = testSession {
        loadSysMD(("""
            package Test { 
                class m { feature v: ScalarValues::Real(1..5); }
                class n :> m {feature v: ScalarValues::Real(1..5); }
            }
        """.trimIndent()))
        // The inconsistency / violation of Liskov Principle must be reported.
        propagate()
        assertEquals(0, status.exceptions.size)
    }


    @Test fun realConstraintTest3() = testSession {
        loadSysMD(("""
            package Test {
                class super { feature v: ScalarValues::Real(1..2); }
                class sub specializes super { feature v: ScalarValues::Real(1.5) = 1.5; }
            }
        """.trimIndent()))
        // The inconsistency / violation of Liskov Principle must be reported.
        propagate()
        assertEquals(0, status.exceptions.size)
        assertEquals(Range(1.5, 1.5), global.resolve<Feature>("Test::sub::v")?.variable?.vectorQuantity?.value?.asAadd()?.getRange())
    }


    @Test fun integerConstraintTest() = testSession {
        loadSysMD(("""
            package Test { 
                class m {feature v: ScalarValues::Integer (1..5); }
                class n :> m {feature v: ScalarValues::Integer(6..6); }; 
            }
        """.trimIndent()))
        // The inconsistency / violation of Liskov Principle must be reported.
        propagate()
        assertEquals(1, status.exceptions.filterIsInstance<SysMDInconsistency>().size)
        val v = global.resolve<Feature>("Test::n::v")?.variable?.vectorQuantity
        assertEquals(IntegerRange(6, 6), v?.value?.asIdd()?.getRange())
    }

    @Test fun integerConstraintTest2() = testSession("ScalarValues", catchExceptions = false) {
        loadSysMD(("""
            package Test { class m; class n isA m; }
            Test::m hasA feature v: ScalarValues::Integer (1..5).
            Test::n hasA feature v: ScalarValues::Integer(2..2).
        """.trimIndent()))
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(IntegerRange(2, 2), global.resolve<Feature>("Test::n::v")?.variable?.vectorQuantity?.value?.asIdd()?.getRange())
    }


    /**
     *  features are inherited.
     */
    @Test fun hasAFeatureTest2() = testSession {
        settings.catchExceptions = false
        loadSysMD("""
            class a; 
            class b isA a {
                feature xx: x; 
            } 
            """.trimIndent())
        initialize()
        val b = global.resolve<Type>("b")
        assertTrue(b is Type)
        assertNotNull(b.findOwnedElement("xx"))
    }


    /**
     * Features are inherited including Multiplicity.
     */
    @Test
    fun issue_featureInheritanceMultiplicity() = testSession {
        loadSysMD("""
            class Car {
                feature wheels: Base::Anything[1..4] ; 
            }
            class VW specializes Car.
            """.trimIndent()
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val vwWheels = global.resolve<Feature>("VW::wheels")
        assertNotNull(vwWheels)
        assertEquals(IntegerRange(1.0, 4.0), vwWheels.multiplicity)
    }

    /**
     * Features are inherited including Multiplicity.
     */
    @Test
    fun issue_featureInheritanceMultiplicity2() = testSession {
        loadSysMD("""
            class Car { part wheels: [1..4] Base::Anything; }
            class Audi :> Car; 
            Audi hasA part wheels: [2 .. 5] Base::Anything.
            """.trimIndent())
        propagate()
        assertTrue(status.exceptions.first() is SysMDInconsistency) // Inconsistency !!!
        val audiWheels = global.resolve<Feature>("Audi::wheels")
        assertEquals(IntegerRange(2, 5), audiWheels?.multiplicity)
    }


    /**
     * Multiplicity with subclasses is correctly transferred to multiplicity property.
     */
    @Test
    fun issue_featureInheritanceMultiplicity3() = testSession {
        loadSysMD("""
            class Car;
            Car hasA feature wheels: Base::Anything [1..4].
            class VW isA Car.
            VW hasA feature wheels: Base::Anything [2 .. 3].
            class Audi isA Car. 
            Audi hasA feature wheels: Base::Anything [2 .. 4].
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val audiWheels = global.resolve<Feature>("Audi::wheels")
        val vwWheels = global.resolve<Feature>("VW::wheels")
        val carWheels = global.resolve<Feature>("Car::wheels")

        assertEquals(IntegerRange(2, 4), audiWheels?.multiplicity)
        assertEquals(IntegerRange(2, 3), vwWheels?.multiplicity)
        assertEquals(IntegerRange(1, 4), carWheels?.multiplicity)
    }

    @Test
    fun subclassWithoutConstraintsInheritsConstraints() = testSession {
        loadSysMD("""
            class general {
                attribute x: ScalarValues::Real(1 .. 2) = 1.5; 
            }
            class special :> general {
                attribute x: ScalarValues::Real; 
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }


    @Test
    fun subclassWithoutConstraintsInheritsConstraints2() = testSession("SI") {
        loadSysMD("""
            class general {
                attribute y: ScalarValues::Real; 
                attribute x: SI::Time(0 .. *) [ms] = 1.0 ms; 
            }
            class special :> general; 
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}
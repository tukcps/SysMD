
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.getAllOfClass
import com.github.tukcps.sysmd.services.session.loadLibrary
import io.github.tukcps.aadd.functions.numInternalNodes
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.XBool
import io.github.tukcps.aadd.values.XBool.Companion.True
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import util.findDifferenceById
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class IssuesAndRegressions {
    @Test
    @Disabled // IDD do not use solver so far, and finding the solution requires the LP solver
    fun minTestMultipleParams3Integer() = testSession("ScalarValues") {
        loadKerML("""
            feature a: ScalarValues::Integer(0..7);
            feature b: ScalarValues::Integer(1..6);
            feature c: ScalarValues::Integer(2..5);
            feature d: ScalarValues::Integer(3..4);
            feature e: ScalarValues::Integer(4..5) = min(a,b,c,d);
        """)
        propagate()
        Assertions.assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        Assertions.assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        Assertions.assertEquals(7, result.vectorQuantity.value.asIdd().max)
        val result1 = global.resolveVar("b")
        Assertions.assertEquals(4, result1!!.vectorQuantity.value.asIdd().min)
        Assertions.assertEquals(6, result1.vectorQuantity.value.asIdd().max)
        val result2 = global.resolveVar("c")
        Assertions.assertEquals(4, result2!!.vectorQuantity.value.asIdd().min)
        Assertions.assertEquals(5, result2.vectorQuantity.value.asIdd().max)
        val result3 = global.resolveVar("d")
        Assertions.assertEquals(4, result3!!.vectorQuantity.value.asIdd().min)
        Assertions.assertEquals(4, result3.vectorQuantity.value.asIdd().max)
        Assertions.assertEquals(0, status.issues.size, status.issues.toString())
    }

    /** Issue #247 in Gitlab */
    @Test
    fun issue247() = testSession("ScalarValues") {
        loadKerML("""
            type Vehicle :> Base::Anything {
               feature car : Vehicle [1..1];
            }
        """)
        assertTrue(status.issues.isNotEmpty(), "A feature may not be typed by a class that is its owner.")
    }

    /** Issue #247 v2 in Gitlab: This is OK. */
    @Test
    fun issue247v2() = testSession {
        loadKerML("""
                type Vehicle :> Base::Anything {
                   type Car :> Vehicle;
                }
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }


    /** Issue #34 in Gitlab. */
    @Test
    fun issue34test() = testSession("Occurrences") {
        loadKerML(input = """
            private import ScalarValues;
            class Wheel { feature price : Real (1.0..1.0); }
            class Body { feature price : Real (1.0..1.0); }
            class Chassis { feature price : Real (1.0..1.0); }
            class Car {
                feature wheel: Wheel;
                feature body: Body;
                feature chassis: Chassis;
                feature carPrice: Real = sumOverParts(price);
            }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val price = global.resolveVar("Car::carPrice")!!
        assertEquals(3.0, price.min(), 0.000001)
        assertEquals(3.0, price.max(), 0.000001)
    }

    /**
     * After repeated load of a document, its elements should be updated,
     * but not duplicated.
     */
    @Test fun elementsNotAppearTwice() = testSession("Base") {
        loadLibrary("ScalarValues")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val elements = get()
        loadLibrary("ScalarValues")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val elements2 = get()
        assertEquals(elements.size, elements2.size)
    }


    /** ... more complex integration test to be sure. Often complex */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice() = testSession {
        loadLibrary("Base")
        loadLibrary("ScalarValues")
        loadLibrary("Links")
        loadLibrary("Occurrences")
        loadLibrary("KerML")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val first = mutableListOf<Element>().also { it.addAll(get()) }

        loadLibrary("Base")
        loadLibrary("ScalarValues")
        loadLibrary("Links")
        loadLibrary("Occurrences")
        loadLibrary("KerML")
        initialize()
        val second = get()
        assertEquals(0, status.issues.size)

        val diff = findDifferenceById(first, second)
        assertTrue(diff.isEmpty())

        // reason possible: in particular, multiplicity that is generated
        // - must not be generated a second time.
    }

    /** Direct test on Multiplicity by simple example. */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice1() = testSession("ScalarValues") {
        loadKerML("package ScalarValues { datatype Integer; }; feature x; ", false)
        initialize()
        assertTrue(status.issues.isEmpty())
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val elem1 = getAllOfClass<Element>()
        loadKerML("feature x; ", false)
        initialize()
        assertEquals(0, status.issues.size)
        val multiplicities2 = getAllOfClass<Multiplicity>()
        val elem2 = getAllOfClass<Element>()
        assertEquals(multiplicities1.size, multiplicities2.size)
        assertEquals(elem1.size, elem2.size)
    }

    /** ... same for inherited properties */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice3() = testSession("Occurrences") {
        loadKerML("package ScalarValues { datatype Integer; }; class x { feature y;}")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val imports1 = getAllOfClass<Import>()
        val specs1 = getAllOfClass<Specialization>()
        val elem1 = getAllOfClass<Element>()
        loadKerML("class x { feature y; } ")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val multiplicities2 = getAllOfClass<Multiplicity>()
        val imports2 = getAllOfClass<Import>()
        val elem2 = getAllOfClass<Element>()
        val specs2 = getAllOfClass<Specialization>()
        assertEquals(multiplicities1.size, multiplicities2.size)
        assertEquals(specs1.size, specs2.size)
        assertEquals(imports1.size, imports2.size)
        assertEquals(elem1.size, elem2.size)
    }

    /**
     * Issue: bySubclasses looses Unit.
     * Fixed: 2.6.4; EvalDown called the superclass method.
     */
    @Test
    fun unitDisappearsTest() = testSession("Occurrences", "SI") {
        loadKerML("""
                    class Vehicle {
                        feature mass: SI::Mass = bySpecializations(mass);
                    }
                    class Car :> Vehicle {
                        feature mass: SI::Mass = 100.0 kg;
                    }
                    class Bicycle :> Vehicle {
                        feature mass: SI::Mass = oneOf(10.0 .. 20.0 [kg]);
                    }
            """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val vehicle = global.resolve<Namespace>("Vehicle") !!
        val mass =vehicle.resolveVar("mass")!!
        assertTrue(mass.vectorQuantity.getMinAsDouble() in 9.99..10.01)
        assertTrue(mass.vectorQuantity.getMaxAsDouble() in 99.99..100.01)
        assertEquals("kg", mass.vectorQuantity.unit.toString())
    }

    /**
     * Issue: multiplicity appears twice.
     * Fixed: v2.6.5; both create and add were called.
     */
    @Test
    fun multiplicityTwiceOrMissingTest() = testSession("ScalarValues") {
        loadKerML("""
                type Test :> Base::Anything {
                    feature comp: Base::Anything[1 .. 2];
                }
            """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val test = global.resolve<Type>("Test") !!
        val comp = test.resolve<Feature>("comp") !!
        assertEquals(1, comp.getOwnedElementsOfType<Multiplicity>().size) // Just the multiplicity
        assertEquals(IntegerRange(1, 2), comp.multiplicity)
    }

    /** Use of multiplicity as variable. */
    @Test
    fun automotiveExample() = testSession("Occurrences")  {
        loadKerML("""
            package ExampleDesign {
                class Axis;
                class Wheels;
                class Chassis {
                    feature  wheels: ExampleDesign::Wheels[2..6];
                    feature numAxis: ScalarValues::Integer = wheels::cardinality/2;
                }
            }""")
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val multi = global.resolve<Feature>("ExampleDesign::Chassis::wheels::cardinality")!!.variable
        assertNotNull(multi)
        assertEquals(2L, multi.min())
        assertEquals(6L, multi.max())
    }


    @Test
    fun variableUnknownIsReportedAsError() = testSession("ScalarValues") {
        loadKerML(input = "feature x: ScalarValues::Real = yyy;")
        assertTrue(status.issues.firstOrNull()?.cause is ElementNotFoundException, "There shall be error reporting yyy not defined.")
    }

    @Test
    fun typeUnknownIsReportedAsError() = testSession {
        loadKerML(input = " feature x: YYY;")
        assertTrue(status.issues.find { it.kind.ordinal >= Issue.Kind.WARN.ordinal }?.message?.contains("YYY") == true,
            "There shall be error reporting that YYY is not defined.")
    }

    @Test
    fun issue112_inheritedPartsOverrideMultiplicity() = testSession("ScalarValues") {
        loadKerML("""
            type Device :> Base::Anything {
                feature sensor [4..5];
            }
            type DeviceA :> Device {
                feature sensor [5..5];
            }
            type DeviceB :> Device {
                feature sensor [4..4];
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val s1 = global.resolve<Feature>("Device::sensor")
        val s2 = global.resolve<Feature>("DeviceB::sensor")
        val s3 = global.resolve<Feature>("DeviceA::sensor")
        assertNotEquals(s1, s2)
        assertNotEquals(s1, s3)
        assertNotEquals(s2, s3)
        assertEquals(IntegerRange(4,5), global.resolve<Feature>("Device::sensor")?.multiplicity)
        assertEquals(IntegerRange(4,4), global.resolve<Feature>("DeviceB::sensor")?.multiplicity)
        assertEquals(IntegerRange(5,5), global.resolve<Feature>("DeviceA::sensor")?.multiplicity)
    }


    @Test
    fun issue112_inheritPartsShort() = testSession("ScalarValues") {
        settings.catchExceptions = false
        loadKerML("""
                type a :> Base::Anything {
                    feature f1;
                }
                type b :> a;
            """)
        loadSysMD("""
                b::f1 hasA feature f: Base::Anything;
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val inherited = global.resolve<Element>( "b::f1")
        assertNotNull(inherited)
        val inheritedUpdated = global.resolve<Element>("b::f1::f")
        assertNotNull(inheritedUpdated)
    }

    /**
     * Check that features from a superclass are inherited to a subclass.
     */
    @Test
    fun issue112_inheritPartsShort2() = testSession("ScalarValues") {
        settings.catchExceptions = false
        loadKerML(
            """
                class a specializes Base::Anything {
                    feature f1: Base::Anything;
                }
                class b :> a;
            """)
        val bF1 = global.resolve<Feature>("b::f1")
        assertNotNull(bF1)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun issue118_cyclicClassification() = testSession {
        loadKerML("""
            class A :> B;
            class B :> C;
            class C :> A;
        """)
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    /**
     * Check for proper definition of a feature owner and report info if i.e., B is not defined.
     */
    @Test
    fun issue136NoErrorOnUndeclaredFeatureClass() = testSession {
        loadKerML("A hasA feature B: Base::Anything.")
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    @Test
    fun issue123_ThreeQualifiedNames() = testSession("ScalarValues") {
        loadSysMD("""
            Global hasA class A :>  Base::Anything.
            A hasA feature B:  Base::Anything.
            A::B hasA feature C:  Base::Anything.
            A::B::C hasA feature D:  Base::Anything.
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    /**
     * Issue: Update is not performed and not initialized and propagated properly.
     */
    @Test
    fun updateFeatureTest() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues;
            package X {
                feature x: Real(10.0);
                feature y: Real = x * 2.0;
            }
        """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        loadKerML("""
            private import ScalarValues;
            package X {
                feature x: Real(20.0);
                feature y: Real = x * 2.0;
            }
        """.trimIndent())
        propagate()
        val x = global.resolveVar("X::x")
        val y = global.resolveVar("X::y")
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(20.0, x?.vectorQuantity?.getMinAsDouble()!!, 0.0001)
        assertEquals(40.0, y?.vectorQuantity?.getMinAsDouble()!!, 0.0001)
    }


    /**
     * Issue: Update is not performed and not initialized and propagated properly.
     */
    @Test
    fun updateFeatureTest2() = testSession("ScalarValues") {
        loadKerML("""
            public import ScalarValues::*;
            package X {
                type Y :> Base::Anything;
                type Z :> Y {
                    feature x: Real = 10.0;
                }
            }
            """.trimIndent())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        loadSysMD("""
            X::Z hasA feature x: Real = 10.0.
            """.trimIndent())
        propagate()
        loadSysMD("""
           X::Z hasA feature x: Real = 10.0.
        """.trimIndent())
        propagate()
        //  val generated = getOwnedElement(textualRepresentation,"Generated elements") as Annotation?

        val x = global.resolveVar("X::Z::x")
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(10.0, x?.vectorQuantity?.getMinAsDouble()!!, 0.0001)
    }

    @Test
    fun propagateStringTest() = testSession("ScalarValues") {

        val value = FeatureImplementation(
            declaredName = "testPropertyString"
        ).also {
            it.expression = "The String is saved here"
        }

        val created = create(value, global)
        create(SpecializationImplementation(created, "ScalarValues::String"), created)
        initialize()
        propagate()
        val str = global.resolveVar("testPropertyString")
        assertNotNull(str)
    }


    @Test
    fun simpleMultiplicationWithZero() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues;
            feature a: Real(1..1);
            feature b: Real(0..5);
            feature c: Real(2..2) = a*b;
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }


    @Test
    fun maxTest1EvalDown() = testSession("ScalarValues") {
        loadKerML(input = """
            feature a: ScalarValues::Real(1..1);
            feature b: ScalarValues::Real(0..5);
            feature c: ScalarValues::Real(2..2) = max(a,b);
            """.trimIndent(), catchExceptions = true)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("b")
        assertEquals(2.0,result!!.vectorQuantity.getMinAsDouble(),0.000001)
        assertEquals(2.0,result.vectorQuantity.getMaxAsDouble(),0.000001)
        assertEquals(0, status.issues.size, status.issues.toString())
    }


    /**
     * There was a problem with repeated analysis of the same model.
     * We test it here by an example.
     */
    @Test
    fun repeatedExecutionCausesWrongResultsTest() = testSession("SI") {
        val model = """
                        type PartWithVolume :> Base::Anything {
                            feature height:  SI::Length = oneOf(10.0 .. 100.0 [cm]);
                            feature width:   SI::Length = oneOf(1.0 .. 1.1  [m]);
                            feature length:  SI::Length = oneOf(1.0 .. 1.1  [m]);
                            feature volume:  SI::Volume(1 .. 2)  = height * width * length;
                        }
                    """
        loadKerML(model)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        var volume = global.resolve<Feature>("PartWithVolume::volume")!!.variable
        val height = global.resolve<Feature>("PartWithVolume::height")!!.variable
        assertNotNull(height)
        assertEquals(1.0, volume!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.21, volume.vectorQuantity.getMaxAsDouble(), 0.001)
        propagate()
        initialize()
        initialize()
        initialize()
        initialize()
        initialize()
        initialize()
        initialize()
        initialize()
        volume = global.resolve<Feature>("PartWithVolume::volume")!!.variable
        assertEquals(1.0, volume!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.21, volume.vectorQuantity.getMaxAsDouble(), 0.001)
        loadKerML(model)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(1.0, volume.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.21, volume.vectorQuantity.getMaxAsDouble(), 0.001)
    }

    @Test
    fun nameOfDefinitionTest() = testSession {
        loadKerML("""
            package a {
                type b :> Base::Anything {
                   type c :> Base::Anything;
                }
            }
        """.trimIndent())
        assertEquals(0, status.issues.size, status.issues.toString())
        val a = global.resolve<Package>("a")
        val ab = global.resolve<Type>("a::b")
        val abc = global.resolve<Type>("a::b::c")
        assertNotNull(a)
        assertNotNull(ab)
        assertNotNull(abc)
        val ab2 = a.resolve<Type>("b")
        val abc2 = ab.resolve<Type>("c")
        assertNotNull(ab2)
        assertNotNull(abc2)
    }

    @Test
    fun extendPropertyRange() = testSession("ScalarValues") {
        loadKerML("""
                type Test :> Base::Anything {
                    feature property: ScalarValues::Real(0 .. *);
                }

                type Car :> Test {
                    feature property: ScalarValues::Real = 4.0; // property: Real(0 .. *) = 4.0. works
                }
            """)
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolve<Feature>("Car::property")!!.variable
        assertEquals(4.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
    }


    /**
     * Issue 134: isSubClassOf runs into infinite loop instead of reporting error.
     */
    @Test fun issue134test() = testSession {
        loadKerML("""
            class Test specializes Test;
            class P specializes Test;
        """.trimIndent())
        propagate()
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    /**
     * It seems that SysMLv2 allows features to be typed by features.
     * Feels wrong, but ... SysMLv2 allows features to be typed by types, and
     * features are types.
     */
    @Test fun issue165typedFeatureInheritance() = testSession("ScalarValues") {
        loadKerML("""
            feature a : ScalarValues::Integer(0 .. 5);
            feature x: a; // Here, a Type is needed. And a feature is a type ...
        """.trimIndent())
        propagate()
        assertTrue(status.issues.isEmpty(), "Features should be OK for typing features?")
    }

    //Did not yet create new issue. Issue #184 is the nearest thematically.
    @Test @Disabled // Covered by the following test in easier way
    fun issue184() = testSession("ScalarValues") {
        loadKerML("""
            feature weight: ScalarValues::Integer = oneOf(0..50);
            feature weightBoundary1: ScalarValues::Integer = oneOf(15);
            feature weightBoundary2: ScalarValues::Integer = oneOf(15);
            feature r: ScalarValues::Boolean(true) = weight <= (weightBoundary1 + weightBoundary2);
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val wb1 = global.resolveVar("weightBoundary1")!!
        val wb2 = global.resolveVar("weightBoundary2")!!

        assertEquals(15, wb1.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(15, wb1.vectorQuantity.value.asIdd().getRange().max)
        assertEquals(15, wb2.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(15, wb2.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test fun issue189nameResolutionIncorrect() = testSession {
        loadKerML("""
            namespace p { namespace p; }
         """)
        // Failure: 'Specialization' object is created in package p, not in element p.
        val pp = global.resolve<Element>("p::p") // there is no p in p.
        propagate()
        assertEquals("p", pp!!.escapedName())
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    @Test fun issue189nameResolutionIncorrect3() = testSession {
        loadKerML("""class p :> Base::Anything;""")
        val pp = global.resolve<Element>("p::p") // there is no p in p.
        assertEquals(null, pp)
    }


    @Test fun issue190InvalidTypeNotReported() = testSession {
        loadKerML("""
            package x;
            type a :> x;
        """)
        assertEquals(1, status.issues.size, status.issues.toString())
        assertTrue(status.issues.firstOrNull()?.message?.contains("type") == true)
    }

    /**
     * Evolution of the issue ... we continuously check that the Modeling example works.
     */
    @Test
    fun issue190test() = testSession("ISO26262", "SI")  {
        loadSysMLv2("""
            private import ScalarValues::*;
            private import ISO26262::*;
            package archExample {
                part def Vehicle :> Component {
                    part engine : Engine[1..2];
                    attribute power: SI::Power in [kW] = engine::power * ToReal(engine::cardinality);
                }
                // Drive is a Function that has NO subclasses; its implementation alternatives are hence not
                // "by Subclasses, but "by Implements relationship".
                part def Drive :> Function;
                // Engine implements Drive.
                part def Engine :> Component {
                    attribute power: SI::Power in [kW] = [10.0 .. 100.0] kW; // bySpecializations(power);
                }
                part def ElectricDrive :> Engine {
                    attribute power: SI::Power(1..50) in [kW];
                }
                part def CombustionEngine :> Engine {
                    attribute power: SI::Power(10..200) in [kW];
                }

                part vehicle: Vehicle;
                part drive: Drive {
                    attribute power: SI::Power in [kW] = byImplements(power);
                }
                connection r: ISO26262::implements connect vehicle::engine to drive;
                assert enoughPower { drive::power > 0.0 kW }
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val drive = global.resolve<Feature>("archExample::drive")
        assertNotNull(drive)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        // val engine = global.resolveName<Type>("archExample::Engine")
        // val power = global.resolveName<Expression>("archExample::drive::power")
        val enoughPower = global.resolveVar("archExample::enoughPower")
        assertEquals(True, enoughPower?.vectorQuantity?.value as XBool)
    }

    /**
     * Issue 194 -- If a calculation result is NaN, it must be taken out of the set of variables for
     * which change is expected by evalDown.
     */
    @Test
    fun issue194NoConvergenceIfNaNisResult() = testSession("ScalarValues")  {
        loadSysMLv2("""
            attribute result3: ScalarValues::Real = power2(10000000000.0);
        """)
        propagate()
        // println(status.numberOfPropagateIterations)
        assertTrue( status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun issue219InheritedNotShown() = testSession("ScalarValues") {
        loadKerML("""
                type Test :> Base::Anything {
                    feature a: ScalarValues::Real(0..1);
                }
                type Test2 :> Test;
            """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }

    @Test
    fun issue222SumOverParts() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues;
            type p :> Base::Anything {
                feature a {
                    feature s: Real = 1.0;
                }
                feature b {
                    feature s: Real = 2.0;
                }
                feature s1: Real = sumOverParts(s);
        }""")
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(3.0, global.resolveVar("p::s1")!!.min(), 0.0001)
    }

    //Tests for Issue #243
    @Test @Disabled
    fun issue243indexExplosionBiggerModelTest() = testSession("ScalarValues") {
        loadKerML(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            attribute r1: ScalarValues::Boolean = x1 <= y1;

            attribute x2: ScalarValues::Real = 1.0;
            attribute y2: ScalarValues::Real = 3.0;
            attribute r2: ScalarValues::Boolean = x2 <= y2;

            attribute x3: ScalarValues::Real = 1.0;
            attribute y3: ScalarValues::Real(1.0..3.0);
            attribute r3: ScalarValues::Boolean = x3 <= y3;

            attribute x4: ScalarValues::Real = 1.0;
            attribute y4: ScalarValues::Real = [1.0..3.0];
            attribute r4: ScalarValues::Boolean = x4 <= y4;
        """)
        assertEquals(0, status.issues.size, status.issues.toString())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        global.resolveVar("x1")!!
        global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!
        global.resolveVar("x2")!!
        global.resolveVar("y2")!!
        val r2 = global.resolveVar("r2")!!
        global.resolveVar("x3")!!
        global.resolveVar("y3")!!
        val r3 = global.resolveVar("r3")!!
        global.resolveVar("x4")!!
        global.resolveVar("y4")!!
        val r4 = global.resolveVar("r4")!!


        println(r1.vectorQuantity.bdd().toIteString())
        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")

        println("r2 depth: ${r2.vectorQuantity.bdd().height()}")
        println("r2 #nodes: ${r2.vectorQuantity.bdd().numInternalNodes()}")
        println("r2 bdd: ${r2.vectorQuantity.bdd().toIteString()}")

        println("r3 depth: ${r3.vectorQuantity.bdd().height()}")
        println("r3 #nodes: ${r3.vectorQuantity.bdd().numInternalNodes()}")
        println("r3 bdd: ${r3.vectorQuantity.bdd().toIteString()}")

        println("r4 depth: ${r4.vectorQuantity.bdd().height()}")
        println("r4 #nodes: ${r4.vectorQuantity.bdd().numInternalNodes()}")
        println("r4 bdd: ${r4.vectorQuantity.bdd().toIteString()}")
    }

    @Test @Disabled
    fun issue243indexExplosionSmallModelTest() = testSession("ScalarValues") {
        loadKerML(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            """
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        global.resolveVar("x1")!!
        global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!
        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")
    }

    @Test @Disabled
    fun issue243indexExplosionDuplicateComparisonTest() = testSession("ScalarValues") {
        loadKerML(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            attribute r2: ScalarValues::Boolean = x1 <= y1;
            //attribute r3: ScalarValues::Boolean = r1 and r2;
            """
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        global.resolveVar("x1")!!
        global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!
        val r2 = global.resolveVar("r2")!!
        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")

        println("r2 depth: ${r2.vectorQuantity.bdd().height()}")
        println("r2 #nodes: ${r2.vectorQuantity.bdd().numInternalNodes()}")
        println("r2 bdd: ${r2.vectorQuantity.bdd().toIteString()}")

        //println("r3: ${r3.vectorQuantity.bdd().toIteString()}")
    }

    @Test @Disabled
    fun issue243indexExplosionSmallModelDeltaTest() = testSession("ScalarValues") {
        loadKerML(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.5..2.5);
            //attribute y1: ScalarValues::Real = 3.0;
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            attribute r2: ScalarValues::Boolean = x1 <= y1;
            attribute r3: ScalarValues::Boolean = x1 <= y1;
            attribute r4: ScalarValues::Boolean = x1 <= y1;
            """
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        global.resolveVar("x1")!!
        global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!
        val r2 = global.resolveVar("r2")!!
        val r3 = global.resolveVar("r3")!!
        val r4 = global.resolveVar("r4")!!



        //println(r1.vectorQuantity.bdd().toIteString())
        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")

        println("r2 depth: ${r2.vectorQuantity.bdd().height()}")
        println("r2 #nodes: ${r2.vectorQuantity.bdd().numInternalNodes()}")
        //println("r2 bdd: ${r2.vectorQuantity.bdd().toIteString()}")

        println("r3 depth: ${r3.vectorQuantity.bdd().height()}")
        println("r3 #nodes: ${r3.vectorQuantity.bdd().numInternalNodes()}")
        //println("r3 bdd: ${r3.vectorQuantity.bdd().toIteString()}")

        println("r4 depth: ${r4.vectorQuantity.bdd().height()}")
        println("r4 #nodes: ${r4.vectorQuantity.bdd().numInternalNodes()}")
        //println("r4 bdd: ${r4.vectorQuantity.bdd().toIteString()}")
    }

    @Test @Disabled
    fun issue243indexExplosionSmallModelAADDTest() = testSession {
        loadKerML(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            //attribute z1: ScalarValues::Real(2.0..6.0) = x1 + y1;
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            //attribute r2: ScalarValues::Boolean = x1 <= y1;
            //attribute rn: ScalarValues::Boolean = z1 > x1;
            """
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val x1 = global.resolveVar("x1")!!
        val y1 = global.resolveVar("y1")!!
        //val r1 = global.resolveName<Expression>("r1")!!



        //println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        //println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        //println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")
        println("x1 depth: ${x1.vectorQuantity.aadd().height()}")
        println("x1 #nodes: ${x1.vectorQuantity.aadd().numInternalNodes()}")
        println("x1 depth: ${y1.vectorQuantity.aadd().height()}")
        println("x1 #nodes: ${y1.vectorQuantity.aadd().numInternalNodes()}")

        println("Builder status:")
        println("Conditions:")
        builder.conds.x.forEach { println("Index: ${it.key}, attribute: ${it.value}") }
    }

    @Test @Disabled
    fun issue243indexExplosionSmallModelUnrelatedVarsAADDTest() = testSession("ScalarValues") {
        loadKerML(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            attribute x2: ScalarValues::Real(5.0..10.0);
            attribute y2: ScalarValues::Real(5.0..10.0);
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            attribute r2: ScalarValues::Boolean = x1 <= y1;
            attribute rn: ScalarValues::Boolean = x2 <= y2;
            """
        )
        assertEquals(0, status.issues.size, status.issues.toString())
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val x1 = global.resolveVar("x1")!!
        val y1 = global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!
        val rn = global.resolveVar("rn")!!


        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("rn depth: ${rn.vectorQuantity.bdd().height()}")
        println("rn #nodes: ${rn.vectorQuantity.bdd().numInternalNodes()}")
        //println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")
        println("x1 depth: ${x1.vectorQuantity.aadd().height()}")
        println("x1 #nodes: ${x1.vectorQuantity.aadd().numInternalNodes()}")
        println("x1 depth: ${y1.vectorQuantity.aadd().height()}")
        println("x1 #nodes: ${y1.vectorQuantity.aadd().numInternalNodes()}")

    }

    @Test
    fun rangesNotConstrained() = testSession("ScalarValues") {
        loadKerML("""
                private import SI::*;
                feature x:  ScalarValues::Real;
                inv a1 { x >= 95.0 }
                inv a2 { x <= 96.0 }
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        global.resolveVar("x")
    }

    @Test
    fun exponentiationIsRightAssociative() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = 2.0 ^ 2.0 ^ 3.0;
        """.trimIndent())
        val x = global.resolve<Feature>("x")
        assertEquals(256.0, x!!.variable!!.min(), 0.0001)
    }

    @Test @Disabled //TODO: Problem in Parser: After or only Product possible, but EE is not in Product
    fun booleanExpression() = testSession {
        loadKerML(
            input = """
            attribute c: ScalarValues::Integer = 1;
            attribute b: ScalarValues::Integer = 2;
            attribute a: ScalarValues::Boolean = c == 0 or b == 0.
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    /**
     * Goes into infinite loop --> BUG in IDD * IDD !
     */
    @Test @Disabled
    fun iddTimesLoopIssue267() = testSession("ScalarValues") {
        loadKerML(
            input = """
            package safety {
                function calcASIL{
                    in feature severity : ScalarValues::Integer(0..3);
                    in feature exposure : ScalarValues::Integer(0..4);
                    in feature controllability: ScalarValues::Integer(0..3);
                    feature sum: ScalarValues::Integer = severity + exposure + controllability;
                    feature sumAdapted : ScalarValues::Integer = if (severity == 0) or (controllability == 0) ? 0 else sum; //special case for S0 and C0 the ASIL is always QM (0)
                    return result: Integer = max(sum-6,0).
                }
                feature S: ScalarValues::Integer = 2;
                feature E: ScalarValues::Integer = 4;
                feature C: ScalarValues::Integer = 2;
                feature ASIL: ScalarValues::Integer = calcASIL(S,E,C).
            }
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
    }

    /**
     * Everything seems OK; no duplicated features.
     * BUT: Supertypes are duplicated.
     */
    @Test
    fun referenceDuplicatesTestIssue260() = testSession("ScalarValues", "Parts", "Ports", "Requirements") {
        settings.catchExceptions = true
        loadSysMLv2("""
            private import ScalarValues::*;
            private import SI::*;
            part testPart {
                attribute att: Real [m];
                in port input;
            }

            requirement testReq {
                subject testRef references testPart;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val ref = global.resolve<Feature>("testReq::testRef")
        val testPart = global.resolve<Feature>("testPart")
        val testReq = global.getOwnedElement("testReq")
        val testRef = testReq?.getOwnedElement("testRef") as Feature
        assertSame(testPart, testRef.referencedFeature?.ref as Feature)
        assertSame(testPart, ref)
        assertTrue(testRef.getOwnedElementsOfType<Feature>().none { it !is Multiplicity })
    }

    @Test
    fun test() = testSession("Parts") {
        loadSysMLv2("""
            package features {
                part def carFeature;
                part def accSystem :> carFeature {
                    part def avoidCollision :> carFeature;
                }
                // part def detectLongDistanceCollision :> avoidCollision;
                // The following works:
                part def detectLongDistanceCollision :> accSystem::avoidCollision;
            }
        """)
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }
}

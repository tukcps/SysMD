
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.exceptions.Issue
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.loadLibrary
import io.github.tukcps.aadd.functions.numInternalNodes
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.XBool
import io.github.tukcps.aadd.values.XBool.Companion.True
import org.junit.jupiter.api.Disabled
import util.assertNoIssues
import util.findDifferenceById
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class IssuesAndRegressions {
    @Test
    @Disabled // IDD do not use solver so far, and finding the solution requires the LP solver
    fun minTestMultipleParams3Integer() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "0..7";}
            feature b: Ranges::IntegerInRange {:>> range = "1..6";}
            feature c: Ranges::IntegerInRange {:>> range = "2..5";}
            feature d: Ranges::IntegerInRange {:>> range = "3..4";}
            feature e: Ranges::IntegerInRange = min(a,b,c,d) {:>> range = "4..5";}
        """)
        propagate()
        assertEquals(0, status.issues.size, status.issues.toString())
        val result = global.resolveVar("a")
        assertEquals(4, result!!.vectorQuantity.value.asIdd().min)
        assertEquals(7, result.vectorQuantity.value.asIdd().max)
        val result1 = global.resolveVar("b")
        assertEquals(4, result1!!.vectorQuantity.value.asIdd().min)
        assertEquals(6, result1.vectorQuantity.value.asIdd().max)
        val result2 = global.resolveVar("c")
        assertEquals(4, result2!!.vectorQuantity.value.asIdd().min)
        assertEquals(5, result2.vectorQuantity.value.asIdd().max)
        val result3 = global.resolveVar("d")
        assertEquals(4, result3!!.vectorQuantity.value.asIdd().min)
        assertEquals(4, result3.vectorQuantity.value.asIdd().max)
        assertEquals(0, status.issues.size, status.issues.toString())
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
    fun issue34test() = testSession("Occurrences", "Ranges") {
        loadKerML(input = """
            class Wheel { feature price : Ranges::RealInRange { :>> range = "1.0..1.0";} }
            class Body { feature price : Ranges::RealInRange { :>> range = "1.0..1.0";} }
            class Chassis { feature price : Ranges::RealInRange { :>> range = "1.0..1.0";}}
            class Car {
                feature wheel: Wheel;
                feature body: Body;
                feature chassis: Chassis;
                feature carPrice: Ranges::RealInRange = sumOverParts(price);
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


    /**
     * Loading libraries and models shall be executed twice, and no
     * additional elements shall be created, and IDs shall remain the same.
     * Check for standard libraries that have UUID5.
     */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice() = testSession {
        loadLibrary("Base")
        loadLibrary("ScalarValues")
        loadLibrary("Links")
        loadLibrary("Occurrences")
        initialize()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val first = mutableListOf<Element>().also { it.addAll(get()) }

        loadLibrary("Base")
        loadLibrary("ScalarValues")
        loadLibrary("Links")
        loadLibrary("Occurrences")
        initialize()
        val second = get()
        assertEquals(0, status.issues.size)

        val diff = findDifferenceById(first, second)
        assertTrue(diff.isEmpty())

        // reason possible: in particular, multiplicity that is generated
        // - must not be generated a second time.
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
        assertNoIssues()
    }

    /**
     * Issue 288:
     * RealInRange was not available, hence not range.
     * The reason was that import was resolved eventually AFTER RealInRange.
     * Fix: Repeat resolution until no progress in initialize.
     */
    @Test
    fun updateFeatureTest() = testSession( "Ranges") {
        loadKerML("""
            private import Ranges;
            package X {
                feature x: RealInRange { :>> range = "10.0..20.0";}
            }
        """)
        propagate()
        val x = global.resolveVar("X::x")
        assertEquals(0, status.issues.size, status.issues.toString())
        assertEquals(10.0, x?.vectorQuantity?.getMinAsDouble()!!, 0.0001)
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
            """)
        propagate()
        assertTrue( status.issues.isEmpty(), status.issues.toString())
        loadSysMD("""
            X::Z hasA feature x: Real = 10.0.
        """)
        propagate()
        loadSysMD("""
            X::Z hasA feature x: Real = 10.0.
        """)
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

        val created = addOwnedMember(value, global)
        addOwnedRelationship(SpecializationImplementation(created, UnresolvedType("ScalarValues::String")), created)
        initialize()
        propagate()
        val str = global.resolveVar("testPropertyString")
        assertNotNull(str)
    }


    @Test
    fun simpleMultiplicationWithZero() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a: Ranges::RealInRange {:>> range = "1..1";}
            feature b: Ranges::RealInRange {:>> range = "0..5";}
            feature c: Ranges::RealInRange = a*b {:>> range = "2..2";}
        """)
        propagate()
        assertEquals(0, status.issues.size, "Error messages: ${status.issues}")
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }


    @Test
    fun maxTest1EvalDown() = testSession("Ranges") {
        loadKerML(input = """
            feature a: Ranges::RealInRange {:>> range = "1..1";}
            feature b: Ranges::RealInRange {:>> range = "0..5";}
            feature c: Ranges::RealInRange = max(a,b) {:>> range = "2..2";}
            """)
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
    @Test //Multiple executions of initialize() and propagate() lead to not working detection of redefinition
    fun repeatedExecutionCausesWrongResultsTest() = testSession("SI") {
        val model = """
                        type PartWithVolume :> Base::Anything {
                            feature height:  SI::Length = oneOf(10.0 .. 100.0 [cm]);
                            feature width:   SI::Length = oneOf(1.0 .. 1.1  [m]);
                            feature length:  SI::Length = oneOf(1.0 .. 1.1  [m]);
                            feature volume:  SI::Volume = height * width * length {:>> range = "1 .. 2"; :>> unit = "m^3";}
                        }
                    """
        loadKerML(model)
        assertNoIssues()
        var volume = global.resolve<Feature>("PartWithVolume::volume")!!.variable
        val height = global.resolve<Feature>("PartWithVolume::height")!!.variable
        assertNotNull(height)
        assertEquals(1.0, volume!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.21, volume.vectorQuantity.getMaxAsDouble(), 0.001)
        propagate()
        initialize()
        initialize()
        volume = global.resolve<Feature>("PartWithVolume::volume")!!.variable
        assertEquals(1.0, volume!!.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.21, volume.vectorQuantity.getMaxAsDouble(), 0.001)
        loadKerML(model)
        propagate()
        assertNoIssues()
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
    fun extendPropertyRange() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                type Test :> Base::Anything {
                    feature property: Ranges::RealInRange { :>> range = "0 .. *";}
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
            type Test specializes Test;
            type P specializes Test;
        """)
        propagate()
        assertTrue(status.issues.isNotEmpty(), status.issues.toString())
    }

    /**
     * It seems that SysMLv2 allows features to be typed by features.
     * Feels wrong, but ... SysMLv2 allows features to be typed by types, and
     * features are types.
     */
    @Test fun issue165typedFeatureInheritance() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
            feature a : Ranges::IntegerInRange { :>> range = "0 .. 5";}
            feature x: a; // Here, a Type is needed. And a feature is a type ...
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), "Features should be OK for typing features?")
    }

    @Test
    fun issue184() = testSession("Ranges") {
        loadKerML("""
            feature weight: ScalarValues::Integer = oneOf(0..50);
            feature weightBoundary1: ScalarValues::Integer = oneOf(15);
            feature weightBoundary2: ScalarValues::Integer = oneOf(15);
            feature r: Ranges::BooleanInSpec = weight <= (weightBoundary1 + weightBoundary2) {:>> spec="true";}
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
        assertTrue(status.issues.any {  issue -> issue.message.contains("type") })
    }

    /**
     * Evolution of the issue ... we continuously check that the Modeling example works.
     */
    @Test
    fun issue190test() = testSession("SI", "ISO26262", "Parts")  {
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
                assert constraint enoughPower { drive::power > 0.0 kW }
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
    fun issue219InheritedNotShown() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                type Test :> Base::Anything {
                    feature a: Ranges::RealInRange {:>> range = "0..1";}
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

    @Test @Ignore //TODO: Problem in Parser: After or only Product possible, but EE is not in Product
    fun booleanExpression() = testSession {
        loadSysMLv2(
            input = """
            attribute c: ScalarValues::Integer = 1;
            attribute b: ScalarValues::Integer = 2;
            attribute a: ScalarValues::Boolean = c == 0 or b == 0.
            """.trimIndent()
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
                    in feature severity : Ranges::IntegerInRange {:>> range = "0..3";}
                    in feature exposure : Ranges::IntegerInRange {:>> range = "0..4";}
                    in feature controllability: Ranges::IntegerInRange {:>> range = "0..3";}
                    feature sum: ScalarValues::Integer = severity + exposure + controllability;
                    feature sumAdapted : ScalarValues::Integer = if (severity == 0) or (controllability == 0) ? 0 else sum; //special case for S0 and C0 the ASIL is always QM (0)
                    return result: ScalarValues::Integer = max(sum-6,0).
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
    fun referenceDuplicatesTestIssue260() = testSession("ScalarValues", "SI", "Parts", "Ports", "Requirements") {
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
        assertSame(testPart, testRef.referencedFeature as Feature)
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

    @Test
    fun assertTestRealMultiplication() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 4.0); 
            assert constraint ass { 2.0 * f < 4.0 } 
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(1.0, global.resolveVar("f")!!.aadd().getRange().min, 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }

    @Ignore
    @Test
    fun assertTestRealDiv() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 4.0); 
            assert constraint ass {  1.0 < 2.0 / f } 
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(1.0, global.resolveVar("f")!!.aadd().getRange().min, 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }

    @Ignore
    @Test
    fun assertTestRealAdd() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real; 
            assert constraint ass { 6.0 + f < 8.0 } 
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        //assertEquals(-2.0, global.resolveVar("f")!!.aadd().getRange().min, 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }

    @Test
    fun assertTestRealSub() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 4.0); 
            assert constraint ass { 6.0 - f < 4.0 } 
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(2.0, global.resolveVar("f")!!.aadd().getRange().min, 0.00001)
        assertEquals(4.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }

    @Ignore
    @Test
    fun assertTestRealDiv2() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 8.0); 
            assert constraint ass {  8.0/f > 4.0 } 
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
        assertEquals(1.0, global.resolveVar("f")!!.aadd().getRange().min, 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }


    @Test
    @Ignore
    fun issueExpression() = testSession("ScalarValues", "SI", "Ranges") {
        loadSysMLv2("""
            private import ScalarValues::*;
            attribute constraint FLOPS: Real = 5/5.0 ;
        """)
        propagate()
        assertTrue(status.issues.isEmpty(), status.issues.toString())
    }



    @Test
    fun issueEmptyBody() = testSession("Constraints", "Requirements") {
        loadSysMLv2("""
            attribute a: ScalarValues::Real;
            attribute b: ScalarValues::Real;
            assert constraint {  a < b }
        """)
        propagate()
        assertNoIssues()
    }

    @Test
    fun issue269() = testSession("ScalarValues") {
        loadKerML("""
            feature weight: ScalarValues::Integer(0..30);
            inv r { weight <= 15 }
        """)
        assertNoIssues()
    }
}


import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Function
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.session.loadLibrary
import io.github.tukcps.aadd.functions.numInternalNodes
import io.github.tukcps.aadd.values.IntegerRange
import io.github.tukcps.aadd.values.XBool
import io.github.tukcps.aadd.values.XBool.Companion.True
import org.junit.jupiter.api.assertAll
import util.assertIssue
import util.assertNoIssues
import util.findDifferenceById
import util.mockup.loadKerML
import util.mockup.loadSysMD
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.*

class IssuesAndRegressions {
    @Test
    @Ignore // IDD do not use solver so far, and finding the solution requires the LP solver
    fun minTestMultipleParams3Integer() = testSession("Ranges") {
        loadKerML("""
            feature a: Ranges::IntegerInRange {:>> range = "0..7";}
            feature b: Ranges::IntegerInRange {:>> range = "1..6";}
            feature c: Ranges::IntegerInRange {:>> range = "2..5";}
            feature d: Ranges::IntegerInRange {:>> range = "3..4";}
            feature e: Ranges::IntegerInRange = min(a,b,c,d) {:>> range = "4..5";}
        """)
        solver.propagate()
        assertNoIssues()
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
        assertNoIssues()
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
        assertNoIssues()
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
        solver.propagate()
        assertNoIssues()
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
        assertNoIssues()
        val elements = get()
        loadLibrary("ScalarValues")
        initialize()
        assertNoIssues()
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
        assertNoIssues()
        val first = get().toList()

        loadLibrary("Base")
        loadLibrary("ScalarValues")
        loadLibrary("Links")
        loadLibrary("Occurrences")
        initialize()
        val second = get()
        assertNoIssues()

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
    fun unitDisappearsTest() = testSession("Occurrences", "ISQ") {
        loadKerML("""
                    class Vehicle {
                        feature mass: ISQ::MassValue = bySpecializations(mass);
                    }
                    class Car :> Vehicle {
                        feature mass: ISQ::MassValue = 100.0 kg;
                    }
                    class Bicycle :> Vehicle {
                        feature mass: ISQ::MassValue = oneOf(10.0 .. 20.0 [kg]);
                    }
            """)
        solver.propagate()
        assertNoIssues()
        val vehicle: Class? = global.resolve("Vehicle")!!.member()
        val mass =vehicle?.resolveVar("mass")!!
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
        solver.propagate()
        assertNoIssues()
        val test = global.resolve("Test")?.member<Type>()
        val comp = test!!.resolve("comp")?.member<Feature>()!!
        assertEquals(1, comp.getOwnedElementsOfType<Multiplicity>().size) // Just the multiplicity
        assertEquals(IntegerRange(1, 2), comp.multiplicityRange)
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
        solver.propagate()
        assertNoIssues()
        val multi = global.resolveVar("ExampleDesign::Chassis::wheels::cardinality")!!
        assertNotNull(multi)
        assertEquals(2L, multi.min())
        assertEquals(6L, multi.max())
    }


    @Test
    fun variableUnknownIsReportedAsError() = testSession("ScalarValues") {
        loadKerML(input = "feature x: ScalarValues::Real = yyy;")
        assertTrue(status.issues.any { it.cause is ElementNotFoundException }, "There shall be error reporting yyy not defined.")
    }

    @Test
    fun typeUnknownIsReportedAsError() = testSession {
        loadKerML(input = " feature x: YYY;")
        assertIssue("YYY")
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
        assertNoIssues()
        val s1: Feature? = global.resolve("Device::sensor")?.member()
        val s2: Feature? = global.resolve("DeviceB::sensor")?.member()
        val s3: Feature? = global.resolve("DeviceA::sensor")?.member()
        assertNotEquals(s1, s2)
        assertNotEquals(s1, s3)
        assertNotEquals(s2, s3)
        assertEquals(IntegerRange(4,5), global.resolve("Device::sensor")?.member<Feature>()?.multiplicityRange)
        assertEquals(IntegerRange(4,4), global.resolve("DeviceB::sensor")?.member<Feature>()?.multiplicityRange)
        assertEquals(IntegerRange(5,5), global.resolve("DeviceA::sensor")?.member<Feature>()?.multiplicityRange)
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
        assertNoIssues()
        val inherited = global.resolve( "b::f1")?.memberElement
        assertNotNull(inherited)
        val inheritedUpdated = global.resolve("b::f1::f")?.memberElement
        assertNotNull(inheritedUpdated)
    }

    /**
     * Check that features from a superclass are inherited to a subclass.
     */
    @Test
    fun issue112_inheritPartsShort2() = testSession("Occurrences") {
        settings.catchExceptions = false
        loadKerML("""
            class a {
                feature f1: Base::Anything;
            }
            class b :> a;
        """)
        val bF1: Feature? = global.resolve("b::f1")?.member()
        assertNotNull(bF1)
        assertNoIssues()
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
    fun issue123_ThreeQualifiedNames() = testSession("Occurrences") {
        loadSysMD("""
            Global hasA class A.
            A hasA feature B.
            A::B hasA feature C.
            A::B::C hasA feature D.
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
            package X {
                private import Ranges::*;
                feature x: RealInRange { :>> range = "10.0..20.0";}
            }
        """)
        assertNoIssues()
        val x = global.resolveVar("X::x")
        assertNotNull(x)
        assertEquals(10.0, x.min(), 0.0001)
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
        solver.propagate()
        assertNoIssues()
        loadSysMD("""
            X::Z hasA feature x: Real = 10.0.
        """)
        solver.propagate()
        loadSysMD("""
            X::Z hasA feature x: Real = 10.0.
        """)
        solver.propagate()
        //  val generated = getOwnedElement(textualRepresentation,"Generated elements") as Annotation?

        val x = global.resolveVar("X::Z::x")
        assertNoIssues()
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
        solver.propagate()
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
        solver.propagate()
        assertNoIssues()
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
        solver.propagate()
        assertNoIssues()
        val result = global.resolveVar("b")
        assertEquals(2.0,result!!.vectorQuantity.getMinAsDouble(),0.000001)
        assertEquals(2.0,result.vectorQuantity.getMaxAsDouble(),0.000001)
        assertNoIssues()
    }


    /**
     * There was a problem with repeated analysis of the same model.
     * We test it here by an example.
     */
    @Test //Multiple executions of initialize() and solver.propagate() lead to not working detection of redefinition
    fun repeatedExecutionCausesWrongResultsTest() = testSession("ISQ") {
        val model = """
                        type PartWithVolume :> Base::Anything {
                            feature height:  ISQ::LengthValue = oneOf(10.0 .. 100.0 [cm]);
                            feature width:   ISQ::LengthValue = oneOf(1.0 .. 1.1  [m]);
                            feature length:  ISQ::LengthValue = oneOf(1.0 .. 1.1  [m]);
                            feature volume:  ISQ::VolumeValue = height * width * length {:>> range = "1 .. 2"; :>> unit = "m^3";}
                        }
                    """
        loadKerML(model)
        assertNoIssues()
        var volume = global.resolveVar("PartWithVolume::volume")!!
        val height = global.resolveVar("PartWithVolume::height")!!
        assertNotNull(height)
        assertEquals(1.0, volume.min(), 0.000001)
        assertEquals(1.21, volume.max(), 0.001)
        solver.propagate()
        initialize()
        initialize()
        volume = global.resolveVar("PartWithVolume::volume")!!
        assertEquals(1.0, volume.min(), 0.000001)
        assertEquals(1.21, volume.max(), 0.001)
        loadKerML(model)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, volume.min(), 0.000001)
        assertEquals(1.21, volume.max(), 0.001)
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
        assertNoIssues()
        val a: Package? = global.resolve("a")?.member()
        val ab: Type? = global.resolve("a::b")?.member()
        val abc: Type? = global.resolve("a::b::c")?.member()
        assertNotNull(a)
        assertNotNull(ab)
        assertNotNull(abc)
        val ab2: Type? = a.resolve("b")?.member()
        val abc2: Type? = ab.resolve("c")?.member()
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
        assertNoIssues()
        val result = global.resolveVar("Car::property")!!
        assertEquals(4.0, result.vectorQuantity.getMinAsDouble(), 0.000001)
    }


    /**
     * Issue 134: isSubClassOf runs into infinite loop instead of reporting error.
     */
    @Test fun issue134test() = testSession {
        loadKerML("""
            type Test specializes Test;
            type P specializes Test;
        """)
        solver.propagate()
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
        solver.propagate()
        assertTrue(status.issues.isEmpty(), "Features should be OK for typing features?")
    }

    @Test
    fun issue184() = testSession("Ranges") {
        loadKerML("""
            feature weight: ScalarValues::Integer = oneOf(0..50);
            feature weightBoundary1: ScalarValues::Integer = oneOf(15);
            feature weightBoundary2: ScalarValues::Integer = oneOf(15);
            inv r { weight <= (weightBoundary1 + weightBoundary2) }
        """)
        solver.propagate()
        assertNoIssues()
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
        val pp = global.resolve("p::p")?.memberElement // there is no p in p.
        solver.propagate()
        assertEquals("p", pp!!.escapedName())
        assertNoIssues()
    }

    @Test fun issue189nameResolutionIncorrect3() = testSession {
        loadKerML("""class p :> Base::Anything;""")
        val pp = global.resolve("p::p")?.memberElement // there is no p in p.
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
    fun issue190test() = testSession("ISQ", "ISO26262", "Parts")  {
        loadSysMLv2("""
            package archExample {
                private import ScalarValues::*;
                private import ISO26262::*;
                part def Vehicle :> ISO26262::Component {
                    part engine : Engine[1..2];
                    attribute power: ISQ::PowerValue in [kW] = engine::power * ToReal(engine::cardinality);
                }
                // Drive is a Function that has NO subclasses; its implementation alternatives are hence not
                // "by Subclasses, but "by Implements relationship".
                part def Drive :> Function;
                // Engine implements Drive.
                part def Engine :> ISO26262::Component {
                    attribute power: ISQ::PowerValue in [kW] = [10.0 .. 100.0] kW; // bySpecializations(power);
                }
                part def ElectricDrive :> Engine {
                    attribute power: ISQ::PowerValue(1..50) in [kW];
                }
                part def CombustionEngine :> Engine {
                    attribute power: ISQ::PowerValue(10..200) in [kW];
                }

                part vehicle: Vehicle;
                part drive: Drive {
                    attribute power: ISQ::PowerValue in [kW] = byImplements(power);
                }
                connection r: ISO26262::implements connect vehicle::engine to drive;
                assert constraint enoughPower { drive::power > 0.0 kW }
            }
        """)
        assertNoIssues()
        val drive = global.resolve("archExample::drive")?.member<Feature>()
        assertNotNull(drive)
        solver.propagate()
        assertNoIssues()
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
        solver.propagate()
        // println(status.numberOfPropagateIterations)
        assertNoIssues()
    }

    @Test
    fun issue219InheritedNotShown() = testSession("ScalarValues", "Ranges") {
        loadKerML("""
                type Test :> Base::Anything {
                    feature a: Ranges::RealInRange {:>> range = "0..1";}
                }
                type Test2 :> Test;
            """)
        assertNoIssues()
    }

    @Test
    fun issue222SumOverParts() = testSession("ScalarValues") {
        loadKerML("""
            private import ScalarValues::*;
            type p :> Base::Anything {
                feature a {
                    feature s: Real = 1.0;
                }
                feature b {
                    feature s: Real = 2.0;
                }
                feature s1: Real = sumOverParts(s);
        }""")
        assertNoIssues()
        assertEquals(3.0, global.resolveVar("p::s1")!!.min(), 0.0001)
    }

    //Tests for Issue #243
    @Test @Ignore
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
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
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

    @Test @Ignore
    fun issue243indexExplosionSmallModelTest() = testSession("ScalarValues") {
        loadKerML(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            """
        )
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
        global.resolveVar("x1")!!
        global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!
        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")
    }

    @Test @Ignore
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
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
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

    @Test @Ignore
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
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
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

    @Test @Ignore
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
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
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

    @Test @Ignore
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
        assertNoIssues()
        solver.propagate()
        assertNoIssues()
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
        solver.propagate()
        assertNoIssues()
        global.resolveVar("x")
    }

    @Test
    fun exponentiationIsRightAssociative() = testSession("ScalarValues") {
        loadKerML("""
            feature x: ScalarValues::Real = 2.0 ^ 2.0 ^ 3.0;
        """.trimIndent())
        val x = global.resolveVar("x")
        assertEquals(256.0, x!!.min(), 0.0001)
    }

    @Test @Ignore //TODO: Problem in Parser: After or only Product possible, but EE is not in Product
    fun booleanExpression() = testSession {
        loadSysMLv2(input = """
            attribute c: ScalarValues::Integer = 1;
            attribute b: ScalarValues::Integer = 2;
            attribute a: ScalarValues::Boolean = c == 0 or b == 0.
        """)
        solver.propagate()
        assertNoIssues()
    }

    /**
     * Goes into infinite loop --> BUG in IDD * IDD !
     */
    @Test @Ignore
    fun iddTimesLoopIssue267() = testSession("ScalarValues") {
        loadKerML("""
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
        """)
        solver.propagate()
        assertNoIssues()
    }

    /**
     * Everything seems OK; no duplicated features.
     * BUT: Supertypes are duplicated.
     */
    @Test
    fun referenceDuplicatesTestIssue260() = testSession("ScalarValues", "ISQ", "Parts", "Ports", "Requirements") {
        settings.catchExceptions = true
        loadSysMLv2("""
            private import ScalarValues::*;
            private import ISQ::*;
            part testPart {
                attribute att: Real [m];
                in port input;
            }

            requirement testReq {
                subject testRef references testPart;
            }
        """)
        assertNoIssues()
        val ref = global.resolve("testReq::testRef")?.memberElement
        val testPart = global.resolve("testPart")?.memberElement
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
        assertNoIssues()
    }

    @Test
    fun assertTestRealMultiplication() = testSession("ScalarValues", "ISQ", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 4.0); 
            assert constraint ass { 2.0 * f < 4.0 } 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("f")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }

    @Ignore
    @Test
    fun assertTestRealDiv() = testSession("ScalarValues", "ISQ", "Ranges") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 4.0); 
            assert constraint ass {  1.0 < 2.0 / f } 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("f")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.max(), 0.00001)
    }

    @Ignore
    @Test
    fun assertTestRealAdd() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real; 
            assert constraint ass { 6.0 + f < 8.0 } 
        """)
        solver.propagate()
        assertNoIssues()
        //assertEquals(-2.0, global.resolveVar("f")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }

    @Test
    fun assertTestRealSub() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 4.0); 
            assert constraint ass { 6.0 - f < 4.0 } 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(2.0, global.resolveVar("f")!!.min(), 0.00001)
        assertEquals(4.0, global.resolveVar("f")!!.aadd().getRange().max, 0.00001)
    }

    @Ignore
    @Test
    fun assertTestRealDiv2() = testSession("ScalarValues") {
        loadSysMLv2("""
            attribute f: ScalarValues::Real = oneOf(1.0 .. 8.0); 
            assert constraint ass {  8.0/f > 4.0 } 
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1.0, global.resolveVar("f")!!.min(), 0.00001)
        assertEquals(2.0, global.resolveVar("f")!!.max(), 0.00001)
    }


    @Test
    @Ignore
    fun issueExpression() = testSession("ScalarValues", "ISQ", "Ranges") {
        loadSysMLv2("""
            private import ScalarValues::*;
            attribute constraint FLOPS: Real = 5/5.0 ;
        """)
        solver.propagate()
        assertNoIssues()
    }



    @Test
    fun issueEmptyBody() = testSession("Constraints", "Requirements") {
        loadSysMLv2("""
            attribute a: ScalarValues::Real;
            attribute b: ScalarValues::Real;
            assert constraint {  a < b }
        """)
        solver.propagate()
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

    @Test
    fun newExpr() = testSession("ScalarValues") {
        loadKerML("""
            feature x : ScalarValues::Integer = 10;
        """)
        assertNoIssues()
    }

    @Test
    fun checkFunctions() = testSession("DataFunctions") {
        assertAll(get().filter { it.isLibraryElement }.filterIsInstance<Function>().map { f -> {
            val res = assertNotNull(f.result, "${f.qualifiedName} has no result")
            val param = f.parameter

            assertEquals(Feature.FeatureDirectionKind.OUT, res.direction)
            assertSame(res, param.last(), "Return of ${f.qualifiedName} isn't last parameter")

            param.dropLast(1).forEach {
                assertEquals(Feature.FeatureDirectionKind.IN, it.direction, "stdlib functions should be pure")
            }

            /*println(
                f.qualifiedName + " :: "  + f.parameter.joinToString(" -> ") {
                    val type = it.type.first().run { escapedName() ?: path() }
                    /*it.escapedName()?.let { n ->
                        "($n :: $type)"
                    } ?:*/ type
                }
            )*/
        } })
    }

    @Test
    fun aliasing() = testSession {
        loadKerML("""
            package Foo {
                feature xyz;
                alias bar for xyz;
            }
            package Foo2 {
                alias bar for Foo::xyz;
            }
        """.trimIndent())
        assertNoIssues()

        val pkg = assertIs<Package>(global.resolve("Foo")?.memberElement)
        pkg.visibleMemberships(isRecursive = false, includeAll = false).let { vis ->
            assertEquals(2, vis.size)
            assertEquals(1, vis.count { it is OwningMembership })
            val alias = vis.single { it !is OwningMembership }
            assertEquals("bar", alias.escapedName())
        }

        val f = assertIs<Feature>(global.resolve("Foo::bar")?.memberElement)
        assertEquals("xyz", f.escapedName())

        assertNull(global.resolve("Foo2::xyz"), "alias must not expose underlying feature")
        val f2 = assertIs<Feature>(global.resolve("Foo2::bar")?.memberElement,
            "cross-package alias must be visible"
        )
        assertEquals("xyz", f2.escapedName())
    }
}


import com.github.tukcps.aadd.functions.numInternalNodes
import com.github.tukcps.aadd.values.IntegerRange
import com.github.tukcps.aadd.values.XBool
import com.github.tukcps.aadd.values.XBool.Companion.True
import com.github.tukcps.sysmd.cspsolver.propagate
import com.github.tukcps.sysmd.exceptions.ElementNotFoundException
import com.github.tukcps.sysmd.exceptions.SysMDInfo
import com.github.tukcps.sysmd.model.kerml.*
import com.github.tukcps.sysmd.model.kerml.Annotation
import com.github.tukcps.sysmd.model.kerml.implementation.FeatureImplementation
import com.github.tukcps.sysmd.model.kerml.implementation.SpecializationImplementation
import com.github.tukcps.sysmd.compiler.loadProject
import com.github.tukcps.sysmd.compiler.loadSysMD
import com.github.tukcps.sysmd.compiler.loadSysMDFromFile
import com.github.tukcps.sysmd.services.initialize
import com.github.tukcps.sysmd.services.resolve.resolveVar
import com.github.tukcps.sysmd.services.resolve.resolve
import com.github.tukcps.sysmd.services.session.SessionManager.testSession
import com.github.tukcps.sysmd.services.session.getAllOfClass
import org.junit.jupiter.api.Disabled
import kotlin.test.*

class IssuesAndRegressions {

    /** Issue #247 in Gitlab */
    @Test
    fun issue247() = testSession(loadKerML = false) {
        + """
            type Vehicle :> Base::Anything {
               feature car : Vehicle;
            }
        """
        assertTrue(status.exceptions.isNotEmpty(), "A feature may not be typed by a class that is its owner.")
    }

    /** Issue #247 v2 in Gitlab: This is OK. */
    @Test
    fun issue247v2() = testSession(loadKerML = false) {
        + """
            type Vehicle :> Base::Anything {
               class Car :> Vehicle;
            }
        """
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /** Issue #129 in Gitlab. */
    @Test
    fun issue129test() = testSession {
        loadSysMD("""
            Global hasA attribute x: ScalarValues::Real = 12.3.
        """.trimIndent()
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    /** Issue #34 in Gitlab. */
    @Test
    fun issue34test() = testSession {
        loadSysMD(
            catchExceptions = false,
            input = """
            import ScalarValues;
            class Wheel { feature price : Real (1.0..1.0); }
            class Body { feature price : Real (1.0..1.0); }
            class Chassis { feature price : Real (1.0..1.0); }
            class Car {
                feature wheel: Wheel;
                feature body: Body;
                feature chassis: Chassis;
                feature carPrice: Real = sumOverParts(price); 
            }
            """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val price = global.resolveVar("Car::carPrice")!!
        assertEquals(3.0, price.min(), 0.000001)
        assertEquals(3.0, price.max(), 0.000001)
    }

    /**
     * After repeated load of a document, its elements should be updated,
     * but not duplicated.
     */
    @Test fun elementsNotAppearTwice() = testSession {
        loadSysMDFromFile("ScalarValues.md")
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val elements = get()
        loadSysMDFromFile("ScalarValues.md")
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val elements2 = get()
        assertEquals(elements.size, elements2.size)
    }


    /** ... more complex integration test to be sure. Often complex */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice() = testSession(catchExceptions = true) {
        loadProject("ISO26262")
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val spec1 = getAllOfClass<Specialization>()
        val imp1 = getAllOfClass<Import>()
        val prop1 = getAllOfClass<Feature>()
        val class1 = getAllOfClass<Class>()
        val elem1 = getAllOfClass<Element>()
        val annotations1 = getAllOfClass<Annotation>()
        loadProject("ISO26262")
        initialize()
        // assertEquals(0, status.errors.size+status.errors.size)
        val multiplicities2 = getAllOfClass<Multiplicity>()
        val elem2 = getAllOfClass<Element>()
        val spec2 = getAllOfClass<Specialization>()
        val imp2 = getAllOfClass<Import>()
        val prop2 = getAllOfClass<Feature>()
        val class2 = getAllOfClass<Class>()
        val annotations2 = getAllOfClass<Annotation>()

        // val rest = elem2.filter { it !is a Specialization && it !is Import && it !is Feature && it !is Class }

        assertEquals(multiplicities1.size, multiplicities2.size)
        assertEquals(spec1.size, spec2.size)
        assertEquals(imp1.size, imp2.size)
        assertEquals(prop1.size, prop2.size)
        assertEquals(class1.size, class2.size)
        assertEquals(annotations1.size, annotations2.size)
        assertEquals(elem1.size, elem2.size)

        // reason possible: in particular, multiplicity that is generated
        // - must not be generated a second time.
    }

    /** Direct test on Multiplicity by simple example. */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice1() = testSession(loadKerML = false) {
        loadSysMD("package ScalarValues { datatype Integer; }; feature x; ", false)
        initialize()
        assertTrue(status.exceptions.isEmpty())
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val elem1 = getAllOfClass<Element>()
        loadSysMD("feature x; ", false)
        initialize()
        assertEquals(0, status.exceptions.size)
        val multiplicities2 = getAllOfClass<Multiplicity>()
        val elem2 = getAllOfClass<Element>()
        assertEquals(multiplicities1.size, multiplicities2.size)
        assertEquals(elem1.size, elem2.size)
    }

    /** Direct test on Specialization */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice2() = testSession(loadKerML = false) {
        loadSysMD("class x isA Base::Anything; ")
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val elem1 = getAllOfClass<Element>()
        loadSysMD("class x;")
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val multiplicities2 = getAllOfClass<Multiplicity>()
        val elem2 = getAllOfClass<Element>()
        assertEquals(multiplicities1.size, multiplicities2.size)
        assertEquals(elem1.size, elem2.size)
    }

    /** ... same for inherited properties */
    @Test
    fun featuresAndMultiplicitiesNotAppearTwice3() = testSession(loadKerML = false) {
        loadSysMD("package ScalarValues { datatype Integer; }; class x { feature y;}")
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val multiplicities1 = getAllOfClass<Multiplicity>()
        val imports1 = getAllOfClass<Import>()
        val specs1 = getAllOfClass<Specialization>()
        val elem1 = getAllOfClass<Element>()
        loadSysMD("class x { feature y; } ")
        initialize()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
    fun unitDisappearsTest() = testSession {
        settings.catchExceptions = false
        loadSysMD(
            """
                class Vehicle {
                    attribute mass: ScalarValues::Real [kg] = bySubclasses(mass);                 
                }
                class Car :> Vehicle {
                    attribute mass: ScalarValues::Real [kg] = 100.0 kg; 
                }
                class Bicycle :> Vehicle {
                    attribute mass: ScalarValues::Real(10 .. 20) [kg];                 
                }
            """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
    fun multiplicityTwiceOrMissingTest() = testSession {
        loadSysMD("""
                class Test {
                    part comp: Base::Anything[1 .. 2];
                }
            """.trimIndent()
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val test = global.resolve<Classifier>("Test") !!
        val comp = test.resolve<Feature>("comp") !!
        assertEquals(1, comp.getOwnedElementsOfType<Multiplicity>().size) // Just the multiplicity
        assertEquals(IntegerRange(1, 2), comp.multiplicity)
    }

    /** Use of multiplicity as variable. */
    @Test
    fun automotiveExample() = testSession  {
        loadSysMD(catchExceptions = false, input = """
            package ExampleDesign;
            ExampleDesign defines
                class Axis;
                class Wheels;
                class Chassis.
            ExampleDesign::Chassis hasA
                part  wheels:  ExampleDesign::Wheels[2..6]; 
                attribute numAxis: ScalarValues::Integer = wheels::multiplicity/2 .
        """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val multi = global.resolve<Feature>("ExampleDesign::Chassis::wheels::multiplicity")!!.variable
        assertNotNull(multi)
        assertEquals(2.0, multi.min())
        assertEquals(6.0, multi.max())
    }


    @Test
    fun variableUnknownIsReportedAsError() = testSession {
        loadSysMD(input = "feature x: ScalarValues::Real = yyy;")
        assertTrue(status.exceptions.first() is ElementNotFoundException, "There shall be error reporting yyy not defined.")
    }

    @Test
    fun typeUnknownIsReportedAsError() = testSession {
        loadSysMD(input = " feature x: YYY;")
        assertTrue(status.exceptions.first() is SysMDInfo, "There shall be error reporting that YYY is not defined.")
    }

    @Test
    fun issue112_inheritedPartsOverrideMultiplicity() = testSession(catchExceptions = false) {
        loadSysMD("""
            class Device {
                feature sensor [4..5];
            }
            class DeviceA :> Device {
                feature sensor [5..5];      
            }
            class DeviceB isA Device {
                feature sensor [4..4];
            }
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val s1 = global.resolve<Feature>("Device::sensor")
        val s2 = global.resolve<Feature>("DeviceB::sensor")
        val s3 = global.resolve<Feature>("DeviceA::sensor")
        assertNotEquals(s1, s2)
        assertNotEquals(s1, s3)
        assertNotEquals(s2, s3)
        assertEquals(IntegerRange(4,5), global.resolve<Feature>("Device::sensor")?.multiplicity)
        assertEquals(IntegerRange(4,4), global.resolve<Feature>("DeviceB::sensor")?.multiplicity)
        assertEquals(IntegerRange(5,5), global.resolve<Feature>("DeviceA::sensor")?.multiplicity)

        // assertEquals(0, status.info.size, status.info.toString())
    }


    @Test
    fun issue112_inheritPartsShort() = testSession {
        settings.catchExceptions = false
        loadSysMD("""
                class a {
                  feature f1;
                }
                class b :> a;
                b::f1 hasA feature f: Base::Anything.
            """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val inherited = global.resolve<Element>( "b::f1")
        assertNotNull(inherited)
        val inheritedUpdated = global.resolve<Element>("b::f1::f")
        assertNotNull(inheritedUpdated)
    }

    /**
     * Check that features from a superclass are inherited to a subclass.
     */
    @Test
    fun issue112_inheritPartsShort2() = testSession {
        settings.catchExceptions = false
        loadSysMD(
            """
                class a specializes Base::Anything;
                class b isA a;
                a hasA feature f1: Base::Anything;
            """)
        val bF1 = global.resolve<Feature>("b::f1")
        assertNotNull(bF1)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun issue118_cyclicClassification() = testSession {
        loadSysMD(
            """
            class A isA B;
            class B isA C;
            class C isA A;
        """.trimIndent()
        )
        assertTrue(status.exceptions.isNotEmpty(), status.exceptions.toString())
    }

    /**
     * Check for proper definition of a feature owner and report info if i.e., B is not defined.
     */
    @Test
    fun issue136NoErrorOnUndeclaredFeatureClass() = testSession {
        loadSysMD("A hasA feature B: Base::Anything.")
        assertTrue(0 < status.exceptions.size, status.exceptions.toString())
    }

    @Test
    fun issue123_ThreeQualifiedNames() = testSession("Parts") {
        loadSysMD("""
            class A isA  Base::Anything.
            A hasA part B:  Base::Anything.
            A::B hasA part C:  Base::Anything.
            A::B::C hasA part D:  Base::Anything.
        """.trimIndent())
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    /**
     * Issue: Update is not performed and not initialized and propagated properly.
     */
    @Test
    fun updateFeatureTest() = testSession {
        loadSysMD("""
            import ScalarValues;
            package X {
                attribute x: Real(10.0);
                attribute y: Real = x * 2.0;
            }
        """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        loadSysMD("""
           X hasA attribute x: Real(20.0).
           X hasA attribute y: Real = x * 2.0.
        """.trimIndent())
        propagate()
        val x = global.resolveVar("X::x")
        val y = global.resolveVar("X::y")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(20.0, x?.vectorQuantity?.getMinAsDouble()!!, 0.0001)
        assertEquals(40.0, y?.vectorQuantity?.getMinAsDouble()!!, 0.0001)
    }


    /**
     * Issue: Update is not performed and not initialized and propagated properly.
     */
    @Test
    fun updateFeatureTest2() = testSession {
        loadSysMD("""
            package X { class Y; class Z isA Y; }
            import ScalarValues::*;
            X::Z hasA feature x: Real(10.0).
            """.trimIndent())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        loadSysMD("""
            X::Z hasA feature x: Real(10.0).
            """.trimIndent())
        propagate()
        loadSysMD("""
           X::Z hasA feature x: Real(10.0).
        """.trimIndent())
        propagate()
        //  val generated = getOwnedElement(textualRepresentation,"Generated elements") as Annotation?

        val x = global.resolveVar("X::Z::x")
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(10.0, x?.vectorQuantity?.getMinAsDouble()!!, 0.0001)
    }

    @Test
    fun propagateStringTest() = testSession {

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


    @Test  // Test does not run, because division by interval with zero during evalDown (l::c4::a). If 0 is changed to 0.1, everything works
    fun astSumIsATest3EvalDown() = testSession {
        loadSysMD("""
                 import ScalarValues;
                 package l {
                    class c1;
                    class c2 isA l::c1;
                    class c3 isA l::c1;
                    class c4 isA l::c3;
                    class c5 isA l::c3;
                 }

                 l::c2 hasA
                    attribute a: Real(0.8..0.8); 
                    attribute b: Real(0.5..0.5).
                 l::c4 hasA
                    attribute a: Real(0.0..1.0);
                    attribute b: Real(0.5..0.5).
                 l::c5 hasA
                    attribute a: Real(0.6..0.6); 
                    attribute b: Real(0.5..0.5).
                 l::c1 hasA
                    attribute resultingSecurityOfSupply: Real(1.05..1.05) = sumOverSubclasses(a*b)."""
        )
        propagate()
        // assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(0.7, global.resolveVar("l::c4::a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(0.7, global.resolveVar("l::c4::a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

    @Test
    fun simpleMultiplicationWithZero() = testSession {
        +"""import ScalarValues;
            attribute a: Real(1..1);
            attribute b: Real(0..5);
            attribute c: Real(2..2) = a*b;"""
        propagate()
        assertEquals(0, status.exceptions.size, "Error messages: ${status.exceptions}")
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(2.0, global.resolveVar("b")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("a")!!.vectorQuantity.getMinAsDouble(), 0.0001)
        assertEquals(1.0, global.resolveVar("a")!!.vectorQuantity.getMaxAsDouble(), 0.0001)
    }

     @Test fun maxTestWihAst() = testSession("ISO26262") {
         loadSysMD(input = """
            import ScalarValues;
            import ISO26262;
            package HwSwPerformance {
                part def ConvLayerModel isA Component;
                part def HardwareModel isA Component;
                part def RefinedRooflineModel isA Component;
            }

            HwSwPerformance::ConvLayerModel hasA
                attribute C: Integer(1 .. *); // Number of input channels
                attribute K: Integer(1 .. *); // Number of kernel/output channels
                attribute H: Integer(1 .. *); // Input height
                attribute W: Integer(1 .. *); // Input width
                attribute F_H: Integer(1 .. *); // Filter height
                attribute F_W: Integer(1 .. *); // Filter width

                attribute s_h: Integer(1 .. *); // Stride in height dimension
                attribute s_w: Integer(1 .. *); // Stride in width dimension
                attribute p_h: Integer(0 .. *); // Padding in height dimension
                attribute p_w: Integer(0 .. *); // Padding in width dimension
                attribute d_h: Integer(1 .. *); // Dilation in height dimension
                attribute d_w: Integer(1 .. *); // Dilation in width dimension

                attribute X_H: Integer(1 .. *) = ToInteger(floor(((ToReal(H)+2.0*ToReal(p_h)-ToReal(d_h)*(ToReal(F_H)-1.0)-1.0)/ToReal(s_h))+1.0)); // Output height
                attribute X_W: Integer(1 .. *) = ToInteger(floor(((ToReal(W)+2.0*ToReal(p_w)-ToReal(d_w)*(ToReal(F_W)-1.0)-1.0)/ToReal(s_w))+1.0)); // Output width

                attribute x0: Integer(1 .. *);
                attribute x1: Integer(1 .. *);
                attribute bits_per_word: Integer(1 .. *); 

                attribute fn: Integer(1 .. *) = K * C * H * W * F_H * F_W; 
                attribute dn: Integer(1 .. *) = (C * X_H * X_W * bits_per_word) + (C * K * F_H * F_W * bits_per_word) + (K * X_H * X_W * bits_per_word). // [bits]

            HwSwPerformance::HardwareModel hasA
                attribute peakPerformance: Real(0.1 .. *) [1/s]; // [ops/s]
                attribute peakBandwidth: Real(0.1 ..*) [1/s]; // [bit/s]
                attribute s0: Integer(1 .. *);
                attribute s1: Integer(1 .. *).

            HwSwPerformance::RefinedRooflineModel hasA
                part hw: HwSwPerformance::HardwareModel; 
                part nn: HwSwPerformance::ConvLayerModel; 
                attribute performance: Real(0 .. *) [s] = ToReal(nn::dn)/hw::peakBandwidth.

            """.trimIndent(), catchExceptions = true)
         assertEquals(0, status.exceptions.size, status.exceptions.toString())
         propagate()
         val result = global.resolveVar("HwSwPerformance::RefinedRooflineModel::performance")
         assertEquals(Double.NEGATIVE_INFINITY,result!!.vectorQuantity.getMinAsDouble())
         assertEquals(Double.POSITIVE_INFINITY,result.vectorQuantity.getMaxAsDouble())
         assertEquals(0, status.exceptions.size, status.exceptions.toString())
         //assertEquals(0.0001,result!!.quantity.getMinAsDouble(),0.000001)
         //assertEquals(100000.0,result.quantity.getMaxAsDouble(),0.000001)
    }


    @Test
    fun maxTest1EvalDown() = testSession {
        loadSysMD(input = """
            feature a: ScalarValues::Real(1..1);
            feature b: ScalarValues::Real(0..5);
            feature c: ScalarValues::Real(2..2) = max(a,b);
            """.trimIndent(), catchExceptions = true)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolveVar("b")
        assertEquals(2.0,result!!.vectorQuantity.getMinAsDouble(),0.000001)
        assertEquals(2.0,result.vectorQuantity.getMaxAsDouble(),0.000001)
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }


    /**
     * There was a problem with repeated analysis of the same model.
     * We test it here by an example.
     */
    @Test fun repeatedExecutionCausesWrongResultsTest() = testSession {
        val model = """
                        import ScalarValues;
                        class PartWithVolume {
                            feature height:  Real(10 .. 100) [cm];
                            feature width:   Real(1 .. 1.1)  [m];
                            feature length:  Real(1 .. 1.1)  [m];
                            feature volume:  Real(1 .. 2)    [m^3] = height * width * length;
                        }
        """.trimIndent()
        loadSysMD(model)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
        loadSysMD(model)
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        assertEquals(1.0, volume.vectorQuantity.getMinAsDouble(), 0.000001)
        assertEquals(1.21, volume.vectorQuantity.getMaxAsDouble(), 0.001)
    }

    @Test fun nameOfDefinitionTest() = testSession {
        loadSysMD("""
            package a {
                class b {
                   class c; 
                }
            }
        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val a = global.resolve<Package>("a")
        val ab = global.resolve<Class>("a::b")
        val abc = global.resolve<Classifier>("a::b::c")
        assertNotNull(a)
        assertNotNull(ab)
        assertNotNull(abc)
        val ab2 = a.resolve<Classifier>("b")
        val abc2 = ab.resolve<Classifier>("c")
        assertNotNull(ab2)
        assertNotNull(abc2)
    }

    @Test
    fun extendPropertyRange() = testSession {
        loadSysMD(input = """
            class Test {
                feature property: ScalarValues::Real(0 .. *);
            }

            class Car :> Test {
                feature property: ScalarValues::Real = 4.0; // property: Real(0 .. *) = 4.0. works
            }
        """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val result = global.resolve<Feature>("Car::property")!!.variable
        assertEquals(4.0, result!!.vectorQuantity.getMinAsDouble(), 0.000001)
    }


    /**
     * Issue 134: isSubClassOf runs into infinite loop instead of reporting error.
     */
    @Test fun issue134test() = testSession(loadKerML = false) {
        loadSysMD("""
            class Test specializes Test;
            class P specializes Test;
        """.trimIndent())
        propagate()
        assertTrue(0 < status.exceptions.size, status.exceptions.toString())
    }

    /**
     * It seems that SysMLv2 allows features to be typed by features.
     * Feels wrong, but ... SysMLv2 allows features to be typed by types, and
     * features are types.
     */
    @Test fun issue165typedFeatureInheritance() = testSession {
        loadSysMD("""
            feature a : ScalarValues::Integer(0 .. 5);
            feature x: a; // Here, a Type is needed. And a feature is a type ... 
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), "Features should be OK for typing features?")
    }

    //Did not yet create new issue. Issue #184 is the nearest thematically.
    @Test @Disabled
    fun issue184() = testSession(catchExceptions = false) {
        loadSysMD(
            """
                feature weight: ScalarValues::Integer(0..50),
                feature weightBoundary1: ScalarValues::Integer(15),
                feature weightBoundary2: ScalarValues::Integer(15),
                feature r: ScalarValues::Boolean(true) = weight <= (weightBoundary1 + weightBoundary2);
            """.trimIndent()
        )
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        val wb1 = global.resolveVar("weightBoundary1")!!
        val wb2 = global.resolveVar("weightBoundary2")!!

        assertEquals(15, wb1.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(15, wb1.vectorQuantity.value.asIdd().getRange().max)
        assertEquals(15, wb2.vectorQuantity.value.asIdd().getRange().min)
        assertEquals(15, wb2.vectorQuantity.value.asIdd().getRange().max)
    }

    @Test fun issue189nameResolutionIncorrect() = testSession(catchExceptions = false) {
        loadSysMD("""
            package p { class p isA Base::Anything; }
        """.trimIndent())
        // Failure: 'Specialization' object is created in package p, not in element p.
        val pp = global.resolve<Element>("p::p") // there is no p in p.
        propagate()
        assertEquals("p", pp!!.escapedName())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    @Test fun issue189nameResolutionIncorrect3() = testSession(catchExceptions = false) {
        +"""
            class p :> Base::Anything;
        """
        val pp = global.resolve<Element>("p::p") // there is no p in p.
        assertEquals(null, pp)
    }

    @Test fun issue189nameResolutionIncorrect4() = testSession("ISO26262", "SI", catchExceptions = false) {
        loadSysMD("""
        package AutomotiveBussystem {
            import ScalarValues::*;
            import ISO26262::*;

            class AutomotiveBussystem isA Component;
            class CANLowSpeed isA AutomotiveBussystem;
            class CANHighSpeed isA AutomotiveBussystem;
            class Ethernet10Base isA AutomotiveBussystem;
            class Ethernet100Base isA AutomotiveBussystem;
            class Ethernet1000Base isA AutomotiveBussystem;
            class Ethernet2500Base isA AutomotiveBussystem;
        }

        AutomotiveBussystem::AutomotiveBussystem hasA
            attribute Bandbreite: Real [Mbit/s].

        AutomotiveBussystem::CANLowSpeed hasA
            attribute Bandbreite: Real [Mbit/s] = 0.13 [Mbit/s].

        AutomotiveBussystem::CANHighSpeed hasA
            attribute Bandbreite: Real [Mbit/s] = 0.5 [Mbit/s].

        AutomotiveBussystem::Ethernet10Base hasA
            attribute Bandbreite: Real [Mbit/s] = 10.0 [Mbit/s].

        AutomotiveBussystem::Ethernet100Base hasA
            attribute Bandbreite: Real [Mbit/s] = 100.0 [Mbit/s].

        AutomotiveBussystem::Ethernet1000Base hasA
            attribute Bandbreite: Real [Mbit/s] = 1000.0 [Mbit/s].

        AutomotiveBussystem::Ethernet2500Base hasA
            attribute Bandbreite: Real [Mbit/s] = 2500.00 [Mbit/s].
                     """.trimIndent())
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        val pp = global.resolve<Element>("p::p") // there is no p in p.
        assertEquals(null, pp)
    }

    @Test fun issue190InvalidTypeNotReported() = testSession  {
        loadSysMD("""
            package x; 
            class a :> x; 
        """)
        assertEquals(1, status.exceptions.size, status.exceptions.toString())
        assertTrue(status.exceptions.toString().contains("type"))
    }

    /**
     * Evolution of the issue ... we continuously check that the Modeling example works.
     */
    @Test
    fun issue190test() = testSession("ISO26262", "SI", catchExceptions = false)  {
        loadSysMD(catchExceptions = false, input = """
            import ScalarValues::*;
            import ISO26262::*;
            package archExample {
                part def Vehicle isA Component {
                    part engine: [1..2] Engine;
                    attribute power: SI::Power [kW] = engine::power * ToReal(engine::multiplicity);
                }
                // Drive is a Function that has NO subclasses; its implementation alternatives are hence not
                // "by Subclasses, but "by Implements relationship".
                part def Drive isA Function;
                // Engine implements Drive.
                part def Engine isA Component {
                    attribute power: SI::Power [kW] = [10.0 .. 100.0] kW; // bySubclasses(power);
                }
                part def ElectricDrive isA Engine {
                    attribute power: SI::Power(1..50) [kW].
                }
                part def CombustionEngine isA Engine {
                    attribute power: SI::Power(10..200) [kW].
                }

                part vehicle: Vehicle;
                part drive: Drive {
                    attribute power: SI::Power [kW] = byImplements(power);
                }
                connector r: ISO26262::implements from vehicle::engine to drive;
                inv enoughPower { drive::power > 0.0 kW }
            }
        """)
        val drive = global.resolve<Feature>("archExample::drive")
        assertNotNull(drive)
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
    fun issue194NoConvergenceIfNaNisResult() = testSession  {
        loadSysMD("""
            attribute result3: ScalarValues::Real = power2(10000000000.0);
        """)
        propagate()
        // println(status.numberOfPropagateIterations)
        assertTrue( status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun issue219InheritedNotShown() = testSession("ISO26262") {
        +"""
            import ISO26262;
            class Test :> Element;
            class Test2 :> Test;
            Test hasA attribute a: ScalarValues::Real(0..1);
        """
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }

    @Test
    fun issue222SumOverParts() = testSession {
        +"""
            import ScalarValues;
            class p {
                feature a {
                    feature s: Real = 1.0;
                }
                feature b {
                    feature s: Real = 2.0;
                }                
                feature s1: Real = sumOverParts(s);
            }"""
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        assertEquals(3.0, global.resolveVar("p::s1")!!.min(), 0.0001)
    }

    //Tests for Issue #243
    @Test @Disabled
    fun issue243indexExplosionBiggerModelTest() = testSession {
        loadSysMD(
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
            """
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
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
    fun issue243indexExplosionSmallModelTest() = testSession {
        loadSysMD(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            """
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        global.resolveVar("x1")!!
        global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!



        //println(r1.vectorQuantity.bdd().toIteString())
        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")
    }

    @Test @Disabled
    fun issue243indexExplosionDuplicateComparisonTest() = testSession {
        loadSysMD(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            attribute r2: ScalarValues::Boolean = x1 <= y1;
            //attribute r3: ScalarValues::Boolean = r1 and r2;
            """
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        global.resolveVar("x1")!!
        global.resolveVar("y1")!!
        val r1 = global.resolveVar("r1")!!
        val r2 = global.resolveVar("r2")!!

        //val r3 = global.resolveName<Expression>("r3")!!


        //println(r1.vectorQuantity.bdd().toIteString())
        println("r1 depth: ${r1.vectorQuantity.bdd().height()}")
        println("r1 #nodes: ${r1.vectorQuantity.bdd().numInternalNodes()}")
        println("r1 bdd: ${r1.vectorQuantity.bdd().toIteString()}")

        println("r2 depth: ${r2.vectorQuantity.bdd().height()}")
        println("r2 #nodes: ${r2.vectorQuantity.bdd().numInternalNodes()}")
        println("r2 bdd: ${r2.vectorQuantity.bdd().toIteString()}")

        //println("r3: ${r3.vectorQuantity.bdd().toIteString()}")
    }

    @Test @Disabled
    fun issue243indexExplosionSmallModelDeltaTest() = testSession {
        loadSysMD(
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
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
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
        loadSysMD(
            input = """
            attribute x1: ScalarValues::Real(1.0..3.0);
            attribute y1: ScalarValues::Real(1.0..3.0);
            //attribute z1: ScalarValues::Real(2.0..6.0) = x1 + y1;
            attribute r1: ScalarValues::Boolean = x1 <= y1;
            //attribute r2: ScalarValues::Boolean = x1 <= y1;
            //attribute rn: ScalarValues::Boolean = z1 > x1;
            """
        )
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
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
    fun issue243indexExplosionSmallModelUnrelatedVarsAADDTest() = testSession {
        loadSysMD(
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
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
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
    fun rangesNotConstrained() = testSession {
        loadSysMD("""
                import SI::*;
                feature x:  ScalarValues::Real;
                inv a1 { x >= 95.0 }
                inv a2 { x <= 96.0 }
        """.trimIndent())
        propagate()
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
        global.resolveVar("x")
    }

    @Test
    fun exponentiationIsRightAssociative() = testSession {
        loadSysMD("""
            feature x: ScalarValues::Real = 2.0 ^ 2.0 ^ 3.0;
        """.trimIndent())
        val x = global.resolve<Feature>("x")
        assertEquals(256.0, x!!.variable!!.min(), 0.0001)
    }

    @Test @Disabled //TODO: Problem in Parser: After or only Product possible, but EE is not in Product
    fun booleanExpression() = testSession {
        loadSysMD(
            input = """
            attribute c: ScalarValues::Integer = 1;
            attribute b: ScalarValues::Integer = 2;
            attribute a: ScalarValues::Boolean = c == 0 or b == 0.
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }
    @Test @Disabled
    fun iddTimesLoop() = testSession {
        loadSysMD(
            input = """
            package safety {
                calc def calcASIL{
                    in attribute severity : ScalarValues::Integer(0..3);
                    in attribute exposure : ScalarValues::Integer(0..4);
                    in attribute controllability: ScalarValues::Integer(0..3);
                    attribute sum: Integer = severity + exposure + controllability;
                    attribute sumAdapted : ScalarValues::Integer = if (severity == 0) or (controllability == 0) ? 0 else sum; //süecial case for S0 and C0 the ASIL is always QM (0)
                    return result: Integer = max(sum-6,0).      
                }
                attribute S: ScalarValues::Integer = 2;
                attribute E: ScalarValues::Integer = 4;
                attribute C: ScalarValues::Integer = 2;
                attribute ASIL: ScalarValues::Integer = calcASIL(S,E,C).     
            }
            """.trimIndent(), catchExceptions = true
        )
        propagate()
        assertEquals(0, status.exceptions.size, status.exceptions.toString())
    }

    /**
     * Everything seems OK; no duplicated features.
     * BUT: Supertypes are duplicated.
     */
    @Test
    fun referenceDuplicatesTestIssue260() = testSession("ScalarValues", "Parts", "Ports", "Requirements") {
        settings.catchExceptions = true
        loadSysMD("""
            import ScalarValues::*; 
            import SI::*; 
            part testPart {
                attribute att: Real [m];
                in port input;
            }
            
            requirement testReq {
                subject testRef references testPart;
            } 
        """)
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
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
        +"""
            package features {
                part def carFeature;
                part def accSystem :> carFeature {
                    part def avoidCollision :> carFeature; 
                }  
                // part def detectLongDistanceCollsision :> avoidCollision;
                // The following works: 
                part def detectLongDistanceCollsision :> accSystem::avoidCollision;
            }
        """
        assertTrue(status.exceptions.isEmpty(), status.exceptions.toString())
    }
}

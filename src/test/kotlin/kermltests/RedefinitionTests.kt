package kermltests

import com.github.tukcps.sysmd.model.kerml.Feature
import com.github.tukcps.sysmd.model.kerml.Multiplicity
import com.github.tukcps.sysmd.model.kerml.Type
import com.github.tukcps.sysmd.services.resolve.resolveVar
import util.assertNoIssues
import util.mockup.loadKerML
import util.mockup.loadSysMLv2
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RedefinitionTests {

    /**
     * A redefined feature should add a multiplicity and also refine its value.
     */
    @Test
    fun redefinitionTestBareRedefinition1() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything {
                 feature f[1..4]; 
            } 
                       
            type b :> a {
                :>> f: ScalarValues::Real; 
            }
        """)
        assertNoIssues()

        val bf: Feature? = global.resolve("b::f")?.member()
        val af: Feature? = global.resolve("a::f")?.member()

        assertNotNull(af)
        assertEquals(1L, af.multiplicityRange.min  )
        assertEquals(4L, af.multiplicityRange.max  )
        assertEquals(anything, af.generalization.first() )

        assertNotNull(bf)
        assertEquals(1L, bf.multiplicityRange.min)
        assertEquals(4L, bf.multiplicityRange.max)
        assertTrue(repo.realType in bf.generalization)
        assertTrue(af in bf.generalization)
    }


    @Test
    fun redefinitionTestBareRedefinition2() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything {
                 feature f: ScalarValues::Real[1 .. 4]; 
            } 
                       
            type b :> a {
                :>> f [2..3];   // Must be of type Real 
            }
        """)
        val af: Feature? = global.resolve("a::f")?.member()
        assertNotNull(af)
        assertEquals(1L, af.multiplicityRange.min  )
        assertEquals(4L, af.multiplicityRange.max  )
        assertTrue(repo.realType in af.generalization)

        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val bf = global.resolve("b::f")?.memberElement as Feature
        assertNotNull(bf)
        assertEquals(2L, bf.multiplicityRange.min )
        assertEquals(3L, bf.multiplicityRange.max  )
        assertTrue(repo.realType in bf.generalization )
    }

    @Test
    fun redefinitionTestBareRedefinition3() = testSession("ScalarValues", "ISQ") {
        loadSysMLv2("""
            attribute def Position {
                attribute x: ISQ::LengthValue [m]; 
                attribute y: ISQ::LengthValue [m]; 
                attribute z: ISQ::LengthValue [m];     
            }
            
            attribute p: Position { 
              redefines x = 1.0 [m];
              redefines y = 2.0 [m]; 
              redefines z = 1.5 [m]; 
            }
        """)
        solver.propagate()
        assertNoIssues()
    }

    /**
     * Basic test for re-definition.
     * - redefinition changes the multiplicity.
     * - redefinition changes the type
     * - The redefined feature is not changed.
     */
    @Test
    fun redefinitionTest1() = testSession("ScalarValues") {
        loadKerML("""
            type a :> Base::Anything {
                 feature f[1..4]; 
            } 
                       
            type b :> a {
                :>> f: ScalarValues::Real[2..3]; 
            }
        """)

        assertTrue(status.issues.isEmpty(), status.issues.toString())
        val bf = global.resolve("b::f")?.memberElement as Feature

        assertNotNull(bf)
        assertEquals(2L, bf.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.min()  )
        assertEquals(3L, bf.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.max()  )
        assertTrue(repo.realType in bf.typing.map { it.type } )

        val af = global.resolve("a::f")?.memberElement as Type?
        assertNotNull(af)
        assertEquals(1L, af.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.min()  )
        assertEquals(4L, af.ownedElement.filterIsInstance<Multiplicity>().first().variable!!.max()  )
        assertEquals(anything, af.generalization.first() )
    }


    @Test
    fun redefinitionTest()  = testSession("Occurrences") {
        loadKerML("""
            class QuantityPowerFactor {
                feature unit: ScalarValues::String;
                feature exponent: ScalarValues::Integer(-10..10);
            }
            class QuantityDimension {
                feature quantityPowerFactors: QuantityPowerFactor;
            }
            feature lengthPF: QuantityPowerFactor { 
                :>> unit = "m";  
                :>> exponent = 1;
            }
            feature massPF: QuantityPowerFactor { 
                :>> unit = "kg";  
                :>> exponent = 1;  
            }
            feature quantityDimension: QuantityDimension { 
                :>> quantityPowerFactors = lengthPF; 
            }
        """)
        solver.propagate()
        assertNoIssues()
        assertEquals(1L, global.resolveVar("quantityDimension::quantityPowerFactors::exponent")!!.max() )
        assertEquals("m", global.resolveVar("quantityDimension::quantityPowerFactors::unit")!!.vectorQuantity.value.asStrDD().toString())
        assertEquals("m", global.resolveVar("lengthPF::unit")!!.vectorQuantity.value.asStrDD().toString())
        assertEquals(1L, global.resolveVar("lengthPF::exponent")!!.min() )
    }

    // The types are not set as expected in addInheritedFeatures (the function should be right but not correctly called for the elements),
    // so that in "initialize" the values in the last row can be set (like in the last test).
    @Test
    fun nestedRedefinitionTest3()  = testSession("ScalarValues") {
        loadKerML("""
            datatype Old {
                feature a: ScalarValues::String default "old";
            }
            datatype OwnsOld {
                feature ownedOld: Old;
            }
            feature ownsOld: OwnsOld { :>> ownedOld = redefinedOld; } 
            feature redefinedOld: Old { :>> a = "new"; }
        """)
        assertNoIssues()
        solver.propagate()
        // val ownsOld = global.resolve<Feature>("ownsOld")
        // val redefinedOld = global.resolve<Feature>("redefinedOld")
        // val new = global.resolveVar("ownsOld::ownedOld::a")!!.ast!!.evalUpRec()
        assertEquals("new", global.resolveVar("ownsOld::ownedOld::a")!!.vectorQuantity.value.asStrDD().toString())
    }
}
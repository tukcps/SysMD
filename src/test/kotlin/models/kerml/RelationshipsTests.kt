package models.kerml

import com.github.tukcps.sysmd.model.datamodel.toElement
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.kerml.Redefinition
import com.github.tukcps.sysmd.model.kerml.ReferenceSubsetting
import com.github.tukcps.sysmd.model.kerml.implementation.*
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipsTests {

    @Test
    fun assocTest() = testSession {
        val a = AssociationImplementation(this, declaredName="a")
        addOwnedMember(a, global)
        // println(a)
    }

    @Test
    fun connectorTest() = testSession {
        val c = ConnectorImplementation(this)
        val cDAO = c.toElementData()
        val cFromDao = cDAO.toElement(this)
        assertTrue(cFromDao is ConnectorImplementation)
    }

    @Test
    fun redefinitionTest() = testSession {
        val f1 = FeatureImplementation(this, declaredName = "f1")
        val f2 = FeatureImplementation(this, declaredName = "f2")
        val r = RedefinitionImplementation(this, redefiningFeature = f1, redefinedFeature = f2)
        val dao = r.toElementData()
        val rFromDao = dao.toElement(this) as Redefinition
        assertEquals(1, rFromDao.source.size)
        assertEquals(1, rFromDao.target.size)
    }

    @Test
    fun referenceTest() = testSession {
        val f1 = FeatureImplementation(this, declaredName = "f1")
        val f2 = FeatureImplementation(this, declaredName = "f2")
        val r = ReferenceSubsettingImplementation(this, referencingFeature = f1, referencedFeature = f2 )
        val dao = r.toElementData()
        val rFromDao = dao.toElement(this) as ReferenceSubsetting
        assertEquals(1, rFromDao.source.size)
        assertEquals(1, rFromDao.target.size)
    }
}
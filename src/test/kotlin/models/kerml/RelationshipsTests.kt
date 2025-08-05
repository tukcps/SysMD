package models.kerml

import com.github.tukcps.sysmd.model.kerml.Redefinition
import com.github.tukcps.sysmd.model.kerml.ReferenceSubsetting
import com.github.tukcps.sysmd.model.kerml.implementation.*
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import com.github.tukcps.sysmd.services.repositories.local.toElement
import util.testSession
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipsTests {

    @Test
    fun assocTest() = testSession {
        val a = AssociationImplementation(declaredName="a")
        addOwnedMember(a, global)
        // println(a)
    }

    @Test
    fun connectorTest() = testSession {
        val c = ConnectorImplementation().also { it.elementId = UUID.randomUUID() }
        c.model=this
        val cDAO = c.toDAO()
        val cFromDao = cDAO.toElement()
        assertTrue(cFromDao is ConnectorImplementation)
    }

    @Test
    fun redefinitionTest() = testSession {
        val f1 = FeatureImplementation(declaredName = "f1").also{ it.elementId = UUID.randomUUID() }
        val f2 = FeatureImplementation(declaredName = "f2").also{ it.elementId = UUID.randomUUID() }
        val r = RedefinitionImplementation(redefiningFeature = f1, redefinedFeature = f2)
            .also{it.elementId = UUID.randomUUID() }
        r.model = this
        val dao = r.toDAO()
        val rFromDao = dao.toElement() as Redefinition
        assertEquals(1, rFromDao.source.size)
        assertEquals(1, rFromDao.target.size)
    }

    @Test
    fun referenceTest() = testSession {
        val f1 = FeatureImplementation(declaredName = "f1").also{ it.elementId = UUID.randomUUID() }
        val f2 = FeatureImplementation(declaredName = "f2").also{ it.elementId = UUID.randomUUID() }
        val r = ReferenceSubsettingImplementation( referencingFeature = f1, referencedFeature = f2 )
            .also{it.elementId = UUID.randomUUID() }
        r.model = this
        val dao = r.toDAO()
        val rFromDao = dao.toElement() as ReferenceSubsetting
        assertEquals(1, rFromDao.source.size)
        assertEquals(1, rFromDao.target.size)
    }
}
package models.sysmlv2

import com.github.tukcps.sysmd.model.datamodel.toElement
import com.github.tukcps.sysmd.model.datamodel.toElementData
import com.github.tukcps.sysmd.model.sysml.implementation.*
import util.testSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializeTests {

    @Test
    fun serializePartUsage() = testSession {
        val element = PartUsageImplementation(
            this,
            declaredName = "name",
            declaredShortName = "shortName"
        )
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PartUsage", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PartUsageImplementation)
    }

    @Test
    fun serializePartDefinition() = testSession {
        val element = PartDefinitionImplementation(this).also {
            it.declaredName = "name"
            it.declaredShortName = "shortName"
        }
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PartDefinition", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PartDefinitionImplementation)
    }

    @Test
    fun serializePortDefinition() = testSession {
        val element = PortDefinitionImplementation(this).also {
            it.declaredName = "name"
            it.declaredShortName = "shortName"
        }
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PortDefinition", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PortDefinitionImplementation)
    }


    @Test
    fun serializePortUsage() = testSession {
        val element = PortUsageImplementation(
            this,
            declaredName = "name",
            declaredShortName = "shortName"
        )
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PortUsage", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PortUsageImplementation)
    }


    @Test
    fun serializeRequirementUsage() = testSession {
        val element = RequirementUsageImplementation(
            this,
            declaredName = "name",
            declaredShortName = "shortName"
        )
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("RequirementUsage", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is RequirementUsageImplementation)
    }



    @Test
    fun serializeConnectionDefinition() = testSession {
        val element = ConnectionDefinitionImplementation(
            this,
            declaredName = "name",
            declaredShortName = "shortName"
        )
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("ConnectionDefinition", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is ConnectionDefinitionImplementation)
    }


    @Test
    fun serializeConnectionUsage() = testSession {
        val element = ConnectionUsageImplementation(this).also {
            it.declaredName = "name"
            it.declaredShortName = "shortName"
        }
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("ConnectionUsage", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is ConnectionUsageImplementation)
    }

    @Test
    fun serializeInterfaceDefinition() = testSession {
        val element = InterfaceDefinitionImplementation(this).also {
            it.declaredName = "name"
            it.declaredShortName = "shortName"
        }
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("InterfaceDefinition", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is InterfaceDefinitionImplementation)
    }


    @Test
    fun serializeInterfaceUsage() = testSession {
        val element = InterfaceUsageImplementation(this).also {
            it.declaredName = "name"
            it.declaredShortName = "shortName"
        }
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("InterfaceUsage", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is InterfaceUsageImplementation)
    }


    @Test
    fun serializeAllocationDefinition() = testSession {
        val element = AllocationDefinitionImplementation(
            this,
            declaredName = "name",
            declaredShortName = "shortName"
        )
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("AllocationDefinition", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is AllocationDefinitionImplementation)
    }


    @Test
    fun serializeAllocationUsage() = testSession {
        val element = AllocationUsageImplementation(this).also {
            it.declaredName = "name"
            it.declaredShortName = "shortName"
        }
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("AllocationUsage", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is AllocationUsageImplementation)
    }

    @Test
    fun serializeCalculationDefinition() = testSession {
        val element = CalculationDefinitionImplementation(this).also {
            it.declaredName = "name"
            it.declaredShortName = "shortName"
        }
        val uuid = element.elementId
        val serializedElement = element.toElementData()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("CalculationDefinition", serializedElement.type.name)
        val deserializedElement = serializedElement.toElement(this)
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is CalculationDefinitionImplementation)
    }
}
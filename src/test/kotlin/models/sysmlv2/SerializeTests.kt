package models.sysmlv2

import com.github.tukcps.sysmd.model.sysml.implementation.CalculationDefinitionImplementation
import com.github.tukcps.sysmd.model.sysml.implementation.*
import com.github.tukcps.sysmd.services.repositories.local.toDAO
import com.github.tukcps.sysmd.services.repositories.local.toElement
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializeTests {

    private val uuid = UUID.randomUUID()

    @Test
    fun serializePartUsage() {
        val element = PartUsageImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PartUsage", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PartUsageImplementation)
    }

    @Test
    fun serializePartDefinition() {
        val element = PartDefinitionImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PartDefinition", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PartDefinitionImplementation)
    }

    @Test
    fun serializePortDefinition() {
        val element = PortDefinitionImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PortDefinition", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PortDefinitionImplementation)
    }


    @Test
    fun serializePortUsage() {
        val element = PortUsageImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("PortUsage", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is PortUsageImplementation)
    }


    @Test
    fun serializeRequirementUsage() {
        val element = RequirementUsageImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("RequirementUsage", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is RequirementUsageImplementation)
    }



    @Test
    fun serializeConnectionDefinition() {
        val element = ConnectionDefinitionImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("ConnectionDefinition", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is ConnectionDefinitionImplementation)
    }


    @Test
    fun serializeConnectionUsage() {
        val element = ConnectionUsageImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("ConnectionUsage", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is ConnectionUsageImplementation)
    }

    @Test
    fun serializeInterfaceDefinition() {
        val element = InterfaceDefinitionImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("InterfaceDefinition", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is InterfaceDefinitionImplementation)
    }


    @Test
    fun serializeInterfaceUsage() {
        val element = InterfaceUsageImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("InterfaceUsage", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is InterfaceUsageImplementation)
    }


    @Test
    fun serializeAllocationDefinition() {
        val element = AllocationDefinitionImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("AllocationDefinition", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is AllocationDefinitionImplementation)
    }


    @Test
    fun serializeAllocationUsage() {
        val element = AllocationUsageImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("AllocationUsage", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is AllocationUsageImplementation)
    }



    @Test
    fun serializeCalculationDefinition() {
        val element = CalculationDefinitionImplementation(
            declaredName = "name",
            declaredShortName = "shortName"
        ).also { it.elementId = uuid }
        val serializedElement = element.toDAO()
        assertEquals(uuid, serializedElement.elementId)
        assertEquals("name", serializedElement.declaredName)
        assertEquals("shortName", serializedElement.declaredShortName)
        assertEquals("CalculationDefinition", serializedElement.type)
        val deserializedElement = serializedElement.toElement()
        assertEquals(uuid, deserializedElement.elementId)
        assertEquals("name", deserializedElement.declaredName)
        assertEquals("shortName", deserializedElement.declaredShortName)
        assertTrue(deserializedElement is CalculationDefinitionImplementation)
    }
}
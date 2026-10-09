package api

import com.github.tukcps.sysmd.model.datamodel.ElementReference
import com.github.tukcps.sysmd.model.datamodel.ElementReference.ByID
import com.github.tukcps.sysmd.model.datamodel.ElementReference.ByName
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class ElementReferenceSerializationTests
{
    private inline fun<reified T : ElementReference> checkJson(ref : T, json : String)
    {
        assertEquals(json, Json.encodeToString<T>(ref))
        assertEquals(json, Json.encodeToString<ElementReference>(ref))

        assertEquals(ref, Json.decodeFromString<T>(json))
        assertEquals(ref, Json.decodeFromString<ElementReference>(json))
    }

    @Test
    fun namedReference()
    {
        checkJson(ByName("foobar"), """{"name":"foobar"}""")
    }

    @Test
    fun idReference()
    {
        val ref = ByID(Uuid.random())
        val json = """{"@id":"${ref.id}"}"""
        checkJson(ref, json)
    }

    @Test
    fun rootReference()
    {
        checkJson(ElementReference.ToRoot, "{}")
    }

    /** Normal fields may be duplicated, for parity with automatically generated serializers */
    @Test
    fun duplicateID()
    {
        val ref = ByID(Uuid.random())
        @Suppress("JsonDuplicatePropertyKeys")
        val json = """{ "@id": "${Uuid.random()}", "@id": "${ref.id}" }"""
        assertEquals(ref, Json.decodeFromString<ByID>(json))
        assertEquals(ref, Json.decodeFromString<ElementReference>(json))
    }

    @Test
    fun duplicateName()
    {
        val ref = ByName("bar")
        @Suppress("JsonDuplicatePropertyKeys")
        val json = """{ "name": "foo", "name": "${ref.name}" }"""
        assertEquals(ref, Json.decodeFromString<ByName>(json))
        assertEquals(ref, Json.decodeFromString<ElementReference>(json))
    }
}
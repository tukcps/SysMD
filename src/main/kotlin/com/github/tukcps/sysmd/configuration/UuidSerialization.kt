package com.github.tukcps.sysmd.configuration


import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.github.tukcps.sysmd.services.util.JsonSupport
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.http.converter.HttpMessageConverters
import org.springframework.http.converter.json.KotlinSerializationJsonHttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.uuid.Uuid

@Configuration
class KotlinSerializationWebConfig : WebMvcConfigurer {

    /**
     * Configures message converters using the new ServerBuilder pattern.
     * This is the modern, non-deprecated way in Spring Boot 4.0+.
     */
    override fun configureMessageConverters(builder: HttpMessageConverters.ServerBuilder) {
        // Adds the Kotlinx serializer natively to the server configuration pipeline
        builder.addCustomConverter(KotlinSerializationJsonHttpMessageConverter(JsonSupport.json))
    }

    /**
     * Registers a global converter to automatically parse kotlin.uuid.Uuid
     * from String values in @PathVariable and @RequestParam.
     */
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(String::class.java, Uuid::class.java, Converter<String, Uuid> { source ->
            // Wandelt den URL-String sicher in das native Kotlin-Uuid-Objekt um
            Uuid.parse(source)
        })
    }
}


@Configuration
class JacksonKotlinUuidConfig {

    companion object {
        fun createModule(): SimpleModule {
            val module = SimpleModule("KotlinUuidModule")

            // 1. Dem Serializer beibringen, Uuid zu String zu machen
            module.addSerializer(Uuid::class.java, object : JsonSerializer<Uuid>() {
                override fun serialize(value: Uuid, gen: JsonGenerator, serializers: SerializerProvider) {
                    gen.writeString(value.toString())
                }
            })

            // 2. Dem Deserializer beibringen, aus dem String oder legacy Object wieder eine Uuid zu parsen
            module.addDeserializer(Uuid::class.java, object : JsonDeserializer<Uuid>() {
                override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Uuid {
                    if (p.currentToken == com.fasterxml.jackson.core.JsonToken.START_OBJECT) {
                        val node: com.fasterxml.jackson.databind.JsonNode = p.codec.readTree(p)
                        val msb = node.get("mostSignificantBits")?.asLong()
                        val lsb = node.get("leastSignificantBits")?.asLong()
                        if (msb != null && lsb != null) {
                            return Uuid.fromLongs(msb, lsb)
                        }
                        if (node.isTextual) {
                            return Uuid.parse(node.asText())
                        }
                    }
                    return Uuid.parse(p.text)
                }
            })

            return module
        }
    }

    @Bean
    fun kotlinUuidModule(): SimpleModule = createModule()
}
package com.github.tukcps.sysmd.imports

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File


object JsonImporter {

    /**
     * Imports a Json file and extracts the:
     * - Modules
     *      -> their attributes
     *      -> and the corresponding old and new values
     *
     * Afterward the values are displayed in the corresponding cell
     */
    fun importJson(filePath : String) : MutableList<Result>?{

        try {
            val mapper = jacksonObjectMapper()
            mapper.registerKotlinModule()
            mapper.registerModule(JavaTimeModule())

            val jsonString: String = File(filePath).readText(Charsets.UTF_8)
            val jsonTextList: List<Result> = mapper.readValue<List<Result>>(jsonString)

            val importedResults = mutableListOf<Result>()

            for (res in jsonTextList) {
                importedResults.add(
                    Result(
                        constraintName = res.constraintName,
                        resultValue = res.resultValue,
                        resultUnit = res.resultUnit,
                        referenceValue = res.referenceValue,
                        referenceUnit = res.referenceUnit,
                        successful = res.successful,
                        attributeQualifiedName = res.attributeQualifiedName
                    )
                )
            }

            return importedResults
        }catch (e : Exception){
            println(e.message)
        }

        return null
    }
}
package com.github.tukcps.sysmd.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Provides the configuration for open api: REST API documentation.
 * @link : http://localhost:8080/api-docs
 * @link FOR SWAGGER UI: http://localhost:8080/swagger-ui.html
 *
 * @author Khushnood Adil Rafique, Christoph Grimm
 */
@Configuration
open class OpenAPIConfig {
    // The tags seem to be considered only partially. No idea, why some work, others not.
    @Bean
    open fun sysMDOpenAPI(): OpenAPI {
        return OpenAPI().components(Components())
            .info(
                Info().title("SysMD REST API")
                    .description("REST API for web clients.")
                    .version("v4.0 +")
                    .license(License().name("(c) TU Kaiserslautern, Department of Cyber-Physical Systems"))
            )        .tags(
                listOf(
                    Tag().name(SESSION_RESOURCE).description("The Session Controller is for the administration of sessions."),
                    Tag().name(PROJECT_RESOURCE).description("CRUD operations on Projects"),
                    Tag().name(ELEMENT_RESOURCE).description("Getting the Elements of Project's Commits"),
                )
            )
            .externalDocs(
                ExternalDocumentation().description("SysMD Wiki Documentation").url("https://put-sysmd-link-here")
            )
    }

    companion object {
        const val SESSION_RESOURCE: String = "Sessions"
        const val PROJECT_RESOURCE: String = "Projects"
        const val ELEMENT_RESOURCE: String = "Elements"
    }
}
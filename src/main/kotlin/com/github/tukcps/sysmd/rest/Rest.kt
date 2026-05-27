package com.github.tukcps.sysmd.rest

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tukcps.sysmd.exceptions.SysMDInfo
import com.github.tukcps.sysmd.settings
import org.springframework.http.*
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange


object Rest {
    private var protocol = "https://"

    private var auth: String = "none"
    private val objectMapper = ObjectMapper()

    /**
     * @SpringBootTest registers a TestRestTemplate bean so we can directly @Autowire
     */
    private val restTemplate: RestTemplate = RestTemplate()

    /**
     * Generates URI from a Map with multiple
     */
    fun generateURI(endpoint: String, queryParameters : Map<String, String>? = null): String {
        // if no query parameters are given, return plain endpoint URI
        var queryParameterString = ""
        if (queryParameters != null) {
            val entries: List<String> = queryParameters.entries.map { "${it.key}=${it.value}" }
            queryParameterString = entries.joinToString(prefix="?", separator = "&")
        }

        protocol = if(settings.rest.port=="443") {
            "https://"
        } else {
            "http://"
        }

        return "$protocol${settings.rest.baseURI}:${settings.rest.port}${settings.rest.entryURI}$endpoint$queryParameterString"
    }

    private fun generateHttpHeaders(withAuthToken: Boolean = true, sessionId: String?, branchId:String?=null): HttpHeaders {
        // set headers to accept and provide JSON data. also include the bearer auth token
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = mutableListOf(MediaType.APPLICATION_JSON)
            if (withAuthToken)
                setBearerAuth(auth)
            if (!sessionId.isNullOrEmpty()) {
                set("SessionId", sessionId)
            }
            if (branchId != null) {
                set("branchId", branchId)
            }
        }
    }

    private fun generateHttpHeadersText(withAuthToken: Boolean = true, sessionId: String?): HttpHeaders {
        return HttpHeaders().apply {
            // Set headers to provide plain text data, but still accept JSON/anything in return
            contentType = MediaType.TEXT_PLAIN
            accept = mutableListOf(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)

            if (withAuthToken) {
                setBearerAuth(auth)
            }
            if (!sessionId.isNullOrEmpty()) {
                set("SessionId", sessionId)
            }
        }
    }

    /**
     * Generate request with JSON payload.
     */
    @JvmOverloads
    fun requestToEndpoint(endpoint: String, payload: String?, method: HttpMethod, queryParameters : Map<String, String>? = null, withAuthToken: Boolean = true, sessionId: String?, branchId: String? = null): ResponseEntity<String> {
        //generate the default header information: ContentType and accepted response are JSON. Bearer auth token is optional
        val headers = generateHttpHeaders(withAuthToken, sessionId = sessionId, branchId = branchId)

        // formulate the request with the default header and given payload
        val request = HttpEntity(payload, headers)
        return try {
            restTemplate.exchange<String>(generateURI(endpoint, queryParameters), method, request)
        } catch (error: RestClientException) {
            ResponseEntity("Unknown error in request: ${error.message}", HttpStatus.BAD_GATEWAY)
        }
    }

    /**
     * Generate request with plain text payload.
     */
    fun requestToEndpointText(endpoint: String, payload: String, method: HttpMethod, withAuthToken: Boolean = true, sessionId: String?): ResponseEntity<String> {
        val headers = generateHttpHeadersText(withAuthToken, sessionId = sessionId)

        // formulate the request with the default header and given payload
        val request = HttpEntity(payload, headers)
        return try {
            restTemplate.exchange<String>(generateURI(endpoint), method, request)
        } catch (error: RestClientException) {
            ResponseEntity("Unknown error in request: ${error.message}", HttpStatus.BAD_GATEWAY)
        }
    }

    fun get(endpoint: String, queryParameters : Map<String, String>? = null, sessionId: String?): ResponseEntity<String> {
        return this.requestToEndpoint(endpoint, null as String?, HttpMethod.GET, queryParameters, true, sessionId)
    }

    fun get(endpoint: String, sessionId: String?): ResponseEntity<String> {
        return this.requestToEndpoint(endpoint, null as String?, HttpMethod.GET, null, true, sessionId)
    }

    fun post(endpoint: String, payload: String?, queryParameters : Map<String, String>? = null, sessionId: String?, branchId: String? = null): ResponseEntity<String> {
        return this.requestToEndpoint(endpoint, payload, HttpMethod.POST, queryParameters, true, sessionId, branchId)
    }

    fun post(endpoint: String, payload: String?, sessionId: String?, branchId: String? = null): ResponseEntity<String> {
        return this.post(endpoint, payload, null, sessionId, branchId)
    }

    fun postText(endpoint: String, text: String, sessionId: String?): ResponseEntity<String> {
        return this.requestToEndpointText(endpoint, text, HttpMethod.POST, true, sessionId)
    }

    fun put(endpoint: String, payload: String, queryParameters : Map<String, String>? = null, sessionId: String?): ResponseEntity<String> {
        return this.requestToEndpoint(endpoint, payload, HttpMethod.PUT, queryParameters, true, sessionId)
    }

    fun delete(endpoint: String, payload: String, queryParameters : Map<String, String>? = null, sessionId: String?): ResponseEntity<String> {
        return this.requestToEndpoint(endpoint, payload, HttpMethod.DELETE, queryParameters, true, sessionId)
    }

    fun delete(endpoint: String, payload: String, sessionId: String?): ResponseEntity<String> {
        return this.delete(endpoint, payload, null, sessionId)
    }

    fun extractKeyFromBody(key: String, body: String?): String? {
        try {
            val root = objectMapper.readTree(body)
            return root[key].asText()
        } catch (_: JsonProcessingException) {
            println("Key $key not found in body.")
        }
        return null
    }

    fun extractEntityIdFromBody(body: String?): String? {
        return this.extractKeyFromBody("@id", body)
    }

    fun extractEntityUUIDFromBody(body: String?):String? {
        return this.extractKeyFromBody("entityId",body)
    }

    fun login(endpoint: String,
              userKey: String, user: String,
              passwordKey: String, password: String)
    {
        val loginJsonObject = """{ "$userKey": "$user", "$passwordKey": "$password" }"""

        val loginResult = requestToEndpoint(endpoint, loginJsonObject, HttpMethod.POST, withAuthToken=false, sessionId = null)

        // check if log in was successful
        // set the bearer token, as we will need that for the following requests
        try {
            // remove the "Bearer: "
            auth = loginResult.headers["authorization"].toString().substring(7)
            // remove the closing bracket
            auth = auth.substring(0, auth.length - 1)
        } catch (_: NullPointerException) {
            throw SysMDInfo("login to backend failed")
        } catch (_: Exception) {
            throw SysMDInfo( "backend could not be reached")
        }
    }
}

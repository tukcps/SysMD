package api

import com.github.tukcps.sysmd.rest.Rest
import com.github.tukcps.sysmd.settings
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled

/**
 * These tests require a running Agila Backend.
 * They should be validated manually to see if the data is transferred to the database.
 */
@Disabled // Only enable if Agila backend is running.
class UploadTest {

    private val user = "admin@cps.de"
    private val password = "admin"
    private val path = "src/test/resources/"

    @BeforeEach
    fun setUp() {
        try {
            settings.rest.entryURI="http://localhost"
            settings.rest.baseURI="/agila-server"
            settings.rest.port="8080"
            Rest.login("/users/login", "email", user, "password", password)
            println("   Agila SysMD: Login as $user succeeded.")
        } catch (exception: Exception) {
            println("   Agila SysMD: Login failed. Check if backend is online and login credentials are ok.")
            println("Backend not reachable")
        }
    }

}

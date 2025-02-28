package fi.metatavu.example.api.test.functional.tests

import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import fi.metatavu.example.api.test.functional.resources.LocalTestProfile
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.TestProfile
import org.junit.jupiter.api.Test

/**
 * Tests for system resources
 *
 * @author Antti Leppä
 */
@QuarkusTest
@TestProfile(LocalTestProfile::class)
class SystemResourceTest {

    @Test
    fun testPingEndpoint() {
        given()
            .contentType("application/json")
            .`when`().get("/v1/system/ping")
            .then()
            .statusCode(200)
            .body(`is`("pong"))
    }

}
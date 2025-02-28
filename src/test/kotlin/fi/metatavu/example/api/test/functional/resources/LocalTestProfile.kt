package fi.metatavu.example.api.test.functional.resources

import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Local Quarkus test profile
 */
class LocalTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): Map<String, String> {
        return mapOf(
            "quarkus.keycloak.devservices.realm-name" to "example",
            "quarkus.keycloak.devservices.enabled" to "true",
            "quarkus.keycloak.devservices.realm-path" to "kc.json"
        )
    }

}
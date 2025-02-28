package fi.metatavu.example.api.test.functional.tests

import fi.metatavu.example.api.test.functional.resources.LocalTestProfile
import io.quarkus.test.junit.NativeImageTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for system resources
 *
 * @author Antti Leppä
 */
@NativeImageTest
@TestProfile(LocalTestProfile::class)
class NativeSystemResourceTest: SystemResourceTest() {

}

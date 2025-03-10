package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for paging policy contacts API
 */
@QuarkusIntegrationTest
@TestProfile(DefaultTestProfile::class)
class NativePagingPolicyContactTestsIT: PagingPolicyContactTestsIT()

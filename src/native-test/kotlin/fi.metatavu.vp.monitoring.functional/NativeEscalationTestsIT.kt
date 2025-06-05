package fi.metatavu.vp.monitoring.functional

import fi.metatavu.vp.deliveryinfo.spec.ThermometersApi
import fi.metatavu.vp.monitoring.functional.resources.*
import fi.metatavu.vp.monitoring.functional.settings.DefaultTestProfile
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusIntegrationTest
import io.quarkus.test.junit.TestProfile

/**
 * Native tests for policy escalations
 */
@QuarkusIntegrationTest
@QuarkusTestResource.List(
    QuarkusTestResource(MailgunTestResource::class),
    QuarkusTestResource(SitesTestResource::class),
    QuarkusTestResource(TerminalThermometersTestResource::class),
    QuarkusTestResource(TruckOrTowablelThermometersTestResource::class),
    QuarkusTestResource(TrucksTestResource::class),
    QuarkusTestResource(TowablesTestResource::class),
)
@TestProfile(DefaultTestProfile::class)
class NativeEscalationTestsIT: EscalationTestsIT()

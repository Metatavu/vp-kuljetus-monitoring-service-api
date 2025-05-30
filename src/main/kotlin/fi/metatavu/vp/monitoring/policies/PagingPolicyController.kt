package fi.metatavu.vp.monitoring.policies

import fi.metatavu.vp.api.model.PagingPolicyType
import fi.metatavu.vp.deliveryinfo.model.Site
import fi.metatavu.vp.deliveryinfo.model.TerminalThermometer
import fi.metatavu.vp.deliveryinfo.spec.SitesApi
import fi.metatavu.vp.monitoring.email.EmailController
import fi.metatavu.vp.monitoring.incidents.ThermalMonitorIncidentEntity
import fi.metatavu.vp.monitoring.incidents.pagedpolicies.PagedPolicyRepository
import fi.metatavu.vp.monitoring.monitors.ThermalMonitorEntity
import fi.metatavu.vp.monitoring.policies.contacts.PagingPolicyContactEntity
import fi.metatavu.vp.usermanagement.model.Driver
import fi.metatavu.vp.vehiclemanagement.model.TruckOrTowableThermometer
import fi.metatavu.vp.vehiclemanagement.spec.TowablesApi
import fi.metatavu.vp.vehiclemanagement.spec.TrucksApi
import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.eclipse.microprofile.rest.client.inject.RestClient
import java.time.OffsetDateTime
import java.util.*


@ApplicationScoped
class PagingPolicyController {

    @Inject
    lateinit var pagingPolicyRepository: PagingPolicyRepository

    @Inject
    lateinit var pagedPolicyRepository: PagedPolicyRepository

    @Inject
    lateinit var emailController: EmailController

    @ConfigProperty(name = "vp.monitoring.incidents.sensorlost.delayminutes")
    lateinit var sensorLostDelayMinutes: String

    @RestClient
    lateinit var trucksApi: TrucksApi

    @RestClient
    lateinit var towablesApi: TowablesApi

    @RestClient
    lateinit var sitesApi: SitesApi

    @RestClient
    lateinit var terminalThermometersApi: fi.metatavu.vp.deliveryinfo.spec.ThermometersApi

    @RestClient
    lateinit var vehicleThermometersApi: fi.metatavu.vp.vehiclemanagement.spec.ThermometersApi

    /**
     * Save a thermal monitor paging policy to the database
     *
     * @param priority
     * @param escalationSeconds
     * @param pagingPolicyContact
     * @param thermalMonitor
     * @param creatorId
     */
    suspend fun create(
        priority: Int,
        escalationSeconds: Int,
        pagingPolicyContact: PagingPolicyContactEntity,
        thermalMonitor: ThermalMonitorEntity,
        creatorId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        return pagingPolicyRepository.create(
            priority = priority,
            escalationSeconds = escalationSeconds,
            pagingPolicyContact = pagingPolicyContact,
            thermalMonitor = thermalMonitor,
            creatorId = creatorId
        )
    }

    /**
     * Retrieve policy by id if exists
     *
     * @param id
     */
    suspend fun find(id: UUID): ThermalMonitorPagingPolicyEntity? {
        return pagingPolicyRepository.findByIdSuspending(id)
    }

    /**
     * Delete policy from the database
     *
     * @param thermalMonitorPagingPolicyEntity
     */
    suspend fun delete(thermalMonitorPagingPolicyEntity: ThermalMonitorPagingPolicyEntity) {
        pagedPolicyRepository.listByPolicy(policy = thermalMonitorPagingPolicyEntity).forEach { pagedPolicyRepository.deleteSuspending(it) }
        pagingPolicyRepository.deleteSuspending(thermalMonitorPagingPolicyEntity)
    }

    /**
     * Delete all policies from the database that belong to a given contact
     *
     * @param pagingPolicyContact
     */
    suspend fun deletePoliciesByContact(pagingPolicyContact: PagingPolicyContactEntity) {
        pagingPolicyRepository.listAllByContact(pagingPolicyContact).forEach { delete(it) }
    }

    /**
     * Delete all policies from the database that belong to a given monitor
     *
     * @param thermalMonitor
     */
    suspend fun deletePoliciesByMonitor(thermalMonitor: ThermalMonitorEntity) {
        pagingPolicyRepository.listAllByMonitor(thermalMonitor).forEach { delete(it) }
    }

    /**
     * List policies in the database
     *
     * @param thermalMonitor
     * @param first
     * @param max
     */
    suspend fun list(
        thermalMonitor: ThermalMonitorEntity,
        first: Int? = null,
        max: Int? = null
    ): List<ThermalMonitorPagingPolicyEntity> {
        return pagingPolicyRepository.list(thermalMonitor = thermalMonitor, first = first, max = max).first
    }

    /**
     * Update policy data in the database
     *
     * @param entityToUpdate
     * @param priority
     * @param escalationDelaySeconds
     * @param pagingPolicyContact
     * @param modifierId
     */
    suspend fun update(
        entityToUpdate: ThermalMonitorPagingPolicyEntity,
        priority: Int,
        escalationDelaySeconds: Int,
        pagingPolicyContact: PagingPolicyContactEntity,
        modifierId: UUID
    ): ThermalMonitorPagingPolicyEntity {
        return pagingPolicyRepository.update(
            entityToUpdate = entityToUpdate,
            priority = priority,
            escalationDelaySeconds = escalationDelaySeconds,
            pagingPolicyContact = pagingPolicyContact,
            modifierId = modifierId
        )
    }

    /**
     * Trigger the next policy for a given incident if policy's time is due
     *
     * @param incident
     */
    suspend fun triggerNextPolicy(incident: ThermalMonitorIncidentEntity) {
        val alreadyTriggeredPolicies = pagedPolicyRepository.listByIncident(incident)

        val policies = pagingPolicyRepository
            .list(thermalMonitor = incident.thermalMonitor)
            .first

        if (policies.size == alreadyTriggeredPolicies.size) {
            return
        }

        val nextPolicy = policies[alreadyTriggeredPolicies.size]

        val now = OffsetDateTime.now()

        val trigger = if (alreadyTriggeredPolicies.isEmpty()) {
            incident.triggeredAt.isBefore(now.minusSeconds(nextPolicy.escalationDelaySeconds!!.toLong()))
        } else {
            val previousPolicy = alreadyTriggeredPolicies.first()
            previousPolicy.createdAt.isBefore(now.minusSeconds(nextPolicy.escalationDelaySeconds!!.toLong()))
        }

        if (trigger) {
            when (PagingPolicyType.valueOf(nextPolicy.pagingPolicyContact.contactType)) {
                PagingPolicyType.EMAIL -> {
                    val receiverEmail = nextPolicy.pagingPolicyContact.contact
                    if (receiverEmail != null) {
                        val subject = "LÄMPÖTILAHÄLYTYS"
                        val content = constructMessage(incident)

                        emailController.sendEmail(
                            to = receiverEmail,
                            subject = subject,
                            content = content
                        )
                    }

                }
            }
            pagedPolicyRepository.create(incident, nextPolicy)
        }
    }

    /**
     * Construct a message based on the type of the incident
     * This message will be sent to a policy contact
     */
    suspend fun constructMessage(incident: ThermalMonitorIncidentEntity): String {
        val temperature = incident.temperature
        val thresholdLow = incident.thresholdLow
        val thresholdHigh = incident.thresholdHigh

        val reason = if (temperature == null) {
            "Lämpötila ei päivittynyt määräajassa. Järjestelmässä asetettu raja on $sensorLostDelayMinutes minuuttia."
        } else if (thresholdLow != null && temperature < thresholdLow) {
            "Lämpötila on $temperature °C, joka on alhaisempi kuin asetettu alaraja: $thresholdLow °C"
        } else if (thresholdHigh != null && temperature > thresholdHigh) {
            "Lämpötila on $temperature °C, joka on korkeampi kuin asetettu yläraja: $thresholdHigh °C"
        } else {
            ""
        }

        val (incidentTargetName, incidentThermometerName) = fetchThermometerExternalInformation(incident.monitorThermometer.thermometerId)

        val message = "HÄLYTYKSEN TIEDOT\n\n"
            .plus("KOHDE: $incidentTargetName\n")
            .plus("VAHTI: ${incident.thermalMonitor.name}\n")
            .plus("ANTURI: $incidentThermometerName\n")
            .plus("SYY: $reason\n")
            .plus("AIKA: ${incident.triggeredAt}")

        return message
    }

    /**
     * Fetches the name of the incident target and the name of the incident thermometer.
     * Target can be a site, truck or towable.
     *
     * @param thermometerId
     */
    private suspend fun fetchThermometerExternalInformation(thermometerId: UUID): Pair<String, String> {
        val sitesList = sitesApi
            .listSites(thermometerId = thermometerId, first = null, max = null, archived = null)
            .awaitSuspending()
            .readEntity(Array<Site>::class.java)

        if (sitesList.isNotEmpty()) {
            val site = sitesList.first()
            val thermometerResponse = terminalThermometersApi
                .findTerminalThermometer(thermometerId = thermometerId)
                .awaitSuspending()

            if (thermometerResponse.status == 404) {
                return Pair(site.name, "[VIRHETILANNE: HÄLYTYKSEEN LIITETTYÄ ANTURIA EI LÖYTYNYT]")
            }

            val thermometer = thermometerResponse.readEntity(TerminalThermometer::class.java)
            return Pair(site.name, thermometer.name ?: thermometer.hardwareSensorId)
        }

        val trucksList = trucksApi
            .listTrucks(thermometerId = thermometerId, first = null, max = null, archived = null, plateNumber = null, sortBy = null, sortDirection = null)
            .awaitSuspending()
            .readEntity(Array<fi.metatavu.vp.vehiclemanagement.model.Truck>::class.java)

        if (trucksList.isNotEmpty()) {
            val truck = trucksList.first()
            val thermometerResponse = vehicleThermometersApi
                .findTruckOrTowableThermometer(thermometerId = thermometerId)
                .awaitSuspending()


            if (thermometerResponse.status == 404) {
                return Pair(truck.name ?: truck.vin, "[VIRHETILANNE: HÄLYTYKSEEN LIITETTYÄ ANTURIA EI LÖYTYNYT]")
            }

            val thermometer = thermometerResponse.readEntity(TruckOrTowableThermometer::class.java)

            return Pair(truck.name ?: truck.vin, thermometer.name ?: thermometer.macAddress)
        }

        val towablesList = towablesApi
            .listTowables(thermometerId = thermometerId, first = null, max = null, archived = null, plateNumber = null)
            .awaitSuspending()
            .readEntity(Array<fi.metatavu.vp.vehiclemanagement.model.Towable>::class.java)

        if (towablesList.isNotEmpty()) {
            val towable = towablesList.first()
            val thermometerResponse = vehicleThermometersApi
                .findTruckOrTowableThermometer(thermometerId = thermometerId)
                .awaitSuspending()


            if (thermometerResponse.status == 404) {
                return Pair(towable.name ?: towable.vin, "[VIRHETILANNE: HÄLYTYKSEEN LIITETTYÄ ANTURIA EI LÖYTYNYT]")
            }

            val thermometer = thermometerResponse.readEntity(TruckOrTowableThermometer::class.java)
            return Pair(towable.name ?: towable.vin, thermometer.name ?: thermometer.macAddress)
        }

        return Pair("[VIRHETILANNE: HÄLYTYKSEN KOHDETTA EI LÖYTYNYT]", "[VIRHETILANNE: HÄLYTYKSEEN LIITETTYÄ ANTURIA EI LÖYTYNYT]")
    }
}
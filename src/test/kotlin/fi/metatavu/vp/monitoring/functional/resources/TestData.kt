package fi.metatavu.vp.monitoring.functional.resources

import fi.metatavu.vp.deliveryinfo.model.Site
import fi.metatavu.vp.deliveryinfo.model.SiteType
import fi.metatavu.vp.deliveryinfo.model.TerminalThermometer
import fi.metatavu.vp.vehiclemanagement.model.Towable
import fi.metatavu.vp.vehiclemanagement.model.Truck
import fi.metatavu.vp.vehiclemanagement.model.TruckOrTowableThermometer
import java.util.*

class TestData {
    companion object {

        private val devices = listOf(
            "E9B4FCB2-7B3E-4AFF-B5A1-729E861D52BD"
        )
        val sites = listOf(
            Site(
                id = UUID.fromString("32E1A2C5-A0FD-4AFC-8AA5-D291CE5DD3BE"),
                name = "Test site",
                address = "Test address",
                location = "Test location",
                postalCode = "12345",
                deviceIds = devices,
                locality = "Mikkeli",
                siteType = SiteType.TERMINAL
            )
        )
        val terminalThermometers = listOf(
            TerminalThermometer(
                hardwareSensorId = "abc000",
                deviceIdentifier = "E9B4FCB2-7B3E-4AFF-B5A1-729E861D52BD",
                id = UUID.randomUUID(),
                siteId = sites.first().id!!,
                name = "target thermometer",
            )
        )
        val trucks = listOf(
            Truck(
                plateNumber = "0000",
                type = Truck.Type.TRUCK,
                vin = "1HGBH41JXMN109186",
                id = UUID.fromString("A1B2C3D4-E5F6-7A8B-9C0D-E1F2A3B4C5D6"),
                name = "Roadkiller"
            )
        )
        val towables = listOf(
            Towable(
                plateNumber = "1111",
                vin = "1HGBU41JXMN109187",
                type = Towable.Type.TRAILER,
                id = UUID.fromString("B2C3D4E5-F6F7-8B9C-0D1E-2F3A4B5C6D7E"),
                name = "Raahattava"
            )
        )
        val truckAndTowableThermometers =  listOf(
            TruckOrTowableThermometer(
                macAddress = "00:11:22:33:44:55",
                entityId = trucks.first().id!!,
                entityType = TruckOrTowableThermometer.EntityType.TRUCK,
                id = UUID.fromString("F3ECBA14-588E-441C-A340-1AF11DED7B9E"),
                name = "Truck thermometer 1"
            ),
            TruckOrTowableThermometer(
                macAddress = "66:77:88:99:AA:BB",
                entityId = towables.first().id!!,
                entityType = TruckOrTowableThermometer.EntityType.TOWABLE,
                id = UUID.fromString("F2533833-33D3-42BA-9A52-0AE6B6A44E30"),
                name = "Raahattava lämpömittari"
            )
        )
    }
}
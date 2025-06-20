package fi.metatavu.vp.monitoring.functional.settings

import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings.Companion.CRON_API_KEY
import io.quarkus.test.junit.QuarkusTestProfile

/**
 * Default test profile
 */
class DefaultTestProfile: QuarkusTestProfile {

    override fun getConfigOverrides(): MutableMap<String, String> {
        val config: MutableMap<String, String> = HashMap()
        config["vp.env"] = "TEST"
        config["mp.messaging.outgoing.vp-out.exchange.name"] = EXCHANGE_NAME
        config["mp.messaging.outgoing.vp-out.connector"] = "smallrye-rabbitmq"
        config["mp.messaging.incoming.vp-in.connector"] = "smallrye-rabbitmq"
        config["mp.messaging.incoming.vp-in.queue.name"] = "incoming_queue"
        config["mp.messaging.incoming.vp-in.queue.x-queue-type"] = "quorum"
        config["mp.messaging.incoming.vp-in.exchange.name"] = EXCHANGE_NAME
        config["mp.messaging.incoming.vp-in.routing-keys"] = "TEMPERATURE"
        config["vp.monitoring.cron.apiKey"] = CRON_API_KEY
        config["vp.monitoring.incidents.sensorlost.delayminutes"] = "5"

        return config
    }

    companion object {
        const val EXCHANGE_NAME = "test-exchange"
    }
}
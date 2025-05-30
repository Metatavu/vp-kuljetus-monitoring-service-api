package fi.metatavu.vp.monitoring.functional.settings

/**
 * Settings implementation for test builder
 *
 * @author Jari Nykänen
 * @author Antti Leppä
 */
class ApiTestSettings {

    companion object {

        /**
         * Returns API service base path
         */
        val apiBasePath: String
            get() = "http://localhost:8081"

        const val CRON_API_KEY = "cron-api-key"

        const val MAILGUN_DOMAIN = "example.com"
        const val MAILGUN_API_KEY = "apiKey"
        const val MAILGUN_API_URL_ENDING = "mgapi"
        const val MAILGUN_SENDER_EMAIL = "noreply@example.com"
    }
}
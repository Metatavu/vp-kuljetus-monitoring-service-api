package fi.metatavu.vp.monitoring.email

import io.vertx.core.MultiMap
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.client.WebClient
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class EmailController {
    @Inject
    @ConfigProperty(name = "fi.metatavu.vp.monitoring.mailgun.apikey")
    lateinit var apiKey: String

    @Inject
    @ConfigProperty(name = "fi.metatavu.vp.monitoring.mailgun.domain")
    lateinit var domain: String

    @Inject
    @ConfigProperty(name = "fi.metatavu.vp.monitoring.mailgun.apiurl")
    lateinit var mailgunUrl: String

    @Inject
    @ConfigProperty(name = "fi.metatavu.vp.monitoring.mailgun.sender.email")
    lateinit var senderEmail: String

    @Inject
    lateinit var vertx: io.vertx.core.Vertx

    /**
     * Sends an email
     * This is used when policies with emails are triggered
     *
     * @param to email which will receive this email
     * @param subject email subject
     * @param content email content
     */
    fun sendEmail(to: String, subject: String, content: String) {
        val client: WebClient = WebClient.create(vertx)
        client.requestAbs(
            HttpMethod.POST,
            "$mailgunUrl/$domain/messages"
        ).basicAuthentication("api", apiKey)
            .sendForm(
                MultiMap.caseInsensitiveMultiMap()
                    .add("to", to)
                    .add("subject", subject)
                    .add("text", content)
                    .add("from", senderEmail)
            )
    }
}
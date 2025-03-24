package fi.metatavu.vp.monitoring.functional.resources

import com.github.tomakehurst.wiremock.client.WireMock
import fi.metatavu.vp.monitoring.functional.settings.ApiTestSettings
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils
import org.apache.http.message.BasicNameValuePair

/**
 * Mocker for Mailgun API
 *
 *
 * Inspired by https://github.com/sargue/mailgun/blob/master/src/test/java/net/sargue/mailgun/test/BasicTests.java
 *
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */

class MailgunMocker() {
    /**
     * Creates parameter list
     *
     * @param fromEmail from email
     * @param to        to email
     * @param subject   subject
     * @param content   content
     */
    fun createParameterList(fromEmail: String, to: String, subject: String, content: String): List<NameValuePair> {
        return listOf<NameValuePair>(
            BasicNameValuePair("to", to),
            BasicNameValuePair("subject", subject),
            BasicNameValuePair("html", content),
            BasicNameValuePair("from", fromEmail)
        )
    }

    /**
     * Verifies that email with parameters has been sent
     *
     * @param parametersList parameters
     */
    fun verifyMessageSent(parametersList: List<NameValuePair>) {
        val parameters: List<NameValuePair> = ArrayList(parametersList)
        val form = URLEncodedUtils.format(parameters, "UTF-8")
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo(apiUrl)).withRequestBody(WireMock.equalTo(form)))
    }

    /**
     * Returns API URL
     *
     * @return API URL
     */
    private val apiUrl: String
        get() = String.format("/%s/%s/messages", ApiTestSettings.MAILGUN_API_URL_ENDING, ApiTestSettings.MAILGUN_DOMAIN)
}
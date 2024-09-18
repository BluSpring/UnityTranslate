package xyz.bluspring.unitytranslate.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.apache.http.HttpHeaders
import org.apache.http.client.config.CookieSpecs
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object HttpHelper {
    fun post(uri: String, body: JsonObject, headers: Map<String, String> = mapOf()): JsonElement {
        HttpClients.createDefault().use { httpClient ->
            val request = HttpPost(uri)
            request.config = RequestConfig.custom()
                .setConnectTimeout(60_000)
                .setSocketTimeout(60_000)
                .setCookieSpec(CookieSpecs.STANDARD)
                .setConnectionRequestTimeout(60_000)
                .build()

            request.setHeader(HttpHeaders.ACCEPT, "application/json")
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            for ((key, value) in headers) {
                request.setHeader(key, value)
            }

            request.entity = StringEntity(body.toString())

            val response = httpClient.execute(request)

            if (response.statusLine.statusCode / 100 != 2)
                throw Exception("Failed to load $uri (code: ${response.statusLine.statusCode})")

            val responseBody = EntityUtils.toString(response.entity, Charsets.UTF_8)

            return JsonParser.parseString(responseBody)
        }
    }
}
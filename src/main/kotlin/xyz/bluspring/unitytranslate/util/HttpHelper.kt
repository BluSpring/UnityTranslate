package xyz.bluspring.unitytranslate.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL

object HttpHelper {
    fun post(uri: String, body: JsonObject, headers: Map<String, String> = mapOf()): JsonElement {
        val url = URL(uri)
        var connection: HttpURLConnection? = null

        val data = body.toString().toByteArray(Charsets.UTF_8)

        try {
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 60_000
            connection.readTimeout = 60_000

            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("Content-Type", "application/json")
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }

            connection.doOutput = true
            connection.setFixedLengthStreamingMode(data.size)

            val outputStream = BufferedOutputStream(connection.outputStream)

            outputStream.write(data)
            outputStream.flush()
            outputStream.close()

            if (connection.responseCode / 100 != 2) {
                throw Exception("Failed to load $uri (code: ${connection.responseCode})")
            } else {
                val inputStream = BufferedInputStream(connection.inputStream)
                val reader = inputStream.bufferedReader(Charsets.UTF_8)
                val result = JsonParser.parseReader(reader)

                reader.close()
                inputStream.close()

                return result
            }
        } finally {
            connection?.disconnect()
        }
    }
}
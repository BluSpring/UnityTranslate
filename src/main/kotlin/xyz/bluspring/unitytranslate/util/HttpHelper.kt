package xyz.bluspring.unitytranslate.util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object HttpHelper {
    private val client = OkHttpClient()

    fun post(uri: String, body: JsonObject, headers: Map<String, String> = mapOf()): JsonElement {
        val request = Request.Builder().apply {
            post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            header("Accept", "application/json; charset=utf-8")
            header("Content-Type", "application/json; charset=utf-8")
            charset("UTF-8")

            for ((key, value) in headers) {
                header(key, value)
            }
        }
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to load $uri (code: ${response.code})")
            }

            (response.body?.charStream() ?: throw Exception("Failed to load $uri (code: ${response.code})")).use { reader ->
                return JsonParser.parseReader(reader)
            }
        }
    }
}
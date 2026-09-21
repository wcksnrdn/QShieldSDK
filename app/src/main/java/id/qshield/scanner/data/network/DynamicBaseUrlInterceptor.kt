package id.qshield.scanner.data.network

import id.qshield.scanner.data.local.PreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class DynamicBaseUrlInterceptor(
    private val preferencesDataStore: PreferencesDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val baseUrlString = runBlocking { preferencesDataStore.baseUrlFlow.first() }
        val apiKeyString = runBlocking { preferencesDataStore.apiKeyFlow.first() }

        if (baseUrlString.isNotBlank()) {
            val newBaseUrl = baseUrlString.toHttpUrlOrNull()
            if (newBaseUrl != null) {
                val newUrl = request.url.newBuilder()
                    .scheme(newBaseUrl.scheme)
                    .host(newBaseUrl.host)
                    .port(newBaseUrl.port)
                    .build()

                request = request.newBuilder()
                    .url(newUrl)
                    .build()
            }
        }

        if (apiKeyString.isNotBlank()) {
            request = request.newBuilder()
                .addHeader("X-API-Key", apiKeyString)
                .build()
        }

        return chain.proceed(request)
    }
}

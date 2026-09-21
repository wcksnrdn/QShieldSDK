package id.qshield.scanner.data.network

import id.qshield.scanner.BuildConfig
import id.qshield.scanner.data.local.PreferencesDataStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    @OptIn(ExperimentalSerializationApi::class)
    val networkJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun createOkHttpClient(preferencesDataStore: PreferencesDataStore): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        val dynamicBaseUrlInterceptor = DynamicBaseUrlInterceptor(preferencesDataStore)

        return OkHttpClient.Builder()
            .addInterceptor(dynamicBaseUrlInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            // The base URL will be replaced by the interceptor, but Retrofit requires a valid URL at initialization
            .baseUrl("http://localhost/")
            .client(okHttpClient)
            .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}

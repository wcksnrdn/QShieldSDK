package id.qshield.scanner.data.network

import id.qshield.scanner.data.local.PreferencesDataStore
import id.qshield.scanner.data.models.QrisVerificationRequest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class DynamicBaseUrlInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var mockDataStore: PreferencesDataStore
    private lateinit var interceptor: DynamicBaseUrlInterceptor
    private lateinit var okHttpClient: OkHttpClient

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        mockDataStore = mockk()
        
        interceptor = DynamicBaseUrlInterceptor(mockDataStore)

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `intercept changes request host based on DataStore base url`() {
        // Given
        val targetUrl = mockWebServer.url("/")
        val targetBaseUrlStr = "http://${targetUrl.host}:${targetUrl.port}"
        
        every { mockDataStore.baseUrlFlow } returns flowOf(targetBaseUrlStr)
        every { mockDataStore.apiKeyFlow } returns flowOf("")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Success"))

        // Create request with a dummy host
        val request = Request.Builder()
            .url("http://dummy.com/api/test")
            .build()

        // When
        val response = okHttpClient.newCall(request).execute()

        // Then
        assertEquals(200, response.code)
        
        // Check that the request recorded by MockWebServer went to the targetUrl host
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/test", recordedRequest.path)
        assertEquals(targetUrl.host, recordedRequest.requestUrl?.host)
        assertEquals(targetUrl.port, recordedRequest.requestUrl?.port)
    }

    @Test
    fun `base url https tanpa port eksplisit tetap benar`() {
        // Jalur BAWAAN aplikasi: https://qshield.fly.dev, tanpa ":443".
        //
        // Interceptor menyalin scheme, host, DAN port dari URL setelan.
        // Untuk URL tanpa port, HttpUrl mengembalikan port bawaan skemanya,
        // jadi yang disalin 443 — benar untuk https, tapi diam-diam SALAH
        // kalau skemanya tidak ikut tersalin lebih dulu. Dikunci di sini
        // karena inilah satu-satunya jalur yang dipakai di lapangan.
        every { mockDataStore.baseUrlFlow } returns flowOf("https://qshield.fly.dev")
        every { mockDataStore.apiKeyFlow } returns flowOf("kunci-uji")

        val interceptorOnly = DynamicBaseUrlInterceptor(mockDataStore)
        val request = Request.Builder().url("http://localhost/api/v1/verify").build()

        // Rantai palsu: hanya memeriksa permintaan yang DIHASILKAN,
        // tanpa benar-benar menembak jaringan.
        var hasil: okhttp3.Request? = null
        val chain = object : okhttp3.Interceptor.Chain {
            override fun request() = request
            override fun proceed(r: okhttp3.Request): okhttp3.Response {
                hasil = r
                return okhttp3.Response.Builder()
                    .request(r).protocol(okhttp3.Protocol.HTTP_1_1)
                    .code(200).message("OK")
                    .body(okhttp3.ResponseBody.create(null, ""))
                    .build()
            }
            override fun connection() = null
            override fun call() = throw UnsupportedOperationException()
            override fun connectTimeoutMillis() = 0
            override fun withConnectTimeout(t: Int, u: TimeUnit) = this
            override fun readTimeoutMillis() = 0
            override fun withReadTimeout(t: Int, u: TimeUnit) = this
            override fun writeTimeoutMillis() = 0
            override fun withWriteTimeout(t: Int, u: TimeUnit) = this
        }
        interceptorOnly.intercept(chain)

        assertEquals("https", hasil!!.url.scheme)
        assertEquals("qshield.fly.dev", hasil!!.url.host)
        assertEquals(443, hasil!!.url.port)
        assertEquals("/api/v1/verify", hasil!!.url.encodedPath)
        assertEquals("https://qshield.fly.dev/api/v1/verify", hasil!!.url.toString())
        assertEquals("kunci-uji", hasil!!.header("X-API-Key"))
    }

    @Test
    fun `retrofit call uses exact verify path and appends api key header`() {
        val targetUrl = mockWebServer.url("/")
        val targetBaseUrlStr = "http://${targetUrl.host}:${targetUrl.port}"
        
        every { mockDataStore.baseUrlFlow } returns flowOf(targetBaseUrlStr)
        every { mockDataStore.apiKeyFlow } returns flowOf("secret-api-key")

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val retrofit = ApiClient.createRetrofit(okHttpClient)
        val apiService = retrofit.create(QShieldApiService::class.java)

        runBlocking {
            try {
                apiService.verifyQris(
                    QrisVerificationRequest(
                    payload = "test",
                    lat = 0.0,
                    lng = 0.0,
                    accuracy_m = 0f,
                    device_anon_id = "test"
                )
                )
            } catch (e: Exception) {
                // Ignore parsing errors for the mock {} body if any
            }
        }

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/api/v1/verify", recordedRequest.path)
        assertEquals("secret-api-key", recordedRequest.getHeader("X-API-Key"))
    }
}

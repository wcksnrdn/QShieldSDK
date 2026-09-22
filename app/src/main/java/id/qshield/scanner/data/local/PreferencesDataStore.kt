package id.qshield.scanner.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qshield_preferences")

class PreferencesDataStore(private val context: Context) {

    companion object {
        val KEY_UUID = stringPreferencesKey("uuid")
        val KEY_BASE_URL = stringPreferencesKey("base_url")
        val KEY_API_KEY = stringPreferencesKey("api_key")

        /**
         * Server produksi. Dipakai kalau pengguna belum menyetel apa pun.
         *
         * Sebelumnya bawaannya kosong, dan permintaan jatuh ke
         * "http://localhost/" milik Retrofit — yang di HP berarti HP itu
         * sendiri. Aplikasi yang baru dipasang gagal tanpa alasan yang
         * bisa ditebak pemakainya, dan tiap anggota tim harus mengetik
         * alamat yang benar lebih dulu.
         *
         * Alamat ini punya sertifikat Let's Encrypt sungguhan, jadi tidak
         * ada lagi peringatan sertifikat dan tidak perlu satu WiFi dengan
         * laptop siapa pun.
         *
         * Setelan tetap bisa diubah di dialog — itu yang dipakai saat
         * menguji server lokal.
         */
        const val BASE_URL_BAWAAN = "https://qshield.fly.dev"
    }

    val uuidFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_UUID] ?: ""
    }

    val baseUrlFlow: Flow<String> = context.dataStore.data.map { preferences ->
        // Kosong DAN belum pernah diisi sama-sama jatuh ke bawaan; pengguna
        // yang sengaja mengosongkannya berarti ingin kembali ke bawaan.
        preferences[KEY_BASE_URL]?.takeIf { it.isNotBlank() } ?: BASE_URL_BAWAAN
    }

    val apiKeyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_API_KEY] ?: ""
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BASE_URL] = url
        }
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_API_KEY] = apiKey
        }
    }

    suspend fun getOrGenerateUuid(): String {
        var currentUuid = ""
        context.dataStore.edit { preferences ->
            if (preferences[KEY_UUID] == null) {
                preferences[KEY_UUID] = UUID.randomUUID().toString()
            }
            currentUuid = preferences[KEY_UUID]!!
        }
        return currentUuid
    }
}

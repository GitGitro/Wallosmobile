package com.grappim.wallosmobile.core.storage.selfsigned

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import kotlin.concurrent.Volatile

@Single(binds = [SelfSignedCertStorage::class])
internal class SelfSignedCertStorageImpl(private val dataStore: DataStore<Preferences>) : SelfSignedCertStorage {

    @Volatile
    private var cached: Boolean? = null

    override val allowSelfSignedCerts: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[KEY_ALLOW_SELF_SIGNED_CERTS] ?: false }
        .distinctUntilChanged()

    /**
     * Blocks on the *first* read only, then serves the cache: the caller is the JSSE trust
     * callback, which cannot suspend. Two threads racing here both read the same value, so the
     * write is idempotent.
     */
    override val isAllowSelfSignedCerts: Boolean
        get() = cached ?: runBlocking { read() }.also { cached = it }

    override suspend fun setAllowSelfSignedCerts(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ALLOW_SELF_SIGNED_CERTS] = enabled
        }
        cached = enabled
    }

    private suspend fun read(): Boolean = dataStore.data.first()[KEY_ALLOW_SELF_SIGNED_CERTS] ?: false

    private companion object {
        private val KEY_ALLOW_SELF_SIGNED_CERTS = booleanPreferencesKey("allow_self_signed_certs")
    }
}

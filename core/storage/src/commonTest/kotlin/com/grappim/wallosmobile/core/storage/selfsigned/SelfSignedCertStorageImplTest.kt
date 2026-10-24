package com.grappim.wallosmobile.core.storage.selfsigned

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import app.cash.turbine.test
import com.grappim.wallosmobile.core.storage.FakePreferencesDataStore
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelfSignedCertStorageImplTest {

    @Test
    fun `self signed certificates are not allowed when nothing was ever stored`() = runTest {
        val storage = SelfSignedCertStorageImpl(FakePreferencesDataStore())

        storage.allowSelfSignedCerts.test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `setAllowSelfSignedCerts is observed by the flow`() = runTest {
        val storage = SelfSignedCertStorageImpl(FakePreferencesDataStore())

        storage.allowSelfSignedCerts.test {
            assertFalse(awaitItem())

            storage.setAllowSelfSignedCerts(true)
            assertEquals(true, awaitItem())

            storage.setAllowSelfSignedCerts(false)
            assertEquals(false, awaitItem())
        }
    }

    @Test
    fun `a value stored before construction is the first value emitted`() = runTest {
        val stored = mutablePreferencesOf(booleanPreferencesKey("allow_self_signed_certs") to true)

        SelfSignedCertStorageImpl(FakePreferencesDataStore(stored)).allowSelfSignedCerts.test {
            assertEquals(true, awaitItem())
        }
    }

    /** Every write to the shared file re-emits the whole `Preferences` (the key, the URL, a pin). */
    @Test
    fun `writing the same value twice emits once`() = runTest {
        val storage = SelfSignedCertStorageImpl(FakePreferencesDataStore())

        storage.allowSelfSignedCerts.test {
            assertFalse(awaitItem())

            storage.setAllowSelfSignedCerts(true)
            assertEquals(true, awaitItem())

            storage.setAllowSelfSignedCerts(true)
            expectNoEvents()
        }
    }

    /**
     * The JSSE trust callback cannot suspend, so this is the read it actually makes — it has to
     * agree with what the flow emits, from the very first call.
     */
    @Test
    fun `the non-suspending read serves the stored value without a write first`() = runTest {
        val stored = mutablePreferencesOf(booleanPreferencesKey("allow_self_signed_certs") to true)

        assertTrue(SelfSignedCertStorageImpl(FakePreferencesDataStore(stored)).isAllowSelfSignedCerts)
    }

    @Test
    fun `a write updates the cached read`() = runTest {
        val storage = SelfSignedCertStorageImpl(FakePreferencesDataStore())

        storage.setAllowSelfSignedCerts(true)

        assertTrue(storage.isAllowSelfSignedCerts)
    }
}

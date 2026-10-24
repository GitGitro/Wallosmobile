package com.grappim.wallosmobile.core.api

import com.grappim.wallosmobile.core.storage.selfsigned.SelfSignedCertStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Stands in for the DataStore-backed setting: a plain value the test flips when it wants to. */
internal class FakeSelfSignedCertStorage(allowed: Boolean = false) : SelfSignedCertStorage {

    private val allow = MutableStateFlow(allowed)

    override val allowSelfSignedCerts: Flow<Boolean> = allow

    override val isAllowSelfSignedCerts: Boolean
        get() = allow.value

    override suspend fun setAllowSelfSignedCerts(enabled: Boolean) {
        allow.value = enabled
    }
}

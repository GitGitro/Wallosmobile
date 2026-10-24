package com.grappim.wallosmobile.core.storage.selfsigned

import kotlinx.coroutines.flow.Flow

/**
 * The standing preference that lets a connection present a certificate the device would not
 * otherwise verify — a self-hosted instance on HTTPS whose certificate the trust prompt has
 * nothing to offer for, most often because its name does not match how the instance is reached.
 *
 * [isAllowSelfSignedCerts] is deliberately **not** `suspend`: it is read from inside the JSSE
 * trust callback, which cannot suspend (plan §4.5). The implementation has to keep the value
 * cached rather than reaching into DataStore on every handshake.
 */
interface SelfSignedCertStorage {

    /** Defaults to `false`. */
    val allowSelfSignedCerts: Flow<Boolean>

    val isAllowSelfSignedCerts: Boolean

    suspend fun setAllowSelfSignedCerts(enabled: Boolean)
}

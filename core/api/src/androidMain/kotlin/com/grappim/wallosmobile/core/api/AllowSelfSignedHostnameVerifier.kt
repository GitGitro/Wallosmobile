package com.grappim.wallosmobile.core.api

import com.grappim.wallosmobile.core.storage.selfsigned.SelfSignedCertStorage
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSession

/**
 * OkHttp verifies the peer *name* after the chain, independently of the trust manager, so a
 * certificate whose name does not match how the instance is reached still fails the handshake
 * even once the chain is waved through. This one answers for the setting first and leaves
 * everything else to the platform's own verifier.
 */
internal class AllowSelfSignedHostnameVerifier(
    private val selfSignedCertStorage: SelfSignedCertStorage
) : HostnameVerifier {

    override fun verify(hostname: String, session: SSLSession): Boolean =
        selfSignedCertStorage.isAllowSelfSignedCerts ||
            HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session)
}

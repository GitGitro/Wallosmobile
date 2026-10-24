package com.grappim.wallosmobile.core.api

import com.grappim.kit.storage.cert.TrustedCertStorage
import com.grappim.kit.trustmanager.CompositeTrustManager
import com.grappim.wallosmobile.core.storage.selfsigned.SelfSignedCertStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.security.KeyStore
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

actual fun createPlatformHttpClientEngine(
    trustedCertStorage: TrustedCertStorage,
    selfSignedCertStorage: SelfSignedCertStorage
): HttpClientEngine {
    val trustManager = AllowSelfSignedTrustManager(
        delegate = CompositeTrustManager(systemTrustManager(), trustedCertStorage),
        selfSignedCertStorage = selfSignedCertStorage
    )
    val sslContext = SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(trustManager), null)
    }

    return OkHttp.create {
        config {
            // While the setting is off this replaces *chain* trust only; the hostname verifier is
            // the platform's own. While it is on, both wave everything through.
            sslSocketFactory(sslContext.socketFactory, trustManager)
            hostnameVerifier(AllowSelfSignedHostnameVerifier(selfSignedCertStorage))
        }
    }
}

/** A `null` KeyStore means the platform's own trust store, which is what it falls back to. */
private fun systemTrustManager(): X509TrustManager {
    val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    factory.init(null as KeyStore?)
    return factory.trustManagers.filterIsInstance<X509TrustManager>().first()
}

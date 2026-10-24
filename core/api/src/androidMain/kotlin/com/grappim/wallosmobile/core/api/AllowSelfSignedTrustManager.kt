package com.grappim.wallosmobile.core.api

import com.grappim.wallosmobile.core.storage.selfsigned.SelfSignedCertStorage
import java.net.Socket
import java.security.cert.X509Certificate
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLSocket
import javax.net.ssl.X509ExtendedTrustManager

/**
 * Lets every server chain through, unexamined, while the user's allow-self-signed setting is on;
 * delegates everything to [delegate] otherwise. The setting is read live at each handshake, so
 * turning it on takes effect on the very next connection with no client rebuild.
 *
 * Extends [X509ExtendedTrustManager] rather than plain `X509TrustManager` so the `Socket`/
 * `SSLEngine` overloads keep carrying the host down to [delegate], whose pins are per host.
 */
internal class AllowSelfSignedTrustManager(
    private val delegate: X509ExtendedTrustManager,
    private val selfSignedCertStorage: SelfSignedCertStorage
) : X509ExtendedTrustManager() {

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) =
        delegate.checkClientTrusted(chain, authType)


    override fun checkClientTrusted(chain: Array<out X509Certificate?>?, authType: String?, socket: Socket?) {
        delegate.checkClientTrusted(chain, authType, socket)
    }

    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String, engine: SSLEngine?) =
        delegate.checkClientTrusted(chain, authType, engine)

    override fun checkServerTrusted(chain: Array<out X509Certificate?>?, authType: String?, socket: Socket?) {
        if (isAllowed) return
        delegate.checkServerTrusted(chain, authType, socket)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        if (isAllowed) return
        delegate.checkServerTrusted(chain, authType)
    }


    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String, engine: SSLEngine?) {
        if (isAllowed) return
        delegate.checkServerTrusted(chain, authType, engine)
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = delegate.acceptedIssuers

    private val isAllowed: Boolean
        get() = selfSignedCertStorage.isAllowSelfSignedCerts
}

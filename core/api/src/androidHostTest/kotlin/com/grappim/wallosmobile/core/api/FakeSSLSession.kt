package com.grappim.wallosmobile.core.api

import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSessionContext

/**
 * Carries one certificate to the platform hostname verifier and nothing else: [SSLSession] is an
 * interface with no lighter double in the JDK, and the verifier only reads the peer certificates
 * off it. Everything a real session tracks is beyond what is under test here.
 */
internal class FakeSSLSession(private val peerCertificates: Array<Certificate>) : SSLSession {

    override fun getPeerCertificates(): Array<Certificate> = peerCertificates

    /** Not the `SSL_NULL` suite, which the platform's own verifier treats as nothing to check. */
    override fun getCipherSuite(): String = "TLS_AES_128_GCM_SHA256"

    override fun getPeerHost(): String = ""

    override fun getPeerPort(): Int = 443

    override fun getPacketBufferSize(): Int = 0

    override fun getApplicationBufferSize(): Int = 0

    override fun getCreationTime(): Long = 0L

    override fun getLastAccessedTime(): Long = 0L

    override fun isValid(): Boolean = true

    override fun invalidate() = Unit

    override fun getLocalCertificates(): Array<Certificate>? = null

    override fun getValidSeconds(): Long = 0L

    override fun getValue(name: String): Any? = null

    override fun putValue(name: String, value: Any?) = Unit

    override fun removeValue(name: String) = Unit

    override fun getValueNames(): Array<String> = emptyArray()

    override fun getPeerCertificateChain(): Array<X509Certificate>? = null

    override fun getSessionContext(): SSLSessionContext? = null
}

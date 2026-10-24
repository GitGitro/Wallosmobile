package com.grappim.wallosmobile.core.api

import com.grappim.kit.testing.FakeTrustedCertStorage
import com.grappim.kit.trustmanager.CompositeTrustManager
import kotlinx.coroutines.test.runTest
import java.security.cert.CertificateException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The delegate under the wrapper is the real [CompositeTrustManager], so these cover the
 * wrapper's own decision only: what the setting short-circuits, and what it leaves alone. A host
 * test for the same reason as [CompositeTrustManagerTest] — `javax.net.ssl` is the JDK's, and
 * nothing here opens a socket.
 *
 * Test names are camelCase because detekt's `FunctionNaming` exclusions cover `commonTest` and
 * not `androidHostTest` — the same reason `CompositeTrustManagerTest` reads that way.
 */
class AllowSelfSignedTrustManagerTest {

    private val certificate = FakeX509Certificate(byteArrayOf(1, 2, 3))

    @Test
    fun anAllowedServerChainIsAcceptedWithoutConsultingTheDeviceTrustStore() {
        val deviceStore = FakeX509TrustManager()
        val sut = AllowSelfSignedTrustManager(
            delegate = CompositeTrustManager(deviceStore, FakeTrustedCertStorage()),
            selfSignedCertStorage = FakeSelfSignedCertStorage(allowed = true)
        )

        sut.checkServerTrusted(arrayOf(certificate), AUTH_TYPE, sslEngine(HOST))

        assertFalse(deviceStore.checkServerTrustedCalled)
    }

    @Test
    fun aDisallowedServerChainStillReachesTheDeviceTrustStore() {
        val deviceStore = FakeX509TrustManager()
        val sut = AllowSelfSignedTrustManager(
            delegate = CompositeTrustManager(deviceStore, FakeTrustedCertStorage()),
            selfSignedCertStorage = FakeSelfSignedCertStorage()
        )

        assertFailsWith<CertificateException> {
            sut.checkServerTrusted(arrayOf(certificate), AUTH_TYPE, sslEngine(HOST))
        }

        assertTrue(deviceStore.checkServerTrustedCalled)
    }

    /** The setting is read at each handshake, so one manager serves both answers. */
    @Test
    fun turningTheSettingOnTakesEffectOnTheSameManagerWithoutARebuild() = runTest {
        val deviceStore = FakeX509TrustManager()
        val storage = FakeSelfSignedCertStorage()
        val sut = AllowSelfSignedTrustManager(CompositeTrustManager(deviceStore, FakeTrustedCertStorage()), storage)

        assertFailsWith<CertificateException> {
            sut.checkServerTrusted(arrayOf(certificate), AUTH_TYPE, sslEngine(HOST))
        }

        storage.setAllowSelfSignedCerts(true)
        sut.checkServerTrusted(arrayOf(certificate), AUTH_TYPE, sslEngine(HOST))

        assertFalse(deviceStore.checkServerTrustedCalled)
    }

    /**
     * A real JDK-provided engine, not a fake: [SSLEngine] carries no handshake state until one is
     * started, so its constructor-supplied [SSLEngine.getPeerHost] is enough to drive the
     * host-taking overload without hand-implementing the platform's own abstract class.
     */
    private fun sslEngine(host: String): SSLEngine =
        SSLContext.getInstance("TLS").apply { init(null, null, null) }.createSSLEngine(host, 443)

    private companion object {
        private const val HOST = "wallos.example.com"
        private const val AUTH_TYPE = "RSA"
    }
}

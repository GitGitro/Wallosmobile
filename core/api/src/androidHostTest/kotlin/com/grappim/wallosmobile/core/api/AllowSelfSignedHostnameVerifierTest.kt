package com.grappim.wallosmobile.core.api

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The verifier the engine installs reads the setting first and the platform's own rules second,
 * so these prove both halves: a name mismatch waved through only while the setting is on, and
 * the platform's verdict left untouched while it is off. `FakeX509Certificate`'s subject is
 * `CN=wallos.example.com`, with no SANs, so the platform verifier answers by that CN.
 */
class AllowSelfSignedHostnameVerifierTest {

    private val certificate = FakeX509Certificate(byteArrayOf(1, 2, 3))

    @Test
    fun aNameMismatchIsAcceptedWhileTheSettingIsOn() {
        val sut = AllowSelfSignedHostnameVerifier(FakeSelfSignedCertStorage(allowed = true))

        assertTrue(sut.verify(IP, FakeSSLSession(arrayOf(certificate))))
    }

    /** The case the switch exists for: the instance is reached by an address the certificate never names. */
    @Test
    fun aNameMismatchIsStillRejectedWhileTheSettingIsOff() {
        val sut = AllowSelfSignedHostnameVerifier(FakeSelfSignedCertStorage())

        assertFalse(sut.verify(IP, FakeSSLSession(arrayOf(certificate))))
    }

    @Test
    fun aMatchingNameIsStillAcceptedWhileTheSettingIsOff() {
        val sut = AllowSelfSignedHostnameVerifier(FakeSelfSignedCertStorage())

        assertTrue(sut.verify(HOST, FakeSSLSession(arrayOf(certificate))))
    }

    private companion object {
        private const val HOST = "wallos.example.com"
        private const val IP = "10.0.2.2"
    }
}

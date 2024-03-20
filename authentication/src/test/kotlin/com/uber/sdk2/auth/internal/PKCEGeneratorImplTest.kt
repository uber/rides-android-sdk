package com.uber.sdk2.auth.internal

import com.uber.sdk2.auth.RobolectricTestBase
import org.junit.Assert.assertNotNull
import org.junit.Test

class PKCEGeneratorImplTest: RobolectricTestBase() {
    @Test
    fun testGenerateCodeVerifier() {
        val codeVerifier = PKCEGeneratorImpl.generateCodeVerifier()
        assert(codeVerifier.isNullOrBlank().not())
    }

    @Test
    fun testGenerateCodeChallenge() {
        val codeVerifier = PKCEGeneratorImpl.generateCodeVerifier()
        val codeChallenge = PKCEGeneratorImpl.generateCodeChallenge(codeVerifier)
        assert(codeChallenge.isNullOrBlank().not())
    }
}
/**
 * Copyright (c) 2024 Uber Technologies, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.uber.sdk2.auth.internal

import android.util.Base64
import com.uber.sdk2.auth.PKCEGenerator
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object PKCEGeneratorImpl : PKCEGenerator {
  override fun generateCodeVerifier(): String {
    val sr = SecureRandom()
    val code = ByteArray(BYTE_ARRAY_SIZE)
    sr.nextBytes(code)
    return Base64.encodeToString(code, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
  }

  override fun generateCodeChallenge(codeVerifier: String): String {
    val bytes = codeVerifier.toByteArray(StandardCharsets.US_ASCII)
    return try {
      val md = MessageDigest.getInstance(SHA_256)
      md.update(bytes, 0, bytes.size)
      val digest = md.digest()
      Base64.encodeToString(digest, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    } catch (e: NoSuchAlgorithmException) {
      throw IllegalStateException("SHA-256 is not supported", e)
    }
  }

  private const val BYTE_ARRAY_SIZE: Int = 32
  private const val SHA_256 = "SHA-256"
}

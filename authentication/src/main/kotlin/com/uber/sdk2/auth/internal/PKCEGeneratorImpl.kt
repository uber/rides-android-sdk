/*
 * Copyright (C) 2024. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

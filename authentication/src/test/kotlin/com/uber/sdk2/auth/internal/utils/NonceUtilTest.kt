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
package com.uber.sdk2.auth.internal.utils

import com.uber.sdk2.auth.RobolectricTestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NonceUtilTest : RobolectricTestBase() {

  private fun buildJwt(payloadJson: String): String {
    val header =
      android.util.Base64.encodeToString(
        """{"alg":"RS256","typ":"JWT"}""".toByteArray(),
        android.util.Base64.URL_SAFE or
          android.util.Base64.NO_WRAP or
          android.util.Base64.NO_PADDING,
      )
    val payload =
      android.util.Base64.encodeToString(
        payloadJson.toByteArray(),
        android.util.Base64.URL_SAFE or
          android.util.Base64.NO_WRAP or
          android.util.Base64.NO_PADDING,
      )
    return "$header.$payload.fake-signature"
  }

  @Test
  fun `extractNonceFromIdToken returns nonce when present`() {
    val jwt = buildJwt("""{"sub":"user123","nonce":"my-nonce-value"}""")
    assertEquals("my-nonce-value", NonceUtil.extractNonceFromIdToken(jwt))
  }

  @Test
  fun `extractNonceFromIdToken returns null when nonce is absent`() {
    val jwt = buildJwt("""{"sub":"user123"}""")
    assertNull(NonceUtil.extractNonceFromIdToken(jwt))
  }

  @Test
  fun `extractNonceFromIdToken returns null for malformed JWT`() {
    assertNull(NonceUtil.extractNonceFromIdToken("not-a-jwt"))
  }

  @Test
  fun `extractNonceFromIdToken returns null for two-segment token`() {
    assertNull(NonceUtil.extractNonceFromIdToken("header.payload"))
  }

  @Test
  fun `extractNonceFromIdToken returns null for invalid base64 payload`() {
    assertNull(NonceUtil.extractNonceFromIdToken("header.!!!invalid!!!.sig"))
  }
}

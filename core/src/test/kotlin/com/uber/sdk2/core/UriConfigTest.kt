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
package com.uber.sdk2.core

import com.uber.sdk2.core.config.UriConfig
import com.uber.sdk2.core.config.UriConfig.CLIENT_ID_PARAM
import com.uber.sdk2.core.config.UriConfig.PLATFORM_PARAM
import com.uber.sdk2.core.config.UriConfig.REDIRECT_PARAM
import com.uber.sdk2.core.config.UriConfig.RESPONSE_TYPE_PARAM
import com.uber.sdk2.core.config.UriConfig.SCOPE_PARAM
import com.uber.sdk2.core.config.UriConfig.SDK_VERSION_PARAM
import com.uber.sdk2.core.config.UriConfig.UNIVERSAL_AUTHORIZE_PATH
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class UriConfigTest : RobolectricTestBase() {
  @Test
  fun `assembleUri should return correct uri`() {
    val clientId = "clientI"
    val responseType = "responseType"
    val redirectUri = "redirectUri"
    val scopes = "scopes"
    val uri = UriConfig.assembleUri(clientId, responseType, redirectUri, scopes = scopes)
    println(uri.toString())
    assertEquals("auth.uber.com", uri.authority)
    assertEquals("https", uri.scheme)
    assertEquals("/$UNIVERSAL_AUTHORIZE_PATH", uri.path)
    assertEquals(clientId, uri.getQueryParameter(CLIENT_ID_PARAM))
    assertEquals(responseType.lowercase(Locale.US), uri.getQueryParameter(RESPONSE_TYPE_PARAM))
    assertEquals(redirectUri, uri.getQueryParameter(REDIRECT_PARAM))
    assertEquals(scopes, uri.getQueryParameter(SCOPE_PARAM))
    assertEquals(BuildConfig.VERSION_NAME, uri.getQueryParameter(SDK_VERSION_PARAM))
    assertEquals("android", uri.getQueryParameter(PLATFORM_PARAM))
  }

  @Test
  fun `getEndpointHost should return correct host`() {
    assertEquals("https://api.uber.com", UriConfig.getEndpointHost())
  }

  @Test
  fun `getAuthHost should return correct host`() {
    assertEquals("https://auth.uber.com", UriConfig.getAuthHost())
  }
}

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
package com.uber.sdk2.core

import com.uber.sdk2.core.config.UriConfig
import com.uber.sdk2.core.config.UriConfig.CLIENT_ID_PARAM
import com.uber.sdk2.core.config.UriConfig.CODE_CHALLENGE_METHOD
import com.uber.sdk2.core.config.UriConfig.CODE_CHALLENGE_METHOD_VAL
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
    assertEquals(CODE_CHALLENGE_METHOD_VAL, uri.getQueryParameter(CODE_CHALLENGE_METHOD))
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

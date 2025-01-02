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
package com.uber.sdk2.core.config

import android.net.Uri
import com.uber.sdk2.core.BuildConfig
import com.uber.sdk2.core.config.UriConfig.EndpointRegion.DEFAULT
import com.uber.sdk2.core.config.UriConfig.Environment.API
import com.uber.sdk2.core.config.UriConfig.Environment.AUTH
import com.uber.sdk2.core.config.UriConfig.Scheme.HTTPS
import java.util.Locale

object UriConfig {

  enum class Scheme(val scheme: String) {
    HTTP("http"),
    HTTPS("https")
  }

  /**
   * An Uber API Environment. See [Sandbox](https://developer.uber.com/v1/sandbox) for more
   * information.
   */
  enum class Environment(val subDomain: String) {
    API("api"),
    AUTH("auth"),
  }

  enum class EndpointRegion(
    /** @return domain to use. */
    val domain: String
  ) {
    DEFAULT("uber.com")
  }

  fun assembleUri(
    clientId: String,
    responseType: String,
    redirectUri: String,
    environment: Environment = AUTH,
    path: String = UNIVERSAL_AUTHORIZE_PATH,
    scopes: String? = null,
  ): Uri {
    val builder = Uri.Builder()
    builder
      .scheme(HTTPS.scheme)
      .authority(environment.subDomain + "." + DEFAULT.domain)
      .appendEncodedPath(UNIVERSAL_AUTHORIZE_PATH)
      .appendQueryParameter(CLIENT_ID_PARAM, clientId)
      .appendQueryParameter(RESPONSE_TYPE_PARAM, responseType.lowercase(Locale.US))
      .appendQueryParameter(REDIRECT_PARAM, redirectUri)
      .appendQueryParameter(SCOPE_PARAM, scopes)
      .appendQueryParameter(SDK_VERSION_PARAM, BuildConfig.VERSION_NAME)
      .appendQueryParameter(PLATFORM_PARAM, "android")
    return builder.build()
  }

  /** Gets the endpoint host used to hit the Uber API. */
  fun getEndpointHost(): String = "${HTTPS.scheme}://${API.subDomain}.${DEFAULT.domain}"

  /** Gets the login host used to sign in to the Uber API. */
  fun getAuthHost(): String = "${HTTPS.scheme}://${AUTH.subDomain}.${DEFAULT.domain}"

  const val CLIENT_ID_PARAM = "client_id"
  const val UNIVERSAL_AUTHORIZE_PATH = "oauth/v2/universal/authorize"
  const val AUTHORIZE_PATH = "oauth/v2/authorize"
  const val REDIRECT_PARAM = "redirect_uri"
  const val RESPONSE_TYPE_PARAM = "response_type"
  const val SCOPE_PARAM = "scope"
  const val PLATFORM_PARAM = "sdk"
  const val SDK_VERSION_PARAM = "sdk_version"
  const val CODE_CHALLENGE_PARAM = "code_challenge"
  const val REQUEST_URI = "request_uri"
  const val CODE_CHALLENGE_METHOD = "code_challenge_method"
  const val CODE_CHALLENGE_METHOD_VAL = "S256"
  const val PROMPT_PARAM = "prompt"
}

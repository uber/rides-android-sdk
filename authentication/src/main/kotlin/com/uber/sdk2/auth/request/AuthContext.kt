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
package com.uber.sdk2.auth.request

import android.os.Parcelable
import com.uber.sdk2.core.config.UriConfig
import kotlinx.parcelize.Parcelize

/**
 * Represents the context of the authentication request needed for Uber to authenticate the user.
 *
 * @param authDestination The destination app to authenticate the user.
 * @param authType The type of authentication to perform.
 * @param options Optional configuration for the authentication request.
 */
@Parcelize
data class AuthContext(
  val authDestination: AuthDestination = AuthDestination.CrossAppSso(),
  val authType: AuthType = AuthType.PKCE(),
  val options: AuthOptions = AuthOptions(),
) : Parcelable {

  val prefillInfo: PrefillInfo?
    get() = options.prefillInfo

  val prompt: Prompt?
    get() = options.prompt

  val environment: UriConfig.UberEnvironment
    get() = options.environment

  val nonce: String?
    get() = options.nonce

  @Deprecated(
    message = "Use the constructor with AuthOptions instead.",
    replaceWith =
      ReplaceWith(
        "AuthContext(authDestination, authType, AuthOptions(prefillInfo, prompt, environment, nonce))"
      ),
  )
  @JvmOverloads
  constructor(
    authDestination: AuthDestination = AuthDestination.CrossAppSso(),
    authType: AuthType = AuthType.PKCE(),
    prefillInfo: PrefillInfo? = null,
    prompt: Prompt? = null,
    environment: UriConfig.UberEnvironment = UriConfig.UberEnvironment.PRODUCTION,
    nonce: String? = null,
  ) : this(
    authDestination = authDestination,
    authType = authType,
    options = AuthOptions(prefillInfo, prompt, environment, nonce),
  )
}

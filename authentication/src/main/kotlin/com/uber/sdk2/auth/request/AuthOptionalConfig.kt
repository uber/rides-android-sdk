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
 * Optional configuration for an authentication request.
 *
 * @param prefillInfo The prefill information to be used for the authentication.
 * @param prompt The [Prompt] to be used for the authentication.
 * @param environment The [UriConfig.UberEnvironment] to target for OAuth flows. Defaults to
 *   [UriConfig.UberEnvironment.PRODUCTION].
 * @param nonce An optional, opaque, single-use string sent on the `/authorize` request. Required by
 *   the server when `openid` is one of the requested scopes; the same value is returned as the
 *   `nonce` claim of the issued ID token and must be validated by the caller's backend to mitigate
 *   token replay.
 */
@Parcelize
data class AuthOptionalConfig(
  val prefillInfo: PrefillInfo? = null,
  val prompt: Prompt? = null,
  val environment: UriConfig.UberEnvironment = UriConfig.UberEnvironment.PRODUCTION,
  val nonce: String? = null,
) : Parcelable

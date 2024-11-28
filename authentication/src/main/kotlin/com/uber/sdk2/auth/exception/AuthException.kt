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
package com.uber.sdk2.auth.exception

/** Represents the exception that occurred during the authentication request. */
sealed class AuthException(override val message: String) : RuntimeException(message) {
  /** Represents the exception that occurred due to server error. */
  data class ServerError(override val message: String) : AuthException(message)

  /** Represents the exception that occurred due to client error. */
  data class ClientError(override val message: String) : AuthException(message)

  /** Represents the exception that occurred due to network error. */
  data class NetworkError(override val message: String) : AuthException(message)

  companion object {
    internal const val CANCELED: String = "User Canceled"

    internal const val SCOPE_NOT_PROVIDED: String = "Scope not provided in the sso config file"

    internal const val REDIRECT_URI_NOT_PROVIDED: String =
      "Redirect URI not provided in the sso config file"

    internal const val AUTH_CODE_INVALID = "Invalid auth code"

    internal const val AUTH_CODE_NOT_PRESENT = "Auth code is not present"

    internal const val NULL_RESPONSE = "Response not received"

    internal const val UNKNOWN = "Unknown error occurred"
  }
}

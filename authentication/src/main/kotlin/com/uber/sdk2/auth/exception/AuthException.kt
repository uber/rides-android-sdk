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

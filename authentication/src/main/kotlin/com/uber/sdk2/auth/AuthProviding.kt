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
package com.uber.sdk2.auth

import com.uber.sdk2.auth.response.AuthResult

/** Provides a way to authenticate the user using SSO flow. */
interface AuthProviding {
  /**
   * Executes the SSO flow.
   *
   * @param ssoLink The SSO link to execute.
   * @return The result from the authentication flow encapsulated in [AuthResult]
   */
  suspend fun authenticate(): AuthResult

  /** Handles the authentication code received from the SSO flow via deeplink. */
  fun handleAuthCode(authCode: String)

  /** Checks if the authentication is in progress. */
  fun isAuthInProgress(): Boolean
}

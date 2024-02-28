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
package com.uber.sdk2.auth.api.config

/**
 * Represents the type of authentication to perform.
 *
 * @see AuthContext
 */
sealed class AuthType {
  /** The authorization code flow. */
  object AuthCode : AuthType()

  /** The proof key for code exchange (PKCE) flow. This is the recommended flow for mobile apps. */
  object PKCE : AuthType()
}

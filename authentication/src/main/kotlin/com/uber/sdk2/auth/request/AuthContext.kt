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
package com.uber.sdk2.auth.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents the context of the authentication request needed for Uber to authenticate the user.
 *
 * @param authDestination The destination app to authenticate the user.
 * @param authType The type of authentication to perform.
 * @param prefillInfo The prefill information to be used for the authentication.
 * @param scopes The scopes to request for the authentication.
 */
@Parcelize
data class AuthContext(
  val authDestination: AuthDestination = AuthDestination.CrossAppSso(),
  val authType: AuthType = AuthType.PKCE(),
  val prefillInfo: PrefillInfo? = null,
) : Parcelable

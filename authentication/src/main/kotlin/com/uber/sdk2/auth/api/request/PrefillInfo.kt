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
package com.uber.sdk2.auth.api.request

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

/**
 * Provides a way to prefill the user's information in the authentication flow.
 *
 * @param email The email to prefill.
 * @param firstName The first name to prefill.
 * @param lastName The last name to prefill.
 * @param phoneNumber The phone number to prefill.
 */
@Parcelize
data class PrefillInfo(
  @Json(name = "email") val email: String,
  @Json(name = "first_name") val firstName: String,
  @Json(name = "last_name") val lastName: String,
  @Json(name = "phone") val phoneNumber: String,
) : Parcelable

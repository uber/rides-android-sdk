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
 * Provides different apps that could be used for authentication using SSO flow.
 *
 * @param packages The list of packages that could be used for authentication.
 */
@Parcelize
sealed class CrossApp(val packages: List<String>) : Parcelable {
  /** The Eats app. */
  data object Eats : CrossApp(EATS_APPS)

  /** The Rider app. */
  data object Rider : CrossApp(RIDER_APPS)

  /** The Driver app. */
  data object Driver : CrossApp(DRIVER_APPS)

  companion object {
    /** The list of all driver apps. */
    private val DRIVER_APPS =
      listOf("com.ubercab.driver", "com.ubercab.driver.debug", "com.ubercab.driver.internal")

    /** The list of all rider apps. */
    private val RIDER_APPS =
      listOf("com.ubercab", "com.ubercab.presidio.development", "com.ubercab.rider.internal")

    /** The list of all Eats apps. */
    private val EATS_APPS =
      listOf("com.ubercab.eats", "com.ubercab.eats.debug", "com.ubercab.eats.internal")
  }
}
